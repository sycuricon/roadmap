package roadmap.diplomacy.demo

import chisel3._
import diplomacy.lazymodule._
import diplomacy.bundlebridge._

import roadmap.diplomacy._

object tutorialDemo1 extends diplomacyTest (
  new LazyModule with ExplicitNode {
    val source = BundleBridgeSource[UInt](() => UInt(32.W))
    val sink = BundleBridgeSink[UInt]()
    sink := source
    lazy val module = new LazyModuleImp(this) {
      source.bundle := 0.U
    }
  })

object tutorialDemo2 extends diplomacyTest (
  new LazyModule with ExplicitNode {
    val source0 = BundleBridgeSource[UInt](() => UInt(32.W))
    val nexus1 = BundleBridgeNexusNode[UInt](Some(() => UInt(48.W)))
    val sink2 = BundleBridgeSink[UInt]()
    val nexus3 = BundleBridgeNexusNode[UInt](Some(() => UInt(64.W)))
    val nexus4 = BundleBridgeNexusNode[UInt]()

    sink2 := nexus1 := source0
    nexus1 := nexus3
    nexus4 := nexus1
    nexus4 := nexus3

    lazy val module = new LazyModuleImp(this) {
      source0.bundle := 0.U
      for ((b, e) <- nexus3.out) b := 0.U
      for ((b, e) <- nexus1.out) b := 0.U
    }
  })