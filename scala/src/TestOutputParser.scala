package neotest

trait TestOutputParser:
  def parseTestOutput(
      testSuiteName: String
  ): List[String] => List[TestSuite]
