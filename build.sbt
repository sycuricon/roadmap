import sbt.complete.Parsers.spaceDelimited

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion     := "2.12.16"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "zjv"

val chisel_src_mode = setSourceMode(true)
val firrtl_src_dir = System.setProperty("sbt.workspace", System.getProperty("user.dir") + "/depend")

/* roadmap Task */
val roadmapInfo = settingKey[Unit]("roadmap boot information").withRank(KeyRanks.Invisible)
val elaborate = inputKey[Unit]("elaborate and dump circuits")
val elaborateDir = settingKey[File]("roadmap elaborate target directory")
val cleanElaborate = taskKey[Unit]("Delete elaborate dictionary")

/* roadmap configurations */
val dumpVerilog = false
val dumpFIRRTL = false
val outputDir = "build"
val otherArgs = Seq(
  "--full-stacktrace",
  "-ll", "info", "--log-class-names",
  "--no-dce",
  "--infer-rw",
  "--emission-options", "disableMemRandomization,disableRegisterRandomization",
  "--gen-mem-verilog", "full"
)

lazy val roadmapSettings = Seq(
  name := "roadmap",
  libraryDependencies ++= Seq(
    // TODO: remove this after chisel 3.6
    "com.sifive" %% "chisel-circt" % "0.6.0",
    "edu.berkeley.cs" %% "firrtl-diagrammer" % "1.5.4",
    "edu.berkeley.cs" %% "chiseltest" % "0.5.4" % "test"
  ),
  Compile / scalacOptions ++= {
    val jar = (plugin / Compile / Keys.`package`).value
    val addPlugin = "-Xplugin:" + jar.getAbsolutePath
    // add plugin timestamp to compiler options to trigger recompile of
    // main after editing the plugin. (Otherwise a 'clean' is needed.)
    val dummy = "-Jdummy=" + jar.lastModified
    Seq(addPlugin, dummy)
  },
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-explaintypes",
    "-Xcheckinit",
    "-language:reflectiveCalls",
    "-P:chiselplugin:genBundleElements",
  ),
  Compile / sourceGenerators += Def.task {
    val file = (Compile / sourceManaged).value / "Elaborate.scala"
    IO.touch(file)
    Seq(file)
  }.taskValue,
  roadmapInfo := {
    val stamp = (Compile / sourceManaged).value / ".roadmap"
    if (!stamp.exists()) {
      val info =
        """|      ___                   Hi, there is the playground of the roadmap project
           |     /__/\__
           |    _\  \/_/\                 Elaborate Verilog:
           |    \  ___ \ \                  1.  "run", and then select your target
           |   __\ \  \ \ \   __            2.  "runMain + main class name"
           | _/_/\\ \__\ \ \_/_/\___      > 3.< "elaborate + chisel module name"
           |/_\ \/_\      \__\ \/__/\
           |\      __       __     \ \    Tips: add `~` before command to automatically execute
           | \_____\/\ _____\/\_____\/          whenever source files change.""".stripMargin
      println(scala.Console.CYAN + info)
      IO.touch(stamp)
    }
  },
  elaborateDir := {
    val targetDirectory = new File(outputDir)
    if (targetDirectory.isAbsolute) {
      targetDirectory
    }
    else {
      baseDirectory.value / outputDir
    }
  },
  elaborate := (Def.inputTaskDyn {
    cleanElaborate.value
    val s: TaskStreams = streams.value
    val args = spaceDelimited("<args>").parsed
    val classPath = args.head
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
              |emitVerilog(new ${classArray.last}(${args.drop(1).mkString(" ")}), Array("--target-dir", "${elaborateDir.value}") ++ Array("${otherArgs.mkString("\", \"")}") )
              |}""".stripMargin)
        }.value
        (Compile / runMain).toTask(
          s" ${classArray.dropRight(1).mkString(".")}._elaborate_${classArray.last}"
        ).value
        Def.task {
          if (dumpFIRRTL) {
            val firrtl_path = elaborateDir.value / s"${classArray.last}.fir"
            if (firrtl_path.exists()) {
              s.log.info(scala.Console.BLUE + s"FIRRTL code in ${firrtl_path}:")
              printf(IO.read(firrtl_path))
            }
            else {
              s.log.info(scala.Console.RED + s"Can not find ${firrtl_path}, does the elaborate task fail?")
            }
          }
          if (dumpVerilog) {
            val verilog_path = elaborateDir.value / s"${classArray.last}.v"
            if (verilog_path.exists()) {
              s.log.info(scala.Console.GREEN + s"Verilog code in ${verilog_path}:")
              printf(IO.read(verilog_path))
            }
            else {
              s.log.info(scala.Console.RED + s"Can not find ${verilog_path}, does the elaborate task fail?")
            }
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
    val build = elaborateDir.value
      s.log.warn(scala.Console.YELLOW + s"cleanElaborate: ${build} is removed")
    IO.delete(build)
  }
)

lazy val chisel = (project in file("depend/chisel3"))
lazy val core = (project in file("depend/chisel3/core"))
lazy val macros = (project in file("depend/chisel3/macros"))
lazy val plugin = (project in file("depend/chisel3/plugin"))

lazy val roadmap = (project in file("."))
  .settings(roadmapSettings: _*)
  .dependsOn(chisel, core, macros, plugin)
