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
object ScalatestXmlTestOutputParser extends TestOutputParser:
  def loadAndParseXmlFiles(testSuiteName: String) =
    // load the XML files from /tmp/scalatest-out and process them one-by-one
    // val xmlFiles = os.list(os.root / "tmp" / "scalatest-out").toList

    var xmlFiles = List.empty[os.Path]

    // loop until we find the XML files
    while xmlFiles.isEmpty do
      Thread.sleep(10)
      xmlFiles = os.list(os.root / "tmp" / "scalatest-out").toList

    xmlFiles
      .map(file => parseTestOutput(testSuiteName)(os.read.lines(file).toList))
      .flatten

  def parseTestOutput(testSuiteName: String) =
    (lines: List[String]) => parseTestSuite(testSuiteName)(lines)

  def parseTestSuite(suiteName: String)(output: List[String]) =
    // parse output as an XML document
    val xml = scala.xml.XML.loadString(output.mkString("\n"))

    val root = xml.head

    val testCases = root \ "testcase"

    val suiteName = root.attribute("name").get.text

    val tests = testCases.map { testCase =>
      val name = testCase.attribute("name").get.text
      val failure = (testCase \ "failure").headOption

      if failure.isEmpty then
        TestResultWithOutput.Passed(s"${suiteName}.${name}")
      else
        val message = failure.get.attribute("message").get.text

        TestResultWithOutput.Failed(s"${suiteName}.${name}", List(message))
    }.toList

    List(TestSuite(suiteName, tests))

object ScalatestTestOutputParser extends TestOutputParser:
  def parseTestOutput(suiteName: String) =
    (output: List[String]) =>
      // find the locations of the test suites
      // val testSuiteLocations = identifyTestSuiteLocations(output)
      // split the output into separate test suites
      // val testSuites = splitSuites(output.drop(1))

      // println("testSuites: " + testSuites)

      // parse each test suite
      // val parsedTestSuites = testSuites.map(parseSingleTestSuite(suiteName))

      // parsedTestSuites.flatten
      List.empty

  def parseSingleTestSuite(
      testClassName: String
  ): List[String] => List[TestSuite] =
    (output: List[String]) =>
      val steps = findTestLocations(output)
      val parsedSuite = parseSteps(output, steps)

      List(parsedSuite)

  private def isTestSuite(line: String) =
    val testSuiteRegex = """\s?+\\[3[1|2]m(.*)\\[0m""".r

    line match
      case testSuiteRegex(suiteName) => Some(suiteName.trim)
      case _                         => None

  private def isTestCase(line: String): Option[String] =
    val passedTestRegex = """\\[3[1|2]m\s{0,}-\s(.*)\\[0m""".r

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
        case None => Step.NoChange(index, line, name)
        case Some(step) =>
          val currentLineWithoutColorCodes = stripColorCodes(line)
          val previousLineWithoutColorCodes = stripColorCodes(step.line)

          val currentIndentation =
            currentLineWithoutColorCodes.takeWhile(_ == ' ').length
          val previousIndentation =
            previousLineWithoutColorCodes.takeWhile(_ == ' ').length

          if currentIndentation > previousIndentation then
            Step.Increment(index, line, name)
          else if currentIndentation < previousIndentation then
            Step.Decrement(index, line, name)
          else Step.NoChange(index, line, name)

      steps.appended(indentationChange)
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

        val tuple = (isTestCase(currentLine), isTestSuite(currentLine))

        tuple match
          case (Some(testName), _) =>
            val updatedSteps =
              doStuff(testName, currentLine, lines, steps, index)

            val currentIndentation = currentLine.takeWhile(_ == ' ').length

            def indentationIsLessThanCurrentLine(line: String) =
              line.takeWhile(_ == ' ').length < currentIndentation

            val nextIndex =
              // start at the current line
              lines
                .drop(index + 1)
                .zipWithIndex
                .find { case (line, _) =>
                  isTestCase(
                    line
                  ).isDefined || indentationIsLessThanCurrentLine(line)
                }
                .map(_._2)
                .map(_ + index + 1)
                .getOrElse(lines.length)

            loop(lines, nextIndex, updatedSteps)
          case (_, Some(suiteName)) =>
            val updatedSteps =
              doStuff(suiteName, currentLine, lines, steps, index)

            loop(lines, index + 1, updatedSteps)
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
      // FIXME: obviously don't hard code "FooSuite" in there
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
              val testName = updatedStack
                .mkString(".") + "." + name.replace("*** FAILED ***", "").trim

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
                val testName = acc.stack
                  .mkString(".") + "." + name.replace("*** FAILED ***", "").trim

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

  private[neotest] def identifyTestSuite(line: String): Option[String] =
    val testSuiteRegex = """\s?+\\[32m(.*)\\[0m""".r

    line match
      case testSuiteRegex(suiteName) => Some(suiteName)
      case _                         => None

  private[neotest] def identifyTestClass(line: String): Option[String] =
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

  private[neotest] def splitSuites(lines: List[String]) =
    val testSuiteLocations = identifyTestSuiteLocations(lines)
    val testSuiteIndexes = testSuiteLocations.map(_.index)

    // split the input at the specified indexes
    val testSuites =
      testSuiteIndexes
        .grouped(2)
        .foldLeft(List.empty[List[String]])((acc, curr) =>
          curr match
            case List(start, end) =>
              val suite = lines.slice(start, end)

              acc.appended(suite)
            case _ => acc
        )

    // take the rest of the lines
    val suite = lines.slice(testSuiteIndexes.last, lines.length)

    val x = testSuites.appended(suite)

    x

  private[neotest] def identifyTestSuiteLocations(lines: List[String]) =
    lines.zipWithIndex
      .foldLeft(List.empty[Step])((acc, curr) =>
        val (line, index) = curr
        // check if this line is a top-level test suite
        // basically, if it's not indented and it's green or red text then it should be a test suite
        val testSuiteRegex = """^\\[3[1|2]m(.*)\\[0m""".r

        line match
          case testSuiteRegex(suiteName)
              if !suiteName.startsWith(" ") && !suiteName.startsWith("-") =>
            acc.appended(Step.NoChange(index, line, suiteName))
          case _ => acc
      )
