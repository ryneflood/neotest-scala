local zio_test_parser = require("neotest-scala.test_parsers.ziotest_test_parser")
local munit_parser = require("neotest-scala.test_parsers.munit_test_parser")
local lib = require("neotest.lib")
local position = require("neotest-scala.position")
local types = require("neotest-scala.types")
local utils = require("neotest-scala.utils")
local func_util = require("neotest.lib.func_util")

local M = {}

local query = [[;;query
    ((class_definition
        name: (identifier) @namespace.name
        extend: (extends_clause
        type: (stable_type_identifier) @type_id (#eq? @type_id "munit.FunSuite")
        )) @namespace.definition)

    ((class_definition
        name: (identifier) @namespace.name 
        extend: (extends_clause
        type: (type_identifier) @class_name (#eq? @class_name "AnyFunSuite"))
    ) @namespace.definition)
    
    ((call_expression
        function: (call_expression
        function: (identifier) @func_name (#eq? @func_name "test")
        arguments: (arguments (string) @test.name))
    )) @test.definition

    ((call_expression
        function: (call_expression
        function: (identifier) @func_name (#eq? @func_name "it")
        arguments: (arguments (string) @test.name))
    )) @test.definition
    
    ((call_expression
        function: (call_expression
        function: (identifier) @func_name (#match? @func_name "suite")
        arguments: (arguments (string) @namespace.name))
    )) @namespace.definition

    ((call_expression
        function: (call_expression
        function: (identifier) @func_name (#match? @func_name "describe")
        arguments: (arguments (string) @namespace.name))
    )) @namespace.definition
]]

function M.get_test_parser(test_framework)
    if test_framework == types.TEST_FRAMEWORKS.ZIO_TEST then
        return zio_test_parser
    elseif test_framework == types.TEST_FRAMEWORKS.MUNIT then
        return munit_parser
    else
        error("unsupported test framework: " .. test_framework)
    end
end

function M.get_package_name(path)
    local file_content = lib.files.read(path)
    local ts = vim.treesitter
    local parser = ts.get_string_parser(file_content, "scala")
    local tree = parser:parse()[1]
    local root = tree:root()

    local query = [[
      (package_clause (package_identifier) @package)
    ]]

    local query_obj = ts.query.parse("scala", query)

    for _, captures, _ in query_obj:iter_matches(root, file_content) do
        for id, node in pairs(captures) do
            local name = query_obj.captures[id] -- capture name
            if name == "package" then
                local package_name = vim.treesitter.get_node_text(node, file_content)

                return package_name
            end
        end
    end

    return nil -- If no package found
end

function M.get_position_name(position)
    -- when the position is parsed, its name is wrapped in quotes for some reason
    -- so, we'll strip them out so we can use the name as an id
    local position_name, _ = string.gsub(position.name, [[^"(.+)"$]], "%1")

    return position_name
end

function M.get_container_object(path, child_range)
    local query = [[;;query
        (object_definition
            name: (identifier) @object.name
            extend: (extends_clause
            type: (type_identifier))) @object.definition
            
        (object_definition
            name: (identifier) @object.name
            extend: (extends_clause
            type: (stable_type_identifier))) @object.definition
            
        ;;((class_definition
        ;;    name: (identifier) @object.name
        ;;    extend: (extends_clause
        ;;    type: (stable_type_identifier
        ;;        (identifier)
        ;;        (type_identifier)))
        ;;) @object.definition)
        
        (class_definition
            name: (identifier) @object.name
            extend: (extends_clause
            type: (type_identifier) @type_id (#eq? @type_id "AnyFunSpec"))
        ) @object.definition
    ]]

    return position.get_containing_object(path, child_range, query)
end

---@param position neotest.Position
---@param parents neotest.Position[]
---@return string
function M.build_position_id(position, parents)
    local type = position.type

    local position_name = M.get_position_name(position)

    if type == "namespace" then
        -- print("it's a namespace")
        -- if we're given a namespace, and it has no parents
        -- then we want to prefix the id with the package name
        if #parents == 0 then
            local containing_object = M.get_container_object(position.path, position.range)
            local package_name = M.get_package_name(position.path)

            if containing_object then
                return package_name .. "." .. containing_object.name .. "." .. position_name
            else
                return package_name .. "." .. position_name
            end
        else
            local closest_parent = parents[#parents]

            return closest_parent.id .. "." .. position_name
        end
    elseif type == "test" then
        -- FIXME: clean this up, right?
        local parent_values = {}

        for i, parent in ipairs(parents) do
            if i == 1 then
                table.insert(parent_values, parent.id)
            else
                local parent_name = M.get_position_name(parent)

                table.insert(parent_values, parent_name)
            end
        end

        local value = table.concat(
            vim.iter({
                parent_values,
            })
                :flatten()
                :totable(),
            "."
        )

        -- FIXME: this is a poor name for this variable
        local updated_value = value .. "." .. position_name

        return updated_value
    else
        -- FIXME: what do we want to do here?
        -- basically, we have an unknown Node Type here
        return ""
    end
end

function M.discover_positions(path)
    local positions = lib.treesitter.parse_positions(path, query, {
        nested_tests = true,
        require_namespaces = true,
        position_id = M.build_position_id,
    })

    return positions
end

---@param tree neotest.Tree
---@return neotestscala.ParsedPosition
function M.parse_tree(tree)
    local type = tree:data().type

    if type == "file" then
        -- in this case we want to run all of the tests in the file
        -- so, we'll want to provide the test runner with the name(s) of the main
        -- object(s) in the file
        local package_name = position.get_package_name(tree:data().path)
        local object_names = M.find_runnable_specs(tree:data().path)

        local positions = {}

        for _, object_name in ipairs(object_names) do
            table.insert(positions, package_name .. "." .. object_name)
        end

        local test_framework = M.get_test_framework_name(tree:data().path)

        --@type neotestscala.ParsedPosition
        return {
            type = type,
            positions = {},
            only = positions,
            test_framework = test_framework,
        }
    end

    if type == "test" then
        -- in this case we want to run a single test
        -- which is pretty straightforward: we'll include only
        -- the one ZIODefaultSpec and the ID of the Test itself

        local package_name = position.get_package_name(tree:data().path)
        -- we need to find the "containing" object of the test suite
        -- basically, the ZIODefaultSpec in which the Test Suite lives
        local containing_object = M.get_container_object(tree:data().path, tree:data().range)
        local test_framework = M.get_test_framework_name(tree:data().path)

        local parent = tree:parent()

        local parent_names = {}
        -- recursively get the names of the parents
        while parent do
            -- print("Parent is... " .. vim.inspect(parent:data().type))
            if parent:data().type == "namespace" then
                table.insert(parent_names, M.get_position_name(parent:data()))
            end
            parent = parent:parent()
        end

        print("Parent names are... " .. vim.inspect(parent_names))

        --@type neotestscala.ParsedTest
        local position = {
            name = M.get_position_name(tree:data()),
            id = tree:data().id,
        }

        if containing_object then
            --@type neotestscala.ParsedPosition
            return {
                type = type,
                only = {
                    package_name .. "." .. containing_object.name,
                },
                positions = {
                    position,
                },
                test_framework = test_framework,
                chain = parent_names,
            }
        else
            local test_suites = M.find_runnable_specs(tree:data().path)
            local test_suite = test_suites[1]

            return {
                type = type,
                only = {
                    package_name .. "." .. test_suite,
                },
                positions = {
                    {
                        id = tree:data().id,
                        name = M.get_position_name(tree:data()),
                    },
                },
                test_framework = test_framework,
                chain = parent_names,
            }
        end
    end

    if type == "namespace" then
        print("it's a namespace")
        -- in ZIO Test a namespace is a Test Suite
        -- and we'll tell the Test Runner to run the entire suite
        -- so, we'll just want to return the ID of the Namespace itself
        -- print("position is... " .. vim.inspect(tree:data()))

        local package_name = position.get_package_name(tree:data().path)
        -- we need to find the "containing" object of the test suite
        -- basically, the ZIODefaultSpec in which the Test Suite lives
        local containing_object = M.get_container_object(tree:data().path, tree:data().range)
        -- TODO: we should probably check if the containing object is nil here
        local test_framework = M.get_test_framework_name(tree:data().path)

        -- print("containing_object is... " .. vim.inspect(containing_object))
        local parent = tree:parent()

        print("Hmmmm")

        local parent_names = {}
        table.insert(parent_names, M.get_position_name(tree:data()))
        -- recursively get the names of the parents
        while parent do
            print("Parent is... " .. vim.inspect(parent:data().type))
            if parent:data().type == "namespace" then
                table.insert(parent_names, M.get_position_name(parent:data()))
            end
            parent = parent:parent()
        end

        print("Parent names are... " .. vim.inspect(parent_names))

        if containing_object then
            local containing_object_name = containing_object.name

            local position = {
                name = M.get_position_name(tree:data()),
                id = tree:data().id,
            }
            --@type neotestscala.ParsedPosition
            return {
                type = type,
                only = {
                    package_name .. "." .. containing_object_name,
                },
                positions = {
                    position,
                },
                test_framework = test_framework,
                chain = parent_names,
            }
        else
            --@type neotestscala.ParsedPosition
            return {
                type = type,
                only = {
                    package_name .. "." .. M.get_position_name(tree:data()),
                },
                positions = {},
                test_framework = test_framework,
                chain = parent_names,
            }
        end
    end

    if type == "dir" then
        local only = {}

        local test_framework = nil

        for _, child in tree:iter_nodes() do
            -- TODO: oh god there's a better way to do this
            test_framework = M.get_test_framework_name(tree:data().path)

            if child:data().type == "namespace" then
                -- in this case we want to run all of the tests in the file
                -- so, we'll want to provide the test runner with the name(s) of the main
                -- object(s) in the file
                local package_name = position.get_package_name(child:data().path)
                local object_names = M.find_runnable_specs(child:data().path)

                for _, object_name in ipairs(object_names) do
                    local fully_qualified_name = package_name .. "." .. object_name

                    table.insert(only, fully_qualified_name)
                end
            end
        end

        local without_duplicates = utils.remove_duplicates(only)

        --@type neotestscala.ParsedPosition
        return {
            type = type,
            positions = {},
            only = without_duplicates,
            test_framework = test_framework,
        }
    end

    error("Unknown type: " .. type)
end

function M.find_runnable_specs(path)
    -- FIXME: we can be more specific with this query; we only want to match
    -- object that extend ZIODefaultSpec, or whatever that's called
    local query = [[
        (object_definition
            name: (identifier) @object.name
            extend: (extends_clause
            type: (type_identifier))) @object.definition
            
        (object_definition
            name: (identifier) @object.name
            extend: (extends_clause
            type: (stable_type_identifier))) @object.definition
            
        (class_definition
            name: (identifier) @object.name
            extend: (extends_clause
            type: (stable_type_identifier))) @object.definition
            
        (class_definition
            name: (identifier) @object.name
            extend: (extends_clause
            type: (type_identifier))) @object.definition
    ]]

    local object_names = {}

    local positions = lib.treesitter.parse_positions(path, query, {
        nested_tests = false,
        require_namespaces = false,
        build_position = position.build_position,
    })

    for _, position in positions:iter() do
        -- TODO: is this test necessary? We're already querying
        -- for object definitions, so we should only get objects?
        if position.type == "object" then
            table.insert(object_names, position.name)
        end
    end

    return object_names
end

function M.get_test_framework_name(fpath)
    local file_content = lib.files.read(fpath)
    local ts = vim.treesitter
    local parser = ts.get_string_parser(file_content, "scala")
    local tree = parser:parse()[1]
    local root = tree:root()

    local query = [[;;query
        (object_definition
            name: (identifier)
            extend: (extends_clause
            type: (type_identifier) @type.id))
            
        (object_definition
            name: (identifier)
            extend: (extends_clause
            type: (stable_type_identifier) @type.id))

        (class_definition
            name: (identifier)
            extend: (extends_clause
            type: (stable_type_identifier
                (identifier)
                (type_identifier) @type.id))
        )
    
        (class_definition
            name: (identifier)
            extend: (extends_clause
            type: (type_identifier) @type.id)
        )
    ]]

    local query_obj = ts.query.parse("scala", query)

    for _, captures, _ in query_obj:iter_matches(root, file_content) do
        for id, node in pairs(captures) do
            local name = query_obj.captures[id] -- capture name
            if name == "type.id" then
                local object_name = vim.treesitter.get_node_text(node, file_content)

                print("object_name: " .. object_name)

                -- test if object_name contains the text "ZIODefaultSpec"
                if string.find(object_name, "ZIOSpecDefault") then
                    return types.TEST_FRAMEWORKS.ZIO_TEST
                end
                if string.find(object_name, "AnyFunSuite") then
                    return types.TEST_FRAMEWORKS.SCALATEST
                end
                if string.find(object_name, "AnyFunSpec") then
                    return types.TEST_FRAMEWORKS.SCALATEST
                end
                if string.find(object_name, "FunSuite") then
                    return types.TEST_FRAMEWORKS.MUNIT
                end

                -- return object_name
            end
        end
    end

    return nil
end

return M
