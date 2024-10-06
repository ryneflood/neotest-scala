package neotest

import zio.*

object CommandlineArgumentsParser:
  def make =
    for
      args <- ZIOAppArgs.getArgs
      options = parseArgs(args)
    yield options

  def parseArgs(args: Chunk[String]) =
    val runner = args
      .grouped(2)
      .collectFirst { case Chunk("--runner", value) =>
        value
      }
      .get

    val outputDirectory = args
      .grouped(2)
      .collectFirst { case Chunk("--to", value) =>
        value
      }
      // FIXME: this should be a proper error
      .get

    val testSuites = args
      .grouped(2)
      .collect { case Chunk("-o", value) =>
        value
      }
      .toList

    val arguments = args
      .grouped(2)
      .collect {
        case Chunk(key, value) if key.startsWith("--") =>
          (key.drop(2), value)
      }
      .toList

    val project = arguments.collectFirst { case ("project", value) =>
      value
    }.get

    val singleTest = arguments.collectFirst { case ("single", value) =>
      value
    }

    val framework = arguments.collectFirst { case ("framework", value) =>
      value
    }.get

    Options(
      runner = runner,
      framework = framework,
      outputDirectory = os.Path(outputDirectory),
      project = project,
      testSuites = testSuites,
      singleTest = singleTest
    )
