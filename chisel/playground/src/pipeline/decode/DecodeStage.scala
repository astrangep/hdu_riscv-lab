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
  data := Mux(io.fetchUnit_ctrl.do_flush, 0.U.asTypeOf(new IfIdData()), Mux(io.fetchUnit_ctrl.allow_to_go, io.fetchUnit.data, data))
  data.valid := !io.fetchUnit_ctrl.do_flush && io.fetchUnit.data.valid && io.fetchUnit_ctrl.allow_to_go
  io.decodeUnit.data := data
}
