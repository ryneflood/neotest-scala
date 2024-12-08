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

val nestedSuitesOutput = """
+ HelloWorldSpec
  + Nested Suite
    + HelloWorld should say hello but different
    + HelloWorld should say hello
  + Test But Not a Suite
Execution took 0.3s
3 tests, 3 passed
All tests in bar.HelloWorldSpec passed

+ FooBarSpec
  + Nested Foo Bar Suite
    + Foo Bar Test
    - Bar Foo Test
      ✗ There was a difference
        Expected
        "Hello World, from Bar."
        Diff -expected +obtained
        Hello World, from Bar-.
      Hello.msg == "Hello World, from Bar."
      .msg = "Hello World, from Bar"
      Hello = bar.Hello$@74dc9765
      at /home/ryne/workspace/tmp/mill-bloop-zio-test/bar/test/src/bar/TestSuite.scala:37

Execution took 16ms
2 tests, 1 passed, 1 failed

4 tests passed. 1 tests failed. 0 tests ignored.


    - FooBarSpec / Nested Foo Bar Suite / Bar Foo Test
      ✗ There was a difference
        Expected
        "Hello World, from Bar."
        Diff -expected +obtained
        Hello World, from Bar-.
      Hello.msg == "Hello World, from Bar."
      .msg = "Hello World, from Bar"
      Hello = bar.Hello$@74dc9765
      at /home/ryne/workspace/tmp/mill-bloop-zio-test/bar/test/src/bar/TestSuite.scala:37

Executed in 249 ms

===============================================
Total duration: 0.31s
1 passed, 1 failed

Failed:
- bar.HelloWorldSpec2:
  * FooBarSpec - Nested Foo Bar Suite - Bar Foo Test -     - Bar Foo Test
      ✗ There was a difference
        Expected
        "Hello World, from Bar."
        Diff -expected +obtained
        Hello World, from Bar-.
      Hello.msg == "Hello World, from Bar."
      .msg = "Hello World, from Bar"
      Hello = bar.Hello$@74dc9765
      at /home/ryne/workspace/tmp/mill-bloop-zio-test/bar/test/src/bar/TestSuite.scala:37
"""

