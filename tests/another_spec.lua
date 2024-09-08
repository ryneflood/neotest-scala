local lazypath = vim.fn.stdpath("data") .. "/lazy"
vim.opt.rtp:append(lazypath .. "/plenary.nvim")
vim.opt.rtp:append(lazypath .. "/nvim-treesitter")
vim.opt.rtp:append(lazypath .. "/nvim-nio")
vim.opt.rtp:append(lazypath .. "/neotest")

describe("another set of basics", function()
    local neotest_scala

    before_each(function()
        neotest_scala = require("neotest-scala")
    end)

    it("it can be required properly", function()
        require("neotest-scala")
    end)

    it("can do stuff with strings", function()
        local str = neotest_scala.is_test_file("/tmp/yes")

        assert.equals(false, str)
    end)

    it("can do stuff other with strings", function()
        local str = neotest_scala.is_test_file("/tmp/yes.scala")

        assert.equals(false, str)
    end)

    it("should calculate the test runner command", function()
        local expected = "bloop test foo.test -o foo.bar.MySuite -- foo.bar.MySuite.hello"

        local str = require("neotest-scala/framework").build_command_with_test_path(
            -- project
            "foo.test",
            -- runner
            "bloop",
            -- package
            "foo.bar.",
            -- "parent" (need to change this name)
            "MySuite",
            -- test path (basically the full test path)
            "foo.bar.MySuite.hello",
            {}
        )

        local result = table.concat(str, " ")

        assert.equals(expected, result)
    end)

    it("hmmmm...", function()
        -- FIXME: we should make this realtive to the test file
        local lib = require("neotest.lib").files.match_root_pattern("build.sbt")("/tmp")

        assert.equals("/tmp", lib)
    end)

    it("should find available projects", function()
        local result = neotest_scala.get_mill_project_name(
            "/home/ryne/workspace/@rocketpants/neovim/neotest-scala/tests/resources/mill-project"
        )

        local expected = {
            "bar.test",
            "foo.test",
        }

        assert.is_same(expected, result)
    end)
end)
