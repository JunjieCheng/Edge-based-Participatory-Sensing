package interpreter

import parser.MOLE_AST.{MicroServiceSelect, ServiceObj}

class ControlFlowGraph(service:ServiceObj) {

  var globalParams: Array[String] = Array[String]()
  var microServiceGraph: MicroServiceGraph = new MicroServiceGraph()

  if (!service.details.getInputParameters.equals("")) {
    globalParams = service.details.getInputParameters.split("\\|")
  }

  def generateExecutionGraph(): Unit = {
    this.initVertices()
  }

  def initVertices(): Unit = {
    for (microService <- service.details.microServices.lis) {
      val microServiceId = microService.identity
      val microServiceParams = microService.params
      val basicMicroService = microService.basicMS
      val vertexId = microServiceGraph.addVertex(MicroServiceTask, microServiceId, microServiceParams, basicMicroService)

      for (statement <- microService.microServiceDescription.statements) {
        if (statement.isInstanceOf[MicroServiceSelect]) {

        }
      }
    }
  }
}
