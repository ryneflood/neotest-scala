package neotest

import zio.*
import zio.json.*

final case class TestSuite(
    name: String,
    tests: List[TestResultWithOutput]
)

enum TestStatus:
  case Passed, Failed

enum TestResult(val name: String):
  case Passed(override val name: String) extends TestResult(name)
  case Failed(override val name: String) extends TestResult(name)

enum TestResultWithOutput(val name: String, val output: List[String]):
  case Passed(override val name: String, override val output: List[String])
      extends TestResultWithOutput(name, output)
  case Failed(override val name: String, override val output: List[String])
      extends TestResultWithOutput(name, output)

final case class TestResultWithOutputFile(
    id: String,
    output: os.Path,
    status: TestStatus
)

object TestResultWithOutputFile:
  given JsonEncoder[TestStatus] = JsonEncoder.string.contramap(_.toString)
  given JsonEncoder[os.Path] = JsonEncoder.string.contramap(_.toString)
  given JsonEncoder[TestResultWithOutputFile] = DeriveJsonEncoder.gen
