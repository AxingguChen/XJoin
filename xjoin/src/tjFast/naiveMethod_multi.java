package tjFast;

import java.io.*;
import java.util.*;

/**
 * Created by zzzhou on 2017-07-19.
 */
public class naiveMethod_multi {
    static int readRDBcount = 0;
    static List<List<List<String>>> myTables = new ArrayList<>();
    //get xml value,id pair list
    public List<List<String>> getValuePair(List<List<String>> pairIDList,List<HashMap<String, String>> allTagIDValue) throws Exception {
        List<List<String>> resultList = new ArrayList<>();
        //loop each pair of result ID and find its value
        for (List<String> p : pairIDList) {
            List<String> valueIDList = new ArrayList<>();
            //loop each tag of one pair-----Here we set i<p.size() to i<2
            for (int i = 0; i < p.size(); i++) {
                //the tag order is the same when load allTagIDValue
                String id = p.get(i);
                String value = allTagIDValue.get(i).get(id);
                valueIDList.add(value);
                valueIDList.add(id);
            }

            resultList.add(valueIDList);
        }

        //[value1,id1, value2,id2]
        return resultList;

    }
    static int numCount = 0;
    static int allCount = 0;
    public HashMap<String, String> getTagMap(String tag) throws Exception {
        HashMap<String, String> tagMap = new HashMap();
        int m = 0;
        try {
            RandomAccessFile r = new RandomAccessFile("xjoin/src/produce/outputData/" + tag, "rw");//read id file
            RandomAccessFile r_v = new RandomAccessFile("xjoin/src/produce/outputData/" + tag + "_v", "rw");//read value file
            r_v.seek(0);
            String value = null;
            loadDataSet lds = new loadDataSet();
            while ((value = r_v.readUTF()) != null) {
                byte len = r.readByte();
                int[] data = new int[len];
                String id = "";
                for (int i = 0; i < len; i++) {
                    data[i] = r.readUnsignedByte();
                    //id = id + "/" + data[i];
                }
                int [] result = lds.convertToIntegers(data);
                id = utilities.ArrayToString(result);
                tagMap.put(id, value);
//                if(id.equals("/128/5/2")){
//                    System.out.println("EXIST");
//                }

                //result = result+"/r/n"+id+","+value;
            }


        } catch (EOFException eofex) {
            //do nothing
        } catch (Exception e) {
            System.out.println("e is " + e);
        }
        return tagMap;
    }



