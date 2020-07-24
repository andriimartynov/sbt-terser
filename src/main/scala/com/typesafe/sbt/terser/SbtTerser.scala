package com.typesafe.sbt.terser

import com.typesafe.sbt.jse.{ SbtJsEngine, SbtJsTask }
import com.typesafe.sbt.web.incremental.{ OpFailure, OpResult, OpSuccess }
import com.typesafe.sbt.web.pipeline.Pipeline
import com.typesafe.sbt.web.{ incremental, Compat, SbtWeb }
import sbt.Keys._
import sbt._
import sbt.io.{ HiddenFileFilter, IO }

import scala.util.Try

object SbtTerser extends AutoPlugin {

  override def requires: Plugins = SbtJsTask

  override def trigger: PluginTrigger = AllRequirements

  val autoImport = Import

  import SbtJsEngine.autoImport.JsEngineKeys._
  import SbtJsTask.autoImport.JsTaskKeys._
  import SbtWeb.autoImport._
  import WebKeys._
  import autoImport._
  import TerserKeys._
  import TerserOps._

  override def projectSettings: Seq[Setting[_]] =
    Seq(
      excludeFilter in terser := HiddenFileFilter ||
        GlobFilter("*.min.js") ||
        new SimpleFileFilter(file => file.startsWith((WebKeys.webModuleDirectory in Assets).value)),
      includeFilter in terser := GlobFilter("*.js"),
      resourceManaged in terser := webTarget.value / terser.key.label,
      terser := runOptimizer.dependsOn(webJarsNodeModules in Plugin).value,
      terserAppDir := (resourceManaged in terser).value / "appdir",
      terserBuildDir := (resourceManaged in terser).value / "build",
      terserBeautifyOptions := None,
      terserComments := None,
      terserCompress := false,
      terserCompressOptions := None,
      terserConfigFile := None,
      terserDefinitionsOptions := None,
      terserEcma := None,
      terserIE8 := false,
      terserKeepClassNames := false,
      terserKeepFNames := false,
      terserMangle := false,
      terserMangleOptions := None,
      terserMangleProps := None,
      terserModule := false,
      terserNameCacheFile := None,
      terserSafari10 := false,
      terserTimings := false,
      terserToplevel := false,
      terserVerbose := false,
      terserWarn := true,
      terserWrap := None
    )

