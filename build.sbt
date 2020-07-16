lazy val `sbt-terser` = project in file(".")
enablePlugins(SbtWebBase)
description := "sbt-web plugin for minifying JavaScript files"
organization := "com.github.andriimartynov"
licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))

sbtPlugin := true
scalacOptions += "-feature"

libraryDependencies ++= Seq(
  "org.webjars.npm" % "terser" % "4.8.0"
)

addSbtJsEngine("1.2.3")
