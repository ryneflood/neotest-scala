package neotest

import java.util.UUID

object TestOutputWriter:
  def writeSuitesToOutputFiles(
      outputDirectory: os.Path,
      suites: List[TestSuite]
  ): List[TestResultWithOutputFile] =
    suites.flatMap(writeOutputToFile(outputDirectory, _))

  def writeOutputToFile(
      outputDirectory: os.Path,
      testSuite: TestSuite
  ): List[TestResultWithOutputFile] =
    testSuite.tests.map { test =>
      val filePath = outputDirectory / s"${UUID.randomUUID().toString.take(8)}"
      os.write(filePath, test.output.mkString("\n"))

      test match
        case TestResultWithOutput.Passed(name, _) =>
          TestResultWithOutputFile(name, filePath, TestStatus.Passed)
        case TestResultWithOutput.Failed(name, _) =>
          TestResultWithOutputFile(name, filePath, TestStatus.Failed)
    }
