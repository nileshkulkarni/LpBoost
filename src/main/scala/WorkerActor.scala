import akka.actor._

import Array._
import collection.mutable._

case class ProcessStringMsg(string: String)
case class StringProcessedMsg(words: Integer)
case class updateWeightsBetaLambdaPhi(fileName:String, weight: Array[Double], beta:Double)
class WorkerActor extends Actor {
    private var id =0
    private var filename="NULL File"
    private var noOfExamples = 4
    private var beta=0.0
    private var lambda = 0.0 
    private var phi = ofDim[Double](noOfExamples) 
    private var weight = ofDim[Double](noOfExamples) 
    def this(value: Integer, filename:String) ={
        this();
        this.id = value
        this.filename = filename
        println("Spwaned Worker Actor with id " + value)

    }
    def receive = {
        case updateWeightsBetaLambdaPhi (fileName: String, weight: Array[Double], beta: Double) => {
            println("File Name " + fileName)
            sender ! updatedWorkerVariables(id,weight,beta,phi,lambda)
        }
        case _ => println("Error: Worker Actor: message not recognized")
    }
}
