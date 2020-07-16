lazy val `sbt-terser` = project in file(".")
enablePlugins(SbtWebBase)
description := "sbt-web plugin for minifying JavaScript files"
organization := "com.github.andriimartynov"
licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))

sbtPlugin := true
scalacOptions += "-feature"

addSbtJsEngine("1.2.3")

crossSbtVersions := Seq("1.2.8")

crossScalaVersions := Seq("2.12.11")

credentials += Credentials(
  "GnuPG Key ID",
  "gpg",
  sys.env.getOrElse("GPG_PUBLIC_KEY", ""), // key identifier
  "ignored" // this field is ignored; passwords are supplied by pinentry
)

libraryDependencies ++= Seq(
  "org.webjars.npm" % "terser" % "4.8.0"
)
