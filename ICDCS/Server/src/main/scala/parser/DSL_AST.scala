package parser

object DSL_AST {

  trait ServiceParameter

  trait MicroserviceParameter

  trait MicroserviceAfterExecutionAction

  // Service
  case class ServiceObj(serviceParameters: ServiceParameters, microservices: Microservices) {
    override def toString: String = {
      serviceParameters + "\n" + microservices
    }
  }

  // Global input
  case class ServiceParameters(serviceParameters: List[ServiceParameter]) {
    override def toString: String = {
      serviceParameters.mkString("|")
    }

    def getParameters(): String = {
      if (serviceParameters == null) {
        ""
      } else {
        serviceParameters.mkString("|")
      }
    }
  }

  case class ServiceTaskName(serviceTaskName: String) extends ServiceParameter {
    override def toString: String = {
      "task_name: " + serviceTaskName
    }
  }

  case class ServiceExpiration(serviceExpiration: String) extends ServiceParameter {
    override def toString: String = {
      "expiration: " + serviceExpiration
    }
  }

  case class ServiceBudget(serviceBudget: String) extends ServiceParameter {
    override def toString: String = {
      "budget: " + serviceBudget
    }
  }

  case class ServiceLocation(serviceLocation: String) extends ServiceParameter {
    override def toString: String = {
      "location: " + serviceLocation
    }
  }

  case class ServiceSynchronization(serviceSynchronization: String) extends ServiceParameter {
    override def toString: String = {
      "synchronization: " + serviceSynchronization
    }
  }

  case class ServiceIncentive(serviceIncentive: String) extends ServiceParameter {
    override def toString: String = {
      "incentive: " + serviceIncentive
    }
  }

  case class ServiceThreshold(serviceThreshold: String) extends ServiceParameter {
    override def toString: String = {
      "threshold: " + serviceThreshold
    }
  }

  // Microservice
  case class Microservices(microservices: List[MicroserviceObj]) {
    override def toString: String = {
      microservices.mkString("\n")
    }
  }

  case class MicroserviceObj(identity: String, microserviceParameters: MicroserviceParameters)

  // Microservice parameters
  case class MicroserviceParameters(microserviceParameters: List[MicroserviceParameter])

  case class MicroserviceDevice(microserviceDevice: String, microserviceParticipant: String) extends MicroserviceParameter {
    override def toString: String = {
      "device: " + microserviceDevice + " " + "participant: " + microserviceParticipant
    }
  }

  case class MicroserviceInstruction(microserviceInstruction: String) extends MicroserviceParameter {
    override def toString: String = {
      "instruction: " + microserviceInstruction
    }
  }

  case class MicroserviceShow(microserviceShow: String) extends MicroserviceParameter {
    override def toString: String = {
      "show: " + microserviceShow
    }
  }

  case class MicroserviceInput(microserviceInput: List[String]) extends MicroserviceParameter {
    override def toString: String = {
      "input: [" + microserviceInput.mkString(", ") + "]"
    }
  }

  case class MicroserviceResult(microserviceResult: List[String]) extends MicroserviceParameter {
    override def toString: String = {
      "result: [" + microserviceResult.mkString(", ") + "]"
    }
  }

  case class MicroserviceOnSuccess(microserviceOnSuccess: String) extends MicroserviceParameter {
    override def toString: String = {
      "on_success: " + microserviceOnSuccess
    }
  }

  case class MicroserviceOnSuccessActions(microserviceOnSuccessActions: List[MicroserviceAfterExecutionAction]) extends MicroserviceParameter {
    override def toString: String = {
      "on_success: {" + microserviceOnSuccessActions.mkString(", ") + "}"
    }
  }

  case class MicroserviceOnSuccessActionConfirmed(microserviceOnSuccessActionConfirmed: String) extends MicroserviceAfterExecutionAction {
    override def toString: String = {
      "confirmed: " + microserviceOnSuccessActionConfirmed
    }
  }

  case class MicroserviceOnSuccessActionRefuted(microserviceOnSuccessActionRefuted: String) extends MicroserviceAfterExecutionAction {
    override def toString: String = {
      "refuted: " + microserviceOnSuccessActionRefuted
    }
  }

  case class MicroserviceOnSuccessActionNotEqual(firstVariable: String, secondVariable: String, action: String) extends MicroserviceAfterExecutionAction {
    override def toString: String = {
      firstVariable + " != " + secondVariable + ": " + action
    }
  }

}
