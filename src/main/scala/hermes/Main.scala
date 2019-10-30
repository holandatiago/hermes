package hermes

import hermes.bots.Bot
import hermes.config.Strategy

import scala.io.StdIn
import scala.util._

object Main extends App {
  var runningBots = Map[String, Bot]()
  var shutdown = false
  while (!shutdown) {
    val input = StdIn.readLine.split(" ")
    runningBots = runningBots.filter(_._2.status != "Offline")
    if (input.nonEmpty) {
      val command = input.head
      val param = input.tail.headOption
      command match {
        case "status" => runningBots.foreach { case (name, bot) => println(s"$name => ${bot.status()}")}
        case "start" =>
          if (param.isEmpty) println("Specify bot name.")
          else {
            val name = param.get
            if (runningBots.contains(name)) println(s"Bot $name already started.")
            else Try(Strategy(name)) match {
              case Success(strategy) =>
                val bot = Bot(strategy)
                bot.start()
                runningBots = runningBots + (name -> bot)
              case Failure(exception) => println(exception.getMessage)
            }
          }
        case "stop" =>
          if (param.isEmpty) println("Specify bot name.")
          else {
            val name = param.get
            if (runningBots.contains(name)) runningBots(name).stop()
            else println(s"Bot $name has not started.")
          }
        case "exit" => shutdown = true
        case _ => println(s"Command $command not found.")
      }
    }
  }
}
