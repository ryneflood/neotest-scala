local Path = require("plenary.path")
local lib = require("neotest.lib")
local fw = require("neotest-scala.framework")
local utils = require("neotest-scala.utils")

---@type neotest.Adapter
local ScalaNeotestAdapter = { name = "neotest-scala" }

ScalaNeotestAdapter.root = lib.files.match_root_pattern("build.sbt")

---@async
---@param file_path string
---@return boolean
function ScalaNeotestAdapter.is_test_file(file_path)
    if not vim.endswith(file_path, ".scala") then
        return false
    end
    local elems = vim.split(file_path, Path.path.sep)
    local file_name = string.lower(elems[#elems])
    local patterns = { "test", "spec", "suite" }
    for _, pattern in ipairs(patterns) do
        if string.find(file_name, pattern) then
            return true
        end
    end
    return false
end

function ScalaNeotestAdapter.filter_dir(_, _, _)
    return true
end

---@param pos neotest.Position
---@return string
local get_parent_name = function(pos)
    if pos.type == "dir" or pos.type == "file" then
        return ""
    end
    if pos.type == "namespace" then
        return utils.get_package_name(pos.path) .. pos.name
    end
    return utils.get_position_name(pos)
end

---@param position neotest.Position The position to return an ID for
---@param parents neotest.Position[] Parent positions for the position
---@return string
local function build_position_id(position, parents)
    return table.concat(
        vim.tbl_flatten({
            vim.tbl_map(get_parent_name, parents),
            utils.get_position_name(position),
        }),
        "."
    )
end

---@async
---@return neotest.Tree | nil
function ScalaNeotestAdapter.discover_positions(path)
    local query = [[
	  (object_definition
	   name: (identifier) @namespace.name)
	   @namespace.definition
	  
      (class_definition
      name: (identifier) @namespace.name)
      @namespace.definition

      ((call_expression
        function: (call_expression
        function: (identifier) @func_name (#match? @func_name "test")
        arguments: (arguments (string) @test.name))
      )) @test.definition
    ]]
    return lib.treesitter.parse_positions(
        path,
        query,
        { nested_tests = true, require_namespaces = true, position_id = build_position_id }
    )
end

local function get_runner()
    local vim_test_runner = vim.g["test#scala#runner"]
    if vim_test_runner == "blooptest" then
        return "bloop"
    end
    if vim_test_runner and lib.func_util.index({ "bloop", "sbt" }, vim_test_runner) then
        return vim_test_runner
    end
    return "bloop"
end

local function get_args()
    return {}
end

local function get_framework()
    -- TODO: Automatically detect framework based on build.sbt
    return "utest"
end

---Get first project name from bloop projects.
---@return string|nil
local function get_bloop_project_name()
    local command = "bloop projects"
    local handle = assert(io.popen(command), string.format("unable to execute: [%s]", command))
    local result = handle:read("*l")
    handle:close()
    return result
end

---Get project name from build file.
---@return string|nil
local function get_project_name(path, runner)
    -- print("get_project_name")
    local root = ScalaNeotestAdapter.root(path)
    local build_file = root .. "/build.sbt"
    print("build_file is", build_file)
    local success, lines = pcall(lib.files.read_lines, build_file)
    if not success then
        return nil
    end
    for _, line in ipairs(lines) do
        local project = line:match('^name := "(.+)"')
        if project then
            return project
        end
    end
    if runner == "bloop" then
        local bloop_project = get_bloop_project_name()
        if bloop_project then
            return bloop_project
        end
    end
    return nil
end

---Builds strategy configuration for running tests.
---@param strategy string
---@param tree neotest.Tree
---@param project string
---@return table|nil
local function get_strategy_config(strategy, tree, project)
    local position = tree:data()
    if strategy ~= "dap" or position.type == "dir" then
        return nil
    end
    if position.type == "file" then
        return {
            type = "scala",
            request = "launch",
            name = "NeotestScala",
            metals = {
                runType = "testFile",
                path = position.path,
            },
        }
    end
    local metals_arguments = nil
    if position.type == "namespace" then
        metals_arguments = {
            testClass = utils.get_package_name(position.path) .. position.name,
        }
    end
    if position.type == "test" then
        local root = ScalaNeotestAdapter.root(position.path)
        local parent = tree:parent():data()
        vim.uri_from_fname(root)
        -- Constructs ScalaTestSuitesDebugRequest request.
        metals_arguments = {
            target = { uri = "file:" .. root .. "/?id=" .. project .. "-test" },
            requestData = {
                suites = {
                    {
                        className = get_parent_name(parent),
                        tests = { utils.get_position_name(position) },
                    },
                },
                jvmOptions = {},
                environmentVariables = {},
            },
        }
    end
    if metals_arguments ~= nil then
        return {
            type = "scala",
            request = "launch",
            -- NOTE: The `from_lens` is set here because nvim-metals passes the
            -- complete `metals` param to metals server without modifying
            -- (reading) it.
            name = "from_lens",
            metals = metals_arguments,
        }
    end
    return nil
end

---@async
---@param args neotest.RunArgs
---@return neotest.RunSpec
function ScalaNeotestAdapter.build_spec(args)
    local position = args.tree:data()
    local runner = get_runner()
    assert(lib.func_util.index({ "bloop", "sbt" }, runner), "set sbt or bloop runner")
    local project = get_project_name(position.path, runner)
    assert(project, "scala project not found in the build file")
    local framework = fw.get_framework_class(get_framework())
    if not framework then
        return {}
    end
    local extra_args = vim.list_extend(get_args(), args.extra_args or {})
    local command = framework.build_command(runner, project, args.tree, utils.get_position_name(position), extra_args)
    local strategy = get_strategy_config(args.strategy, args.tree, project)
    return { command = command, strategy = strategy }
end

---Extract results from the test output.
---@param tree neotest.Tree
---@param test_results table<string, string>
---@param match_func nil|fun(test_results: table<string, string>, position_id :string):string|nil
---@return table<string, neotest.Result>
local function get_results(tree, test_results, match_func)
    local no_results = vim.tbl_isempty(test_results)
    local results = {}
    for _, node in tree:iter_nodes() do
        local position = node:data()
        if no_results then
            results[position.id] = { status = TEST_FAILED }
        else
            local test_result
            if match_func then
                test_result = match_func(test_results, position.id)
            else
                test_result = test_results[position.id]
            end
            if test_result then
                results[position.id] = { status = test_result }
            end
        end
    end
    return results
end

---@async
---@param result neotest.StrategyResult
---@param tree neotest.Tree
---@return table<string, neotest.Result>
function ScalaNeotestAdapter.results(_, result, tree)
    local success, lines = pcall(lib.files.read_lines, result.output)
    local framework = fw.get_framework_class(get_framework())
    if not success or not framework then
        return {}
    end
    local test_results = framework.get_test_results(lines)
    return get_results(tree, test_results, framework.match_func)
end

local is_callable = function(obj)
    return type(obj) == "function" or (type(obj) == "table" and obj.__call)
end

setmetatable(ScalaNeotestAdapter, {
    __call = function(_, opts)
        if is_callable(opts.args) then
            get_args = opts.args
        elseif opts.args then
            get_args = function()
                return opts.args
            end
        end
        if is_callable(opts.runner) then
            get_runner = opts.runner
        elseif opts.runner then
            get_runner = function()
                return opts.runner
            end
        end
        if is_callable(opts.framework) then
            get_framework = opts.framework
        elseif opts.framework then
            get_framework = function()
                return opts.framework
            end
        end
        return ScalaNeotestAdapter
    end,
})

return ScalaNeotestAdapter
