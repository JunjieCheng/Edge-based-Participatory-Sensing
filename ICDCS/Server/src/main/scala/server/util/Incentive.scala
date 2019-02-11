package server.util

import server.model.Data

import scala.collection.mutable.ListBuffer

object Incentive {

  def calculateBudget(routers: List[String], prices: Map[String, ListBuffer[Data]], totalBudget: Double, incentiveInterval: Int,
                      expiration: Long, threshold: Double, first: Boolean): Map[String, Double] = {

    val budgets: scala.collection.mutable.Map[String, Double] = scala.collection.mutable.Map()

    if (first) {
      val subBudget: Double = totalBudget * (incentiveInterval.asInstanceOf[Double] / (expiration - System.currentTimeMillis())) / routers.size

      for (router <- routers) {
        budgets(router) = subBudget
      }
    } else {
      var bidCount: Int = 0
      val counter: scala.collection.mutable.Map[String, Int] = scala.collection.mutable.Map()

      // Count accepted bids
      for (router <- routers) {
        budgets(router) = 0
        counter(router) = 0

        for (data <- prices(router)) {
          if (data.price <= threshold) {
            counter(router) += 1
            bidCount += 1
          }
        }
      }

      // Give the router 1 count if it hasn't collect any data
      for (router <- routers) {
        if (budgets(router) == 0) {
          counter(router) += 1
          bidCount += 1
        }
      }

      val subBudget: Double = totalBudget * (incentiveInterval.toDouble / (expiration - System.currentTimeMillis())) / bidCount

      println("prices -> " + prices.toString)
      println("subBudget -> " + subBudget)
      println("totalCount -> " + bidCount)
      println(counter)

      for (router <- routers) {
        budgets(router) = subBudget * counter(router)
      }
    }

    budgets.toMap
  }

  def requestIncentiveData(routers: List[String], taskName: String, prices: scala.collection.mutable.Map[String, ListBuffer[Data]]): Double = {
    var remainingBudget: Double = 0

    for (router <- routers) {
      val params: Map[String, String] = Map("incentive_upload" -> taskName)
      val response = HTTPClient.get(router, params)

      // response format: remaining budget,price1|acc,price2|rej,price3|acc,...
      val data: Array[String] = response.split(",")
      remainingBudget += data(0).toDouble

      for ((price, i) <- data.view.zipWithIndex) {
        if (i != 0) {
          val pair: Array[String] = price.split("\\|")
          val accepted: Boolean = if (pair(1) == "acc") true else false
          prices(router) += Data("", pair(0).toDouble, accepted, "")
        }
      }
    }

    remainingBudget
  }

  def calculateThreshold(routers: List[String], prices: Map[String, ListBuffer[Data]], totalBudget: Double, incentiveInterval: Int,
                         expiration: Long): Double = {
    val selectedBids: ListBuffer[Data] = new ListBuffer[Data]()
    val bids: ListBuffer[Data] = new ListBuffer[Data]()
    val currentBudget: Double = totalBudget * (incentiveInterval.toDouble / (expiration - System.currentTimeMillis()))

    for (router <- routers) {
      bids ++= prices(router)
    }

    bids.sortBy(_.price)

    for (bid <- bids) {
      if (bid.price <= currentBudget / (selectedBids.size + 1)) {
        selectedBids += bid
      }
    }

    1 / (Math.max(0.01, selectedBids.size) / currentBudget / 1.5)
  }

  def distributeIncentive(routers: List[String], taskName: String, budgets: Map[String, Double], threshold: Double): Unit = {
    for (router <- routers) {
      val params: Map[String, String] = Map("incentive_download" -> taskName, "budget" -> budgets(router).toString,
        "threshold" -> threshold.toString)
      HTTPClient.get(router, params)
    }
  }
}
