require("setup_test_environment")

local project = require("neotest-scala.project")
local utils = require("neotest-scala.utils")
local types = require("neotest-scala.types")

describe("project", function()
    describe("get_project_type", function()
        it("should return Mill if the directory contains a Mill project", function()
            local result = project.get_project_type(utils.make_test_resources_path("projects/mill-project"))
            local expected = "mill"

            assert.is_same(expected, result)
        end)

        it("sould return SBT if the directory contains a SBT project", function()
            local result = project.get_project_type(utils.make_test_resources_path("projects/sbt-project"))
            local expected = "sbt"

            assert.is_same(expected, result)
        end)

        it("sould return Scala CLI if the directory contains a Scala CLI project", function()
            local result = project.get_project_type(utils.make_test_resources_path("projects/scala-cli-project"))
            local expected = "scala-cli"

            assert.is_same(expected, result)
        end)
    end)

    describe("get_build_tool", function()
        it("should return the SBT framework if given the path to a SBT project", function()
            local result = project.get_project_build_tool(utils.make_test_resources_path("projects/sbt-project"))

            local expected = types.PROJECT_TYPES.SBT

            assert.is_same(expected, result.type)
        end)

        it("should return Mill if the given the path to a Mill project", function()
            local result = project.get_project_build_tool(utils.make_test_resources_path("projects/mill-project"))

            local expected = types.PROJECT_TYPES.MILL

            assert.is_same(expected, result.type)
        end)

        it("should return Scala CLI if the given the path to a Scala CLI project", function()
            local result = project.get_project_build_tool(utils.make_test_resources_path("projects/scala-cli-project"))

            local expected = types.PROJECT_TYPES.SCALA_CLI

            assert.is_same(expected, result.type)
        end)
    end)
end)
