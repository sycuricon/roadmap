package roadmap

import chisel3._
import chisel3.util._
import chisel3.experimental._

object Helpers {
    implicit class UIntToAugmentedUInt(private val x: UInt) extends AnyVal {
        def extract(hi: Int, lo: Int): UInt = {
            require(hi >= lo-1)
            if (hi == lo-1) 0.U
            else x(hi, lo)
        }
    }
}

class LogNextractDemo() extends Module {
    import Helpers._
    
    val addr = WireInit(0.U(40.W))
    val coreInstBytes = 16 / 8
    val fetchWidth = 4

    val idx = addr.extract(log2Ceil(fetchWidth)+log2Ceil(coreInstBytes)-1, log2Ceil(coreInstBytes))

    val f1_mask = ((1 << fetchWidth)-1).U << idx
    dontTouch(f1_mask)
}
