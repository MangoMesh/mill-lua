import mill._
import mill.scalalib._
import mill.scalalib.publish._
import mill.scalalib.api.ZincWorkerUtil._

import $ivy.`io.chris-kipp::mill-ci-release::0.1.9`
import io.kipp.mill.ci.release.CiReleaseModule
import io.kipp.mill.ci.release.SonatypeHost

val millVersions = Seq("0.11.7", "0.10.15")

object `mill-lua` extends Cross[LuaModuleCross](millVersions)
trait LuaModuleCross extends Cross.Module[String] with ScalaModule with CiReleaseModule {
  override def scalaVersion = "2.13.12"
  override def artifactName = s"mill-lua_mill${scalaNativeBinaryVersion(crossValue)}"

  override def compileIvyDeps = super.compileIvyDeps() ++ Agg(
    ivy"com.lihaoyi::mill-main:$crossValue",
    ivy"com.lihaoyi::mill-scalalib:$crossValue"
  )

  override def sonatypeHost = Some(SonatypeHost.s01)

  def pomSettings = PomSettings(
    description = "A Lua plugin for the Mill build tool",
    organization = "com.ivmoreau",
    url = "https://github.com/MangoMesh/lua.mill",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github("MangoMesh", "lua.mill"),
    developers = Seq(Developer("ivanmoreau", "Ivan Molina Rebolledo", "https://github.com/ivanmoreau"))
  )
}