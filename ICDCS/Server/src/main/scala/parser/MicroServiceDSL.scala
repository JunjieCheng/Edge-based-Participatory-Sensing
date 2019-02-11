package parser

import parser.DSL_AST._

import scala.util.parsing.combinator.syntactical.{StandardTokenParsers, _}

object MicroServiceDSL extends StandardTokenParsers {

  lexical.reserved += ("Service", "Microservices", "task_name", "expiration", "budget", "location", "synchronization",
    "incentive", "threshold", "device", "instruction", "show", "input", "result", "on_success", "on_failure", "confirmed",
    "refuted", "hour", "minute", "second", "return")

  lexical.delimiters += ("{", "}", ":", "[", "]", ",", "-", "!=")

  def service: Parser[ServiceObj] = "Service" ~> ":" ~> "{" ~> service_parameters ~ microservices <~ "}" ^^ { case params ~ ms => ServiceObj(params, ms) }

  // Global
  def service_parameters: Parser[ServiceParameters] = rep((service_task_name | service_expiration | service_budget |
    service_location | service_synchronization | service_incentive | service_threshold) <~ ",") ^^ ServiceParameters

  def service_task_name: Parser[ServiceTaskName] = "task_name" ~> ":" ~> stringLit ^^ ServiceTaskName

  def service_expiration: Parser[ServiceExpiration] = "expiration" ~> ":" ~> date ^^ ServiceExpiration

  def service_budget: Parser[ServiceBudget] = "budget" ~> ":" ~> numericLit ^^ ServiceBudget

  def service_location: Parser[ServiceLocation] = "location" ~> ":" ~> stringLit ^^ ServiceLocation

  def service_synchronization: Parser[ServiceSynchronization] = "synchronization" ~> ":" ~> numericLit ^^ ServiceSynchronization

  def service_incentive: Parser[ServiceIncentive] = "incentive" ~> ":" ~> ident ^^ ServiceIncentive

  def service_threshold: Parser[ServiceThreshold] = "threshold" ~> ":" ~> numericLit ^^ ServiceThreshold

  // Microservice
  def microservices: Parser[Microservices] = "Microservices" ~> ":" ~> "{" ~> rep1(microservice <~ opt(",")) <~ "}" ^^ Microservices

  def microservice: Parser[MicroserviceObj] = (ident <~ ":" <~ "{") ~ microservice_parameters <~ "}" ^^ {
    case i ~ p => MicroserviceObj(i, p)
  }

  def microservice_parameters: Parser[MicroserviceParameters] = rep((microservice_device | microservice_instruction |
    microservice_show | microservice_input | microservice_result | microservice_on_success | microservice_on_success_actions
    ) <~ opt(",")) ^^ MicroserviceParameters

  def microservice_device: Parser[MicroserviceDevice] = "device" ~> ":" ~> ident ~ opt(ident) ^^ {
    case device ~ None => MicroserviceDevice(device, null)
    case device ~ Some(participant) => MicroserviceDevice(device, participant)
  }

  def microservice_instruction: Parser[MicroserviceInstruction] = "instruction" ~> ":" ~> stringLit ^^ MicroserviceInstruction

  def microservice_show: Parser[MicroserviceShow] = "show" ~> ":" ~> ident ^^ MicroserviceShow

  def microservice_input: Parser[MicroserviceInput] = "input" ~> ":" ~> opt("[") ~> rep1(ident <~ opt(",")) <~ opt("]") ^^ MicroserviceInput

  def microservice_result: Parser[MicroserviceResult] = "result" ~> ":" ~> opt("[") ~> rep1(ident <~ opt(",")) <~ opt("]") ^^ MicroserviceResult

  def microservice_on_success: Parser[MicroserviceOnSuccess] = "on_success" ~> ":" ~> ident ^^ MicroserviceOnSuccess

  def microservice_on_success_actions: Parser[MicroserviceOnSuccessActions] = "on_success" ~> ":" ~> "{" ~>
    rep((microservice_on_success_action_confirmed | microservice_on_success_action_refuted | microservice_on_success_action_not_equal)
      <~ opt(",")) <~ "}" ^^ MicroserviceOnSuccessActions

  def microservice_on_success_action_confirmed: Parser[MicroserviceOnSuccessActionConfirmed] = "confirmed" ~> ":" ~> ident ^^ MicroserviceOnSuccessActionConfirmed

  def microservice_on_success_action_refuted: Parser[MicroserviceOnSuccessActionRefuted] = "refuted" ~> ":" ~> ident ^^ MicroserviceOnSuccessActionRefuted

  def microservice_on_success_action_not_equal: Parser[MicroserviceOnSuccessActionNotEqual] = ident ~ ("!=" ~> ident) ~ (":" ~> ident) ^^ {
    case first ~ second ~ action => MicroserviceOnSuccessActionNotEqual(first, second, action)
  }

  //  def microservice_on_success_action_refuted:

  // Helper
  def date: Parser[String] = numericLit ~ ("-" ~> numericLit) ~ ("-" ~> numericLit) ~ numericLit ~ (":" ~> numericLit) ~ (":" ~> numericLit) ^^ {
    case year ~ month ~ day ~ hour ~ min ~ sec => year + "-" + month + "-" + day + " " + hour + ":" + min + ":" + sec
  }
}
