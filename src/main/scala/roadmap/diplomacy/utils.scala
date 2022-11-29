package roadmap.diplomacy

import chisel3.emitVerilog
import diplomacy.lazymodule._
import org.chipsalliance.cde.config.Parameters
import roadmap.diplomacy.unit.module.emptyModule

object writeGraphML {
  def apply(m: LazyModule, target_dir: String = "build"): Unit = {
    val fw = new java.io.FileWriter(new java.io.File(target_dir, s"${m.className}.graphml"))
    fw.write(m.graphML)
    fw.close()
  }
}

trait DisplayInGraphML { this: LazyModule =>
  override def omitGraphML: Boolean = false
}

abstract class diplomacyTest(module: LazyModule) extends App {
  implicit val config = Parameters.empty
  val m = LazyModule(module)
  emitVerilog(m.module, Array("--target-dir", "build", "--no-dce"))
  writeGraphML(m)
}