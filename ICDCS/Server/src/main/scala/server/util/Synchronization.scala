package server.util

object Synchronization {

  def distributeJob(routers: List[String], task: String, budgets: Map[String, Double], threshold: String,
                    expiration: String): Unit = {
    for (router <- routers) {
      val params: Map[String, String] = Map("new_task" -> task, "budget" -> budgets(router).toString,
        "threshold" -> threshold, "expiration" -> expiration)
      HTTPClient.get(router, params)
    }
  }

  // Data synchronization contains all accepted bids
  // Format of HTTP body: [target1,target2,...]
  def synchronizeData(routers: List[String], taskName: String): Unit = {
    val routerTargets: scala.collection.mutable.Map[String, List[String]] = scala.collection.mutable.Map()
    val allTargets: scala.collection.mutable.Set[String] = scala.collection.mutable.Set()
    var params: Map[String, String] = Map("synchronization_upload" -> taskName)

    // Upload data
    for (router <- routers) {
      val response: String = HTTPClient.get(router, params)
      val targets: Array[String] = response.split(",")
      routerTargets(router) = targets.toList.filter(_ != "")
      allTargets ++= targets.filter(_ != "")
    }

    // Synchronize data
    for (router <- routers) {
      val result: String = allTargets.diff(routerTargets(router).toSet).mkString(",")
      params = Map("synchronization_download" -> taskName, "data" -> result)
      HTTPClient.get(router, params)
    }
  }

}
