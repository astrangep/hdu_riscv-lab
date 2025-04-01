package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig

class WriteBackUnit extends Module {
  val io = IO(new Bundle {
    val writeBackStage = Input(new MemoryUnitWriteBackUnit())
    val regfile        = Output(new RegWrite())
    val debug          = new DEBUG()
  })
  //调试接口
  io.debug.pc := io.writeBackStage.data.pc
  io.debug.rf_wdata := io.writeBackStage.data.rd_info.wdata
  io.debug.rf_wnum := io.writeBackStage.data.info.reg_waddr
  io.debug.commit := io.writeBackStage.data.info.valid 
  //寄存器接口
  io.regfile.wen := io.writeBackStage.data.info.valid & io.writeBackStage.data.info.reg_wen
  io.regfile.waddr := io.writeBackStage.data.info.reg_waddr
  io.regfile.wdata := io.writeBackStage.data.rd_info.wdata
}
