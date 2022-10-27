package roadmap

import chisel3._
import chisel3.experimental.BundleLiterals._

class MyBundle(w: Int) extends Bundle {
  val foo = UInt(w.W)
  val bar = UInt(w.W)
}

class MyModule(gen: () => MyBundle) extends Module {
                                                            //   Hardware   Literal
    val xType:    MyBundle     = new MyBundle(3)            //      -          -
    val dirXType: MyBundle     = Input(new MyBundle(3))     //      -          -
    val xReg:     MyBundle     = Reg(new MyBundle(3))       //      x          -
    val xIO:      MyBundle     = IO(Input(new MyBundle(3))) //      x          -
    val xRegInit: MyBundle     = RegInit(xIO)               //      x          -
    val xLit:     MyBundle     = xType.Lit(                 //      x          x 
      _.foo -> 0.U(3.W), 
      _.bar -> 0.U(3.W)
    )
    val y:        MyBundle = gen()                          //      ?          ?
    
    // Need to initialize all hardware values
    xReg := DontCare
}
