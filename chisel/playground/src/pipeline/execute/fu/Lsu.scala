package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class Lsu extends Module  with HasExceptionNO {
  val io = IO(new Bundle {
    val exc_info = Input(new ExceptionInfo())
    val info     = Input(new Info())
    val src_info = Input(new SrcInfo())
    val result   = Output(UInt(XLEN.W))
    val ex       = Output(new ExceptionInfo())
    val dataSram = new DataSram()
  })
  val op = io.info.op
  val src2_data =io.src_info.src2_data
  val mem_addr = io.src_info.src1_data + io.info.imm
  val addr_low = mem_addr(2,0)
  val shift = Wire(UInt(8.W))
  val mem_wen_tmp = Wire(UInt(8.W))
  val is_load = LSUOpType.isLoad(op)
  val is_store = LSUOpType.isStore(op)
  val addr_misaligned = MuxLookup(op(1,0), false.B)(
    Seq(
      "b00".U -> false.B,
      "b01".U -> (mem_addr(0) =/= 0.U),
      "b10".U -> (mem_addr(1,0) =/= 0.U),
      "b11".U -> (mem_addr(2,0) =/= 0.U)
    )
  )
  val ex = io.exc_info
  val has_exc = false.B
  when(io.info.valid && io.info.fusel === FuType.lsu) {
    when(is_load && addr_misaligned) {
      ex.exception(loadAddrMisaligned) := true.B
      ex.tval(loadAddrMisaligned) := mem_addr
      has_exc := true.B
    }.elsewhen(is_store && addr_misaligned) {
      ex.exception(storeAddrMisaligned) := true.B
      ex.tval(storeAddrMisaligned) := mem_addr
      has_exc := true.B
    }
  }
  shift := MuxLookup(op(1, 0), 0.U)(Seq(
    "b00".U -> "b00000001".U,
    "b01".U -> "b00000011".U,
    "b10".U -> "b00001111".U,
    "b11".U -> "b11111111".U
  ))

  mem_wen_tmp := shift << addr_low

  io.dataSram.en := !reset.asBool && !has_exc
  io.dataSram.wen := Mux(io.info.valid && (io.info.fusel === FuType.lsu) && LSUOpType.isStore(op), mem_wen_tmp, 0.U)
  io.dataSram.addr := mem_addr(SRAM_ADDR_WID-1,0)
  io.dataSram.wdata := MuxLookup(op(1,0),0.U)(Seq(
    "b00".U -> Fill(8, src2_data(7, 0)),    
    "b01".U -> Fill(4, src2_data(15, 0)),   
    "b10".U -> Fill(2, src2_data(31, 0)),   
    "b11".U -> src2_data                    
  ))
  io.result := 0.U
  io.ex := ex
}

