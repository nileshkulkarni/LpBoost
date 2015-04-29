import akka.actor._

object Main extends App {

    import akka.util.Timeout
        import scala.concurrent.duration._
        import akka.pattern.ask
        import akka.dispatch.ExecutionContexts._


        override def main(args: Array[String]) {
            val system = ActorSystem("System")
                val actor = system.actorOf(Props(new MasterActor(args(0))))
                implicit val ec = global
                implicit val timeout = Timeout(25 seconds)
                val future = actor ? startLpBoost()
                future.map { result => 
                    println("Total number of words " + result)
                        system.shutdown
                }           
        }           
}