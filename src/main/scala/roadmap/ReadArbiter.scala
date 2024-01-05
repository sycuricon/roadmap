package roadmap

import chisel3._
import chisel3.util._
import chisel3.experimental._

class L1MetaReadReq() extends Bundle {
  val idx    = UInt(3.W)
}

class MetaReadReq(memWidth: Int) extends Bundle {
  val req = Vec(memWidth, new L1MetaReadReq)
}

class L1MetadataArray() extends Module() {
  val io = IO(new Bundle {
    val read = Flipped(Decoupled(new L1MetaReadReq))
  })
  io.read.ready := true.B
}

class ArbiterDemo() extends Module {
  val io = IO(new Bundle {

  })
  val memWidth = 1
  val meta = Seq.fill(memWidth) {
    Module(new L1MetadataArray())
  }
  val metaReadArb = Module(new Arbiter(new MetaReadReq(memWidth), 6))
  metaReadArb.io.in := DontCare
  for (w <- 0 until memWidth) {
    meta(w).io.read.valid := metaReadArb.io.out.valid
    meta(w).io.read.bits := metaReadArb.io.out.bits.req(w)
  }
  metaReadArb.io.out.ready := meta.map(_.io.read.ready).reduce(_ || _)
}

