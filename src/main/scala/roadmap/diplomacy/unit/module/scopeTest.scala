package roadmap.diplomacy.unit.module

import chisel3._
import diplomacy.lazymodule._

import roadmap.diplomacy._

object scopeTest extends diplomacyTest(
  new LazyModule {
    override lazy val desiredName = "wrapper_LazyModule"
    val scop = LazyScope {
      LazyModule(new LazyModule { lazy val module = new LazyModuleImp(this) })
    }

    lazy val module = new LazyModuleImp(this)
  }
)

