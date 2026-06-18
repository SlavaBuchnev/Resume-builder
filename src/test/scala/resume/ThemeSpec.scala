package resume

import org.scalatest.funsuite.AnyFunSuite

class ThemeSpec extends AnyFunSuite {

  test("loadAll should return correct number of themes from test file") {
    val themes = Theme.loadAll("test-themes.yaml")
    assert(themes.size == 2)
  }

  test("loaded themes should contain classic and dark") {
    val themes = Theme.loadAll("test-themes.yaml")
    val names = themes.map(_.name)
    assert(names.contains("classic"))
    assert(names.contains("dark"))
  }

  test("classic theme should have correct color values") {
    val classic = Theme.loadAll("test-themes.yaml").find(_.name == "classic").get
    assert(classic.primaryColor == "#2c3e50")
    assert(classic.secondaryColor == "#34495e")
    assert(classic.backgroundColor == "#ffffff")
    assert(classic.fontFamily.contains("DejaVu Sans"))
  }

  test("dark theme should have correct color values") {
    val dark = Theme.loadAll("test-themes.yaml").find(_.name == "dark").get
    assert(dark.primaryColor == "#ecf0f1")
    assert(dark.backgroundColor == "#2c3e50")
    assert(dark.borderColor == "#ecf0f1")
  }

  test("Theme.all should load the default themes.yaml from classpath") {
    val all = Theme.all
    assert(all.nonEmpty)
    val names = all.map(_.name)
    assert(names.contains("classic"))
    assert(names.contains("modern"))
    assert(names.contains("dark"))
  }

  test("fromString should return Some for an existing theme name") {
    assert(Theme.fromString("classic").isDefined)
    assert(Theme.fromString("dark").isDefined)
  }

  test("fromString should return None for a non-existent theme name") {
    assert(Theme.fromString("unicorn").isEmpty)
  }
}
