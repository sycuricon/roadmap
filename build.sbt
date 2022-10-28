// See README.md for license details.
import sys.process._
import sbt.complete.Parsers.spaceDelimited

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "riscfree"

val chiselVersion = "3.5.1"

Global / onChangedBuildSource := ReloadOnSourceChanges

/* Roadmap required configurations */
val roadmap_boot = settingKey[Unit]("roadmap boot information").withRank(KeyRanks.Invisible)
val elaborate = inputKey[Unit]("elaborate and dump circuits")
val cleanElaborate = taskKey[Unit]("Delete elaborate dictionary")
val dumpVerilog = false
val dumpFIRRTL = false
val targetDirectory = "build"
val otherArgs = Seq("--no-dce", 
                    "--infer-rw",
                    "--emission-options", "disableMemRandomization,disableRegisterRandomization",
                    "--gen-mem-verilog", "full"
                    )

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
    Compile / sourceGenerators += Def.task(Seq((Compile / sourceManaged).value / "Elaborate.scala")).taskValue,
    roadmap_boot := {
      val info = """|      ___                   Hi, there is the playground of the riscfree Project
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

