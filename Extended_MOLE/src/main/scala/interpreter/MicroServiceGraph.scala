package interpreter

import parser.MOLE_AST.MicroServiceParameter


trait VertexType{
  def vertexType:String
}

case object MicroServiceTask extends VertexType { val vertexType = "microService" }
case object StartExecution extends VertexType { val vertexType = "startExecution" }
case object SuccessEndExecution extends VertexType { val vertexType = "SuccessEndExecution" }
case object FailureEndExecution extends VertexType { val vertexType = "FailureEndExecution" }

class MicroServiceGraph {

  var controlFlowGraph = Topology.empty[Vertex, Edge]

  def addVertex(vertexType: VertexType, microServiceId: String, microServiceParams: List[MicroServiceParameter], basicMicroService: String): Int = {
    val vertexId = controlFlowGraph.vertices.size + 1

    controlFlowGraph = controlFlowGraph.addVertex(Vertex(vertexType, microServiceId, vertexId, microServiceParams, basicMicroService))

    vertexId
  }
}

case class Vertex(vertexType: VertexType, microServiceId: String, vertexId: Int, var params: List[MicroServiceParameter], basicMicroService: String) {

}

case class Edge(sourceVertex: Vertex, targetVertex: Vertex) extends Topology.Edge[Vertex]