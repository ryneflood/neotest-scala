local project = require("neotest-scala.project")

local M = {}

---@class neotest-scala.Framework
---@field build_command fun(runner: string, project: string, tree: neotest.Tree, name: string, extra_args: table|string): string[]
---@field get_test_results fun(output_lines: string[]): table<string, string>
---@field match_func nil|fun(test_results: table<string, string>, position_id :string):string|nil

M.type = PROJECT_TYPES.SBT

return M
