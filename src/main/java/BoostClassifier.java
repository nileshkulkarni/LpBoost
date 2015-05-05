import java.util.Vector;
public class BoostClassifier {
    Vector<Float> alphas;
    Vector<Stump> hypotheses;


    public BoostClassifier(BoostClassifier b) {
        this.alphas = b.alphas;
        this.hypotheses = b.hypotheses;
    }



    public BoostClassifier(Vector<Float> alphas, Vector<Stump> hypotheses) {
        super();
        this.alphas = alphas;
        this.hypotheses = hypotheses;
    }       

    public int classify(DataPoint p){
        float class_sum = 0;
        for(int i=0; i<this.alphas.size();i++){
            class_sum+=this.alphas.elementAt(i)*this.hypotheses.elementAt(i).classify(p);
        }
        //System.out.println("DataPoint : "+ p.toString() + " Classsum " + Float.toString(class_sum));
        if(class_sum < 0.0)
            return -1;
        return 1;   
    }

}

