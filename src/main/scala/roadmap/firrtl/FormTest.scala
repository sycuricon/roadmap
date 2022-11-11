package roadmap.firrtl

import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation}
import firrtl._
import roadmap._

object FormTest extends App {
  def run(moduleName:String): Unit = {
    println(s"Elaborating ${moduleName} ...")

    new (chisel3.stage.ChiselStage).execute(
      Array("--target-dir", s"build/${moduleName}"),
      Seq(ChiselGeneratorAnnotation(() => Class.forName(moduleName).getConstructor().newInstance().asInstanceOf[RawModule])))

    val emitter_name = Array("chirrtl", "mhigh", "high", "middle", "low", "low-opt")
    var i: Int = 0
    for (name <- emitter_name) {
      new (chisel3.stage.ChiselStage).execute(
        Array("--target-dir", s"build/${moduleName}/${i}_".format(i).concat(name), "-E", name),
        Seq(ChiselGeneratorAnnotation(() => Class.forName(moduleName).getConstructor().newInstance().asInstanceOf[RawModule])))
      i = i + 1
    }
  }

  run("roadmap.chisel.RegFileDemo")
  run("roadmap.chisel.ImmGenDemo")
  run("roadmap.chisel.ScoreboardDemo")
}

