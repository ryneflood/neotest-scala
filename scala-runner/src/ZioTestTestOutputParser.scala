package neotest

object ZioTestTestOutputParser extends TestOutputParser:
  def parseTestOutput(testSuiteNames: List[String]) =
    removeSummary andThen splitIntoSuites(testSuiteNames) andThen parseSuites

  private[neotest] def stripColorCodes(line: String): String =
    line.replaceAll("\u001B\\[[;\\d]*m", "")

  private[neotest] def splitIntoSuites(testSuiteNames: List[String])(
      lines: List[String]
  ): Map[String, List[String]] =
    val emptyMap = Map.empty[String, List[String]]

    val starts =
      lines.zipWithIndex.foldLeft(List.empty[Int])((acc, lineAndIndex) =>
        val (line, index) = lineAndIndex

        if isTestSuite(testSuiteNames, line).isDefined then acc.appended(index)
        else acc
      )

    val startsAndEnds = starts.map(start =>
      val linesToRead = lines.drop(start + 1).map(stripColorCodes)

      val end =
        linesToRead.indexWhere(_.matches("^[0-9]+ tests(.+)*$"))

      (start, start + end)
    )

    startsAndEnds.foldLeft(emptyMap) { (acc, startAndEnd) =>
      val (start, end) = startAndEnd

      val regex = """^\+\s(.+)*$""".r

      val testSuiteName = stripColorCodes(lines(start)) match
        case regex(testName) =>
          testSuiteNames.find(testSuiteName => testSuiteName.contains(testName))
        case _ => throw Throwable("Test suite name not found")

      val output = lines.slice(start, end + 2)

      acc.updated(testSuiteName.get, output)
    }

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

  private def removeSummary(lines: List[String]): List[String] =
    val (before, _) = lines.span(line => !line.startsWith("=============="))

    before.toList
    
  private[neotest] def parseSuites(suites: Map[String, List[String]]): List[TestSuite] =
    suites.map: (key, value) =>
      parseTestSuite(key, value)
    .toList
    
  private def parseTestSuite(key: String, lines: List[String]): TestSuite =
    // we want to look through the lines until we find a test name (the start of the output for a test)
    // and then take the lines until we find another test name (the end of the output for that test)
    val testLocations = lines.zipWithIndex.map { case (line, index) =>
      (isPassedTest(line) orElse isFailedTest(
        line
      )).map(testResult => (testResult, index))
    }.flatten

    val hi = testLocations.zipWithIndex.map { (testLocation, index) =>
      val startsAt = testLocation._2
      // get the next test location
      val endsAt = testLocations.lift(index + 1).map(_._2)

      (startsAt, endsAt)
    }
    
    val hii = testLocations.zip(hi).map { (testLocation, testLocation2) =>
      val (startsAt, endsAt) = testLocation2

      val testOutput = endsAt match
        case Some(end) => lines.slice(startsAt, end)
        case None      => lines.slice(startsAt, lines.size)

      testLocation._1 match
        case TestResult.Passed(name) =>
          TestResultWithOutput.Passed(s"${key}.${name}", testOutput)
        case TestResult.Failed(name) =>
          TestResultWithOutput.Failed(s"${key}.${name}", testOutput)
    }

    TestSuite(key, hii)

  private def isPassedTest(line: String): Option[TestResult.Passed] =
    val regex = """\s+\+\s(.+)*$""".r

    // use regex to extract the test name from the line
    stripColorCodes(line) match
      case regex(testName) => Some(TestResult.Passed(testName.trim))
      case _ => None
      
  def isFailedTest(line: String): Option[TestResult] =
      val regex = """\s+\-\s(.+)*$""".r

      // use regex to extract the test name from the line
      stripColorCodes(line) match
        case regex(testName) => Some(TestResult.Failed(testName.trim))
        case _ => None
