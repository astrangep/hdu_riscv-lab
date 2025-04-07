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
 
   val ans = Wire(UInt(XLEN.W))
   val rs1 = io.src_info.src1_data
   val rs2 = io.src_info.src2_data
   ans := 0.U
   switch(io.info.op) {
     is(MDUOpType.mul) {
       ans := rs1 * rs2
     }
     is(MDUOpType.mulh) {
       val result128 = (rs1.asSInt * rs2.asSInt).asUInt
       ans := result128(127, 64)
     }
     is(MDUOpType.mulhsu) {
       val result128 = (rs1.asSInt * rs2.asUInt).asUInt
       ans := result128(127, 64)
     }
     is(MDUOpType.mulhu) {
       val result128 = rs1 * rs2
       ans := result128(127, 64)
     }
     is(MDUOpType.div) {
       val dividend = rs1.asSInt
       val divisor = rs2.asSInt
       val result = Mux(divisor === 0.S, (-1).S, dividend / divisor)
       ans := result.asUInt
     }
     is(MDUOpType.divu) {
       val result = Mux(
         rs2 === 0.U,
         Cat(Fill(64, 1.U)),
         rs1 / rs2
       )
       ans := result
     }
     is(MDUOpType.rem) {
       val dividend = rs1.asSInt
       val divisor = rs2.asSInt
       val q_result = Mux(divisor === 0.S, (-1).S, dividend / divisor)
 
       
 
       val dividend_abs = Mux(rs1(63), (~rs1).asSInt + 1.S, rs1.asSInt)
       val divisor_abs  = Mux(rs2(63), (~rs2).asSInt + 1.S, rs2.asSInt)
 
       val sign = rs1(63) ^ rs2(63)
 
       val result_abs = Mux(rs2 === 0.U, 
         (-1).S.asUInt, 
         (dividend_abs / divisor_abs).asUInt
       )
       val result_rem = Mux(sign, (~result_abs + 1.U), result_abs)
       val result = dividend.asSInt - result_rem.asSInt * divisor.asSInt
       //printf("dividend:%x, divisor:%x, result:%x, q_result:%x\n",dividend,divisor,result,result_rem)
 
       ans := result.asUInt
     }
     is(MDUOpType.remu) {
       val result = Mux(rs2 === 0.U, rs1, rs1 % rs2)
       ans := result
     }
     is(MDUOpType.mulw) {
       val result32 = (rs1(31,0).asSInt * rs2(31,0).asSInt).asUInt(31,0)
       ans := Cat(Fill(32, result32(31)), result32)
     }
     is(MDUOpType.divw) {
       val dividend_abs = Mux(rs1(31), (~rs1(31,0)).asSInt + 1.S, rs1(31,0).asSInt)
       val divisor_abs  = Mux(rs2(31), (~rs2(31,0)).asSInt + 1.S, rs2(31,0).asSInt)
       val sign = rs1(31) ^ rs2(31)
       val result32 = Mux(rs2(31,0) === 0.U, 
         (-1).S(31,0).asUInt, 
         (dividend_abs / divisor_abs).asUInt
       )
       ans := Mux(rs2(31,0) === 0.U, 
         (-1).S(63,0).asUInt,
         Cat(Fill(32, sign), Mux(sign, (~result32 + 1.U)(31,0), result32)(31,0))
       )
       when(result32 === 0.U){
         ans := Cat(Fill(32, 0.U), result32)
       }
       val test_re32_back = (~result32 + 1.U)(31,0)
       // printf("test_re32_back:%x\n",test_re32_back)
       // printf("divw_ dividend:%x divisor:%x result32:%x sign:%x\n",dividend_abs,divisor_abs,result32,sign)
     }
     is(MDUOpType.divuw) {
       val rs1_32_unsigned = rs1(31, 0).asUInt
       val rs2_32_unsigned = rs2(31, 0).asUInt
       val result32 = Mux(
         rs2_32_unsigned === 0.U,
         ~0.U(32.W),
         rs1_32_unsigned / rs2_32_unsigned
       )
       when(rs2_32_unsigned === 0.U){
         ans := Cat(Fill(32, 1.U), result32)
       }
       when(rs2_32_unsigned =/= 0.U){
         ans := Cat(Fill(32, result32(31)), result32)
       }
       
     }
     is(MDUOpType.remw) {
       val dividend = rs1(31, 0).asSInt
       val divisor = rs2(31, 0).asSInt
       val q = dividend / divisor
       val remainder = Mux(divisor === 0.S, dividend, dividend - divisor * q)
       ans := Cat(Fill(32, rs1(63)), remainder)
       // printf("q: %x\n",q)
       // printf("remw_ dividend:%x,divisor:%x,remainder:%x,ans:%x\n",dividend,divisor,remainder,ans)
     }
     is(MDUOpType.remuw) {
       val result32 = Mux(rs2(31,0) === 0.U, rs1(31,0), rs1(31,0) % rs2(31,0))
       ans := Cat(Fill(32, result32(31)), result32)
     }
   }
   io.result := ans
 }