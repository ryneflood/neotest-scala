local types = require("neotest-scala.types")
local utils = require("neotest-scala.utils")

local M = {}

---@param runner_path string
---@param project string
---@param build_tool string
---@param parsed_position neotestscala.ParsedPosition
function M.build_command(runner_path, project, build_tool, parsed_position)
    local only = {}
    local tests = {}

    for _, position in ipairs(parsed_position.only) do
        table.insert(only, "--only")
        table.insert(only, position)
    end

    -- unfortunately, zio test doesn't support running a single test using
    -- its fully qualified name, so the best we can do is pass the test's name
    if parsed_position.test_framework == types.TEST_FRAMEWORKS.ZIO_TEST then
        if parsed_position.type == "test" then
            table.insert(tests, "--test")
            table.insert(tests, '"' .. parsed_position.position.name .. '"')
        end
    elseif parsed_position.test_framework == types.TEST_FRAMEWORKS.SCALATEST then
        -- bloop test baz.test -o foo.bar.FooSuite -- -z "Foo Suite Bar Suite"
        -- we want a test command like ^^
        local position = parsed_position.position

        local p = position
        local pp = {}

        while p do
            -- p = p.parent
            table.insert(pp, p.name)

            p = p.parent
        end

        local reversed = utils.reverseList(pp)
        -- remove the last element, which is the test name
        -- table.remove(reversed, 1)
        local c = table.concat(reversed, " ")

        if parsed_position.type == "test" or parsed_position.type == "namespace" then
            table.insert(tests, "--test")
            table.insert(tests, '"' .. c .. '"')
        end

        -- end
    else
        -- for _, position in ipairs(parsed_position.positions) do
        if parsed_position.type == "test" then
            table.insert(tests, "--test")
            -- surround the position in quotes
            table.insert(tests, '"' .. parsed_position.position.id .. '"')
        end
    end

    return table.concat(
        vim.iter({
            runner_path,
            "--runner",
            build_tool,
            "--project",
            project,
            "--framework",
            parsed_position.test_framework,
            only,
            tests,
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
