package neotest

enum Step(val index: Int, val line: String, val name: String):
  case Increment(override val index: Int, override val line: String, override val name: String = "")
      extends Step(index, line, name)
  case Decrement(override val index: Int, override val line: String, override val name: String = "", val indentationChange: Integer = 1)
      extends Step(index, line, name)
  case NoChange(override val index: Int, override val line: String, override val name: String = "")
      extends Step(index, line, name)

object ZioTestTestOutputParser extends TestOutputParser:
  def parseTestOutput(testSuiteName: String) =
    removeSummary andThen splitIntoSuites(testSuiteName) andThen parseSuites(testSuiteName)

  private[neotest] def stripColorCodes(line: String): String =
    line.replaceAll("\u001B\\[[;\\d]*m", "")

  private[neotest] def splitIntoSuites(testSuiteNames: String)(
      lines: List[String]
  ) =
    val suiteIndexes =
      lines.zipWithIndex.foldLeft(List.empty[Int])((acc, curr) =>
        val (line, index) = curr

        if line.startsWith("Execution took") then acc.appended(index)
        else acc
      )

    val startsAndEnds =
      suiteIndexes.zipWithIndex.foldLeft(List.empty[(Int, Int)]) {
        (acc, curr) =>
          val (start, index) = curr

          if index == 0 then acc.appended(0, start)
          else acc.appended(suiteIndexes(index - 1) + 1, start)
      }

    startsAndEnds.map: (start, end) =>
      lines
        .slice(start, end)
        .dropWhile(line => !stripColorCodes(line).trim.startsWith("+"))

  // basically each test suite starts with a line that looks like this:
  // + TestSuiteName
  // but, also, nested test suites can look like this:
  // + TestSuiteName
  //   + InnerTestSuite
  // basically, the nested suite is indented by 2 spaces
  // but, then, the test name(s) will also be indented, like:
  // + TestSuiteName
  //   + InnerTestSuite
  //     + Foo Bar Test
  // but, we can tell if a line is a test because the next line won't be indented, unless it's a failed test
  // in the case that it's a test case, the indented line will start with "✗"
  //
  // so, written as code, we could check each pair of lines, and if the second line is indented, then it's either
  // a nested test suite or a test case

  private[neotest] def mightBeTestSuite(
      testSuiteNames: List[String],
      line: String
  ): Option[String] =
    val lineWithoutColorCodes = stripColorCodes(line).trim

    val regex = """^\+\s(.+)*$""".r

    lineWithoutColorCodes match
      case regex(testName) =>
        testSuiteNames.find(testSuiteName => testSuiteName.contains(testName))
      case _ => None

  private[neotest] def isTestSuite(
      testSuiteNames: List[String],
      line: String
  ): Option[String] =
    val lineWithoutColorCodes = stripColorCodes(line)

    val regex = """^\+\s(.+)*$""".r

    lineWithoutColorCodes match
      case regex(testName) =>
        testSuiteNames.find(testSuiteName => testSuiteName.contains(testName))
      case _ => None

  private[neotest] def removeSummary(lines: List[String]): List[String] =
    val (before, _) = lines.span(line => !line.startsWith("=============="))

    before.toList

  private[neotest] def parseSuites(
      testSuiteName: String)(
      suites: List[List[String]]
  ): List[TestSuite] =
    suites.map(parseTestSuite(testSuiteName, _))

  private def findTestPositions(lines: List[String]): List[Step] =
    def loop(
        lines: List[String],
        index: Int,
        steps: List[Step]
    ): List[Step] =
      if index >= lines.size then steps
      else
        // if this is line we're interested in, then we'll add it to the Steps we've collected so far
        val currentLine = lines(index)
        if isPassedTest(currentLine).isDefined || isFailedTest(
            currentLine
          ).isDefined
        then
          val previousStep = steps.lastOption
          val indentationChange = previousStep match
            case None => Step.NoChange(index, currentLine)
            case Some(step) =>
              val currentIndentation = currentLine.takeWhile(_ == ' ').length
              val previousIndentation = step.line.takeWhile(_ == ' ').length

              if currentIndentation > previousIndentation then
                Step.Increment(index, currentLine)
              else if currentIndentation < previousIndentation then
                Step.Decrement(index, currentLine)
              else Step.NoChange(index, currentLine)

          val updatedSteps = steps.appended(indentationChange)

          loop(lines, index + 1, updatedSteps)
        else loop(lines, index + 1, steps)

    loop(lines, 0, List.empty)

  private def parseTestPositions(testSuiteName: String, lines: List[String])(
      steps: List[Step]
  ): TestSuite =
    def loop(
        index: Int,
        steps: List[Step],
        context: List[String] = List.empty,
        testNames: List[TestResultWithOutput] = List.empty
    ): TestSuite =
      val currentStep = steps.lift(index)
      val nextStep = steps.lift(index + 1)

      (currentStep, nextStep) match
        case (Some(current), Some(next)) =>
          next match
            case Step.Increment(_, line, _) =>
              val testName = stripColorCodes(current.line).trim.drop(2)
              val updatedContext = testName :: context

              loop(index + 1, steps, updatedContext, testNames)
            case Step.Decrement(_, line, _, indentationChange) =>
              val updatedTestNames = if isFailedTest(current.line).isDefined then
                val currentIndentation = current.line.takeWhile(_ == ' ').length

                val endOfTestOutputIndex = lines
                  .drop(index + 1)
                  .indexWhere(line =>
                    val lineIndentation = line.takeWhile(_ == ' ').length
                    stripColorCodes(line).trim
                      .startsWith(
                        "at "
                      ) && lineIndentation > currentIndentation
                  )

                val testOutput =
                  lines.drop(index + 1).take(endOfTestOutputIndex + 1)

                val testName = stripColorCodes(current.line).trim.drop(2)
                testNames.appended(
                  TestResultWithOutput.Failed(
                    testSuiteName + "." + context.reverse.mkString(".") + "." + testName,
                    testOutput
                  )
                )
              else
                val testName = stripColorCodes(current.line).trim.drop(2)

                  testNames.appended(
                    TestResultWithOutput.Passed(
                      testSuiteName + "." + context.reverse.mkString(".") + "." + testName
                    )
                  )

              val updatedContext = context.tail

              loop(index + 1, steps, updatedContext, updatedTestNames)
            case Step.NoChange(_, line, _) =>
              val updatedTestNames = 
                if isFailedTest(current.line).isDefined then
                  val currentIndentation = current.line.takeWhile(_ == ' ').length


                  val endOfTestOutputIndex = lines
                    .drop(index + 1)
                    .indexWhere(line =>
                      val lineIndentation = line.takeWhile(_ == ' ').length
                      stripColorCodes(line).trim
                        .startsWith(
                          "at "
                        ) && lineIndentation > currentIndentation
                    )

                  val testOutput =
                    lines.drop(index + 1).take(endOfTestOutputIndex + 1)
                  
                  val testName = stripColorCodes(current.line).trim.drop(2)
                    testNames.appended(
                      TestResultWithOutput.Failed(
                        testSuiteName + "."  + context.reverse.mkString(".") + "." + testName,
                        testOutput
                      )
                    )
                else // it's a passed test
                  val testName = stripColorCodes(current.line).trim.drop(2)
                    testNames.appended(
                      TestResultWithOutput.Passed(
                        testSuiteName + "." + context.reverse.mkString(".") + "." + testName
                      )
                    )

              loop(index + 1, steps, context, updatedTestNames)
        case (Some(current), None) =>
          val updatedTestNames = 
            if isFailedTest(current.line).isDefined then
              val currentIndentation = current.line.takeWhile(_ == ' ').length

              val endOfTestOutputIndex = lines
                .drop(index + 1)
                .indexWhere(line =>
                  val lineIndentation = line.takeWhile(_ == ' ').length
                  stripColorCodes(line).trim
                    .startsWith(
                      "at "
                    ) && lineIndentation > currentIndentation
                )

              val testOutput =
                lines.drop(index + 1).take(endOfTestOutputIndex + 1)
              
              val testName = stripColorCodes(current.line).trim.drop(2)
                testNames.appended(
                  TestResultWithOutput.Failed(
                    testSuiteName + "." + context.reverse.mkString(".") + "." + testName,
                    testOutput
                  )
                )
            else // it's a passed test
              val testName = stripColorCodes(current.line).trim.drop(2)
                testNames.appended(
                  TestResultWithOutput.Passed(
                    testSuiteName + "." + context.reverse.mkString(".") + "." + testName
                  )
                )

          TestSuite(testSuiteName, updatedTestNames)

        case _ => TestSuite(testSuiteName, testNames)

    loop(0, steps)

  private[neotest] def parseTestSuite(testSuiteName: String, lines: List[String]): TestSuite =
    val hi = findTestPositions andThen parseTestPositions(testSuiteName, lines)

    hi(lines)

  private def isPassedTest(line: String): Option[TestResult.Passed] =
    val regex = """\s*\+\s(.+)*$""".r

    // use regex to extract the test name from the line
    stripColorCodes(line) match
      case regex(testName) => Some(TestResult.Passed(testName.trim))
      case _               => None

  def isFailedTest(line: String): Option[TestResult] =
    val regex = """\s*\-\s(.+)*$""".r

    // use regex to extract the test name from the line
    stripColorCodes(line) match
      case regex(testName) => Some(TestResult.Failed(testName.trim))
      case _               => None
