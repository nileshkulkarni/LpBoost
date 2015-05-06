public class MasterVariables{
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
        /*
        for(int i =0;i<noOfExamples;i++){
            System.out.println("psik " + Integer.toString(i) + " " + Double.toString(psik[i]));
        }
        */
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
