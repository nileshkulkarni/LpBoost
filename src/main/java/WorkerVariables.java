public class WorkerVariables{
    public int noOfExamples;
    public double betak;
    public double[] weightsk;
    public double lambdak;
    public double[] psik;
    public WorkerVariables(int noOfExamples,double beta, double[] weights, double lambdak, double[] psik){
        this.noOfExamples = noOfExamples;
        this.betak = beta;
        this.weightsk =weights;
        this.lambdak = lambdak;
        this.psik = psik;
    }
}

