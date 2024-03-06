package roadmap

import chisel3._
import chisel3.util._
import chisel3.experimental._


class VecFilterDemo() extends Module {
    val fetchWidth = 4
    val numEntries = 8
    val decodeWidth = 2
    val coreWidth = decodeWidth
    val numRows = numEntries / coreWidth

    val head = RegInit(1.U(numRows.W))
    val tail = RegInit(1.U(numEntries.W))
    
    val maybe_full = RegInit(false.B)

    def rotateLeft(in: UInt, k: Int) = {
        val n = in.getWidth
        Cat(in(n-k-1,0), in(n-1, n-k))
    }

    val might_hit_head = (1 until fetchWidth).map(
        k => VecInit(rotateLeft(tail, k).asBools.zipWithIndex.filter {
            case (e,i) => i % coreWidth == 0
        }.map {
            case (e,i) => e
        }).asUInt
    ).map(
        tail => head & tail
    ).reduce(_|_).orR

    val at_head = (VecInit(tail.asBools.zipWithIndex.filter {
        case (e,i) => i % coreWidth == 0
    }.map {
        case (e,i) => e
    }).asUInt & head).orR

    val do_enq = !(at_head && maybe_full || might_hit_head)

    dontTouch(might_hit_head)
    dontTouch(at_head)
    // dontTouch(do_enq)
}
