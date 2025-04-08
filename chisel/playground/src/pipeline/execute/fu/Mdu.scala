package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
class Mdu extends Module {
  val io = IO(new Bundle {
    val info     = Input(new Info())
    val src_info = Input(new SrcInfo())
    val result   = Output(UInt(XLEN.W))
  })

  val rs1 = io.src_info.src1_data
  val rs2 = io.src_info.src2_data
  val is_w = io.info.is_w  // 假设info中已定义W后缀标志

  // ============= 共用计算逻辑 =============
  // 1. 乘法结果（所有变种）
  val mul_signed   = (rs1.asSInt * rs2.asSInt).asUInt
  val mul_signed_u = (rs1.asSInt * rs2.asUInt).asUInt
  val mul_unsigned = rs1 * rs2

  // 2. 除法/取余结果（共用中间值）
  val (div_signed, rem_signed) = {
    val q = Mux(rs2 === 0.U, (-1).S, rs1.asSInt / rs2.asSInt)
    val r = rs1.asSInt - q * rs2.asSInt
    (q.asUInt, r.asUInt)
  }
  val (div_unsigned, rem_unsigned) = {
    val q = Mux(rs2 === 0.U, ~0.U, rs1 / rs2)
    (q, rs1 - q * rs2)
  }

  // ============= 结果选择 =============
  io.result := MuxLookup(io.info.op, 0.U, Seq(
    // 乘法指令
    MDUOpType.mul    -> mul_signed(XLEN-1, 0),
    MDUOpType.mulh   -> mul_signed(2*XLEN-1, XLEN),
    MDUOpType.mulhsu -> mul_signed_u(2*XLEN-1, XLEN),
    MDUOpType.mulhu  -> mul_unsigned(2*XLEN-1, XLEN),
    
    // 除法指令
    MDUOpType.div    -> div_signed,
    MDUOpType.divu   -> div_unsigned,
    
    // 取余指令
    MDUOpType.rem    -> rem_signed,
    MDUOpType.remu   -> rem_unsigned,
    
    // W后缀指令（RV64）
    MDUOpType.mulw   -> SignedExtend(mul_signed(31, 0),XLEN)
    MDUOpType.divw   -> SignedExtend(div_signed(31, 0),XLEN)
    MDUOpType.divuw  -> ZeroExtend(div_unsigned(31, 0),XLEN)
    MDUOpType.remw   -> SignedExtend(rem_signed(31, 0).XLEN)
    MDUOpType.remuw  -> ZeroExtend(rem_unsigned(31, 0),XLEN)
  ))
}