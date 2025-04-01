package cpu.defines

import chisel3._
import chisel3.util._

trait HasInstrType {
  def InstrN = "b000".U
  def InstrI = "b100".U
  def InstrR = "b101".U
  def InstrS = "b010".U
  def InstrB = "b001".U
  def InstrU = "b110".U
  def InstrJ = "b111".U

  def isRegWen(instrType: UInt): Bool = instrType(2)
}

object FuType {
  def num     = 1
  def alu     = 0.U // arithmetic logic unit
  def apply() = UInt(log2Up(num).W)
}

object FuOpType {
  def apply() = UInt(5.W)
}

// ALU
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
