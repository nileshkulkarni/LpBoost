import java.lang.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

public class PrimalVariables{
   public float rho =0f;
   public Vector<Float> alphas;
   int N;
   public PrimalVariables(){
       alphas = new Vector<Float>(); 
   }
   public PrimalVariables(int N){
       this.N = N;
       alphas = new Vector<Float>(); 
   }
   public PrimalVariables(PrimalVariables p){
        this.rho =p.rho;
        this.N = p.N;
        this.alphas = p.alphas; 
   }
}
