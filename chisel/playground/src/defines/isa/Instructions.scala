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
  // TODO: 定义更多的ALU操作类型
}
