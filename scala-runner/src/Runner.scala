package neotest

import zio.*
import zio.json.*

final case class Options(
  runner: String,
  framework: String,
  outputDirectory: os.Path,
  project: String,
  testSuites: List[String],
  singleTest: Option[String]
)

// this will be adapters like munit, ZioTest, ScalaTest, etc.
// trait TestFrameworkAdapter:
//   def parseTestOutput(project: String, testSuites: List[String], singleTest: Option[String]): List[TestSuite]

// this will be the test runner like SBT, bloop, mill,, scala-cli etc.
trait TestRunnerAdapter:
  def runTests(options: Options): Task[List[String]]

final case class BloopRunnerAdapter() extends TestRunnerAdapter:
  def runTests(options: Options): Task[List[String]] =
    ZIO.attempt {
      val testSuitesSubcommand =
        options.testSuites.map(suite => List("-o", suite)).flatten

      val singleTestCommand = if options.framework == "zio-test" then
        options.singleTest match
          case Some(test) => 
            // remove the test suite from the test name
            val testSuiteName = options.testSuites(0)
            val updatedTestName = test.drop(testSuiteName.length + 1)
            
            List("--", "-t", updatedTestName)
            
          case None       => List.empty
      else
        options.singleTest match
          case Some(test) => List("--", "-z", test)
          case None       => List.empty
      

      val command = List(
        "bloop",
        "test",
        options.project
      ) ::: testSuitesSubcommand ::: singleTestCommand

      // println(command.mkString(" "))

      val commandResult: os.CommandResult = os
        .proc(command)
        .call(check = false)

      commandResult.exitCode match
        case 0 | 32 => commandResult.out.lines().toList
        case _      => throw Throwable(commandResult.err.text())
    }

final case class ScalaCliRunnerAdapter() extends TestRunnerAdapter:
  def runTests(options: Options): Task[List[String]] =
      val testSuitesSubcommand = options.singleTest match
        case Some(_) => List.empty
        case None => 
          options.testSuites.map: suite =>
            List(
              "scala-cli",
              "test",
              ".",
              "--test-only",
              s"${suite}*"
            )

      if testSuitesSubcommand.isEmpty then
        ZIO.attempt {
          val singleTestCommand = options.singleTest match
            case Some(test) => List("--", test)
            case None       => List.empty
            
          val command = List(
            "scala-cli",
            "test",
            ".",
          ) ::: singleTestCommand
          
          val commandResult: os.CommandResult = os
            .proc(command)
            .call(check = false)

          commandResult.exitCode match
            case 0 | 1 => {
              commandResult.out.lines().toList
            }
            case _      => throw Throwable(commandResult.err.text())
        }
      else
        ZIO.foreachPar(testSuitesSubcommand): command =>
          ZIO.attempt {
            val commandResult: os.CommandResult = os
              .proc(command)
              .call(check = false)
              
            commandResult.exitCode match
              case 0 | 1 => {
                commandResult.out.lines().toList
              }
              case _      => throw Throwable(commandResult.err.text())
          }
        .map(_.flatten)

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
      testOutputParser = options.framework match
        case "munit" => MunitTestOutputParser
        case "zio-test" => ZioTestTestOutputParser
        case _ => throw Throwable(s"Unsupported framework: ${options.framework}")
      parsedOutput = testOutputParser.parseTestOutput(options.testSuites)(output)
      result <- ZIO.succeed:
        TestOutputWriter.writeSuitesToOutputFiles(os.Path("/tmp"), parsedOutput)
      _ <- Console.printLine(result.toJsonPretty)
    yield ()
