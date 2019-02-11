package parser

import interpreter.ControlFlowGraph
import parser.MicroServiceDSL._

import scala.io.Source

object testDSL {

  val filename = System.getProperty("user.dir")+"/src/main/scala/resources/digitRecognition.serv"
  val str = Source.fromFile(filename).getLines.mkString

  def main(args: Array[String]) {
    service(new lexical.Scanner(str)) match {
      case Success(service, _) => {
        println("Success " + service.details)

        val serviceGraph = new ControlFlowGraph(service)

        serviceGraph.generateExecutionGraph()
      }
      case Failure(msg, _) => println("Failure:"+msg)
      case Error(msg, _) => println("Error"+msg)
    }
  }
}
