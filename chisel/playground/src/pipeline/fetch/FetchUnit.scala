package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines.Const._
import cpu.CpuConfig
import cpu.defines._

class FetchUnit extends Module {
  val io = IO(new Bundle {
    val decodeStage = new FetchUnitDecodeUnit()
    val instSram    = new InstSram()
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

  io.instSram.addr := pc + 4.U

  io.decodeStage.data.valid := state === receive
  io.decodeStage.data.pc    := pc
  io.decodeStage.data.inst  := io.instSram.rdata

  io.instSram.en    := !reset.asBool
  io.instSram.wen   := 0.U
  io.instSram.wdata := 0.U
}
