import java.util.Vector;
class DualVariables{

    public double beta; // Dual Variables
    public double[] weights;
    public int M; // usually equal to the number of data points
    public DualVariables(int M){
        this.beta = 0f;
        this.M = M;
        for(int i =0;i<M;i++){
            weights[i] = 1.0f/M
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
}


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
            alphas[i] = d.alphas[i];
        }
   }
}

class MasterVariables{
    public double beta;
    public double[] weights;
    public double lambdak;
    public double pho;
    public double[] psik;
    public int noOfExamples;

    public MasterVariables(double beta, double[] weights, double lambdak, double pho, double[] psik, int noOfExamples){
        this.noOfExamples = noOfExamples;
        this.beta = beta;
        this.noOfExamples = noOfExamples;
        this.lambdak = lambdak;
        this.pho = pho;

        this.weights = new double[noOfExamples];
        this.psik= new double[noOfExamples];
        for(int i =0;i<noOfExamples;i++){
            this.weights[i] = weights[i];
            this.psik[i] = psik[i];
        }
    }
    public MasterVariables(int noOfExamples){
        this.noOfExamples = noOfExamples;
        this.weights = new double[noOfExamples];
        this.psik = new double[noOfExamples];
        for(int i =0;i<noOfExamples;i++){
            this.weights[i]=0f;
            this.psik[i]=0f;
        }
        this.beta =0f;
        this.lambdak=0f;
        this.pho = 0f;
    }
}
public WorkerVariables{
    public int noOfExamples;
    public double betak
    public double[] weightsk;
    public double lambdak;
    public double[] psik;
}



public class LPBoostWorker{
    public static WorkerVariables optimize(Vector<Datapoint> e, Vector<Integer> l, float v1, float eps,MasterVariables mv){
   
        Vector<Datapoint> examples =e;
        Vector<Integer> labels = l;

        float v=v1;
        float epsilon = eps;
        int M = examples.size();
        float D = 1.0f/(M*v);

        DualVariables dV = new DualVariables(M);
        Vector<Stump> hypotheses = new Vector<Stump>();

        while(true){

            Stump candHyp = getMostViolatingStump(dV.weightsk,examples,labels);
            float classSum =0f;
            for(int i=0;i<examples.size();i++){
                classSum += (dV.weightsk[i]) * (labels.elementAt(i)) * (candHyp.classify(examples.elementAt(i)));        
            }   
            if((classSum <= (dV.betak + epsilon)) || (hypotheses.size() >= M)){
                break;
            }   
            hypotheses.add(candHyp);
            dV = solveOptimization(examples,labels,hypotheses,D,mv);
        }

    }
    
    public static DualVariables solveOptimization(Vector<Datapoint> examples, Vector<Integer> labels, Vector<Stump> hypotheses, float d, MasterVariables mv){
        try{
            IloCplex cplex = new IloCplex();
            IloNumVar betak = cplex.numVar(Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY)
            IloNumVar[] weightsK = cplex.numVarArray(examples.size(),0.0,d);
            for (int i =0;i<hypotheses.size();i++){
                IloLinearNumExpr hypConstraint = cplex.linearNumExpr();
                for(int j =0;j<examples.size();j++){
                    hypConstraint.addTerm(hypotheses.get(i).classify(examples.get(j)*labels.get(j)),weights[j]);
                }
                cplex.addLe(hypConstraint,beta);
                
            }

            IloLinearNumExpr sumConstraint = cplex.linearNumExpr();
            for(int j =0;j<examples.size();j++){
                sumConstraint.addTerm(1,weights[j]);
            }
            cplex.addEq(sumConstraint,1);


            IloNumExpr objective = cplex.sum(betak,cplex.prod(mv.lambdak,betak));
            for(int i =0;i<noOfExamples;i++){
                objective = cplex.sum(objectiveM,cplex.prod(mv.psik[i],weights[i]))
            }

            objective = cplex.sum(cplex.prod(mv.pho/2,betak));
            objective = cplex.sum(cplex.prod(-mv.pho/2,cplex.prod(betak,mv.beta)));
            if(noOfExamples > 0){
                IloNumExpr augLag1 = cplex.sum(0,cplex.prod(cplex.diff(weightsK[0],mv.weights[0]),cplex.diff(weightsK[0],mv.weights[0])));
                for(int i =1;i<noOfExamples;i++){
                    augLag1 = cplex.sum(augLag1,cplex.prod(cplex.diff(weightsK[i],mv.weights[i]),cplex.diff(weightsK[i],mv.weights[i])));
                }
                objective = cplex.sum(objective,augLag1)
            }

            cplex.addMinimize(objective);
            DualVariables dV = new DualVariables(examples.size());
            if(cplex.solve()){
                dV.beta = (float)cplex.getValue(beta);
                double[] tmpWeights= cplex.getValues(weights);
                for(int i =0,i<tmpWeights.length;i++){
                    dV.weights[i]= tmpWeights[i]
                }
                return dV;
            }
            else{
                System.out.println("Model not solved");
            }
        
        }
        catch(Exeception e){
            e.printStackTrace();

        }
        return null;
    }

}