object ZioTestTestOutputParserSuite:
  class StripColorCodesSuite extends munit.FunSuite:
    test("should strip color codes from the test output"):
      val input =
        "[91m==> X [0m[91mbar.BarSuite[0m.[91mBar[0m  [90m0.014s[0m munit.ComparisonFailException: /home/ryne/workspace/tmp/mill-bloop-neotest/bar/test/src/bar/TestSuite.scala:20"
      val expected =
        "==> X bar.BarSuite.Bar  0.014s munit.ComparisonFailException: /home/ryne/workspace/tmp/mill-bloop-neotest/bar/test/src/bar/TestSuite.scala:20"
      val result = ZioTestTestOutputParser.stripColorCodes(input)

      assertEquals(expected, result)
  
  class ParseSuitesSuite extends munit.FunSuite:
    test("should be able to parse a single Test Suite"):
      val testSuiteOutput = """|
        |[32m+[0m FooSpec
        |  [32m+[0m BarSuite
        |    [32m+[0m Bar
        |    [32m+[0m Foo
        |  [32m+[0m Bar
        |  [32m+[0m BazSuite
        |    [32m+[0m Nested Test
        |  [31m- Foo[0m
        |    [31m✗ There was a difference[0m
        |    [31m  [0m[4mExpected[0m
        |    [31m  [0m"Hello World, from Bar."
        |    [31m  [0m[4mDiff[0m[2m [31m-expected [32m+obtained[0m
        |    [31m  [0m[0m[2mHello[0m[2m [0m[2mWorld[0m[2m, [0m[2mfrom[0m[2m [0m[2mBar[0m[31m-[4m.[0m
        |    [1mHello.msg == [0m[1m[33m"Hello World, from Bar."[0m[0m[1m[0m
        |    [2m.msg = [0m"Hello World, from Bar"
        |    [2mHello = [0mbar.Hello$@a3ba154
        |    [36mat /home/ryne/workspace/tmp/mill-bloop-zio-test/bar/test/src/bar/AnotherHelloWorldSpec.scala:11 [0m

        |  [32m+[0m Baz
        |Execution took 0.38s
        |6 tests, 5 passed, 1 failed
        |
        |5 tests passed. 1 tests failed. 0 tests ignored.
        |
        |
        |  [31m- [31mFooSpec[0m[2m[31m / [0m[0m[31mFoo[0m[0m
        |    [31m✗ There was a difference[0m
        |    [31m  [0m[4mExpected[0m
        |    [31m  [0m"Hello World, from Bar."
        |    [31m  [0m[4mDiff[0m[2m [31m-expected [32m+obtained[0m
        |    [31m  [0m[0m[2mHello[0m[2m [0m[2mWorld[0m[2m, [0m[2mfrom[0m[2m [0m[2mBar[0m[31m-[4m.[0m
        |    [1mHello.msg == [0m[1m[33m"Hello World, from Bar."[0m[0m[1m[0m
        |    [2m.msg = [0m"Hello World, from Bar"
        |    [2mHello = [0mbar.Hello$@a3ba154
        |    [36mat /home/ryne/workspace/tmp/mill-bloop-zio-test/bar/test/src/bar/AnotherHelloWorldSpec.scala:11 [0m
        |
        |Executed in 159 ms
        |
        |The test execution was successfully closed.
        |===============================================
        |Total duration: 0.38s
        |1 failed
        |
        |Failed:
        |- bar.FooSpec:
        |  * FooSpec - Foo -   [31m- Foo[0m
        |    [31m✗ There was a difference[0m
        |    [31m  [0m[4mExpected[0m
        |    [31m  [0m"Hello World, from Bar."
        |    [31m  [0m[4mDiff[0m[2m [31m-expected [32m+obtained[0m
        |    [31m  [0m[0m[2mHello[0m[2m [0m[2mWorld[0m[2m, [0m[2mfrom[0m[2m [0m[2mBar[0m[31m-[4m.[0m
        |    [1mHello.msg == [0m[1m[33m"Hello World, from Bar."[0m[0m[1m[0m
        |    [2m.msg = [0m"Hello World, from Bar"
        |    [2mHello = [0mbar.Hello$@a3ba154
        |    [36mat /home/ryne/workspace/tmp/mill-bloop-zio-test/bar/test/src/bar/AnotherHelloWorldSpec.scala:11 [0m
        |===============================================
      """.stripMargin

      val lines = testSuiteOutput.split("\n").toList

      val result = ZioTestTestOutputParser.parseTestOutput("FooSpec")(lines)

      val expectedTestNames = 
        List(
          "FooSpec.FooSpec.BarSuite.Bar",
          "FooSpec.FooSpec.BarSuite.Foo",
          "FooSpec.FooSpec.Bar",
          "FooSpec.FooSpec.BazSuite.Nested Test",
          "FooSpec.FooSpec.Foo",
          "FooSpec.FooSpec.Baz"
      )

      val obtainedTestNames = result.flatMap(_.tests.map(_.name))

      assertEquals(obtainedTestNames, expectedTestNames)
       
    test("should be able to parse a single Test Suite 2"):
      val testSuiteOutput = """
        |+ HelloWorldSpec
        |  + Another Nested Suite
        |    + HelloWorld should say hello
        |    - HelloWorld should say hello but different
        |      ✗ There was a difference
        |        Expected
        |        "Hello World, from Bar."
        |        Diff -expected +obtained
        |        Hello World, from Bar-.
        |      Hello.msg == "Hello World, from Bar."
        |      .msg = "Hello World, from Bar"
        |      Hello = bar.Hello$@50523b55
        |      at /home/ryne/workspace/tmp/mill-bloop-zio-test/bar/test/src/bar/TestSuite.scala:34
        |
        |  + Another Test That's Not a Suite
        |  + Test But Not a Suite
        |  + Nested Suite
        |    + Foo Test
        |    + Bar Test
        |  + One More Test That's at the Bottom
        |Execution took 0.55s
        |7 tests, 6 passed, 1 failed
      """.stripMargin

      // in this case the test names are:
      // + Another Nested Suite / HelloWorld should say hello but different
      // + Another Nested Suite / HelloWorld should say hello
      // + Another Test That's Not a Suite
      // + Test But Not a Suite
      // + Nested Suite / HelloWorld should say hello
      // + Nested Suite / HelloWorld should say hello but different
      // + One More Test That's at the Bottom
      
      val result = ZioTestTestOutputParser.parseTestSuite("HelloWorldSpec", testSuiteOutput.split("\n").toList)
      
      val expectedTestNames = 
        List(
          "HelloWorldSpec.HelloWorldSpec.Another Nested Suite.HelloWorld should say hello",
          "HelloWorldSpec.HelloWorldSpec.Another Nested Suite.HelloWorld should say hello but different",
          "HelloWorldSpec.HelloWorldSpec.Another Test That's Not a Suite",
          "HelloWorldSpec.HelloWorldSpec.Test But Not a Suite",
          "HelloWorldSpec.HelloWorldSpec.Nested Suite.Foo Test",
          "HelloWorldSpec.HelloWorldSpec.Nested Suite.Bar Test",
          "HelloWorldSpec.HelloWorldSpec.One More Test That's at the Bottom"
      )

      val obtainedTestNames = result.tests.map(_.name)

       assertEquals(obtainedTestNames, expectedTestNames)
