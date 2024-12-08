local lib = require("neotest.lib")
local types = require("neotest-scala.types")

local M = {}

local is_sbt = lib.files.match_root_pattern("build.sbt")
local is_mill = lib.files.match_root_pattern("build.sc")
local is_scala_cli = lib.files.match_root_pattern("project.scala")

function M.get_project_type(path)
    if is_sbt(path) then
        return types.PROJECT_TYPES.SBT
    end
    if is_mill(path) then
        return types.PROJECT_TYPES.MILL
    end
    if is_scala_cli(path) then
        return types.PROJECT_TYPES.SCALA_CLI
    end

    return nil
end

function M.get_project_build_tool(path)
    local project_type = M.get_project_type(path)

    if project_type == types.PROJECT_TYPES.SBT then
        return require("neotest-scala.build_tools.build_tool_sbt")
    end
    if project_type == types.PROJECT_TYPES.MILL then
        return require("neotest-scala.build_tools.build_tool_mill")
    end
    if project_type == types.PROJECT_TYPES.SCALA_CLI then
        return require("neotest-scala.build_tools.build_tool_scala_cli")
    end
end

return M
