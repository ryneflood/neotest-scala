//> using test.dep org.scalameta::munit::1.0.2
//> using file "../src/Runner.scala"

package neotest

val singleSuiteTestOutput = """
bar.FooSuite:
  + Foo 0.01s
  + Bar 0.001s
==> X bar.FooSuite.Baz  0.021s munit.ComparisonFailException: /home/ryne/workspace/tmp/mill-bloop-neotest/bar/test/src/bar/TestSuite.scala:13
12:  test("Baz"):
13:    assertEquals("Bar", "Baz")
14:  
values are not the same
=> Obtained
Bar
=> Diff (- obtained, + expected)
-Bar
+Baz
    at munit.Assertions.failComparison(Assertions.scala:278)
Execution took 32ms
3 tests, 2 passed, 1 failed

The test execution was successfully closed.
===============================================
Total duration: 32ms
1 failed

Failed:
- bar.FooSuite:
  * bar.FooSuite.Baz - munit.ComparisonFailException: /home/ryne/workspace/tmp/mill-bloop-neotest/bar/test/src/bar/TestSuite.scala:13
12:  test("Baz"):
13:    assertEquals("Bar", "Baz")
14:  
values are not the same
=> Obtained
Bar
=> Diff (- obtained, + expected)
-Bar
+Baz
===============================================
"""

