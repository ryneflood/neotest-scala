package neotest

val output = """
Compiling foo (1 Scala source)
Compiled foo (1400ms)
Compiling foo.test (1 Scala source)
Compiled foo.test (1078ms)
[32m+[0m Foo Suite
  [32m+[0m HelloWorld should say hello
  [31m- HelloWorld should say hello[0m
    [31m✗ [0m[1m[34mtrue[0m[0m [31mwas not equal to[0m [1m[34mfalse[0m[0m
    [1mtrue == [0m[1m[33mfalse[0m[0m[1m[0m
    [36mat /home/ryne/workspace/tmp/mill-bloop-zio-test/foo/test/src/foo/TestSuite.scala:14 [0m

1 tests passed. 1 tests failed. 0 tests ignored.


  [31m- [31mFoo Suite[0m[2m[31m / [0m[0m[31mHelloWorld should say hello[0m[0m
Execution took 90ms
    [31m✗ [0m[1m[34mtrue[0m[0m [31mwas not equal to[0m [1m[34mfalse[0m[0m
    [1mtrue == [0m[1m[33mfalse[0m[0m[1m[0m
    [36mat /home/ryne/workspace/tmp/mill-bloop-zio-test/foo/test/src/foo/TestSuite.scala:14 [0m

Executed in 155 ms

2 tests, 1 passed, 1 failed

===============================================
Total duration: 90ms
1 failed

Failed:
- foo.bar.FooTests:
  * Foo Suite - HelloWorld should say hello -   [31m- HelloWorld should say hello[0m
    [31m✗ [0m[1m[34mtrue[0m[0m [31mwas not equal to[0m [1m[34mfalse[0m[0m
    [1mtrue == [0m[1m[33mfalse[0m[0m[1m[0m
    [36mat /home/ryne/workspace/tmp/mill-bloop-zio-test/foo/test/src/foo/TestSuite.scala:14 [0m
===============================================
The test execution was successfully closed.
"""

val multipleSuitesOutput = """
Compiling foo (1 Scala source)
Compiled foo (1400ms)
Compiling foo.test (1 Scala source)
Compiled foo.test (1078ms)
[32m+[0m Bar Suite
  [32m+[0m Bar Test
  [31m- Baz Test[0m
    [31m✗ [0m[1m[34mtrue[0m[0m [31mwas not equal to[0m [1m[34mfalse[0m[0m
    [1mtrue == [0m[1m[33mfalse[0m[0m[1m[0m
    [36mat /home/ryne/workspace/tmp/mill-bloop-zio-test/foo/test/src/foo/TestSuite.scala:26 [0m

Execution took 83ms
2 tests, 1 passed, 1 failed

[32m+[0m Foo Suite
  [31m- Baz Test[0m
    [31m✗ [0m[1m[34mtrue[0m[0m [31mwas not equal to[0m [1m[34mfalse[0m[0m
    [1mtrue == [0m[1m[33mfalse[0m[0m[1m[0m
    [36mat /home/ryne/workspace/tmp/mill-bloop-zio-test/foo/test/src/foo/TestSuite.scala:14 [0m

  [31m- Foo Test[0m
    [31m✗ [0m[1m[34mtrue[0m[0m [31mwas not equal to[0m [1m[34mfalse[0m[0m
    [1mtrue == [0m[1m[33mfalse[0m[0m[1m[0m
    [36mat /home/ryne/workspace/tmp/mill-bloop-zio-test/foo/test/src/foo/TestSuite.scala:11 [0m

Execution took 2ms
2 tests, 2 failed

1 tests passed. 3 tests failed. 0 tests ignored.


  [31m- [31mBar Suite[0m[2m[31m / [0m[0m[31mBaz Test[0m[0m
    [31m✗ [0m[1m[34mtrue[0m[0m [31mwas not equal to[0m [1m[34mfalse[0m[0m
    [1mtrue == [0m[1m[33mfalse[0m[0m[1m[0m
    [36mat /home/ryne/workspace/tmp/mill-bloop-zio-test/foo/test/src/foo/TestSuite.scala:26 [0m


  [31m- [31mFoo Suite[0m[2m[31m / [0m[0m[31mBaz Test[0m[0m
    [31m✗ [0m[1m[34mtrue[0m[0m [31mwas not equal to[0m [1m[34mfalse[0m[0m
    [1mtrue == [0m[1m[33mfalse[0m[0m[1m[0m
    [36mat /home/ryne/workspace/tmp/mill-bloop-zio-test/foo/test/src/foo/TestSuite.scala:14 [0m

  [31m- [31mFoo Suite[0m[2m[31m / [0m[0m[31mFoo Test[0m[0m
    [31m✗ [0m[1m[34mtrue[0m[0m [31mwas not equal to[0m [1m[34mfalse[0m[0m
    [1mtrue == [0m[1m[33mfalse[0m[0m[1m[0m
    [36mat /home/ryne/workspace/tmp/mill-bloop-zio-test/foo/test/src/foo/TestSuite.scala:11 [0m

Executed in 205 ms

===============================================
Total duration: 85ms
2 failed

Failed:
- foo.bar.BarTests:
  * Bar Suite - Baz Test -   [31m- Baz Test[0m
    [31m✗ [0m[1m[34mtrue[0m[0m [31mwas not equal to[0m [1m[34mfalse[0m[0m
    [1mtrue == [0m[1m[33mfalse[0m[0m[1m[0m
    [36mat /home/ryne/workspace/tmp/mill-bloop-zio-test/foo/test/src/foo/TestSuite.scala:26 [0m
- foo.bar.FooTests:
  * Foo Suite - Baz Test -   [31m- Baz Test[0m
    [31m✗ [0m[1m[34mtrue[0m[0m [31mwas not equal to[0m [1m[34mfalse[0m[0m
    [1mtrue == [0m[1m[33mfalse[0m[0m[1m[0m
    [36mat /home/ryne/workspace/tmp/mill-bloop-zio-test/foo/test/src/foo/TestSuite.scala:14 [0m
  * Foo Suite - Foo Test -   [31m- Foo Test[0m
    [31m✗ [0m[1m[34mtrue[0m[0m [31mwas not equal to[0m [1m[34mfalse[0m[0m
    [1mtrue == [0m[1m[33mfalse[0m[0m[1m[0m
    [36mat /home/ryne/workspace/tmp/mill-bloop-zio-test/foo/test/src/foo/TestSuite.scala:11 [0m
===============================================
""""

