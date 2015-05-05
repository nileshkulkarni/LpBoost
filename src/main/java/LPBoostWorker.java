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


class PrimalVariables{
   public float rho =0f;
   public double[] alphas;
   int N;
   public PrimalVariables(int N){
       this.N = N;
       alphas = new double[N]; 
   }
   public PrimalVariables(PrimalVariables p){
        this.rho =p.rho;
        this.N = p.N;
        this.alphas = new double[N]; 
        for(int i =0;i<N;i++){
            alphas[i] = p.alphas[i];
        }
   }
}



public class LPBoostWorker{
    public static Stump getMostViolatingStump(double[] weights, Vector<DataPoint> examples, Vector<Integer> labels){
       float highestError=Float.NEGATIVE_INFINITY;
        Stump bestLearner=null;
        for(int i =0;i<examples.elementAt(0).size();i++){
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

        }
        return bestLearner;
    }
    public static WorkerVariables optimize(Vector<DataPoint> e, Vector<Integer> l, float v1, float eps,MasterVariables mv){
   
        Vector<DataPoint> examples =e;
        Vector<Integer> labels = l;

        float v=v1;
        float epsilon = eps;
        int M = examples.size();
        float D = 1.0f/(M*v);

        DualVariables dV = new DualVariables(M);
        Vector<Stump> hypotheses = new Vector<Stump>();

        while(true){

            Stump candHyp = getMostViolatingStump(dV.weights,examples,labels);
            float classSum =0f;
            for(int i=0;i<examples.size();i++){
                classSum += (dV.weights[i]) * (labels.elementAt(i)) * (candHyp.classify(examples.elementAt(i)));        
            }   
            if((classSum <= (dV.beta + epsilon)) || (hypotheses.size() >= M)){
                break;
            }   
            hypotheses.add(candHyp);
            dV = solveOptimization(examples,labels,hypotheses,D,mv);
        }

        mv.lambdak = mv.lambdak + mv.pho*(dV.beta - mv.beta);
        for(int i =0;i<examples.size();i++){
            mv.psik[i]= mv.psik[i] + mv.pho*(dV.weights[i] - mv.weights[i]);
        }
        WorkerVariables wV = new WorkerVariables(examples.size(), dV.beta,dV.weights, mv.lambdak, mv.psik);
        return wV;
    }
    
    public static DualVariables solveOptimization(Vector<DataPoint> examples, Vector<Integer> labels, Vector<Stump> hypotheses, float d, MasterVariables mv){
        try{
            IloCplex cplex = new IloCplex();
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
            objective = cplex.sum(0,cplex.prod(mv.pho/2, cplex.prod(cplex.diff(betak,mv.beta), cplex.diff(betak,mv.beta))));
            if(examples.size() > 0){
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
                System.out.println("Model not solved");
            }
        
        }
        catch(Exception e){
            e.printStackTrace();

        }
        return null;
    }
}
