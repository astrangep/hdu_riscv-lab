package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig

class IdExeData extends Bundle {
  val pc       = UInt(XLEN.W)
  val info     = new Info()
  val src_info = new SrcInfo()
}

class DecodeUnitExecuteUnit extends Bundle {
  val data = new IdExeData()
}

class ExecuteStage extends Module {
  val io = IO(new Bundle {
    val decodeUnit  = Input(new DecodeUnitExecuteUnit())
    val executeUnit = Output(new DecodeUnitExecuteUnit())
    val decodeUnit_ctrl = Flipped(new CtrlSignal())
  })

  val data = RegInit(0.U.asTypeOf(new IdExeData()))
  data := io.decodeUnit.data
  io.executeUnit.data := Mux(io.decodeUnit_ctrl.do_flush, 0.U.asTypeOf(new IdExeData()), Mux(io.decodeUnit_ctrl.allow_to_go, data, io.executeUnit.data))
  io.executeUnit.data.info.valid := Mux(io.decodeUnit_ctrl.do_flush, false.B, data.info.valid && io.decodeUnit_ctrl.allow_to_go)
}
