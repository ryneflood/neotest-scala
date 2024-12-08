import mill._
import mill.scalalib._

object foo extends ScalaModule {
  def scalaVersion = "3.5.0"

  object test extends ScalaTests with TestModule.ZioTest {
    def ivyDeps = Agg(
      ivy"dev.zio::zio::2.1.9",
      ivy"dev.zio::zio-test::2.1.9",
      ivy"dev.zio::zio-test-sbt::2.1.9",
      ivy"dev.zio::zio-test-junit::2.1.9"
    )
  }
}

object bar extends ScalaModule {
  def scalaVersion = "3.5.0"

  object test extends ScalaTests with TestModule.ZioTest {
    def ivyDeps = Agg(
      ivy"dev.zio::zio::2.1.9",
      ivy"dev.zio::zio-test::2.1.9",
      ivy"dev.zio::zio-test-sbt::2.1.9",
      ivy"dev.zio::zio-test-junit::2.1.9"
    )
  }
}
