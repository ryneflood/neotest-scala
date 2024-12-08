require("setup_test_environment")

local async = require("nio").tests
local build_tool_sbt = require("neotest-scala.build_tools.build_tool_sbt")
local files = require("neotest.lib").files
local path = require("plenary.path")
local utils = require("neotest-scala.utils")

describe("sbt_build_tool", function()
    local build_sbt_file = [[
        lazy val root = project.in(file("."))
            .settings(
                name := "Root Project",
                version := "1.0",
                libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test
            )

        lazy val foo = (project in file("foo"))
            .settings(
                name := "Foo Project",
                version := "1.0",
                libraryDependencies += "dev.zio" %% "zio-test" % "2.1.13" % Test,
                libraryDependencies += "dev.zio" %% "zio-test-sbt" % "2.1.13" % Test
            )
        
        lazy val oof = (not-a-match in file("foo"))

        lazy val bar = (project in file("bar"))
            .dependsOn(foo)
            .settings(
                name := "Bar Project",
                version := "1.0",
                libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test
            )
    ]]

    describe("find_projects", function()
        async.it("should return a list of available projects", function()
            local fpath = vim.fn.tempname() .. ".sbt"
            files.write(fpath, build_sbt_file)

            local result = build_tool_sbt.find_projects(fpath)
            local expected = { "root", "foo", "bar" }

            assert.is_same(expected, result)
        end)
    end)

    describe("get_test_framework", function()
        async.it(
            "should find the name of the test framework for a given project (project: foo, test framework: zio-test)",
            function()
                local root_directory = vim.fn.tempname()
                local fpath = root_directory .. "/build.sbt"

                path:new(root_directory):mkdir()
                files.write(fpath, build_sbt_file)

                local result = build_tool_sbt.get_test_framework(root_directory, "foo")

                assert.is_same(TEST_FRAMEWORKS.ZIO_TEST, result.type)
            end
        )

        async.it(
            "should find the name of the test framework for a given project (project: bar, test framework: munit)",
            function()
                local root_directory = vim.fn.tempname()
                local fpath = root_directory .. "/build.sbt"

                path:new(root_directory):mkdir()
                files.write(fpath, build_sbt_file)

                local result = build_tool_sbt.get_test_framework(root_directory, "bar")

                assert.is_same(TEST_FRAMEWORKS.MUNIT, result.type)
            end
        )
    end)

    describe("get_project_root", function()
        it("should return the root of the project for the given file path, project `bar`", function()
            local result = build_tool_sbt.get_project_root(
                utils.make_test_resources_path("projects/sbt-project/bar/src/main/scala/app/Main.scala")
            )
            local expected = utils.make_test_resources_path("projects/sbt-project")

            assert.is_same(expected, result)
        end)
    end)

    describe("get_project_name", function()
        async.it("should return the project name for the given file in the `root` project", function()
            local result = build_tool_sbt.get_project_name(
                utils.make_test_resources_path("projects/sbt-project/src/main/scala/app/Main.scala")
            )
            local expected = "root"

            assert.is_same(expected, result)
        end)

        async.it("should return the project name for the given file in the `bar` project", function()
            local result = build_tool_sbt.get_project_name(
                utils.make_test_resources_path("projects/sbt-project/bar/src/main/scala/app/Main.scala")
            )
            local expected = "bar"

            assert.is_same(expected, result)
        end)

        async.it("should return the project name for the given file in the `foo` project", function()
            local result = build_tool_sbt.get_project_name(
                utils.make_test_resources_path("projects/sbt-project/foo/src/main/scala/app/Main.scala")
            )
            local expected = "foo"

            assert.is_same(expected, result)
        end)

        async.it(
            "should return the project name for the given file when the project's name doesn't match its file directory",
            function()
                local result = build_tool_sbt.get_project_name(
                    utils.make_test_resources_path("projects/sbt-project/foo-baz/src/main/scala/app/Main.scala")
                )
                local expected = "baz"

                assert.is_same(expected, result)
            end
        )
    end)
end)
