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
  val op = io.info.op
  val src2_data =io.src_info.src2_data
  val mem_addr = io.src_info.src1_data + io.info.imm
  val addr_low = mem_addr(2,0)
  val shift = Wire(UInt(8.W))
  val mem_wen_tmp = Wire(UInt(8.W))

  shift := MuxLookup(op(1, 0), 0.U)(Seq(
    "b00".U -> "b00000001".U,
    "b01".U -> "b00000011".U,
    "b10".U -> "b00001111".U,
    "b11".U -> "b11111111".U
  ))

  mem_wen_tmp := shift << addr_low

  io.dataSram.en := !reset.asBool
  io.dataSram.wen := Mux(io.info.valid && (io.info.fusel === FuType.lsu) && LSUOpType.isStore(op), mem_wen_tmp, 0.U)
  io.dataSram.addr := mem_addr(SRAM_ADDR_WID-1,0)
  io.dataSram.wdata := MuxLookup(op(1,0),0.U)(Seq(
    "b00".U -> Fill(8, src2_data(7, 0)),    // sb
    "b01".U -> Fill(4, src2_data(15, 0)),   // sh
    "b10".U -> Fill(2, src2_data(31, 0)),   // sw
    "b11".U -> src2_data                    // sd
  ))
  io.result := 0.U
}