val testOutputMultipleSuites = """
[32mbar.BarSuite:[0m
[32m  + [0m[32mFoo[0m [90m0.007s[0m
[91m==> X [0m[91mbar.BarSuite[0m.[91mBar[0m  [90m0.014s[0m munit.ComparisonFailException: /home/ryne/workspace/tmp/mill-bloop-neotest/bar/test/src/bar/TestSuite.scala:20
19:  test("Bar"):
[7m20:    assertEquals("Bar", "Baz")[0m
21:    
values are not the same
[1m=> Obtained[0m
Bar
[1m=> Diff[0m ([91m- obtained[0m, [92m+ expected[0m)
[91m-Bar[0m
[92m+Baz[0m
[90m    at [0m[90mmunit.Assertions.failComparison[0m[90m([0m[90mAssertions.scala[0m:[90m278[0m[90m)[0m
Execution took 21ms
2 tests, 1 passed, 1 failed

[32mbar.BazSuite:[0m
[91m==> X [0m[91mbar.BazSuite[0m.[91mFoo[0m  [90m0.001s[0m munit.ComparisonFailException: /home/ryne/workspace/tmp/mill-bloop-neotest/bar/test/src/bar/TestSuite.scala:24
23:  test("Foo"):
[7m24:    assertEquals("Foo", "Baz")[0m
25:
values are not the same
[1m=> Obtained[0m
Foo
[1m=> Diff[0m ([91m- obtained[0m, [92m+ expected[0m)
[91m-Foo[0m
[92m+Baz[0m
[90m    at [0m[90mmunit.Assertions.failComparison[0m[90m([0m[90mAssertions.scala[0m:[90m278[0m[90m)[0m
[91m==> X [0m[91mbar.BazSuite[0m.[91mBaz[0m  [90m0.002s[0m munit.ComparisonFailException: /home/ryne/workspace/tmp/mill-bloop-neotest/bar/test/src/bar/TestSuite.scala:27
26:  test("Baz"):
[7m27:    assertEquals("Baz", "Foo")[0m
values are not the same
[1m=> Obtained[0m
Baz
[1m=> Diff[0m ([91m- obtained[0m, [92m+ expected[0m)
[91m-Baz[0m
[92m+Foo[0m
[90m    at [0m[90mmunit.Assertions.failComparison[0m[90m([0m[90mAssertions.scala[0m:[90m278[0m[90m)[0m
Execution took 3ms
2 tests, 2 failed

[32mbar.FooSuite:[0m
[32m  + [0m[32mFoo[0m [90m0.001s[0m
[32m  + [0m[32mBar[0m [90m0.0s[0m
[91m==> X [0m[91mbar.FooSuite[0m.[91mBaz[0m  [90m0.001s[0m munit.ComparisonFailException: /home/ryne/workspace/tmp/mill-bloop-neotest/bar/test/src/bar/TestSuite.scala:13
12:  test("Baz"):
[7m13:    assertEquals("Bar", "Baz")[0m
14:  
values are not the same
[1m=> Obtained[0m
Bar
[1m=> Diff[0m ([91m- obtained[0m, [92m+ expected[0m)
[91m-Bar[0m
[92m+Baz[0m
[90m    at [0m[90mmunit.Assertions.failComparison[0m[90m([0m[90mAssertions.scala[0m:[90m278[0m[90m)[0m
Execution took 2ms
3 tests, 2 passed, 1 failed

The test execution was successfully closed.
===============================================
Total duration: 26ms
3 failed

Failed:
- bar.BarSuite:
  * bar.BarSuite.Bar - munit.ComparisonFailException: /home/ryne/workspace/tmp/mill-bloop-neotest/bar/test/src/bar/TestSuite.scala:20
19:  test("Bar"):
[7m20:    assertEquals("Bar", "Baz")[0m
21:    
values are not the same
[1m=> Obtained[0m
Bar
[1m=> Diff[0m ([91m- obtained[0m, [92m+ expected[0m)
[91m-Bar[0m
[92m+Baz[0m
- bar.BazSuite:
  * bar.BazSuite.Foo - munit.ComparisonFailException: /home/ryne/workspace/tmp/mill-bloop-neotest/bar/test/src/bar/TestSuite.scala:24
23:  test("Foo"):
[7m24:    assertEquals("Foo", "Baz")[0m
25:
values are not the same
[1m=> Obtained[0m
Foo
[1m=> Diff[0m ([91m- obtained[0m, [92m+ expected[0m)
[91m-Foo[0m
[92m+Baz[0m
  * bar.BazSuite.Baz - munit.ComparisonFailException: /home/ryne/workspace/tmp/mill-bloop-neotest/bar/test/src/bar/TestSuite.scala:27
26:  test("Baz"):
[7m27:    assertEquals("Baz", "Foo")[0m
values are not the same
[1m=> Obtained[0m
Baz
[1m=> Diff[0m ([91m- obtained[0m, [92m+ expected[0m)
[91m-Baz[0m
[92m+Foo[0m
- bar.FooSuite:
  * bar.FooSuite.Baz - munit.ComparisonFailException: /home/ryne/workspace/tmp/mill-bloop-neotest/bar/test/src/bar/TestSuite.scala:13
12:  test("Baz"):
[7m13:    assertEquals("Bar", "Baz")[0m
14:  
values are not the same
[1m=> Obtained[0m
Bar
[1m=> Diff[0m ([91m- obtained[0m, [92m+ expected[0m)
[91m-Bar[0m
[92m+Baz[0m
===============================================
"""

