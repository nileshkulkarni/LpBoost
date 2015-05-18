import java.util.Vector;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumExpr;
import ilog.cplex.IloCplex;

class DualVariables{

    public double beta; // Dual Variables
    public double[] weights;
    public int M; // usually equal to the number of data points
    public DualVariables(int M){
        this.beta = 0f;
        this.M = M;
        this.weights = new double[M];
        for(int i =0;i<M;i++){
            weights[i] = 1.0f/M;
        }
    }
    public DualVariables(DualVariables d){
        this.M = d.M;
        this.beta = d.beta;
        this.weights = new double[M]; 
        for(int i =0;i<M;i++){
            weights[i] = d.weights[i];
        }
    }
}// end of class




public class LPBoostWorker{
    private DualVariables prev_dV;
    private Vector<Stump> hypotheses ;
    private int actorId;
    private int totalActors;

    LPBoostWorker(){
        actorId =-1;
    }
    LPBoostWorker(int id,int totalActors){
        prev_dV=null;
        hypotheses = new Vector<Stump>();
        actorId = id;
        this.totalActors = totalActors;
    }
    public  Stump getMostViolatingStump(double[] weights, Vector<DataPoint> examples, Vector<Integer> labels){
       float highestError=Float.NEGATIVE_INFINITY;
        Stump bestLearner=null;
        
        for(int i =0;i<examples.elementAt(0).size();i++){
            
            if((i%totalActors) ==actorId){
                Stump stump1 = new Stump(i, true);
                Stump stump2 = new Stump(i, false);
                float sum1=0f;
                float sum2=0f;
                for(int j=0;j<examples.size();j++){
                    sum1 += (stump1.classify(examples.elementAt(j))) * (labels.elementAt(j)) * (weights[j]);
                    sum2 += (stump2.classify(examples.elementAt(j))) * (labels.elementAt(j)) * (weights[j]);
                }   
                if(sum1>highestError){
                    highestError=sum1;
                    bestLearner=new Stump(stump1.index,stump1.orientation);
                }   
                if(sum2>highestError){
                    highestError=sum2;
                    bestLearner=new Stump(stump2.index,stump2.orientation);
                }
                if(i ==0){
                    System.out.println("[ACTOR] " + actorId+" Class sum for ind =0 is:  " + sum1 +" : " + sum2);
                }
            }
            /*
            if((actorId % 2) == 1){
                if(sum1>highestError){
                    highestError=sum1;
                    bestLearner=new Stump(stump1.index,stump1.orientation);
                }   
            }
            else{
                if(sum2>highestError){
                    highestError=sum2;
                    bestLearner=new Stump(stump2.index,stump2.orientation);
                }
            }
            */
        }
        return bestLearner;
    }

    public  WorkerVariables optimize(Vector<DataPoint> e, Vector<Integer> l, float v1, float eps,MasterVariables mv){
        try{

            System.out.println("[ACTOR] " + actorId + " Inside optimize");
       
            Vector<DataPoint> examples =e;
            Vector<Integer> labels = l;

            float v=v1;
            float epsilon = eps;
            int M = examples.size();
            float D = 1.0f/(M*v);


            DualVariables dV = new DualVariables(M);
            System.out.println("here");
            //Vector<Stump> hypotheses = new Vector<Stump>();

            while(true){
                if(prev_dV!=null){
                    dV = prev_dV;
                }
                System.out.println("[ACTOR] " + actorId + " Finding the most violating constraint");
                Stump candHyp = getMostViolatingStump(dV.weights,examples,labels);
                System.out.println("[ACTOR] " + actorId + " Most Violating Stump: " + candHyp.toString());
                System.out.println("Epsilon: " + epsilon);
                float classSum =0f;
                for(int i=0;i<examples.size();i++){
                    classSum += (dV.weights[i]) * (labels.elementAt(i)) * (candHyp.classify(examples.elementAt(i)));        
                }   
                System.out.println("Class sum is: " + classSum);
                System.out.println("Beta is: " + dV.beta);
                if((classSum <= (dV.beta + epsilon)) || (hypotheses.size() >= M)){
                    System.out.println("[ACTOR] " + actorId + " Breaking out");
                    // Since master has send updates its variables might have changed
                    dV = solveOptimization(examples,labels,hypotheses,D,mv);
                    prev_dV = null;
                    prev_dV = dV;
                    break;
                }   
                hypotheses.add(candHyp);
                System.out.println("[ACTOR] " + actorId + " Hypostesis set size " + hypotheses.size());
                dV = solveOptimization(examples,labels,hypotheses,D,mv);
                prev_dV = null;
                prev_dV = dV;
            }

            mv.lambdak = mv.lambdak + mv.pho*(dV.beta - mv.beta);
            for(int i =0;i<examples.size();i++){
                mv.psik[i]= mv.psik[i] + mv.pho*(dV.weights[i] - mv.weights[i]);
            }
            WorkerVariables wV = new WorkerVariables(examples.size(), dV.beta,dV.weights, mv.lambdak, mv.psik,hypotheses);
            return wV;
        }
        catch(Exception b){
            b.printStackTrace();
            return null;
        }
    }
    
