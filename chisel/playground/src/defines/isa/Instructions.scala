package cpu.defines

import chisel3._
import chisel3.util._

// 指令类型
trait HasInstrType {
  def InstrN = "b000".U
  def InstrI = "b100".U
  def InstrR = "b101".U
  def InstrS = "b010".U
  def InstrB = "b001".U
  def InstrU = "b110".U
  def InstrJ = "b111".U

  // I、R、U、J类型的指令都需要写寄存器
  def isRegWen(instrType: UInt): Bool = instrType(2)
}

// 功能单元类型 Function Unit Type
object FuType {
  def num     = 4
  def alu     = 0.U // arithmetic logic unit
  def mdu     = 1.U
  def lsu     = 2.U
  def bru     = 3.U
  def apply() = UInt(log2Up(num).W)
}

// 功能单元操作类型 Function Unit Operation Type
object FuOpType {
  def apply() = UInt(5.W) // 宽度与最大的功能单元操作类型宽度一致
}

// 算术逻辑单元操作类型 Arithmetic Logic Unit Operation Type
object ALUOpType {
  def add  = "b00000".U
  def sub  = "b01000".U
  def sll  = "b00001".U
  def slt  = "b00010".U
  def sltu = "b00011".U
  def xor  = "b00100".U
  def srl  = "b00101".U
  def sra  = "b01101".U
  def or   = "b00110".U
  def and  = "b00111".U
  def addw = "b10000".U
  def subw = "b11000".U
  def sllw = "b10001".U
  def srlw = "b10101".U
  def sraw = "b11101".U
}
//MDU
object MDUOpType{
  def mul    = "b0000".U
  def mulh   = "b0001".U
  def mulhsu = "b0010".U
  def mulhu  = "b0011".U
  def div    = "b0100".U
  def divu   = "b0101".U
  def rem    = "b0110".U
  def remu   = "b0111".U
  def mulw   = "b1000".U
  def divw   = "b1100".U
  def divuw  = "b1101".U
  def remw   = "b1110".U
  def remuw  = "b1111".U

  def isDiv(op: UInt) = op(2)
  def isDivSign(op: UInt) = isDiv(op) && !op(0)
  def isWordOp(op: UInt) = op(3)
}
object LSUOpType{
  def lb  = "b0000".U
  def lh  = "b0001".U
  def lw  = "b0010".U
  def ld  = "b0011".U
  def lbu = "b0100".U
  def lhu = "b0101".U
  def lwu = "b0110".U
  
  def sb  = "b1000".U
  def sh  = "b1001".U
  def sw  = "b1010".U
  def sd  = "b1011".U
  def isStore(func: UInt) : Bool = func(3)
  def isLoad(func : UInt) : Bool = !isStore(func)
}
object  BRUOpType{
  def jal  = "b1000".U
  def jalr = "b1010".U
  def beq  = "b0000".U
  def bne  = "b0001".U
  def blt  = "b0100".U
  def bge  = "b0101".U
  def bltu = "b0110".U
  def bgeu = "b0111".U
  def isBranch(func: UInt) = !func(3)
  def isJump(func: UInt) = !isBranch(func)
}