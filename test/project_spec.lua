require("setup_test_environment")

local async = require("nio").tests
local project = require("neotest-scala.project")
local project_framework_mill = require("neotest-scala.project_framework_mill")

describe("#project", function()
    describe("#get_project_type", function()
        it("sould return Mill if the directory contains a Mill project", function()
            local result = project.get_project_type(
                "/home/ryne/workspace/@rocketpants/neovim/neotest-scala/test/resources/projects/mill-project"
            )
            local expected = "mill"

            assert.is_same(expected, result)
        end)

        it("sould return SBT if the directory contains a SBT project", function()
            local result = project.get_project_type(
                "/home/ryne/workspace/@rocketpants/neovim/neotest-scala/test/resources/projects/sbt-project"
            )
            local expected = "sbt"

            assert.is_same(expected, result)
        end)
    end)

    describe("#get_project_framework", function()
        it("should return the Mill framework if given the path to a Mill project", function()
            local result = project.get_project_framework(
                "/home/ryne/workspace/@rocketpants/neovim/neotest-scala/test/resources/projects/mill-project"
            )

            local expected = PROJECT_TYPES.MILL

            assert.is_same(expected, result.type)
        end)

        it("should return the SBT framework if given the path to a SBT project", function()
            local result = project.get_project_framework(
                "/home/ryne/workspace/@rocketpants/neovim/neotest-scala/test/resources/projects/sbt-project"
            )

            local expected = PROJECT_TYPES.SBT

            assert.is_same(expected, result.type)
        end)
    end)
end)

describe("Mill Project Framework", function()
    describe("#find_projects", function()
        it("should return a list of projects", function()
            local result = project_framework_mill.find_projects(
                "/home/ryne/workspace/@rocketpants/neovim/neotest-scala/test/resources/projects/mill-project"
            )
            local expected = { "bar", "foo" }

            assert.is_same(expected, result)
        end)
    end)

    describe("#find_project_name", function()
        it("should return the project name for the given file, project `foo`", function()
            local result = project_framework_mill.get_project_name(
                "/home/ryne/workspace/@rocketpants/neovim/neotest-scala/test/resources/projects/mill-project/foo/test/src/foo/Main.scala"
            )
            local expected = "foo.test"

            assert.is_same(expected, result)
        end)

        it("should return the project name for the given file, project `bar`", function()
            local result = project_framework_mill.get_project_name(
                "/home/ryne/workspace/@rocketpants/neovim/neotest-scala/test/resources/projects/mill-project/bar/test/src/baz/Main.scala"
            )
            local expected = "bar.test"

            assert.is_same(expected, result)
        end)
    end)

    describe("#get_test_framework", function()
        async.it("should return the test framework by parsing the build.sc file, `foo`, project", function()
            local result = project_framework_mill.get_test_framework(
                "/home/ryne/workspace/@rocketpants/neovim/neotest-scala/test/resources/projects/mill-project",
                "foo"
            )

            local expected = TEST_FRAMEWORKS.MUNIT

            assert.is_same(expected, result)
        end)

        async.it("should return the test framework by parsing the build.sc file, `bar` project", function()
            local result = project_framework_mill.get_test_framework(
                "/home/ryne/workspace/@rocketpants/neovim/neotest-scala/test/resources/projects/mill-project",
                "bar"
            )

            local expected = TEST_FRAMEWORKS.MUNIT

            assert.is_same(expected, result)
        end)

        async.it(
            "should return `nil` if no project exists with that name, or if it doesn't have a test module",
            function()
                local result = project_framework_mill.get_test_framework(
                    "/home/ryne/workspace/@rocketpants/neovim/neotest-scala/test/resources/projects/mill-project",
                    "foo-baz"
                )

                local expected = nil

                assert.is_same(expected, result)
            end
        )
    end)
end)
