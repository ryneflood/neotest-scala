require("setup_test_environment")

local files = require("neotest.lib").files
local async = require("nio").tests
local position = require("neotest-scala.position")
local utils = require("neotest-scala.utils")
local Path = require("plenary.path")

local function assert_has_position(tree, pos_id)
    assert(position.has_position(tree, pos_id), "Position " .. pos_id .. " not found in tree " .. vim.inspect(tree))
end

local function assert_has_positions(spec, pos_ids)
    local fpath = vim.fn.tempname() .. ".scala"
    files.write(fpath, spec)

    local tree = position.discover_positions(fpath)

    for _, pos_id in ipairs(pos_ids) do
        assert_has_position(tree, pos_id)
    end
end

describe("adapter", function()
    local adapter = require("neotest-scala.adapter")({
        get_scala_runner = function()
            return "scala-runner"
        end,
    })

    describe("#root", function()
        it("should match directory that contains a Mill project", function()
            local project_directory =
                "/home/ryne/workspace/@rocketpants/neovim/neotest-scala/test/resources/projects/mill-project"

            local result = adapter.root(project_directory)

            assert.is_same(project_directory, result)
        end)

        it("should match directory that contains an SBT project", function()
            local project_directory =
                "/home/ryne/workspace/@rocketpants/neovim/neotest-scala/test/resources/projects/sbt-project"

            local result = adapter.root(project_directory)

            assert.is_same(project_directory, result)
        end)

        it(
            "should not match a directory which doesn't contain a supported Scala project (in this case an NPM project)",
            function()
                local project_directory =
                    "/home/ryne/workspace/@rocketpants/neovim/neotest-scala/tests/resources/projects/unsupported-project"

                local result = adapter.root(project_directory)

                assert.is_same(nil, result)
            end
        )
    end)

    describe("#is_test_file", function()
        it("should not match if the file does not end in .scala", function()
            local result = adapter.is_test_file("foo.bar.baz")

            assert.is_false(result)
        end)

        it("should match a filename which ends in ...Suite", function()
            local result = adapter.is_test_file("FooSuite.scala")

            assert.is_true(result)
        end)

        it("should match a filename which ends in ...Test", function()
            local result = adapter.is_test_file("FooTest.scala")

            assert.is_true(result)
        end)

        it("should match a filename which ends in ...Spec", function()
            local result = adapter.is_test_file("FooSpec.scala")

            assert.is_true(result)
        end)

        it("should not match a file whose name contains 'Spec', but not at the end of the file name", function()
            local result = adapter.is_test_file("FooSpecBar.scala")

            assert.is_false(result)
        end)

        it("should not match a file whose name contains 'Suite', but not at the end of the file name", function()
            local result = adapter.is_test_file("FooSuiteBar.scala")

            assert.is_false(result)
        end)

        it("should not match a file whose name contains 'Test', but not at the end of the file name", function()
            local result = adapter.is_test_file("FooTestBar.scala")

            assert.is_false(result)
        end)

        it("should match a file whose name ends in .test.scala", function()
            local result = adapter.is_test_file("Foo.test.scala")

            assert.is_true(result)
        end)
    end)

    describe("#find_positions", function()
        async.it("should be able to parse a Test Suite with a single test", function()
            local spec = [[
          package foo.bar

          import foo.Hello

          class MySuite extends munit.FunSuite:
            test("hello.with dot"):
              assertEquals(Hello.msg, "Hello World!")
        ]]

            assert_has_positions(spec, {
                "foo.bar.MySuite.hello.with dot",
            })
        end)

        async.it("should be able to parse a Test Suite with multiple tests", function()
            local test_cwd = vim.fn.getcwd()
            print("test_cwd is: ", test_cwd)
            local spec = [[
          package foo.bar

          import foo.Hello

          class MySuite extends munit.FunSuite:
            test("hello.with dot"):
              assertEquals(Hello.msg, "Hello World!")

            test("hello again"):
              assertEquals(Hello.msg, "Hello World.")

            test("hello again and again"):
              assertEquals(Hello.msg, "Hello World!")

            test("hello passing"):
              assertEquals(Hello.msg, "Hello World!")
        ]]

            assert_has_positions(spec, {
                "foo.bar.MySuite.hello.with dot",
                "foo.bar.MySuite.hello again",
                "foo.bar.MySuite.hello again and again",
                "foo.bar.MySuite.hello passing",
            })
        end)

        async.it("should be able to parse multiple Test Suites, each with multiple tests", function()
            local test_cwd = vim.fn.getcwd()
            print("test_cwd is: ", test_cwd)
            local spec = [[
          package foo.bar

          import foo.Hello

          class MySuite extends munit.FunSuite:
            test("hello.with dot"):
              assertEquals(Hello.msg, "Hello World!")

            test("hello again"):
              assertEquals(Hello.msg, "Hello World.")

            test("hello again and again"):
              assertEquals(Hello.msg, "Hello World!")

            test("hello passing"):
              assertEquals(Hello.msg, "Hello World!")
              
          class OtherSuite extends munit.FunSuite:
            test("hello.with dot"):
              assertEquals(Hello.msg, "Hello World!")

            test("hello again"):
              assertEquals(Hello.msg, "Hello World.")

            test("hello again and again"):
              assertEquals(Hello.msg, "Hello World!")

            test("hello passing"):
              assertEquals(Hello.msg, "Hello World!")
        ]]

            assert_has_positions(spec, {
                "foo.bar.MySuite.hello.with dot",
                "foo.bar.MySuite.hello again",
                "foo.bar.MySuite.hello again and again",
                "foo.bar.MySuite.hello passing",
                "foo.bar.OtherSuite.hello.with dot",
                "foo.bar.OtherSuite.hello again",
                "foo.bar.OtherSuite.hello again and again",
                "foo.bar.OtherSuite.hello passing",
            })
        end)
    end)

    -- FIXME: really this is specifically part of the Munit test framework adapter
    describe("#extract_test_suites", function()
        local Munit = require("neotest-scala.framework_munit")

        async.it("given a test position which is a test, should find the Test Suite + Test Name", function()
            local spec = [[
          package foo.bar

          import foo.Hello

          class MySuite extends munit.FunSuite:
            test("hello.with dot"):
              assertEquals(Hello.msg, "Hello World!")

            test("hello again"):
              assertEquals(Hello.msg, "Hello World.")

            test("hello again and again"):
              assertEquals(Hello.msg, "Hello World!")

            test("hello passing"):
              assertEquals(Hello.msg, "Hello World!")
        ]]

            local fpath = vim.fn.tempname() .. ".scala"
            files.write(fpath, spec)

            local tree = position.discover_positions(fpath)
            local test_position = position.find_position(tree, "foo.bar.MySuite.hello again")
            local result = Munit.extract_test_suites(test_position)

            local expected = {
                type = "test",
                test_name = "hello again",
                test_suites = { "foo.bar.MySuite" },
            }

            assert.is_same(expected, result)
        end)

        async.it("should find the single Test Suite in a file", function()
            local spec = [[
          package foo.bar

          import foo.Hello

          class MySuite extends munit.FunSuite:
            test("hello.with dot"):
              assertEquals(Hello.msg, "Hello World!")

            test("hello again"):
              assertEquals(Hello.msg, "Hello World.")

            test("hello again and again"):
              assertEquals(Hello.msg, "Hello World!")

            test("hello passing"):
              assertEquals(Hello.msg, "Hello World!")
        ]]

            local fpath = vim.fn.tempname() .. ".scala"
            files.write(fpath, spec)

            local tree = position.discover_positions(fpath)
            local result = Munit.extract_test_suites(tree)

            local expected = {
                type = "file",
                test_suites = { "foo.bar.MySuite" },
            }

            assert.is_same(expected, result)
        end)

        async.it("should find the multiple Test Suites in a file", function()
            local spec = [[
          package foo.bar

          import foo.Hello

          class MySuite extends munit.FunSuite:
            test("hello.with dot"):
              assertEquals(Hello.msg, "Hello World!")

            test("hello again"):
              assertEquals(Hello.msg, "Hello World.")

            test("hello again and again"):
              assertEquals(Hello.msg, "Hello World!")

            test("hello passing"):
              assertEquals(Hello.msg, "Hello World!")
              
          class OtherSuite extends munit.FunSuite:
            test("hello.with dot"):
              assertEquals(Hello.msg, "Hello World!")

            test("hello again"):
              assertEquals(Hello.msg, "Hello World.")

            test("hello again and again"):
              assertEquals(Hello.msg, "Hello World!")

            test("hello passing"):
              assertEquals(Hello.msg, "Hello World!")
        ]]

            local fpath = vim.fn.tempname() .. ".scala"
            files.write(fpath, spec)

            local tree = position.discover_positions(fpath)
            local result = Munit.extract_test_suites(tree)

            local expected = {
                type = "file",
                test_suites = { "foo.bar.MySuite", "foo.bar.OtherSuite" },
            }

            assert.is_same(expected, result)
        end)

        async.it("should find the Test Suite given a Namespace", function()
            local spec = [[
          package foo.bar

          import foo.Hello

          class MySuite extends munit.FunSuite:
            test("hello.with dot"):
              assertEquals(Hello.msg, "Hello World!")

            test("hello again"):
              assertEquals(Hello.msg, "Hello World.")

            test("hello again and again"):
              assertEquals(Hello.msg, "Hello World!")

            test("hello passing"):
              assertEquals(Hello.msg, "Hello World!")
              
          class OtherSuite extends munit.FunSuite:
            test("hello.with dot"):
              assertEquals(Hello.msg, "Hello World!")

            test("hello again"):
              assertEquals(Hello.msg, "Hello World.")

            test("hello again and again"):
              assertEquals(Hello.msg, "Hello World!")

            test("hello passing"):
              assertEquals(Hello.msg, "Hello World!")
        ]]

            local fpath = vim.fn.tempname() .. ".scala"
            files.write(fpath, spec)

            local tree = position.discover_positions(fpath)
            local test_position = position.find_position(tree, "foo.bar.MySuite")
            local result = Munit.extract_test_suites(test_position)

            local expected = {
                type = "namespace",
                test_suites = { "foo.bar.MySuite" },
            }

            assert.is_same(expected, result)
        end)

        async.it("should find nested Test Suites", function()
            -- the test id should look something like:
            -- neotest.TestOutputParserSuite$IsFailedTestSuite.should parse a failed test
            local spec = [[
          package foo.bar

          import foo.Hello

          object Suite:
            class InnerSuite extends munit.FunSuite:
                test("foo"):
                    assertEquals(Hello.msg, "Hello World!")
                    
                test("bar"):
                    assertEquals(Hello.msg, "Hello World!")
        ]]

            local fpath = vim.fn.tempname() .. ".scala"
            files.write(fpath, spec)

            local tree = position.discover_positions(fpath)
            local test_position = position.find_position(tree, "foo.bar.Suite")
            local result = Munit.extract_test_suites(test_position)

            local expected = {
                type = "namespace",
                test_suites = { "foo.bar.Suite", "foo.bar.Suite$InnerSuite" },
            }

            assert.is_same(expected, result)
        end)

        async.it("should find a single test within a Nested Test Suite", function()
            -- the test id should look something like:
            -- foo.bar.Suite$InnerSuite.foo
            local spec = [[
          package foo.bar

          import foo.Hello

          object Suite:
            class InnerSuite extends munit.FunSuite:
                test("foo"):
                    assertEquals(Hello.msg, "Hello World!")
                    
                test("bar"):
                    assertEquals(Hello.msg, "Hello World!")
        ]]

            local fpath = vim.fn.tempname() .. ".scala"
            files.write(fpath, spec)

            local tree = position.discover_positions(fpath)
            local test_position = position.find_position(tree, "foo.bar.Suite$InnerSuite.foo")
            local result = Munit.extract_test_suites(test_position)

            local expected = {
                type = "test",
                test_suites = { "foo.bar.Suite$InnerSuite" },
                test_name = "foo",
            }

            assert.is_same(expected, result)
        end)

        async.it("should find a single test within a doubly-Nested Test Suite", function()
            -- the test id should look something like:
            -- foo.bar.GrandparentSuite$ParentSuite$Suite.foo
            local spec = [[
          package foo.bar

          import foo.Hello

          object GrandparentSuite:
            object ParentSuite:
                class Suite extends munit.FunSuite:
                    test("foo"):
                        assertEquals(Hello.msg, "Hello World!")
                    
                    test("bar"):
                        assertEquals(Hello.msg, "Hello World!")
        ]]

            local fpath = vim.fn.tempname() .. ".scala"
            files.write(fpath, spec)

            local tree = position.discover_positions(fpath)
            local test_position = position.find_position(tree, "foo.bar.GrandparentSuite$ParentSuite$Suite.foo")
            local result = Munit.extract_test_suites(test_position)

            local expected = {
                type = "test",
                test_suites = { "foo.bar.GrandparentSuite$ParentSuite$Suite" },
                test_name = "foo",
            }

            assert.is_same(expected, result)
        end)

        async.it("should find a single, nested, Test Suite within a file", function()
            -- the test id should look something like:
            -- foo.bar.GrandparentSuite$ParentSuite$Suite
            local spec = [[
          package foo.bar

          import foo.Hello

          object GrandparentSuite:
            object ParentSuite:
                class Suite extends munit.FunSuite:
                    test("foo"):
                        assertEquals(Hello.msg, "Hello World!")
                    
                    test("bar"):
                        assertEquals(Hello.msg, "Hello World!")
        ]]

            local fpath = vim.fn.tempname() .. ".scala"
            files.write(fpath, spec)

            local tree = position.discover_positions(fpath)
            local test_position = position.find_position(tree, "foo.bar.GrandparentSuite$ParentSuite$Suite")
            local result = Munit.extract_test_suites(test_position)

            local expected = {
                type = "namespace",
                test_suites = { "foo.bar.GrandparentSuite$ParentSuite$Suite" },
            }

            assert.is_same(expected, result)
        end)

        async.it("should find multiple, nested, Test Suites within a file [zio-test]", function()
            local spec = [[
                package bar

                import bar.Hello

                import zio.test._

                object HelloWorldSpec extends ZIOSpecDefault:
                    def spec =
                        suite("HelloWorldSpec")(
                            test("Test But Not a Suite") {
                                assertTrue(Hello.msg == "Hello World, from Bar")
                            },
                            suite("Nested Suite")(
                                test("HelloWorld should say hello") {
                                    assertTrue(Hello.msg == "Hello World, from Bar")
                                },
                                test("HelloWorld should say hello but different") {
                                    val expected = "Expected"
                                    val result = Hello.msg

                                    assertTrue(Hello.msg == "Hello World, from Bar")
                                }
                            ),
                            test("Another Test That's Not a Suite") {
                                assertTrue(Hello.msg == "Hello World, from Bar")
                            },
                            suite("Another Nested Suite")(
                                test("HelloWorld should say hello") {
                                    assertTrue(Hello.msg == "Hello World, from Bar")
                                },
                                test("HelloWorld should say hello but different") {
                                    val result = Hello.msg

                                    assertTrue(Hello.msg == "Hello World, from Bar.")
                                }
                            ),
                            test("One More Test That's at the Bottom") {
                                assertTrue(Hello.msg == "Hello World, from Bar")
                            }
                        )
            ]]

            local fpath = vim.fn.tempname() .. ".scala"
            files.write(fpath, spec)

            local tree = position.discover_positions(fpath)
            -- print("tree is: ", vim.inspect(tree))
            local test_position = position.find_position(tree, fpath)
            local result = Munit.extract_test_suites(test_position)

            local expected = {
                type = "file",
                test_suites = {
                    "bar.HelloWorldSpec",
                    "bar.HelloWorldSpec.Nested Suite",
                    "bar.HelloWorldSpec.Another Nested Suite",
                },
            }

            assert.is_same(expected, result)
        end)

        async.it("should find multiple, nested, Test Suites within a file", function()
            local spec = [[
          package foo.bar

          import foo.Hello

          object GrandparentSuite:
            object ParentSuite:
                class Suite extends munit.FunSuite:
                    test("foo"):
                        assertEquals(Hello.msg, "Hello World!")
                    
                    test("bar"):
                        assertEquals(Hello.msg, "Hello World!")
                        
          object OuterSuite:
            object InnerSuite:
                class Suite extends munit.FunSuite:
                    test("foo"):
                        assertEquals(Hello.msg, "Hello World!")
                    
                    test("bar"):
                        assertEquals(Hello.msg, "Hello World!")
        ]]

            local fpath = vim.fn.tempname() .. ".scala"
            files.write(fpath, spec)

            local tree = position.discover_positions(fpath)
            local test_position = position.find_position(tree, fpath)
            local result = Munit.extract_test_suites(test_position)

            local expected = {
                type = "file",
                test_suites = {
                    "foo.bar.GrandparentSuite",
                    "foo.bar.GrandparentSuite$ParentSuite",
                    "foo.bar.GrandparentSuite$ParentSuite$Suite",
                    "foo.bar.OuterSuite",
                    "foo.bar.OuterSuite$InnerSuite",
                    "foo.bar.OuterSuite$InnerSuite$Suite",
                },
            }

            assert.is_same(expected, result)
        end)
    end)

    describe("#prepare_command", function()
        it("should prepare a command for a single test", function()
            local test_suites = { "foo.bar.FooSuite" }
            local test_name = "bar test"

            local result = utils.prepare_command("scala-runner", "bloop", "foo.test", "test", test_suites, test_name)

            local expected = {
                "scala-runner",
                -- "--",
                "--runner",
                "bloop",
                "--project",
                "foo.test",
                "-o",
                "foo.bar.FooSuite",
                "--single",
                '"foo.bar.FooSuite.bar test"',
                "--to",
                "/tmp",
            }

            assert.is_same(expected, result)
        end)

        it("should prepare a command for a single test suite", function()
            local test_suites = { "foo.bar.FooSuite" }

            local result = utils.prepare_command("scala-runner", "bloop", "foo.test", "file", test_suites)

            local expected = {
                "scala-runner",
                -- "--",
                "--runner",
                "bloop",
                "--project",
                "foo.test",
                "-o",
                "foo.bar.FooSuite",
                "--to",
                "/tmp",
            }

            assert.is_same(expected, result)
        end)

        it("should prepare a command for multiple test suites", function()
            local test_suites = { "foo.bar.FooSuite", "foo.bar.BarSuite" }

            local result = utils.prepare_command("scala-runner", "bloop", "foo.test", "file", test_suites)

            local expected = {
                "scala-runner",
                -- "--",
                "--runner",
                "bloop",
                "--project",
                "foo.test",
                "-o",
                "foo.bar.FooSuite",
                "-o",
                "foo.bar.BarSuite",
                "--to",
                "/tmp",
            }

            assert.is_same(expected, result)
        end)
    end)

    describe("#get_package_name", function()
        async.it("should read the package name from a .scala file", function()
            local spec = [[
          package foo.bar

          import foo.Hello

          class MySuite extends munit.FunSuite:
            test("hello"):
              assertEquals(Hello.msg, "Hello World!")
        ]]

            local fpath = vim.fn.tempname() .. ".scala"
            files.write(fpath, spec)

            local result = position.get_package_name(fpath)

            assert.is_same("foo.bar", result)
        end)
    end)

    describe("#build_spec", function()
        async.it("should build a spec for a single Test", function()
            local spec = [[
          package foo.bar

          import foo.Hello

          class MySuite extends munit.FunSuite:
            test("hello"):
              assertEquals(Hello.msg, "Hello World!")
        ]]
            local directory = vim.fn.tempname() .. "/foo/test/src/foo/"
            local fpath = directory .. "Spec.scala"
            Path:new(directory):mkdir({ parents = true })
            files.write(fpath, spec)

            local tree = position.discover_positions(fpath)
            local test_position = position.find_position(tree, "foo.bar.MySuite.hello")

            local args = {
                path = fpath,
                strategy = {},
                tree = test_position,
            }

            local result = adapter.build_spec(args)

            local expected = {
                command = 'scala-runner --runner bloop --project foo.test -o foo.bar.MySuite --single "foo.bar.MySuite.hello" --to /tmp',
                strategy = {},
                context = {
                    results_path = "/tmp",
                },
            }

            assert.is_same(expected.command, result.command)
        end)

        async.it("should build a spec for a single test suite", function()
            local spec = [[
          package foo.bar

          import foo.Hello

          class MySuite extends munit.FunSuite:
            test("hello"):
              assertEquals(Hello.msg, "Hello World!")
        ]]
            local directory = vim.fn.tempname() .. "/foo/test/src/foo/"
            local fpath = directory .. "Spec.scala"
            Path:new(directory):mkdir({ parents = true })
            files.write(fpath, spec)

            local tree = position.discover_positions(fpath)

            local test_position = position.find_position(tree, "foo.bar.MySuite")

            local args = {
                path = fpath,
                strategy = {},
                tree = test_position,
            }

            local result = adapter.build_spec(args)

            local expected = {
                command = "scala-runner --runner bloop --project foo.test -o foo.bar.MySuite --to /tmp",
                strategy = {},
                context = {
                    results_path = "/tmp",
                },
            }

            assert.is_same(expected.command, result.command)
        end)
    end)

    describe("#get_project_root", function()
        local Mill = require("neotest-scala.project_framework_mill")

        it("should find the project root for a file in a Mill project", function()
            local file_path =
                "/home/ryne/workspace/@rocketpants/neovim/neotest-scala/test/resources/projects/mill-project/bar/test/src/bar/Main.scala"

            local result = Mill.get_project_root(file_path)

            local expected =
                "/home/ryne/workspace/@rocketpants/neovim/neotest-scala/test/resources/projects/mill-project"

            assert.is_same(expected, result)
        end)
    end)
end)
