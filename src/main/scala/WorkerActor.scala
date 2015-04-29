import akka.actor._

case class ProcessStringMsg(string: String)
case class StringProcessedMsg(words: Integer)
case class updateWeightsBeta(fileName:String, weight: Array[Double], beta:Double)
class WorkerActor (id: Integer, filename: String) extends Actor {
    def receive = {
        case updateWeightsBetaLambdaPhi (fileName: String, weight: Array[Double], beta: Double) => {
            println("File Name " + fileName); 
            sender ! updatedWorkerVariables()
        }
        case _ => println("Error: Worker Actor: message not recognized")
    }
}
