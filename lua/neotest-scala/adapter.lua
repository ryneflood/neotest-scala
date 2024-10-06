local lib = require("neotest.lib")
local utils = require("neotest-scala.utils")
local Position = require("neotest-scala.position")
local Project = require("neotest-scala.project")
local Munit = require("neotest-scala.framework_munit")
local Mill = require("neotest-scala.project_framework_mill")

local get_scala_runner = function(mode)
    -- FIXME: this probably shouldn't be hard-coded, yeah?
    -- local scala_runner = "scala-cli run /home/ryne/workspace/@rocketpants/neovim/neotest-scala/runner"
    -- local scala_runner = "/home/ryne/workspace/@rocketpants/neovim/neotest-scala/runner/scala-runner"
    -- function M.get_script_path()
    --
    if mode == "dev" then
        return "scala-cli /home/ryne/workspace/@rocketpants/neovim/neotest-scala-runner --"
    else
        local paths = vim.api.nvim_get_runtime_file("bin/scala-runner", true)

        for _, path in ipairs(paths) do
            if vim.endswith(path, ("neotest-scala%sbin%sscala-runner"):format(lib.files.sep, lib.files.sep)) then
                return path
            end
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
            ScalaNeotestAdapter.root(position.path)
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
            if Project.get_project_type(path) then
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
            return Position.discover_positions(path)
        end,
        ---@param args neotest.RunArgs
        ---@return neotest.RunSpec
        build_spec = function(args)
            -- obviously don't hard code the Munit framework
            local framework_adapter = Munit
            -- FIXME: obviously don't hard code this
            -- local framework = "munit"
            local test_suites = framework_adapter.extract_test_suites(args.tree)

            local project_name = Mill.get_project_name(args.tree:data().path)

            -- local runner = "bloop"
            local runner = utils.get_test_runner(args.tree:data().path)
            assert(lib.func_util.index({ "bloop", "sbt", "scala-cli" }, runner), "unsupported runner: " .. runner)
            local project = assert(project_name, "scala project not found in the build file")

            local strategy = get_strategy_config(args.strategy, args.tree, project)

            local scala_runner = nil

            -- check if the config table has a scala_runner key
            if config.get_scala_runner then
                scala_runner = config.get_scala_runner()
            else
                scala_runner = get_scala_runner(config.mode)
            end

            print("path is", args.tree:data().path)

            -- local test_framework = Mill.get_test_framework(args.tree:data().path, project_name)
            local test_framework = "zio-test"

            local command = utils.prepare_command(
                scala_runner,
                runner,
                project,
                test_framework,
                test_suites.type,
                test_suites.test_suites,
                test_suites.test_name
            )

            local flat_command = table.concat(command, " ")
            print("command", flat_command)

            return {
                command = table.concat(command, " "),
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
            local test_results = {}
            local success, contents = pcall(lib.files.read, result.output)
            -- local framework = fw.get_framework_class(get_framework())
            -- local framework = "munit"
            -- if not success or not framework then
            --     return {}
            -- end
            if not success then
                return {}
            end

            -- print("contents", vim.inspect(contents))
            -- parse contents into a table
            local lines = vim.split(contents, "\n")
            -- FIXME: this, as far as I can tell, only applies to scala-cli projects
            -- so we can move this into the scala-cli adapter
            -- remove any lines that start with 'compiled' or 'compiling'
            local filtered_lines = vim.tbl_filter(function(line)
                return not vim.startswith(line, "\27[90mCompiling project")
                    and not vim.startswith(line, "\27[90mCompiled project")
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
