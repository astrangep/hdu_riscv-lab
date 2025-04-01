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
  val src1_32 = src1(31, 0)
  val src2_32 = src2(31, 0)
  val src2_shift64 = src2(5, 0)
  val src2_shift32 = src2(4, 0)
  val op = io.info.op
  
  val add_res  = src1 + src2
  val sub_res  = src1 - src2
  val sll_res  = src1 << src2_shift64
  val slt_res  = (src1.asSInt < src2.asSInt).asUInt
  val sltu_res = (src1 < src2).asUInt
  val xor_res  = src1 ^ src2
  val srl_res  = src1 >> src2_shift64
  val sra_res  = (src1.asSInt >> src2_shift64).asUInt
  val or_res   = src1 | src2
  val and_res  = src1 & src2
  val addw_res = signExtend(src1_32 + src2_32, 64)
  val subw_res = signExtend(src1_32 - src2_32, 64)
  val sllw_res = signExtend((src1_32 << src2_32(4, 0))(31, 0), 64)
  val srlw_res = signExtend((src1_32 >> src2_32(4, 0))(31, 0), 64)
  val sraw_res = signExtend((src1_32.asSInt >> src2_32(4, 0))(31, 0), 64)
  io.result := MuxLookup(op, 0.U)(
    Seq(
      ALUOpType.add  -> add_res,
      ALUOpType.sub  -> sub_res,
      ALUOpType.sll  -> sll_res,
      ALUOpType.slt  -> slt_res,
      ALUOpType.sltu -> sltu_res,
      ALUOpType.xor  -> xor_res,
      ALUOpType.srl  -> srl_res,
      ALUOpType.sra  -> sra_res,
      ALUOpType.or   -> or_res,
      ALUOpType.and  -> and_res,
      ALUOpType.addw -> addw_res,
      ALUOpType.subw -> subw_res,
      ALUOpType.sllw -> sllw_res,
      ALUOpType.srlw -> srlw_res,
      ALUOpType.sraw -> sraw_res
    )
  )
}
