package com.softwaremill.csvperf

import scala.language.postfixOps

import scala.util.parsing.combinator._

/**
 * Based on http://stackoverflow.com/questions/5063022/use-scala-parser-combinator-to-parse-csv-files
 */
trait SCDelimitedParser extends RegexParsers {
  override val skipWhitespace = false // meaningful spaces in CSV

  def delimiter = ","

  def RECORD_SEPARATOR = "\r\n" | "\n"

  def DQUOTE = "\""

  def DQUOTE2 = "\"\"" ^^ { case _ => "\""}

  // combine 2 dquotes into 1
  def TXT = ("[^\"\r\n" + delimiter + "]").r

  def SPACES = "[ \t]+".r

  def COMMENT_START = "#"

  def file: Parser[List[List[String]]] = repsep(record, RECORD_SEPARATOR) <~ (RECORD_SEPARATOR ?) ^^ {
    case l => l.filterNot(_ == Nil)
  }

  def comment: Parser[List[String]] = (COMMENT_START ~ ("[^\r\n]".r *)) ^^ {
    case _ => Nil
  }

  def escaped: Parser[String] = {
    ((SPACES ?) ~> DQUOTE ~> ((TXT | delimiter | RECORD_SEPARATOR | DQUOTE2) *) <~ DQUOTE <~ (SPACES ?)) ^^ {
      case ls => ls.mkString("")
    }
  }

  def nonescaped: Parser[String] = (TXT *) ^^ { case ls => ls.mkString("")}

  def field: Parser[String] = escaped | nonescaped

  def record: Parser[List[String]] = comment | repsep(field, delimiter)

  private def resultHandler[T](result: ParseResult[T]) = result match {
    case Success(res, _) => res
    case e => throw new Exception(e.toString)
  }

  def parseFile(s: String) = resultHandler(parseAll(file, s))

  def parseLine(s: String) = resultHandler(parse(record, s))
}

class CustomSepDelimitedParser(sep : String) extends SCDelimitedParser {
  override def delimiter = sep
}

object CommaSCDelimitedParser extends SCDelimitedParser

object PipedSCDelimitedParser extends SCDelimitedParser {
  override def delimiter = "|"
}
