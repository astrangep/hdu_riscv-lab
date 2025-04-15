package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.defines.BRUOpType.isBranch
import cpu.defines.BRUOpType.isJump
class Bru extends Module{
    val io = IO(new Bundle {
        val info = Input(new Info())
        val src_info = Input(new SrcInfo)
        val pc = Input(UInt(XLEN.W))
        val result = Output(UInt(XLEN.W))
        val branch = Output(Bool())
        val target = Output(UInt(XLEN.W))
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
    val is_branch = isBranch(op)
    val is_jump = isJump(op)
    io.result := pc + 4.U
    io.branch := io.info.valid && (io.info.fusel === FuType.bru) && ((is_branch && branch_bool)| is_jump)
    io.target := Mux(io.info.op === BRUOpType.jalr, (src1_data + imm) & (~1.U(XLEN.W)), pc + imm)
}
