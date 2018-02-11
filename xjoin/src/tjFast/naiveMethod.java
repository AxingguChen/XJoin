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
    public List<List<String>> getValuePair(List<List<String>> pairIDList,List<HashMap<String, String>> allTagIDValue) throws Exception {
        //write down allTagIDValue
//        try {
//            BufferedWriter out = new BufferedWriter(new FileWriter("xjoin/src/allTagIDValueDouble.txt"));
//            out.write("Result\r\n"+allTagIDValue);  //Replace with the string
//            //you are trying to write
//            out.close();
//        }
//        catch (IOException e)
//        {
//            System.out.println("Exception ");
//
//        }
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

    //only value
    public List<List<String>> getValuePair_multi(List<List<String>> pairIDList,List<HashMap<String, String>> allTagIDValue) throws Exception {
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
            }
            resultList.add(valueIDList);
        }
        //[value1, value2...]
        return resultList;
    }

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
            }
        } catch (EOFException eofex) {
            //do nothing
        } catch (Exception e) {
            System.out.println("e is " + e);
        }
        return tagMap;
    }

    public List<List<String>> loadRDBValue(List<String> tagList, String rdb_Table) throws Exception {
        List<List<String>> rdbValue = new ArrayList<>();
        String csvFile = rdb_Table;
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
                list = Arrays.asList(line.split("\\|"));
                if (list.size() > tagLocation.get(tagLocation.size() - 1)) {
                    List<String> valueList = new ArrayList<>();
                    for (int i : tagLocation) {
                        valueList.add(list.get(i));
                    }
                    rdbValue.add(valueList);
                    readRDBcount++;
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
        List<List<String>> joinList = new ArrayList<>();
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

    public int getResult(List<List<String>> solutionPairIDList,List<HashMap<String, String>> allTagIDValue, String rdb_Table, List<String> leaves) throws Exception{
        //rdb table
        //load tag list from leaves of query
//        List<String> leaves = Query.getLeaves();
        List<List<String>> rdbValue = loadRDB(leaves, rdb_Table);
        //XML
        //load
        long loadbeginTime = System.currentTimeMillis();
        List<List<String>> xmlList = getValuePair(solutionPairIDList,allTagIDValue);
        long loadendTime = System.currentTimeMillis();
        System.out.println("Find xml value by id time is " + (loadendTime - loadbeginTime));
        //sort value pair list according to the first value of each pair
        long sortbeginTime = System.currentTimeMillis();
        Collections.sort(xmlList,new MyComparator());
        long sortendTime = System.currentTimeMillis();
        System.out.println("sort xml data time is " + (sortendTime - sortbeginTime));
        System.out.println("xmlList:"+xmlList.size());
        long joinbegintime = System.currentTimeMillis();
        List<List<String>> resultList = joinValue(xmlList,rdbValue);
        long joinendtime = System.currentTimeMillis();
        System.out.println("join xml&rdb data time is " + (joinendtime - joinbegintime));
        return resultList.size();
    }

    //compare by column numbers one by one
    public class MyComparator implements Comparator<List<String>> {
        @Override
        public int compare(List<String> l1, List<String> l2){
            int length = l1.size();
            int result = 0;
            for(int i=0; i<length ; i=i+2){
                int compa = (l1.get(i)).compareTo(l2.get(i));
                if(compa < 0){
                    result = -1;
                    break;
                }
                else if(compa == 0)
                    result = 0;
                else {result = 1;break;}
            }
            return result;
        }
    }

    //compare by column numbers one by one
    public class MyComparator_one implements Comparator<List<String>> {
        @Override
        public int compare(List<String> l1, List<String> l2){
            int length = l1.size();
            int result = 0;
            for(int i=0; i<length ; i++){
                int compa = (l1.get(i)).compareTo(l2.get(i));
                if(compa < 0){
                    result = -1;
                    break;
                }
                else if(compa == 0)
                    result = 0;
                else {result = 1;break;}
            }
            return result;
        }
    }


    public int getResult_doubleLayer(List<List<String>> solutionPairIDList,List<HashMap<String, String>> allTagIDValue, String rdb_Table, List<String> leaves, Boolean doubleAD) throws Exception{
        if(doubleAD) {
            //for double layer AD only
//            List<String> newLeaves = new ArrayList<>();
//            newLeaves.addAll(leaves.subList(1,leaves.size()));
//            newLeaves.add(leaves.get(0));
//            leaves = newLeaves;
        }
        List<List<String>> rdbValue = loadRDB(leaves, rdb_Table);
        //XML
        //load
        long loadbeginTime = System.currentTimeMillis();

        List<List<String>> xmlList;
        if(doubleAD) {
//            //for AD double layer
//            List<HashMap<String, String>> ADDouble = new ArrayList<>();
//            ADDouble.add(allTagIDValue.get(1));
//            ADDouble.add(allTagIDValue.get(2));
//            ADDouble.add(allTagIDValue.get(0));

            xmlList = getValuePair(solutionPairIDList,allTagIDValue);
        }
        else {
            xmlList = getValuePair(solutionPairIDList,allTagIDValue);
        }

        long loadendTime = System.currentTimeMillis();
        System.out.println("Find xml value by id time is " + (loadendTime - loadbeginTime));

        //sort value pair list according to the first value of each pair
        long sortbeginTime = System.currentTimeMillis();
        Collections.sort(xmlList,new MyComparator());
        long sortendTime = System.currentTimeMillis();
        System.out.println("sort xml data time is " + (sortendTime - sortbeginTime));
        System.out.println("xmlList:"+xmlList.size());
        long joinbegintime = System.currentTimeMillis();
        List<List<String>> resultList = joinValue(xmlList,rdbValue);
        long joinendtime = System.currentTimeMillis();
        System.out.println("join xml&rdb data time is " + (joinendtime - joinbegintime));
        return resultList.size();
    }

    public List<List<String>> loadRDB(List<String> leaves, String rdb_Table) throws Exception{
        long loadRDBbeginTime = System.currentTimeMillis();
        List<List<String>> rdbValue = loadRDBValue(leaves, rdb_Table);
        long loadRDBendTime = System.currentTimeMillis();
        System.out.println("load rdb table data time is " + (loadRDBendTime - loadRDBbeginTime));
        //sort
        long sortRDBbeginTime = System.currentTimeMillis();
        Collections.sort(rdbValue,new MyComparator_one());
        long sortRDBendTime = System.currentTimeMillis();
        System.out.println("sort rdb data time is " + (sortRDBendTime - sortRDBbeginTime));
        System.out.println("rdbValue:"+rdbValue.size());
        return rdbValue;
    }

    public List<List<String>> getResult_doubleLayermulti(List<List<String>> solutionPairIDList,List<HashMap<String, String>> allTagIDValue) throws Exception{
        //XML
        //load
        long loadbeginTime = System.currentTimeMillis();
        List<List<String>> xmlList = getValuePair_multi(solutionPairIDList,allTagIDValue);
        long loadendTime = System.currentTimeMillis();
        System.out.println("Find xml value by id time is " + (loadendTime - loadbeginTime));

//        //sort value pair list according to the first value of each pair
//        long sortbeginTime = System.currentTimeMillis();
//        Collections.sort(xmlList,new MyComparator_one());
//        long sortendTime = System.currentTimeMillis();
//        System.out.println("sort xml data time is " + (sortendTime - sortbeginTime));
        System.out.println("xmlList:"+xmlList.size());
        return xmlList;
    }




}

