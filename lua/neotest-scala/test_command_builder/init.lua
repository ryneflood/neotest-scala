local types = require("neotest-scala.types")
local utils = require("neotest-scala.utils")

local M = {}

---@param parsed_position neotestscala.ParsedPosition
---@param command_pieces string[]
---@return string[]
local function add_test_arguments(parsed_position, command_pieces)
    -- first things first, if there's no specific test specified, then we can return early
    if parsed_position.type ~= "test" and parsed_position.type ~= "namespace" then
        return command_pieces
    end

    -- each test framework handles specifying specific Test Suites or Test Cases
    -- slightly differently, so we'll calculate the command depending on the Test Framework
    --
    if parsed_position.test_framework == types.TEST_FRAMEWORKS.ZIO_TEST and parsed_position.type == "test" then
        table.insert(command_pieces, "--test")
        table.insert(command_pieces, '"' .. parsed_position.position.name .. '"')
    end

    if parsed_position.test_framework == types.TEST_FRAMEWORKS.SCALATEST then
        -- for Scalatest, the way to specify a specific test is by taking the name of the test and the parent suite(s), concatenating them, separated by spaces
        -- like: `<suite> <suite> <test>`: `Foo Suite Bar Suite Baz Test`
        -- and we don't include the class name or package name
        local position = parsed_position.position

        local current_position = position
        local test_ancestry_names = {}

        while current_position do
            table.insert(test_ancestry_names, current_position.name)

            current_position = current_position.parent
        end

        -- since the list of names was built going from the test to the parent(s), we'll need to reverse the order
        -- and then concat the names with a space to get `<test suite> <test suite> <test name>`
        local test_selector = table.concat(utils.reverse_list(test_ancestry_names), " ")

        if parsed_position.type == "test" or parsed_position.type == "namespace" then
            table.insert(command_pieces, "--test")
            table.insert(command_pieces, '"' .. test_selector .. '"')
        end
    end

    if parsed_position.test_framework == types.TEST_FRAMEWORKS.MUNIT and parsed_position.type == "test" then
        table.insert(command_pieces, "--test")

        local test_id = parsed_position.position.parent.id .. "." .. parsed_position.position.name

        table.insert(command_pieces, '"' .. test_id .. '"')
    end

    return command_pieces
end

---@param parsed_position neotestscala.ParsedPosition
---@param command_pieces string[]
---@return string[]
local function add_test_class_arguments(parsed_position, command_pieces)
    for _, position in ipairs(parsed_position.only) do
        table.insert(command_pieces, "--only")
        table.insert(command_pieces, position)
    end

    return command_pieces
end

---@param runner_path string
---@param project string
---@param build_tool string
---@param parsed_position neotestscala.ParsedPosition
function M.build_command(runner_path, project, build_tool, parsed_position)
    local command_pieces = {}

    add_test_class_arguments(parsed_position, command_pieces)
    add_test_arguments(parsed_position, command_pieces)

    return table.concat(
        vim.iter({
            runner_path,
            "--runner",
            build_tool,
            "--project",
            project,
            "--framework",
            parsed_position.test_framework,
            command_pieces,
            "--to",
            "/tmp",
            "--kind",
            parsed_position.type,
        })
            :flatten()
            :totable(),
        " "
    )
end

return M
