package roadmap.diplomacy

import chisel3._
import sys.process._
import diplomacy.lazymodule._
import chipsalliance.rocketchip.config._

object writeGraphML {
  def apply(m: LazyModule): Unit = {
    (Seq("echo", m.graphML) #> new java.io.File(s"./build/${m.className}.graphml")).!
  }
}

class dummyLazyModule(implicit p: Parameters) extends SimpleLazyModule

object lazyModuleTest extends App {
  implicit val config = Parameters.empty
  val lazyModule = LazyModule(new dummyLazyModule())
  emitVerilog(lazyModule.module, Array("--target-dir", "build"))
  writeGraphML(lazyModule)
}