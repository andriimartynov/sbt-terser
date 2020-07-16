sbt-terser
==========

An sbt-web plugin to perform [terser optimization](https://github.com/terser/terser) on the asset pipeline.

Why choose terser?
------------------

`uglify-es` is [no longer maintained](https://github.com/mishoo/UglifyJS2/issues/3156#issuecomment-392943058) and `uglify-js` does not support ES6+.

**`terser`** is a fork of `uglify-es` that mostly retains API and CLI compatibility
with `uglify-es` and `uglify-js@3`.

Usage
-----
To use this plugin, use the addSbtPlugin command within your project's `plugins.sbt` file:

```scala
addSbtPlugin("com.github.andriimartynov" % "sbt-terser" % "0.0.1")
```

Your project's build file also needs to enable sbt-web plugins. For example, with build.sbt:

```scala
lazy val root = (project in file(".")).enablePlugins(SbtWeb)
```

As with all sbt-web asset pipeline plugins you must declare their order of execution:

```scala
pipelineStages := Seq(terser)
```

A standard build profile for the terser optimizer is provided which will mangle variables for obfuscation and
compression. Each input `.js` file found in your assets folders will have a corresponding `.min.js` file and source maps will also be generated.

## includeFilter

If you wish to limit or extend what is uglified then you can use filters:
```scala
includeFilter in terser := GlobFilter("myjs/*.js"),
```
...where the above will include only those files under the `myjs` folder.

The sbt `excludeFilter` is also available to the `terser` scope and defaults to excluding the public folder and extracted Webjars.

## Settings
You are able to use and/or customize settings already made, and add your own. Here are a list of relevant settings and
their meanings (please refer to the [terser documentation](https://terser.org/docs/cli-usage) for details on the
options):

Option                  | Description                                                                                   | Default
------------------------|-----------------------------------------------------------------------------------------------|----------
terserBeautifyOptions   | Specify output options.                                                                       | `None`
terserComments          | Preserve copyright comments in the output.                                                    | `None`
terserCompress          | Enable compressor.                                                                            | `false`
terserCompressOptions   | Enable compressor/specify compressor options.                                                 | `None`
terserConfigFile        | Read `minify()` options from JSON file.                                                       | `None`
terserDefinitionsOptions| Global definitions.                                                                           | `None`
terserEcma              | Specify ECMAScript release.                                                                   | `None`
terserIE8               | Support non-standard Internet Explorer 8.                                                     | `false`
terserKeepClassNames    | Do not mangle/drop class names.                                                               | `false`
terserKeepFNames        | Do not mangle/drop function names.                                                            | `false`
terserMangle            | Mangle names.                                                                                 | `false`
terserMangleOptions     | Mangle names/specify mangler options.                                                         | `None`
terserMangleProps       | Mangle properties/specify mangler options.                                                    | `None`
terserModule            | Input is an ES6 module.                                                                       | `false`
terserNameCacheFile     | File to hold mangled name mappings.                                                           | `None`
terserSafari10          | Support non-standard Safari 10/11.                                                            | `false`
terserTimings           | Display operations run time on STDERR.                                                        | `false`
terserToplevel          | Compress and/or mangle variables in top level scope.                                          | `false`
terserVerbose           | Print diagnostic messages.                                                                    | `false`
terserWarn              | Print warning messages.                                                                       | `true`
terserWrap              | Embed everything in a big function, making the “exports” and “global” variables available.    | `None`

The plugin is built on top of [JavaScript Engine](https://github.com/typesafehub/js-engine) which supports different JavaScript runtimes.