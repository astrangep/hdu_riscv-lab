package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
class FowardCtrl extends Module{
    val io= IO(new Bundle{
        val executeUnit_info = Input(new Info())
        val memoryUnit_info = Input(new Info())
        val writeBackUnit_info = Input(new WriteBackUnit_info())
        val executeUnit_rd_info = Input(new RdInfo())
        val memoryUnit_rd_info = Input(new RdInfo())
        val writeBackUnit_rd_info = Input(new RdInfo())
        val src1_read_signal = Input(new SrcReadSignal())
        val src2_read_signal = Input(new SrcReadSignal())
        val src_info_in = Input(new SrcInfo())
        val src_info_out = Output(new SrcInfo())
    })
    val src1_raddr = io.src1_read_signal.raddr
    val src2_raddr = io.src2_read_signal.raddr
    val src1_ren   = io.src1_read_signal.ren
    val src2_ren   = io.src2_read_signal.ren
    val src1_data  = io.src_info_in.src1_data
    val src2_data  = io.src_info_in.src2_data
    val ex_waddr   = io.executeUnit_info.reg_waddr
    val me_waddr   = io.memoryUnit_info.reg_waddr
    val wb_waddr   = io.writeBackUnit_info.reg_waddr
    io.src_info_out.src1_data := Mux(src1_ren, MuxLookup(src1_raddr, src1_data)(Seq(
            0.U      -> 0.U,
            ex_waddr -> io.executeUnit_rd_info.wdata,
            me_waddr -> io.memoryUnit_rd_info.wdata,
            wb_waddr -> io.writeBackUnit_rd_info.wdata
    )), src1_data)
    io.src_info_out.src2_data := Mux(src2_ren, MuxLookup(src2_raddr, src2_data)(Seq(
            0.U      -> 0.U,
            ex_waddr -> io.executeUnit_rd_info.wdata,
            me_waddr -> io.memoryUnit_rd_info.wdata,
            wb_waddr -> io.writeBackUnit_rd_info.wdata
    )), src2_data)
}

