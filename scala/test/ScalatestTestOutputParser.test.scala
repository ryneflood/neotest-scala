package neotest

import munit.*

class ScalatestTestParserSuite extends munit.FunSuite:
  val simpleTestOutput = """|
    |Compiling baz.test (1 Scala source)
    |Compiled baz.test (494ms)
    |[32mFooSuite:[0m
    |[32mFoo Suite[0m
    |[32m- Foo[0m
    |[32m- Bar[0m
    |Execution took 9ms
    |2 tests, 2 passed
    |All tests in foo.bar.FooSuite passed
    |
    |===============================================
    |Total duration: 9ms
    |All 1 test suites passed.
    |===============================================
    |The test execution was successfully closed.""".stripMargin

  val basic = """
    |[32mFooSuite:[0m
    |[32mFoo Suite[0m
    |[32m  Bar Suite[0m
    |[32m    Again[0m
    |[32m    - Baz[0m
    |[32m    - Not Baz[0m
    |[32m- Foo[0m
    |[32m- Bar[0m
    |[32mAnother Suite[0m
    |[32m- Another Test[0m
  """.stripMargin

  val suiteWithFailedTest = """|
    |[32mFoo Suite[0m
    |[32m  Bar Suite[0m
    |[32m    Again[0m
    |[31m    - Baz *** FAILED ***[0m
    |[31m      true did not equal false (TestSuite.scala:11)[0m
    |[32m    - Not Baz[0m
    |[32m- Foo[0m
    |[31m- Bar *** FAILED ***[0m
    |[31m  true did not equal false (TestSuite.scala:24)[0m
    |""".stripMargin

  val successfulTestOutput = """
    |[32mFooSuite:[0m [32mFoo Suite[0m [32m Bar Suite[0m [32m - Baz[0m
    |[32m - Not Baz[0m [32m- Foo[0m [32m- Bar[0m Execution took 7ms 4
    |tests, 4 passed All tests in foo.bar.FooSuite passed

    |The test execution was successfully closed.
    |=============================================== Total duration: 7ms All 1
    |test suites passed.
    |===============================================
  """.stripMargin

  test("should parse a simple, non-nested, successful test output".ignore):
    val result = ScalatestTestOutputParser.parseTestOutput("FooSuite")(simpleTestOutput.linesIterator.toList)

    val expected = List(
      TestSuite(
        "FooSuite", 
        List(
          TestResultWithOutput.Passed("FooSuite.Foo Suite.Foo"),
          TestResultWithOutput.Passed("FooSuite.Foo Suite.Bar")
        )
      )
    )

    assertEquals(result, expected)

  test("find test locations"):
    val locations = ScalatestTestOutputParser.findTestLocations(basic.linesIterator.toList)

    val obtainedNames = locations.map(_.name)

    val expectedNames = List(
      "",
      "Foo Suite",
      "Bar Suite",
      "Again",
      "Baz",
      "Not Baz",
      "Foo",
      "Bar",
      "Another Suite",
      "Another Test"
    )

    assertEquals(obtainedNames, expectedNames)

  test("should parse steps into a TestSuite"):
     val locations = List(
        Step.NoChange(0, "Foo Suite", "Foo Suite"),
        Step.Increment(0, "  Bar Suite", "Bar Suite"),
        Step.Increment(0, "    Again", "Again"),
        Step.NoChange(0, "    - Baz", "Baz"),
        Step.NoChange(0, "    - Not Baz", "Not Baz"),
        Step.Decrement(0, "- Foo", "Foo", 2),
        Step.NoChange(0, "- Bar", "Bar"),
       )

     val expected = TestSuite(
       "FooSuite",
       List(
          TestResultWithOutput.Passed("Foo Suite.Bar Suite.Again.Baz"),
          TestResultWithOutput.Passed("Foo Suite.Bar Suite.Again.Not Baz"),
          TestResultWithOutput.Passed("Foo Suite.Foo"),
          TestResultWithOutput.Passed("Foo Suite.Bar"),
         )
       )

     val obtained = ScalatestTestOutputParser.parseSteps(List.empty, locations)
     
     assertEquals(obtained, expected)

  test("should parse steps into a TestSuite, including a failed test with its output"):
     val locations = List(
        Step.NoChange(0, "[32mFoo Suite[0m", "Foo Suite"),
        Step.Increment(1, "[32m  Bar Suite[0m", "Bar Suite"),
        Step.Increment(2, "[32m    Again[0m", "Again"),
        Step.NoChange(3, "[31m    - Baz *** FAILED ***[0m", "Baz"),
        Step.NoChange(5, "[32m    - Not Baz[0m", "Not Baz"),
        Step.Decrement(6, "[32m- Foo[0m", "Foo", 2),
        Step.NoChange(7, "[31m- Bar *** FAILED ***[0m", "Bar"),
       )

     val expectedFailedTestOutput = List(
       "[31m    - Baz *** FAILED ***[0m",
       "[31m      true did not equal false (TestSuite.scala:11)[0m"
       )

     val expectedFailedTestOutput2 = List(
        "[31m- Bar *** FAILED ***[0m",
        "[31m  true did not equal false (TestSuite.scala:24)[0m"
      )

     val expected = TestSuite(
       "FooSuite",
       List(
          TestResultWithOutput.Failed("Foo Suite.Bar Suite.Again.Baz", expectedFailedTestOutput),
          TestResultWithOutput.Passed("Foo Suite.Bar Suite.Again.Not Baz"),
          TestResultWithOutput.Passed("Foo Suite.Foo"),
          TestResultWithOutput.Failed("Foo Suite.Bar", expectedFailedTestOutput2),
         )
       )

     val obtained = ScalatestTestOutputParser.parseSteps(suiteWithFailedTest.linesIterator.toList, locations)
     
     assertEquals(obtained, expected)
  

  test("should parse Successful test output".ignore):
    val result = ScalatestTestOutputParser.parseTestOutput("FooSuite")(successfulTestOutput.linesIterator.toList)

    val expected = List(
      TestSuite(
        "FooSuite", 
        List(
          TestResultWithOutput.Passed("Foo Suite.Bar Suite"),
          TestResultWithOutput.Passed("Foo Suite.Bar Suite.Baz"),
          TestResultWithOutput.Passed("Foo Suite.Bar Suite.Not Baz"),
          TestResultWithOutput.Passed("Foo Suite.Foo"),
          TestResultWithOutput.Passed("Foo Suite.Bar"),
        )
      )
    )

    assertEquals(result, expected)

  test("should parse test output with at least one failed test".ignore):
    assertEquals(true, false)
