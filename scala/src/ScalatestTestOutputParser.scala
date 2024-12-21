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
        // get the body of the failure message
        val body = failure.get.text

        // colorize the message red
        val messageWithColor = s"\u001b[31m${message}\u001b[0m"

        val messageWithAnalysis = s"${messageWithColor}\n\n${body}"

        TestResultWithOutput.Failed(
          s"${suiteName}.${name}",
          List(messageWithAnalysis)
        )
    }.toList

    List(TestSuite(suiteName, tests))
