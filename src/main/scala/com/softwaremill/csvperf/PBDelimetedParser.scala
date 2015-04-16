package com.softwaremill.csvperf

import org.parboiled2._

import scala.util.Try

// https://github.com/sirthias/parboiled2/issues/61
case class PBDelimetedParser(input: ParserInput, delimeter: String) extends Parser {

  def DQUOTE = '"'
  def DELIMITER_TOKEN = rule(capture(delimeter))

  // combine 2 dquotes into 1
  def DQUOTE2 = rule("\"\"" ~ push("\""))
  def CRLF = rule(capture("\n\r" | "\n"))
  def NON_CAPTURING_CRLF = rule("\n\r" | "\n")

  val delims = s"$delimeter\r\n" + DQUOTE
  def TXT = rule(capture(!anyOf(delims) ~ ANY))
  val WHITESPACE = CharPredicate(" \t")
  def SPACES: Rule0 = rule(oneOrMore(WHITESPACE))

  def escaped = rule(optional(SPACES) ~ DQUOTE ~ zeroOrMore(DELIMITER_TOKEN | TXT | CRLF | DQUOTE2)
    ~ DQUOTE ~ optional(SPACES) ~> (_.mkString("")))
  def nonEscaped = rule(zeroOrMore(TXT | capture(DQUOTE)) ~> (_.mkString("")))

  def field = rule(escaped | nonEscaped)
  def row: Rule1[Seq[String]] = rule(oneOrMore(field).separatedBy(delimeter))
  def file = rule(zeroOrMore(row).separatedBy(NON_CAPTURING_CRLF))

  def parsed(): Try[Seq[Seq[String]]] = file.run()

}
