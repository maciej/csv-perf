package com.softwaremill.csvperf

import org.parboiled2.Parser.DeliveryScheme
import org.parboiled2._

import scala.util.Try

case class CsvFile(records: Seq[Record])
case class Record(fields: Seq[String])

object CsvFile {
  def fromRecords(records: Record*): CsvFile = CsvFile(records)
}

object Record {
  def fromFields(fields: String*): Record = Record(fields)
}

case class PBParser2(input: ParserInput, delimiter: Char) extends Parser {

  def DQUOTE = '"'
  def DELIMITER_TOKEN = rule(capture(delimiter))

  // combine 2 dquotes into 1
  def DQUOTE2 = rule("\"\"" ~ push("\""))
  def CRLF = rule(capture("\n\r" | "\n"))
  def NON_CAPTURING_CRLF = rule("\n\r" | "\n")

  val delims = s"$delimiter\r\n" + DQUOTE
  def TXT = rule(capture(!anyOf(delims) ~ ANY))
  val WHITESPACE = CharPredicate(" \t")
  def SPACES: Rule0 = rule(oneOrMore(WHITESPACE))

  def escaped = rule(optional(SPACES) ~ DQUOTE ~ zeroOrMore(DELIMITER_TOKEN | TXT | CRLF | DQUOTE2)
    ~ DQUOTE ~ optional(SPACES) ~> (_.mkString("")))
  def nonEscaped = rule(zeroOrMore(TXT | capture(DQUOTE)) ~> (_.mkString("")))

  def field = rule(escaped | nonEscaped)
  def row = rule {oneOrMore(field).separatedBy(delimiter) ~> Record.apply _}
  def file = rule {zeroOrMore(row).separatedBy(NON_CAPTURING_CRLF) ~> CsvFile.apply _}

  def parse(): Try[CsvFile] = file.run()

  def parseThrowing(): CsvFile = file.run()(DeliveryScheme.Throw)

}
