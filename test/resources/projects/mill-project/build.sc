import mill._
import mill.scalalib._

object foo extends ScalaModule {
  def scalaVersion = "3.5.0"

  object test extends ScalaTests with TestModule.Munit {
    def ivyDeps = Agg(
      ivy"org.scalameta::munit::1.0.1"
    )
  }
}

object bar extends ScalaModule {
  def scalaVersion = "3.5.0"

  object test extends ScalaTests with TestModule.Munit {
    def ivyDeps = Agg(
      ivy"org.scalameta::munit::1.0.1"
    )
  }
}
