package parser

object MOLE_AST {

  case class ServiceObj(serviceId:ServiceIdentity, details:ServiceDescription)

  case class ServiceIdentity(serviceId:String)

  case class ServiceDescription(inputs:ServiceParameters, microServices:MicroServices) {
    def getInputParameters:String = {
      if(inputs == null){
        ""
      }
      else{
        inputs.toString
      }
    }
  }

  case class ServiceParameters(params:List[ServiceParameter]) {
    override def toString:String = {
      params.mkString("|")
    }
  }

  case class ServiceParameter(inputKey:String, inputValue:String) {
    override def toString: String = {
      inputKey + "=" + inputValue
    }
  }

  case class MicroServices(lis:Seq[MicroServiceObj]) {
    override def toString: String = {
      lis.mkString("\n")
    }
  }

  case class MicroServiceObj(identity:String, params:List[MicroServiceParameter], basicMS:String, microServiceDescription:MicroServiceDescription) {
    override def toString:String = {
      identity + params + " with " + basicMS + ", " + microServiceDescription
    }
  }

  case class MicroServiceParameter(dataType:String, name:String) {
    override def toString: String = {
      dataType + " " + name
    }
  }

  case class MicroServiceDescription(statements:Seq[MicroServiceRule])

  trait MicroServiceRule
  trait MicroServiceOnRule

  case class MicroServiceSelect(key:String, value:String) extends MicroServiceRule {
    override def toString: String = {
      "select" + "." + key + "=" + value
    }
  }

  case class MicroServiceSet(key:String, value:String) extends MicroServiceRule {
    override def toString: String = {
      "set" + "." + key + "=" + value
    }
  }

  case class MicroServiceOn(condition:String, value:MicroServiceOnRule) extends MicroServiceRule {
    override def toString: String = {
      "on" + "." + condition + ":" + value.toString
    }
  }

  case class MicroServiceOnCase(details:List[MicroServiceOnCaseDetail]) extends MicroServiceOnRule

  case class MicroServiceOnCaseDetail(condition:String, operation:MicroServiceOnRule) {
    override def toString: String = {
      condition + ":" + operation.toString
    }
  }

  case class MicroServiceOnReturn(operation:List[MicroServiceParameter]) extends MicroServiceOnRule {
    override def toString: String = {
      "return " + operation.mkString(", ")
    }
  }

  case class MicroServiceOnExit(operation:String) extends MicroServiceOnRule {
    override def toString: String = {
      operation
    }
  }

  case class MicroServiceOnRedirection(msName:String, params:List[MicroServiceParameter]) extends MicroServiceOnRule {
    override def toString: String = {
      msName + "(" + params.toString() + ")"
    }
  }
}
