package cpu.pipeline

import chisel3._
import chisel3.util._
import chisel3.util.experimental.BoringUtils
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig

class MemoryUnit extends Module {
  val io = IO(new Bundle {
    val memoryStage    = Input(new ExecuteUnitMemoryUnit())
    val writeBackStage = Output(new MemoryUnitWriteBackUnit())
    val rdata = Input(UInt(XLEN.W))
  })
  val lsu_mem = Module(new LsuMem()).io
  lsu_mem.info     := io.memoryStage.data.info
  lsu_mem.rdata := io.rdata
  lsu_mem.src_info := io.memoryStage.data.src_info
  io.writeBackStage.data.pc                        := io.memoryStage.data.pc
  io.writeBackStage.data.info                      := lsu_mem.info
  io.writeBackStage.data.rd_info.wdata             := Mux(lsu_mem.info.fusel === FuType.lsu, lsu_mem.result, io.memoryStage.data.rd_info.wdata)
}
