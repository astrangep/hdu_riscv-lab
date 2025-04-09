package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig
class LsuMem extends Module {
   val io = IO(new Bundle {
     val info     = Input(new Info())
     val src_info = Input(new SrcInfo())
     val rdata    = Input(UInt(XLEN.W))
     val result   = Output(UInt(XLEN.W))
   })

   val op = io.info.op
   val addr = io.src_info.src1_data + io.info.imm
   val addr_low = addr(2,0)
   val r_data = io.rdata
   val rdata_shifted = Wire(UInt(XLEN.W))
   rdata_shifted := (r_data >> (addr_low << 3))
 
   val load_result = MuxLookup(op, 0.U)(Seq(
     LSUOpType.lb  -> SignedExtend(rdata_shifted(7, 0), XLEN),
     LSUOpType.lh  -> SignedExtend(rdata_shifted(15, 0), XLEN),
     LSUOpType.lw  -> SignedExtend(rdata_shifted(31, 0), XLEN),
     LSUOpType.ld  -> rdata_shifted,
     LSUOpType.lbu -> ZeroExtend(rdata_shifted(7, 0), XLEN),
     LSUOpType.lhu -> ZeroExtend(rdata_shifted(15, 0), XLEN),
     LSUOpType.lwu -> ZeroExtend(rdata_shifted(31, 0), XLEN)
   ))
 
   io.result := load_result
 }

