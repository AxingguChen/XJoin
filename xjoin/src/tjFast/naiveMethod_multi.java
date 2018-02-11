package tjFast;

import java.io.*;
import java.util.*;

/**
 * Created by zzzhou on 2017-07-19.
 */
public class naiveMethod_multi {
    static int readRDBcount = 0;
    static long sortTableTime = 0L;
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
                valueIDList.add(value);//do not need id anymore
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
    public void readRDB(String rdb_Table) throws Exception{
        File directory = new File(rdb_Table);
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



    public void joinTables(List<List<String>> xmlTable, List<List<List<String>>> rdbTables, List<String> xmlTagList){
        List<List<String>> finalResult = new ArrayList<>();

        //join each rdbTable with xmlTable
        for(List<List<String>> rdbTable: rdbTables){
            List<String> rdbTags = rdbTable.get(0);
            List<Integer> xmlColumNos = new ArrayList<>();
            List<Integer> rdbColumNos = new ArrayList<>();

            //find tag column in rdb/xml tables
            for(int xmlTag=0; xmlTag<xmlTagList.size(); xmlTag++){
                for(int rdbTag=0; rdbTag<rdbTags.size(); rdbTag++){
                    if(xmlTagList.get(xmlTag).equals(rdbTags.get(rdbTag))){
                        xmlColumNos.add(xmlTag);
                        rdbColumNos.add(rdbTag);
                    }
                }
            }

            //sort tables to join
            long sortBeginTime = System.currentTimeMillis();
            Collections.sort(rdbTable,new MyComparator(rdbColumNos));
            Collections.sort(xmlTable,new MyComparator(xmlColumNos));
            long sortEndTime = System.currentTimeMillis();
            sortTableTime += sortEndTime - sortBeginTime;
            //join tables
            Boolean notEnd = true;
            int xmlRow =0, rdbRow =0;
            while(notEnd){
                //any one of the tables has gone to the end
                if(xmlRow==xmlTable.size() || rdbRow ==rdbTable.size()){
                    break;
                }
                List<String> xmlTableRow = xmlTable.get(xmlRow);
                List<String> rdbTableRow = rdbTable.get(rdbRow);
                //tagValues is to store current row values from tables
                String xmlValue = xmlTableRow.get(xmlColumNos.get(0));
                String rdbValue = rdbTableRow.get(rdbColumNos.get(0));
                int compareResult = xmlValue.compareTo(rdbValue);

                //equals
                if(compareResult == 0){
                    String xmlTableNextValue = xmlTableRow.get(xmlColumNos.get(1));
                    String rdbTableNextValue = rdbTableRow.get(rdbColumNos.get(1));
                    int compare = xmlTableNextValue.compareTo(rdbTableNextValue);
                    if(compare==0) {
                        finalResult.add(xmlTableRow);
                        //@@@@@@@@@@@ may lose correct answers here
                        if(rdbRow+1 != rdbTable.size()) rdbRow++;
                        else xmlRow++;
                    }
                    else if(compare <0) xmlRow++;
                    else rdbRow++;
                }
                else if(compareResult < 0) xmlRow++;
                else rdbRow++;
            }
        }
        System.out.println("final result size:"+finalResult.size());
    }


    //compare by column numbers one by one
    public class MyComparator implements Comparator<List<String>> {
        List<Integer> columnNos;
        public MyComparator(List<Integer> columnNos) {
            this.columnNos = columnNos;
        }
        @Override
        public int compare(List<String> l1, List<String> l2){
            int result = 0;
            for(int i=0; i<columnNos.size(); i++){

                int compa = (l1.get((int)columnNos.get(i)).toString()).compareTo(l2.get((int)columnNos.get(i)).toString());
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


    public int getResult(List<List<String>> solutionPairIDList,List<HashMap<String, String>> allTagIDValue,String rdb_Table, List<String> xmlTagList) throws Exception{
        //rdb table
        //load
        long loadRDBbeginTime = System.currentTimeMillis();
        readRDB(rdb_Table);
        List<List<List<String>>> rdbValue = myTables;
        long loadRDBendTime = System.currentTimeMillis();
        System.out.println("load rdb table data time is: " + (loadRDBendTime - loadRDBbeginTime));

        //XML
        //load
        long loadbeginTime = System.currentTimeMillis();

        //for AD double layer
        List<HashMap<String, String>> ADDouble = new ArrayList<>();
        ADDouble.add(allTagIDValue.get(1));
        ADDouble.add(allTagIDValue.get(2));
        ADDouble.add(allTagIDValue.get(0));
//        List<List<String>> xmlList = getValuePair(solutionPairIDList,ADDouble);
        List<List<String>> xmlList = getValuePair(solutionPairIDList,allTagIDValue);
        long loadendTime = System.currentTimeMillis();
        System.out.println("Find xml value by id time is " + (loadendTime - loadbeginTime));

//        System.out.println("xml List. get(0):"+xmlList.get(0));
//        System.out.println("xml List. get(1):"+xmlList.get(1));
//        System.out.println("xml List. get(2):"+xmlList.get(2));


        //join table time
        long joinbegintime = System.currentTimeMillis();
        joinTables(xmlList,rdbValue, xmlTagList );
        long joinendtime = System.currentTimeMillis();
        System.out.println("sort table time is: "+ sortTableTime);
        System.out.println("only join xml&rdb time is: " + (joinendtime - joinbegintime-sortTableTime));
        System.out.println("total join xml&rdb time is: " + (joinendtime - joinbegintime));
        return 0;
    }

    public int getResult_doubleLayer(List<List<String>> solutionPairIDList,List<HashMap<String, String>> allTagIDValue,String rdb_Table, List<String> xmlTagList) throws Exception{
        //rdb table
        //for double layer AD only
        xmlTagList.add(xmlTagList.get(0));
        xmlTagList.remove(0);

        long loadRDBbeginTime = System.currentTimeMillis();
        readRDB(rdb_Table);
        List<List<List<String>>> rdbValue = myTables;
        long loadRDBendTime = System.currentTimeMillis();
        System.out.println("load rdb table data time is: " + (loadRDBendTime - loadRDBbeginTime));

        //XML
        //load
        long loadbeginTime = System.currentTimeMillis();

        //for AD double layer
        List<HashMap<String, String>> ADDouble = new ArrayList<>();
        ADDouble.add(allTagIDValue.get(1));
        ADDouble.add(allTagIDValue.get(2));
        ADDouble.add(allTagIDValue.get(0));
        List<List<String>> xmlList = getValuePair(solutionPairIDList,ADDouble);

        long loadendTime = System.currentTimeMillis();
        System.out.println("Find xml value by id time is " + (loadendTime - loadbeginTime));

        //join table time
        long joinbegintime = System.currentTimeMillis();
        joinTables(xmlList,rdbValue, xmlTagList );
        long joinendtime = System.currentTimeMillis();
        System.out.println("sort table time is: "+ sortTableTime);
        System.out.println("only join xml&rdb time is: " + (joinendtime - joinbegintime-sortTableTime));
        System.out.println("total join xml&rdb time is: " + (joinendtime - joinbegintime));
        return 0;
    }
}

