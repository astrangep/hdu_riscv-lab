package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig

class IfIdData extends Bundle {
  val inst  = UInt(XLEN.W)
  val valid = Bool()
  val pc    = UInt(XLEN.W)
}

class  FetchUnitDecodeUnit extends Bundle {
  val data = Output(new IfIdData())
}

class DecodeStage extends Module {
  val io = IO(new Bundle {
    val fetchUnit  = Flipped(new FetchUnitDecodeUnit())
    val decodeUnit = new FetchUnitDecodeUnit()
    val fetchUnit_ctrl = Flipped(new CtrlSignal())
  })

  val data = RegInit(0.U.asTypeOf(new IfIdData()))

  data.inst := Mux(io.fetchUnit_ctrl.do_flush, 0.U, Mux(io.fetchUnit_ctrl.allow_to_go, io.fetchUnit.data.inst, data.inst))
  data.pc := Mux(io.fetchUnit_ctrl.do_flush, 0.U, Mux(io.fetchUnit_ctrl.allow_to_go, io.fetchUnit.data.pc, data.pc))
  data.valid := Mux(io.fetchUnit_ctrl.do_flush, false.B, io.fetchUnit.data.valid && io.fetchUnit_ctrl.allow_to_go)
  io.decodeUnit.data := data
}
