package cpu.pipeline
import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.defines.CSROpType.isCSROp

class Csr extends Module with HasExceptionNO {
  val io = IO(new Bundle {
    val exc_info = Input(new ExceptionInfo())
    val info = Input(new Info())
    val src_info = Input(new SrcInfo())
    val result = Output(UInt(XLEN.W))
    val pc = Input(UInt(XLEN.W))
    val interrupt  = Output(new InterruptInfo())
    val mode = Output(UInt(2.W))
    val target = Output(UInt(XLEN.W))
    val flush = Output(Bool())
    val has_exc = Output(Bool())
  })

  val CSR_ADDRS = Map(
    "CYCLE"       -> "hc00".U(12.W),
    "MVENDORID"   -> "hf11".U(12.W),
    "MARCHID"     -> "hf12".U(12.W),
    "MIMPID"      -> "hf13".U(12.W),
    "MHARTID"     -> "hf15".U(12.W),
    "MSTATUS"     -> "h300".U(12.W),
    "MISA"        -> "h301".U(12.W),
    "MIE"         -> "h304".U(12.W),
    "MTVEC"       -> "h305".U(12.W),
    "MCOUNTEREN"  -> "h306".U(12.W),
    "MSCRATCH"    -> "h340".U(12.W),
    "MEPC"        -> "h341".U(12.W),
    "MCAUSE"      -> "h342".U(12.W),
    "MTVAL"       -> "h343".U(12.W),
    "MIP"         -> "h344".U(12.W)
  )

