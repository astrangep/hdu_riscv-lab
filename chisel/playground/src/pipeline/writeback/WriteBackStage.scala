package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig

class MemWbData extends Bundle {
  val pc      = UInt(XLEN.W)
  val info    = new Info()
  val rd_info = new RdInfo()
}

class MemoryUnitWriteBackUnit extends Bundle {
  val data = new MemWbData()
}
class WriteBackStage extends Module {
  val io = IO(new Bundle {
    val memoryUnit    = Input(new MemoryUnitWriteBackUnit())
    val writeBackUnit = Output(new MemoryUnitWriteBackUnit())
    val memoryUnit_ctrl = Flipped(new CtrlSignal())
  })

  val data = RegInit(0.U.asTypeOf(new MemWbData()))
  
  data := io.memoryUnit.data
  data.info.valid := Mux(io.memoryUnit_ctrl.do_flush, false.B, io.memoryUnit.data.info.valid && io.memoryUnit_ctrl.allow_to_go)
  
  io.writeBackUnit.data := data
}
