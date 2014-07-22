package com.softwaremill.csvperf

import java.lang.StringBuilder
import collection.immutable.Vector
import collection.immutable.VectorBuilder

object SimpleCsvParser {

  val INITIAL_STATE = 0
  val INSIDE_STRING = 1
  val QUOTE_HANDLING = 2
  val LINE_END = 3
  val INSIDE_JSON = 4
  val NORMAL_FIELD = 5

  private[this] sealed class Parser(source: String, sep: Char) {

    var line = 1
    var state = INITIAL_STATE
    var rows = new VectorBuilder[Vector[String]]
    var row = new VectorBuilder[String]
    var rowSize = 0
    var field = new StringBuilder

    def endField(): Unit = {
      row += field.toString
      rowSize += 1
      field = new StringBuilder
    }

    def endLine(): Unit = {
      rows += row.result
      row = new VectorBuilder[String]
      rowSize = 0
      line += 1
    }

    def doStateZero(c: Char): Unit = {
      c match {
        case '"' => state = INSIDE_STRING
        case '\n' => endField(); endLine()
        case '\r' => state = LINE_END
        case '{' => state = INSIDE_JSON; field.append(c)
        case _ => if (c == sep) endField() else { field.append(c); state = NORMAL_FIELD}
      }
    }

    def parse(): Vector[Vector[String]] = {
      source.foreach {
        c =>
          state match {
            case INITIAL_STATE => // Initial state.
              doStateZero(c)
            case INSIDE_STRING => // Inside a string.
              c match {
                case '"' => state = QUOTE_HANDLING
                case _ => field.append(c)
              }
            case QUOTE_HANDLING => // Did we match one double-quote, or two?
              c match {
                case '"' => field.append('"'); state = INSIDE_STRING
                case '\n' | '\r' => state = INITIAL_STATE; doStateZero(c)
                case _ => if (c != sep) throw new IllegalStateException(s"Failed to parse field $field at line $line"); state = INITIAL_STATE; doStateZero(c)
              }
            case LINE_END => // Have '\r', squash following '\n', if any.
              endField(); endLine(); state = INITIAL_STATE; if (c != '\n') doStateZero(c)
            case INSIDE_JSON =>
              c match {
                case '}' => state = INITIAL_STATE; field.append(c)
                case _ => field.append(c)
              }
            case NORMAL_FIELD =>
              c match {
                case _ => if (c == sep) { endField(); state = INITIAL_STATE } else field.append(c)
              }
          }
      }
      state match {
        case INITIAL_STATE => if (field.length > 0 || rowSize > 0) {
          endField(); endLine()
        }
        case QUOTE_HANDLING | INSIDE_JSON | NORMAL_FIELD => endField(); endLine()
      }
      rows.result()
    }
  }

  def fromString(source: String, sep: Char = ','): Vector[Vector[String]] = {
    val parser = new Parser(source, sep)
    parser.parse()
  }
}
