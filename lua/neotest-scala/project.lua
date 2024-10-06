local lib = require("neotest.lib")
local Path = require("plenary.path")

local M = {}

SBT_PROJECT = "sbt"
MILL_PROJECT = "mill"
GRADLE_PROJECT = "gradle"
SCALA_CLI_PROJECT = "scala-cli"

PROJECT_TYPES = { SBT = SBT_PROJECT, MILL = MILL_PROJECT, GRADLE = GRADLE_PROJECT, SCALA_CLI = SCALA_CLI_PROJECT }

local is_sbt = lib.files.match_root_pattern("build.sbt")
local is_gradle = lib.files.match_root_pattern("build.gradle")
local is_mill = lib.files.match_root_pattern("build.sc")
local is_scala_cli = lib.files.match_root_pattern("project.scala")

function M.get_project_type(path)
    if is_sbt(path) then
        return PROJECT_TYPES.SBT
    end
    if is_gradle(path) then
        return PROJECT_TYPES.GRADLE
    end
    if is_mill(path) then
        return PROJECT_TYPES.MILL
    end
    if is_scala_cli(path) then
        return PROJECT_TYPES.SCALA_CLI
    end
    return nil
end

function M.get_project_framework(path)
    local project_type = M.get_project_type(path)

    if project_type == PROJECT_TYPES.SBT then
        return require("neotest-scala.project_framework_sbt")
    end
    if project_type == PROJECT_TYPES.MILL then
        return require("neotest-scala.project_framework_mill")
    end
    if project_type == PROJECT_TYPES.SCALA_CLI then
        return require("neotest-scala.project_framework_scala_cli")
    end
end

return M
