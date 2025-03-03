import chisel3._
import chisel3.util._
import cpu._
import cpu.defines._

// class PuaCpu extends Module {
//   val io = IO(new Bundle {
//     val ext_int   = Input(new ExtInterrupt())
//     val inst_sram = new InstSram()
//     val data_sram = new DataSram()
//     val debug     = new DEBUG()
//   })
//
//   val core = Module(new Core())
//
//   io.ext_int <> core.io.interrupt
//   io.inst_sram <> core.io.instSram
//   io.data_sram <> core.io.dataSram
//   io.debug <> core.io.debug
// }

class PuaCpu extends Module {
  val port = IO(new Bundle {
    val x = Input(UInt(8.W))
    val y = Input(UInt(8.W))
    val s = Output(Bool())
  })
  when (port.x>=port.y){
      port.s:=1.B
  }
  .otherwise{
    port.s:=0.B
  }
}
