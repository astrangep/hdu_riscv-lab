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

  // CSR寄存器地址常量
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

  // CSR寄存器定义
  case class CsrReg(
    reg: UInt,
    readMask: UInt = Fill(XLEN, 1.U),
    writeMask: UInt = 0.U(XLEN.W),
    isRO: Bool = false.B
  )
  // 初始化所有CSR寄存器
  val csrRegs = Map(
    CSR_ADDRS("CYCLE") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
      isRO = true.B
    ),
    CSR_ADDRS("MVENDORID") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
      isRO = true.B
    ),
    CSR_ADDRS("MARCHID") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
      isRO = true.B
    ),
    CSR_ADDRS("MIMPID") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
      isRO = true.B
    ),
    CSR_ADDRS("MHARTID") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
      isRO = true.B
    ),
    CSR_ADDRS("MSTATUS") -> CsrReg(
      reg = RegInit("h1800".U(XLEN.W)),
      writeMask = "h88".U(XLEN.W)
    ),
    CSR_ADDRS("MISA") -> CsrReg(
      reg = RegInit("h8000000000001100".U(XLEN.W)),
      isRO = true.B
    ),
    CSR_ADDRS("MIE") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
      writeMask = Fill(XLEN, 1.U)
    ),
    CSR_ADDRS("MTVEC") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
      writeMask = Fill(XLEN, 1.U)
    ),
    CSR_ADDRS("MCOUNTEREN") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
      writeMask = "hfffffffffffffff7".U(XLEN.W)
    ),
    CSR_ADDRS("MSCRATCH") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
      writeMask = Fill(XLEN, 1.U)
    ),
    CSR_ADDRS("MEPC") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
      writeMask = Fill(XLEN, 1.U)
    ),
    CSR_ADDRS("MCAUSE") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
      writeMask = Fill(XLEN, 1.U)
    ),
    CSR_ADDRS("MTVAL") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
      writeMask = Fill(XLEN, 1.U)
    ),
    CSR_ADDRS("MIP") -> CsrReg(
      reg = RegInit(0.U(XLEN.W)),
      writeMask = "h888".U(XLEN.W)
    )
  )

  // cycle寄存器自增
  csrRegs(CSR_ADDRS("CYCLE")).reg := csrRegs(CSR_ADDRS("CYCLE")).reg + 1.U

  // 获取CSR地址和操作数
  val csrAddr = io.info.inst(31, 20)
  val rs1Data = io.src_info.src1_data
  val zimm = Cat(0.U((XLEN - 5).W), io.info.inst(19, 15))
  val isImm = io.info.op(2)
  val srcValue = Mux(isImm, zimm, rs1Data)

  // 默认返回值
  val defaultRead = WireInit(0.U(XLEN.W))
  val readValid = WireInit(false.B)

  // 查找并处理CSR寄存器
  csrRegs.foreach { case (addr, csr) =>
    when(csrAddr === addr) {
      defaultRead := csr.reg & csr.readMask
      readValid := true.B
      
      // 写操作处理
      when(io.info.valid && io.info.fusel === FuType.csr && !csr.isRO) {
        val writeData = MuxLookup(io.info.op(1, 0), 0.U)(
          Seq(
            "b01".U -> srcValue,                     // write/writei
            "b10".U -> (csr.reg | srcValue),        // set/seti
            "b11".U -> (csr.reg & ~srcValue)        // clear/cleari
          )
        )
        csr.reg := (writeData & csr.writeMask) | (csr.reg & ~csr.writeMask)
      }
    }
  }

  // 输出结果
  io.result := defaultRead
}