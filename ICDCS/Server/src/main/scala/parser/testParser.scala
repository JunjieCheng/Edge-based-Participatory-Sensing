package parser

import compiler.ServiceAnalyzer
import parser.MicroServiceDSL._

import scala.io.Source

object testParser {

  val filename: String = System.getProperty("user.dir") + "/src/main/scala/use_case/room.serv"
  val str: String = Source.fromFile(filename).getLines.mkString

  def main(args: Array[String]) {
    service(new lexical.Scanner(str)) match {
      case Success(service, _) =>
        println("Success\n" + service)
      case Failure(msg, _) => println("Failure: " + msg)
      case Error(msg, _) => println("Error: " + msg)
    }
  }
}
