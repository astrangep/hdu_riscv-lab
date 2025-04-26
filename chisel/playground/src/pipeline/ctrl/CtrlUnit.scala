package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig

class CtrlUnit extends Module{
    val io= IO(new Bundle{
        val decodeUnit_info = Input(new Info())
        val executeUnit_info = Input(new Info())
        val memoryUnit_info = Input(new Info())
        val writeBackUnit_info = Input(new WriteBackUnit_info())
        val fetchUnit_ctrl = new CtrlSignal()
        val decodeUnit_ctrl = new CtrlSignal()
        val executeUnit_ctrl = new CtrlSignal()
        val memoryUnit_ctrl = new CtrlSignal()
        val branch = Input(Bool())
    })
    val exe_conflict =(io.executeUnit_info.valid && io.executeUnit_info.fusel ===
        FuType.lsu && LSUOpType.isLoad(io.executeUnit_info.op))&&io.executeUnit_info.reg_wen && !(io.executeUnit_info.reg_waddr === 0.U)&&((io.decodeUnit_info.src1_ren && (io.decodeUnit_info.src1_raddr === io.executeUnit_info.reg_waddr))|
        (io.decodeUnit_info.src2_ren && (io.decodeUnit_info.src2_raddr === io.executeUnit_info.reg_waddr)))  
    io.fetchUnit_ctrl.allow_to_go := Mux(exe_conflict, false.B, true.B)
    io.fetchUnit_ctrl.do_flush := io.branch
    io.decodeUnit_ctrl.allow_to_go := Mux(exe_conflict, false.B, true.B)
    io.decodeUnit_ctrl.do_flush := io.branch
    io.executeUnit_ctrl.allow_to_go := true.B
    io.executeUnit_ctrl.do_flush := false.B
    io.memoryUnit_ctrl.allow_to_go := true.B
    io.memoryUnit_ctrl.do_flush := false.B
}
