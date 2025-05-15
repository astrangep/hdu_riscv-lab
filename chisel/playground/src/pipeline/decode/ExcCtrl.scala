package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.defines.FuType.csr
class ExcCtrl extends Module with HasExceptionNO {
  val io = IO(new Bundle {
    val pc         = Input(UInt(XLEN.W))
    val info       = Input(new Info())
    val is_illegal = Input(Bool())
    val mode       = Input(UInt(2.W))  // 来自CSR的当前特权模式
    val interrupt_info  = Input(new ExtInterrupt()) // 来自CSR的中断信号
    
    val exc_info   = Output(new ExceptionInfo())
  })
  io.exc_info.exception := VecInit(Seq.fill(EXC_WID)(false.B))
  io.exc_info.interrupt := VecInit(Seq.fill(INT_WID)(false.B))
  io.exc_info.tval      := VecInit(Seq.fill(EXC_WID)(0.U(XLEN.W)))
  val is_ebreak = io.info.fusel === csr && io.info.op === CSROpType.ebreak
  val is_ecall  = io.info.fusel === csr && io.info.op === CSROpType.ecall
  when(io.info.valid){
      io.exc_info.exception(instAddrMisaligned) := io.pc(1,0) =/= 0.U
      io.exc_info.tval(instAddrMisaligned) := io.pc

      io.exc_info.exception(illegalInst) := io.is_illegal
      io.exc_info.tval(illegalInst) := io.info.inst

      io.exc_info.exception(breakPoint) := is_ebreak
      when(is_ecall){
        switch(io.mode){
          is(Priv.u) {io.exc_info.exception(ecallU) := true.B}
          is(Priv.m) {io.exc_info.exception(ecallM) := true.B}
      }
      }
      io.exc_info.interrupt(msi) := io.interrupt_info.msi
      io.exc_info.interrupt(mti) := io.interrupt_info.mti
      io.exc_info.interrupt(mei) := io.interrupt_info.mei
  }
}