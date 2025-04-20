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

  data.info := Mux(io.decodeUnit_ctrl.do_flush, 0.U.asTypeOf(new Info()), Mux(io.decodeUnit_ctrl.allow_to_go, io.decodeUnit.data.info, data.info))
  data.pc := Mux(io.decodeUnit_ctrl.do_flush, 0.U, Mux(io.decodeUnit_ctrl.allow_to_go, io.decodeUnit.data.pc, data.pc))
  data.src_info := Mux(io.decodeUnit_ctrl.do_flush, 0.U.asTypeOf(new SrcInfo()), Mux(io.decodeUnit_ctrl.allow_to_go, io.decodeUnit.data.src_info, data.src_info))
  data.info.valid := Mux(io.decodeUnit_ctrl.do_flush, false.B, io.decodeUnit.data.info.valid)
  io.executeUnit.data := data
  io.executeUnit.data.info.valid := data.info.valid && io.decodeUnit_ctrl.allow_to_go
}
