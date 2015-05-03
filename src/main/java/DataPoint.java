public class DataPoint {
    Vector<Float> v;
    public DataPoint(Vector<Float> v){ 
        this.v=v;
    }   
    public float getAttribute(int index){
        if(index<0||index>=this.v.size())throw new IndexOutOfBoundsException("Index " + index + " is out of bounds!");
        return (float) v.elementAt(index);
    }   
    public int size(){
        return this.v.size();
    }   
    public String toString(){
        String s="";
        for (int i =0;i<v.size();i++)
            s = s + Float.toString(v.get(i))+" ";
        return s;
    }   
    
}
