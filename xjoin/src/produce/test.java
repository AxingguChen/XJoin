package produce;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

import org.apache.commons.collections4.ListUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Created by zzzhou on 2017-06-29.
 */
public class test {
    //if array1 > array2, return true
    public static Boolean compareIntArray(int[] array1, int[] array2){
        Boolean flag = false;
        for(int i = 0;i<array2.length;){
            if(i<array1.length){
                if(array1[i] == array2[i]) i++;
                else if(array1[i] > array2[i]) {flag=true; break;}
                    else {flag=false;break;}
            }
            else {flag=false;break;}
        }
        return flag;
    }

    public static List<int[]> insert(int[] x, List<int[]> l){
        // loop through all elements
        for (int i = 0; i < l.size(); i++) {
            // if the element you are looking at is smaller than x,
            // go to the next element
            if ( compareIntArray(x,l.get(i))) continue;
            // otherwise, we have found the location to add x
            l.add(i, x);
            return l;
        }
        // we looked through all of the elements, and they were all
        // smaller than x, so we add ax to the end of the list
        l.add(x);
        return l;
    }
    public void getTagMap(List<String> tags)  throws Exception{
        List<List<String>> results = new ArrayList<>();
        for(String tag:tags){
            List<String> result = new ArrayList<>();
            try{
                //outputLabel.readUTF8_v(tag);
                RandomAccessFile r_v = null;
                r_v = new  RandomAccessFile("xjoin/src/produce/outputData/"+tag+"_v","rw");//read value file
                r_v.seek(0);
                String value = null;
                while ((value = r_v.readUTF()) != null ) {
                    result.add(value);
                }}
            catch (EOFException eofex) {
                //do nothing
            }
            catch(Exception e){
                System.out.println("e is "+e);
            }
            results.add(result);
        }

        PrintWriter pw = new PrintWriter(new File("xjoin/src/buildTest100wSmallResult.csv"));
        StringBuilder sb = new StringBuilder();
        sb.append("asin");
        sb.append('|');
        sb.append("price");
        sb.append('|');
        sb.append("OrderId");
        sb.append('\n');
        pw.write(sb.toString());
        int count=0;
        int tag2 = 0;
        for(int i=0;i<10;i++){
            StringBuilder sbb = new StringBuilder();
            for(int tag=0;tag<results.size();tag++) {
                if(tag==2){
                    sbb.append(tag2);
                    sbb.append('\n');
                    if(count==10){
                        count=0;
                        tag2++;
                    }
                }
                else {
                    sbb.append(results.get(tag).get(i));
                    sbb.append('|');
                    count++;
                }
            }
            pw.write(sbb.toString());
        }
        for(int i=0;i<999990;i++){
            StringBuilder sbb = new StringBuilder();
            for(int tag=0;tag<results.size();tag++) {
                sbb.append("no");
                if(tag==2){
                    sbb.append('\n');
                }
                else {
                    sbb.append('|');
                }
            }
            pw.write(sbb.toString());
        }
        pw.close();
        System.out.println("done!");
    }

    public void getTagMap_OtherDataSet()  throws Exception{
        PrintWriter pw = new PrintWriter(new File("xjoin/src/buildOther10wSmallResult.csv"));
        StringBuilder sb = new StringBuilder();
        sb.append("quantity");
        sb.append('|');
        sb.append("type");
        sb.append('|');
        sb.append("date");
        sb.append('|');
        sb.append("increase");
        sb.append('\n');
        pw.write(sb.toString());
        Random random = new Random();
        for(int i=0;i<100000;i++){
            StringBuilder sbb = new StringBuilder();
            for(int tag=0;tag<4;tag++) {
//                String a = random.nextInt(101) + "";//random.nextInt(max - min + 1) + min
                String a = "no_"+i;
                sbb.append(a);
                if(tag==3){
                    sbb.append('\n');
                }
                else {
                    sbb.append('|');
                }
            }
            pw.write(sbb.toString());
        }
        pw.close();
        System.out.println("done!");
    }

    public static int compareId(int[] id1, int[] id2){
        int compareResult = 0;
        //sizes are also need to be compared
        int id1_size = id1.length;
        int id2_size = id2.length;
        int commonSize = id1_size;
        if(id1_size > id2_size){
            commonSize = id2_size;
        }
        for(int i=0; i< commonSize;i++){
            if(id1[i] != id2[i]){
                if(id1[i] > id2[i]){
                    compareResult = 1;
                }
                else{
                    compareResult = -1;
                }
                break;
            }
        }
        if(compareResult == 0){
            if(id1_size > id2_size) compareResult = 1;
            else if(id1_size < id2_size) compareResult = -1;
        }

        return compareResult;
    }
    static public void main(String[] args) throws Exception {
        List<String> a = Arrays.asList("1","2","3");
        List<String> b = a.subList(0,3);
        System.out.println(b);

    }
    public static boolean isPC(List<String[]> tagList,String parent, String child){
        for(int i=0;i<tagList.size();i++){
            if(tagList.get(i)[0].equals(parent) && tagList.get(i)[1].equals(child)){
                return true;
            }
        }
        return false;
    }
}
