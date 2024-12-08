require("setup_test_environment")

local files = require("neotest.lib").files
local async = require("nio").tests
local position = require("neotest-scala.position")
local types = require("neotest-scala.types")
local test_parser = require("neotest-scala.test_parsers")

local function assert_has_position(tree, pos_id)
    local position_ids = {}

    for _, node in tree:iter_nodes() do
        local data = node:data()

        table.insert(position_ids, data.id)
    end

    assert(
        position.has_position(tree, pos_id),
        "Position " .. pos_id .. " not found in positions: " .. vim.inspect(position_ids)
    )
end

local function assert_has_positions(spec, pos_ids)
    local fpath = vim.fn.tempname() .. ".scala"
    files.write(fpath, spec)

    local tree = test_parser.discover_positions(fpath)

    for _, pos_id in ipairs(pos_ids) do
        assert_has_position(tree, pos_id)
    end
end

local zio_test_spec = [[
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

local scalatest_fun_spec = [[
    package foo.bar

    import org.scalatest.funspec.AnyFunSpec

    class FooSuite extends AnyFunSpec {
        describe("Foo Suite") {
            describe("Bar Suite") {
                it("Baz") {
                    assert(true == true)
                }
            }
            it("Foo") {
                assert(true == true)
            }
            it("Bar") {
                assert(true == true)
            }
        }
    }
]]

local scalatest_fun_suite = [[
    package foo.bar

    import org.scalatest.funsuite.AnyFunSuite

    class FooSuite extends AnyFunSuite {
        test("Baz") {
            assert("true" == "true")
        }
        test("Foo") {
            assert(true == true)
        }
        test("Bar") {
            assert(true == true)
        }
    }
]]

describe("Test Parser", function()
    describe("find_positions", function()
        describe("zio-test", function()
            async.it("should find the Test Suite + Test in a file (zio-test)", function()
                local expected = {
                    "bar.FooSpec.Foo", -- this is the Suite
                    "bar.FooSpec.Foo.Foo Bar", -- this is the Test
                    "bar.FooSpec.Foo.Bar Foo", -- this is the Test
                    "bar.BarSpec.Bar", -- this is the Suite
                    "bar.BarSpec.Bar.Test But Not a Suite", -- this is the Test
                }

                assert_has_positions(zio_test_spec, expected)
            end)
        end)

        describe("munit", function()
            async.it("should find the Test Suite + Test in a file (munit)", function()
                local expected = {
                    "foo.bar.FooSuite", -- this is the Suite
                    "foo.bar.FooSuite.Foo.Bar", -- this is a test
                    "foo.bar.FooSuite.Foo Bar", -- this is a test
                    "foo.bar.FooSuite.Bar Foo", -- this is a test
                    "foo.bar.FooSuite.Foo Baz", -- this is a test
                }

                assert_has_positions(munit_spec, expected)
            end)
        end)

        describe("scala-test", function()
            async.it("should find the Test Suite + Test in a file (AnyFunSpec)", function()
                local expected = {
                    "foo.bar.FooSuite.Foo Suite",
                    "foo.bar.FooSuite.Foo Suite.Bar Suite",
                    "foo.bar.FooSuite.Foo Suite.Bar Suite.Baz",
                    "foo.bar.FooSuite.Foo Suite.Foo",
                    "foo.bar.FooSuite.Foo Suite.Bar",
                }

                assert_has_positions(scalatest_fun_spec, expected)
            end)

            async.it("should find the Test Suite + Test in a file (AnyFunSuite)", function()
                local expected = {
                    "foo.bar.FooSuite.Baz",
                    "foo.bar.FooSuite.Foo",
                    "foo.bar.FooSuite.Bar",
                }

                assert_has_positions(scalatest_fun_suite, expected)
            end)
        end)
    end)

    describe("parse_tree", function()
        describe("munit", function()
            async.it("should be able to parse a File and find the FunSuites", function()
                local fpath = vim.fn.tempname() .. ".scala"
                files.write(fpath, munit_spec)

                local tree = test_parser.discover_positions(fpath)
                local test_position = position.get_position_by_name(tree, "0.scala")

                local result = test_parser.parse_tree(test_position)

                local expected = {
                    type = "file",
                    only = {
                        "foo.bar.FooSuite",
                    },
                    positions = {},
                    test_framework = types.TEST_FRAMEWORKS.MUNIT,
                }

                assert.is_same(expected, result)
            end)

            async.it("should be able to parse a Namespace and return the Test Suite", function()
                local fpath = vim.fn.tempname() .. ".scala"
                files.write(fpath, munit_spec)

                local positions = test_parser.discover_positions(fpath)
                local test_position = position.find_position(positions, "foo.bar.FooSuite")

                local result = test_parser.parse_tree(test_position)

                local expected = {
                    type = "namespace",
                    only = {
                        "foo.bar.FooSuite",
                    },
                    positions = {},
                    test_framework = types.TEST_FRAMEWORKS.MUNIT,
                }

                assert.is_same(expected, result)
            end)

            async.it("should be able to parse a Test", function()
                local fpath = vim.fn.tempname() .. ".scala"
                files.write(fpath, munit_spec)

                local positions = test_parser.discover_positions(fpath)
                local test_position = position.find_position(positions, "foo.bar.FooSuite.Bar Foo")

                local result = test_parser.parse_tree(test_position)

                local expected = {
                    type = "test",
                    only = {
                        "foo.bar.FooSuite",
                    },
                    positions = {
                        {
                            id = "foo.bar.FooSuite.Bar Foo",
                            name = "Bar Foo",
                        },
                    },
                    test_framework = types.TEST_FRAMEWORKS.MUNIT,
                }

                assert.is_same(expected, result)
            end)
        end)

        describe("zio-test", function()
            async.it("should be able to parse a File and find the ZIOSpecDefaults", function()
                local fpath = vim.fn.tempname() .. ".scala"
                files.write(fpath, zio_test_spec)

                local tree = test_parser.discover_positions(fpath)
                local test_position = position.get_position_by_name(tree, "0.scala")

                local result = test_parser.parse_tree(test_position)

                local expected = {
                    type = "file",
                    only = {
                        "bar.FooSpec",
                        "bar.BarSpec",
                    },
                    positions = {},
                    test_framework = types.TEST_FRAMEWORKS.ZIO_TEST,
                }

                assert.is_same(expected, result)
            end)

            async.it("should be able to parse a Namespace and return the Test Suite", function()
                local fpath = vim.fn.tempname() .. ".scala"
                files.write(fpath, zio_test_spec)

                local positions = test_parser.discover_positions(fpath)
                local test_position = position.find_position(positions, "bar.FooSpec.Foo")

                local result = test_parser.parse_tree(test_position)

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
                    test_framework = types.TEST_FRAMEWORKS.ZIO_TEST,
                }

                assert.is_same(expected, result)
            end)

            async.it("should be able to parse a Test", function()
                local fpath = vim.fn.tempname() .. ".scala"
                files.write(fpath, zio_test_spec)

                local positions = test_parser.discover_positions(fpath)
                local test_position = position.find_position(positions, "bar.FooSpec.Foo.Foo Bar")

                local result = test_parser.parse_tree(test_position)

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
                    test_framework = types.TEST_FRAMEWORKS.ZIO_TEST,
                }

                assert.is_same(expected, result)
            end)
        end)
    end)
end)
