import gcd._
import circt.stage._

object TestMain extends App {
  def top                = new GCD()
  val generator          = Seq(chisel3.stage.ChiselGeneratorAnnotation(() => top))
  (new ChiselStage).execute(args, generator :+ CIRCTTargetAnnotation(CIRCTTarget.Verilog))
}
