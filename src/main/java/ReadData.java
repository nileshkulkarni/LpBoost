import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class ReadData{
    public static DataSet buildVector(String file,String delimRegex) throws IOException,NumberFormatException, FileNotFoundException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            DataSet d = new DataSet();
            String line;
            while ((line = br.readLine()) != null) {
               // process the line
                if(line.length()>1){
                    delimRegex=" ";
                    System.out.println("delimRegex : " + delimRegex);
                    String[] lines=line.split(delimRegex,2);
                    //String[] lines=line.split(" ",2);
                    //System.out.println("Line[0] " + lines[0]);
                    Integer b=Integer.parseInt(lines[0]);
                    d.labels.add(Integer.parseInt(lines[0]));

                    Vector<Float>v=new Vector<Float>();
                    for (String retval: lines[1].split(delimRegex)){
                        v.add(Float.parseFloat(retval));
                     }
                    d.examples.add(new DataPoint(v));

                }
            }
            return d;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    } // end of buildVector
} // end of class
