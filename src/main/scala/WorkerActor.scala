import akka.actor._

import Array._
import java.util.Vector

case class ProcessStringMsg(string: String)
case class StringProcessedMsg(words: Integer)
case class updateWeightsBetaLambdaPhi(weight: Array[Double], beta:Double)
class WorkerActor extends Actor {
    private var id =0
    private var filename="NULL File"
    private var noOfExamples = 4
    private var beta=0.0
    private var lambda = 0.0 
    private var phi = ofDim[Double](noOfExamples) 
    private var weight = ofDim[Double](noOfExamples) 
    private var D = new DataSet()
    private var firstTime = true
    private var outerIteration =0
    private var workerObject = new LPBoostWorker()
    private var totalActors=0

    def this(value: Integer, filename:String,totalActors: Integer) ={
        this()
        this.id = value
        this.filename = filename
        this.totalActors = totalActors
        println("[ACTOR: " + id + " ] Spwaned Worker Actor with id " + value)
        this.D = ReadFile(filename)
        noOfExamples = D.examples.size()
        phi = ofDim[Double](noOfExamples) 
        weight = ofDim[Double](noOfExamples) 
        println("[ACTOR: " + id + " ] File Name " + filename)
        firstTime = true
        workerObject = new LPBoostWorker(value,totalActors)
    }
    def receive = {
        case updateWeightsBetaLambdaPhi (weightM: Array[Double], betaM: Double) => {
            println("[ACTOR: " + id + " ] Outer Iteration no" + outerIteration)
            outerIteration = outerIteration +1

            var pho = 5.0
            println("[ACTOR: " + id + " ] No of examples " + D.examples.size())
            println("[ACTOR: " + id + " ] No of labels " + D.labels.size())

            var mV = new MasterVariables(betaM,weightM,lambda,pho,phi,noOfExamples)
            println("[ACTOR: " + id + " ] Sending master variables for optimization")

            var wV = workerObject.optimize(D.examples,D.labels, 0.07f,0.00001f,mV) 

            var weight = wV.weightsk
            var beta = wV.betak
            lambda = lambda + mV.pho*(beta -betaM) 

            for(a<- 0 until noOfExamples){
                phi(a) = phi(a) + mV.pho*(weight(a) - weightM(a))
            }
            println("[ACTOR: " + id + " ] Lambda " +lambda)

            println("[ACTOR: " + id + " ] Sending updates to master");
            sender ! updatedWorkerVariables(id,weight,beta,phi,lambda,wV.hypSet)
            println("[ACTOR: " + id + " ] Sent updates to master");
        }
        case _ => println("[ACTOR: " + id + " ] Error: Worker Actor: message not recognized")
    }

    def ReadFile(filename:String) :DataSet={
        var d = ReadData.buildVector(filename," ")
        println("[ACTOR: " + id + " ] Read Data successfully")
        return d
    }
}
