package produce;

import java.io.EOFException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class generateData {

    public List<List<String>> buildRDBValue(List<String> tagList) throws  Exception{
        List<List<String>> result = new ArrayList<>();
        String twigL = "asin";
        String twigR = "price";
        int count = 0;
        try{
            RandomAccessFile r_vl = new  RandomAccessFile("xjoin/src/produce/outputData/"+twigL+"_v","rw");//read value file
            RandomAccessFile r_vr = new  RandomAccessFile("xjoin/src/produce/outputData/"+twigR+"_v","rw");//read value file
            r_vl.seek(0);
            r_vr.seek(0);
            String valuel = null;
            String valuer = null;

            while ( (valuel=r_vl.readUTF()) != null && (valuer=r_vr.readUTF()) != null )
            {
                List<String> valueList = new ArrayList<>();
//                valuel = valuel+"_"+count;
//                valuer = valuer+"_"+count;
                valueList.addAll(Arrays.asList(valuel,valuer));
                result.add(valueList);
                count++;
                //System.out.println("build value:"+valuel + " "+valuer);
            }

        }
        catch (EOFException eofex) {
            //do nothing
        }
        catch(Exception e){
            System.out.println("e is:"+e);
        }
        ////System.out.println("original build RDB value count:"+count);
        return result;
    }
}
