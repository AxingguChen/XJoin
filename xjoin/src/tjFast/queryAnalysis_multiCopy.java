package tjFast;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import produce.generateValueIdPair;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;

public class queryAnalysis_multiCopy extends DefaultHandler{
    Hashtable twigTagNames;

    static String filename;

    String ROOT;

    Stack TagStack;
    static long totalSortTableTime = 0L;

    static String basicDocuemnt;
    static List<List<Vector>> myTables = new ArrayList<>();
    static List<Vector> tjFastTable = new ArrayList<>();
    static Set<String> xmlRelationTagSet = new HashSet<>();

    static  HashMap<String, List<Vector>> tagMaps = new HashMap<>();

    public void getSolution(){
        try{
            long beginTime = System.currentTimeMillis();
//            List<String> tagList = Arrays.asList("a","b","c","d","e");
            List<String> tagList = Arrays.asList("Invoice","OrderId","Orderline","asin","price");
            for(String s:tagList){
                if(! xmlRelationTagSet.contains(s)){
                    Vector v = new Vector();
                    List<Vector> l = new ArrayList<>();
                    v.add(s);
                    l.add(v);
                    myTables.add(l);
                }
            }
//            System.out.println("getSolution:"+ myTables);
            generateValueIdPair generate = new generateValueIdPair();

            //offline prepare all tag tables(value, id)
            long generateTagMapStartTime = System.currentTimeMillis();
            tagMaps = generate.generateTagVId(tagList,basicDocuemnt);
            long generateTagMapEndTime = System.currentTimeMillis();
            long generateTagMapTime = generateTagMapEndTime - generateTagMapStartTime;

            long startTime1 = System.currentTimeMillis();
            myTables = getPCTables(tagMaps,myTables);
            long endTime1 = System.currentTimeMillis();
            System.out.println("getPC tables:"+(endTime1-startTime1));
            //divide p-c relation in xml to RDBs.
//            myTables = generate.generatePCVId(myTables);
            long endTime = System.currentTimeMillis();
            System.out.println("generate tagMap time:"+(generateTagMapTime));
            System.out.println("get xml table total time:"+(endTime-beginTime));
            System.out.println("generate p-c xml tables time:"+(endTime-beginTime-generateTagMapTime));

            beginTime = System.currentTimeMillis();
            //read RDB files, add rdb_tables to myTables.
            System.out.println("read RDB");
            readRDB();
            endTime = System.currentTimeMillis();
            System.out.println("read rdb time:"+(endTime-beginTime));

            beginTime = System.currentTimeMillis();
            System.out.println("merge tables");
            List<Vector> myResult = mergeTable(tagList);
            Collections.sort(myResult,new MyComparator(Arrays.asList(0,2,4,6,8)));
            tjFastTable =  myResult;
            System.out.println("tjFastTable size:"+tjFastTable.size());
            endTime = System.currentTimeMillis();
            System.out.println("sort table time:"+totalSortTableTime);
            System.out.println("merge Table time:"+(endTime-beginTime-totalSortTableTime));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.exit(1); // also you can use System.exit(0);
        }

    }

