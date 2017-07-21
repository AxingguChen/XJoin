package produce;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import tjFast.*;

import java.io.*;
import java.util.*;

/**
 * Created by zzzhou on 2017-06-27.
 * Final version of label Matching.
 */
public class labelMatching {
    static String runningResult="";
    static int readRDBcount = 0;
    public static class Match {
        private String leftTagValue;
        private String rightTagValue;
        private List leftTagID;
        private List rightTagID;
        Match(String l_v, String r_v, List l_id, List r_id) {
            this.leftTagValue = l_v;
            this.rightTagValue = r_v;
            this.leftTagID = l_id;
            this.rightTagID = r_id;
        }
        void set_LID(List l_id){this.leftTagID = l_id;}
        void set_RID(List r_id){this.rightTagID = r_id;}
        public String toString() {
            return String.format("{%s , %s , %s , %s}", leftTagValue, rightTagValue, leftTagID,rightTagID);
        }
        String getL_v() { return leftTagValue; }
        String getR_v() { return rightTagValue; }
        public List getL_ID(){ return leftTagID;}
        public List getR_ID(){ return rightTagID;}
    }

    // this function still need modification to meet analysis the tag name automatically
    public List<Match> readRDBValue_line(String twigL, String twigR) throws Exception{
        List<Match> result = new ArrayList<>();
        String csvFile = "xjoin/src/table.csv";
        String line = "";
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            while ((line = br.readLine()) != null) {

                // use comma as separator
                //String[] str = line.split(cvsSplitBy);

                List<String> list = Arrays.asList(line.split("\\|"));
                if(list.size() > 2){
                    //System.out.println("asin: " + list.get(0) + " price:" + list.get(2) );
                    readRDBcount++;
                    result.add(new Match(list.get(0),list.get(2),null,null));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        ////System.out.println("valid read RDB row:"+readRDBcount);
        return  result;
    }


    public List<Match> readRDBValue(String twigL, String twigR) throws Exception{
        List<Match> result = new ArrayList<>();
        IdentityHashMap<String,String> tableMap = new IdentityHashMap<>(); // save matched value pair
        File file = new File("xjoin/src/table.xlsx");
        Integer twigL_n=null; //The position of left tag and right tag in RDB table
        Integer twigR_n=null;
        FileInputStream fIP = new FileInputStream(file);//read excel file
        XSSFWorkbook workbook = new XSSFWorkbook(fIP);
        Sheet sheet = workbook.getSheetAt(0);
        //Find specified columns by comparing column names in first row
        Row first_row = sheet.getRow(0);
        for (int cn=first_row.getFirstCellNum(); cn<first_row.getLastCellNum(); cn++) {
            Cell c = first_row.getCell(cn);
            if(c.toString().equals(twigL)){
                twigL_n = cn;
            }
            if(c.toString().equals(twigR)){
                twigR_n = cn;
            }
        }
        // if left child and right child of twig exists in RDB table
        if(twigL_n != null && twigR_n != null){
            //Compare values
            for (Row row:sheet){
                int i = row.getRowNum();
                // if cell is numerical value, start from second row(first row is name of tags)
                if(i != 0){
                    Cell cellL = row.getCell(twigL_n);
                    Cell cellR = row.getCell(twigR_n);
                    String left = null;
                    String right = null;
                    // check value type since different type value need to be read by different method
                    //left
                    if(cellL.getCellTypeEnum()== CellType.NUMERIC){
                        left = String.valueOf((int)cellL.getNumericCellValue());
                    }
                    else if(cellL.getCellTypeEnum()== CellType.STRING){
                        left = cellL.getStringCellValue();
                    }
                    //right
                    if(cellR.getCellTypeEnum()== CellType.NUMERIC){
                        right = String.valueOf((int)cellR.getNumericCellValue());
                    }
                    else if(cellR.getCellTypeEnum()== CellType.STRING){
                        right = cellR.getStringCellValue();
                    }

                    //Add table value to map
                    result.add(new Match(left,right,null,null));

                }
                //tableMap.put(cellL.getStringCellValue(),cellR.getStringCellValue());
            }
        }
        else {System.out.println("The twig have not been found in RDB table.");}
        return  result;
    }

     public List<Match> buildRDBValue(String twigL, String twigR) throws  Exception{
     List<Match> result = new ArrayList<>();
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
         valuel = valuel+"_"+count;
         valuer = valuer+"_"+count;
        result.add(new Match(valuel,valuer,null,null));
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


    public List<Match> buildRDBValue_100(){
        List<Match> result = new ArrayList<>();

        result.add(new Match("B001CXLSSW","37.94",null,null));
        result.add(new Match("B00IL2QNQO","38.97",null,null));
        result.add(new Match("B00FR84QG4","59.95",null,null));
        result.add(new Match("B001QBZIMC","6.93",null,null));
        result.add(new Match("B000UUBFZY","9.99",null,null));
        result.add(new Match("B0093Q8JCI","22.9",null,null));
        result.add(new Match("B004J41HQY","85.55",null,null));
        result.add(new Match("B000NMBLDU","14.95",null,null));
        result.add(new Match("B001BS98N0","11.95",null,null));
        result.add(new Match("B005G2G2OU","172.95",null,null));
        int count;
        List a = new ArrayList();
        a.add("aaaa");
        for(count=10; count<100;count++){
        result.add(new Match("aaa","bbb",a,null));
        }


        ////System.out.println("original build RDB value count:"+count);
        return result;
    }


    public List<List<String>> getTagMap(String tag)  throws Exception{
        List<List<String>> tagList = new ArrayList<>();
        int m=0;
        try{
            //outputLabel.readUTF8_v(tag);
            RandomAccessFile r = null;
            RandomAccessFile r_v = null;
            r = new  RandomAccessFile("xjoin/src/produce/outputData/"+tag,"rw");//read id file
            r_v = new  RandomAccessFile("xjoin/src/produce/outputData/"+tag+"_v","rw");//read value file
            r_v.seek(0);
            String value = null;
            loadDataSet lds = new loadDataSet();
            while ((value = r_v.readUTF()) != null) {
                byte len = r.readByte();
                int[] data = new int[len];
                String id = "";
                for (int i = 0; i < len; i++) {
                    data[i] = r.readUnsignedByte();
                    id = id + "/" + data[i];
                }
                int [] result = convertToIntegers(data);
                id = utilities.ArrayToString(result);
                List<String> l = new ArrayList<>();//every row [value, id]
                //value = value + "_"+m;
                m++;
                l.add(value);l.add(id);
                tagList.add(l);//value, id

            }}
        catch (EOFException eofex) {
            //do nothing
        }
        catch(Exception e){
            System.out.println("e is "+e);
        }
        long sortTagBeginTime = System.currentTimeMillis();
        Collections.sort(tagList,new Comparator<List<String>>(){
            public int compare(List<String> l1, List<String> l2){
                return l1.get(0).compareTo(l2.get(0));
            }}
        );
        long sortTagEndTime = System.currentTimeMillis();
        ////System.out.println("sort tag "+tag+", time:"+(sortTagEndTime-sortTagBeginTime));
        ////System.out.println("tag row:"+m);
        runningResult = runningResult +"\r\n"+"sort tag "+tag+", time:"+(sortTagEndTime-sortTagBeginTime);
        return tagList;
    }
    int[] convertToIntegers(int[] data) {

        int inputi = 0, outputi = 0;

        int output[] = new int[data.length];

        while (inputi < data.length)
            if (data[inputi] < 128) {
                int a[] = new int[1];
                a[0] = data[inputi++];
                output[outputi++] = UTF8ToInteger(a);
            } else if (data[inputi] < 224) {
                int a[] = new int[2];
                a[0] = data[inputi++];
                a[1] = data[inputi++];
                output[outputi++] = UTF8ToInteger(a);
            } else if (data[inputi] < 240) {
                int a[] = new int[3];
                a[0] = data[inputi++];
                a[1] = data[inputi++];
                a[2] = data[inputi++];
                output[outputi++] = UTF8ToInteger(a);
            } else if (data[inputi] < 248) {
                int a[] = new int[4];
                a[0] = data[inputi++];
                a[1] = data[inputi++];
                a[2] = data[inputi++];
                a[3] = data[inputi++];
                output[outputi++] = UTF8ToInteger(a);
            } else if (data[inputi] < 252) {
                int a[] = new int[5];
                a[0] = data[inputi++];
                a[1] = data[inputi++];
                a[2] = data[inputi++];
                a[3] = data[inputi++];
                a[4] = data[inputi++];
                output[outputi++] = UTF8ToInteger(a);
            }
        int[] result = new int[outputi];

        for (int i = 0; i < outputi; i++)
            result[i] = output[i];

        return result;

    }//end convertToIntegers


    static int UTF8ToInteger (int []  bytes){

        if (bytes.length == 1)
        {
            return bytes[0];
        }//end
        else if (bytes.length == 2)
        {
            bytes[0]= bytes[0] ^ 192 ;
            bytes[1]= bytes[1] ^ 128 ;
            bytes[0]= bytes[0] << 6 ;

            return (bytes[0] | bytes[1]);

        }//end else
        else if (bytes.length == 3)
        {	bytes[0]= bytes[0] ^ 224 ;
            bytes[1]= bytes[1] ^ 128 ;
            bytes[2]= bytes[2] ^ 128 ;

            bytes[1]= bytes[1] << 6 ;

            bytes[1] = bytes[1] | bytes[2];

            bytes[0]= bytes[0] << 12 ;


            return (bytes[0] | bytes[1]);
        }//end else
        else if (bytes.length == 4)
        {	bytes[0]= bytes[0] ^ 240 ;
            bytes[1]= bytes[1] ^ 128 ;
            bytes[2]= bytes[2] ^ 128 ;
            bytes[3]= bytes[3] ^ 128 ;

            bytes[2]= bytes[2] << 6 ;

            bytes[2] = bytes[2] | bytes[3];

            bytes[1]= bytes[1] << 12 ;

            bytes[1] = bytes[1] | bytes[2];

            bytes[0]= bytes[0] << 18 ;

            return (bytes[0] | bytes[1]);

        }//end else
        else if (bytes.length == 5)
        {	bytes[0]= bytes[0] ^ 248 ;
            bytes[1]= bytes[1] ^ 128 ;
            bytes[2]= bytes[2] ^ 128 ;
            bytes[3]= bytes[3] ^ 128 ;
            bytes[4]= bytes[4] ^ 128 ;

            bytes[3]= bytes[3] << 6 ;

            bytes[3] = bytes[3] | bytes[4];

            bytes[2]= bytes[2] << 12 ;

            bytes[2] = bytes[2] | bytes[3];

            bytes[1]= bytes[1] << 18 ;

            bytes[1] = bytes[1] | bytes[2];

            bytes[0]= bytes[0] << 24 ;

            return (bytes[0] | bytes[1]);

        }//end else

        return 0;
    }//end convertToUTF8

    // tFlag{left,right} -> left column value or right column value of table
    public void matchValue(List<Match> result, List<List<String>> tagList, String tFlag){
        if(tFlag.equals("left")) {
            int i=0; int j = 0;
            while(i != result.size() && j != tagList.size()){
                String table_value = result.get(i).getL_v();
                //String rightValue = result.get(i).getR_v();
                List id_list = new ArrayList<>();//To store id list for every row
                String tag_value = tagList.get(j).get(0); // 0-value, 1-id
                int compare_result = table_value.compareTo(tag_value);
                if (compare_result == 0) { //equals
                    if(result.get(i).getL_ID() != null)
                        id_list = result.get(i).getL_ID();
                    id_list.add(tagList.get(j).get(1));// add corresponding tag id
                    //Collections.sort(id_list);
                    result.get(i).set_LID(id_list);
                    if(j+1 != tagList.size())
                        j++;
                    else {///**
                        id_list=result.get(i).getL_ID();
                        if(id_list != null){
                            Collections.sort(id_list);
                            result.get(i).set_LID(id_list);
                        }//*/
                        i++;
                    }
                }
                else if (compare_result < 0){ // table_value < tag_value
                    ///**
                    id_list=result.get(i).getL_ID();
                    if(id_list != null){
                        Collections.sort(id_list);
                        result.get(i).set_LID(id_list);
                    }//*/
                    i++;
                    //previous table value equals current table value
                    if(i!= result.size() && table_value.equals(result.get(i).getL_v())){
                        id_list = result.get(i-1).getL_ID();
                        result.get(i).set_LID(id_list);
                        i++;
                    }
                }
                else if(compare_result > 0){ // table_value > tag_value
                    j++;
                }
            }
        }

        else if(tFlag.equals("right")) {
            int i=0; int j = 0;
            while(i != result.size() && j != tagList.size()){
                String table_value = result.get(i).getR_v();
                List<String> id_list = new ArrayList<>();//To store id list for every row
                String tag_value = tagList.get(j).get(0); // 0-value, 1-id
                int compare_result = table_value.compareTo(tag_value);
                if (compare_result == 0) { //equals
                    if(result.get(i).getR_ID() != null)
                        id_list = result.get(i).getR_ID();
                    id_list.add(tagList.get(j).get(1));// add corresponding tag id
                    //Collections.sort(id_list);
                    result.get(i).set_RID(id_list);
                    if(j+1 != tagList.size())
                        j++;
                    else{///**
                        id_list=result.get(i).getR_ID();
                        if(id_list != null){
                            Collections.sort(id_list);
                            result.get(i).set_RID(id_list);
                        }//*/
                        i++;
                    }
                }
                else if (compare_result < 0){ // table_value < tag_value
                    ///**
                    id_list=result.get(i).getR_ID();
                    if(id_list != null){
                        Collections.sort(id_list);
                        result.get(i).set_RID(id_list);
                    }//*/
                    i++;
                    //previous table value equals current table value
                    if(i!= result.size() && table_value.equals(result.get(i).getR_v())){
                        id_list = result.get(i-1).getR_ID();
                        result.get(i).set_RID(id_list);
                        i++;
                    }
                }
                else if(compare_result > 0){ // table_value > tag_value
                    j++;
                }
            }
        }
    }

    public List<Match> getSolution(String leftTag, String rightTag)  throws Exception{
        labelMatching m = new labelMatching();
        //left_tag/right_tag -> left/right id list
        long loadbeginTime = 0L;
        long loadendTime = 0L;
        long loadRDBbeginTime = 0L;
        long loadRDBendTime = 0L;
        long sortbeginTime = 0L;
        long sortendTime = 0L;
        long totalSortTime = 0L;
        long matchbeginTime = 0L;
        long matchendTime = 0L;
        long totalMatchTime = 0L;
        long startTimeWithoutLoadData = 0L;
        long endTimeWithoutLoadData = 0L;

        //Load tag value and id
        //System.out.println("load tag map");
        loadbeginTime = System.currentTimeMillis();
        List<List<String>> left_tag = m.getTagMap(leftTag);
        List<List<String>> right_tag = m.getTagMap(rightTag);
        loadendTime = System.currentTimeMillis();
        ////System.out.println("Total load tag value and ID time is(include sort tag time)" + (loadendTime-loadbeginTime ));

        runningResult=runningResult + "\r\n"+"Total load tag value and ID time is(include sort tag time)" + (loadendTime-loadbeginTime );
        //System.out.println(leftTag+" "+left_tag);
        //System.out.println(rightTag+" "+right_tag);

        //Load RDB value
        //System.out.println("load RDB value");
        loadRDBbeginTime = System.currentTimeMillis();
        List<Match> result =m.readRDBValue_line(leftTag,rightTag);
        //List<Match> result =m.buildRDBValue(leftTag,rightTag);
        //List<Match> result =m.readRDBValue(leftTag,rightTag);
        loadRDBendTime = System.currentTimeMillis();
        //System.out.println("valid read RDB row:"+readRDBcount);
        ////System.out.println("Total load RDB tag value time is " + (loadRDBendTime-loadRDBbeginTime ));
        runningResult=runningResult+"\r\n"+"Total load RDB tag value time is " + (loadRDBendTime-loadRDBbeginTime );

        //System.out.println(result);

        //System.out.println("Start match, sort left");
        startTimeWithoutLoadData = System.currentTimeMillis();
        sortbeginTime = System.currentTimeMillis();
        Comparator<Match> comparator = Comparator.comparing(Match::getL_v);
        result.sort(comparator);
        sortendTime = System.currentTimeMillis();
        totalSortTime = sortendTime-sortbeginTime;

        //System.out.println("match left");
        matchbeginTime = System.currentTimeMillis();
        m.matchValue(result,left_tag,"left");
        matchendTime = System.currentTimeMillis();
        totalMatchTime = matchendTime - matchbeginTime;
        //System.out.println("Start sort right");
        sortbeginTime = System.currentTimeMillis();
        comparator = Comparator.comparing(Match::getR_v);
        result.sort(comparator);
        sortendTime = System.currentTimeMillis();
        totalSortTime = totalSortTime + sortendTime-sortbeginTime;
        //System.out.println("match right");
        matchbeginTime = System.currentTimeMillis();
        m.matchValue(result,right_tag,"right");
        matchendTime = System.currentTimeMillis();
        totalMatchTime = totalMatchTime + matchendTime - matchbeginTime;

        //System.out.println(result);

        ////System.out.println("total sort table value time: " + totalSortTime);
        runningResult = runningResult+"\r\n"+"total sort table value time: " + totalSortTime;

        ////System.out.println("total match xml and RDB value time(include sort each row ID time): " + totalMatchTime);
        runningResult = runningResult+"\r\n"+"total match xml and RDB value time(include sort each row ID time): " + totalMatchTime;
        //System.out.println(result + " size:"+result.size());
        int i = 0;
        int remove_count=0;
        //System.out.println("start remove candidates");
        Long removeStartTime = System.currentTimeMillis();
        while(i != result.size()){
            //System.out.println("i:"+i+" l:" + result.get(i).getL_ID() + " r:"+result.get(i).getR_ID());
            if(result.get(i).getL_ID() == null || result.get(i).getR_ID() == null)
            {
                //System.out.println("remove result:"+result.get(i).getL_v()+" "+result.get(i).getR_v());
                //System.out.println("ID list:"+result.get(i).getL_ID()+" "+result.get(i).getR_ID());
                result.remove(i);
                i--;
                remove_count++;
            }
            i++;
        }
        endTimeWithoutLoadData = System.currentTimeMillis();
        ////System.out.println("remove ID empty row time:"+(endTimeWithoutLoadData-removeStartTime));
        runningResult = runningResult+"\r\n"+"remove ID empty row time:"+(endTimeWithoutLoadData-removeStartTime);
        ////System.out.println("total get candidate time without load data: " + (endTimeWithoutLoadData-startTimeWithoutLoadData));
        runningResult = runningResult+"\r\n"+"total get candidate time without load data: " + (endTimeWithoutLoadData-startTimeWithoutLoadData);
        runningResult = runningResult+"\r\n"+"candidate size:"+result.size();
        //System.out.println("write result to file");
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("xjoin/src/testResult.txt"));
            out.write("Result\r\n"+runningResult);  //Replace with the string
            //you are trying to write
            out.close();
        }
        catch (IOException e)
        {
            System.out.println("Exception ");

        }
        //System.out.println("return !");
        ////System.out.println("remove count:"+remove_count+ " after remove candidate size:"+result.size());
        System.out.println(runningResult);
        return result;
    }

    public static void main(String[] args) throws Exception{
        labelMatching lm = new labelMatching();
        List<Match> re = lm.getSolution("b","c");
        //List<Match> re = lm.getSolution("asin","price");
        //System.out.println(re);
        //lm.readRDBValue_line("asin","price");
    }
}