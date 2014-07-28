package com.softwaremill.csvperf

import org.parboiled2.ParserInput
import org.scalatest.{FlatSpec, MustMatchers}


class PbdCsvSpec extends FlatSpec with MustMatchers {

  it should "not fail when quotes are inside a field" in {
      // given
      val line1 = """ala,("ma"),kota"""
      val line2 = """ala,asdf"ma"asdf,kota"""

      // when & then
      new PBDelimetedParser(ParserInput(line1), ",").file.run().get(0) must contain allOf("ala", """("ma")""", "kota")
      new PBDelimetedParser(ParserInput(line2), ",").file.run().get(0) must contain allOf("ala", """asdf"ma"asdf""", "kota")
    }

  it should   "parse line with tabular separator" in {
      // given
      val line = "ala\tma\tkota"

      // when
      val csvLines = new PBDelimetedParser(ParserInput(line), "\t").file.run().get

      // then
      csvLines must  have size 1
      csvLines(0) must contain allOf("ala", "ma", "kota")
    }

  it should  "parse line with default separator" in {
      // given
      val line = "ala,ma,kota"

      // when
      val csvLines = new PBDelimetedParser(ParserInput(line), ",").file.run().get

    // then
      csvLines must  have size 1
      csvLines(0) must contain allOf("ala", "ma", "kota")
    }

}


