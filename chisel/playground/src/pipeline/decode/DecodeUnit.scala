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
    val executeUnit_info = Input(new Info())
    val memoryUnit_info = Input(new Info())
    val writeBackUnit_info = Input(new WriteBackUnit_info())
    val executeUnit_rd_info = Input(new RdInfo())
    val memoryUnit_rd_info = Input(new RdInfo())
    val writeBackUnit_rd_info = Input(new RdInfo())
    // 输出
    val executeStage = Output(new DecodeUnitExecuteUnit())
  })

  // 译码阶段完成指令的译码操作以及源操作数的准备

  val decoder = Module(new Decoder()).io
  decoder.in.inst := io.decodeStage.data.inst

  val pc   = io.decodeStage.data.pc
  val info = Wire(new Info())

  info       := decoder.out.info
  info.valid := io.decodeStage.data.valid

  //完成寄存器堆的读取
  io.regfile.src1.raddr := info.src1_raddr 
  io.regfile.src2.raddr := info.src2_raddr    

  val src1_data = Mux(info.src1_ren, io.regfile.src1.rdata, Mux(info.is_lui, 0.U, pc))
  val src2_data = Mux(info.src2_ren, io.regfile.src2.rdata, info.imm)
  val fowardctrl = Module(new FowardCtrl()).io
  fowardctrl.executeUnit_info := io.executeUnit_info
  fowardctrl.memoryUnit_info := io.memoryUnit_info
  fowardctrl.writeBackUnit_info := io.writeBackUnit_info
  fowardctrl.executeUnit_rd_info := io.executeUnit_rd_info
  fowardctrl.memoryUnit_rd_info := io.memoryUnit_rd_info
  fowardctrl.writeBackUnit_rd_info := io.writeBackUnit_rd_info
  fowardctrl.src1_read_signal.ren := info.src1_ren
  fowardctrl.src1_read_signal.raddr := info.src1_raddr
  fowardctrl.src2_read_signal.ren := info.src2_ren
  fowardctrl.src2_read_signal.raddr := info.src2_raddr
  fowardctrl.src_info_in.src1_data := src1_data
  fowardctrl.src_info_in.src2_data := src2_data
  //完成DecodeUnit模块的逻辑
  io.executeStage.data.pc                 := pc
  io.executeStage.data.info               := info
  io.executeStage.data.src_info           := fowardctrl.src_info_out

}
