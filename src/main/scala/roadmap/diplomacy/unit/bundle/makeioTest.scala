package roadmap.diplomacy.unit.bundle

import chisel3._
import diplomacy.lazymodule._
import diplomacy.bundlebridge._

import roadmap.diplomacy._

class nexusLazyModule extends LazyModule with ExplicitNode {
  val source = BundleBridgeSource(() => UInt(32.W))

  lazy val module = new LazyModuleImp(this) {
    source.bundle := 4.U
  }
}

object makeIOTest extends diplomacyTest (
  new LazyModule with ExplicitNode {
    val sinkNode = LazyModule(new SinkLazyModule).sink
    val srcNode = BundleBridgeSource[UInt](() => UInt(32.W))
    sinkNode := srcNode

    lazy val module = new LazyModuleImp(this) {
    // srcNode.bundle := dontTouch
    // val IO_in = srcNode.makeIO()
    // val IO_out = out.makeIO()
    }
  })