    //read RDB value and merge list to myTables.
    public void readRDB() throws Exception{
        File directory = new File("xjoin/src/multi_rdbs/testTables");
        for(File f: directory.listFiles()){
            String line = "";
            Boolean firstLine = true;
            List<List<String>> rdb = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                while ((line = br.readLine()) != null) {
                    List<String> vec = new ArrayList<>();
                    if(firstLine){
                        vec.addAll(Arrays.asList(line.split("\\s*,\\s*")));
                        firstLine = false;
                    }
                    else{
                        String[] values = line.split("\\s*,\\s*");
                        //                    if()
                        for(String s:values){
                            vec.add(s);
                            vec.add(null);
                        }
                    }
//                    vec.addAll(Arrays.asList(line.split("\\s*,\\s*")));// "\\|"
                    rdb.add(vec);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            myTables.add(rdb);
        }
    }


    public List<List<String>> joinValue(List<List<String>> xmlList, List<List<String>> rdbValue){
        int i=0; int j = 0;
        List<List<String>> joinList = new ArrayList<>();
        //i -> row number of rdb table, j-> row number of xmlList
        while(i != rdbValue.size() && j != xmlList.size()){
            //get the first column value of rdb table
            String table_value = rdbValue.get(i).get(0);
            // odd-value, even-id. 0 is value
            String xml_value = xmlList.get(j).get(0);
//            if(j == 5140){
//
//                System.out.println();
//            }
//
//            if(j==35473){
//
//                System.out.println();
//            }
//            if(xmlList.get(j).get(5).equals("/1/144/1")){
//
//                System.out.println();
//            }
            int compare_result = table_value.compareTo(xml_value);
            if (compare_result == 0) { //equals
                Boolean flag = true;
                for(int tagCursor=1;tagCursor<rdbValue.get(i).size();tagCursor++){
                    //get the first column value of rdb table
                    String table_nextValue = rdbValue.get(i).get(tagCursor);
                    // odd-value, even-id. 0 is value
                    String xml_nextValue = xmlList.get(j).get(tagCursor*2);

                    int compare = table_nextValue.compareTo(xml_nextValue);
                    flag = ((compare==0)&&flag);
                    if(compare<0) {i++;break;}
                    else if(compare >0) {j++;break;}
                }
                //if match successfully, move xml cursor to next row
                if(flag==true){
                    joinList.add(xmlList.get(j));
                    j++;}

            }
            else if (compare_result < 0){ // table_value < tag_value
                i++;
            }
            else if(compare_result > 0){ // table_value > tag_value
                //xmlList.remove(j);
                j++;
            }
        }
        return joinList;
    }

    public int getResult(List<List<String>> solutionPairIDList,List<HashMap<String, String>> allTagIDValue) throws Exception{
        //rdb table
        //load
        List<String> leaves = Query.getLeaves();

        //for double layer AD only
        leaves.add(leaves.get(0));
        leaves.remove(0);

//        //List<String> leaves = A
        long loadRDBbeginTime = System.currentTimeMillis();
        readRDB();
        List<List<List<String>>> rdbValue = myTables;
////        //List<List<String>> rdbValue = buildRDBValue(leaves);
        long loadRDBendTime = System.currentTimeMillis();
        System.out.println("load rdb table data time is " + (loadRDBendTime - loadRDBbeginTime));


//        //sort
//        long sortRDBbeginTime = System.currentTimeMillis();
//        Collections.sort(rdbValue,new Comparator<List<String>>(){
//            public int compare(List<String> l1, List<String> l2){
//                int length = l1.size();
//                int result = 0;
//                for(int i=0; i<length; i++){
//                    int compa = (l1.get(i)).compareTo(l2.get(i));
//                    if(compa < 0){
//                        result = -1;
//                        break;
//                    }
//                    else if(compa == 0)
//                        result = 0;
//                    else {result = 1;break;}
//                }
//                return result;
//            }}
//        );
//        long sortRDBendTime = System.currentTimeMillis();
//        System.out.println("sort rdb data time is " + (sortRDBendTime - sortRDBbeginTime));
//        System.out.println("rdbValue:"+rdbValue.size());

//        try {
//            for(List<String> l:rdbValue){
//
//                BufferedWriter out = new BufferedWriter(new FileWriter("xjoin/src/rdbValue.txt",true));
//                out.write(l+"\r\n");  //Replace with the string
//                //you are trying to write
//                out.close();
//            }}
//        catch (IOException e)
//        {
//            System.out.println("Exception ");
//
//        }
        //XML
        //load
        long loadbeginTime = System.currentTimeMillis();

        //for AD double layer
        List<HashMap<String, String>> ADDouble = new ArrayList<>();
        ADDouble.add(allTagIDValue.get(1));
        ADDouble.add(allTagIDValue.get(2));
        ADDouble.add(allTagIDValue.get(0));
        List<List<String>> xmlList = getValuePair(solutionPairIDList,ADDouble);
//        List<List<String>> xmlList = getValuePair(solutionPairIDList,allTagIDValue);
        long loadendTime = System.currentTimeMillis();
        System.out.println("Find xml value by id time is " + (loadendTime - loadbeginTime));
        System.out.println("xml List. get(0):"+xmlList.get(0));
        System.out.println("xml List. get(1):"+xmlList.get(1));
        System.out.println("xml List. get(2):"+xmlList.get(2));
        //sort value pair list according to the first value of each pair
        long sortbeginTime = System.currentTimeMillis();
        Collections.sort(xmlList,new Comparator<List<String>>(){
            public int compare(List<String> l1, List<String> l2){
                int length = l1.size();
                int result = 0;
                for(int i=0; i<length ; i=i+2){
//                    System.out.println("i:"+i);
//                    System.out.println("L1:"+l1.get(i)+",L2:"+l2.get(i));
                    int compa = (l1.get(i)).compareTo(l2.get(i));
//                    System.out.println(compa);
                    if(compa < 0){
                        result = -1;
                        break;
                    }
                    else if(compa == 0)
                        result = 0;
                    else {result = 1;break;}
                }
                return result;
            }}
        );
        long sortendTime = System.currentTimeMillis();
        System.out.println("sort xml data time is " + (sortendTime - sortbeginTime));
        System.out.println("xmlList:"+xmlList.size());



        long joinbegintime = System.currentTimeMillis();
//        List<List<String>> resultList = joinValue(xmlList,rdbValue);
        long joinendtime = System.currentTimeMillis();
        System.out.println("join xml&rdb data time is " + (joinendtime - joinbegintime));

//        System.out.println(resultList);

        //System.out.println("final result:"+resultList);
        //following code is to test if all the result is exist in rdb table
//        Boolean exsit = false;
//        Boolean res = true;
//        for(List<String> l:xmlList){
//            String asin = l.get(0);
//            for(List<String> s:rdbValue){
//                String tableAsin = s.get(0);
//                if(asin.equals(tableAsin)){
//                    exsit = true;
//                    break;
//                }
//                else exsit=false;
//            }
//            res = (res && exsit);
//        }
//        System.out.println("correct or not?"+res);
//        try {
//            for(List<String> l:resultList){
//
//                BufferedWriter out = new BufferedWriter(new FileWriter("xjoin/src/naiveResult.txt",true));
//                out.write(l+"\r\n");  //Replace with the string
//                //you are trying to write
//                out.close();
//            }}
//            catch (IOException e)
//            {
//                System.out.println("Exception ");
//
//            }

//        try (BufferedReader br = new BufferedReader(new FileReader("xjoin/src/xjoinDoubleLayerResultCountFull.txt"))) {
//
//            String sCurrentLine;
//
//            while ((sCurrentLine = br.readLine()) != null) {
//                String line = sCurrentLine.split(",")[1];
//
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }



        return 0;
    }

