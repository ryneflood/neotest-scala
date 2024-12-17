package neotest

import zio.*
// import zio.test.*
// import zio.test.ReporterEventRenderer.IntelliJEventRenderer
// import java.net.URLClassLoader
// import zio.test.render.TestRenderer
// import zio.test.render.ExecutionResult
// import zio.test.render.ExecutionResult.ResultType
import zio.json.*

// val projectPath = "/path/to/other/project/target/scala-2.xx/classes"
// val classLoader = new URLClassLoader(
//   Array(new java.io.File(projectPath).toURI.toURL),
//   this.getClass.getClassLoader
// )

// object HelloWorldSpec extends ZIOSpecDefault {
//   def spec =
//     suite("HelloWorldSpec")(
//       test("HelloWorld should say hello") {
//         assertTrue(true == false)
//       }
//     )
// }

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
          case Some(test) => List("--", "-t", test)
          case None       => List.empty
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

        // println("command: " + command.mkString(" "))

        val commandResult: os.CommandResult = os
          .proc(command)
          .call(check = false)

        val output = commandResult.exitCode match
          case 0 | 32 => commandResult.out.lines().toList
          case _      => throw Throwable(commandResult.err.text())

        if options.framework == "zio-test" then
          ZioTestTestOutputParser.parseTestOutput(options.testSuites.head)(output)
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

              val extraOptions = if options.framework == "scalatest" then
                List("--", "-u", "/tmp/scalatest-out/")
              else List.empty

              val commandResult: os.CommandResult =
                os.proc(command ::: extraOptions).call(check = false)

              commandResult.exitCode match
                case 0 | 32 => commandResult.out.lines().toList
                case _      => throw Throwable(commandResult.err.text())

            }
            .map(output =>
              if options.framework == "zio-test" then
                ZioTestTestOutputParser.parseTestOutput(suite)(output)
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
            ZioTestTestOutputParser.parseTestOutput(options.testSuites.head)
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
                  ZioTestTestOutputParser.parseTestOutput(suite)(output)
                else MunitTestOutputParser.parseTestOutput(List(suite))(output)
              )
          }
          .map(_.flatten)

// import zio.Trace
// import zio.internal.stacktracer.SourceLocation
// import zio.test.ExecutionEvent.{
//   SectionEnd,
//   SectionStart,
//   Test,
//   TestStarted,
//   TopLevelFlush
// }
// import zio.test.TestAnnotationRenderer.LeafRenderer
// import zio.test.render.ExecutionResult.{ResultType, Status}
// import zio.test.render.LogLine.Message
// import zio.test.*
// import zio.test.render.LogLine.Message
// import zio.test.render.*

// class CustomTestRenderer extends TestRenderer:
//   override def renderEvent(event: ExecutionEvent, includeCause: Boolean)(
//       implicit trace: Trace
//   ): Seq[ExecutionResult] =
//     Seq(
//       ExecutionResult.withoutSummarySpecificOutput(
//         ResultType.Suite,
//         // label = nonEmptyList.last,
//         label = "label...",
//         Status.Passed,
//         offset = 1,
//         List(TestAnnotationMap.empty),
//         // lines = List(fr(nonEmptyList.last).toLine),
//         lines = List(fr("line...").toLine),
//         duration = None
//       )
//     )
//
//   override protected def renderOutput(results: Seq[ExecutionResult])(implicit
//       trace: Trace
//   ): Seq[String] =
//     results.foldLeft(List.empty[String]) { (acc, result) =>
//       result match
//         case r @ ExecutionResult(
//               ResultType.Suite,
//               _,
//               Status.Started,
//               _,
//               _,
//               _,
//               _,
//               _
//             ) =>
//           acc :+ "Suite started: " + r.label
//         case r @ ExecutionResult(ResultType.Suite, _, _, _, _, _, _, _) =>
//           acc :+ "Suite finished: " + r.label
//         case r @ ExecutionResult(
//               ResultType.Test,
//               _,
//               Status.Passed,
//               _,
//               _,
//               _,
//               _,
//               duration
//             ) =>
//           acc :+ "Test started: " + r.label
//         case r @ ExecutionResult(
//               ResultType.Test,
//               _,
//               Status.Failed,
//               _,
//               _,
//               _,
//               _,
//               _
//             ) =>
//           acc :+ "Test started: " + r.label
//         case r @ ExecutionResult(
//               ResultType.Test,
//               _,
//               Status.Ignored,
//               _,
//               _,
//               _,
//               _,
//               _
//             ) =>
//           acc :+ "Test ignored: " + r.label
//         case r => acc :+ "Something else happened " + r.label
//
//     }
//
//   def renderSummary(summary: Summary): String = ""
//
// class MyCustomEventHandler extends ZTestEventHandler {
//   def handle(event: ExecutionEvent) =
//     event match {
//       case ExecutionEvent.TestStarted(
//             labelsReversed,
//             annotations,
//             ancestors,
//             id,
//             fullyQualifiedName
//           ) =>
//         Console.printLine(s"Test started: $fullyQualifiedName").orDie
//
//       case v @ ExecutionEvent.Test(
//             labelsReversed,
//             test,
//             annotations,
//             ancestors,
//             duration,
//             id,
//             fullyQualifiedName
//           ) =>
//         Console.printLine(s"Test finished: $fullyQualifiedName").orDie *>
//           test.fold(
//             failure =>
//               val failureDetails = failure match
//                 case TestFailure.Assertion(testResult, _) =>
//                   val tr: TestResult = testResult
//
//                   tr.failures.map(_.values).mkString("\n")
//
//                 case TestFailure.Runtime(cause, _) =>
//                   cause.prettyPrint
//               Console.printLine(s"Test failed: $failureDetails").orDie,
//             success => Console.printLine(s"Test passed: $success").orDie
//           )
//
//       case ExecutionEvent.SectionStart(labelsReversed, id, ancestors) =>
//         Console.printLine(s"Section started: $id").orDie
//
//       case ExecutionEvent.SectionEnd(labelsReversed, id, ancestors) =>
//         Console.printLine(s"Section ended: $id").orDie
//
//       case ExecutionEvent.TopLevelFlush(id) =>
//         Console.printLine(s"Top level flush: $id").orDie
//
//       case ExecutionEvent.RuntimeFailure(
//             id,
//             labelsReversed,
//             failure,
//             ancestors
//           ) =>
//         Console.printLine(s"Runtime failure: $id").orDie
//     }
// }

