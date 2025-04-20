package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig

class ExeMemData extends Bundle {
  val pc       = UInt(XLEN.W)
  val info     = new Info()
  val rd_info  = new RdInfo()
  val src_info = new SrcInfo()
}

class ExecuteUnitMemoryUnit extends Bundle {
  val data = new ExeMemData()
}

class MemoryStage extends Module {
  val io = IO(new Bundle {
    val executeUnit = Input(new ExecuteUnitMemoryUnit())
    val memoryUnit  = Output(new ExecuteUnitMemoryUnit())
    val executeUnit_ctrl = Flipped(new CtrlSignal())
  })

  val data = RegInit(0.U.asTypeOf(new ExeMemData()))
  data := io.executeUnit.data
  io.memoryUnit.data := Mux(io.executeUnit_ctrl.do_flush, 0.U.asTypeOf(new ExeMemData()), Mux(io.executeUnit_ctrl.allow_to_go, data, io.memoryUnit.data))
  io.memoryUnit.data.info.valid := Mux(io.executeUnit_ctrl.do_flush, false.B, data.info.valid && io.executeUnit_ctrl.allow_to_go)
}
