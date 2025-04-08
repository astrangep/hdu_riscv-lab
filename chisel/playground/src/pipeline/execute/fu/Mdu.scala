package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.defines.MDUOpType.isWordOp
class Mdu extends Module {
  val io = IO(new Bundle {
    val info     = Input(new Info())
    val src_info = Input(new SrcInfo())
    val result   = Output(UInt(XLEN.W))
  })

  val rs1 = io.src_info.src1_data
  val rs2 = io.src_info.src2_data
  val rs1_32 = rs1(31,0)
  val rs2_32 = rs2(31,0)
  val is_w =isWordOp(io.info.op) 
  val mul_signed   = (rs1.asSInt * rs2.asSInt).asUInt
  val mul_signed_u = (rs1.asSInt * rs2.asUInt).asUInt
  val mul_unsigned = rs1 * rs2
  val (div_signed, rem_signed) = {
    val q = Mux(rs2 === 0.U, (-1).S, rs1.asSInt / rs2.asSInt)
    val r = rs1.asSInt - q * rs2.asSInt
    (q.asUInt, r.asUInt)
  }
  val (div_unsigned, rem_unsigned) = {
    val q = Mux(rs2 === 0.U, ~0.U(XLEN.W), rs1 / rs2)
    (q, rs1 - q * rs2)
  }
  val (divw_signed, remw_signed) ={
    val q = Mux(rs2_32 === 0.U, (-1).S, rs1_32.asSInt / rs2_32.asSInt)
    val r = Mux(rs2_32 === 0.U, rs1_32.asSInt, rs1_32.asSInt - (q * rs2_32.asSInt)(31,0).asSInt) 
    (q.asUInt, r.asUInt)
  }
  val (divw_unsigned, remw_unsigned) = {
    val q = Mux(rs2_32 === 0.U, ~0.U(32.W), rs1_32 / rs2_32)
    val r = Mux(rs2_32 === 0.U, rs1_32, rs1_32 - (q * rs2_32)(31,0))
    (q, r)
  }
  io.result := MuxLookup(io.info.op, 0.U)(Seq(
    MDUOpType.mul    -> mul_signed(XLEN-1, 0),
    MDUOpType.mulh   -> mul_signed(2*XLEN-1, XLEN),
    MDUOpType.mulhsu -> mul_signed_u(2*XLEN-1, XLEN),
    MDUOpType.mulhu  -> mul_unsigned(2*XLEN-1, XLEN),
    MDUOpType.div    -> div_signed,
    MDUOpType.divu   -> div_unsigned,
    MDUOpType.rem    -> rem_signed,
    MDUOpType.remu   -> rem_unsigned,
    MDUOpType.mulw   -> SignedExtend(mul_signed(31,0),XLEN),
    MDUOpType.divw   -> SignedExtend(divw_signed(31,0),XLEN),
    MDUOpType.divuw  -> SignedExtend(divw_unsigned,XLEN),
    MDUOpType.remw   -> SignedExtend(remw_signed(31,0),XLEN),
    MDUOpType.remuw  -> SignedExtend(remw_unsigned,XLEN)
  )
  )
}