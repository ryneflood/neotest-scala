local function setup_test_environment()
    local lazypath = vim.fn.stdpath("data") .. "/lazy"
    vim.opt.rtp:append(lazypath .. "/plenary.nvim")
    vim.opt.rtp:append(lazypath .. "/nvim-treesitter")
    vim.opt.rtp:append(lazypath .. "/nvim-nio")
    vim.opt.rtp:append(lazypath .. "/neotest")
end

setup_test_environment()