object ZioTestTestOutputParserSuite:
  class StripColorCodesSuite extends munit.FunSuite:
    test("should strip color codes from the test output"):
      val input =
        "[91m==> X [0m[91mbar.BarSuite[0m.[91mBar[0m  [90m0.014s[0m munit.ComparisonFailException: /home/ryne/workspace/tmp/mill-bloop-neotest/bar/test/src/bar/TestSuite.scala:20"
      val expected =
        "==> X bar.BarSuite.Bar  0.014s munit.ComparisonFailException: /home/ryne/workspace/tmp/mill-bloop-neotest/bar/test/src/bar/TestSuite.scala:20"
      val result = ZioTestTestOutputParser.stripColorCodes(input)

      assertEquals(expected, result)
  
  class SplitIntoSuitesSuite extends munit.FunSuite:
    test("should split the test output into suites"):
      val input = multipleSuitesOutput.split("\n").toList
      val expectedKeys = List("foo.bar.Bar Suite", "foo.bar.Foo Suite")
      
      val result = ZioTestTestOutputParser.splitIntoSuites(List("foo.bar.Foo Suite", "foo.bar.Bar Suite"))(input)

      val foundTestSuiteNames = result.keys.toList

      assertEquals(foundTestSuiteNames, expectedKeys)

  class ParseSuitesSuite extends munit.FunSuite:
    test("should parse output that has been split into Test Suites, and indicate which tests passed and failed"):
      val input = multipleSuitesOutput.split("\n").toList
      
      val testSuites = ZioTestTestOutputParser.splitIntoSuites(List("foo.bar.Foo Suite", "foo.bar.Bar Suite"))(input)
      val result = ZioTestTestOutputParser.parseSuites(testSuites)

      assert(result(0).tests(0).isInstanceOf[TestResultWithOutput.Passed])
      assertEquals(result(0).tests(0).name, "foo.bar.Bar Suite.Bar Test")
      
      assert(result(0).tests(1).isInstanceOf[TestResultWithOutput.Failed])
      assertEquals(result(0).tests(1).name, "foo.bar.Bar Suite.Baz Test")

      assert(result(1).tests(0).isInstanceOf[TestResultWithOutput.Failed])
      assertEquals(result(1).tests(0).name, "foo.bar.Foo Suite.Baz Test")
      
      assert(result(1).tests(1).isInstanceOf[TestResultWithOutput.Failed])
      assertEquals(result(1).tests(1).name, "foo.bar.Foo Suite.Foo Test")
