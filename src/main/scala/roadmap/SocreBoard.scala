package roadmap

import chisel3._

class Scoreboard(n: Int, zero: Boolean = false) {
  def set(en: Bool, addr: UInt): Unit = update(en, _next | mask(en, addr))
  def clear(en: Bool, addr: UInt): Unit = update(en, _next & ~mask(en, addr))
  def read(addr: UInt): Bool = r(addr)
  def readBypassed(addr: UInt): Bool = _next(addr)

  private val _r = RegInit(0.U(n.W))
  private val r = if (zero) (_r >> 1 << 1) else _r
  private var _next = r
  private var ens = false.B
  private def mask(en: Bool, addr: UInt) = Mux(en, 1.U << addr, 0.U)
  private def update(en: Bool, update: UInt) = {
    _next = update
    ens = ens || en
    when (ens) { _r := _next }
  }
}

class ScoreboardDemo() extends Module {
  val io = IO(new Bundle{
    val dec_ren1 = Input(Bool())
    val dec_ren2 = Input(Bool())
    val dec_ren3 = Input(Bool())
    val dec_wen = Input(Bool())
    val dec_raddr1 = Input(UInt(5.W))
    val dec_raddr2 = Input(UInt(5.W))
    val dec_raddr3 = Input(UInt(5.W))
    val dec_waddr = Input(UInt(5.W))
    val wb_valid = Input(Bool())
    val wb_waddr = Input(UInt(5.W))
    val resp_fpu = Input(Bool())
    val resp_waddr = Input(UInt(5.W))
    val stall_fpu = Output(Bool())
  })

  def checkHazards(targets: Seq[(Bool, UInt)], cond: UInt => Bool) =
    targets.map(h => h._1 && cond(h._2)).reduce(_||_)
  
  val fp_hazard_targets = Seq((io.dec_ren1, io.dec_raddr1),
                              (io.dec_ren2, io.dec_raddr2),
                              (io.dec_ren3, io.dec_raddr3),
                              (io.dec_wen, io.dec_waddr))

  val fp_sboard = new Scoreboard(32)
  fp_sboard.set(io.wb_valid, io.wb_waddr)
  fp_sboard.clear(io.resp_fpu, io.resp_waddr)
  
  io.stall_fpu := checkHazards(fp_hazard_targets, fp_sboard.read _)
}
