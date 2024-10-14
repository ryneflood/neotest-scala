local lib = require("neotest.lib")
local project = require("neotest-scala.project")

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

--- Strip quotes from the (captured) test position.
---@param position neotest.Position
---@return string
function M.get_position_name(position)
    print("get_position_name", vim.inspect(position.name))
    -- if position.type == "test" then
    -- remove quotes from the test name
    -- if the string starts with `'"` and ends with `"'`, remove them using a regex
    local without_quotes = string.gsub(position.name, [[^'"(.+)"'$]], "%1")

    local without_quotes_again = string.gsub(without_quotes, '"', "")

    print("without_quotes", without_quotes_again)

    return without_quotes_again
    -- string.gsub(position.name, '"', "")_
    -- return value
    -- end
    -- return position.name
end

function M.get_test_runner(path)
    if project.get_project_type(path) == PROJECT_TYPES.SCALA_CLI then
        return PROJECT_TYPES.SCALA_CLI
    end

    return "bloop"
end

-- FIXME: we should probably have one of these for each runner/framework/build tool? Is that necessary?
-- either way, putting it here for the moment
function M.prepare_command(scala_test_runner, runner, project, test_framework, type, test_suites, test_name)
    local test_suites_command = {}

    print("test_suites", vim.inspect(test_suites))

    for _, suite in ipairs(test_suites) do
        table.insert(test_suites_command, "-o")
        print("before shell_escape", suite)
        table.insert(test_suites_command, shell_escape(suite))
    end

    local individual_test_command = {}

    -- escape any characters in string that are special to the shell
    -- test_name = vim.fn.shellescape(test_name)

    if type == "test" then
        test_name = shell_escape(test_name)
        print("before shell_escape", test_suites[1])
        local test_suite_name = shell_escape(test_suites[1])
        print("test_suite_name", test_suite_name)
        table.insert(individual_test_command, "--single")
        table.insert(individual_test_command, '"' .. test_suite_name .. "." .. test_name .. '"')
    end

    -- local temp_name = vim.fn.tempname()
    local temp_name = "/tmp"

    print("test_framework", test_framework)

    local command = vim.iter({
        scala_test_runner,
        "--runner",
        runner,
        "--project",
        project,
        "--framework",
        test_framework,
        test_suites_command,
        individual_test_command,
        "--to",
        temp_name,
    })
        :flatten()
        :totable()

    return command
end

return M
