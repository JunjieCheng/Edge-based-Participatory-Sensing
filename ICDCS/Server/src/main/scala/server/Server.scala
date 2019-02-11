package server

import java.text.SimpleDateFormat

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import compiler.ServiceAnalyzer
import parser.DSL_AST.ServiceObj
import parser.MicroServiceDSL._
import server.model.{Data, Sleep}
import server.util.Incentive

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.io.{Source, StdIn}
import scala.util.control.Breaks._

import scala.concurrent._
import ExecutionContext.Implicits.global

object Server {

  val MAX_INCENTIVE_INTERVAL: Int = 3600000

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("server")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    breakable {
      while (true) {
        print("> ")
        StdIn.readLine() match {
          case "start" => {
            val service: ServiceObj = parse()
            val serviceAnalyzer = new ServiceAnalyzer(service)
            startTask(serviceAnalyzer)
          }
          case "exit" => break
        }
      }
    }
  }

  def parse(): ServiceObj = {
    val filename: String = System.getProperty("user.dir") + "/src/main/scala/use_case/room.serv"
    val str: String = Source.fromFile(filename).getLines.mkString

    service(new lexical.Scanner(str)) match {
      case Success(service, _) =>
        //        println("Success\n" + service)
        service
      case Failure(msg, _) =>
        println("Failure: " + msg)
        null
      case Error(msg, _) =>
        println("Error: " + msg)
        null
    }
  }

  def startTask(service: ServiceAnalyzer): Unit = {
    val task = Future[Unit] {
      taskLoop(service)
    }

    Await.result(task, 24.hours)
  }

  def taskLoop(service: ServiceAnalyzer): Unit = {
    // Service parameters
    val taskName: String = service.task_name
    val expiration: Long = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(service.expiration).getTime
    var budget: Double = service.budget.toDouble
    val dataSynchronizationInterval: Int = service.synchronization.toInt * 1000
    var incentiveInterval: Int = 600000
    var threshold: Double = service.threshold.toDouble
    val routers: List[String] = List("http://localhost:8081/ebps")

    // Collected data
    val prices: scala.collection.mutable.Map[String, ListBuffer[Data]] = scala.collection.mutable.Map()

    for (router <- routers) {
      prices(router) = new ListBuffer[Data]()
    }

    // Sleep queue
    val sleepQueue = new mutable.PriorityQueue[Sleep]()(Ordering.by(_.endTime)).reverse

    sleepQueue.enqueue(Sleep(System.currentTimeMillis() + dataSynchronizationInterval, "data"))
    sleepQueue.enqueue(Sleep(System.currentTimeMillis() + incentiveInterval, "incentive"))

    // Calculate budget for each router
    val budgets: Map[String, Double] = Incentive.calculateBudget(routers, prices.toMap, budget, incentiveInterval,
      expiration, threshold, first = true)

    // Minus from total budget
    for (router <- routers) {
      budget -= budgets(router)
    }

    // Distribute jobs
    util.Synchronization.distributeJob(routers, taskName, budgets, threshold.toString, expiration.toString)

    while (System.currentTimeMillis() < expiration) {
      println(sleepQueue)

      val sleep: Sleep = sleepQueue.dequeue()

      if (sleep.method == "data") {
        println("Start data sleeping...")
        Thread.sleep(Math.min(Math.max(0, sleep.endTime - System.currentTimeMillis()), expiration - System.currentTimeMillis()))
        util.Synchronization.synchronizeData(routers, taskName)
        sleepQueue.enqueue(Sleep(sleep.endTime + dataSynchronizationInterval, "data"))
      } else {
        println("Start incentive sleeping...")
        Thread.sleep(Math.min(Math.max(0, sleep.endTime - System.currentTimeMillis()), expiration - System.currentTimeMillis()))

        // Update condition variables
        if (incentiveInterval * 2 > MAX_INCENTIVE_INTERVAL) {
          incentiveInterval = MAX_INCENTIVE_INTERVAL
        } else {
          incentiveInterval *= 2
        }

        // Get incentive data
        budget += Incentive.requestIncentiveData(routers, taskName, prices)

        // Distribute incentive
        threshold = Incentive.calculateThreshold(routers, prices.toMap, budget, incentiveInterval, expiration)
        val budgets: Map[String, Double] = Incentive.calculateBudget(routers, prices.toMap, budget,
          Math.min(incentiveInterval, (expiration - System.currentTimeMillis()).toInt), expiration, threshold, first = false)
        Incentive.distributeIncentive(routers, taskName, budgets, threshold)

        sleepQueue.enqueue(Sleep(sleep.endTime + incentiveInterval, "incentive"))
      }

    }

    // TODO: Collect data

  }
}
