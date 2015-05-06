import akka.actor._
import akka.actor.ActorRef
import akka.actor.Actor

import Array._

import collection.mutable._

case class startLpBoost() // This is the message recieved to start the process
case class updatedWorkerVariables(id: Integer,wK: Array[Double], betaK: Double,phi:Array[Double] ,lambda:Double)
case class doneProcessing(status: Integer)

class MasterActor() extends Actor {

    private var runningLpBoost = false
    private var totalLines = 0
    private var linesProcessed = 0
    private var result = 0
    private var fileSender: Option[ActorRef] = None
    private var noOfPartitions = 1
    private var noOfExamples = 4
    
    
    private var weights = ofDim[Double](noOfPartitions,noOfExamples) 
    private var betas = ofDim[Double](noOfPartitions) 
    private var lambdas = ofDim[Double](noOfPartitions) 
    private var phis = ofDim[Double](noOfPartitions,noOfExamples) 
    

    private var con_weightsM = ofDim[Double](noOfExamples) 
    private var betaM=0.0
    
    private var furtherUpdates = ofDim[Boolean](noOfPartitions)

    private var fileName="NULL"
    private var maxOuterIterations = 2

    private var iterationNo = 0
    private var pho =0.0

    private var temp: Int = 1
    def this( fileName:String) ={
        this();
        this.fileName = fileName
        pho = 1.0
    }
    def receive = {
        case startLpBoost() => {
            if (runningLpBoost) {
                // println just used for example purposes;
                // Akka logger should be used instead
                println("Warning: duplicate start message received")
            } 
            else {
                runningLpBoost = true
                fileSender = Some(sender) // save reference to process invoker
                sender ! doneProcessing(1)
                //create actors
                createActors()
                //updateBetaAndLambda()
                updateBetaAndLambdaInitialize() 
            }
        }
        case updatedWorkerVariables(id,wK,betaK,phi,lambda) => {
            println("Received response for weights and beta")
            furtherUpdates(id) = false
            weights(id) = wK
            betas(id) = betaK
            updateLambdaKBetaK(id,phi,lambda)
            println("Yeyeye")
            updateBetaAndLambda()
        }
        case _ => println("message not recognized!")
    }
    def createActors(){
       for(a <-0 until noOfPartitions){
            furtherUpdates(a) = false
       }

       for(a <-0 until noOfPartitions){
            ActorFactory.addActor(a,context.actorOf(Props(new WorkerActor(a,fileName)))) 
       }
    }

    def checkIfMoreUpdatesRequired(): Boolean={
        var updatesReq :Boolean =false
        for(a<-0 until noOfPartitions){
            updatesReq = furtherUpdates(a) || updatesReq
        }
        return updatesReq
    }

    def checkForStoppingCriterion(): Boolean={
        if(iterationNo >=maxOuterIterations){
            return true
        }
        else{
            return false
        }
    }
    def updateBetaAndLambdaInitialize() {
        // compute Beta and lambda and call the update Weights function

                for( i <- 0 until noOfExamples){
                     con_weightsM(i) = 1.0/noOfExamples
                     betaM =0.0
                }
                /*
                println("Printing in master")
                for (i<-0 until noOfExamples){
                    println("Weight " + con_weightsM(i))
                }
                */
                println("here noOfPartitions is " + noOfPartitions);
                println("File Name " + fileName)
                for( a <- 0 until noOfPartitions){
                    var actorWorker: ActorRef = ActorFactory.getActor(a)
                    println("Sending beta and lambda updates to actor " + a);
                    actorWorker ! updateWeightsBetaLambdaPhi(con_weightsM,betaM)
                    furtherUpdates(a) = true
                }
                println("Sent updates to all worker actors");
    }
    def updateBetaAndLambda() {
            if(!checkIfMoreUpdatesRequired()){
                // update betaM
                betaM = 0;
                for(i <-0 until noOfPartitions){
                    betaM = betaM +lambdas(i) + pho*betas(i)
                }
                for(j <-0 until noOfExamples ){
                    con_weightsM(j)=0
                    for(i <-0 until noOfPartitions){
                        con_weightsM(j) = pho*weights(i)(j) + phis(i)(j)
                    }
                    con_weightsM(j) = con_weightsM(j)/(noOfPartitions*pho)
                }
                betaM = betaM/(noOfPartitions*pho);

                println("Master: BetaM : " + betaM);
                // updating con_weightsM
                // Check if stopping criterion is met
                if(checkForStoppingCriterion()){
                    println("Stopping Criterion met.")
                    return
                }
                println("here noOfPartitions is " + noOfPartitions);
                println("File Name " + fileName)
                for( a <- 0 until noOfPartitions){
                    var actorWorker: ActorRef = ActorFactory.getActor(a)
                    println("Sending beta and lambda updates to actor " + a);
                    actorWorker ! updateWeightsBetaLambdaPhi(con_weightsM,betaM)
                }
                iterationNo = iterationNo+1
                println("here1");
            }
    }
    def updateLambdaKBetaK(id:Integer,phi:Array[Double],lambda:Double)={
            lambdas(id) = lambda
            phis(id) = phi
    }
}
/*
object ActorFactory {
    val actorMap = new HashMap[Integer,ActorRef] with SynchronizedMap[Integer,ActorRef]
    def addActor(key: Integer,myActor: ActorRef)={
        actorMap.put(key,myActor) 
    }
    def getActor(symbol:Integer): ActorRef = {
        println("getActor : called with symbol " + symbol)
        val wactor : ActorRef = actorMap.get(symbol).asInstanceOf[ActorRef]
        //val wactor : ActorRef = actorMap.get(symbol)
        println("Returning actor: "+ wactor)
        for ((k,v) <- actorMap) { 
            println("Key : " + k  + " actorRef  "+ v)
        }        // k is the key, v is the value
        return wactor
    }
}
*/

object ActorFactory {
    val actorMap = ofDim[ActorRef](2)
    def addActor(key: Integer,myActor: ActorRef)={
        actorMap(key) = myActor; 
    }
    def getActor(symbol:Integer): ActorRef = {
        println("getActor : called with symbol " + symbol)
        val wactor : ActorRef = actorMap(symbol) 
        //val wactor : ActorRef = actorMap.get(symbol)
        println("Returning actor: "+ wactor)
        return wactor
    }
}
