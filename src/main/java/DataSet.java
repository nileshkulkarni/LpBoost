import java.util.Vector;

class DataSet{
    public  Vector<DataPoint> examples ;
    public Vector<Integer> labels ;
    public DataSet(DataSet d){
        this.examples=d.examples;
        this.labels=d.labels;
    }
    public DataSet() {
        // TODO Auto-generated constructor stub
        this.examples=new Vector<DataPoint>();
        this.labels=new Vector<Integer>();
    }

}