  private def runOptimizer: Def.Initialize[Task[Pipeline.Stage]] =
    Def.task {
      val include       = (includeFilter in terser).value
      val exclude       = (excludeFilter in terser).value
      val appDirValue   = terserAppDir.value
      val buildDirValue = terserBuildDir.value
      val streamsValue  = streams.value

      val commandValue                       = (command in terser).value
      val engineTypeValue                    = (engineType in terser).value
      val nodeModuleDirectoriesInPluginValue = (nodeModuleDirectories in Plugin).value
      val nodeModulePaths                    = nodeModuleDirectoriesInPluginValue.map(_.getPath)
      val stateValue                         = state.value
      val timeoutPerSourceValue              = (timeoutPerSource in terser).value
      val webJarsNodeModulesDirectoryValue   = (webJarsNodeModulesDirectory in Plugin).value

      lazy val commonArgs: Seq[String] =
        beautifyOptionsArgs ++
          commentsArgs ++
          compressArgs ++
          compressOptionsArgs ++
          configFileArgs ++
          definitionsOptionsArgs ++
          ecmaArgs ++
          ie8Args ++
          keepClassNamesArgs ++
          keepFNamesArgs ++
          mangleArgs ++
          mangleOptionsArgs ++
          manglePropsArgs ++
          moduleArgs ++
          nameCacheFileArgs ++
          safari10Args ++
          timingsArgs ++
          toplevelArgs ++
          verboseArgs ++
          warnArgs ++
          wrapArgs

      lazy val beautifyOptionsArgs: Seq[String] =
        terserBeautifyOptions.value
          .fold(Seq.empty[String]) { options =>
            Seq("-b", options)
          }

      lazy val commentsArgs: Seq[String] =
        terserComments.value
          .fold(Seq.empty[String]) { options =>
            Seq("--comments", options)
          }

      lazy val compressArgs: Seq[String] =
        if (terserCompress.value && terserCompressOptions.value.isEmpty) Seq("-c")
        else Seq.empty[String]

      lazy val compressOptionsArgs: Seq[String] =
        terserCompressOptions.value
          .fold(Seq.empty[String]) { options =>
            Seq("-c", options)
          }

      lazy val configFileArgs: Seq[String] =
        terserConfigFile.value
          .fold(Seq.empty[String]) { file =>
            Seq("--config-file", file.getPath)
          }

      lazy val definitionsOptionsArgs: Seq[String] =
        terserDefinitionsOptions.value
          .fold(Seq.empty[String]) { options =>
            Seq("-d", options)
          }

      lazy val ecmaArgs: Seq[String] =
        terserEcma.value
          .fold(Seq.empty[String]) { options =>
            Seq("--ecma", options)
          }

      lazy val ie8Args: Seq[String] =
        if (terserIE8.value) Seq("--ie8")
        else Seq.empty[String]

      lazy val keepClassNamesArgs: Seq[String] =
        if (terserKeepClassNames.value) Seq("--keep-classnames")
        else Seq.empty[String]

      lazy val keepFNamesArgs: Seq[String] =
        if (terserKeepFNames.value) Seq("--keep-fnames")
        else Seq.empty[String]

      lazy val mangleArgs: Seq[String] =
        if (terserMangle.value && terserMangleOptions.value.isEmpty) Seq("-m")
        else Seq.empty[String]

      lazy val mangleOptionsArgs: Seq[String] =
        terserMangleOptions.value
          .fold(Seq.empty[String]) { options =>
            Seq("-m", options)
          }

      lazy val manglePropsArgs: Seq[String] =
        terserMangleProps.value
          .fold(Seq.empty[String]) { options =>
            Seq("--mangle-props", options)
          }

      lazy val moduleArgs: Seq[String] =
        if (terserModule.value) Seq("--module")
        else Seq.empty[String]

      lazy val nameCacheFileArgs: Seq[String] =
        terserNameCacheFile.value
          .fold(Seq.empty[String]) { file =>
            Seq("--name-cache", file.getPath)
          }

      lazy val safari10Args: Seq[String] =
        if (terserSafari10.value) Seq("--safari10")
        else Seq.empty[String]

      lazy val timingsArgs: Seq[String] =
        if (terserTimings.value) Seq("--timings")
        else Seq.empty[String]

      lazy val toplevelArgs: Seq[String] =
        if (terserToplevel.value) Seq("--toplevel")
        else Seq.empty[String]

      lazy val verboseArgs: Seq[String] =
        if (terserVerbose.value) Seq("--verbose")
        else Seq.empty[String]

      lazy val warnArgs: Seq[String] =
        if (terserWarn.value) Seq("--warn")
        else Seq.empty[String]

      lazy val wrapArgs: Seq[String] =
        terserWrap.value
          .fold(Seq.empty[String]) { options =>
            Seq("--wrap", options)
          }

      mappings =>
        streamsValue.log.info("Optimizing JavaScript with terser")

        val optimizerMappings: Seq[(File, String)] = mappings
          .fileFilter(include, exclude)

        syncMappings(
          optimizerMappings,
          streamsValue,
          appDirValue
        )

        val (outputFiles, ()) =
          incremental
            .syncIncremental(
              streamsValue.cacheDirectory / "run",
              optimizerMappings.groupings(appDirValue)
            ) { modifiedGroupings =>
              if (modifiedGroupings.nonEmpty) {

                def execute(args: Seq[String]) = {
                  streamsValue.log.info("terser " + args.mkString(" "))
                  SbtJsTask.executeJs(
                    stateValue,
                    engineTypeValue,
                    commandValue,
                    nodeModulePaths,
                    webJarsNodeModulesDirectoryValue / "terser" / "bin" / "terser",
                    args,
                    timeoutPerSourceValue * optimizerMappings.size
                  )
                }

                val res: Seq[(OpGrouping, OpResult)] = modifiedGroupings.map { op =>
                  val outputFile = buildDirValue / op.outputFile
                  IO.createDirectory(outputFile.getParentFile)

                  val arg: Seq[String] = commonArgs ++ Seq[String](
                    "-o",
                    outputFile.getPath,
                    "--",
                    s"${op.inputFiles.head._1}"
                  )

                  (
                    op,
                    Try(execute(arg))
                      .map[OpResult](_ => OpSuccess(op.inputFiles.map(_._1).toSet, Set(outputFile)))
                      .recover { case _ => OpFailure }
                      .get
                  )
                }

                (res.toMap, ())

              } else {
                streamsValue.log.warn("no files to minify")
                (Map.empty, ())
              }

            }

        (mappings.toSet ++ outputFiles.pair(buildDirValue.relateTo)).toSeq
    }

  private def syncMappings(
    optimizerMappings: Seq[(File, String)],
    stream: Keys.TaskStreams,
    target: File
  ): File =
    SbtWeb.syncMappings(
      Compat.cacheStore(stream, "sync-terser"),
      optimizerMappings,
      target
    )

}
