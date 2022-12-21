package roadmap.diplomacy.unit.module

import chisel3._
import diplomacy.lazymodule._

import roadmap.diplomacy._

object moduleTest extends diplomacyTest(
  new LazyModule {
    val m1 = LazyModule(new SimpleLazyModule)
    val m2 = LazyModule(new SimpleLazyModule)

    lazy val module = new LazyModuleImp(this)
  }
)
