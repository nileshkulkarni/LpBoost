import akka.actor._
import akka.actor.ActorRef
import akka.actor.Actor

import Array._

import java.util.Vector
import collection.mutable._

case class startLpBoost() // This is the message recieved to start the process
case class updatedWorkerVariables(id: Integer,wK: Array[Double], betaK: Double,phi:Array[Double] ,lambda:Double, hypothesisSet: Vector[Stump])
case class doneProcessing(status: Integer)

class MasterActor() extends Actor {

    private var runningLpBoost = false
    private var totalLines = 0
    private var linesProcessed = 0
    private var result = 0

    private var MainSender: Option[ActorRef] = None

    private var noOfPartitions = 2
    private var noOfExamples = 3
    private var HypothesisSet = new Vector [ Vector [Stump] ]();   
    
    private var weights = ofDim[Double](noOfPartitions,noOfExamples) 
    private var betas = ofDim[Double](noOfPartitions) 
    private var lambdas = ofDim[Double](noOfPartitions) 
    private var phis = ofDim[Double](noOfPartitions,noOfExamples) 
    

    private var con_weightsM = ofDim[Double](noOfExamples) 
    private var betaM=0.0
    
    private var furtherUpdates = ofDim[Boolean](noOfPartitions)

    private var fileName="NULL"
    private var testFile="NULL"

    private var maxOuterIterations = 10

    private var iterationNo = 0
    private var pho =0.0
    private var temp: Int = 1

