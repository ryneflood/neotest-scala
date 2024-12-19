local lib = require("neotest.lib")
local position = require("neotest-scala.position")
local types = require("neotest-scala.types")
local utils = require("neotest-scala.utils")

local M = {}

local function find_test_classes(path)
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

---@param tree neotest.Tree
local function make_position(tree)
    if tree:parent() == nil then
        return nil
    end

    if tree:data().type == "file" then
        return nil
    end

    if tree:data().type == "dir" then
        return nil
    end

    local position = {
        name = M.get_position_name(tree:data()),
        id = tree:data().id,
        parent = make_position(tree:parent(), positions),
    }

    print("position: " .. vim.inspect(position))

    return position
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
        -- a namespace represents a test suite
        -- but, since we want to support multiple test frameworks, and they allow you to define
        -- test suites in different ways, we'll need to handle each case
        --
        -- for example, in ZIO Test, a test suite is defined like this:
        --
        --  | package bar
        --  |
        --  | object FooSpec extends ZIOSpecDefault:
        --  |     def spec = suite("Foo Suite") <-- the current position is this
        --  |       ( ... )
        --
        -- or, for ScalaTest:
        --
        --  | package bar
        --  |
        --  | import org.scalatest.funspec.AnyFunSpec
        --  |
        --  | class FooSpec extends AnyFunSpec {
        --  |   describe("Foo Suite"): <-- the current position is this
        --  |     ...
        --
        --  in both of these cases, the fully-qualified path of the test suite is "bar.FooSpec.Foo Suite"
        --  which is `<package-name>.<object-name>.<test-suite-name>`
        --  since we aren't matching `object FooSpec` in our treesitter query, what we can do is test whether the matched namespace
        --  has any parent nodes; if not then it should be the case that we're dealing with a Test Suite
        --  which has been defined as the child of an Object/Class, so we can ask for the "container" object/class name
        --  and then construct the fully-qualified path from that: `<package-name>.<object-name>.<test-suite-name>`
        --
        --  but, in the case of Munit, a test suite is defined like this:
        --
        --  | package bar
        --  |
        --  | import foo.Hello
        --  |
        --  | class FooSuite extends munit.FunSuite: <-- the current position is here
        --
        -- in this case the class name is the test suite name, and there's no "parent" that was matched by the treesitter query
        if #parents == 0 then
            local parent_class = M.get_container_object(position.path, position.range)
            local package_name = M.get_package_name(position.path)

            if parent_class then
                -- we're in a ZIO Test-style test suite, where the test suite is defined as a child of an object
                return package_name .. "." .. parent_class.name .. "." .. position_name
            else
                -- we're in a Munit-style test suite, where the test suite is defined as a class
                return package_name .. "." .. position_name
            end
        else
            -- we'll also want to support nested test suites
            -- so, we'll construct the fully-qualified path by taking the closest parent
            -- and appending the current position name to it
            -- (the closest parent's ID will have been recursively constructed already)
            return parents[#parents].id .. " " .. position_name
        end
    elseif type == "test" then
        -- in the case that we're given a test, we'll be able to construct the fully-qualified path
        -- by simply taking its nearest parent and appending the test name to it;
        -- the nearest parent's ID (because it's a namespace/test suite) will have been recursively constructed already
        return parents[#parents].id .. " " .. position_name
    else
        -- throw an error, we don't know how to handle this type
        error("Unknown type: " .. type)
    end
end

function M.discover_positions(path)
    return lib.treesitter.parse_positions(path, query, {
        nested_tests = true,
        require_namespaces = true,
        position_id = M.build_position_id,
    })
end

---@param tree neotest.Tree
---@return neotestscala.ParsedPosition
function M.parse_tree(tree)
    local type = tree:data().type

    if type == "file" then
        -- in this case we want to run all of the tests in the file
        -- so, we'll want to provide the test runner with the name(s) of the matching classes/objects
        -- in the file
        --
        -- for example, in ZIO Test, a test suite is defined like this:
        --
        -- package bar
        --
        -- object FooSpec extends ZIOSpecDefault:
        --   ...
        --
        -- object BarSpec extends ZIOSpecDefault:
        --   ...
        --
        -- we'll want to return `<package-name>.<class-name>` for all matching objects
        -- in this case we'll want to return [ "bar.FooSpec", "bar.BarSpec"]
        --
        local package_name = position.get_package_name(tree:data().path)
        local test_class_names = find_test_classes(tree:data().path)

        local test_classes = {}

        for _, test_class_name in ipairs(test_class_names) do
            table.insert(test_classes, package_name .. "." .. test_class_name)
        end

        local test_framework = M.get_test_framework_name(tree:data().path)

        --@type neotestscala.ParsedPosition
        return {
            type = type,
            position = nil,
            only = test_classes,
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
        local position = make_position(tree)

        if containing_object then
            --@type neotestscala.ParsedPosition
            return {
                type = type,
                only = {
                    package_name .. "." .. containing_object.name,
                },
                position = position,
                test_framework = test_framework,
            }
        else
            local test_suites = find_test_classes(tree:data().path)
            local test_suite = test_suites[1]

            return {
                type = type,
                only = {
                    package_name .. "." .. test_suite,
                },
                position = position,
                test_framework = test_framework,
            }
        end
    end

    if type == "namespace" then
        print("it's a namespace")
        -- in ZIO Test a namespace is a Test Suite
        -- and we'll tell the Test Runner to run the entire suite
        -- so, we'll just want to return the ID of the Namespace itself

        local package_name = position.get_package_name(tree:data().path)
        -- we need to find the "containing" object of the test suite
        -- basically, the ZIODefaultSpec in which the Test Suite lives
        local containing_object = M.get_container_object(tree:data().path, tree:data().range)
        -- TODO: we should probably check if the containing object is nil here
        local test_framework = M.get_test_framework_name(tree:data().path)

        local position = make_position(tree)

        if containing_object then
            local containing_object_name = containing_object.name

            --@type neotestscala.ParsedPosition
            return {
                type = type,
                only = {
                    package_name .. "." .. containing_object_name,
                },
                position = position,
                test_framework = test_framework,
            }
        else
            --@type neotestscala.ParsedPosition
            return {
                type = type,
                only = {
                    package_name .. "." .. M.get_position_name(tree:data()),
                },
                position = position,
                test_framework = test_framework,
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
                local object_names = find_test_classes(child:data().path)

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
            position = nil,
            only = without_duplicates,
            test_framework = test_framework,
        }
    end

    error("Unknown type: " .. type)
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
