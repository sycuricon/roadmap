package roadmap.diplomacy.unit.bundle

import chisel3._
import diplomacy.lazymodule._

import roadmap.diplomacy._

object makeSinkTest extends diplomacyTest (
  new LazyModule with ExplicitNode {
    val src = LazyModule(new SourceLazyModule)
    val sink = src.source.makeSink()
    lazy val module = new LazyModuleImp(this)
  })