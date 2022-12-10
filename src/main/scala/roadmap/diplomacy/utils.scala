package roadmap.diplomacy

import chisel3.emitVerilog
import diplomacy.lazymodule._

object writeGraphML {
  def apply(m: LazyModule, target_dir: String = "build"): Unit = {
    val fw = new java.io.FileWriter(new java.io.File(target_dir, s"${m.className}.graphml"))
    fw.write(m.graphML)
    fw.close()
  }
}

trait ExplicitNode { this: LazyModule =>
  override def omitGraphML: Boolean = false
}

abstract class diplomacyTest(lm: LazyModule) extends App {
  val module = LazyModule(lm)
  emitVerilog(module.module, Array("--target-dir", "build", "--no-dce"))
  writeGraphML(module)
}