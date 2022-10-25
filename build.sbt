// See README.md for license details.
import complete.DefaultParsers._

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "riscfree"

val chiselVersion = "3.5.1"
val roadmap_boot = settingKey[Unit]("roadmap boot information")
val elaborateCircuit = inputKey[Unit]("elaborate and dump circuits")

lazy val root = (project in file("."))
  .settings(
    name := "roadmap",
    libraryDependencies ++= Seq(
      "edu.berkeley.cs" %% "chisel3" % chiselVersion,
      "edu.berkeley.cs" %% "firrtl-diagrammer" % "1.5.4",
      "edu.berkeley.cs" %% "chiseltest" % "0.5.1" % "test"
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit",
      "-P:chiselplugin:genBundleElements",
    ),
    addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full),
    Global / excludeLintKeys += roadmap_boot,
    roadmap_boot := {
      val info = """|      ____
                    |     /___/\_
                    |    _\   \/_/\__              Hi, there is the playground of the riscfree Project
                    |  __\       \/_/\
                    |  \   __    __ \ \              Elaborate Verilog:
                    | __\  \_\   \_\ \ \   __          1. "runMain + class name (with main method)"
                    |/_/\\   __   __  \ \_/_/\         2. "run", and select your target
                    |\_\/_\__\/\__\/\__\/_\_\/       Tips: add `~` before command to automatically execute
                    |   \_\/_/\       /_\_\/               whenever source files change.
                    |      \_\/       \_\/       """.stripMargin
      println(info)
    },
    elaborateCircuit := (Def.inputTaskDyn {
      val s: TaskStreams = streams.value
      val classPath = trimmed(any.* map(_.mkString)).parsed
      val classArray = classPath.split("\\.")
      if (classArray.length > 1) {
        Def.taskDyn{
          (Compile / runMain).toTask(" " + (classArray.dropRight(1) ++ Array("Gen" + classArray.last)).mkString(".")).value
          Def.task {
            import sys.process._
            val firrtl_path = baseDirectory.value + "/build/" + classArray.last + ".fir"
            s.log.info(scala.Console.BLUE + "Dump FIRRTL (" + firrtl_path + ")")
            println(("cat " + firrtl_path).!!)
            val verilog_path = baseDirectory.value + "/build/" + classArray.last + ".v"
            s.log.info(scala.Console.BLUE + "Dump FIRRTL (" + verilog_path + ")")
            println(("cat " + verilog_path).!!)
          }
        }
      } else {
        Def.task {
          s.log.err("Invalid class path: " + classPath)
        }
      }
    }).evaluated
  )

