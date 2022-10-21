package gcd
import chisel3._
import chisel3.util._

object GenVerilog extends App {
    println("Generating Verilog...")
    emitVerilog(new GCD(), Array("--target-dir", "build"))
}
