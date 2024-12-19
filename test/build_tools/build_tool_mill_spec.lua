require("setup_test_environment")

local async = require("nio").tests
local build_tool_mill = require("neotest-scala.build_tools.build_tool_mill")
local utils = require("neotest-scala.utils")

describe("mill_build_tool", function()
    describe("get_project_name", function()
        it("should return the project name for the given file, project `foo`", function()
            local result = build_tool_mill.get_project_name(
                utils.make_test_resources_path("projects/mill-project/foo/test/src/foo/Main.scala")
            )
            local expected = "foo.test"

            assert.is_same(expected, result)
        end)

        it("should return the project name for the given file, project `bar`", function()
            local result = build_tool_mill.get_project_name(
                utils.make_test_resources_path("projects/mill-project/bar/test/src/bar/Main.scala")
            )
            local expected = "bar.test"

            assert.is_same(expected, result)
        end)
    end)

    describe("find_projects", function()
        it("should return all available projects which have tests", function()
            local result = build_tool_mill.find_projects(utils.make_test_resources_path("projects/mill-project"))
            local expected = {
                "bar",
                "foo",
            }

            assert.is_same(expected, result)
        end)
    end)

    describe("get_project_root", function()
        it("should return the root of the project for the given file path, project `foo`", function()
            local result = build_tool_mill.get_project_root(
                utils.make_test_resources_path("projects/mill-project/foo/test/src/foo/Main.scala")
            )
            local expected = utils.make_test_resources_path("projects/mill-project")

            assert.is_same(expected, result)
        end)
    end)

    describe("get_test_framework", function()
        async.it("should return the test framework (project: foo, framework: munit))", function()
            local result =
                build_tool_mill.get_test_framework(utils.make_test_resources_path("projects/mill-project"), "foo.test")

            local expected = TEST_FRAMEWORKS.MUNIT

            assert.is_same(expected, result)
        end)

        async.it("should return the test framework (project: bar, framework: munit)", function()
            local result =
                build_tool_mill.get_test_framework(utils.make_test_resources_path("projects/mill-project"), "bar.test")

            local expected = TEST_FRAMEWORKS.MUNIT

            assert.is_same(expected, result)
        end)

        async.it("should return the test framework (project: bar, framework: zio-test)", function()
            local result = build_tool_mill.get_test_framework(
                utils.make_test_resources_path("projects/mill-project-zio-test"),
                "bar.test"
            )

            local expected = TEST_FRAMEWORKS.ZIO_TEST

            assert.is_same(expected, result)
        end)

        async.it(
            "should return `nil` if no project exists with that name, or if it doesn't have a test module",
            function()
                local result = build_tool_mill.get_test_framework(
                    utils.make_test_resources_path("projects/mill-project"),
                    "foo-baz"
                )

                local expected = nil

                assert.is_same(expected, result)
            end
        )
    end)
end)
