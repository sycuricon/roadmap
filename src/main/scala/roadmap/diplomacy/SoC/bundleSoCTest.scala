//package roadmap.diplomacy.SoC.bundle
//
//import chisel3._
//import diplomacy.lazymodule._
//import diplomacy.bundlebridge._
//import chipsalliance.rocketchip.config._
//import org.chipsalliance.cde.config.Parameters.empty
//
//import roadmap.diplomacy._
//
//class CPU_M extends Bundle
//class CPU_S extends Bundle
//class Core extends SimpleLazyModule with DisplayInGraphML
//class Block extends SimpleLazyModule with DisplayInGraphML
//class CPU extends SimpleLazyModule with DisplayInGraphML {
//  val cores = Seq.fill(2)(LazyModule(new Core()))
//  val cache = LazyModule(new Block())
//}
//
//class Device_M extends Bundle
//class Device_S extends Bundle
//class UART extends SimpleLazyModule with DisplayInGraphML
//class MEM extends SimpleLazyModule with DisplayInGraphML
//class DMA extends SimpleLazyModule with DisplayInGraphML
//
//class IO extends Bundle
//class SoC extends LazyModule with DisplayInGraphML {
//  val cpus = Seq.fill(2)(LazyModule(new CPU()))
//  val uart = LazyModule(new UART())
//  val memory = LazyModule(new MEM())
//  val dma = LazyModule(new DMA())
//
//  val ioInput = BundleBridgeSource[Bool](() => Bool())
//
//  lazy val module = new LazyModuleImp(this) {
//    ioInput.makeIO()
//  }
//}
//
//object bundleSoCTest extends App {
//  implicit val config = Parameters.empty
//  val m = LazyModule(new SoC())
//  emitVerilog(m.module, Array("--target-dir", "build"))
//  writeGraphML(m)
//}