local create_adapter = require("neotest-scala.adapter")

local ScalaNeotestAdapter = create_adapter()

setmetatable(ScalaNeotestAdapter, {
    __call = function(_, config)
        return create_adapter(config)
    end,
})

return ScalaNeotestAdapter
