lazy val root = project
  .in(file("."))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(CalibanPlugin)
  .settings(sharedSettings)
  .settings(
    name                := "root",
    Compile / mainClass := Some("app.Main"),
    Test / fork         := true
  )
  .dependsOn(core, square)

lazy val foo = (project in file("foo"))
  .settings(
    name := "Foo Project",
    version := "1.0"
  )

lazy val bar = (project in file("bar"))
  .settings(
    name := "Bar Project",
    version := "1.0"
  )
  .dependsOn(foo)
  
lazy val baz = (project in file("foo-baz"))
  .settings(
    name := "Baz Project",
    version := "1.0"
  )
  .dependsOn(foo)
  
