local types = require("neotest-scala.types")

local M = {}

---@param project string
---@param test_framework string
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
        for _, position in ipairs(parsed_position.positions) do
            table.insert(tests, "--test")
            -- surround the position in quotes
            table.insert(tests, '"' .. position.name .. '"')
        end
    elseif parsed_position.test_framework == types.TEST_FRAMEWORKS.SCALATEST then
        -- bloop test baz.test -o foo.bar.FooSuite -- -z "Foo Suite Bar Suite"
        -- we want a test command like ^^
        --
        -- for _, position in ipairs(parsed_position.positions) do
        --     table.insert(tests, "--test")
        --     -- surround the position in quotes
        --     table.insert(tests, '"' .. position.name .. '"')
        -- end
    else
        for _, position in ipairs(parsed_position.positions) do
            table.insert(tests, "--test")
            -- surround the position in quotes
            table.insert(tests, '"' .. position.id .. '"')
        end
    end

    return table.concat(
        vim.iter({
            runner_path,
            "--runner",
            -- "bloop",
            build_tool,
            "--project",
            project,
            "--framework",
            parsed_position.test_framework,
            only,
            tests,
            "--to",
            "/tmp",
        })
            :flatten()
            :totable(),
        " "
    )
end

return M
