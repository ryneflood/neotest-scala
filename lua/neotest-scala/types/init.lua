local M = {}

---@class neotestscala.ParsedTest
---@field id string
---@field name string
---@field path string[]
---@field test_framework neotestscala.TestFramework

---@class neotestscala.ParsedPosition
---@field type string
---@field positions neotestscala.ParsedTest[]
---@field only string[]

---@enum neotestscala.ProjectType
M.PROJECT_TYPES = {
    SBT = "sbt",
    MILL = "mill",
    SCALA_CLI = "scala-cli",
}

---@enum neotestscala.TestFramework
M.TEST_FRAMEWORKS = {
    MUNIT = "munit",
    ZIO_TEST = "zio-test",
    SCALATEST = "scalatest",
}

return M
