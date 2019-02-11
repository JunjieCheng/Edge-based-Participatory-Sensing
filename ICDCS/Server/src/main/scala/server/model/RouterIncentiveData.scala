package server.model

import scala.collection.mutable.ListBuffer

case class RouterIncentiveData(prices: ListBuffer[Double], remainingBudget: Double)
