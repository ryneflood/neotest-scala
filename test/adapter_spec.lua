require("setup_test_environment")

local utils = require("neotest-scala.utils")

describe("adapter", function()
    local adapter = require("neotest-scala.adapter")({
        get_scala_runner = function()
            return "scala-runner"
        end,
    })

    describe("#root", function()
        it("should match directory that contains a Mill project", function()
            local project_directory = utils.make_test_resources_path("projects/mill-project")

            local result = adapter.root(project_directory)

            assert.is_same(project_directory, result)
        end)

        it("should match directory that contains an SBT project", function()
            local project_directory = utils.make_test_resources_path("projects/sbt-project")

            local result = adapter.root(project_directory)

            assert.is_same(project_directory, result)
        end)

        it(
            "should not match a directory which doesn't contain a supported Scala project (in this case an NPM project)",
            function()
                local project_directory = utils.make_test_resources_path("projects/unsupported-project")

                local result = adapter.root(project_directory)

                assert.is_same(nil, result)
            end
        )
    end)

    describe("#is_test_file", function()
        it("should not match if the file does not end in .scala", function()
            local result = adapter.is_test_file("foo.bar.baz")

            assert.is_false(result)
        end)

        it("should match a filename which ends in ...Suite", function()
            local result = adapter.is_test_file("FooSuite.scala")

            assert.is_true(result)
        end)

        it("should match a filename which ends in ...Test", function()
            local result = adapter.is_test_file("FooTest.scala")

            assert.is_true(result)
        end)

        it("should match a filename which ends in ...Spec", function()
            local result = adapter.is_test_file("FooSpec.scala")

            assert.is_true(result)
        end)

        it("should not match a file whose name contains 'Spec', but not at the end of the file name", function()
            local result = adapter.is_test_file("FooSpecBar.scala")

            assert.is_false(result)
        end)

        it("should not match a file whose name contains 'Suite', but not at the end of the file name", function()
            local result = adapter.is_test_file("FooSuiteBar.scala")

            assert.is_false(result)
        end)

        it("should not match a file whose name contains 'Test', but not at the end of the file name", function()
            local result = adapter.is_test_file("FooTestBar.scala")

            assert.is_false(result)
        end)

        it("should match a file whose name ends in .test.scala", function()
            local result = adapter.is_test_file("Foo.test.scala")

            assert.is_true(result)
        end)
    end)
end)