object MunitTestOutputParserSuite:
  class StripColorCodesSuite extends munit.FunSuite {
    test("should strip color codes from the test output") {
      val input =
        "[91m==> X [0m[91mbar.BarSuite[0m.[91mBar[0m  [90m0.014s[0m munit.ComparisonFailException: /home/ryne/workspace/tmp/mill-bloop-neotest/bar/test/src/bar/TestSuite.scala:20"
      val expected =
        "==> X bar.BarSuite.Bar  0.014s munit.ComparisonFailException: /home/ryne/workspace/tmp/mill-bloop-neotest/bar/test/src/bar/TestSuite.scala:20"
      val result = MunitTestOutputParser.stripColorCodes(input)

      assertEquals(expected, result)
    }
  }

  class ParseTestOutputSuite extends munit.FunSuite {
    test("should parse the test output for a single Test Suite") {
      val result =
        MunitTestOutputParser.parseTestOutput(List("bar.FooSuite"))(
          singleSuiteTestOutput.split("\n").toList
        )

      val testSuite = result.head

      val parsedTestNames = testSuite.tests.map(_.name)

      assertEquals("bar.FooSuite", result.head.name)
      assert(parsedTestNames(0) == "bar.FooSuite.Foo")
      assert(parsedTestNames(1) == "bar.FooSuite.Bar")
      assert(parsedTestNames(2) == "bar.FooSuite.Baz")

      assert(testSuite.tests(2).isInstanceOf[TestResultWithOutput.Failed])
      assert(
        testSuite
          .tests(2)
          .output
          .mkString("\n")
          .contains("values are not the same")
      )
    }

    test("should parse the test output for multiple Test Suites") {
      val result =
        MunitTestOutputParser.parseTestOutput(
          List("bar.FooSuite", "bar.BarSuite", "bar.BazSuite")
        )(
          testOutputMultipleSuites.split("\n").toList
        )

      val expectedTestNames =
        List(
          "bar.BarSuite.Foo", // ==> passed
          "bar.BarSuite.Bar", // ==> failed
          "bar.BazSuite.Foo", // ==> failed
          "bar.BazSuite.Baz", // ==> failed
          "bar.FooSuite.Foo", // ==> passed
          "bar.FooSuite.Bar", // ==> passed
          "bar.FooSuite.Baz" // ==> failed
        )

      val successfulTestNames =
        List(
          "bar.BarSuite.Foo", // ==> passed
          "bar.FooSuite.Foo", // ==> passed
          "bar.FooSuite.Bar" // ==> passed
        )

      val testResults = result.flatMap(_.tests)
      // assert that for each of the successful tests, we have a TestResultWithOutput.Passed
      assert(
        successfulTestNames.forall: testName =>
          testResults
            .find(_.name == testName)
            .get
            .isInstanceOf[TestResultWithOutput.Passed]
      )

      assertEquals(3, result.length)
      assertEquals(result.flatMap(_.tests.map(_.name)), expectedTestNames)
      assert(
        result(0).tests
          .find(_.name == "bar.BarSuite.Bar")
          .get
          .isInstanceOf[TestResultWithOutput.Failed]
      )
      assert(
        result(0).tests
          .find(_.name == "bar.BarSuite.Bar")
          .get
          .output
          .mkString("\n")
          .contains("values are not the same")
      )
      assert(
        result(1).tests
          .find(_.name == "bar.BazSuite.Foo")
          .get
          .isInstanceOf[TestResultWithOutput.Failed]
      )
      assert(
        result(1).tests
          .find(_.name == "bar.BazSuite.Foo")
          .get
          .output
          .mkString("\n")
          .contains("values are not the same")
      )
      assert(
        result(1).tests
          .find(_.name == "bar.BazSuite.Baz")
          .get
          .isInstanceOf[TestResultWithOutput.Failed]
      )
      assert(
        result(1).tests
          .find(_.name == "bar.BazSuite.Baz")
          .get
          .output
          .mkString("\n")
          .contains("values are not the same")
      )
    }
  }

  class IsPassedTestSuite extends munit.FunSuite {
    test("should parse a passed test") {
      val input = "  + Foo 0.01s"
      val result = MunitTestOutputParser.isPassedTest(input)

      assert(result.isDefined)
      assert(result.get.isInstanceOf[TestResult.Passed])
      assertEquals("Foo", result.get.asInstanceOf[TestResult.Passed].name)
    }
  }
  
  class IsFailedTestSuite extends munit.FunSuite {
    test("should parse a failed test") {
      val input = "==> X bar.FooSuite.Baz  0.021s munit.ComparisonFailException: /home/ryne/workspace/tmp/mill-bloop-neotest/bar/test/src/bar/TestSuite.scala:13"
      val result = MunitTestOutputParser.isFailedTest(input)

      assert(result.isDefined)
      assert(result.get.isInstanceOf[TestResult.Failed])
      assertEquals("bar.FooSuite.Baz", result.get.asInstanceOf[TestResult.Failed].name)
    }
  }
