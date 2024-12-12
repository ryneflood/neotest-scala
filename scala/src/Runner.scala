package neotest

import zio.*
import zio.json.*

final case class Options(
  runner: String,
  framework: String,
  outputDirectory: os.Path,
  project: String,
  testSuites: List[String],
  test: Option[String]
)

// this will be the test runner like SBT, bloop, mill,, scala-cli etc.
trait TestRunnerAdapter:
  def runTests(options: Options): Task[List[TestSuite]]

final case class BloopRunnerAdapter() extends TestRunnerAdapter:
  def runTests(options: Options): Task[List[TestSuite]] =
      val singleTestCommand = if options.framework == "zio-test" || options.framework == "scalatest" then
        options.test match
          case Some(test) => List("--", "-t", test)
          case None       => List.empty
      else
        options.test match
          case Some(test) => List("--", "-z", test)
          case None       => List.empty

      if singleTestCommand.nonEmpty then
        ZIO.attempt {
          val testSuitesSubcommand =
            options.testSuites.map(suite => List("-o", suite)).flatten
          
          val command = List(
            "bloop",
            "test",
            options.project
          ) ::: testSuitesSubcommand ::: singleTestCommand

          println(command.mkString(" "))

          val commandResult: os.CommandResult = os
            .proc(command)
            .call(check = false)

          val output = commandResult.exitCode match
            case 0 | 32 => commandResult.out.lines().toList
            case _      => throw Throwable(commandResult.err.text())

          println("Output: " + output)

          val testOutputParser = if options.framework == "zio-test" then
            ZioTestTestOutputParser.parseTestOutput(options.testSuites.head)
          else
            MunitTestOutputParser.parseTestOutput(options.testSuites)
            
          testOutputParser(output)
        }
      else
        ZIO.foreach(options.testSuites) {
          suite =>
          ZIO.succeed {
            val command = List(
              "bloop",
              "test",
              options.project,
              "-o",
              suite
            )

            val commandResult: os.CommandResult = os.proc(command).call(check = false)
            
            commandResult.exitCode match
              case 0 | 32 => commandResult.out.lines().toList
              case _      => throw Throwable(commandResult.err.text())
            
          }
            .map(output => 
                if options.framework == "zio-test" then
                  ZioTestTestOutputParser.parseTestOutput(suite)(output)
                else
                  MunitTestOutputParser.parseTestOutput(List(suite))(output)
                )
        }.map(_.flatten)


final case class ScalaCliRunnerAdapter() extends TestRunnerAdapter:
  private[neotest] def prepareCommand(testSuite: String, test: Option[String]): List[String] =
    List(
      "scala-cli",
      "test",
      ".",
      "--test-only",
      testSuite,
      "--suppress-outdated-dependency-warning",
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
          case _      => throw Throwable(commandResult.err.text())
          
        val testOutputParser = if options.framework == "zio-test" then
          ZioTestTestOutputParser.parseTestOutput(options.testSuites.head)
        else
          MunitTestOutputParser.parseTestOutput(options.testSuites)
            
        ZIO.succeed(testOutputParser(output))
        
      case None =>
        // we want to run at least one, maybe many tests suites
        ZIO.foreachPar(options.testSuites) {
          suite =>
          ZIO.succeed {
            val command = prepareCommand(suite, None)

            val commandResult: os.CommandResult = os.proc(command).call(check = false)
            
            commandResult.exitCode match
              case 0 | 1 => commandResult.out.lines().toList
              case _      => throw Throwable(commandResult.err.text())
            
          }
            .map(output => 
                if options.framework == "zio-test" then
                  ZioTestTestOutputParser.parseTestOutput(suite)(output)
                else
                  MunitTestOutputParser.parseTestOutput(List(suite))(output)
                )
        }.map(_.flatten)

object Runner extends zio.ZIOAppDefault:
  private def getRunner(runner: String): TestRunnerAdapter =
    runner match
      case "bloop" => BloopRunnerAdapter()
      case "scala-cli" => ScalaCliRunnerAdapter()
      case _ => throw Throwable(s"Unsupported runner: $runner")

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
