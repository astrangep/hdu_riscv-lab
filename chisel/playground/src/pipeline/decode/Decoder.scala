package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class Decoder extends Module with HasInstrType {
  val io = IO(new Bundle {
    // inputs
    val in = Input(new Bundle {
      val inst = UInt(XLEN.W)
    })
    // outputs
    val out = Output(new Bundle {
      val info = new Info()
    })
  })
  val inst = io.in.inst
  // 根据输入的指令inst从Instructions.DecodeTable中查找对应的指令类型、功能单元类型和功能单元操作类型
  // 如果找不到匹配的指令，则使用Instructions.DecodeDefault作为默认值
  val instrType :: fuType :: fuOpType :: Nil =
    ListLookup(inst, Instructions.DecodeDefault, Instructions.DecodeTable)
  val imm_i = SignedExtend(inst(31, 20), 64)
  val imm_u = SignedExtend(Cat(inst(31, 12), 0.U(12.W)), 64)
  val imm_s = SignedExtend(Cat(inst(31, 25), inst(11, 7)), 64)

  val (rs, rt, rd) = (inst(19, 15), inst(24, 20), inst(11, 7))
  io.out.info.inst := inst
  io.out.info.valid := false.B
  io.out.info.src1_raddr := rs
  io.out.info.src2_raddr := rt
  io.out.info.op := fuOpType
  io.out.info.fusel := fuType
  io.out.info.reg_wen := isRegWen(instrType)
  io.out.info.reg_waddr := rd
  io.out.info.is_lui := inst(6, 0) === "b0110111".U
  io.out.info.imm := MuxLookup(instrType, 0.U)(Seq(InstrI -> imm_i, InstrU -> imm_u, InstrS -> imm_s))
  io.out.info.src1_ren := instrType === InstrR | instrType === InstrI | instrType === InstrS
  io.out.info.src2_ren := instrType === InstrR | instrType === InstrS
}
