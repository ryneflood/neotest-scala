local lib = require("neotest.lib")
local utils = require("neotest-scala.utils")
local project = require("neotest-scala.project")
local test_command_builder = require("neotest-scala.command_builders.bloop_test_command_builder")
local test_parser = require("neotest-scala.test_parsers")

local data = {}

local get_scala_runner = function()
    local paths = vim.api.nvim_get_runtime_file("bin/scala-runner", true)

    for _, path in ipairs(paths) do
        if vim.endswith(path, ("neotest-scala%sbin%sscala-runner"):format(lib.files.sep, lib.files.sep)) then
            return path
        end
    end

    error("scala-runner not found")
end

---@class neotest-python._AdapterConfig
---@field dap_args? table
---@field is_test_file fun(file_path: string):boolean
---@field get_args fun(runner: string, position: neotest.Position, strategy: string): string[]
---@field get_runner fun(python_command: string[]): string
return function(config)
    local function initialize()
        data["persistent"] = "persistent_data"

        -- get the cwd
        local cwd = vim.fn.getcwd()

        local project_type = project.get_project_type(cwd)

        data["project_type"] = project_type
        data["build_tool"] = project.get_project_build_tool(cwd)
    end

    initialize()
    -- FIXME: move this somewhere more appropriate
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
            -- local root = Project.get_project_root(position.path)
            -- ScalaNeotestAdapter.root(position.path)
            local parent = tree:parent():data()
            vim.uri_from_fname(root)
            -- Constructs ScalaTestSuitesDebugRequest request.
            metals_arguments = {
                target = { uri = "file:" .. root .. "/?id=" .. project .. "-test" },
                requestData = {
                    suites = {
                        {
                            className = utils.get_parent_name(parent),
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

    --@type neotest.Adapter
    return {
        name = "neotest-scala",
        root = function(path)
            if project.get_project_type(path) then
                return path
            end

            return nil
        end,
        filter_dir = function(_)
            return true
        end,
        is_test_file = function(file_path)
            local file_extension = ".scala"
            -- immediately, we can return false if the filename doesn't end in .scala
            if not vim.endswith(file_path, file_extension) then
                return false
            end

            -- now that we know it's a .scala file, we can remove that part of the file path
            -- remove ".scala" from the end of the string
            local filename_without_extension = file_path:sub(1, string.len(file_path) - string.len(file_extension))

            -- if the file ends in any of these patterns, we'll consider it a test file
            local test_file_patterns = { "test", "spec", "suite" }

            for _, pattern in ipairs(test_file_patterns) do
                -- check if the string ends with the pattern
                if utils.ends_with(string.lower(filename_without_extension), pattern) then
                    return true
                end
            end
            return false
        end,
        discover_positions = function(path)
            return test_parser.discover_positions(path)
        end,

        ---@param args neotest.RunArgs
        ---@return neotest.RunSpec
        build_spec = function(args)
            local build_tool = data["build_tool"]
            local project_name = build_tool.get_project_name(args.tree:data().path)
            -- local project_root = build_tool.get_project_root(args.tree:data().path)

            -- FIXME: we should have something like a SpecBuilder which is responsible for this
            -- so that we can test it in isolation
            local parsed_position = test_parser.parse_tree(args.tree)

            -- local runner = utils.get_test_runner(args.tree:data().path)
            -- assert(lib.func_util.index({ "bloop", "sbt", "scala-cli" }, runner), "unsupported runner: " .. runner)
            local project = assert(project_name, "scala project not found in the build file")

            local strategy = get_strategy_config(args.strategy, args.tree, project)

            local scala_runner = nil

            if config.get_scala_runner then
                scala_runner = config.get_scala_runner()
            else
                scala_runner = get_scala_runner()
            end

            local test_runner = "bloop"

            local command = test_command_builder.build_command(scala_runner, project, test_runner, parsed_position)

            print("COMMAND", command)

            return {
                command = command,
                strategy = strategy,
                context = {
                    results_path = "/tmp",
                },
            }
        end,
        ---@param spec neotest.RunSpec
        ---@param result neotest.StrategyResult
        ---@return neotest.Result[]
        results = function(spec, result)
            -- FIXME: write a test for this
            -- FIXME: factor this out into its own function/module
            local test_results = {}
            local success, contents = pcall(lib.files.read, result.output)

            if not success then
                return {}
            end

            -- parse contents into a table
            local lines = vim.split(contents, "\n")
            -- FIXME: this, as far as I can tell, only applies to scala-cli projects
            -- so we can move this into the scala-cli adapter
            local filtered_lines = vim.tbl_filter(function(line)
                return not vim.startswith(line, "\27[90mCompiling project")
                    and not vim.startswith(line, "\27[90mCompiled project")
                    and not vim.startswith(line, "Starting compilation server")
                    and not vim.startswith(line, "Bloop server started")
            end, lines)

            local filtered_lines = table.concat(filtered_lines, "\n")

            local json = vim.json.decode(filtered_lines)

            for _, node in ipairs(json) do
                test_results[node.id] = {
                    status = string.lower(node.status),
                    output = node.output,
                }
            end

            return test_results
        end,
    }
end
