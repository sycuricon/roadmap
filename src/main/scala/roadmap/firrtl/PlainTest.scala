package roadmap.firrtl

import firrtl._
import firrtl.ir._
import firrtl.passes._

object CircuitCases {
  val case1 =
    """
      |circuit PullMuxesTest :
      |  module PullMuxesTest :
      |    input clock : Clock
      |    input reset : UInt<1>
      |    output io : { flip en : UInt<1>, flip in1 : UInt<64>[2], flip in2 : UInt<64>[2], res : UInt<64>}
      |
      |    node _io_res_T = mux(io.en, io.in1, io.in2) @[FormTest.scala 23:16]
      |    io.res <= _io_res_T[0] @[FormTest.scala 23:10]
      |""".stripMargin
}

object PlainTest extends App {
  val passes = Seq(PullMuxes)
  val input = CircuitCases.case1

  val result = passes.foldLeft(Parser.parse(input)) { (c: Circuit, p: Pass) =>
    p.run(c)
  }
  println(result.serialize)
}
