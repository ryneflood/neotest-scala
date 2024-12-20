package neotest

import zio.*
import zio.json.*

final case class Options(
    runner: String,
    framework: String,
    outputDirectory: os.Path,
    project: String,
    testSuites: List[String],
    test: Option[String],
    kind: String
)

// this will be the test runner like SBT, bloop, mill,, scala-cli etc.
trait TestRunnerAdapter:
  def runTests(options: Options): Task[List[TestSuite]]

final case class BloopRunnerAdapter() extends TestRunnerAdapter:
  def runTests(options: Options): Task[List[TestSuite]] =
    val singleTestCommand =
      if options.framework == "zio-test" then
        options.test match
          case Some(test) => List("--", "-t", test, "-renderer", "intellij")
          case None       => List("--", "-renderer", "intellij")
      else if options.framework == "scalatest" then
        options.test match
          case Some(test) => 
            if options.kind == "test" then
              List("--", "-t", test, "-u", "/tmp/scalatest-out/")
            else List("--", "-z", test, "-u", "/tmp/scalatest-out/")
          case None       => List.empty
      else
        options.test match
          case Some(test) => List("--", "-z", test)
          case None       => List.empty

    if singleTestCommand.nonEmpty then
      ZIO.attempt {
        val testSuitesSubcommand =
          options.testSuites.map(suite => List("-o", suite)).flatten

        // delete all files in the /tmp/scalatest-out directory
        os.remove.all(os.root / "tmp" / "scalatest-out")
        
        // make sure the /tmp/scalatest-out directory exists
        os.makeDir.all(os.root / "tmp" / "scalatest-out")

        val command = List(
          "bloop",
          "test",
          options.project
        ) ::: testSuitesSubcommand ::: singleTestCommand
        
        val commandResult: os.CommandResult = os
          .proc(command)
          .call(check = false)

        val output = commandResult.exitCode match
          case 0 | 32 => commandResult.out.lines().toList
          case _      => throw Throwable(commandResult.err.text())

        if options.framework == "zio-test" then
          List(ZiotestTeamcityOutputParser.parseTestOutput(options.testSuites.head)(output))
        else if options.framework == "scalatest" then

          ScalatestXmlTestOutputParser.loadAndParseXmlFiles(options.testSuites.head)
        else MunitTestOutputParser.parseTestOutput(options.testSuites)(output)
      }
    else
      ZIO
        .foreach(options.testSuites) { suite =>
          ZIO
            .succeed {
              val command = List(
                "bloop",
                "test",
                options.project,
                "-o",
                suite
              )

              val extraOptions =
                if options.framework == "scalatest" then
                  List("--", "-u", "/tmp/scalatest-out/")
                else if options.framework == "zio-test" then
                  List("--", "-renderer", "intellij")
                else List.empty

              val commandResult: os.CommandResult =
                os.proc(command ::: extraOptions).call(check = false)

              commandResult.exitCode match
                case 0 | 32 => commandResult.out.lines().toList
                case _      => throw Throwable(commandResult.err.text())
            }
            .map(output =>
              if options.framework == "zio-test" then
                List(ZiotestTeamcityOutputParser.parseTestOutput(suite)(output))
              else if options.framework == "munit" then
                MunitTestOutputParser.parseTestOutput(List(suite))(output)
              else
                ScalatestXmlTestOutputParser.loadAndParseXmlFiles(suite)
            )
        }
        .map(_.flatten)

final case class ScalaCliRunnerAdapter() extends TestRunnerAdapter:
  private[neotest] def prepareCommand(
      testSuite: String,
      test: Option[String]
  ): List[String] =
    List(
      "scala-cli",
      "test",
      ".",
      "--test-only",
      testSuite,
      "--suppress-outdated-dependency-warning"
    ) ++ test.map(t => List("--", t)).getOrElse(List.empty)

  def runTests(options: Options): Task[List[TestSuite]] =
    options.test match
      case Some(test) =>
        // we want to run a single test, which means we'll only have one Test Suite to run
        val command = prepareCommand(options.testSuites.head, Some(test))

        val commandResult: os.CommandResult = os
          .proc(command)
          .call(check = false)

        val output = commandResult.exitCode match
          case 0 | 1 => commandResult.out.lines().toList
          case _     => throw Throwable(commandResult.err.text())

        val testOutputParser =
          if options.framework == "zio-test" then
        ZiotestTeamcityOutputParser.parseTestOutput(options.testSuites.head) andThen ((x) => List(x))
          else MunitTestOutputParser.parseTestOutput(options.testSuites)

        ZIO.succeed(testOutputParser(output))

      case None =>
        // we want to run at least one, maybe many tests suites
        ZIO
          .foreachPar(options.testSuites) { suite =>
            ZIO
              .succeed {
                val command = prepareCommand(suite, None)

                val commandResult: os.CommandResult =
                  os.proc(command).call(check = false)

                commandResult.exitCode match
                  case 0 | 1 => commandResult.out.lines().toList
                  case _     => throw Throwable(commandResult.err.text())

              }
              .map(output =>
                if options.framework == "zio-test" then
                  List(ZiotestTeamcityOutputParser.parseTestOutput(suite)(output))
                else MunitTestOutputParser.parseTestOutput(List(suite))(output)
              )
          }
          .map(_.flatten)

object Runner extends zio.ZIOAppDefault:
  private def getRunner(runner: String): TestRunnerAdapter =
    runner match
      case "bloop"     => BloopRunnerAdapter()
      case "scala-cli" => ScalaCliRunnerAdapter()
      case _           => throw Throwable(s"Unsupported runner: $runner")

  override def run =
    for
      options <- CommandlineArgumentsParser.make
      runner = getRunner(options.runner)
      output <- runner.runTests(options)
        .foldZIO(
          throwable => {
            println("!!!!!Throwable message is!!!!!")
            println(throwable.getMessage())
            println("!!!!!====================!!!!!")
            ZIO.succeed(List.empty)
          },
          output => ZIO.succeed(output)
        )
      result <- ZIO.succeed:
        TestOutputWriter.writeSuitesToOutputFiles(os.Path("/tmp"), output)
      _ <- Console.printLine(result.toJsonPretty)
    yield ()
