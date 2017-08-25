package tjFast;

import java.io.*;
import java.util.*;

/**
 * Created by zzzhou on 2017-06-27.
 * Final version of label Matching.
 */
public class labelMatching {
    static String runningResult="";
    static int readRDBcount = 0;
    // this function still need modification to meet analysis the tag name automatically
    public List<Vector> readRDBValue_line(List<String> tagList) throws Exception{
        List<Vector> result = new ArrayList<>();
        String csvFile = "xjoin/src/table.csv";
        String line = "";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            //read first line to locate the tags
            line = br.readLine();
            //split first line by "|"
            List<String> list = Arrays.asList(line.split("\\|"));
            //List<String> list = Arrays.asList(line.split(","));
            //initialize new list to store the column location of tags in tables
            List<Integer> tagLocation = new ArrayList<>();
            //@@here has a nested loop, cuz we need to read the table according to the query leaves order
            for (int i = 0; i < tagList.size(); i++) {
                for (int j = 0; j < list.size(); j++) {
                    if (tagList.get(i).equals(list.get(j))){
                        tagLocation.add(j);
                        break;
                    }
                }
            }
            while ((line = br.readLine()) != null) {

                // use comma as separator
                //String[] str = line.split(cvsSplitBy);
//                long T = System.currentTimeMillis();
                list = Arrays.asList(line.split("\\|"));
                //list = Arrays.asList(line.split(","));
//                long T1 = System.currentTimeMillis();
//                totalT = totalT + (T1-T);
                if(list.size() > tagLocation.get(tagLocation.size() - 1)){
                    //System.out.println("asin: " + list.get(0) + " price:" + list.get(2) );
                    //List<String> valueList = new ArrayList<>();
                    Vector v = new Vector();
                    //if(list.size() < 2) System.out.println("less than 2");
                    for (int i : tagLocation) {
                        //valueList.add(list.get(i));
                        List<int[]> idList = new ArrayList<>();
                        v.add(list.get(i));
                        v.add(idList);
                    }
                    readRDBcount++;
                    result.add(v);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        ////System.out.println("valid read RDB row:"+readRDBcount);
        return  result;
    }


//    public List<Match> buildRDBValue(String twigL, String twigR) throws  Exception{
//        List<Match> result = new ArrayList<>();
//        int count = 0;
//        try{
//            RandomAccessFile r_vl = new  RandomAccessFile("xjoin/src/produce/outputData/"+twigL+"_v","rw");//read value file
//            RandomAccessFile r_vr = new  RandomAccessFile("xjoin/src/produce/outputData/"+twigR+"_v","rw");//read value file
//            r_vl.seek(0);
//            r_vr.seek(0);
//            String valuel = null;
//            String valuer = null;
//
//            while ( (valuel=r_vl.readUTF()) != null && (valuer=r_vr.readUTF()) != null )
//            {
//                valuel = valuel+"_"+count;
//                valuer = valuer+"_"+count;
//                result.add(new Match(valuel,valuer,null,null));
//                count++;
//                //System.out.println("build value:"+valuel + " "+valuer);
//            }
//
//        }
//        catch (EOFException eofex) {
//            //do nothing
//        }
//        catch(Exception e){
//            System.out.println("e is:"+e);
//        }
//        ////System.out.println("original build RDB value count:"+count);
//        return result;
//    }

    public List<Vector> getTagMap(String tag)  throws Exception{
        List<Vector> tagList = new ArrayList<>();
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
            loadDataSet load = new loadDataSet();
            while ((value = r_v.readUTF()) != null) {
                byte len = r.readByte();
                int[] data = new int[len];
                String id = "";
                for (int i = 0; i < len; i++) {
                    data[i] = r.readUnsignedByte();
                }
                int [] result = load.convertToIntegers(data);
//                id = utilities.ArrayToString(result);
                //every row [value, id]
                Vector v = new Vector();
                //next line is only for build rdb value
                //value = value + "_"+m;
                m++;
                v.add(value);v.add(result);
                tagList.add(v);//value, id

            }}
        catch (EOFException eofex) {
            //do nothing
        }
        catch(Exception e){
            System.out.println("e is "+e);
        }
        long sortTagBeginTime = System.currentTimeMillis();
        Collections.sort(tagList,new Comparator<Vector>(){
            public int compare(Vector l1, Vector l2){
                return ((String) l1.get(0)).compareTo((String) l2.get(0));
            }}
        );
        long sortTagEndTime = System.currentTimeMillis();
        runningResult = runningResult +"\r\n"+"sort tag "+tag+", time:"+(sortTagEndTime-sortTagBeginTime);
        return tagList;
    }
    public class MyComparator implements Comparator<Vector> {
        int columnToSortOn;
        public MyComparator(int columnToSortOn) {
            this.columnToSortOn = columnToSortOn;
        }
        @Override
        public int compare(Vector l1, Vector l2) {
            return ((String) l1.get(columnToSortOn)).compareTo((String) l2.get(columnToSortOn));
        }
    }

        public List<Vector> matchValue(List<Vector> result, List<List<Vector>> tagLists){
        long totalSortTableTime = 0L;
        long sortStartTime;
        long sortEndTime;
        for(int tag = 0;tag<tagLists.size();tag++) {
            int i=0; int j = 0;
            //sort table according to tag, sort corresponding tag list
            sortStartTime = System.currentTimeMillis();
            Collections.sort(result,new MyComparator(tag*2));

            sortEndTime = System.currentTimeMillis();
            totalSortTableTime += sortEndTime-sortStartTime;
            List<Vector> tagList = tagLists.get(tag);
            while (i != result.size() && j != tagList.size()) {
                String table_value = (String) result.get(i).get(tag*2);

                //String rightValue = result.get(i).getR_v();
                //List<String> id_list = new ArrayList<>();//To store id list for every row
                String tag_value = (String) tagList.get(j).get(0); // 0-value, 1-id
                int compare_result = table_value.compareTo(tag_value);
                if (compare_result == 0) { //equals
                    ((List<int[]>) result.get(i).get(tag*2+1)).add((int[]) tagList.get(j).get(1));// add corresponding tag id
                    if ((j + 1) != tagList.size())
                        j++;
                    else {//tag list has goes to the end, so add the iterator of the table list
                        i++;
                    }
                } else if (compare_result < 0) { // table_value < tag_value
                    //previous table value equals current table value
                    if (i != result.size() && i > 0 && table_value.equals(result.get(i - 1).get(tag*2))) {
                        ((List<int[]>) result.get(i).get(tag * 2 + 1)).addAll((List<int[]>) result.get(i - 1).get(tag * 2 + 1));
                    }
                        i++;

                } else if (compare_result > 0) { // table_value > tag_value
                    j++;
                }
            }
        }
        runningResult=runningResult+"\r\n"+"Total sort RDB value time is " + totalSortTableTime;
        return result;
    }


    public List<Vector> getSolution(List<String> tags)  throws Exception{
        labelMatching m = new labelMatching();
        //left_tag/right_tag -> left/right id list
        long loadbeginTime = 0L;
        long loadendTime = 0L;
        long loadRDBbeginTime = 0L;
        long loadRDBendTime = 0L;
        long matchbeginTime = 0L;
        long matchendTime = 0L;
        long totalMatchTime = 0L;


        //Load tag value and id
        List<List<Vector>> tagLists = new ArrayList<>();
        //System.out.println("load tag map");
        loadbeginTime = System.currentTimeMillis();
        for(String tag:tags){
            List<Vector> tagList = m.getTagMap(tag);
            tagLists.add(tagList);
        }
        loadendTime = System.currentTimeMillis();
        ////System.out.println("Total load tag value and ID time is(include sort tag time)" + (loadendTime-loadbeginTime ));

        runningResult=runningResult + "\r\n"+"Total load tag value and ID time is(include sort table time)" + (loadendTime-loadbeginTime );
        //System.out.println(leftTag+" "+left_tag);
        //System.out.println(rightTag+" "+right_tag);

        //Load RDB value
        //System.out.println("load RDB value");
        loadRDBbeginTime = System.currentTimeMillis();
        List<Vector> result =m.readRDBValue_line(tags);
        //List<Match> result =m.buildRDBValue(leftTag,rightTag);
        //List<Match> result =m.readRDBValue(leftTag,rightTag);
        loadRDBendTime = System.currentTimeMillis();
        //System.out.println("valid read RDB row:"+readRDBcount);
        ////System.out.println("Total load RDB tag value time is " + (loadRDBendTime-loadRDBbeginTime ));
        runningResult=runningResult+"\r\n"+"Total load RDB tag value time is " + (loadRDBendTime-loadRDBbeginTime );

        //System.out.println(result);

        //System.out.println("Start match, sort left");

        //System.out.println("match rdb & xml");
        matchbeginTime = System.currentTimeMillis();
        result = m.matchValue(result,tagLists);
        matchendTime = System.currentTimeMillis();
        totalMatchTime = matchendTime - matchbeginTime;

        ////System.out.println("total sort table value time: " + totalSortTime);

        ////System.out.println("total match xml and RDB value time(include sort each row ID time): " + totalMatchTime);
        runningResult = runningResult+"\r\n"+"total match xml and RDB value time(include sort rdb time): " + totalMatchTime;
        //System.out.println(result + " size:"+result.size());
        System.out.println("before remove candidates size:"+result.size());
        List<Vector> matchResult = new ArrayList<>();
        for(Vector v:result){
            Boolean flag=true;
            for(int tag = 0; tag<(v.size()/2); tag++){
                if(((List<int[]>) v.get(tag*2+1)).size() == 0){
                    flag=false;
                    break;
                }
            }
            if(flag==true){
                matchResult.add(v);
            }
        }
        runningResult = runningResult+"\r\n"+"candidate size:"+matchResult.size();
//        //System.out.println("write result to file");
//        try {
//            BufferedWriter out = new BufferedWriter(new FileWriter("xjoin/src/testResult.txt"));
//            out.write("Result\r\n"+runningResult);  //Replace with the string
//            //you are trying to write
//            out.close();
//        }
//        catch (IOException e)
//        {
//            System.out.println("Exception ");
//
//        }
        //System.out.println("return !");
        ////System.out.println("remove count:"+remove_count+ " after remove candidate size:"+result.size());
        System.out.println(runningResult);
        return matchResult;
    }

    public static void main(String[] args) throws Exception{
        labelMatching lm = new labelMatching();
        //List<Match> re = lm.getSolution("b","c");
        List<Vector> re = lm.getSolution(Arrays.asList("asin","price"));

        //System.out.println(re);
//        lm.readRDBValue_line("asin","price");
//        lm.getTagMap("asin");
    }
}