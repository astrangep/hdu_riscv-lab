package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class DecodeUnit extends Module {
  val io = IO(new Bundle {
    // 输入
    val decodeStage = Flipped(new FetchUnitDecodeUnit())
    val regfile     = new Src12Read()
    // 输出
    val executeStage = Output(new DecodeUnitExecuteUnit())
  })

  val decoder = Module(new Decoder()).io
  decoder.in.inst := io.decodeStage.data.inst

  val pc   = io.decodeStage.data.pc
  val info = Wire(new Info())

  info       := decoder.out.info
  info.valid := io.decodeStage.data.valid

  //完成寄存器堆的读取
  io.regfile.src1.raddr := info.src1_raddr 
  io.regfile.src2.raddr := info.src2_raddr    

  val src1_data = Mux(decoder.out.src1_ren, io.regfile.src1.rdata, Mux(decoder.out.is_lui, 0.U, pc))
  val src2_data = Mux(decoder.out.src2_ren, io.regfile.src2.rdata, decoder.out.imm)

  //完成DecodeUnit模块的逻辑
  io.executeStage.data.pc                 := pc
  io.executeStage.data.info               := info
  io.executeStage.data.src_info.src1_data := src1_data
  io.executeStage.data.src_info.src2_data := src2_data

}
