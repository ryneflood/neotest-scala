require("setup_test_environment")

local types = require("neotest-scala.types")
local async = require("nio").tests
local command_builder = require("neotest-scala.command_builders.bloop_test_command_builder")

describe("Bloop Test Command Builder", function()
    -- the Scala Test Runner expects a command which looks like:
    -- scala-runner --runner bloop --project foo.test --framework zio-test --only foo.bar.FooTests --only foo.bar.BarTests --to /tmp
    --
    -- we have:
    -- --> scala-runner: the name of the binary
    -- --> --runner: the name of the test runner; bloop, sbt, scala-cli, etc.
    -- --> --project: the name of the project, as prescribed by the build tool
    -- --> --framework: the test framework, like zio-test, munit, scalatest, specs2, etc.
    -- --> --only: the fully-qualified name of the class(es) for the test runner to load. Can be specified multiple times
    -- --> --to: the path to write the test results to
    describe("build_command", function()
        async.it("should prepare a test runner command for a single Class", function()
            local expected =
                "scala-runner --runner bloop --project foo.test --framework zio-test --only foo.bar.FooTests --to /tmp --kind namespace"

            local parsed_position = {
                type = "namespace",
                only = { "foo.bar.FooTests" },
                test_framework = types.TEST_FRAMEWORKS.ZIO_TEST,
                position = nil,
            }

            local result = command_builder.build_command("scala-runner", "foo.test", "bloop", parsed_position)

            assert.is_same(expected, result)
        end)

        async.it("should prepare a test runner command for multiple Classes", function()
            local expected =
                "scala-runner --runner bloop --project foo.test --framework zio-test --only foo.bar.FooTests --only foo.bar.BarTests --to /tmp --kind namespace"

            local parsed_position = {
                type = "namespace",
                only = { "foo.bar.FooTests", "foo.bar.BarTests" },
                test_framework = types.TEST_FRAMEWORKS.ZIO_TEST,
                positions = nil,
            }

            local result = command_builder.build_command("scala-runner", "foo.test", "bloop", parsed_position)

            assert.is_same(expected, result)
        end)

        async.it("should prepare a test runner command for a single test", function()
            local expected =
                'scala-runner --runner bloop --project foo.test --framework zio-test --only foo.bar.FooTests --test "Foo Bar Test" --to /tmp --kind test'

            local parsed_position = {
                type = "test",
                only = { "foo.bar.FooTests" },
                test_framework = types.TEST_FRAMEWORKS.ZIO_TEST,
                position = {
                    id = "foo.bar.FooTests.Foo Bar Test",
                    name = "Foo Bar Test",
                },
            }

            local result = command_builder.build_command("scala-runner", "foo.test", "bloop", parsed_position)

            assert.is_same(expected, result)
        end)

        async.it("should prepare a test runner command for a single test (Munit)", function()
            local expected =
                'scala-runner --runner bloop --project foo.test --framework munit --only foo.bar.FooTests --test "foo.bar.FooTests.Foo Bar Test" --to /tmp --kind test'

            local parsed_position = {
                type = "test",
                only = { "foo.bar.FooTests" },
                test_framework = types.TEST_FRAMEWORKS.MUNIT,
                position = {
                    id = "foo.bar.FooTests.Foo Bar Test",
                    name = "Foo Bar Test",
                },
            }

            local result = command_builder.build_command("scala-runner", "foo.test", "bloop", parsed_position)

            assert.is_same(expected, result)
        end)

        async.it("should prepare a test runner command for a single test (Scalatest)", function()
            -- for scalatest, we need to pass the test name separated by spaces
            -- so, it should look something like:
            -- --only foo.bar.FooSuite --test "Foo Suite Bar Suite Baz"
            local expected =
                'scala-runner --runner bloop --project foo.test --framework scalatest --only foo.bar.FooSuite --test "Foo Suite Bar Suite Baz" --to /tmp --kind test'

            local parsed_position = {
                type = "test",
                only = { "foo.bar.FooSuite" },
                position = {
                    id = "foo.bar.FooSuite.Foo Suite.Bar Suite.Baz",
                    name = "Baz",
                    parent = {
                        id = "foo.bar.FooSuite.Foo Suite.Bar Suite",
                        name = "Bar Suite",
                        parent = {
                            id = "foo.bar.FooSuite.Foo Suite",
                            name = "Foo Suite",
                            parent = {
                                id = "foo.bar.FooSuite",
                                name = "FooSuite",
                                parent = nil,
                            },
                        },
                    },
                },
                test_framework = types.TEST_FRAMEWORKS.SCALATEST,
            }

            local result = command_builder.build_command("scala-runner", "foo.test", "bloop", parsed_position)

            assert.is_same(expected, result)
        end)

        async.it("should prepare a test runner command for a Test Suite (Scalatest)", function()
            -- for scalatest, we need to pass the test name separated by spaces
            -- so, it should look something like:
            -- --only foo.bar.FooSuite --test "Foo Suite Bar Suite"
            local expected =
                'scala-runner --runner bloop --project foo.test --framework scalatest --only foo.bar.FooSuite --test "Foo Suite Bar Suite" --to /tmp --kind namespace'

            local parsed_position = {
                type = "namespace",
                only = { "foo.bar.FooSuite" },
                position = {
                    id = "foo.bar.FooSuite.Foo Suite.Bar Suite",
                    name = "Bar Suite",
                    parent = {
                        id = "foo.bar.FooSuite.Foo Suite",
                        name = "Foo Suite",
                        parent = {
                            id = "foo.bar.FooSuite",
                            name = "FooSuite",
                            parent = nil,
                        },
                    },
                },
                test_framework = types.TEST_FRAMEWORKS.SCALATEST,
            }

            local result = command_builder.build_command("scala-runner", "foo.test", "bloop", parsed_position)

            assert.is_same(expected, result)
        end)
    end)
end)
