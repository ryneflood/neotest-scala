//> using test.dep org.scalameta::munit::1.0.2
//> using file "../src/Runner.scala"

package neotest

// import zio.*

class ScalaCliRunnerAdapterSuite extends munit.FunSuite {
  // the scala-cli command should look something like:
  // scala-cli test . --test-only neotest.ScalaCliRunnerAdapterSuite -- "neotest.ScalaCliRunnerAdapterSuite.Foo"
  test("should prepare the command to run all tests in a single suite") {
    val expected = List(
      "scala-cli",
      "test",
      ".",
      "--test-only",
      "foo.bar.FooSuite"
    )

    val result =
      ScalaCliRunnerAdapter().prepareCommand("foo.bar.FooSuite", None)

    assertEquals(result, expected)
  }

  test("should prepare the command to run all tests in a single suite, 2") {
    val expected = List(
      "scala-cli",
      "test",
      ".",
      "--test-only",
      "bar.BarSuite"
    )

    val result = ScalaCliRunnerAdapter().prepareCommand("bar.BarSuite", None)

    assertEquals(result, expected)
  }

  test("should prepare the command to run a single test") {
    val expected = List(
      "scala-cli",
      "test",
      ".",
      "--test-only",
      "bar.BarSuite",
      "--",
      "bar.BarSuite.Bar"
    )

    val result = ScalaCliRunnerAdapter().prepareCommand(
      "bar.BarSuite",
      Some("bar.BarSuite.Bar")
    )

    assertEquals(result, expected)
  }
}
