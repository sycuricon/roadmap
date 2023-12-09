import sbt.complete.Parsers.spaceDelimited

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion     := "2.13.10"
ThisBuild / version          := "0.1.1"
ThisBuild / organization     := "sycuricon"

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

/* roadmap Task */
val roadmapInfo = settingKey[Unit]("roadmap boot information").withRank(KeyRanks.Invisible)
val elaborate = inputKey[Unit]("elaborate and dump circuits")
val elaborateDir = settingKey[File]("roadmap elaborate target directory")
val cleanElaborate = taskKey[Unit]("Delete elaborate dictionary")

/* roadmap configurations */
val dumpVerilog = false
val dumpFIRRTL = false
val outputDir = "build"
val userChiselArgs = Seq(
  "--dump-fir"
)
val userFirtoolArgs = Seq(
  "--disable-all-randomization",
  "--disable-opt"
)

lazy val roadmapSettings = Seq(
  name := "roadmap",
  libraryDependencies ++= Seq(
    "edu.berkeley.cs" %% "chiseltest" % "5.0.2" % "test"
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
          val chiselArgs = if (userChiselArgs.isEmpty) "" else "\"" + s"${userChiselArgs.mkString("\", \"")}" + "\", "
          val firtoolArgs = if (userFirtoolArgs.isEmpty) "" else "\"" + s"${userFirtoolArgs.mkString("\", \"")}" + "\""
          IO.write(file,
            s"""package ${classArray.dropRight(1).mkString(".")}
              |import circt.stage._
              |object ${"_elaborate_" + classArray.last} extends App {
              |println("[info] elaborating ${classPath} module")
              |ChiselStage.emitSystemVerilogFile(
              |    new ${classArray.last}(${args.drop(1).mkString(" ")}), 
              |    Array(${chiselArgs}"--target-dir", "${elaborateDir.value}"),
              |    Array(${firtoolArgs}))
              |}""".stripMargin)
        }.value
        (Compile / runMain).toTask(
          s" ${classArray.dropRight(1).mkString(".")}._elaborate_${classArray.last}"
        )
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
  .settings(commonSettings: _*)

lazy val roadmap = (project in file("."))
  .dependsOn(chisel)
  .settings(commonSettings: _*)
  .settings(roadmapSettings: _*)
  .settings(
       Compile / scalacOptions ++= {
         val jar = (Project("plugin", file("depend/chisel3/plugin")) / Compile / Keys.`package`).value
         val addPlugin = "-Xplugin:" + jar.getAbsolutePath
         // add plugin timestamp to compiler options to trigger recompile of
         // main after editing the plugin. (Otherwise a 'clean' is needed.)
         val dummy = "-Jdummy=" + jar.lastModified
         Seq(addPlugin, dummy)
       }
  )

