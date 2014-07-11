package com.softwaremill.csvperf

import com.softwaremill.thegarden.lawn.io.Resources
import org.parboiled2.ParserInput
import org.scalameter.api._

object ScalaMeterPerfTest extends PerformanceTest.Quickbenchmark {

  override def reporter = super.reporter

  private lazy val ratings1k = InputData.fromResource("ratings-1k.csv")
  private lazy val ratings10k = InputData.fromResource("ratings-10k.csv")
  private lazy val ratings10M = InputData.fromResource("ratings-10M.csv")

  val parserRun = new ScalaUtilParsersRun(ratings1k)

  private val smallestFile = Gen.single("Smallest file")(ratings1k)
  private val smallFiles = Gen.enumeration("Small files")(ratings1k, ratings10k)

  performance of "Scala Utils CSV parser" in {
    using(smallFiles) in { data =>
      new ScalaUtilParsersRun(data).run()
    }
  }

  performance of "Scala Utils with splitter CSV parser" in {
    using(smallFiles) in { data =>
      new ScalaUtilParserWithSplitAndJoinRun(data).run()
    }
  }

  performance of "Scala Utils with splitter and par CSV parser" in {
    using(smallFiles) in { data =>
      new ScalaUtilParserWithSplitAndJoinRun(data).run()
    }
  }

  performance of "Parboiled2 CSV parser" in {
    using(smallFiles) in { data =>
      new ParboiledParserRun(data).run()
    }
  }

}

case class InputData(data: String, desc: String) {
  override def toString = desc
}

object InputData {
  def fromResource(resourcePath: String) = InputData(Resources.readToString(resourcePath), resourcePath)
}

trait ParserTestRun {
  protected val inputData: InputData
  protected val parseFun: (String => Seq[Seq[String]])

  def run(): Seq[Seq[String]] = parseFun(inputData.data)
}

class ScalaUtilParsersRun(protected val inputData: InputData) extends ParserTestRun {

  override protected val parseFun: (String) => Seq[Seq[String]] = s => CommaSCDelimitedParser.parseFile(s)

}

class ScalaUtilParserWithSplitAndJoinRun(protected val inputData: InputData) extends ParserTestRun {

  override protected val parseFun: (String) => Seq[Seq[String]] = { data =>
    data.split("\n").map(line => CommaSCDelimitedParser.parseLine(line))
  }
}

class ParboiledParserRun(protected val inputData: InputData) extends ParserTestRun {

  override protected val parseFun : (String) => Seq[Seq[String]] =
    s => new PBDelimetedParser(ParserInput(s), ",").file.run().getOrElse(throw new RuntimeException)

}

