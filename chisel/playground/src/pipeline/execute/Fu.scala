package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig

class Fu extends Module {
  val io = IO(new Bundle {
    val data = new Bundle {
      val pc       = Input(UInt(XLEN.W))
      val info     = Input(new Info())
      val src_info = Input(new SrcInfo())
      val rd_info  = Output(new RdInfo())
    }

    val dataSram = new DataSram()
  })

  val alu = Module(new Alu()).io
  val mdu = Module(new Mdu()).io
  val lsu = Module(new Lsu()).io

  lsu.dataSram <> io.dataSram
  alu.info     := io.data.info
  alu.src_info := io.data.src_info

  mdu.info     := io.data.info
  mdu.src_info := io.data.src_info

  lsu.info     := io.data.info
  lsu.src_info := io.data.src_info
  io.data.rd_info.wdata := MuxLookup(io.data.info.fusel,0.U)(Seq(FuType.alu -> alu.result, FuType.mdu -> mdu.result, FuType.lsu -> lsu.result))
}
