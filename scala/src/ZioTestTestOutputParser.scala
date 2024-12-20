package neotest

object ZiotestTeamcityOutputParser:
  final case class TestSuiteName(value: String) extends AnyVal

  object TeamcityOutput:
    object TestSuite:
      def unapply(line: String): Option[String] =
        val pattern = """##teamcity\[testSuiteStarted name='(.+)'\]""".r

        line match
          case pattern(name) => Some(name)
          case _             => None

    object TestCase:
      def unapply(line: String): Option[String] =
        val pattern =
          """##teamcity\[testFinished name='(.+)' duration='.+'\]""".r

        line match
          case pattern(name) => Some(name)
          case _             => None

    object FailedTestCase:
      def unapply(line: String): Option[(String, String, String)] =
        val pattern =
          """##teamcity\[testFailed name='(.+)' message='(.+)' details='(.+)'\]""".r

        line match
          case pattern(name, message, details) => Some((name, message, details))
          case _                               => None

    object TestSuiteFinished:
      def unapply(line: String): Option[String] =
        val pattern = """##teamcity\[testSuiteFinished name='(.+)'\]""".r

        line match
          case pattern(name) => Some(name)
          case _             => None

  final case class Context(
      testClassName: String,
      stack: List[TestSuiteName],
      testSuite: TestSuite
  ):
    def pushTestSuite(testSuiteName: TestSuiteName) =
      copy(stack = stack.appended(testSuiteName))

    def popTestSuite() =
      copy(stack = stack.dropRight(1))

    def addPassedTest(name: String) =
      val testId = makeTestId(name)
      copy(testSuite =
        testSuite.copy(tests =
          testSuite.tests.appended(TestResultWithOutput.Passed(testId))
        )
      )

    def addFailedTest(name: String, message: String, details: String) =
      val testId = makeTestId(name)

      val lines =
        """\[(\d+)m""".r
          .replaceAllIn(details, m => s"\u001B[${m.group(1)}m")
          .split("\\|n")
          .toList

      copy(testSuite =
        testSuite.copy(tests =
          testSuite.tests.appended(
            TestResultWithOutput.Failed(testId, lines)
          )
        )
      )

    private def makeTestId(testName: String) =
      testClassName + "." + stack.map(_.value).mkString(" ") + " " + testName

  def parseTestOutput(testSuiteName: String) =
    removeSummary andThen parseTeamCityOutput(testSuiteName)

  private[neotest] def parseTeamCityOutput(testSuiteName: String)(
      lines: List[String]
  ) =
    val context = lines.foldLeft(
      Context(testSuiteName, List.empty, TestSuite(testSuiteName, List.empty))
    ) { (acc, curr) =>
      curr match
        case TeamcityOutput.TestSuite(suiteName) =>
          acc.pushTestSuite(TestSuiteName(suiteName))

        case TeamcityOutput.FailedTestCase(testName, message, details) =>
          acc.addFailedTest(testName, message, details)

        case TeamcityOutput.TestCase(testName) =>
          acc.addPassedTest(testName)

        case TeamcityOutput.TestSuiteFinished(_) =>
          acc.popTestSuite()

        case _ => acc
    }

    context.testSuite

  private[neotest] def removeSummary(lines: List[String]): List[String] =
    val (before, _) = lines.span(line => !line.startsWith("=============="))

    before.toList
