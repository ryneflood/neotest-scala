local Path = require("plenary.path")
local Project = require("neotest-scala.project")
local lib = require("neotest.lib")

TEST_FRAMEWORKS = {
    MUNIT = "munit",
    ZIO_TEST = "zio-test",
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
end

local function get_project_root(path)
    print("local #get_project_root for path", path)
    local result = lib.files.match_root_pattern("build.sc")(path:absolute())

    print("result here is ", result)

    if result then
        return result
    else
        if path:is_root() then
            return nil
        end
        return get_project_root(path:parent())
    end
end

function M.get_project_root(file_path)
    local thing = Path:new(file_path)
    print("@thing", thing)
    print("#get_project_root for file_path", file_path)
    local parent = Path.parent(thing)
    print("@parent", parent)

    return get_project_root(parent)
    -- return nil
end

function M.get_test_framework(path, project_name)
    print("#get_test_framework for path and project", path, project_name)
    local command = "cd " .. path .. " && " .. "mill show " .. project_name .. ".testFramework"
    print("command", command)
    local handle = assert(io.popen(command), string.format("unable to execute: [%s]", command))
    local result = handle:read("*l")
    handle:close()

    print("result of asking mill for the test framework", result)

    if result == nil then
        return nil
    end

    result = string.gsub(result, '"', "") -- remove the quotes

    if result == "munit.Framework" then
        return TEST_FRAMEWORKS.MUNIT
    elseif result == "zio.test.sbt.ZTestFramework" then
        return TEST_FRAMEWORKS.ZIO_TEST
    else
        return nil
    end
end

return M
