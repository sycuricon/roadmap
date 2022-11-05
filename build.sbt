import sys.process._
import sbt.complete.Parsers.spaceDelimited

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion     := "2.12.16"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "zjv"

lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-explaintypes",
    "-Xcheckinit",
    "-language:reflectiveCalls",
  ),
)

lazy val usePluginSettings = Seq(
  Compile / scalacOptions ++= {
    val jar = (plugin / Compile / Keys.`package`).value
    val addPlugin = "-Xplugin:" + jar.getAbsolutePath
    // add plugin timestamp to compiler options to trigger recompile of
    // main after editing the plugin. (Otherwise a 'clean' is needed.)
    val dummy = "-Jdummy=" + jar.lastModified
    Seq(addPlugin, dummy)
  }
)

/* roadmap Task */
val roadmapInfo = settingKey[Unit]("roadmap boot information").withRank(KeyRanks.Invisible)
val elaborate = inputKey[Unit]("elaborate and dump circuits")
val cleanElaborate = taskKey[Unit]("Delete elaborate dictionary")

/* roadmap configurations */
val dumpVerilog = false
val dumpFIRRTL = false
val targetDirectory = "build"
val otherArgs = Seq("--full-stacktrace",
                    "--no-dce", "-ll", "info", "--log-class-names",
                    "--infer-rw",
                    "--emission-options", "disableMemRandomization,disableRegisterRandomization",
                    "--gen-mem-verilog", "full"
                    )

lazy val roadmapSettings = Seq(
  name := "roadmap",
  libraryDependencies ++= Seq(
    // TODO: remove this after chisel 3.6
    "com.sifive" %% "chisel-circt" % "0.6.0",
    "edu.berkeley.cs" %% "firrtl-diagrammer" % "1.5.4"
  ),
  scalacOptions ++= Seq("-P:chiselplugin:genBundleElements"),
  Compile / sourceGenerators += Def.task {
    val file = (Compile / sourceManaged).value / "Elaborate.scala"
    IO.touch(file)
    Seq(file)
  }.taskValue,
  roadmapInfo := {
    val info = """|      ___                   Hi, there is the playground of the roadmap project
                  |     /__/\__                
                  |    _\  \/_/\                 Elaborate Verilog:
                  |    \  ___ \ \                  1.  "run", and then select your target
                  |   __\ \  \ \ \   __            2.  "runMain + main class name"
                  | _/_/\\ \__\ \ \_/_/\___      > 3.< "elaborate + chisel module name"
                  |/_\ \/_\      \__\ \/__/\     
                  |\      __       __     \ \    Tips: add `~` before command to automatically execute
                  | \_____\/\ _____\/\_____\/          whenever source files change.""".stripMargin
    println(scala.Console.CYAN+info)
  },
  elaborate := (Def.inputTaskDyn {
    cleanElaborate.value
    val s: TaskStreams = streams.value
    val args = spaceDelimited("<args>").parsed
    val classPath = args(0)
    val classArray = classPath.split("\\.")
    if (classArray.length > 1) {
      Def.taskDyn{
        Def.task {
          val file = (Compile / sourceManaged).value / "Elaborate.scala"
          IO.write(file,
            s"""package ${classArray.dropRight(1).mkString(".")}
              |import chisel3._
              |object ${"_elaborate_" + classArray.last} extends App {
              |println("[info] elaborating ${classPath} module")
              |emitVerilog(new ${classArray.last}(${args.drop(1).mkString(" ")}), Array("--target-dir", "${targetDirectory}") ++ Array("${otherArgs.mkString("\", \"")}") )
              |}""".stripMargin)
        }.value
        (Compile / runMain).toTask(
          s" ${classArray.dropRight(1).mkString(".")}._elaborate_${classArray.last}"
        ).value
        Def.task {
          if (dumpFIRRTL) {
            val firrtl_path = s"${baseDirectory.value}/${targetDirectory}/${classArray.last}.fir"
            s.log.info(scala.Console.BLUE + "Dump FIRRTL (" + firrtl_path + ")")
            println(("cat " + firrtl_path).!!)
          }
          if (dumpVerilog) {
            val verilog_path = s"${baseDirectory.value}/${targetDirectory}/${classArray.last}.v"
            s.log.info(scala.Console.BLUE + "Dump Verilog (" + verilog_path + ")")
            println(("cat " + verilog_path).!!)
          }
        }
      }
    } else {
      Def.task {
        s.log.err("Invalid class path: " + classPath)
      }
    }
  }).evaluated,
  cleanElaborate := {
    val s: TaskStreams = streams.value
    val build = baseDirectory.value / targetDirectory
    s.log.warn(scala.Console.YELLOW + s"cleanElaborate: ${build} is removed")
    IO.delete(build)
  }
)

lazy val roadmap = (project in file("."))
  .settings(commonSettings: _*)
  .settings(roadmapSettings: _*)
  .settings(usePluginSettings: _*)
  .dependsOn(firrtl, chisel, core, `macro`, plugin, chiseltest)

lazy val chiseltest = (project in file("repo/chiseltest"))
  .settings(commonSettings: _*)

lazy val firrtl = (project in file("repo/firrtl"))
  .settings(commonSettings: _*)

lazy val chisel = (project in file("repo/chisel3"))
  .settings(commonSettings: _*)
  .dependsOn(firrtl)

lazy val core = (project in file("repo/chisel3/core"))
  .settings(commonSettings: _*)
  .dependsOn(firrtl)

lazy val `macro` = (project in file("repo/chisel3/macro"))
  .settings(commonSettings: _*)
  .dependsOn(firrtl)

lazy val plugin = (project in file("repo/chisel3/plugin"))
  .settings(commonSettings: _*)
  .dependsOn(firrtl)

