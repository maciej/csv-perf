package com.softwaremill.csvperf

import org.parboiled2.ParserInput
import org.scalatest.{FlatSpec, ShouldMatchers}

class PBDelimitedParserSpec extends FlatSpec with ShouldMatchers {
  val MultilineTabSeparatedFile = "a|b|c\nc|c|c"

  def toCsvFile(input: ParserInput, delimiter: Char = ','): CsvFile = PBParser2(input, delimiter).parseThrowing()

  it should "parse multiline strings" in {
    toCsvFile(MultilineTabSeparatedFile, '|') shouldEqual
      CsvFile.fromRecords(Record.fromFields("a", "b", "c"), Record.fromFields("c", "c", "c"))
  }

  def firstRowFieldsOf(rawCsv: String) = toCsvFile(rawCsv).records.head.fields

  Seq(
    ( """ala,("ma"),kota""", Seq("ala", """("ma")""", "kota")),
    ( """ala,asdf"ma"asdf,kota""", Seq("ala", """asdf"ma"asdf""", "kota"))
  ).foreach { case (rawCsv, expectedElements) =>
    it should s"not fail when quotes are inside a field, in: $rawCsv" in {
      firstRowFieldsOf(rawCsv) shouldEqual expectedElements
    }
  }

  it should "parse line with tabular separator" in {
    toCsvFile("ala\tma\tkota", '\t').shouldEqual(CsvFile.fromRecords(Record.fromFields("ala", "ma", "kota")))
  }

  it should "parse line with default separator" in {
    toCsvFile("ala,ma,kota") shouldEqual CsvFile.fromRecords(Record.fromFields("ala", "ma", "kota"))
  }

  it should "correctly parse lines with escaped fields with delimiters" in {
    toCsvFile( """"field1","field2","field3 with , before to end","field4"""").records.head.fields.length shouldEqual 4
  }

  it should "correctly parse the example provided by Toby at StackOverflow" in {
    toCsvFile( """"a,b", "c"""") shouldEqual CsvFile.fromRecords(Record.fromFields("a,b", "c"))
  }
}
