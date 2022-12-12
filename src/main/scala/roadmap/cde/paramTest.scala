package roadmap.cde

import org.chipsalliance.cde.config._

object paramTest extends App {
  var p: Parameters = Parameters.empty
  assert(p(Key0) == p.apply(Key0))
  assert(Some(0) == p.lift(Key0))

  assert(0 == p(Key0))
  try { p(Key1) }
  catch {
    case e: java.lang.IllegalArgumentException => println(s"Key1: ${p.lift(Key1)}")
  }

  paramDump(p)
}