object Runner extends zio.ZIOAppDefault:
  private def getRunner(runner: String): TestRunnerAdapter =
    runner match
      case "bloop"     => BloopRunnerAdapter()
      case "scala-cli" => ScalaCliRunnerAdapter()
      case _           => throw Throwable(s"Unsupported runner: $runner")

  // override def run =
  //   val spec = createSpec()
  //   // val spec = HelloWorldSpec.spec
  //   val sinkLayer =
  //     ExecutionEventSink.live(Console.ConsoleLive, IntelliJEventRenderer)
  //
  //   val eventHandlerZ: ZTestEventHandler = MyCustomEventHandler()
  //   // use the default event handler
  //   val sharedSpecLayer: ZLayer[Any, Nothing, Any] = ZLayer.succeed(())
  //
  //   val freshLayerPerSpec: ZLayer[Any, Nothing, TestEnvironment & Scope] =
  //     (testEnvironment ++ Scope.default)
  //
  //   val specExecutor: TestExecutor[TestEnvironment & Scope, Any] =
  //     TestExecutor.default(
  //       sharedSpecLayer,
  //       freshLayerPerSpec,
  //       sinkLayer,
  //       eventHandlerZ
  //     )
  //   val specExec = specExecutor
  //     .run("MySpec", spec, ExecutionStrategy.Sequential)
  //     .flatMap { (summary: Summary) =>
  //       // Console.printLine(s"Test Successful: ${summary.success}") *>
  //       //   Console.printLine(s"Test Failures: ${summary.fail}") *>
  //       //   Console.printLine("Test Summary: " + summary.failureDetails)
  //       // Console.printLine("Test Summary: " + summary)
  //       ZIO.unit
  //     }
  //     .exitCode
  //
  //   for
  //     _ <- Console.printLine("Hello, world!")
  //     exitCode <- specExec
  //     _ <- Console.printLine(s"Exit code: $exitCode")
  //   yield ()
  //
  // def createSpec(): Spec[zio.test.TestEnvironment & zio.Scope, Any] = {
  //   import org.portablescala.reflect.*
  //   // val fqn = args.testClass.stripSuffix("$") + "$"
  //   // val fqn = "neotest.HelloWorldSpec$"
  //   // val classPath =
  //   //   "/home/ryne/workspace/tmp/mill-bloop-zio-test/.bloop/out/bar.test/bloop-bsp-clients-classes/classes-bloop-cli/bar/AnotherHelloWorldSpec.class"
  //
  //   val classDirectory =
  //     "/home/ryne/workspace/tmp/mill-bloop-zio-test/.bloop/out/bar.test/bloop-bsp-clients-classes/classes-bloop-cli/"
  //
  //   val file = new java.io.File(classDirectory)
  //
  //   // load the class from the filesystem
  //   val classLoader =
  //     new URLClassLoader(Array(file.toURI.toURL), getClass.getClassLoader)
  //
  //   println("Class loader: " + classLoader)
  //
  //   val fqn = "bar.FooSpec$"
  //
  //   Reflect
  //     .lookupLoadableModuleClass(fqn, classLoader)
  //     .getOrElse(
  //       throw new ClassNotFoundException("failed to load object: " + fqn)
  //     )
  //     .loadModule()
  //     .asInstanceOf[ZIOSpec[TestEnvironment]]
  //     .spec
  // }

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
