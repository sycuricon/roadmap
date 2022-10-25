// See README.md for license details.
import complete.DefaultParsers._
import sys.process._

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "riscfree"

val chiselVersion = "3.5.1"

/* Roadmap required configurations */
val roadmap_boot = settingKey[Unit]("roadmap boot information")
val elaborate = inputKey[Unit]("elaborate and dump circuits")
val dumpVerilog = true
val dumpFIRRTL = true

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
      val info = """|      ___                   Hi, there is the playground of the riscfree Project
                    |     /__/\__                
                    |    _\  \/_/\                 Elaborate Verilog:
                    |    \  ___ \ \                  1.  "run", and then select your target
                    |   __\ \  \ \ \   __            2.  "runMain + main class name"
                    | _/_/\\ \__\ \ \_/_/\___       >3.< "elaborate + chisel module name"
                    |/_\ \/_\      \__\ \/__/\   
                    |\      __       __     \ \    Tips: add `~` before command to automatically execute
                    | \_____\/\ _____\/\_____\/          whenever source files change.""".stripMargin
      println(scala.Console.CYAN+info)
    },
    elaborate := (Def.inputTaskDyn {
      val s: TaskStreams = streams.value
      val classPath = trimmed(any.* map(_.mkString)).parsed
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
                |emitVerilog(new ${classArray.last}(), Array("--target-dir", "build"))
                |}""".stripMargin)
            Seq(file)
          }.value
          (Compile / runMain).toTask(" " + (classArray.dropRight(1) ++ Array("_elaborate_" + classArray.last)).mkString(".")).value
          Def.task {
            if (dumpFIRRTL) {
              val firrtl_path = baseDirectory.value + "/build/" + classArray.last + ".fir"
              s.log.info(scala.Console.BLUE + "Dump FIRRTL (" + firrtl_path + ")")
              println(("cat " + firrtl_path).!!)
            }
            if (dumpVerilog) {
              val verilog_path = baseDirectory.value + "/build/" + classArray.last + ".v"
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
    Compile / sourceGenerators += Def.task(Seq((Compile / sourceManaged).value / "Elaborate.scala")).taskValue
  )

