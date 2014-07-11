package com.softwaremill.csvperf

import org.parboiled2.ParserInput
import org.scalatest.{FlatSpec, ShouldMatchers}

class PBDelimitedParserSpec extends FlatSpec with ShouldMatchers {
  val MultilineTabSeparatedFile = "a|b|c\nc|c|c"

  it should "parse multiline strings" in {
    val parser = new PBDelimetedParser(ParserInput(MultilineTabSeparatedFile), "|")

    val result = parser.file.run()

    println(result)
  }
}
