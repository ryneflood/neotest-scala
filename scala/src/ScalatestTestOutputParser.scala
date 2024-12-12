package neotest

// output from Scalatest's runner, when all tests are successful, will look something like:
/** [32mFooSuite:[0m [32mFoo Suite[0m [32m Bar Suite[0m [32m - Baz[0m
  * [32m - Not Baz[0m [32m- Foo[0m [32m- Bar[0m Execution took 7ms 4
  * tests, 4 passed All tests in foo.bar.FooSuite passed
  *
  * The test execution was successfully closed.
  * =============================================== Total duration: 7ms All 1
  * test suites passed.
  * ===============================================
  */
object ScalatestTestOutputParser extends TestOutputParser:
  def parseTestOutput(
      x: String
  ): List[String] => List[TestSuite] =
    (output: List[String]) =>
      val testClassNames = output
        .flatMap(identifyTestClass)

      println("testClasses")
      println(testClassNames)

      val testClassName = testClassNames.head

      val testSuiteNames = output
        .flatMap(identifyTestSuite)

      val testSuiteName = testSuiteNames.head

      val testNames = output
        .flatMap(identifyTest)
        .map(testName =>
          TestResultWithOutput.Passed(
            s"$testClassName.$testSuiteName.$testName"
          )
        )

      List(
        TestSuite(
          testClassName,
          testNames
        )
      )
  private[neotest] def parseSuite(lines: List[String]): TestSuite =
    ???

  private def isTestSuite(line: String) =
    val testSuiteRegex = """\s?+\\[32m(.*)\\[0m""".r

    line match
      case testSuiteRegex(suiteName) => Some(suiteName.trim)
      case _                         => None

  private def isPassedTest(line: String): Option[String] =
    val passedTestRegex = """\\[32m\s{0,}-\s(.*)\\[0m""".r

    line match
      case passedTestRegex(testName) => Some(testName.trim)
      case _                         => None

  private[neotest] def findTestLocations(output: List[String]): List[Step] =
    def doStuff(
        name: String,
        line: String,
        lines: List[String],
        steps: List[Step],
        index: Integer
    ) = {
      val previousStep = steps.lastOption
      val indentationChange = previousStep match
        case None => Step.NoChange(index, name)
        case Some(step) =>
          val currentLineWithoutColorCodes = stripColorCodes(line)
          val previousLineWithoutColorCodes = stripColorCodes(step.line)

          val currentIndentation =
            currentLineWithoutColorCodes.takeWhile(_ == ' ').length
          val previousIndentation =
            previousLineWithoutColorCodes.takeWhile(_ == ' ').length

          if currentIndentation > previousIndentation then
            Step.Increment(index, currentLineWithoutColorCodes, name)
          else if currentIndentation < previousIndentation then
            Step.Decrement(index, currentLineWithoutColorCodes, name)
          else Step.NoChange(index, currentLineWithoutColorCodes, name)

      val updatedSteps = steps.appended(indentationChange)

      loop(lines, index + 1, updatedSteps)
    }

    def loop(
        lines: List[String],
        index: Int,
        steps: List[Step]
    ): List[Step] =
      if index >= lines.size then steps
      else
        // if this is the line we're interested in, then we'll add it to the Steps we've collected so far
        val currentLine = lines(index)

        val tuple = (isPassedTest(currentLine), isTestSuite(currentLine))

        tuple match
          case (Some(testName), _) =>
            doStuff(testName, currentLine, lines, steps, index)
          case (_, Some(suiteName)) =>
            doStuff(suiteName, currentLine, lines, steps, index)
          case _ => loop(lines, index + 1, steps)

    loop(output, 0, List.empty)

  final case class Context(
      stack: List[String],
      testSuite: TestSuite
  )

  private[neotest] def parseSteps(
      lines: List[String],
      steps: List[Step]
  ): TestSuite =
    steps.zipWithIndex
      .foldLeft(Context(List.empty, TestSuite("FooSuite", List.empty))) {
        (acc, curr) =>
          // if we have a NoChange --> Increment then we're describing a parent --> child relationship
          // (i.e. a test suite containing a test, or a test suite containing another test suite)
          curr match
            case (Step.Increment(_, _, name), index) =>
              val updatedStack = acc.stack.appended(name)

              acc.copy(
                stack = updatedStack
              )

            // if we're going up a level, then we're done with this Test Suite, so we'll pop it off the stack
            case (
                  Step.Decrement(
                    testOutputLocation,
                    line,
                    name,
                    indentationChange
                  ),
                  index
                ) =>
              // remove the last element from the stack
              val updatedStack = acc.stack.dropRight(indentationChange)
              val testName = updatedStack.mkString(".") + "." + name

              val test =
                if line.startsWith("\u001B[31m") then
                  // find the index of the beginning of the next test's output
                  val nextTestLocation =
                    steps.lift(index + 1).map(_.index).getOrElse(lines.length)

                  val failedTestOutput =
                    lines.slice(testOutputLocation, nextTestLocation + 1)

                  TestResultWithOutput.Failed(
                    testName,
                    failedTestOutput
                  )
                else
                  TestResultWithOutput.Passed(
                    testName
                  )

              val updatedTests = acc.testSuite.tests.appended(
                test
              )

              acc.copy(
                stack = updatedStack,
                testSuite = acc.testSuite.copy(tests = updatedTests)
              )

            // it looks like we've reached a test case
            case (Step.NoChange(testOutputLocation, line, name), index) =>
              if acc.stack.isEmpty then
                // push the test case onto the stack
                acc.copy(
                  stack = acc.stack.appended(name)
                )
              else
                // find out whether the test failed or password
                val testName = acc.stack.mkString(".") + "." + name

                val test =
                  if line.startsWith("\u001B[31m") then
                    // find the index of the beginning of the next test's output
                    val nextTestLocation =
                      steps.lift(index + 1).map(_.index).getOrElse(lines.length)

                    val failedTestOutput =
                      lines.slice(testOutputLocation + 1, nextTestLocation + 1)

                    TestResultWithOutput.Failed(
                      testName,
                      failedTestOutput
                    )
                  else
                    TestResultWithOutput.Passed(
                      testName
                    )

                val updatedTests = acc.testSuite.tests.appended(
                  test
                )

                acc.copy(
                  testSuite = acc.testSuite.copy(tests = updatedTests)
                )
      }
      .testSuite

  private def identifyTestSuite(line: String): Option[String] =
    val testSuiteRegex = """\s?+\\[32m(.*)\\[0m""".r

    line match
      case testSuiteRegex(suiteName) => Some(suiteName)
      case _                         => None

  private def identifyTestClass(line: String): Option[String] =
    val testRegex = """\s?+\\[32m(.*):\\[0m""".r

    line match
      case testRegex(suiteName) => Some(suiteName)
      case _                    => None

  def identifyTest(line: String): Option[String] =
    val testRegex = """\s?+\\[32m+-\s(.*)\\[0m""".r

    line match
      case testRegex(testName) => Some(testName)
      case _                   => None

  private[neotest] def stripColorCodes(line: String): String =
    line.replaceAll("\u001B\\[[;\\d]*m", "")
