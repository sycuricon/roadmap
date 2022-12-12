package roadmap.cde

import org.chipsalliance.cde.config._

import org.reflections.Reflections
import scala.reflect.runtime.universe

case object Key0 extends Field[Int](0)
case object Key1 extends Field[Int]
case object Key2 extends Field[Int](0)
case object Key3 extends Field[Int]

object paramDump {
  def apply(p: Parameters) = {
    val reflections = new Reflections("roadmap.cde")
    val fields = reflections.getSubTypesOf(classOf[Field[Int]]).toArray
    val result = fields.map(_.toString).sorted.map(s => {
      val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)
      val module = runtimeMirror.staticModule(s.substring(6))
      val field = runtimeMirror.reflectModule(module).instance

      val liftCall = universe.typeOf[View].decl(universe.TermName("lift")).asMethod
      val value = runtimeMirror.reflect(p).reflectMethod(liftCall)(field)

      s"  ${field}: ${value}"
    })

    println(p.getClass.toString.substring(6))
    result.foreach(println)
  }
}

object dumpTest extends App {
  val p = Parameters.empty
  paramDump(p)
}