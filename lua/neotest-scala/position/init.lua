local lib = require("neotest.lib")
local utils = require("neotest-scala.utils")

local M = {}

-- ---@param test_name string
-- function M.escape_test_name(test_name)
--     -- Escape special characters in test_name to safely use it in the pattern
--     local escaped_test_name = test_name:gsub("([%^%$%(%)%%%.%[%]%*%+%-%?])", "%%%1")
--
--     return escaped_test_name
-- end

--- Strip quotes from the (captured) test position.
---@param position neotest.Position
---@return string
-- function M.get_position_name(position)
--     local without_quotes = string.gsub(position.name, [[^"(.+)"$]], "%1")
--
--     return without_quotes
-- end

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
function M.build_position_id(position, parents)
    local parent_values = {}
    -- alright, I see what's happening
    -- we can call this recursively to build the fully-qualified id of each test suite
    if position.type == "object" then
        if #parents == 0 then
            local package_name = M.get_package_name(position.path)
            -- local object_name = M.get_object_name(position.path)

            local position_name = utils.get_position_name(position)

            local value = package_name .. "." .. position_name

            return value
        end

        local parent = parents[#parents]

        local position_name = utils.get_position_name(position)

        return parent.id .. "." .. position_name
    elseif position.type == "namespace" then
        -- print("It's a namespace, does it have any parents?")
        print("namespace name: ", position.name)
        -- print("parents: ", vim.inspect(parents))
        if #parents == 0 then
            -- get the treesitter node
            local range = position.range
            print("range: ", vim.inspect(range))
            local package_name = M.get_package_name(position.path)
            local object_name = M.get_object_name(position.path)

            local container_object = M.get_container_object(position.path, range)

            print("container_object: ", vim.inspect(container_object))

            local object_name = container_object.name

            local position_name = utils.get_position_name(position)

            local value = package_name .. "." .. object_name .. "." .. position_name

            return value
        end

        local parent = parents[#parents]

        local position_name = utils.get_position_name(position)

        return parent.id .. "." .. position_name
    else
        for i, parent in ipairs(parents) do
            if i == 1 then
                table.insert(parent_values, parent.id)
            else
                local parent_name = utils.get_position_name(parent)

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

function M.has_position_by_name(tree, position_name)
    for _, node in tree:iter_nodes() do
        local data = node:data()

        if data.name == position_name then
            return true
        end
    end
    return false
end

function M.get_position_by_name(tree, position_name)
    for _, node in tree:iter_nodes() do
        local data = node:data()

        if data.name == position_name then
            return node
        end
    end
    return nil
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

---@param path string
function M.get_package_name(path)
    local file_content = lib.files.read(path)
    local ts = vim.treesitter
    -- Create a parser for the content using Tree-sitter
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

function M.get_object_name(path)
    local file_content = lib.files.read(path)
    local ts = vim.treesitter
    local parser = ts.get_string_parser(file_content, "scala")
    local tree = parser:parse()[1]
    local root = tree:root()

    local query = [[
      (object_definition (identifier) @object)
    ]]

    local query_obj = ts.query.parse("scala", query)

    for _, captures, _ in query_obj:iter_matches(root, file_content) do
        for id, node in pairs(captures) do
            local name = query_obj.captures[id] -- capture name
            if name == "object" then
                local object_name = vim.treesitter.get_node_text(node, file_content)

                return object_name
            end
        end
    end

    return nil -- if no object found
end

---@param path string The path to the file on the filesystem
---@param child_range number[]|nil The range of the child node
---@return neotest.Position|nil
function M.get_containing_object(path, child_range, query)
    print("get_containing_object")
    local positions = lib.treesitter.parse_positions(path, query, {
        nested_tests = false,
        require_namespaces = false,
        build_position = M.build_position,
    })

    print("number of positions: ", #positions)

    for _, position in positions:iter() do
        if child_range then
            if position.type == "object" and M.contains_node_by_range(position, child_range) then
                return position
            end
        else
            if position.type == "object" then
                return position
            end
        end
    end

    return nil
end

-- determines whether a given node "contains" another node
-- for example, in the case of
-- object Parent:
--    function fooBar: Unit = ???
--
-- we would say that Parent "contains" #fooBar
---@param node_a neotest.Position
---@param node_b neotest.Position
---@return boolean
function M.contains_node(node_a, node_b)
    local node_b_range = node_b.range

    return M.contains_node_by_range(node_a, node_b_range)
end

-- determines whether a given node "contains" another node by its range
---@param node neotest.Position
---@param child_range number[]
---@return boolean
function M.contains_node_by_range(node, child_range)
    local node_range = node.range

    if child_range[1] >= node_range[1] and child_range[3] <= node_range[3] then
        return true
    end

    return false
end

local function get_match_type(captured_nodes)
    if captured_nodes["object.name"] then
        return "object"
    end
    if captured_nodes["test.name"] then
        return "test"
    end
    if captured_nodes["namespace.name"] then
        return "namespace"
    end
end

function M.build_position(file_path, source, captured_nodes)
    local match_type = get_match_type(captured_nodes)

    if not match_type then
        return
    end

    local name = vim.treesitter.get_node_text(captured_nodes[match_type .. ".name"], source)
    local definition = captured_nodes[match_type .. ".definition"]

    return {
        type = match_type,
        path = file_path,
        name = name,
        range = { definition:range() },
    }
end

return M
