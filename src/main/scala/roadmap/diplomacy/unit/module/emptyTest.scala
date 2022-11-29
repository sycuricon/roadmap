package roadmap.diplomacy.unit.module

import chisel3._
import diplomacy.lazymodule._
import chipsalliance.rocketchip.config._

import roadmap.diplomacy._

class emptyModule extends SimpleLazyModule

object lazyModuleTest extends diplomacyTest(new emptyModule())