package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
class Mdu extends Module {
   val io = IO(new Bundle {
     val info     = Input(new Info())
     val src_info = Input(new SrcInfo())
     val result   = Output(UInt(XLEN.W))
   })
 }