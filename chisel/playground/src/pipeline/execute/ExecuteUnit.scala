package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.CpuConfig
import cpu.defines._
import cpu.defines.Const._
import chisel3.util.experimental.BoringUtils

class ExecuteUnit extends Module {
  val io = IO(new Bundle {
    val interrupt_info = Input(new ExtInterrupt())
    val executeStage = Input(new DecodeUnitExecuteUnit())
    val memoryStage  = Output(new ExecuteUnitMemoryUnit())
    val dataSram     = new DataSram()
    val flush       = Output(Bool())
    val target       = Output(UInt(XLEN.W))
    val mode = Output(UInt(2.W))
    val interrupt = Output(new ExtInterrupt())
    val has_exc = Output(Bool())
  })

  // 执行阶段完成指令的执行操作

  val fu = Module(new Fu()).io
  fu.data.exc_info  := io.executeStage.data.ex
  fu.data.pc        := io.executeStage.data.pc
  fu.data.info      := io.executeStage.data.info
  fu.data.src_info  := io.executeStage.data.src_info
  fu.interrupt_info := io.interrupt_info
  
  io.memoryStage.data.has_exc := fu.has_exc
  io.dataSram <> fu.dataSram
  io.mode := fu.mode
  io.interrupt := fu.interrupt
  io.flush := fu.flush
  io.target := fu.target
  io.memoryStage.data.pc       := fu.data.pc
  io.memoryStage.data.info     := fu.data.info
  io.memoryStage.data.src_info := fu.data.src_info
  io.memoryStage.data.rd_info  := fu.data.rd_info
  io.has_exc := fu.has_exc
}
