package debug

import chisel3._
import chisel3.stage.ChiselGeneratorAnnotation

class atom() extends Module {
  val io = IO(new Bundle{
    val in = Input(UInt(64.W))
    val out = Output(Vec(2, UInt(64.W)))
  })
  io.out(0) := io.in
  io.out(1) := io.in
}

class PullMuxesTest() extends Module {
  val io = IO(new Bundle{
    val en = Input(Bool())
    val in1 = Input(Vec(2, UInt(64.W)))
    val in2 = Input(Vec(2, UInt(64.W)))
    val res = Output(UInt(64.W))
  })

  io.res := Mux(io.en, io.in1, io.in2)(0)
}


class ReplaceAccessesTest() extends Module {
  val io = IO(new Bundle{
    val in = Input(Vec(2, UInt(64.W)))
    val res = Output(UInt(64.W))
  })
  val index = 1.U
  io.res := io.in(index)
}

class ExpandConnectTest() extends Module {
  val io = IO(new Bundle{
    val in = Input(Vec(2, UInt(64.W)))
    val res = Output(Vec(2, UInt(64.W)))
  })
  io.res := io.in
}

class RemoveAccessesTest() extends Module {
  val io = IO(new Bundle{
    val id = Input(UInt(2.W))
    val in = Input(Vec(4, UInt(64.W)))
    val out = Output(UInt(64.W))
  })
  io.out := io.in(io.id)
}

class ZeroLengthVecsTest() extends Module {
  val io = IO(new Bundle{
    val in = Input(Vec(0, UInt(64.W)))
    val out = Output(Vec(0, UInt(64.W)))
  })
  io.out := io.in
}

object buildWithDifferentEmitter extends App {
  def run(moduleName:String, gen: () => RawModule): Unit = {
    println("Elaborating Moudle ...")

    new (chisel3.stage.ChiselStage).execute(
      Array("--target-dir", s"build/${moduleName}"),
      Seq(ChiselGeneratorAnnotation(gen)))

    val emitter_name = Array("chirrtl", "mhigh", "high", "middle", "low", "low-opt")
    var i: Int = 0
    for (name <- emitter_name) {
      new (chisel3.stage.ChiselStage).execute(
        Array("--target-dir", s"build/${moduleName}/${i}_".format(i).concat(name), "-E", name),
        Seq(ChiselGeneratorAnnotation(gen)))
      i = i + 1
    }

  }

  run("PullMuxesTest", () => new PullMuxesTest())
  //run("RegFileDemo", () => new RegFileDemo())
  //run("ImmGenDemo", () => new ImmGenDemo())
  //run("ScoreboardDemo", () => new ScoreboardDemo())
}
