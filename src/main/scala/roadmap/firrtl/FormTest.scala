package roadmap.firrtl

import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import firrtl._
import firrtl.passes._
import firrtl.ir.Circuit
import firrtl.options.Dependency
import firrtl.stage.TransformManager
import firrtl.stage._
import firrtl.options.DependencyManager
import roadmap._

abstract class PassTester {
  val input: Any
  val passes: Seq[Dependency[Pass]]

  def runCase(circuit: Circuit, transformer: TransformManager): Unit = {
    println("=" * 20 + "INPUT" + "=" * 20)
    val state = CircuitState(circuit, ChirrtlForm)
    println(state.circuit.serialize)
    println("=" * 20 + "OUTPUT" + "=" * 19)
    val new_state = transformer.execute(state)
    println(new_state.circuit.serialize)
    println("=" * 45 + "\n\n")
  }

  def runTest(test: Any, transformer: TransformManager): Unit = test match {
      case in: String => runCase(Parser.parse(in), transformer)
      case in: (() => RawModule) => runCase(ChiselStage.convert(in()), transformer)
      case in: List[Any] => in.map(runTest(_, transformer))
  }

  def run(): Unit = {
    println("Passes:")
    val transformer = new TransformManager(passes)
    println(transformer.prettyPrint() + "\n\n")
    runTest(input, transformer)
  }

}

object RegFileDemoTest extends PassTester {
  val input =
    """
      |circuit RegFileDemo :
      |  module RegFileDemo :
      |    input clock : Clock
      |    input reset : UInt<1>
      |    output io : { flip rf_wen : UInt<1>, flip rf_wdata : UInt<64>, flip rf_waddr : UInt<5>, flip rf_raddr1 : UInt<5>, flip rf_raddr2 : UInt<5>, rf_rdata1 : UInt<64>, rf_rdata2 : UInt<64>}
      |
      |    cmem rf : UInt<64> [31] @[RegFile.scala 9:15]
      |    wire id_rs_0 : UInt @[RegFile.scala 16:28]
      |    node _id_rs_RegFile_read_T = eq(io.rf_raddr1, UInt<1>("h0")) @[RegFile.scala 17:43]
      |    node _id_rs_RegFile_read_T_1 = and(UInt<1>("h0"), _id_rs_RegFile_read_T) @[RegFile.scala 17:35]
      |    node _id_rs_RegFile_read_T_2 = bits(io.rf_raddr1, 4, 0) @[RegFile.scala 10:44]
      |    node _id_rs_RegFile_read_T_3 = not(_id_rs_RegFile_read_T_2) @[RegFile.scala 10:39]
      |    infer mport id_rs_RegFile_read_MPORT = rf[_id_rs_RegFile_read_T_3], clock @[RegFile.scala 10:38]
      |    node _id_rs_RegFile_read_T_4 = mux(_id_rs_RegFile_read_T_1, UInt<1>("h0"), id_rs_RegFile_read_MPORT) @[RegFile.scala 17:27]
      |    id_rs_0 <= _id_rs_RegFile_read_T_4 @[RegFile.scala 17:21]
      |    wire id_rs_1 : UInt @[RegFile.scala 16:28]
      |    node _id_rs_RegFile_read_T_5 = eq(io.rf_raddr2, UInt<1>("h0")) @[RegFile.scala 17:43]
      |    node _id_rs_RegFile_read_T_6 = and(UInt<1>("h0"), _id_rs_RegFile_read_T_5) @[RegFile.scala 17:35]
      |    node _id_rs_RegFile_read_T_7 = bits(io.rf_raddr2, 4, 0) @[RegFile.scala 10:44]
      |    node _id_rs_RegFile_read_T_8 = not(_id_rs_RegFile_read_T_7) @[RegFile.scala 10:39]
      |    infer mport id_rs_RegFile_read_MPORT_1 = rf[_id_rs_RegFile_read_T_8], clock @[RegFile.scala 10:38]
      |    node _id_rs_RegFile_read_T_9 = mux(_id_rs_RegFile_read_T_6, UInt<1>("h0"), id_rs_RegFile_read_MPORT_1) @[RegFile.scala 17:27]
      |    id_rs_1 <= _id_rs_RegFile_read_T_9 @[RegFile.scala 17:21]
      |    when io.rf_wen : @[RegFile.scala 50:20]
      |      node _RegFile_write_T = neq(io.rf_waddr, UInt<1>("h0")) @[RegFile.scala 24:18]
      |      when _RegFile_write_T : @[RegFile.scala 24:27]
      |        node _RegFile_write_T_1 = bits(io.rf_waddr, 4, 0) @[RegFile.scala 10:44]
      |        node _RegFile_write_T_2 = not(_RegFile_write_T_1) @[RegFile.scala 10:39]
      |        infer mport RegFile_write_MPORT = rf[_RegFile_write_T_2], clock @[RegFile.scala 10:38]
      |        RegFile_write_MPORT <= io.rf_wdata @[RegFile.scala 25:22]
      |        node _RegFile_write_T_3 = eq(io.rf_waddr, io.rf_raddr1) @[RegFile.scala 27:22]
      |        when _RegFile_write_T_3 : @[RegFile.scala 27:33]
      |          id_rs_0 <= io.rf_wdata @[RegFile.scala 27:41]
      |        node _RegFile_write_T_4 = eq(io.rf_waddr, io.rf_raddr2) @[RegFile.scala 27:22]
      |        when _RegFile_write_T_4 : @[RegFile.scala 27:33]
      |          id_rs_1 <= io.rf_wdata @[RegFile.scala 27:41]
      |    io.rf_rdata1 <= id_rs_0 @[RegFile.scala 52:16]
      |    io.rf_rdata2 <= id_rs_1 @[RegFile.scala 53:16]
      |""".stripMargin

