public class Stump {
    int index;
    boolean orientation;
    public Stump(int index,boolean orientation){
        this.index=index;
        this.orientation=orientation;
    }   
                    
    public int classify(DataPoint p){ 
        if (this.orientation == true){
            if(p.getAttribute(index) < 0.5)
                return -1; 
            return 1;
        }   
        else{
            if(p.getAttribute(index) < 0.5)
                return 1;
            return -1; 
        }   
    }   

    @Override
    public String toString() {
        return "Stump [index=" + index + ", orientation=" + orientation + "]";
    }   
            

}

