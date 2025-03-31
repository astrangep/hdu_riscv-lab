package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class Alu extends Module {
  val io = IO(new Bundle {
    val info     = Input(new Info())
    val src_info = Input(new SrcInfo())
    val result   = Output(UInt(XLEN.W))
  })

  def signExtend(data: UInt, len: Int): UInt = {
  val signBit = data(data.getWidth - 1) 
  Cat(Fill(len - data.getWidth, signBit), data)
  }

  val src1 = io.src_info.src1_data
  val src2 = io.src_info.src2_data
  val src1_32 = src1(31,0)
  val src2_32 = src2(31,0)
  val src2_shift64 = src2(5,0)
  val src2_shift32 = src2(4,0)
  val op = io.info.op
  
  val add_res  = src1 + src2
  val sub_res  = src1 - src2
  val sll_res  = src1 << src2_shift64
  val slt_res  = (src1.asSInt() < src2.asSInt()).asUInt()
  val sltu_res = (src1 < src2).asUInt()
  val xor_res  = src1 ^ src2
  val srl_res  = src1 >> src2_shift64
  val sra_res  = (src1.asSInt() >> src2_shift64).asUInt()
  

}
