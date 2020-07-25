package com.typesafe.sbt.terser

import com.typesafe.sbt.web.PathMapping
import com.typesafe.sbt.web.pipeline.Pipeline
import sbt.io.IO
import sbt._

object Import {

  val terser: TaskKey[Pipeline.Stage] =
    TaskKey[Pipeline.Stage]("terser", "Perform terser optimization on the asset pipeline.")

  case class OpGrouping(inputFiles: Seq[PathMapping], outputFile: String)

  object TerserKeys {
    val terserAppDir: SettingKey[File]                       =
      SettingKey[File](
        "terser-app-dir",
        "The top level directory that contains your app js files. In effect, this is the source folder that terser reads from."
      )
    val terserBuildDir: SettingKey[File]                     =
      SettingKey[File](
        "terser-build-dir",
        "By default, all modules are located relative to this path. In effect this is the target directory for terser."
      )
    val terserBeautifyOptions: SettingKey[Option[String]]    =
      settingKey[Option[String]]("Specify output options. Default: None")
    val terserComments: SettingKey[Option[String]]           =
      settingKey[Option[String]]("Preserve copyright comments in the output. Default: None")
    val terserCompress: SettingKey[Boolean]                  =
      settingKey[Boolean]("Enable compressor. Default: false")
    val terserCompressOptions: SettingKey[Option[String]]    =
      settingKey[Option[String]]("Enable compressor/specify compressor options. Default: None")
    val terserConfigFile: SettingKey[Option[File]]           =
      settingKey[Option[File]]("Read `minify()` options from JSON file. Default: None")
    val terserDefinitionsOptions: SettingKey[Option[String]] =
      settingKey[Option[String]]("Global definitions. Default: None")
    val terserEcma: SettingKey[Option[String]]               =
      settingKey[Option[String]]("Specify ECMAScript release. Default: None")
    val terserIE8: SettingKey[Boolean]                       =
      settingKey[Boolean]("Support non-standard Internet Explorer 8. Default: false")
    val terserKeepClassNames: SettingKey[Boolean]            =
      settingKey[Boolean]("Do not mangle/drop class names. Default: false")
    val terserKeepFNames: SettingKey[Boolean]                =
      settingKey[Boolean]("Do not mangle/drop function names. Default: false")
    val terserMangle: SettingKey[Boolean]                    =
      settingKey[Boolean]("Mangle names. Default: false")
    val terserMangleOptions: SettingKey[Option[String]]      =
      settingKey[Option[String]]("Mangle names/specify mangler options. Default: None")
    val terserMangleProps: SettingKey[Option[String]]        =
      settingKey[Option[String]]("Mangle properties/specify mangler options. Default: None")
    val terserModule: SettingKey[Boolean]                    =
      settingKey[Boolean]("Input is an ES6 module. Default: false")
    val terserNameCacheFile: SettingKey[Option[File]]        =
      settingKey[Option[File]]("File to hold mangled name mappings. Default: None")
    val terserSafari10: SettingKey[Boolean]                  =
      settingKey[Boolean]("Support non-standard Safari 10/11. Default: false")
    val terserTimings: SettingKey[Boolean]                   =
      settingKey[Boolean]("Display operations run time on STDERR. Default: false")
    val terserToplevel: SettingKey[Boolean]                  =
      settingKey[Boolean]("Compress and/or mangle variables in top level scope. Default: false")
    val terserVerbose: SettingKey[Boolean]                   =
      settingKey[Boolean]("Print diagnostic messages. Default: false")
    val terserWarn: SettingKey[Boolean]                      =
      settingKey[Boolean]("Print warning messages. Default: true")
    val terserWrap: SettingKey[Option[String]]               =
      settingKey[Option[String]](
        "Embed everything in a big function, making the “exports” and “global” variables available. Default: None"
      )
  }

  private[terser] object TerserOps {

    implicit class JsNameOps(
      private val self: String
    ) extends AnyVal {
      def minName: String = {
        val exti       = self.lastIndexOf('.')
        val (pfx, ext) =
          if (exti == -1) (self, "")
          else self.splitAt(exti)
        pfx + ".min" + ext
      }

    }

    implicit class MappingsOps(
      private val self: Seq[(File, String)]
    ) extends AnyVal {
      def fileFilter(
        include: FileFilter,
        exclude: FileFilter
      ): Seq[(File, String)] =
        self
          .filter(f =>
            !f._1.isDirectory &&
              include.accept(f._1) &&
              !exclude.accept(f._1)
          )

      def groupings(appDirValue: File): Seq[OpGrouping] =
        self
          .map(p => OpGrouping(Seq(appDirValue / p._2 -> p._2), p._2.minName))

    }

    implicit class RichFile(private val self: File) extends AnyVal {
      def startsWith(dir: File): Boolean =
        self.getPath.startsWith(dir.getPath)

      def relateTo: File => Option[String] =
        IO.relativize(self, _)

    }

  }

}
