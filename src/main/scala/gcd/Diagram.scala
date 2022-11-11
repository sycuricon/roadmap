package gcd

import chisel3.stage.ChiselStage
import dotvisualizer.stage.{DiagrammerStage, OpenCommandAnnotation}
import firrtl.stage.FirrtlSourceAnnotation

object GcdDiagramExample {
  def main(args: Array[String]): Unit = {
    (new DiagrammerStage).execute(
      Array(
        "--target-dir", "build/dg"
      ),
      Seq(
        FirrtlSourceAnnotation(ChiselStage.emitFirrtl(new GCD)),
        OpenCommandAnnotation("none")
      ))
  }
}