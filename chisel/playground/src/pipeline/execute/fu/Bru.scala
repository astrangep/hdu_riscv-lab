package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.defines.BRUOpType.isBranch
import cpu.defines.BRUOpType.isJump
class Bru extends Module with HasExceptionNO{
    val io = IO(new Bundle {
        val exc_info = Input(new ExceptionInfo())
        val info = Input(new Info())
        val src_info = Input(new SrcInfo)
        val pc = Input(UInt(XLEN.W))
        val result = Output(UInt(XLEN.W))
        val branch = Output(Bool())
        val target = Output(UInt(XLEN.W))
        val ex     = Output(new ExceptionInfo())
    })
    val op = io.info.op
    val src1_data = io.src_info.src1_data
    val src2_data = io.src_info.src2_data
    val imm = io.info.imm
    val pc = io.pc
    val branch_bool = MuxLookup(op, false.B)(Seq(
        BRUOpType.beq -> (src1_data === src2_data),
        BRUOpType.bne -> !(src1_data === src2_data),
        BRUOpType.blt -> (src1_data.asSInt < src2_data.asSInt),
        BRUOpType.bge -> (src1_data.asSInt >= src2_data.asSInt),
        BRUOpType.bltu -> (src1_data < src2_data),
        BRUOpType.bgeu -> (src1_data >= src2_data)
    ))
    io.ex.exception := io.exc_info.exception.map(e => e)
    io.ex.interrupt := io.exc_info.interrupt.map(i => i)
    io.ex.tval      := io.exc_info.tval.map(t => t)
    val is_branch = isBranch(op)
    val is_jump = isJump(op)
    val branch = io.info.valid && (io.info.fusel === FuType.bru) && ((is_branch && branch_bool)| is_jump)
    val target = Mux(io.info.op === BRUOpType.jalr, (src1_data + src2_data) & (~1.U(XLEN.W)), pc + imm)
    val target_misaligned = target(1, 0) =/= 0.U
    val exc_before = io.exc_info.exception.asUInt.orR 
    val has_exc = branch && target_misaligned && !exc_before
    when(has_exc){
        io.ex.exception(instAddrMisaligned) := true.B
        io.ex.tval(instAddrMisaligned) := target
    }
    io.result := pc + 4.U
    io.branch := branch
    io.target := target
}
