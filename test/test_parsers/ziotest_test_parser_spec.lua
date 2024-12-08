require("setup_test_environment")

local files = require("neotest.lib").files
local async = require("nio").tests
local position = require("neotest-scala.position")
local types = require("neotest-scala.types")
local framework_adapter = require("neotest-scala.test_parsers.ziotest_test_parser")

-- TODO: obviously this isn't a "single" spec anymore
local single_test_spec = [[
    package bar

    import bar.Hello

    import zio.test._

    object FooSpec extends ZIOSpecDefault:
        def spec =
            suite("Foo")(
                test("Foo Bar") {
                    assertTrue(Hello.msg == "Hello World, from Bar")
                },
                test("Bar Foo") {
                    assertTrue(Hello.msg == "Hello World, from Bar")
                }
            )
            
    object BarSpec extends zio.test.ZIOSpecDefault:
        def spec =
            suite("Bar")(
                test("Test But Not a Suite") {
                    assertTrue(Hello.msg == "Hello World, from Bar")
                }
            )
]]

local function assert_has_position(tree, pos_id)
    assert(position.has_position(tree, pos_id), "Position " .. pos_id .. " not found in tree " .. vim.inspect(tree))
end

local function assert_has_positions(spec, pos_ids)
    local fpath = vim.fn.tempname() .. ".scala"
    files.write(fpath, spec)

    local tree = framework_adapter.discover_positions(fpath)

    for _, pos_id in ipairs(pos_ids) do
        assert_has_position(tree, pos_id)
    end
end

describe("ZIO Test Framework", function()
    describe("parse_tree", function()
        async.it("should be able to parse a File and find the ZIOSpecDefaults", function()
            local fpath = vim.fn.tempname() .. ".scala"
            files.write(fpath, single_test_spec)

            local tree = framework_adapter.discover_positions(fpath)
            local test_position = position.get_position_by_name(tree, "0.scala")

            local result = framework_adapter.parse_tree(test_position)

            local expected = {
                type = "file",
                only = {
                    "bar.FooSpec",
                    "bar.BarSpec",
                },
                positions = {},
            }

            assert.is_same(expected, result)
        end)

        async.it("should be able to parse a Namespace and return the Test Suite", function()
            local fpath = vim.fn.tempname() .. ".scala"
            files.write(fpath, single_test_spec)

            local positions = framework_adapter.find_positions(fpath)
            local test_position = position.find_position(positions, "bar.FooSpec.Foo")

            local result = framework_adapter.parse_tree(test_position)

            local expected = {
                type = "namespace",
                only = {
                    "bar.FooSpec",
                },
                positions = {
                    {
                        id = "bar.FooSpec.Foo",
                        name = "Foo",
                    },
                },
            }

            assert.is_same(expected, result)
        end)

        async.it("should be able to parse a Test", function()
            local fpath = vim.fn.tempname() .. ".scala"
            files.write(fpath, single_test_spec)

            local positions = framework_adapter.discover_positions(fpath)
            local test_position = position.find_position(positions, "bar.FooSpec.Foo.Foo Bar")

            local result = framework_adapter.parse_tree(test_position)

            local expected = {
                type = "test",
                only = {
                    "bar.FooSpec",
                },
                positions = {
                    {
                        id = "bar.FooSpec.Foo.Foo Bar",
                        name = "Foo Bar",
                    },
                },
            }

            assert.is_same(expected, result)
        end)
    end)

    describe("find_positions", function()
        async.it("should find the Test Suite + Test in a file", function()
            local expected = {
                "bar.FooSpec.Foo", -- this is the Suite
                "bar.FooSpec.Foo.Foo Bar", -- this is the Test
                "bar.FooSpec.Foo.Bar Foo", -- this is the Test
                "bar.BarSpec.Bar", -- this is the Suite
                "bar.BarSpec.Bar.Test But Not a Suite", -- this is the Test
            }

            assert_has_positions(single_test_spec, expected)
        end)
    end)

    describe("get_test_framework_name", function()
        describe("should find the test framework defined for the given file", function()
            async.it("ZIODefaultSpec/zio-test", function()
                local fpath = vim.fn.tempname() .. ".scala"
                files.write(fpath, single_test_spec)

                local result = framework_adapter.get_test_framework_name(fpath)

                assert.is_same(types.TEST_FRAMEWORKS.ZIO_TEST, result)
            end)

            local munit_spec = [[
            package foo.bar

            import foo.Hello

            class FooSuite extends munit.FunSuite:
                test("Foo.Bar"):
                assertEquals(Hello.msg, "Hello World!")

                test("Foo Bar"):
                assertEquals(Hello.msg, "Hello World.")

                test("Bar Foo"):
                assertEquals(Hello.msg, "Hello World!")

                test("Foo Baz"):
                assertEquals(Hello.msg, "Hello World!")
            ]]

            async.it("FunSuite/munit", function()
                local fpath = vim.fn.tempname() .. ".scala"
                files.write(fpath, munit_spec)

                local result = framework_adapter.get_test_framework_name(fpath)

                assert.is_same(types.TEST_FRAMEWORKS.MUNIT, result)
            end)
        end)
    end)
end)