  case class CsrReg(
    reg: UInt,
    readMask: UInt = Fill(XLEN, 1.U),
    writeMask: UInt = 0.U(XLEN.W),
    write: Bool = false.B
  )
  val csrRegs = Map(
    CSR_ADDRS("CYCLE") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
    ),
    CSR_ADDRS("MVENDORID") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
    ),
    CSR_ADDRS("MARCHID") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
    ),
    CSR_ADDRS("MIMPID") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
    ),
    CSR_ADDRS("MHARTID") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
    ),
    CSR_ADDRS("MSTATUS") -> CsrReg(
      reg = RegInit("h200000000".U(XLEN.W)),
      writeMask = "h21888".U(XLEN.W),
      write = true.B
    ),
    CSR_ADDRS("MISA") -> CsrReg(
      reg = RegInit("h8000000000101100".U(XLEN.W)),
    ),
    CSR_ADDRS("MIE") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
      writeMask = Fill(XLEN, 1.U),
      write = true.B
    ),
    CSR_ADDRS("MTVEC") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
      writeMask = Fill(XLEN, 1.U),
      write = true.B
    ),
    CSR_ADDRS("MCOUNTEREN") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
      writeMask = "hfffffffffffffff".U(XLEN.W),
      write = true.B
    ),
    CSR_ADDRS("MSCRATCH") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
      writeMask = Fill(XLEN, 1.U),
      write = true.B
    ),
    CSR_ADDRS("MEPC") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
      writeMask = Fill(XLEN, 1.U),
      write = true.B
    ),
    CSR_ADDRS("MCAUSE") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
      writeMask = Fill(XLEN, 1.U),
      write = true.B
    ),
    CSR_ADDRS("MTVAL") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
      writeMask = Fill(XLEN, 1.U),
      write = true.B
    ),
    CSR_ADDRS("MIP") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
      writeMask = "h888".U(XLEN.W),
      write = true.B
    )
  )
  csrRegs(CSR_ADDRS("CYCLE")).reg := csrRegs(CSR_ADDRS("CYCLE")).reg + 1.U
  val mode = RegInit(Priv.m)
  val is_mret = io.info.fusel === FuType.csr && io.info.op === CSROpType.mret
  val csr_addr = io.info.inst(31, 20)
  val rs1_data = io.src_info.src1_data
  val zimm = ZeroExtend(io.info.inst(19,15), 64)
  val is_imm = io.info.op(2)
  val src_value = Mux(is_imm, zimm, rs1_data)

  val default_read = WireInit(0.U(XLEN.W))
  val illegal_addr = WireInit(true.B)

  val only_read = rs1_data === 0.U && (io.info.op === CSROpType.clear | io.info.op === CSROpType.cleari | io.info.op === CSROpType.set | io.info.op === CSROpType.seti)
  val write = io.info.valid && isCSROp(io.info.op)
  val illegal_write = write && csr_addr(11,10) === "b'11".U && !only_read
  val illegal_mode = mode < csr_addr(9,8)
  val illegal_access = illegal_write | illegal_mode
  val wen = write && !illegal_access
  csrRegs.foreach { case (addr, csr) =>
    when(csr_addr === addr) {
      default_read := csr.reg & csr.readMask
      illegal_addr := false.B
      when(wen) {
        val writeData = MuxLookup(io.info.op(1, 0), 0.U)(
          Seq(
            "b01".U -> src_value,                    
            "b10".U -> (csr.reg | src_value),        
            "b11".U -> (csr.reg & ~src_value)        
          )
        )
        csr.reg := (writeData & csr.writeMask) | (csr.reg & ~csr.writeMask)
      }
    }
  }
  when((write && illegal_access) | illegal_addr){
    io.exc_info.exception(illegalInst) := true.B
    io.exc_info.tval := io.info.inst
  }
  val is_exception = io.exc_info.exception.asUInt.orR 
  val is_interrupt = io.exc_info.interrupt.asUInt.orR
  val has_exc = is_exception | is_interrupt
  when(has_exc){
      val interruptNO = PriorityMux(IntPriority.map(i => (io.exc_info.interrupt(i), i.U)))
      val exceptionNO = PriorityMux(ExcPriority.map(e => (io.exc_info.exception(e), e.U)))
      val causeNO = Mux(is_interrupt, interruptNO, exceptionNO)
      csrRegs(CSR_ADDRS("MTVAL")).reg := Mux(is_interrupt, 0.U, causeNO)
      csrRegs(CSR_ADDRS("MCAUSE")).reg := Mux(is_interrupt, causeNO | (1 << (XLEN-1)).U, causeNO)
      csrRegs(CSR_ADDRS("MEPC")).reg := io.pc
      val legal_mode = mode === Priv.m | mode === Priv.u
      val mstatus = csrRegs(CSR_ADDRS("MSTATUS")).reg
      csrRegs(CSR_ADDRS("MSTATUS")).reg := Cat(
      mstatus(XLEN-1, 13),
      Mux(legal_mode, mode, mstatus(12,11)),                            // MPP
      mstatus(10, 8),
      mstatus(3),                        // MPIE = MIE
      mstatus(6,4),                             
      0.U,                               // MIE = 0
      mstatus(2,0)
      )
      mode := Priv.m
      io.flush := true.B
      val mtvec = csrRegs(CSR_ADDRS("MTVEC")).reg
      io.target := (mtvec(XLEN-1, 2) << 2) + Mux(mtvec(0) && is_interrupt, causeNO << 2, 0.U)
  }
  .elsewhen(is_mret) {
    val mstatus = csrRegs(CSR_ADDRS("MSTATUS")).reg
    csrRegs(CSR_ADDRS("MSTATUS")).reg := Cat(
      mstatus(XLEN-1, 18),
      mstatus(12,11) === Priv.m,         // MPriv
      mstatus(16,13),
      Priv.u,                            // MPP = U
      mstatus(10, 8),
      1.U,                               // MPIE = 1
      mstatus(6,4),
      mstatus(7),                        // MIE = MPIE
      mstatus(2, 0)
    )
    mode := mstatus(12, 11)
    io.flush := true.B              
    io.target := csrRegs(CSR_ADDRS("MEPC")).reg
  }

  // 中断检测与生成
  val mie = csrRegs(CSR_ADDRS("MIE")).reg
  val mip = WireInit(0.U(XLEN.W))
  
  // 更新MIP寄存器
  mip := Cat(
    Fill(XLEN-12, 0.U),
    io.exc_info.interrupt(mei),
    0.U, io.exc_info.interrupt(mti),
    0.U, io.exc_info.interrupt(msi),
    0.U
  )
  csrRegs(CSR_ADDRS("MIP")).reg := mip & "h0000000000000888".U

  // 中断响应条件
  val mstatus = csrRegs(CSR_ADDRS("MSTATUS")).reg
  def checkInterrupt(irq: Bool, bit: Int): Bool = {
    irq && mie(bit) && (mode === Priv.m && mstatus(3) || mode < Priv.m)
  }

  io.interrupt.msi := checkInterrupt(mip(3), 3)
  io.interrupt.mti := checkInterrupt(mip(7), 7)
  io.interrupt.mei := checkInterrupt(mip(11), 11)
  io.has_exc := has_exc
  io.result := default_read
}