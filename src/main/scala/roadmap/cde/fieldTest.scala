package roadmap.cde

import org.chipsalliance.cde.config._

object fieldTest extends App {
  val p0 = Parameters((site, here, up) => {
    case Key0 => 0
  })
  val p1 = p0.alter((site, here, up) => {
    case Key1 => 1
  })
  val p2 = p1.alterPartial({
    case Key2 => 2
  })
  val p3 = p2.alterMap(Map(
    Key3 -> 3
  ))

  assert(p0(Key0) == 0)
  assert(p1(Key1) == 1)
  assert(p2(Key2) == 2)
  assert(p3(Key3) == 3)

  paramDump(p0)
  paramDump(p1)
  paramDump(p2)
  paramDump(p3)
}