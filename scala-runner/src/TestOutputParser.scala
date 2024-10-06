package neotest

trait TestOutputParser:
  def parseTestOutput(
      testSuiteNames: List[String]
  ): List[String] => List[TestSuite]
