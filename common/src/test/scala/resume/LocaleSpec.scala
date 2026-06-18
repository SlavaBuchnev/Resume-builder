package resume

class LocaleSpec extends AnyFunSuite {
  test("loadAll returns correct list from test file") {
    val list = Locale.loadAll("test-locales.yaml")
    assert(list.size == 2)
    assert(list.exists(_.name == "ru"))
    assert(list.exists(_.name == "en"))
  }

  test("Locale.all should load default file") {
    assert(Locale.all.nonEmpty)
  }

  test("fromString should find ru") {
    val ru = Locale.fromString("ru")
    assert(ru.exists(_.aboutMe == "О себе"))
  }

  test("fromString with unknown key should fallback to first locale") {
    val unknown = Locale.fromString("unknown")
    assert(unknown.isEmpty)
  }
}
