local project = require("neotest-scala.project")
local files = require("neotest.lib").files
local path = require("plenary.path")
local types = require("neotest-scala.types")

-- Function to escape special shell characters in a string
local function shell_escape(str)
    -- Table of special characters to escape
    local special_chars = {
        ["%$"] = "\\$", -- Lua patterns need '%' for special chars
        -- ["`"] = "%`",
        -- ['"'] = '"', -- In shell context '\ " ' should be handled
        -- ["'"] = "'", -- Single quotes handled separately in lua
        -- ["\\"] = "\\\\", -- Double escape for Lua and Bash
        -- ["\n"] = "\\n", -- Newline should be represented correctly
        -- ["\r"] = "\\r",
        -- ["!"] = "\\!",
        -- Add more special characters as needed
    }

    -- Iterate and replace each special character
    for k, v in pairs(special_chars) do
        str = string.gsub(str, k, v)
    end

    return str
end

local M = {}

function M.remove_duplicates(list)
    -- Create a new table to act as a set
    local set = {}
    -- Create a new list to store the unique elements
    local uniqueList = {}

    -- Iterate through each item in the original list
    for _, item in ipairs(list) do
        -- Check if the item is not yet in the set
        if not set[item] then
            -- Mark the item as seen
            set[item] = true
            -- Add the item to the unique list
            table.insert(uniqueList, item)
        end
    end

    -- Return the list of unique items
    return uniqueList
end

function M.ends_with(input_string, word)
    local word_len = string.len(word)

    -- Compare the end of the input string to the word
    return string.sub(input_string, -word_len) == word
end

function M.get_test_runner(path)
    if project.get_project_type(path) == types.PROJECT_TYPES.SCALA_CLI then
        return types.PROJECT_TYPES.SCALA_CLI
    end

    return "bloop"
end

function M.make_test_resources_path(file_path)
    local cwd = vim.fn.getcwd()

    local path_parts = {
        cwd,
        "test",
        "resources",
        file_path,
    }

    local file_path_obj = path:new(table.concat(path_parts, files.sep))

    return file_path_obj:absolute()
end

function M.reverseList(list)
    local reversed = {}
    for i = #list, 1, -1 do
        table.insert(reversed, list[i])
    end
    return reversed
end

return M
