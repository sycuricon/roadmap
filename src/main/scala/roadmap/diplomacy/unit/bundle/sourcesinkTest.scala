package roadmap.diplomacy.unit.bundle

import chisel3._
import diplomacy.lazymodule._
import diplomacy.bundlebridge._

import roadmap.diplomacy._

class SourceLazyModule extends LazyModule with DisplayInGraphML {
  val source = BundleBridgeSource[UInt](() => UInt(32.W))
  lazy val module = new LazyModuleImp(this)
}

class SinkLazyModule extends LazyModule with DisplayInGraphML {
  val sink = BundleBridgeSink[UInt]()
  lazy val module = new LazyModuleImp(this)
}

object sourceSinkTest extends diplomacyTest (
  new LazyModule with DisplayInGraphML {
  val sourceModule = LazyModule(new SourceLazyModule)
  val sinkModule = LazyModule(new SinkLazyModule)
  sinkModule.sink := sourceModule.source
  lazy val module = new LazyModuleImp(this)
})