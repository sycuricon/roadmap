package roadmap.cde

import org.chipsalliance.cde.config._

object viewTest extends App {
  val p = new Config((_, _, _) => {
    case Key0 => 0
  }) ++ Parameters((site, here, up) => {
    case Key0 => 1
    case Key1 => here(Key0)
    case Key2 => up(Key3) - 1
  }) ++ Parameters((site, here, up) => {
    case Key3 => site(Key0) + 3
  })
  assert(p(Key0) == 0, s"Expect 0, got ${p(Key0)}")
  assert(p(Key1) == 1, s"Expect 1, got ${p(Key1)}")
  assert(p(Key2) == 2, s"Expect 2, got ${p(Key2)}")
  assert(p(Key3) == 3, s"Expect 3, got ${p(Key3)}")

  paramDump(p)
}