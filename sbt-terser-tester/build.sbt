import WebJs._

lazy val root = (project in file(".")).enablePlugins(SbtWeb)

pipelineStages := Seq(terser)