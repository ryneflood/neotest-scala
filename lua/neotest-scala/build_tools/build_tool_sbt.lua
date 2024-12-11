local path = require("plenary.path")
local lib = require("neotest.lib")
local test_parsers = require("neotest-scala.test_parsers")
local types = require("neotest-scala.types")

local M = {}

M.type = types.PROJECT_TYPES.SBT

function M.find_projects(path)
    -- local query = [[;;query
    --   (infix_expression left: ((identifier) @left (#eq? @left "project")))
    -- ]]

    local query = [[;;query
    ((val_definition
        pattern: (identifier) @id
        value: ((_) @value)))
    ]]

    -- we can use Treesitter to parse the build.sbt file and find the projects
    local file_content = lib.files.read(path)
    local ts = vim.treesitter
    -- Create a parser for the content using Treesitter
    local parser = ts.get_string_parser(file_content, "scala")
    local tree = parser:parse()[1]
    local root = tree:root()

    local query_obj = ts.query.parse("scala", query)

    local projects = {}
    local caps = {}

    for _, captures, _ in query_obj:iter_matches(root, file_content) do
        local capture = {}

        for id, node in pairs(captures) do
            local name = query_obj.captures[id] -- capture name

            local node_text = vim.treesitter.get_node_text(node, file_content)

            if name == "id" then
                capture.id = node_text
            end

            if name == "value" then
                capture.value = node_text
            end
        end

        table.insert(caps, capture)
    end

    for _, capture in ipairs(caps) do
        local pattern = "project in ([^%s]+)"
        local pattern2 = [[project.in%(file%(%"([.]+)%"%)%)]]

        local project_in_match = string.match(capture.value, pattern) or string.match(capture.value, pattern2)

        if project_in_match then
            table.insert(projects, capture.id)
        end
    end

    return projects
end

function M.get_test_framework(project_root, project_name)
    -- local query = [[;;query
    --   (infix_expression left: ((identifier) @left (#eq? @left "project")))
    -- ]]

    local query = [[;;query
    ((val_definition
        pattern: (identifier) @id
        value: ((_) @value)))
    ]]

    -- we can use Treesitter to parse the build.sbt file and find the projects
    local file_content = lib.files.read(project_root .. "/build.sbt")
    local ts = vim.treesitter
    -- Create a parser for the content using Treesitter
    local parser = ts.get_string_parser(file_content, "scala")
    local tree = parser:parse()[1]
    local root = tree:root()

    local query_obj = ts.query.parse("scala", query)

    local projects = {}
    local caps = {}

    for _, captures, _ in query_obj:iter_matches(root, file_content) do
        local capture = {}

        for id, node in pairs(captures) do
            local name = query_obj.captures[id] -- capture name

            local node_text = vim.treesitter.get_node_text(node, file_content)

            if name == "id" then
                capture.id = node_text
            end

            if name == "value" then
                capture.value = node_text
            end
        end

        table.insert(caps, capture)
    end

    for _, capture in ipairs(caps) do
        local pattern = [[%(project in file%(%"(.+)%"%)%).*]]
        local pattern2 = [[project.in%(file%("(.+)"%)%).*]]
        local no_line_breaks = string.gsub(capture.value, "\n", "")
        local sanitized = string.gsub(no_line_breaks, [[%s%s+]], "")
        local project_in_match = string.match(sanitized, pattern) or string.match(sanitized, pattern2)

        if project_in_match then
            table.insert(projects, capture.id)
        end
    end

    -- find the project from the list
    -- and test to find the test framework
    for _, capture in ipairs(caps) do
        if capture.id == project_name then
            if string.find(capture.value, "zio%-test") then
                return test_parsers.get_test_parser(types.TEST_FRAMEWORKS.ZIO_TEST)
            elseif string.find(capture.value, "munit") then
                return test_parsers.get_test_parser(types.TEST_FRAMEWORKS.MUNIT)
            end
        end
    end

    return nil
end

local function get_project_root(file_path)
    local result = lib.files.match_root_pattern("build.sbt")(file_path:absolute())

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

function M.get_project_name(file_path)
    local project_root = M.get_project_root(file_path)
    local absolute_file_path = path:new(file_path):absolute()
    local absolute_project_root_path = path:new(project_root):absolute()

    -- get the difference between the project root and the file path
    local file_path_split = vim.split(absolute_file_path, lib.files.sep, { plain = true, trimempty = true })
    local root_file_path_split =
        vim.split(absolute_project_root_path, lib.files.sep, { plain = true, trimempty = true })

    -- find the difference between two arrays
    local number_of_elements = #root_file_path_split

    local diff = file_path_split[number_of_elements + 1]

    print("diff", diff)

    ----
    local query = [[;;query
    ((val_definition
        pattern: (identifier) @id
        value: ((_) @value)))
    ]]

    -- we can use Treesitter to parse the build.sbt file and find the projects
    local file_content = lib.files.read(project_root .. "/build.sbt")
    local ts = vim.treesitter
    -- Create a parser for the content using Treesitter
    local parser = ts.get_string_parser(file_content, "scala")
    local tree = parser:parse()[1]
    local root = tree:root()

    local query_obj = ts.query.parse("scala", query)

    local caps = {}

    for _, captures, _ in query_obj:iter_matches(root, file_content) do
        local capture = {}

        for id, node in pairs(captures) do
            local name = query_obj.captures[id] -- capture name

            local node_text = vim.treesitter.get_node_text(node, file_content)

            if name == "id" then
                capture.id = node_text
            end

            if name == "value" then
                capture.value = node_text
            end
        end

        table.insert(caps, capture)
    end

    for _, capture in ipairs(caps) do
        local no_line_breaks = string.gsub(capture.value, "\n", "")
        local pattern = [[%(project in file%(%"(.+)%"%)%).*]]
        local pattern2 = [[project.in%(file%("(.+)"%)%).*]]
        local sanitized = string.gsub(no_line_breaks, [[%s%s+]], "")
        local project_in_match = string.match(sanitized, pattern) or string.match(sanitized, pattern2)
        print("capture.value", sanitized)

        print("project_in_match", project_in_match)

        if diff == "src" and project_in_match == "." then
            return capture.id
        end

        if project_in_match then
            if project_in_match == diff then
                return capture.id
            end
        end
    end
    ----

    return nil
end

return M
