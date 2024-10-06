package neotest

object MunitTestOutputParser extends TestOutputParser:
  def parseTestOutput(testSuiteNames: List[String]) =
   (removeSummary andThen splitIntoSuites(testSuiteNames) andThen parseSuites)

  def stripColorCodes(line: String): String =
    line.replaceAll("\u001B\\[[;\\d]*m", "")
  
  def isPassedTest(line: String): Option[TestResult] =
      val regex = """\s+\+\s(.+)[0-9]+\.[0-9]*s""".r

      // use regex to extract the test name from the line
      stripColorCodes(line) match
        case regex(testName) => Some(TestResult.Passed(testName.trim))
        case _ => None
        
  def isFailedTest(line: String): Option[TestResult] =
    val regex = """==>\s*X\s(.+)\s+[0-9]*\.[0-9].*""".r
          
      // use regex to extract the test name from the line
    stripColorCodes(line) match
      case regex(testName) => Some(TestResult.Failed(testName.trim))
      case _ => None
  
  private def splitIntoSuites(testSuiteNames: List[String])(lines: List[String]): Map[String, List[String]] =
    val emptyMap = Map.empty[String, List[String]]

    lines.foldLeft(emptyMap) { (acc, line) =>
      // FIXME: this is a really naive way to check if a line is a test suite
      // and then drop the trailing `:` character from the end of the line
      // this would obviously be much better as a regex
      if isTestSuite(testSuiteNames, line) then acc.updated(stripColorCodes(line).dropRight(1), List.empty)
      else {
        if acc.keys.lastOption.isDefined then
          acc.updated(acc.keys.last, acc.values.last :+ line)
        else acc
      }
    }
    
  private def parseSuites(suites: Map[String, List[String]]): List[TestSuite] =
    suites.map: (key, value) =>
      parseTestSuite(key, value)
    .toList
    
  private def removeSummary(lines: List[String]): List[String] =
    val (before, _) = lines.span(line => !line.startsWith("========"))

    before.toList
    
  private def isTestSuite(testSuiteNames: List[String], line: String): Boolean =
    // FIXME: obviously this should be a regex
    // FIXME: obviously this shouldn't be hard coded
    val lineWithoutColorCodes = stripColorCodes(line)

    testSuiteNames.exists(testSuiteName =>
      lineWithoutColorCodes.startsWith(testSuiteName)
    )
    
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
          TestResultWithOutput.Failed(name, testOutput)
    }

    TestSuite(key, hii)
