import akka.actor._
import akka.actor.ActorRef
import akka.actor.Actor
import Array._

case class startLpBoost() // This is the message recieved to start the process
case class updatedWorkerVariables(id: Integer,wK: Array[Double], betaK: Double,phi:Array[Double] ,lambda:Double)

class MasterActor(filename: String) extends Actor {

    private var runningLpBoost = false
    private var totalLines = 0
    private var linesProcessed = 0
    private var result = 0
    private var fileSender: Option[ActorRef] = None
    private var noOfPartitions = 2
    private var noOfExamples = 4
    private var weights = ofDim[Double](noOfPartitions,noOfExamples) 
    private var betas = ofDim[Double](noOfPartitions) 
    private var lambdas = ofDim[Double](noOfPartitions) 
    private var phis = ofDim[Double](noOfPartitions,noOfExamples) 
    private var phi = ofDim[Double](noOfExamples)
    private var beta=0.0
    def receive = {
        case startLpBoost() => {
            if (runningLpBoost) {
                // println just used for example purposes;
                // Akka logger should be used instead
                println("Warning: duplicate start message received")
            } else {
                runningLpBoost = true
                fileSender = Some(sender) // save reference to process invoker
                //create actors
                creatActors()
            }
        }
        case updatedWorkerVariables(id,wK,betaK,phi,lambda) => {
            println("Received response for weights and beta")
            //for(a<- 0 untill noOfExamples){
            weights(id) = wK
            betas(id) = betaK;
            //}
            updateLambdaKBetaK(id,phi,lambda)
            updateBetaAndLambda()
        }
        case _ => println("message not recognized!")
    }
    def creatActors(){
       for(a <-0 until noOfPartitions){
            ActorFactory.addActor(a,context.actorOf(Props(new WorkerActor(a,filename)))) 
       }
    }
    def checkIfMoreUpdatesRequired(){
        return false
    }
    def updateBetaAndLambda = {
        // compute Beta and lambda and call the update Weights function
            
            val furtherUpdates = checkIfMoreUpdatesRequired()
            if(furtherUpdates==true){
                for( a <- 0 until noOfPartitions){
                    val actorWorker = ActorFactory.getActor(a)
                    actorWorker ! updateWeightsBetaLambdaPhi(filename,weights,beta)
                }
            }
    }
    def updateLambdaKBetaK(id:Integer,phi:Array[Double],lambda:Double)={
            lambdas(id) = lambda
            phis(id) = phi
    }
}
object ActorFactory {
    val actorMap = new HashMap[Intger,Actor] with SynchronizedMap[Integer,Actor]
    def addActor(key: Integer,myActor: Actor)={
        actorMap += (key,myActor) 
    }
    def getActor(symbol:Integer): Actor = {
        val actor = actorMap.get(symbol)
        return actor
    }
}
