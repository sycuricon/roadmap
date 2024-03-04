package roadmap

import chisel3._
import chisel3.util._
import chisel3.experimental._


class QueueDemo() extends Module {
    val io = IO(new Bundle {
        val provider = Input(UInt(3.W))
    })
    
    val q = Seq(
        Module(new Queue(UInt(4.W), 1, pipe=false, flow=false)),
        Module(new Queue(UInt(4.W), 1, pipe=false, flow=true)),
        Module(new Queue(UInt(4.W), 1, pipe=true, flow=false)),
        Module(new Queue(UInt(4.W), 1, pipe=true, flow=true))
    )

    for (i <- 0 until 4) {
        q(i).io.enq := DontCare
        dontTouch(q(i).io.enq)

        q(i).io.deq := DontCare
        dontTouch(q(i).io.deq)

    }
}
