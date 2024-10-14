local lib = require("neotest.lib")
local utils = require("neotest-scala.utils")

local M = {}

--- Strip quotes from the (captured) test position.
---@param position neotest.Position
---@return string
function M.get_position_name(position)
    if position.type == "test" then
        local value = string.gsub(position.name, '"', "")
        return value
    end
    return position.name
end
---@param position neotest.Position The position to return an ID for
---@param parents neotest.Position[] Parent positions for the position
---@return string
function M.build_position_id_munit(position, parents)
    local parent_values = {}
    if position.type == "namespace" then
        if #parents == 0 then
            local package_name = M.get_package_name(position.path)

            local value = package_name .. "." .. position.name

            return value
        end

        local parent = parents[#parents]

        return parent.id .. "$" .. utils.get_position_name(position)
    else
        for i, parent in ipairs(parents) do
            if i == 1 then
                table.insert(parent_values, parent.id)
            else
                table.insert(parent_values, parent.name)
            end
        end

        local value = table.concat(
            vim.iter({
                parent_values,
            })
                :flatten()
                :totable(),
            "$"
        )

        local updated_value = value .. "." .. utils.get_position_name(position)

        return updated_value
    end
end

---@param position neotest.Position The position to return an ID for
---@param parents neotest.Position[] Parent positions for the position
---@return string
function M.build_position_id(position, parents)
    print("Building position id for", position.name)
    print("Type: ", position.type)
    print("Parents: ", vim.inspect(parents))
    -- get the treesitter tree node for the position
    -- local tree = position.print("Tree: ", vim.inspect(tree))
    local parent_values = {}
    -- alright, I see what's happening
    -- we can call this recursively to build the fully-qualified id of each test suite
    if position.type == "namespace" then
        if #parents == 0 then
            local package_name = M.get_package_name(position.path)

            local position_name = utils.get_position_name(position)

            local value = package_name .. "." .. position_name

            return value
        end

        local parent = parents[#parents]

        local position_name = utils.get_position_name(position)

        -- if position.type == "test" then
        --     local value = string.gsub(position.name, '"', "")
        --     return value
        -- end

        -- print("Position name: ", position_name)

        return parent.id .. "." .. position_name
    else
        for i, parent in ipairs(parents) do
            if i == 1 then
                print("Parent id: ", parent.id)
                -- local parent_name = utils.get_position_name(parent)

                -- print("Fixed parent name: ", parent_name)
                table.insert(parent_values, parent.id)
            else
                print("Parent name: ", parent.name)
                local parent_name = utils.get_position_name(parent)

                print("Fixed parent name: ", parent_name)

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

        local position_name = utils.get_position_name(position)

        -- print("position_name", position_name)

        local updated_value = value .. "." .. position_name

        return updated_value
    end
end

function M.has_position(tree, pos_id)
    for _, node in tree:iter_nodes() do
        local data = node:data()

        if data.id == pos_id then
            return true
        end
    end
    return false
end

function M.find_position(tree, pos_id)
    for _, node in tree:iter_nodes() do
        local data = node:data()

        if data.id == pos_id then
            return node
        end
    end
    return nil
end

function M.get_package_name(path)
    local file_content = lib.files.read(path)
    local ts = vim.treesitter
    -- local ts_utils = require("nvim-treesitter.ts_utils")
    -- Create a parser for the content using Tree-sitter
    local parser = ts.get_string_parser(file_content, "scala")
    local tree = parser:parse()[1]
    local root = tree:root()

    -- Corrected Scala query to capture the package name

    local query = [[
      (package_clause (package_identifier) @package)
    ]]

    local query_obj = ts.query.parse("scala", query)

    -- Iterate over matches and print the package name
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

local function get_match_type(captured_nodes)
    if captured_nodes["suite.name"] then
        return "suite"
    end
    if captured_nodes["test.name"] then
        return "test"
    end
    if captured_nodes["namespace.name"] then
        return "namespace"
    end
end

function M.build_position(file_path, source, captured_nodes)
    -- print("#build_position")
    -- print("file_path: ", file_path)
    -- print("source: ", source)
    -- print("captured_nodes: ", vim.inspect(captured_nodes))
    local match_type = get_match_type(captured_nodes)
    -- print("match_type: ", match_type)
    -- local match_type = get_match_type(captured_nodes)
    if not match_type then
        return
    end
    local name = vim.treesitter.get_node_text(captured_nodes[match_type .. ".name"], source)
    -- print("name: ", name)
    local definition = captured_nodes[match_type .. ".definition"]

    local parent = definition:parent():parent()

    -- local node = vim.treesitter.get_node(definition)

    -- print("node: ", vim.inspect(node))

    -- print("parent: ", vim.inspect(parent))

    -- local parent_name = vim.treesitter.get_node_text(parent, source)

    -- print("parent_name: ", parent_name)

    ---@type string
    -- local name = vim.treesitter.get_node_text(captured_nodes[match_type .. ".name"], source)
    -- local definition = captured_nodes[match_type .. ".definition"]

    -- return {
    --   type = match_type,
    --   path = file_path,
    --   name = name,
    --   range = { definition:range() },
    --   is_parameterized = captured_nodes["each_property"] and true or false,
    -- }
    return {}
end

function M.discover_positions(path)
    -- (call_expression
    -- function: (call_expression
    --     function: (identifier)
    --     arguments: (arguments
    --     (string) @test.name))
    -- ) @test.definition
    local zio_test_query = [[
	    ;(object_definition
	    ;name: (identifier) @namespace.name)
	    ;@namespace.definition
        
        ((call_expression
            function: (call_expression
            function: (identifier) @func_name (#match? @func_name "test")
            arguments: (arguments (string) @test.name))
        )) @test.definition
        
        ((call_expression
            function: (call_expression
            function: (identifier) @func_name (#match? @func_name "suite")
            arguments: (arguments (string) @namespace.name))
        )) @namespace.definition
    ]]
    local suite_query = [[
    
    (call_expression
        function: (call_expression
            function: (identifier)
            arguments: (arguments
            (string)))
        arguments: (arguments
            (call_expression
            function: (call_expression
                function: (identifier)
                arguments: (arguments
                (string) @test.name))
            arguments: (block
                (call_expression
                function: (identifier)
                arguments: (arguments
            )))
            ) @test.definition
        ))
    ]]

    local query = [[
	    (object_definition
	    name: (identifier) @namespace.name)
	    @namespace.definition
	    
        (class_definition
        name: (identifier) @namespace.name)
        @namespace.definition

        ((call_expression
            function: (call_expression
            function: (identifier) @func_name (#match? @func_name "test")
            arguments: (arguments (string) @test.name))
        )) @test.definition
    ]]

    local positions = lib.treesitter.parse_positions(path, zio_test_query, {
        nested_tests = true,
        require_namespaces = false,
        position_id = M.build_position_id,
        -- build_position = M.build_position,
    })

    return positions
end

return M
