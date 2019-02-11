package parser

import parser.MOLE_AST._

import scala.util.parsing.combinator.syntactical._

object MicroServiceDSL extends StandardTokenParsers {

  lexical.reserved += ("Service", "MS", "with", "global", "select", "set", "on", "return", "exit", "case")
  lexical.delimiters += ("{", "}", "[", "]", ".", ":", ";", "(", ")", ",", "=", "<", ">", "==")

  def service: Parser[ServiceObj] = service_id ~ service_description ^^ { case i ~ a => ServiceObj(i, a) }

  def service_id: Parser[ServiceIdentity] = "Service" ~> ident ^^ ServiceIdentity

  def service_description: Parser[ServiceDescription] = "{" ~> opt(service_params) ~ micro_services <~ "}" ^^ {
    case Some(input) ~ microServices => ServiceDescription(input, microServices)
    case None ~ microServices => ServiceDescription(null, microServices)
  }

  def service_params: Parser[ServiceParameters] = rep1("global" ~> "." ~> (service_param_array | service_param_value)) ^^ ServiceParameters

  def service_param_value: Parser[ServiceParameter] = ident ~ ("=" ~> (float | numericLit | stringLit)) ^^ {
    case k ~ v => ServiceParameter(k, v)
  }

  def service_param_array: Parser[ServiceParameter] = ident ~ ("=" ~> opt("[") ~> rep1sep(stringLit, ",") <~ opt("]")) ^^ {
    case k ~ loc => ServiceParameter(k, loc.mkString(","))
  }

  def micro_services: Parser[MicroServices] = rep1(micro_service) ^^ MicroServices

  def micro_service: Parser[MicroServiceObj] = "MS" ~> ":" ~> ident ~ ("(" ~> repsep(micro_service_parameter, ",") <~ ")") ~ ("with" ~> opt(ident <~ ".")) ~ ident ~ micro_service_description ^^ {
    case ms_name ~ params ~ Some(package_name) ~ extend_ms ~ description => MicroServiceObj(ms_name, params, package_name + "." + extend_ms, description)
    case ms_name ~ params ~ None ~ extend_ms ~ description => MicroServiceObj(ms_name, params, extend_ms, description)
  }

  def micro_service_parameter: Parser[MicroServiceParameter] = ident ~ ident ^^ { case k ~ v => MicroServiceParameter(k, v) }

  def micro_service_description: Parser[MicroServiceDescription] = "{" ~> rep(micro_service_select | micro_service_set
    | micro_service_on) <~ "}" ^^ MicroServiceDescription

  def micro_service_select: Parser[MicroServiceSelect] = "select" ~> "." ~>  opt(ident <~ ".") ~ ident ~ ("=" ~> stringLit) ^^ {
    case Some(extension) ~ k ~ v => MicroServiceSelect(extension + "." + k, v)
    case None ~ k ~ v => MicroServiceSelect(k, v)
  }

  def micro_service_set: Parser[MicroServiceSet] = "set" ~> "." ~> opt(ident <~ ".") ~ ident ~ ("=" ~> (stringLit | float | numericLit)) ^^ {
    case Some(extension) ~ k ~ v => MicroServiceSet(extension + "." + k, v)
    case None ~ k ~ v => MicroServiceSet(k, v)
  }

  def micro_service_on: Parser[MicroServiceOn] = "on" ~> "." ~> ident ~ (":" ~> (micro_service_on_redirection |
    micro_service_on_return | micro_service_on_exit | micro_service_on_case)) ^^ {
    case condition ~ operation => MicroServiceOn(condition, operation)
  }

  def micro_service_on_case: Parser[MicroServiceOnCase] = "{" ~> rep1(micro_service_on_case_detail) <~ "}" ^^ MicroServiceOnCase

  def micro_service_on_case_detail: Parser[MicroServiceOnCaseDetail]  = "case" ~> (micro_service_expression_greater |
    micro_service_expression_less | micro_service_expression_equal | ident) ~ (":" ~> (micro_service_on_redirection |
    micro_service_on_return | micro_service_on_exit)) ^^ {
    case condition ~ operation => MicroServiceOnCaseDetail(condition, operation)
  }

  def micro_service_on_exit: Parser[MicroServiceOnExit] = ident <~ ":" <~ "exit" ^^ MicroServiceOnExit

  def micro_service_on_return: Parser[MicroServiceOnReturn] = "return" ~> rep1sep(micro_service_parameter, ",") ^^ MicroServiceOnReturn

  def micro_service_on_redirection: Parser[MicroServiceOnRedirection] = ident ~ ("(" ~> repsep(micro_service_parameter, ",") <~ ")") ^^ {
    case ms_name ~ params => MicroServiceOnRedirection(ms_name, params)
  }

  def float: Parser[String] = numericLit ~ ("." ~> numericLit) ^^ {
    case v1 ~ v2 => v1 + "." + v2
  }

  def micro_service_expression_greater: Parser[String] = micro_service_attribute ~ (">" ~> (float | numericLit)) ^^ {
    case i1 ~ i2 => i1 + ">" + i2
  }

  def micro_service_expression_less: Parser[String] = micro_service_attribute ~ ("<" ~> (float | numericLit)) ^^ {
    case i1 ~ i2 => i1 + "<" + i2
  }

  def micro_service_expression_equal: Parser[String] = micro_service_attribute ~ ("==" ~> (float | numericLit | ident)) ^^ {
    case i1 ~ i2 => i1 + "==" + i2
  }

  def micro_service_attribute: Parser[String] = ident ~ ("." ~> ident) ^^ {
    case i1 ~ i2 => i1 + "." + i2
  }
}
