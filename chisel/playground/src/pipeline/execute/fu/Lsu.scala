package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class Lsu extends Module {
  val io = IO(new Bundle {
    val info     = Input(new Info())
    val src_info = Input(new SrcInfo())
    val result   = Output(UInt(XLEN.W))
    val dataSram = new DataSram()
  })
  io.result := 0.U 
  io.dataSram.en    := false.B
  io.dataSram.addr  := DontCare 
  io.dataSram.wdata := DontCare
  io.dataSram.wen   := 0.U
}

