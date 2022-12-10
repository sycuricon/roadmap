//package roadmap.diplomacy.SoC.module
//
//import chisel3._
//import diplomacy.lazymodule._
//import chipsalliance.rocketchip.config._
//
//import roadmap.diplomacy._
//
//class Core extends SimpleLazyModule with DisplayInGraphML
//class Block extends SimpleLazyModule with DisplayInGraphML
//class CPU extends SimpleLazyModule with DisplayInGraphML {
//  val cores = Seq.fill(2)(LazyModule(new Core()))
//  val cache = LazyModule(new Block())
//}
//
//class UART extends SimpleLazyModule with DisplayInGraphML
//class MEM extends SimpleLazyModule with DisplayInGraphML
//class DMA extends SimpleLazyModule with DisplayInGraphML
//
//class SoC extends SimpleLazyModule with DisplayInGraphML {
//  val cpus = Seq.fill(2)(LazyModule(new CPU()))
//  val uart = LazyModule(new UART())
//  val memory = LazyModule(new MEM())
//  val dma = LazyModule(new DMA())
//}
//
//object hierarchyTest extends App {
//  implicit val config = Parameters.empty
//  val m = LazyModule(new SoC())
//  emitVerilog(m.module, Array("--target-dir", "build"))
//  writeGraphML(m)
//}