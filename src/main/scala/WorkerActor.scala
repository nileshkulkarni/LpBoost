import akka.actor._

import Array._
import java.util.Vector

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
    private var D = new DataSet()

    def this(value: Integer, filename:String) ={
        this()
        this.id = value
        this.filename = filename
        println("Spwaned Worker Actor with id " + value)
        this.D = ReadFile(filename)
    }
    def receive = {
        case updateWeightsBetaLambdaPhi (fileName: String, weightM: Array[Double], betaM: Double) => {
            println("File Name " + fileName)
            
            var pho = 1.0
            var mV = new MasterVariables(betaM,weightM,lambda,pho,phi,noOfExamples)
            var wV = LPBoostWorker.optimize(D.examples,D.labels, 0.07f,0.00001f,mV) 
            var weight = wV.weightsk
            var beta = wV.betak
            lambda = lambda + pho*(beta -betaM) 
            for(a<- 0 until noOfExamples){
                phi(a) = phi(a) + pho*(weight(a) - weightM(a))
            }
            sender ! updatedWorkerVariables(id,weight,beta,phi,lambda)
        }
        case _ => println("Error: Worker Actor: message not recognized")
    }

    def ReadFile(filename:String) :DataSet={
        var d = ReadData.buildVector(filename,",")
        return d
    }
}