  val passes = Seq(Dependency(ZeroLengthVecs))
}

object PullMuxesTest extends PassTester {
  class test extends Module {
    val io = IO(new Bundle {
      val en = Input(Bool())
      val in1 = Input(Vec(2, UInt(64.W)))
      val in2 = Input(Vec(2, UInt(64.W)))
      val res = Output(UInt(64.W))
    })

    io.res := Mux(io.en, io.in1, io.in2)(0)
  }

  val input = () => new test
  val passes = Seq(Dependency(PullMuxes))
}


object ReplaceAccessesTest extends PassTester {
  class test extends Module {
    val io = IO(new Bundle {
      val in = Input(Vec(2, UInt(64.W)))
      val res = Output(UInt(64.W))
    })
    val index = 1.U
    io.res := io.in(index)
  }
  val input = () => new test
  val passes = Seq(Dependency(ReplaceAccesses))
}
object ExpandConnectsTest extends PassTester {
  class test extends Module {
    val io = IO(new Bundle {
      val in = Input(Vec(2, UInt(64.W)))
      val res = Output(Vec(2, UInt(64.W)))
    })
    io.res := io.in
  }

  val input = () => new test
  val passes = Seq(Dependency(ExpandConnects))
}

object RemoveAccessesTest extends PassTester {
  class test extends Module {
    val io = IO(new Bundle {
      val id = Input(UInt(2.W))
      val in = Input(Vec(4, UInt(64.W)))
      val out = Output(UInt(64.W))
    })
    io.out := io.in(io.id)
  }

  val input = () => new test
  val passes = Seq(Dependency(RemoveAccesses))
}


object ZeroLengthVecsTest extends PassTester {
  class test_1 extends Module {
    val io = IO(new Bundle {
      val in = Input(Vec(0, UInt(64.W)))
      val out = Output(Vec(0, UInt(64.W)))
    })
    io.out := io.in
  }


  val test_2 =
    """
      |circuit ZeroLengthVecsTest :
      |  module ZeroLengthVecsTest :
      |    input clock : Clock
      |    input reset : UInt<1>
      |
      |    wire x: Analog<2>
      |    wire y: Analog<2>
      |    wire z: Analog<2>
      |
      |    attach(x, y, z)
      |
      |""".stripMargin


  val input = List(() => new test_1, test_2)
  val passes = Seq(Dependency(RemoveAccesses))
}





object buildWithDifferentEmitter extends App {
  def run(moduleName: String, gen: () => RawModule): Unit = {
    println("Elaborating Module ...")
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
}

object runPassesTest extends App {

  run("roadmap.firrtl.ReplaceAccessesTest")
  run("roadmap.chisel.RegFileDemo")
  run("roadmap.chisel.ImmGenDemo")
  run("roadmap.chisel.ScoreboardDemo")
  def runPassesTest(test: PassTester): Unit = test.run()

  //run("RegFileDemo", () => new RegFileDemo())
  //run("ImmGenDemo", () => new ImmGenDemo())
  //run("ScoreboardDemo", () => new ScoreboardDemo())

  //runPassesTest(RegFileDemo)
  runPassesTest(ZeroLengthVecsTest)
}
