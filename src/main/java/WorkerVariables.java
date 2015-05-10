import java.util.*;
public class WorkerVariables{
    public int noOfExamples;
    public double betak;
    public double[] weightsk;
    public double lambdak;
    public double[] psik;
    public Vector<Stump> hypSet;
    public WorkerVariables(int noOfExamples,double beta, double[] weights, double lambdak, double[] psik, Vector<Stump> hyp){
        this.noOfExamples = noOfExamples;
        this.betak = beta;
        this.weightsk =weights;
        this.lambdak = lambdak;
        this.psik = psik;
        this.hypSet = hyp;
    }
}

