package roadmap.cde

import org.chipsalliance.cde.config._

import org.reflections.Reflections
import scala.reflect.runtime.universe._
import scala.reflect.runtime.currentMirror

case object Key0 extends Field[Int](0)
case object Key1 extends Field[Int]
case object Key2 extends Field[Int](0)
case object Key3 extends Field[Int]
case object Key4 extends Field[Float]
case object Key5 extends Field[Seq[Option[String]]]

object paramDump {
  def apply(p: Parameters, withscopes: Seq[String] = Seq()) = {
    def findPackageName(sym: Symbol): String = {
      if (sym.isPackage) sym.fullName
      else findPackageName(sym.owner)
    }

    val caller = Thread.currentThread.getStackTrace().toSeq.apply(2).getClassName()
    val scopes = findPackageName(currentMirror.staticClass(caller)) +: withscopes
    dump(p, scopes.toSet)
  }

  private def dump(p: Parameters, scopes: Set[String]) = {
    val fields = scopes.map(new Reflections(_)).flatMap(_.getSubTypesOf(classOf[Field[_]]).toArray).toSet
    val result = fields.toSeq.map(_.toString).sorted.map(s => {
      val rm = runtimeMirror(getClass.getClassLoader)
      val module = rm.staticModule(s.substring(6))
      val field = rm.reflectModule(module).instance

      val liftCall = typeOf[View].decl(TermName("lift")).asMethod
      val value = rm.reflect(p).reflectMethod(liftCall)(field)

      s"${field}: ${value}"
    })

    println("Dumping " + p.getClass.toString.substring(6))
    println("  under scopes: " + scopes)
    result.map("\t" + _).foreach(println)
  }
}

object paramDumpTest extends App {
  val p = Parameters.empty
  paramDump(p)
}