local M = {}

local utils = require("neotest-scala.utils")
local Position = require("neotest-scala.position")

function M.extract_test_suites(tree)
    local type = tree:data().type

    print("#extract_test_suites")
    print("type: " .. type)

    if type == "test" then
        local test_name = string.gsub(tree:data().name, '"', "")
        local parent_tree = tree:parent()
        local parent_name = parent_tree:data().name

        local test_id = tree:data().id
        -- Escape special characters in test_name to safely use it in the pattern
        local escaped_test_name = test_name:gsub("([%^%$%(%)%%%.%[%]%*%+%-%?])", "%%%1")

        -- Use gsub to remove test_name from test_id
        -- remove the test_name from the end of test_id
        local result = test_id:sub(1, -string.len(test_name) - 1)

        local result_without_trailing_period = result:sub(1, -2)

        return {
            type = type,
            test_name = test_name,
            test_suites = { result_without_trailing_period },
        }
    end

    -- FIXME: obviously this is duplicated with the below type == "namespace" case
    if type == "file" then
        local test_suites = {}

        for _, child in tree:iter_nodes() do
            if child:data().type == "namespace" then
                print(child:data().id)
                table.insert(test_suites, child:data().id)
            end
        end

        return {
            type = type,
            test_suites = test_suites,
        }
    end

    -- FIXME: obviously this is duplicated with the above type == "file" case
    if type == "namespace" then
        local test_suites = {}

        -- print("-----------------------")
        -- print("it's a namespace, and I'm extracting the test suites ...")

        for _, child in tree:iter_nodes() do
            if child:data().type == "namespace" then
                print(child:data().id)
                table.insert(test_suites, child:data().id)
            end
        end

        return {
            type = type,
            test_suites = test_suites,
        }
    end

    if type == "dir" then
        local packages = {}

        for _, child in tree:iter_nodes() do
            if child:data().type == "namespace" then
                local package = Position.get_package_name(child:data().path)

                table.insert(packages, package .. "." .. child:data().name)
            end
        end

        return {
            type = type,
            test_suites = packages,
        }
    end
end

return M
