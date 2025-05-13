package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.defines.FuType.csr
class InterruptInfo extends Bundle {
  val msi = Bool()  // 机器软件中断 (编码3)
  val mti = Bool()  // 机器定时器中断 (编码7) 
  val mei = Bool()  // 机器外部中断 (编码11)
}
class ExcCtrl extends Module with HasExceptionNO {
  val io = IO(new Bundle {
    val pc         = Input(UInt(XLEN.W))
    val info       = Input(new Info())
    val is_illegal = Input(Bool())
    val mode       = Input(UInt(2.W))  // 来自CSR的当前特权模式
    val interrupt  = Input(new InterruptInfo()) // 来自CSR的中断信号
    
    val exc_info   = Output(new ExceptionInfo())
  })
  io.exc_info.exception := VecInit(Seq.fill(EXC_WID)(false.B))
  io.exc_info.interrupt := VecInit(Seq.fill(INT_WID)(false.B))
  io.exc_info.tval      := VecInit(Seq.fill(EXC_WID)(0.U(XLEN.W)))
  val is_ebreak = io.info.fusel === csr && io.info.op === CSROpType.ebreak
  val is_ecall  = io.info.fusel === csr && io.info.op === CSROpType.ecall

  io.exc_info.exception(instAddrMisaligned) := io.pc(1,0) =/= 0.U
  io.exc_info.tval := io.pc

  io.exc_info.exception(illegalInst) := io.is_illegal
  io.exc_info.tval := io.info.inst

  io.exc_info.exception(breakPoint) := is_ebreak
  when(is_ecall){
    switch(io.mode){
      is(Priv.u) {io.exc_info.exception(ecallU) := true.B}
      is(Priv.m) {io.exc_info.exception(ecallM) := true.B}
    }
  }
  io.exc_info.interrupt(msi) := io.interrupt.msi
  io.exc_info.interrupt(mti) := io.interrupt.mti
  io.exc_info.interrupt(mei) := io.interrupt.mei


}