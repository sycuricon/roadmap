// See README.md for license details.

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "riscfree"

val chiselVersion = "3.5.1"
val roadmap_boot = settingKey[Unit]("roadmap boot information")

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
  )