    private var D = new DataSet()
    def this( fileName:String,testFile: String) ={
        this();
        this.fileName = fileName
        this.testFile = testFile 
        this.D = ReadFile(fileName)
        noOfExamples = D.examples.size()
        weights = ofDim[Double](noOfPartitions,noOfExamples) 
        phis = ofDim[Double](noOfPartitions,noOfExamples) 
        con_weightsM = ofDim[Double](noOfExamples) 
        pho = 2.0
        var totalHyp = 2*noOfExamples
        HypothesisSet = new Vector[ Vector [Stump] ](noOfPartitions)
    }
    def receive = {
        case startLpBoost() => {
            if (runningLpBoost) {
                // println just used for example purposes;
                // Akka logger should be used instead
                println("[MASTER] Warning: duplicate start message received")
            } 
            else {
                runningLpBoost = true
                //MainSender = Some(sender) // save reference to process invoker
                MainSender = Some(sender) // save reference to process invoker
                //sender ! doneProcessing(1)
                //create actors
                createActors()
                //updateBetaAndLambda()
                updateBetaAndLambdaInitialize() 
            }
        }
        case updatedWorkerVariables(id,wK,betaK,phi,lambda,hypothesisSetK) => {
            println("[MASTER] Received response for weights and beta")
            weights(id) = wK
            betas(id) = betaK
            println("Setting Hypothesis : current size " + HypothesisSet.size())
            if(HypothesisSet.size() < id){
                HypothesisSet.set(id,hypothesisSetK)
            }
            else{
                HypothesisSet.add(id,hypothesisSetK)
            }
            updateLambdaKBetaK(id,phi,lambda)
            println("[MASTER] Yeyeye")
            furtherUpdates(id) = false
            updateBetaAndLambda()
        }
        case _ => println("[MASTER] message not recognized!")
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
            println("[MASTER] Printing in master")
            for (i<-0 until noOfExamples){
                println("[MASTER] Weight " + con_weightsM(i))
            }
            */
            println("[MASTER] here noOfPartitions is " + noOfPartitions);
            println("[MASTER] File Name " + fileName)
            for( a <- 0 until noOfPartitions){
                var actorWorker: ActorRef = ActorFactory.getActor(a)
                println("[MASTER] Sending beta and lambda updates to actor " + a);
                actorWorker ! updateWeightsBetaLambdaPhi(con_weightsM,betaM)
                furtherUpdates(a) = true
            }
            println("[MASTER] Sent updates to all worker actors");
    }
    def updateBetaAndLambda() {
            println("[MASTER] Iteration no is " + iterationNo) 
            println("[MASTER] Check for more updates " + checkIfMoreUpdatesRequired());
            if(!checkIfMoreUpdatesRequired()){
                // update betaM
                betaM = 0;
                println("[MASTER] All fine")
                for(i <-0 until noOfPartitions){
                    betaM = betaM +lambdas(i) + pho*betas(i)
                }
                println("[MASTER] All fine1")
                println("[MASTER] No of partitions " + noOfPartitions)
                println("[MASTER] No of examples " + noOfExamples)
                println("[MASTER] Weights size "+ weights.size + " " + weights(0).size) 
                println("[MASTER] Con Weights size "+ con_weightsM.size ) 
                for(j <-0 until noOfExamples){
                    con_weightsM(j)=0
                    for(i <-0 until noOfPartitions){
                        con_weightsM(j) = pho*weights(i)(j) + phis(i)(j)
                    }
                    con_weightsM(j) = con_weightsM(j)/(noOfPartitions*pho)
                }
                println("[MASTER] All fine2")
                betaM = betaM/(noOfPartitions*pho);

                println("[MASTER] Master: BetaM : " + betaM);
                // updating con_weightsM
                // Check if stopping criterion is met
                if(checkForStoppingCriterion()){
                    println("[MASTER] Stopping Criterion met.")
                    solvePrimalModel()
                    println("Primal Model Solved")
                    MainSender.map(_ ! doneProcessing(1))
                    //fileSender.map(_ ! result)  // provide result to process invoker
                    return
                }
                println("[MASTER] here noOfPartitions is " + noOfPartitions);
                println("[MASTER] File Name " + fileName)
                for( a <- 0 until noOfPartitions){
                    var actorWorker: ActorRef = ActorFactory.getActor(a)
                    println("[MASTER] Sending beta and lambda updates to actor " + a);
                    actorWorker ! updateWeightsBetaLambdaPhi(con_weightsM,betaM)
                    furtherUpdates(a) = true
                }
                iterationNo = iterationNo+1
                println("[MASTER] here1");
            }
    }
    def updateLambdaKBetaK(id:Integer,phi:Array[Double],lambda:Double)={
            lambdas(id) = lambda
            phis(id) = phi
    }
    def ReadFile(filename:String) :DataSet={
        var d = ReadData.buildVector(filename," ")
        println("[MASTER] Read Data successfully")
        return d
    }
    def solvePrimalModel(): PrimalVariables={
        var totalHypSet = new Vector[Stump] ()
        for(i <-0 until noOfPartitions){
            for( j <-0 until HypothesisSet.get(i).size()){
                totalHypSet.add(HypothesisSet.get(i).get(j)) 
            }
        }
        var M = D.examples.size()
        var d = 1.0/(M*0.07f)
        var pV = LPBoostMaster.solvePrimalModel(D.examples,D.labels,totalHypSet,d)
        
        println("Rho " + pV.rho)
        println("No of hypo " + pV.alphas.size())
        var boostClassfier = new BoostClassifier(pV.alphas, totalHypSet)
        getAccuracy(testFile,boostClassfier)
        return pV
    }

    def getAccuracy(testFile: String,boostClassfier: BoostClassifier): Double={
        var total=0.0f
        var correct=0.0f
        var testDataSet = ReadData.buildVector(testFile," ")
        for( i<-0 until testDataSet.examples.size()){
            total = total+1
            if(boostClassfier.classify(testDataSet.examples.get(i)) * testDataSet.labels.get(i) > 0.0f)
                correct = correct+1
        }
        var acc = correct/total
        println("acc is " + acc)
        return acc 
    }
}
object ActorFactory {
    val actorMap = ofDim[ActorRef](2)
    def addActor(key: Integer,myActor: ActorRef)={
        actorMap(key) = myActor; 
    }
    def getActor(symbol:Integer): ActorRef = {
        println("[MASTER] getActor : called with symbol " + symbol)
        val wactor : ActorRef = actorMap(symbol) 
        //val wactor : ActorRef = actorMap.get(symbol)
        println("[MASTER] Returning actor: "+ wactor)
        return wactor
    }
}
