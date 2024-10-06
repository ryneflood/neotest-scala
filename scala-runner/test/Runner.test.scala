//> using test.dep org.scalameta::munit::1.0.2
//> using file "../src/Runner.scala"

package neotest

import zio.*

class ParseCommandLineOptionsSuite extends munit.FunSuite {
  test("Can parse command line arguments with a single test suite") {
    val commandLineArguments = Chunk(
      "--runner",
      "bloop",
      "--framework",
      "munit",
      "--to",
      "/tmp",
      "--project",
      "foo.test",
      "-o",
      "foo.bar.TestSuite"
    )

    val expected = Options(
      runner = "bloop",
      framework = "munit",
      outputDirectory = os.Path("/tmp"),
      project = "foo.test",
      testSuites = List("foo.bar.TestSuite"),
      singleTest = None
    )

    val options = CommandlineArgumentsParser.parseArgs(commandLineArguments)

    assertEquals(expected, options)
  }

  test("Can parse command line arguments which specify multiple Test Suites") {
    val commandLineArguments = Chunk(
      "--runner",
      "bloop",
      "--framework",
      "zio-test",
      "--to",
      "/tmp",
      "--project",
      "foo.test",
      "-o",
      "foo.bar.TestSuite",
      "-o",
      "foo.bar.TestSuite2"
    )

    val expected = Options(
      runner = "bloop",
      framework = "zio-test",
      outputDirectory = os.Path("/tmp"),
      project = "foo.test",
      testSuites = List("foo.bar.TestSuite", "foo.bar.TestSuite2"),
      singleTest = None
    )

    val options = CommandlineArgumentsParser.parseArgs(commandLineArguments)

    assertEquals(expected, options)
  }

  test("Can parse command line arguments which specify a single test") {
    val commandLineArguments = Chunk(
      "--runner",
      "bloop",
      "--framework",
      "munit",
      "--to",
      "/tmp",
      "--project",
      "foo.test",
      "-o",
      "foo.bar.TestSuite2",
      "--single",
      "foo.bar.TestSuite2.test"
    )

    val expected = Options(
      runner = "bloop",
      framework = "munit",
      outputDirectory = os.Path("/tmp"),
      project = "foo.test",
      testSuites = List("foo.bar.TestSuite2"),
      singleTest = Some("foo.bar.TestSuite2.test")
    )

    val options = CommandlineArgumentsParser.parseArgs(commandLineArguments)

    assertEquals(expected, options)
  }
}
