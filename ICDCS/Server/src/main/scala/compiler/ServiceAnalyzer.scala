package compiler

import parser.DSL_AST

class ServiceAnalyzer(service: DSL_AST.ServiceObj) {

  var serviceParameters: Array[String] = Array[String]()

  var task_name: String = ""
  var expiration: String = ""
  var budget: String = ""
  var location: String = ""
  var synchronization: String = ""
  var incentive: String = ""
  var threshold: String = ""

  if (!service.serviceParameters.getParameters().equals("")) {
    serviceParameters = service.serviceParameters.toString().split("\\|")
  }

  for (parameter <- serviceParameters) {
    val pair = parameter.split(": ")

    pair(0) match {
      case "task_name" => task_name = pair(1)
      case "expiration" => expiration = pair(1)
      case "budget" => budget = pair(1)
      case "location" => location = pair(1)
      case "synchronization" => synchronization = pair(1)
      case "incentive" => incentive = pair(1)
      case "threshold" => threshold = pair(1)
    }
  }
}
