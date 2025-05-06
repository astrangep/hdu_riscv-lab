package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class Csr extends Module {
  val io = IO(new Bundle {
    val info = Input(new Info())
    val src_info = Input(new SrcInfo())
    val result = Output(UInt(XLEN.W))
  })

  // CSR寄存器地址
  val CSR_CYCLE = "hc00".U(12.W)
  val CSR_MVENDORID = "hf11".U(12.W)
  val CSR_MARCHID = "hf12".U(12.W)
  val CSR_MIMPID = "hf13".U(12.W)
  val CSR_MHARTID = "hf15".U(12.W)
  val CSR_MSTATUS = "h300".U(12.W)
  val CSR_MISA = "h301".U(12.W)
  val CSR_MIE = "h304".U(12.W)
  val CSR_MTVEC = "h305".U(12.W)
  val CSR_MCOUNTEREN = "h306".U(12.W)
  val CSR_MSCRATCH = "h340".U(12.W)
  val CSR_MEPC = "h341".U(12.W)
  val CSR_MCAUSE = "h342".U(12.W)
  val CSR_MTVAL = "h343".U(12.W)
  val CSR_MIP = "h344".U(12.W)

  // CSR寄存器
  val cycle = RegInit(0.U(XLEN.W))
  val mvendorid = RegInit(0.U(XLEN.W))
  val marchid = RegInit(0.U(XLEN.W))
  val mimpid = RegInit(0.U(XLEN.W))
  val mhartid = RegInit(0.U(XLEN.W))
  val mstatus = RegInit("h1800".U(XLEN.W))
  val misa = RegInit("h8000000000001100".U(XLEN.W))
  val mie = RegInit(0.U(XLEN.W))
  val mtvec = RegInit(0.U(XLEN.W))
  val mcounteren = RegInit(0.U(XLEN.W))
  val mscratch = RegInit(0.U(XLEN.W))
  val mepc = RegInit(0.U(XLEN.W))
  val mcause = RegInit(0.U(XLEN.W))
  val mtval = RegInit(0.U(XLEN.W))
  val mip = RegInit(0.U(XLEN.W))

  // cycle寄存器自增
  cycle := cycle + 1.U

  // CSR指令地址
  val csrAddr = io.info.inst(31, 20)

  // 读取数据
  val rs1Data = io.src_info.src1_data
  val zimm = Cat(0.U((XLEN - 5).W), io.info.inst(19, 15))
  val isImm = io.info.op(2)
  val srcValue = Mux(isImm, zimm, rs1Data)

  // 读取CSR寄存器值
  val csrRdata = MuxLookup(csrAddr, 0.U)(
    Seq(
      CSR_CYCLE -> cycle,
      CSR_MVENDORID -> mvendorid,
      CSR_MARCHID -> marchid,
      CSR_MIMPID -> mimpid,
      CSR_MHARTID -> mhartid,
      CSR_MSTATUS -> mstatus,
      CSR_MISA -> misa,
      CSR_MIE -> mie,
      CSR_MTVEC -> mtvec,
      CSR_MCOUNTEREN -> mcounteren,
      CSR_MSCRATCH -> mscratch,
      CSR_MEPC -> mepc,
      CSR_MCAUSE -> mcause,
      CSR_MTVAL -> mtval,
      CSR_MIP -> mip
    )
  )

  // 计算写入值
  val writeData = MuxLookup(io.info.op(1, 0), 0.U)(
    Seq(
      "b01".U -> srcValue, // write/writei
      "b10".U -> (csrRdata | srcValue), // set/seti
      "b11".U -> (csrRdata & ~srcValue) // clear/cleari
    )
  )

  // 定义CSR寄存器的读掩码和写掩码
  def getReadMask(addr: UInt): UInt = {
    MuxLookup(addr, 0.U)(
      Seq(
        CSR_CYCLE -> Fill(XLEN, 1.U(1.W)),
        CSR_MVENDORID -> Fill(XLEN, 1.U(1.W)),
        CSR_MARCHID -> Fill(XLEN, 1.U(1.W)),
        CSR_MIMPID -> Fill(XLEN, 1.U(1.W)),
        CSR_MHARTID -> Fill(XLEN, 1.U(1.W)),
        CSR_MSTATUS -> Fill(XLEN, 1.U(1.W)),
        CSR_MISA -> Fill(XLEN, 1.U(1.W)),
        CSR_MIE -> Fill(XLEN, 1.U(1.W)),
        CSR_MTVEC -> Fill(XLEN, 1.U(1.W)),
        CSR_MCOUNTEREN -> Fill(XLEN, 1.U(1.W)),
        CSR_MSCRATCH -> Fill(XLEN, 1.U(1.W)),
        CSR_MEPC -> Fill(XLEN, 1.U(1.W)),
        CSR_MCAUSE -> Fill(XLEN, 1.U(1.W)),
        CSR_MTVAL -> Fill(XLEN, 1.U(1.W)),
        CSR_MIP -> Fill(XLEN, 1.U(1.W))
      )
    )
  }

  def getWriteMask(addr: UInt): UInt = {
    MuxLookup(addr, 0.U)(
      Seq(
        CSR_CYCLE -> 0.U(XLEN.W),
        CSR_MVENDORID -> 0.U(XLEN.W),
        CSR_MARCHID -> 0.U(XLEN.W),
        CSR_MIMPID -> 0.U(XLEN.W),
        CSR_MHARTID -> 0.U(XLEN.W),
        CSR_MSTATUS -> "h88".U(XLEN.W),
        CSR_MISA -> 0.U(XLEN.W),
        CSR_MIE -> Fill(XLEN, 1.U(1.W)),
        CSR_MTVEC -> Fill(XLEN, 1.U(1.W)),
        CSR_MCOUNTEREN -> "hfffffffffffffff7".U(XLEN.W),
        CSR_MSCRATCH -> Fill(XLEN, 1.U(1.W)),
        CSR_MEPC -> Fill(XLEN, 1.U(1.W)),
        CSR_MCAUSE -> Fill(XLEN, 1.U(1.W)),
        CSR_MTVAL -> Fill(XLEN, 1.U(1.W)),
        CSR_MIP -> "h888".U(XLEN.W)
      )
    )
  }

  val readMask = getReadMask(csrAddr)
  val writeMask = getWriteMask(csrAddr)
  val maskedWriteData = writeData & writeMask

  // 写入CSR寄存器
  when(io.info.valid && io.info.fusel === FuType.csr) {
    switch(csrAddr) {
      is(CSR_MSTATUS) { mstatus := maskedWriteData | (mstatus & ~writeMask) }
      is(CSR_MIE) { mie := maskedWriteData }
      is(CSR_MTVEC) { mtvec := maskedWriteData }
      is(CSR_MCOUNTEREN) {
        mcounteren := maskedWriteData | (mcounteren & ~writeMask)
      }
      is(CSR_MSCRATCH) { mscratch := maskedWriteData }
      is(CSR_MEPC) { mepc := maskedWriteData }
      is(CSR_MCAUSE) { mcause := maskedWriteData }
      is(CSR_MTVAL) { mtval := maskedWriteData }
      is(CSR_MIP) { mip := maskedWriteData | (mip & ~writeMask) }
    }
  }

  // 输出读取的数据
  io.result := csrRdata & readMask
}
