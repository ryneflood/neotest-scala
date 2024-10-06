local Path = require("plenary.path")

TEST_FRAMEWORKS = {
    MUNIT = "munit",
}

local M = {}

M.type = PROJECT_TYPES.MILL

function M.find_projects(path)
    local command = "cd " .. path .. " && " .. "mill resolve _.test"
    local handle = assert(io.popen(command), string.format("unable to execute: [%s]", command))
    local result = handle:read("*a")
    handle:close()

    local projects = {}
    for line in result:gmatch("[^\r\n]+") do
        -- remove the ".test" suffix from each line
        line = line:gsub(".test$", "")
        table.insert(projects, line)
    end

    return projects
end

function M.get_project_name(file_path)
    -- FIXME: we seem to given a file path which could be:
    -- ${project_root}/${module}/test/src/${package}/MainSpec.scala
    -- ${project_root}/${module}/test/src/${package}
    -- ${project_root}/${module}/test/src/
    -- ${project_root}/${module}/test/
    -- ${project_root}/${module}
    -- so, basically, we need to be able to find the project name at any depth
    -- we'll probably want to do this by finding the project's root and comparing the current path diff
    print("#get_project_name for file_path", file_path)
    -- local path = Path:new(file_path):absolute()
    local pieces = vim.split(file_path, Path.path.sep)
    local base_project_path = "([^/]+)"
    local test_path_regex = "([^/]+)/test"
    local regex = "([^/]+)/test/src"
    -- test the file_path string for a match
    local match = string.match(file_path, regex)
        or string.match(file_path, test_path_regex)
        -- or get the last part of the path
        or pieces[#pieces]

    return match .. ".test"
    -- extract the matching group
    --
    -- print("match", match)

    -- we'll have a list of project names, like { "foo", "bar" }
    -- and Mill will expect that tests are stored in a directory like:
    -- {{project-name}}/test/src/{{package}}/MainSpec.scala
    -- so we should be able to break apart the path to get the project name

    -- for _, project in ipairs(projects) do
    --     local project_path = project .. "/test/src/"
    --
    --     if string.find(path, project_path) then
    --         return project .. ".test"
    --     end
    -- end

    -- return nil
end

function M.get_test_framework(path, project_name)
    local command = "cd " .. path .. " && " .. "mill show " .. project_name .. ".test.testFramework"
    local handle = assert(io.popen(command), string.format("unable to execute: [%s]", command))
    local result = handle:read("*l")
    handle:close()

    if result == nil then
        return nil
    end

    result = string.gsub(result, '"', "") -- remove the quotes

    if result == "munit.Framework" then
        return TEST_FRAMEWORKS.MUNIT
    end

    return nil
end

return M
