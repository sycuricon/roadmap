// See README.md for license details.

package gcd

import chisel3._

/**
  * Compute GCD using subtraction method.
  * Subtracts the smaller from the larger until register y is zero.
  * value in register x is then the GCD
  */
class GCD extends Module {
  val io = IO(new Bundle {
    val value1        = Input(UInt(16.W))
    val value2        = Input(UInt(16.W))
    val loadingValues = Input(Bool())
    val outputGCD     = Output(UInt(16.W))
    val outputValid   = Output(Bool())
  })

  val x  = Reg(UInt())
  val y  = Reg(UInt())
  val valid = Wire(Bool())

  when(x > y) { x := x - y }
    .otherwise { y := y - x }

  valid := Mux(io.loadingValues === true.B, true.B, false.B)

  when(valid) {
    x := io.value1
    y := io.value2
  }

  io.outputGCD := x
  io.outputValid := y === 0.U
}

object GenGCD extends App {
    println("Elaborating GCD Moudle ...")
    emitVerilog(new GCD(), Array("--target-dir", "build"))
}
