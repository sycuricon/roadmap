package roadmap.diplomacy.unit.bundle

import chisel3._
import diplomacy.lazymodule._
import diplomacy.bundlebridge._

import roadmap.diplomacy._

object nexusTest extends diplomacyTest(
  new LazyModule with ExplicitNode {
    val sourceModule = LazyModule(new SourceLazyModule)
    val sinkModule = LazyModule(new SinkLazyModule)
    val othersinkModule = LazyModule(new SinkLazyModule)

    val nexusNode = BundleBridgeNexus[UInt]()

    nexusNode := sourceModule.source
    sinkModule.sink := nexusNode
    othersinkModule.sink := nexusNode

    lazy val module = new LazyModuleImp(this)
  })