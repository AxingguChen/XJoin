package tjFast;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Created by zzzhou on 2017-07-19.
 */
public class naiveMethod {
    static int readRDBcount = 0;
    //get xml value,id pair list
    public List<List<String>> getValuePair(List<List<String>> pairIDList) throws Exception {
        String[] queryNode = Query.getQueryNodes();
        List<List<String>> resultList = new ArrayList<>();
        List<HashMap<String, String>> allTagIDValue = new ArrayList<>();
        //load all related tag's value and id
        for (int i = 1; i < queryNode.length; i++) {
            String tagName = queryNode[i];
            HashMap<String, String> tagMap = getTagMap(tagName);
            allTagIDValue.add(tagMap);
        }
        //loop each pair of result ID and find its value
        for (List<String> p : pairIDList) {
            List<String> valueIDList = new ArrayList<>();
            //loop each tag of one pair
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

    public List<List<String>> loadRDBValue(List<String> tagList) throws Exception {
        List<List<String>> rdbValue = new ArrayList<>();
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
                list = Arrays.asList(line.split("\\|"));
                //list = Arrays.asList(line.split(","));
                if (list.size() > tagLocation.get(tagLocation.size() - 1)) {
                    //System.out.println("asin: " + list.get(0) + " price:" + list.get(2) );
                    List<String> valueList = new ArrayList<>();
                    //if(list.size() < 2) System.out.println("less than 2");
                    for (int i : tagLocation) {
                        valueList.add(list.get(i));
                    }
                    rdbValue.add(valueList);
                    readRDBcount++;
                    //result.add(new Match());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("valid read RDB row:" + readRDBcount);

        return rdbValue;
    }

    public List<List<String>> joinValue(List<List<String>> xmlList, List<List<String>> rdbValue){
        int i=0; int j = 0;
        //i -> row number of rdb table, j-> row number of xmlList
        while(i != rdbValue.size() && j != xmlList.size()){
            //get the first column value of rdb table
            String table_value = rdbValue.get(i).get(0);
            // odd-value, even-id. 0 is value
            String xml_value = xmlList.get(j).get(0);


            int compare_result = table_value.compareTo(xml_value);
            if (compare_result == 0) { //equals
                Boolean flag = true;
                for(int tagCursor=1;tagCursor<rdbValue.get(i).size();tagCursor++){
                    //get the first column value of rdb table
                    String table_nextValue = rdbValue.get(i).get(tagCursor);
                    // odd-value, even-id. 0 is value
                    String xml_nextValue = xmlList.get(j).get(tagCursor*2);
                    flag = ((table_nextValue.compareTo(xml_nextValue)==0)&&flag);
                    if(flag==false) break;
                }
                //if match successfully, move xml cursor to next row
                if(flag==false) xmlList.remove(j);
                else j++;
            }
            else if (compare_result < 0){ // table_value < tag_value
                i++;
            }
            else if(compare_result > 0){ // table_value > tag_value
                xmlList.remove(j);
            }
        }
        return xmlList;
    }

    public List<List<String>> getResult(List<List<String>> solutionPairIDList) throws Exception{
        //XML
        //load
        long loadbeginTime = System.currentTimeMillis();
        List<List<String>> xmlList = getValuePair(solutionPairIDList);
        long loadendTime = System.currentTimeMillis();
        System.out.println("load xml data time is " + (loadendTime - loadbeginTime));

        //sort value pair list according to the first value of each pair
        long sortbeginTime = System.currentTimeMillis();
        Collections.sort(xmlList,new Comparator<List<String>>(){
            public int compare(List<String> l1, List<String> l2){
                return l1.get(0).compareTo(l2.get(0));
            }}
        );
        long sortendTime = System.currentTimeMillis();
        System.out.println("sort xml data time is " + (sortendTime - sortbeginTime));
        System.out.println("xmlList:"+xmlList.size());

        //rdb table
        //load
        List<String> leaves = Query.getLeaves();
        loadbeginTime = System.currentTimeMillis();
        List<List<String>> rdbValue = loadRDBValue(leaves);
        loadendTime = System.currentTimeMillis();
        System.out.println("load rdb table data time is " + (loadendTime - loadbeginTime));
        //sort
        sortbeginTime = System.currentTimeMillis();
        Collections.sort(rdbValue,new Comparator<List<String>>(){
            public int compare(List<String> l1, List<String> l2){
                return l1.get(0).compareTo(l2.get(0));
            }}
        );
        sortendTime = System.currentTimeMillis();
        System.out.println("sort rdb data time is " + (sortendTime - sortbeginTime));
        System.out.println("rdbValue:"+rdbValue.size());

        long joinbegintime = System.currentTimeMillis();
        List<List<String>> resultList = joinValue(xmlList,rdbValue);
        long joinendtime = System.currentTimeMillis();
        System.out.println("join xml&rdb data time is " + (joinendtime - joinbegintime));
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
        return resultList;
    }

    static public void main(String[] args) throws Exception{
        naiveMethod nm = new naiveMethod();
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
            nm.getTagMap("asin");
    }
}