    //Return 0 -> equals. Return 1 -> id1 > id2. Return -1, id1 < id2
    public int compareId(int[] id1, int[] id2){
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

    public List<List<Vector>> getPCTables(HashMap<String, List<Vector>> tagMaps, List<List<Vector>> myTables){
        List<List<Vector>> pcTables = new ArrayList<>();
        for(int tableCursor=0; tableCursor<myTables.size(); tableCursor++){
            List<Vector> pcTable = new ArrayList<>();
            Vector pc = myTables.get(tableCursor).get(0);
            pcTable.add(pc);//first row indicates tag names.
            if(pc.size() == 2){
                //parent tag
                String tag_p = pc.get(0).toString();
                //children tag
                String tag_c = pc.get(1).toString();
                List<Vector> table_p = tagMaps.get(tag_p);
                List<Vector> table_c = tagMaps.get(tag_c);
                int p=0, c=0;
                int p_size = table_p.size();
                int c_size = table_c.size();
                while(p != p_size && c != c_size){
                    // 0: value, 1: id
                    int[] p_id = (int[])table_p.get(p).get(1);
                    int[] c_id = (int[])table_c.get(c).get(1);
                    int compResult = compareId(p_id, Arrays.copyOf(c_id, c_id.length-1));
                    //if equals
                    if(compResult == 0){
                        Vector v = new Vector();
                        v.addAll(Arrays.asList(table_p.get(p).get(0).toString(), p_id, table_c.get(c).get(0).toString(),c_id));
                        pcTable.add(v);
                        p++;
                        c++;
                    }
                    else if(compResult > 0) c++;
                    else p++;
                }
            }
            //only one tag, no pc relation
            else{
                pcTable.addAll(tagMaps.get(pc.get(0)));
            }
            pcTables.add(pcTable);
        }
        return pcTables;
    }

    public List<Vector> mergeTable(List<String> mergeOrder){
        List<Vector> myResult = new ArrayList<>();
        //add tag one by one to myResult
        for(int order=0; order<mergeOrder.size(); order++){
            //the tag that is going to be added to Result
            String addTag = mergeOrder.get(order);
            System.out.println("add tag:"+addTag);
            //column numbers of addTag in tables
            List<Integer> tableColumns = new ArrayList<>();
            //tables that contains addTag
            List<List<Vector>> tablesToMerge = new ArrayList<>();
            //first row(table tags name) of tablesToMerge
            List<Vector> tableTags = new ArrayList<>();
            //####add tables that contains current add-tag to a list(tablesToMerge)#####
            for(List<Vector> table:myTables){
                Vector tagVector = table.get(0);
                if(tagVector.contains(addTag)){
                    int table_column = getColumn(tagVector,addTag);
                    //Vector in table is [v,id,v,id,...] except the first row. "*2" -> value
                    tableColumns.add(table_column*2);
                    //remove first row of table since the first row is the names of tags, and table needs to be sorted later.
                    List<Vector> table_removeFirstRow = table.subList(1,table.size());
                    //sort table by addTag
                    long time1 = System.currentTimeMillis();
                    Collections.sort(table_removeFirstRow,new MyComparator(Arrays.asList(table_column*2)));
                    long time2 = System.currentTimeMillis();
                    totalSortTableTime += time2 -time1;
                    //add table that contains addTag to list
                    tablesToMerge.add(table_removeFirstRow);
                    //add first row of table(table tag names) to a list, since we need to check tables' other tags later.
                    tableTags.add(tagVector);
                }
            }
            //Here we have all tables that contains addTag
            //####Join tables to get addTag HashMap####
            HashMap<String, List<int[]>> tagHashMap = getAddTagHashMap(tablesToMerge, tableColumns);

            //if the Result is empty(first tag to be added)
            if(myResult.isEmpty()){
                for (Map.Entry<String, List<int[]>> entry : tagHashMap.entrySet()) {
                    myResult.add(new Vector(Arrays.asList(entry.getKey(),entry.getValue())));
                }
            }
            //else Result is not empty, find if any other added tags in Result occurs in tablesToMerge
            else{
                List<String> addedTags = mergeOrder.subList(0, order);
                for(int addedTagCursor=0; addedTagCursor<addedTags.size(); addedTagCursor++){
                    List<List<Vector>> tablesToMergeOnAddedTag = new ArrayList<>();
                    List<List<Integer>> addedTagColumn = new ArrayList<>();
                    //find table(has addedTag), and its table column
                    String addedTag = addedTags.get(addedTagCursor);
                    for(int tableCursor=0; tableCursor<tableTags.size(); tableCursor++){
                        int column = getColumn(tableTags.get(tableCursor), addedTag);
                        //if this table contains this tag
                        if(column != -1){
                            //columnNos contains columns of addedTag and addTag
                            List<Integer> columnNos = Arrays.asList(column*2,tableColumns.get(tableCursor));
                            addedTagColumn.add(columnNos);
                            List<Vector> currentTable = tablesToMerge.get(tableCursor);
                            //sort table by first addedTag, then addTag
                            long time1 = System.currentTimeMillis();
                            Collections.sort(currentTable,new MyComparator(columnNos));
                            long time2 = System.currentTimeMillis();
                            totalSortTableTime += time2-time1;
                            //add table to mergeList
                            tablesToMergeOnAddedTag.add(currentTable);
                        }
                    }
                    //exists table(s) to join with Result table
                    if(!addedTagColumn.isEmpty()){
                        //sort myResult on addedTag
                        long time1 = System.currentTimeMillis();
                        Collections.sort(myResult,new MyComparator(Arrays.asList(addedTagCursor*2)));
                        long time2 = System.currentTimeMillis();
                        totalSortTableTime += time2 - time1;
                        myResult = joinWithResult(myResult, addedTagCursor*2, tablesToMergeOnAddedTag, addedTagColumn, tagHashMap, order*2);

                        }
                }

                //if tablesToMerge does not have any common tag with Result list, add each key in tagHashMap to each row of the table
                if(myResult.get(0).size() == order*2){//(1*2 = 2)
                    List<Vector> simulNewResult = new ArrayList<>();
                    for (Map.Entry<String, List<int[]>> entry : tagHashMap.entrySet()) {
                        String value = entry.getKey();
                        List<int[]> idList = entry.getValue();
                        for(Vector v:myResult){
                            Vector v_clone = (Vector) v.clone();
                            v_clone.addAll(Arrays.asList(value,idList));
                            simulNewResult.add(v_clone);
                        }
                    }
                    myResult = simulNewResult;
                }
            }

        }
        return myResult;
    }

    public List<Vector> joinWithResult(List<Vector> myResult, int resultColumn, List<List<Vector>> tablesToMergeOnAddedTag, List<List<Integer>> addedTagColumn, HashMap<String, List<int[]>> tagHashMap, int orgRowSize){
        Boolean notEnd = true;
        List<Vector> myNewResult = new ArrayList<>();
        int[] rowCursor = new int[tablesToMergeOnAddedTag.size()+1];
        int myResultSize = myResult.size();
//        System.out.println("myResultSize:"+myResultSize);
        while(notEnd){
//            System.out.println("myResultRowCursor:"+rowCursor[0]);
            //any one of the tables has gone to the end
            if(rowCursor[0]==myResultSize || isEnd(tablesToMergeOnAddedTag,rowCursor)){
                break;
            }
            //tagValues is to store current row values from tables
            List<String> tagValues = new ArrayList<>();
            Vector resultRow = myResult.get(rowCursor[0]);
            tagValues.add(resultRow.get(resultColumn).toString());
            for(int tableCursor = 0; tableCursor < tablesToMergeOnAddedTag.size(); tableCursor++){
                tagValues.add(tablesToMergeOnAddedTag.get(tableCursor).get(rowCursor[tableCursor+1]).get((int) addedTagColumn.get(tableCursor).get(0)).toString());
            }
            int compareResult = makeComparision(tagValues);

            if(compareResult == -1){
                String commonValue = tagValues.get(0);
                int[] orgRowCursor =  rowCursor;
                Vector retur = moveCursorUntilNextNew(myResult, rowCursor[0], resultColumn, commonValue);
                rowCursor[0] = (int)retur.get(0);
                List<Vector> partResultRows = (List<Vector>)retur.get(1);
                List<List<Vector>> partMergeTableRows = new ArrayList<>();
                for(int tableCursor = 0; tableCursor < tablesToMergeOnAddedTag.size(); tableCursor++){
                    Vector returr = (Vector) moveCursorUntilNextNew(tablesToMergeOnAddedTag.get(tableCursor),rowCursor[tableCursor+1],(int) addedTagColumn.get(tableCursor).get(0),commonValue);
                    rowCursor[tableCursor+1] = (int) returr.get(0);
                    partMergeTableRows.add((List<Vector>)returr.get(1));
                }

                //compare addTag column value
                //@@@@@ only one table to join
                Vector row = (Vector) resultRow;//@@@@if needs to be cloned
                List<Vector> mergeTableRows = partMergeTableRows.get(0);
                //if myResult does not have addTag to join. @@@@ add addTag to myResult, id_list can get from tagMap
                //@@@@@@@@@@@@also need to compare id to see if the id can be added to the list
                if(row.size() == orgRowSize){
                    for(int i=0; i<partResultRows.size(); i++){
                        for(int j=0; j<mergeTableRows.size(); j++){
                            Vector thisRow = (Vector)partResultRows.get(i).clone();
                            String value = mergeTableRows.get(j).get((int) addedTagColumn.get(0).get(1)).toString();
                            if(tagHashMap.containsKey(value)){
                                thisRow.addAll(Arrays.asList(value, tagHashMap.get(value)));
                                myNewResult.add(thisRow);
                            }
                        }
                    }

                }
                //else myResult has addTag to join
                else{
                    //sort part table on addTag
                    long time1 = System.currentTimeMillis();
                    Collections.sort(partResultRows,new MyComparator(Arrays.asList(orgRowSize)));
                    Collections.sort(mergeTableRows,new MyComparator(Arrays.asList(addedTagColumn.get(0).get(1))));
                    long time2 = System.currentTimeMillis();
                    totalSortTableTime += time2 - time1;
                    int i=0, j=0;
                    while(i != partResultRows.size() && j != mergeTableRows.size()){
                        Vector partResultRow = partResultRows.get(i);
                        Vector partJoinTableRow = mergeTableRows.get(j);
                        String resultAddTagValue = partResultRow.get(orgRowSize).toString();
                        String joinAddTagValue = partJoinTableRow.get(addedTagColumn.get(0).get(1)).toString();
                        int compare_result = resultAddTagValue.compareTo(joinAddTagValue);
                        if(compare_result == 0){
                            myNewResult.add(partResultRow);
                            if((j+1)!=mergeTableRows.size()){
                                j++;
                            }
                            else i++;
                        }
                        else if(compare_result > 0){
                            j++;
                        }
                        else{
                            if (i != partResultRows.size() && i > 0 && resultAddTagValue.equals(partResultRows.get(i-1).get(orgRowSize).toString())) {
                                myNewResult.add(partResultRow);
                            }
                            i++;
                        }
                    }
                }


            }
            //else move the table which value is minimal
            else rowCursor[compareResult] = rowCursor[compareResult]+1;
        }
//        System.out.println("myNewResultSize:"+myNewResult.size());
        return myNewResult;
    }






    public int compareIds(List<int[]> ids){
        int compareResult = 0;
        Boolean isEqual = true;
        Boolean sizeIsEqual = true;
        int size_sFlag = 0;
        int allSize = ids.size();
        int smallSize = ids.get(0).length;
        //sizes are also need to be compared
        int[] idSizes = new int[allSize];
        //compare if size are the same length
        for(int i=1; i< allSize; i++){
            int curSize = ids.get(i).length;
            if(curSize !=smallSize){
                sizeIsEqual = false;
                if(curSize<smallSize){
                    smallSize = curSize;
                    size_sFlag = i;
                }
            }
        }
        for(int i=0; i< smallSize;i++){
            int baseV = ids.get(0)[i];
            for(int j=1; j<allSize; j++){
                int compV = ids.get(j)[i];
                if(compV != baseV){
                    isEqual = false;
                    if(compV < baseV){
                        compareResult = j;
                        baseV = compV;
                    }
                }
            }
        }
        //if the size is same
        if(!sizeIsEqual && isEqual){
            return size_sFlag;

        }else{
            if(isEqual) return -1;
            else return compareResult;
        }
    }


    public List<int[]> getCommonId(List<List<int[]>> idLists){
        int idLists_size = idLists.size();
        List<int[]> commonIds = new ArrayList<>();
        if(idLists_size == 1){
            commonIds = idLists.get(0);
        }
        else{
            int[] rowCursor = new int[idLists_size];
            Boolean notEnd = true;
            while(notEnd){
                if(isIdEnd(idLists, rowCursor)) break;
                //add current row id to list
                List<int[]> currentRowIds = new ArrayList<>();
                for(int i=0; i<idLists_size; i++){
                    currentRowIds.add(idLists.get(i).get(rowCursor[i]));
                }
                int compa = compareIds(currentRowIds);
                //compa-> -1 means the ids are equal. other values means to move corresponding cursor.
                if(compa==-1){
                    commonIds.add(currentRowIds.get(0));
                    //since id will not duplicate in the same list, so move each idList to the next row
                    for(int i=0; i<idLists_size; i++){
                        rowCursor[i] += 1;
                    }
                }
                else rowCursor[compa] += 1;
            }
        }
        return commonIds;
    }

//    //list1/list2 has no same id in the list itself
//    public List<int[]> getCommonIds(List<int[]> list1, List<int[]> list2){
//        List<int[]> commonIds = new ArrayList<>();
//        //the id lists should already be sorted since they are generating by an order
//        int i = 0, j=0;
//        int list1_size = list1.size();
//        int list2_size = list2.size();
//        while(i != list1_size && j != list2_size){
//            //compare ids
//            int[] id1 = list1.get(i);
//            int[] id2 = list2.get(j);
//            int compareResult = compareId(id1, id2);
//            //id1 = id2, add this id to commonId, then compare their next id to move the cursor
//            if(compareResult == 0){
//                commonIds.add(id1);
//                if( i+1 != list1_size) i++;
//                else j++;
//            }
//            //if id1 > id2
//            else if(compareResult > 0){
//                j++;
//            }
//            //else id1 < id2
//            else {
//                i++;
//            }
//        }
//
//        return commonIds;
//    }

    public HashMap<String, List<int[]>> getAddTagHashMap(List<List<Vector>> tablesToMerge, List<Integer> tableColumns){
        Boolean notEnd = true;
        HashMap<String, List<int[]>> tagValueIdMap = new HashMap();
        int tableCount = tableColumns.size();
        int[] rowCursor = new int[tableCount];
        //only one table contains addTag
        if(tableCount == 1){
            List<Vector> table = tablesToMerge.get(0);
            for(int rowNo=0; rowNo<table.size();) {
                int columnNo = tableColumns.get(0);
                String value = table.get(rowNo).get(columnNo).toString();
                Vector v = moveCursorUntilNewValue(table,rowNo,columnNo,value);
                List<int[]> id_list = (List<int[]>)v.get(1);
                rowNo = (int)v.get(0);
                tagValueIdMap.put(value, id_list);
            }
        }
        //many table contains addTag, join them on addTag. But maybe not all of the tables have ids.
        else{
            while(notEnd){
                //any one of the tables has gone to the end
                if(isEnd(tablesToMerge,rowCursor)){
                    break;
                }
                List<String> tagValues = new ArrayList<>();
                for(int tableCursor = 0; tableCursor < tablesToMerge.size(); tableCursor++){
                    tagValues.add(tablesToMerge.get(tableCursor).get(rowCursor[tableCursor]).get(tableColumns.get(tableCursor)).toString());
                }
                int compareResult = makeComparision(tagValues);
                //if values equals, compare ids
                if(compareResult == -1){
                    List<List<int[]>> id_Lists = new ArrayList<>();
                    String commonValue = tagValues.get(0);
                    for(int tableCursor=0; tableCursor<tablesToMerge.size(); tableCursor++){
                        Vector v = moveCursorUntilNewValue(tablesToMerge.get(tableCursor),rowCursor[tableCursor],tableColumns.get(tableCursor),commonValue);
                        rowCursor[tableCursor] = (int)v.get(0);
                        List<int[]> id_list = (List<int[]>)v.get(1);
                        if(!id_list.isEmpty()) id_Lists.add(id_list);//only add null id list to id_Lists
                    }
                    //find id that occurs in all list
                    List<int[]> sameId_List = getCommonId(id_Lists);
                    tagValueIdMap.put(commonValue,sameId_List);
                }
                //if not equals, add one to the row cursor number of the smallest table
                else{
                    rowCursor[compareResult] = rowCursor[compareResult]+1;
                }
            }
        }
        return tagValueIdMap;
    }


    //move rowNo until the next value is not the same, also add the same values' id to a list and return it.
    public Vector moveCursorUntilNewValue(List<Vector> table, int rowNo, int columnNo, String commonValue){
        List<int[]> id_list = new ArrayList<>();
        int row=rowNo;
        Vector v = new Vector();
        for(; row<table.size();){
            String compareValue =  table.get(row).get(columnNo).toString();
            if(commonValue.equals(compareValue)){
                int[] id = (int[])table.get(row).get(columnNo+1);
                if(id != null) id_list.add(id);//only add null value id to list
                row++;
            }
            else break;
        }
        v.add(row);
        v.add(id_list);
        return v;
    }

    //move rowNo until the next value is not the same, also add the same values' id to a list and return it.
    public Vector moveCursorUntilNextNew(List<Vector> table, int rowNo, int columnNo, String commonValue){
        Vector retur = new Vector();
        List<Vector> v = new Vector();
        int row=rowNo;
        for(; row<table.size();){
            Vector thisRow = table.get(row);
            String compareValue =  thisRow.get(columnNo).toString();
            if(commonValue.equals(compareValue)){
                row++;
                v.add(thisRow);
            }
            else break;
        }
        retur.add(row);
        retur.add(v);
        return retur;
    }

    //return table cursor, which need to go to next row.
    //if the values are all the same, return -1
    public int makeComparision(List<String> values){
        String smallValue = values.get(0);
        int smallValueCursor = 0;
        Boolean equals = true;
        for(int i=1;i<values.size();i++){
            String currentValue = values.get(i);
            int compare = smallValue.compareTo(currentValue);
            if(compare > 0){
                smallValue = values.get(i);
                smallValueCursor = i;
            }
            if(compare != 0){
                equals = false;
            }
        }
        if(equals) return -1;

        return smallValueCursor;
    }

    //return true means one table has gone to the end.
    public boolean isEnd(List<List<Vector>> tablesLists, int[] rowCursor){
        for(int i=0;i<tablesLists.size();i++){
            // the last element of this table
            if(tablesLists.get(i).size() == rowCursor[i]){
                return true;
            }
        }
        return false;
    }

    //return true means one table has gone to the end.
    public boolean isIdEnd(List<List<int[]>> idLists, int[] rowCursor){
        for(int i=0;i<idLists.size();i++){
            // the last element of this table
            if(idLists.get(i).size() == rowCursor[i]){
                return true;
            }
        }
        return false;
    }


    //compare by column numbers one by one
    public class MyComparator implements Comparator<Vector> {
        List<Integer> columnNos;
        public MyComparator(List<Integer> columnNos) {
            this.columnNos = columnNos;
        }
        @Override
        public int compare(Vector l1, Vector l2){
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

    //get the column number of current table, may be replaced by e.g.[0,0,1,0,1]
    public int getColumn(Vector v, String tag){
        for(int i=0;i<v.size();i++){
            if(v.get(i).toString().equals(tag)){
                return i;
            }
        }
        return -1;
    }

    //read RDB value and merge list to myTables.
    public void readRDB() throws Exception{
        File directory = new File("xjoin/src/multi_rdbs/testTables");
        for(File f: directory.listFiles()){
            String line = "";
            Boolean firstLine = true;
            List<Vector> rdb = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                while ((line = br.readLine()) != null) {
                    Vector vec = new Vector();
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

    static public void xjoinOne () throws Exception {
        //filename = args[0];

        //basicDocuemnt = args[1];
//        basicDocuemnt = "xjoin/src/test.xml";
        basicDocuemnt = "xjoin/src/multi_rdbs/Invoice.xml";

        if (filename == null) {
            usage();
        }

        if (basicDocuemnt == null) {
            usage();
        }

        SAXParserFactory spf = SAXParserFactory.newInstance();

        spf.setNamespaceAware(true);

        // Create a JAXP SAXParser
        SAXParser saxParser = spf.newSAXParser();

        // Get the encapsulated SAX XMLReader
        XMLReader xmlReader = saxParser.getXMLReader();

        // Set the ContentHandler of the XMLReader
        xmlReader.setContentHandler(new queryAnalysis_multiCopy());

        // Set an ErrorHandler before parsing
        xmlReader.setErrorHandler(new MyErrorHandler(System.err));

        // Tell the XMLReader to parse the XML document
        xmlReader.parse(convertToFileURL(filename));
    }

    static Boolean isFirst = true;
    static public void main(String[] args) throws Exception {

        filename = "xjoin/src/multi_rdbs/queries/query1.xml";
        xjoinOne();



        filename = null;

        Stack TagStack;
        totalSortTableTime = 0L;

        myTables = new ArrayList<>();
        tjFastTable = new ArrayList<>();
        xmlRelationTagSet = new HashSet<>();
        System.out.println("second join");

        //System.out.println("Total tjFast time is " + (tjFastEndTime-tjFastbeginTime));
        //System.out.println("Total tjFast by add time is " + tjFastbyAddTime);
        //offline prepare all tag tables(value, id)
        filename = "xjoin/src/multi_rdbs/queries/query2.xml";
        isFirst = false;
        xjoinOne();

    }
    // Error handler to report errors and warnings
    private static class MyErrorHandler implements ErrorHandler {
        /**
         * Error handler output goes here
         */
        private PrintStream out;

        MyErrorHandler(PrintStream out) {
            this.out = out;
        }

        /**
         * Returns a string describing parse exception details
         */
        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            String info = "URI=" + systemId +
                    " Line=" + spe.getLineNumber() +
                    ": " + spe.getMessage();
            return info;
        }

        // The following methods are standard SAX ErrorHandler methods.
        // See SAX documentation for more info.

        public void warning(SAXParseException spe) throws SAXException {
            out.println("Warning: " + getParseExceptionInfo(spe));
        }

        public void error(SAXParseException spe) throws SAXException {
            String message = "Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }
    /**
     * Convert from a filename to a file URL.
     */
    private static String convertToFileURL(String filename) {
        // On JDK 1.2 and later, simplify this to:
        // "path = file.toURL().toString()".
        String path = new File(filename).getAbsolutePath();
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "file:" + path;
    }

    private static void usage() {
        System.err.println("Usage: QueryAnalysis <file.xml>");
        System.exit(1);
    }
    // Parser calls this once at the beginning of a document
    public void startDocument() throws SAXException {

        twigTagNames = new Hashtable();

        TagStack = new Stack();

    }//end startDocument


    public void characters(char[] ch, int start, int length) {
        String value = new String(ch, start, length);

        if (value.equalsIgnoreCase("1")) { //is PC relationship
            //cut PC to R(P,C)
            String child = (String) TagStack.peek();
            String parent = (String) TagStack.elementAt(TagStack.size() - 2);
            List<Vector> pc = new ArrayList<>();
            Vector v = new Vector();
            v.add(parent);v.add(child);
            pc.add(v);
            myTables.add(pc);
            xmlRelationTagSet.addAll(Arrays.asList(parent, child));
            Vector temp = (Vector) twigTagNames.get(parent);
            for (int i = 0; i < temp.size(); i++)
                if ((((QueryDataType) temp.elementAt(i)).getTagName().equalsIgnoreCase(child))) {
                    ((QueryDataType) temp.elementAt(i)).setPCEdge();
                    break;
                }

        }//end if


    }// end characters

    // Parser calls this for each element in a document
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts)
            throws SAXException {
        String currentTag = localName;

        if (TagStack.size() > 0) {
            String parent = (String) TagStack.peek();
            if (twigTagNames.containsKey(parent)) {
                Vector temp = (Vector) twigTagNames.get(parent);
                QueryDataType data = new QueryDataType(currentTag, false);
                temp.add(data);
            }//end if
            else {
                QueryDataType data = new QueryDataType(currentTag, false);
                Vector temp = new Vector();
                temp.add(data);
                twigTagNames.put(parent, temp);
            }//end else

        }//end if
        else
            ROOT = currentTag;

        TagStack.push(currentTag);

    }//end startElement


    public void endElement(String namespaceURI, String localName,
                           String qName)
            throws SAXException {

        TagStack.pop();


    }//end endElement

    // Parser calls this once after parsing a document
    static List<Vector> result = new ArrayList<>();
    public void endDocument() throws SAXException {
        if(isFirst){
            getSolution();
            System.out.println("begin analysis query !");

            Query.setTwigTagNames(twigTagNames);


            Query.setRoot(ROOT);

            utilities.DebugPrintln("Query root is " + Query.getRoot());

            System.out.println("begin analysis document !");

            try {
                DTDTable DTDInfor = loadDataSet.produceDTDInformation(basicDocuemnt);

                long totalbeginTime = System.currentTimeMillis();
                long loadendTime = 0L;
                long loadQueryEndTime = 0L;
                long joinbeginTime = 0L;
                long joinendTime = 0L;
                long totalLoadTime = 0L;
                long totalJoinTime = 0L;

                Query.preComputing(DTDInfor);

                loadDataSet d = new loadDataSet();
                System.out.println("begin load data !");


                List<Hashtable[]> ALLData = new ArrayList<Hashtable[]>();
                labelMatching lm = new labelMatching();
                List<String> tagList = new ArrayList<>();
                for(int i=0;i< Query.getLeaves().size();i++){
                    tagList.add((String) Query.getLeaves().elementAt(i)); // get query leaves
                }

                int solutionCount = 0;

                //produce tjFast leaves table
                List<Vector> tjFastData = new ArrayList<>();
                for(Vector v:tjFastTable){
                    Vector v1 = new Vector();
                    v1.addAll(Arrays.asList(v.get(3*2), v.get(3*2+1), v.get(4*2), v.get(4*2+1),v.get(1*2), v.get(1*2+1)));
                    tjFastData.add(v1);
                }


                for(int i=0;i<tjFastData.size();i++) {
                    long loadbeginTime = System.currentTimeMillis();
                    Hashtable[] alldata = d.loadAllLeafData(tjFastData.get(i), DTDInfor,tagList);

                    //System.out.println("Query leaves:" + Query.getLeaves());

    //                System.out.println("i:"+i);
                    loadendTime = System.currentTimeMillis();
                    //System.out.println("load data time is " + (loadendTime - loadbeginTime));
                    totalLoadTime += loadendTime - loadbeginTime;

                    //join
                    //System.out.println("begin join !");

                    joinbeginTime = System.currentTimeMillis();

                    TwigSet join = new TwigSet(DTDInfor, alldata[1], alldata[0]);

                    solutionCount += join.beginJoin();
                    //keep this row in result
                    if(solutionCount>0){
                        result.add(tjFastTable.get(i));
                    }
    //                System.out.println("solutionCount:"+solutionCount);
                    joinendTime = System.currentTimeMillis();
                    //System.out.println("join data time is " + (joinendTime - joinbeginTime));
                    totalJoinTime += joinendTime - joinbeginTime;
                    //tjFastbyAddTime = tjFastbyAddTime + joinendTime -loadbeginTime;


                }
                long tjFastEndTime = System.currentTimeMillis();
                long totalendTime = System.currentTimeMillis();
                System.out.println("after first join, solutionCount:"+solutionCount+" , "+ result.size());

                System.out.println("Total tjFast load data time is " + totalLoadTime);

                System.out.println("Total tjFast join data time is " + totalJoinTime);

                System.out.println("Total running time is " + (totalendTime - totalbeginTime));



            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            getSolution2();
            System.out.println("begin analysis query 2!");

            Query.setTwigTagNames(twigTagNames);


            Query.setRoot(ROOT);

            utilities.DebugPrintln("Query root is " + Query.getRoot());

            System.out.println("begin analysis document !");

            try {
                DTDTable DTDInfor = loadDataSet.produceDTDInformation(basicDocuemnt);

                long totalbeginTime = System.currentTimeMillis();
                long loadendTime = 0L;
                long loadQueryEndTime = 0L;
                long joinbeginTime = 0L;
                long joinendTime = 0L;
                long totalLoadTime = 0L;
                long totalJoinTime = 0L;

                Query.preComputing(DTDInfor);

                loadDataSet d = new loadDataSet();
                System.out.println("begin load data !");


                List<Hashtable[]> ALLData = new ArrayList<Hashtable[]>();
                labelMatching lm = new labelMatching();
                List<String> tagList = new ArrayList<>();
                for(int i=0;i< Query.getLeaves().size();i++){
                    tagList.add((String) Query.getLeaves().elementAt(i)); // get query leaves
                }

                int solutionCount = 0;

                //produce tjFast leaves table
                List<Vector> tjFastData = new ArrayList<>();
                for(Vector v:tjFastTable){
                    Vector v1 = new Vector();
                    v1.addAll(Arrays.asList(v.get(3*2), v.get(3*2+1), v.get(4*2), v.get(4*2+1),v.get(1*2), v.get(1*2+1)));
                    tjFastData.add(v1);
                }


                for(int i=0;i<tjFastData.size();i++) {
                    long loadbeginTime = System.currentTimeMillis();
                    Hashtable[] alldata = d.loadAllLeafData(tjFastData.get(i), DTDInfor,tagList);
                    loadendTime = System.currentTimeMillis();
                    //System.out.println("load data time is " + (loadendTime - loadbeginTime));
                    totalLoadTime += loadendTime - loadbeginTime;

                    //join
                    //System.out.println("begin join !");

                    joinbeginTime = System.currentTimeMillis();

                    TwigSet join = new TwigSet(DTDInfor, alldata[1], alldata[0]);

                    solutionCount += join.beginJoin();

                    joinendTime = System.currentTimeMillis();
                    //System.out.println("join data time is " + (joinendTime - joinbeginTime));
                    totalJoinTime += joinendTime - joinbeginTime;
                    //tjFastbyAddTime = tjFastbyAddTime + joinendTime -loadbeginTime;


                }
                long tjFastEndTime = System.currentTimeMillis();
                long totalendTime = System.currentTimeMillis();
                System.out.println("after second join, solutionCount:"+solutionCount+" , "+ result.size());

                System.out.println("Total tjFast load data time is " + totalLoadTime);

                System.out.println("Total tjFast join data time is " + totalJoinTime);

                System.out.println("Total running time is " + (totalendTime - totalbeginTime));



            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    public void getSolution2(){
        try{
            long beginTime = System.currentTimeMillis();
//            List<String> tagList = Arrays.asList("a","b","c","d","e");
            List<String> tagList = Arrays.asList("Orderline","price","productId");
            for(String s:tagList){
                if(! xmlRelationTagSet.contains(s)){
                    Vector v = new Vector();
                    List<Vector> l = new ArrayList<>();
                    v.add(s);
                    l.add(v);
                    myTables.add(l);
                }
            }
//            System.out.println("getSolution:"+ myTables);
            generateValueIdPair generate = new generateValueIdPair();



            long startTime1 = System.currentTimeMillis();
            myTables = getPCTables(tagMaps,myTables);
            long endTime1 = System.currentTimeMillis();
            System.out.println("getPC tables:"+(endTime1-startTime1));
            //divide p-c relation in xml to RDBs.
//            myTables = generate.generatePCVId(myTables);
            long endTime = System.currentTimeMillis();

            System.out.println("generate p-c xml tables time:"+(endTime1-startTime1));



            beginTime = System.currentTimeMillis();
            System.out.println("merge tables");
            List<Vector> myResult = mergeTable(tagList);
            Collections.sort(myResult,new MyComparator(Arrays.asList(0,2,4,6,8)));
            tjFastTable =  myResult;
            System.out.println("tjFastTable size:"+tjFastTable.size());
            endTime = System.currentTimeMillis();
            System.out.println("sort table time:"+totalSortTableTime);
            System.out.println("merge Table time:"+(endTime-beginTime-totalSortTableTime));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.exit(1); // also you can use System.exit(0);
        }

    }




}
