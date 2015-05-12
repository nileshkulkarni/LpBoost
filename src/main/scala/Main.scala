import akka.actor._
import Array._
//import scala.collection.mutable._
import java.lang._
import java.util.Vector
//import scala.collection.JavaConversions._
//import cuttingplane.*

object Main extends App {

    import akka.util.Timeout
        import scala.concurrent.duration._
        import akka.pattern.ask
        import akka.dispatch.ExecutionContexts._


        override def main(args: Array[String]) {
            val system = ActorSystem("System")
            val actor = system.actorOf(Props(new MasterActor(args(0),args(1),args(2).toInt)))
            implicit val ec = global
            implicit val timeout = Timeout(125 seconds)
            val future = actor ? startLpBoost()
            future.map { result => 
                println("Total number of words " + result)
                    system.shutdown
            }           
        }           
}
