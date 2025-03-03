package cpu.defines

import chisel3._
import chisel3.util._
import cpu.CpuConfig

trait CoreParameter {
  def cpuConfig = new CpuConfig
  val XLEN      = if (cpuConfig.isRV32) 32 else 64
  val VADDR_WID = if (cpuConfig.isRV32) 32 else 39
  val PADDR_WID = 32
}

trait Constants extends CoreParameter {
  // 全局
  val PC_INIT = "h80000000".U(XLEN.W)

  val INT_WID = 12
  val EXC_WID = 16

  // inst rom
  val INST_WID = 32

  // GPR RegFile
  val AREG_NUM     = 32
  val REG_ADDR_WID = 5
}

trait SRAMConst extends Constants {
  val SRAM_ADDR_WID      = PADDR_WID // 32
  val DATA_SRAM_DATA_WID = XLEN
  val DATA_SRAM_WEN_WID  = XLEN / 8
  val INST_SRAM_DATA_WID = INST_WID
  val INST_SRAM_WEN_WID  = INST_WID / 8
}
object Const extends Constants with SRAMConst

object Instructions extends HasInstrType with CoreParameter {
  def NOP           = 0x00000013.U
  val DecodeDefault = List(InstrN, FuType.alu, ALUOpType.add)
  def DecodeTable   = RVIInstr.table
}
