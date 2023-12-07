package roadmap.diplomacy.unit.bundle

import chisel3._
import diplomacy.lazymodule._
import diplomacy.bundlebridge._
import org.chipsalliance.cde.config.Parameters

import roadmap.diplomacy.{ExplicitNode, diplomacyTest}

// Add this line in diplomacy.nodes.IdentityNode.instantiate
// implicit val opts: chisel3.CompileOptions = chisel3.ExplicitCompileOptions.NotStrict.copy(inferModuleReset = true)

class SourceIdentity(implicit p: Parameters) extends LazyModule with ExplicitNode {
  val s0 = LazyModule(new SourceLazyModule)
  val s1 = LazyModule(new SourceLazyModule)
  val node = BundleBridgeIdentityNode[UInt]()
  node := s0.source
  node := s1.source
  lazy val module = new LazyModuleImp(this)
}

class SinkIdentity(implicit p: Parameters) extends LazyModule with ExplicitNode {
  val s0 = LazyModule(new SinkLazyModule)
  val s1 = LazyModule(new SinkLazyModule)
  val node = BundleBridgeIdentityNode[UInt]()
  s0.sink := node
  s1.sink := node
  lazy val module = new LazyModuleImp(this)
}

class IdentityModule(implicit p: Parameters) extends LazyModule with ExplicitNode {
  val sourceModule = LazyModule(new SourceIdentity)
  val sinkModule = LazyModule(new SinkIdentity)
  sinkModule.node :=* sourceModule.node
  lazy val module = new LazyModuleImp(this)
}

object identityTest extends diplomacyTest(new IdentityModule()(roadmap.diplomacy.default))
