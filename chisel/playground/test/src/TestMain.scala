// import cpu._
// import circt.stage._

// object TestMain extends App {
//   implicit val cpuConfig = new CpuConfig()
//   def top                = new Top()
//   val generator          = Seq(chisel3.stage.ChiselGeneratorAnnotation(() => top))
//   (new ChiselStage).execute(args, generator :+ CIRCTTargetAnnotation(CIRCTTarget.Verilog))
// }