    static public void main(String[] args) throws Exception{
        naiveMethod_multi nm = new naiveMethod_multi();
        long loadRDBbeginTime = System.currentTimeMillis();
        nm.readRDB();
        List<List<List<String>>> rdbValue = myTables;
////        //List<List<String>> rdbValue = buildRDBValue(leaves);
        long loadRDBendTime = System.currentTimeMillis();
        System.out.println("load rdb table data time is " + (loadRDBendTime - loadRDBbeginTime));
//        String[] queryNode = {"asin","price"};
//        List<List<String>> resultList = new ArrayList<>();
//        List<HashMap<String, String>> allTagIDValue = new ArrayList<>();
//        //load all related tag's value and id
//        for (int i = 0; i < queryNode.length; i++) {
//            String tagName = queryNode[i];
//            HashMap<String, String> tagMap = nm.getTagMap(tagName);
//            allTagIDValue.add(tagMap);
//        }
//        List<String> sortedKeys=new ArrayList(allTagIDValue.get(0).keySet());
//
//        //sortedKeys.addAll(new ArrayList(allTagIDValue.get(1).keySet()));
//        Collections.sort(sortedKeys);
//        FileWriter writer = new FileWriter("xjoin/src/allTag.txt");
//        for(String str: sortedKeys) {
//            writer.write(str+","+allTagIDValue.get(0).get(str)+"\r\n");
//        }
//        writer.close();
//
//
//        System.out.println("contains:"+allTagIDValue.get(1).containsKey("/128/5/2"));
//            nm.loadRDBValue(Arrays.asList("asin","price"));
    }
}

