package cpu.defines

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig

class ExtInterrupt extends Bundle {
  val mei = Bool()
  val mti = Bool()
  val msi = Bool()
}

class SrcInfo extends Bundle {
  val src1_data = UInt(XLEN.W)
  val src2_data = UInt(XLEN.W)
}

class RdInfo extends Bundle {
  val wdata = UInt(XLEN.W)
}

class Info extends Bundle {
  val valid      = Bool()
  val src1_raddr = UInt(REG_ADDR_WID.W)
  val src2_raddr = UInt(REG_ADDR_WID.W)
  val op         = FuOpType()
  val reg_wen    = Bool()
  val reg_waddr  = UInt(REG_ADDR_WID.W)
  val imm = Output(UInt(XLEN.W))
  val src1_ren = Output(Bool())
  val src2_ren = Output(Bool())
  val is_lui = Output(Bool())
  val fusel = FuType()
}

class SrcReadSignal extends Bundle {
  val ren   = Bool()
  val raddr = UInt(REG_ADDR_WID.W)
}

class InstSram extends Bundle {
  val en    = Output(Bool())
  val addr  = Output(UInt(SRAM_ADDR_WID.W))
  val wdata = Output(UInt(INST_SRAM_DATA_WID.W))
  val wen   = Output(UInt(INST_SRAM_WEN_WID.W))
  val rdata = Input(UInt(INST_SRAM_DATA_WID.W))
}

class DataSram extends Bundle {
  val en    = Output(Bool())
  val addr  = Output(UInt(SRAM_ADDR_WID.W))
  val wdata = Output(UInt(DATA_SRAM_DATA_WID.W))
  val wen   = Output(UInt(DATA_SRAM_WEN_WID.W))
  val rdata = Input(UInt(DATA_SRAM_DATA_WID.W))
}

class DEBUG extends Bundle {
  val commit   = Output(Bool()) // 写回阶段的commit信号，仅在每条指令提交时置为true
  val pc       = Output(UInt(XLEN.W)) // 写回阶段的pc
  val rf_wnum  = Output(UInt(REG_ADDR_WID.W)) // 写回阶段的寄存器写地址
  val rf_wdata = Output(UInt(XLEN.W)) // 写回阶段的寄存器写数据
}
