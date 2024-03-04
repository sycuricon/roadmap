package roadmap

import chisel3._
import chisel3.util._
import chisel3.experimental._

object MaskLower {
  def apply(in: UInt) = {
    val n = in.getWidth
    (0 until n).map(i => in >> i.U).reduce(_|_)
  }
}

class MaskLowDemo() extends Module {
    val io = IO(new Bundle {
        val provider = Input(UInt(3.W))
    })
    
    
    // val allocatable_slots = (
    //   VecInit(f3_resps.map(r => !r(w).valid && r(w).bits.u === 0.U)).asUInt &
    //   ~(MaskLower(UIntToOH(provider)) & Fill(tageNTables, provided))
    // )
    val allocatable_slots = VecInit(~(MaskLower(UIntToOH(io.provider))))
    dontTouch(allocatable_slots)
}
