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

---@param pos neotest.Position
---@return string
-- local get_parent_name = function(pos)
--     if pos.type == "dir" or pos.type == "file" then
--         return ""
--     end
--     if pos.type == "namespace" then
--         print("from get_parent_name, it's a namespace")
--         -- local parent_name = M.get_package_name(pos.path) .. "." .. pos.name
--         -- print("parent_name", parent_name)
--         return M.get_package_name(pos.path) .. "." .. pos.name
--     end
--     return utils.get_position_name(pos)
-- end
--
-- local function findCommonPrefix(str1, str2)
--     local len1, len2 = #str1, #str2
--     local i = 1
--
--     -- Compare characters until they diverge
--     while i <= len1 and i <= len2 do
--         if str1:sub(i, i) ~= str2:sub(i, i) then
--             break
--         end
--         i = i + 1
--     end
--
--     -- Return the common substring
--     return str1:sub(1, i - 1)
-- end

-- Function to find common prefix in a list of strings
-- local function findCommonPrefixInValues(strings)
--     if #strings == 0 then
--         return ""
--     end
--
--     local commonPrefix = strings[1]
--
--     for i = 2, #strings do
--         commonPrefix = findCommonPrefix(commonPrefix, strings[i])
--
--         -- Early exit if there's no common prefix
--         if commonPrefix == "" then
--             break
--         end
--     end
--
--     return commonPrefix
-- end

-- local function flatten_parent_namespaces(parents)
--     -- if there's only one test suite/no parents, then just return the test suite name
--     if #parents == 1 then
--         return parents
--     end
--
--     -- otherwise, we'll have a list of fully-qualified test suite names
--     -- like
--     -- foo.bar.FooSuite
--     -- foo.bar.BarSuite
--     local commonprefix = findCommonPrefixInValues(parents)
--
--     -- find the common substring between the list items
--     local result = {}
--
--     -- map over each entry in the table
--     for _, parent in ipairs(parents) do
--         print("parent", parent)
--         -- remove the common prefix from each entry
--         local suite_name = parent:sub(#commonprefix + 1)
--         -- add the suffix to the result table
--         table.insert(result, suite_name)
--     end
--
--     return commonprefix .. table.concat(result, "$")
-- end

---@param position neotest.Position The position to return an ID for
---@param parents neotest.Position[] Parent positions for the position
---@return string
function M.build_position_id(position, parents)
    local parent_values = {}
    -- alright, I see what's happening
    -- we can call this recursively to build the fully-qualified id of each test suite
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

function M.discover_positions(path)
    -- print("Discovering positions for", path)
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

    local positions = lib.treesitter.parse_positions(
        path,
        query,
        { nested_tests = true, require_namespaces = true, position_id = M.build_position_id }
    )

    return positions
end

return M