    public DualVariables solveOptimization(Vector<DataPoint> examples, Vector<Integer> labels, Vector<Stump> hypotheses, float d, MasterVariables mv){
        try{
            IloCplex cplex = new IloCplex();
            cplex.setOut(null);
            IloNumVar betak = cplex.numVar(Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
            IloNumVar[] weightsK = cplex.numVarArray(examples.size(),0.0,d);
            // Hyposthesis Constraints
            for (int i =0;i<hypotheses.size();i++){
                IloLinearNumExpr hypConstraint = cplex.linearNumExpr();
                for(int j =0;j<examples.size();j++){
                    hypConstraint.addTerm(hypotheses.get(i).classify(examples.get(j))*labels.get(j),weightsK[j]);
                }
                cplex.addLe(hypConstraint,betak);
                
            }
            // Sum Constraint
            IloLinearNumExpr sumConstraint = cplex.linearNumExpr();
            for(int j =0;j<examples.size();j++){
                sumConstraint.addTerm(1,weightsK[j]);
            }
            cplex.addEq(sumConstraint,1);

            // Objective 
            IloNumExpr objective = cplex.sum(betak,cplex.prod(mv.lambdak,betak));
            for(int i =0;i<examples.size();i++){
                objective = cplex.sum(objective,cplex.prod(mv.psik[i],weightsK[i]));
            }
            objective = cplex.sum(objective,cplex.prod(mv.pho/2, cplex.prod(cplex.diff(betak,mv.beta), cplex.diff(betak,mv.beta))));
            //objective = cplex.sum(objective,cplex.prod(mv.pho/2, IloAbs(cplex.diff(betak,mv.beta))));
            if(examples.size() > 0){
                //IloNumExpr augLag1 = cplex.sum(0,cplex.prod(cplex.diff(weightsK[0],mv.weights[0]),cplex.diff(weightsK[0],mv.weights[0])));
                IloNumExpr augLag1 = cplex.sum(0,cplex.prod(cplex.diff(weightsK[0],mv.weights[0]),cplex.diff(weightsK[0],mv.weights[0])));
                for(int i =1;i<examples.size();i++){
                    augLag1 = cplex.sum(augLag1,cplex.prod(cplex.diff(weightsK[i],mv.weights[i]),cplex.diff(weightsK[i],mv.weights[i])));
                }
                objective = cplex.sum(objective,augLag1);
            }

            cplex.addMinimize(objective);
            DualVariables dV = new DualVariables(examples.size());
            if(cplex.solve()){
                dV.beta = (float)cplex.getValue(betak);
                double[] tmpWeights= cplex.getValues(weightsK);
                for(int i =0;i<tmpWeights.length;i++){
                    dV.weights[i]= tmpWeights[i];
                }
                return dV;
            }
            else{
                System.out.println("[ACTOR] " + actorId + " Model not solved");
            }
        
        }
        catch(Exception e){
            e.printStackTrace();

        }
        return null;
    }
}
