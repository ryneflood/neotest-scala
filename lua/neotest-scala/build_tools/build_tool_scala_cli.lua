local project = require("neotest-scala.project")
local types = require("neotest-scala.types")
local lib = require("neotest.lib")
local path = require("plenary.path")

local M = {}

---@class neotest-scala.Framework
---@field build_command fun(runner: string, project: string, tree: neotest.Tree, name: string, extra_args: table|string): string[]
---@field get_test_results fun(output_lines: string[]): table<string, string>
---@field match_func nil|fun(test_results: table<string, string>, position_id :string):string|nil

M.type = types.PROJECT_TYPES.SCALA_CLI

function M.find_projects(fpath)
    return { "." }
end

function M.get_project_name(fpath)
    return "."
end

local function get_project_root(file_path)
    local result = lib.files.match_root_pattern("project.scala")(file_path:absolute())

    if result then
        return result
    else
        if file_path:is_root() then
            return nil
        end
        return get_project_root(file_path:parent())
    end
end

function M.get_project_root(file_path)
    local parent = path.parent(path:new(file_path))

    return get_project_root(parent)
end

return M
