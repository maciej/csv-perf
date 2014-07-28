package com.softwaremill.csvperf

import org.scalatest.{MustMatchers, FlatSpec, ShouldMatchers}


class CsvSpec extends FlatSpec with MustMatchers {

  it should "not fail when quotes are inside a field" in {
      // given
      val line1 = """ala,("ma"),kota"""
      val line2 = """ala,asdf"ma"asdf,kota"""

      // when & then
      SimpleCsvParser.fromString(line1)(0) must contain allOf("ala", """("ma")""", "kota")
      SimpleCsvParser.fromString(line2)(0) must contain allOf("ala", """asdf"ma"asdf""", "kota")
    }

  it should   "parse line with tabular separator" in {
      // given
      val line = "ala\tma\tkota"

      // when
      val csvLines = SimpleCsvParser.fromString(line, '\t')

      // then
      csvLines must  have size 1
      csvLines(0) must contain allOf("ala", "ma", "kota")
    }

  it should  "parse line with default separator" in {
      // given
      val line = "ala,ma,kota"

      // when
      val csvLines = SimpleCsvParser.fromString(line)

      // then
      csvLines must  have size 1
      csvLines(0) must contain allOf("ala", "ma", "kota")
    }

  it should   "parse line with quotes in field and default separator and remove quotes" in {
      // given
      val line = "ala,\"ma\",kota"

      // when
      val csvLines = SimpleCsvParser.fromString(line)

      // then
      csvLines must  have size 1
      csvLines(0) must contain allOf("ala", "ma", "kota")
    }

  it should   "accept json content in field" in {
      // given
      val line = """a,b,{"a":"asd","b":"asd"},po"""

      // when
      val csvLines = SimpleCsvParser.fromString(line)

      csvLines must have size 1
      csvLines(0) must contain allOf("a", "b", "po", """{"a":"asd","b":"asd"}""")
    }

  it should   "accept user-agent string with curly braces and prepended by json field" in {
      // given
      val line = """"{""retarget"":""E""}",4,Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SIMBAR={66452C55-2A96-475D-BD1C-F0A0C078F4CB}"""

      // when
      val csvLines = SimpleCsvParser.fromString(line)

      csvLines must  have size 1
      csvLines(0) must contain allOf("""{"retarget":"E"}""", "4", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SIMBAR={66452C55-2A96-475D-BD1C-F0A0C078F4CB}")
    }

  it should   "accept curly brace inside a field" in {
      // given
      val line = "a,b,case{asd,c"

      // when
      val csvLines = SimpleCsvParser.fromString(line)

      // then
      csvLines must  have size 1
      csvLines(0) must contain allOf("a", "b", "c", "case{asd")
    }

}


