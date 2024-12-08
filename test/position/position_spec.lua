require("setup_test_environment")

local files = require("neotest.lib").files
local async = require("nio").tests
local position = require("neotest-scala.position")
local lib = require("neotest.lib")

local single_test_spec = [[
    package bar

    import bar.Hello

    import zio.test._

    object HelloWorldSpec extends ZIOSpecDefault:
        def spec =
            suite("HelloWorld")(
                test("Test But Not a Suite") {
                    assertTrue(Hello.msg == "Hello World, from Bar")
                }
            )
]]

local nested_objects = [[
    package bar

    import bar.Hello

    object Parent:
      object Child:
        val foo = "bar"
]]

describe("position", function()
    describe("contains_node", function()
        async.it("returns true if the given node is contained in the given range", function()
            -- TODO: this situation is repeated over and over
            local fpath = vim.fn.tempname() .. ".scala"
            files.write(fpath, nested_objects)

            local query = [[
                (object_definition name: (identifier) @object.name) @object.definition
            ]]

            local positions = lib.treesitter.parse_positions(
                fpath,
                query,
                { nested_tests = true, require_namespaces = false, build_position = position.build_position }
            )

            local parent_node = position.get_position_by_name(positions, "Parent")
            local child_node = position.get_position_by_name(positions, "Child")

            local result = position.contains_node(parent_node:data(), child_node:data())

            assert.is_true(result)
        end)
    end)

    describe("get_containing_object", function()
        async.it("should return the outer Object, since we're not providing a range", function()
            local fpath = vim.fn.tempname() .. ".scala"
            files.write(fpath, single_test_spec)

            -- TODO: this is repeated over and over
            local query = [[
                (object_definition name: (identifier) @object.name) @object.definition
            ]]

            local result = position.get_containing_object(fpath, nil, query)

            local expected_name = "HelloWorldSpec"

            assert.is_equal(expected_name, result.name)
        end)

        async.it("should return the outer Object, since it's the parent of the Child object", function()
            local fpath = vim.fn.tempname() .. ".scala"
            files.write(fpath, nested_objects)

            -- TODO: this is repeated over and over
            local query = [[
                (object_definition name: (identifier) @object.name) @object.definition
            ]]

            local positions = lib.treesitter.parse_positions(
                fpath,
                query,
                { nested_tests = true, require_namespaces = false, build_position = position.build_position }
            )

            local child_node = position.get_position_by_name(positions, "Child")

            local result = position.get_containing_object(fpath, child_node:data().range, query)

            assert.is_equal("Parent", result.name)
        end)
    end)

    describe("get_package_name", function()
        async.it("should read the package name from a .scala file", function()
            local spec = [[
          package foo.bar

          import foo.Hello

          class MySuite extends munit.FunSuite:
            test("hello"):
              assertEquals(Hello.msg, "Hello World!")
        ]]

            local fpath = vim.fn.tempname() .. ".scala"
            files.write(fpath, spec)

            local result = position.get_package_name(fpath)

            assert.is_same("foo.bar", result)
        end)
    end)
end)
