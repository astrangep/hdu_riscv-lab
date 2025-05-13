package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines.Const._
import cpu.CpuConfig
import cpu.defines._

class FetchUnit extends Module {
  val io = IO(new Bundle {
    val decodeStage    = new FetchUnitDecodeUnit()
    val instSram       = new InstSram()
    val branch         = Input(Bool())
    val target         = Input(UInt(XLEN.W))
    val fetchUnit_ctrl = Flipped(new CtrlSignal())
  })

  val boot :: send :: receive :: Nil = Enum(3)
  val state                          = RegInit(boot)

  switch(state) {
    is(boot) {
      state := send
    }
    is(send) {
      state := receive
    }
    is(receive) {}
  }

  // 取指阶段完成指令的取指操作

  val pc = RegEnable(io.instSram.addr, (PC_INIT - 4.U), state =/= boot)
  val pc_next = Mux(io.branch, io.fetchUnit_ctrl.do_flush, Mux(io.fetchUnit_ctrl.allow_to_go, pc + 4.U, pc))
  val is_align = pc_next(1,0) === "b'00".U
  io.instSram.addr := pc_next

  io.decodeStage.data.valid := state === receive
  io.decodeStage.data.pc    := pc
  io.decodeStage.data.inst  := Mux(is_align, io.instSram.rdata,"h'00000013".U )

  io.instSram.en    := !reset.asBool && is_align
  io.instSram.wen   := 0.U
  io.instSram.wdata := 0.U
}
