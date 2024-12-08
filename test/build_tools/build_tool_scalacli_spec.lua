require("setup_test_environment")

local async = require("nio").tests
local build_tool_sbt = require("neotest-scala.build_tools.build_tool_scala_cli")
local files = require("neotest.lib").files
local path = require("plenary.path")
local utils = require("neotest-scala.utils")

describe("sbt_build_tool", function()
    local build_sbt_file = [[
      //> using scala "3.5.0"
      //> using options -Werror -Wunused:all -Wvalue-discard -Wnonunit-statement
      //> using dep "dev.zio::zio:2.1.13"
      //> using dep "dev.zio::zio-test:2.1.13"
      //> using dep "dev.zio::zio-json:0.7.3"
      //> using dep "com.lihaoyi::os-lib:0.11.3"
    ]]

    describe("find_projects", function()
        async.it("should return a list of available projects", function()
            local fpath = vim.fn.tempname() .. ".sbt"
            files.write(fpath, build_sbt_file)

            local result = build_tool_sbt.find_projects(fpath)
            local expected = { "." }

            assert.is_same(expected, result)
        end)
    end)

    describe("get_project_root", function()
        it("should return the root of the project for the given file path, project `bar`", function()
            local result = build_tool_sbt.get_project_root(
                utils.make_test_resources_path("projects/scala-cli-project/bar/src/main/scala/app/Main.scala")
            )
            local expected = utils.make_test_resources_path("projects/scala-cli-project")

            assert.is_same(expected, result)
        end)
    end)

    describe("get_project_name", function()
        async.it("should return the project name for the given file in the `root` project", function()
            local result = build_tool_sbt.get_project_name(
                utils.make_test_resources_path("projects/scala-cli-project/src/main/scala/app/Main.scala")
            )
            local expected = "."

            assert.is_same(expected, result)
        end)
    end)
end)
