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

  // 取指单元
  fetchUnit.instSram <> io.instSram
  fetchUnit.decodeStage <> decodeStage.fetchUnit

  decodeStage.decodeUnit <> decodeUnit.decodeStage
  decodeUnit.regfile <> regfile.read
  decodeUnit.executeStage <> executeStage.decodeUnit

  executeStage.executeUnit <> executeUnit.executeStage
  executeUnit.memoryStage <> memoryStage.executeUnit
  executeUnit.dataSram <> io.dataSram

  memoryStage.memoryUnit <> memoryUnit.memoryStage
  io.dataSram.rdata <> memoryUnit.rdata
  memoryUnit.writeBackStage <> writeBackStage.memoryUnit

  writeBackStage.writeBackUnit <> writeBackUnit.writeBackStage
  writeBackUnit.regfile <> regfile.write
  
  writeBackUnit.debug <> io.debug
}
