package cpu

import chisel3._
import chisel3.util._

import defines._
import defines.Const._
import pipeline._

class Core extends Module {
  val io = IO(new Bundle {
    val interrupt = Input(new ExtInterrupt())
    val instSram  = new InstSram()
    val dataSram  = new DataSram()
    val debug     = new DEBUG()
  })

  val fetchUnit      = Module(new FetchUnit()).io
  val decodeStage    = Module(new DecodeStage()).io
  val decodeUnit     = Module(new DecodeUnit()).io
  val regfile        = Module(new ARegFile()).io
  val executeStage   = Module(new ExecuteStage()).io
  val executeUnit    = Module(new ExecuteUnit()).io
  val memoryStage    = Module(new MemoryStage()).io
  val memoryUnit     = Module(new MemoryUnit()).io
  val writeBackStage = Module(new WriteBackStage()).io
  val writeBackUnit  = Module(new WriteBackUnit()).io
  val ctrlunit       = Module(new CtrlUnit()).io

  // 取指单元
  fetchUnit.flush := executeUnit.flush
  fetchUnit.target := executeUnit.target
  fetchUnit.instSram <> io.instSram
  fetchUnit.decodeStage <> decodeStage.fetchUnit

  decodeStage.decodeUnit <> decodeUnit.decodeStage
  decodeUnit.regfile <> regfile.read
  decodeUnit.executeStage <> executeStage.decodeUnit
  decodeUnit.executeUnit_info <> executeUnit.memoryStage.data.info
  decodeUnit.memoryUnit_info <> memoryUnit.writeBackStage.data.info
  decodeUnit.writeBackUnit_info <> writeBackUnit.writeBackUnit_info
  decodeUnit.executeUnit_rd_info <> executeUnit.memoryStage.data.rd_info
  decodeUnit.memoryUnit_rd_info <> memoryUnit.writeBackStage.data.rd_info
  decodeUnit.writeBackUnit_rd_info <> writeBackUnit.writeBackUnit_rd_info

  executeStage.executeUnit <> executeUnit.executeStage
  executeUnit.memoryStage <> memoryStage.executeUnit
  executeUnit.dataSram <> io.dataSram
  executeUnit.mode <> decodeUnit.mode
  executeUnit.interrupt <> decodeUnit.interrupt_info
  io.interrupt <> executeUnit.interrupt_info

  memoryStage.memoryUnit <> memoryUnit.memoryStage
  io.dataSram.rdata <> memoryUnit.rdata
  memoryUnit.writeBackStage <> writeBackStage.memoryUnit

  writeBackStage.writeBackUnit <> writeBackUnit.writeBackStage
  writeBackUnit.regfile <> regfile.write
  
  writeBackUnit.debug <> io.debug
  //ctrl
  decodeUnit.executeStage.data.info <> ctrlunit.decodeUnit_info
  executeUnit.memoryStage.data.info <> ctrlunit.executeUnit_info
  executeUnit.flush <> ctrlunit.flush
  memoryUnit.writeBackStage.data.info <> ctrlunit.memoryUnit_info
  writeBackUnit.writeBackUnit_info <> ctrlunit.writeBackUnit_info

  ctrlunit.fetchUnit_ctrl <> fetchUnit.fetchUnit_ctrl
  ctrlunit.fetchUnit_ctrl <> decodeStage.fetchUnit_ctrl
  ctrlunit.decodeUnit_ctrl <> executeStage.decodeUnit_ctrl
  ctrlunit.executeUnit_ctrl <> memoryStage.executeUnit_ctrl
  ctrlunit.memoryUnit_ctrl <> writeBackStage.memoryUnit_ctrl
}


