package roadmap.chisel

import chisel3._
import chisel3.experimental._
import chisel3.util._

import scala.collection.mutable.ArrayBuffer

class RegFile(n: Int, w: Int, zero: Boolean = false) {
  val rf = Mem(n, UInt(w.W))
  private def access(addr: UInt) = rf(~addr(log2Up(n)-1,0))
  private val reads = ArrayBuffer[(UInt,UInt)]()
  private var canRead = true
  def read(addr: UInt) = {
    prefix("RegFile_read"){
      require(canRead)
      reads += addr -> Wire(UInt())
      reads.last._2 := Mux(zero.B && addr === 0.U, 0.U, access(addr))
      reads.last._2
    }
  }
  def write(addr: UInt, data: UInt) = {
    prefix("RegFile_write") {
      canRead = false
      when (addr =/= 0.U) {
        access(addr) := data
        for ((raddr, rdata) <- reads)
          when (addr === raddr) { rdata := data }
      }
    }
  }
}

class RegFileDemo() extends Module {
  val io = IO(new Bundle{
    val rf_wen = Input(Bool())
    val rf_wdata = Input(UInt(64.W))
    val rf_waddr = Input(UInt(5.W))
    val rf_raddr1 = Input(UInt(5.W))
    val rf_raddr2 = Input(UInt(5.W))
    val rf_rdata1 = Output(UInt(64.W))
    val rf_rdata2 = Output(UInt(64.W))
  })

  val regAddrMask = (1 << 5) - 1
  val xLen = 64
  val rf = new RegFile(regAddrMask, xLen)

  val id_raddr = IndexedSeq(io.rf_raddr1, io.rf_raddr2)
  val id_rs = id_raddr.map(rf.read _)
  when (io.rf_wen) { rf.write(io.rf_waddr, io.rf_wdata) }
  
  io.rf_rdata1 := id_rs(0)
  io.rf_rdata2 := id_rs(1)
}
