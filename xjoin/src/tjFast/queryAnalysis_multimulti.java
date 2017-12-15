package tjFast;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import produce.generateValueIdPair;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;

/**
 * Created by zzzhou on 2017-12-05.
 */
public class queryAnalysis_multimulti extends DefaultHandler {
    static String basicDocuemnt;
    Hashtable twigTagNames;
    String ROOT;
    Stack TagStack;
    static Set<String> xmlRelationTagSet = new HashSet<>();
    static List<String> allTags = new ArrayList<>();
    static List<Vector> result = new ArrayList<>();
    static List<List<Vector>> myTable = new ArrayList<>();
    static List<List<List<Vector>>> myTables = new ArrayList<>();
    static int queryNo;
    static int unitJump;
    static boolean joinContainResultTable = true;
    static List<String> basicDocuemntList = new ArrayList<>();
    static List<Hashtable> twigTagNamesList = new ArrayList<>();
    static List<String> rootList = new ArrayList<>();
    long sortTotalTime = 0L;

    public void getSolution() throws Exception{
        //add-order
//        List<String> joinOrderList = Arrays.asList("a","b","c","d","e","f");
        List<String> joinOrderList = Arrays.asList("Invoice","OrderId","Orderline","asin","price","productId");
        //get p-c relation table list
        myTables = getPCTables(joinOrderList);
        //read rdb tables
        readRDB();
        //join tables
        long startTime1 = System.currentTimeMillis();
        joinTablesByOrder(joinOrderList);
        long endTime1 = System.currentTimeMillis();
        System.out.println("join tables total time:"+(endTime1-startTime1));
        System.out.println("sort table total time:"+sortTotalTime);
    }

    public void joinTablesByOrder(List<String> joinOrderList){
        unitJump = queryNo + 1;
        for(int joinOrder=0; joinOrder<joinOrderList.size(); joinOrder++){
            //the tag that is going to be added to Result
            String addTag = joinOrderList.get(joinOrder);
            System.out.println("add tag:"+addTag);
            List<List<List<Vector>>> thisMyTables = myTables;
            //join add_tag with join_result
            List<List<String>> tagCombs = getJoinedTagComb(joinOrderList,joinOrder);
            //check joined tags combinations one by one
            for(List<String> tagComb:tagCombs) {
                List<List<Vector>> tablesToMerge = new ArrayList<>();
                List<List<Integer>> tableColumns = new ArrayList<>();
                for(int setOrder=0; setOrder<thisMyTables.size(); setOrder++){
                    List<List<Vector>> thisSet = thisMyTables.get(setOrder);
                    for(int tableCursor=0; tableCursor<thisSet.size(); tableCursor++){
                        List<Vector> thisTable = thisSet.get(tableCursor);
                        Vector tableTag = thisTable.get(0);
                        if (tableTag.containsAll(tagComb)) {
                            List<Integer> tableColumn = new ArrayList<>();
                            //find common tags column number
                            for (String tag : tagComb) {
                                int table_column = getColumn(tableTag, tag);
                                tableColumn.add(table_column*2);
                            }

                            //how to remove the already joined table???
                            //...

                            List<Vector> table_removeFirstRow = thisTable.subList(1, thisTable.size());
                            //sort current table according to column order
                            long startTime1 = System.currentTimeMillis();
                            Collections.sort(table_removeFirstRow, new MyComparator(tableColumn));
                            long endTime1 = System.currentTimeMillis();
                            sortTotalTime += endTime1-startTime1;
                            //add table
                            tablesToMerge.add(table_removeFirstRow);
                            //add column number to list
                            tableColumns.add(tableColumn);
                        }
                    }

                }
                //here all the tables that contain the join-tag combinations have been added to tablesToMerge
                //start to join these tables
                //if has available tables to join
                if(! tablesToMerge.isEmpty()){
                    //if result is empty, it is the first join
                    if(result.isEmpty()){
                        result = selfJoinTable(tablesToMerge, tableColumns);
                    }
                    //case 2: result has add-tag to join, normal joins
                    else if(result.get(0).size() == (joinOrder+1)*(unitJump)){
                        List<String> joinedTags = joinOrderList.subList(0,joinOrder+1);
                        List<Integer> resultColumn = new ArrayList<>();
                        //find tag columns in result
                        for (int i = 0; i < joinedTags.size(); i++) {
                            if (tagComb.contains(joinedTags.get(i))) {
                                resultColumn.add(i*unitJump);
                            }
                        }
                        //sort result table
                        long startTime1 = System.currentTimeMillis();
                        Collections.sort(result, new MyComparator(resultColumn));
                        long endTime1 = System.currentTimeMillis();
                        sortTotalTime += endTime1-startTime1;
                        //join result, and tables
                        result = joinTables(result, resultColumn, tablesToMerge, tableColumns);
                    }
                    //case 3: result does not have add-tag yet
                    else{
                        List<String> joinedTags = joinOrderList.subList(0,joinOrder+1);
                        List<Integer> resultColumn = new ArrayList<>();
                        //find tag columns in result
                        List<String> tagComb_sub = tagComb.subList(0,tagComb.size()-1);
                        for (int i = 0; i < joinedTags.size(); i++) {
                            if (tagComb_sub.contains(joinedTags.get(i))) {
                                resultColumn.add(i*unitJump);
                            }
                        }
                        //result has any other tag to join with tablesToMerge
                        if(! resultColumn.isEmpty()){
                            //sort result table
                            long startTime1 = System.currentTimeMillis();
                            Collections.sort(result, new MyComparator(resultColumn));
                            long endTime1 = System.currentTimeMillis();
                            sortTotalTime += endTime1-startTime1;
                            //join result, and tables
                            result = joinTables(result, resultColumn, tablesToMerge, tableColumns);

                        }
                        //else, result has no common tag with tables that contain add-tag -> n*n
                        else{
                            List<Vector> updateResult = new ArrayList<>();
                            List<Vector> tagTable= selfJoinTable(tablesToMerge, tableColumns);
                            for(int i=0; i<result.size(); i++){
                                for(int j=0; j<tagTable.size(); j++){
                                    Vector v = result.get(i);
                                    v.addAll(tagTable.get(j));
                                    updateResult.add(v);
                                }
                            }
                            result = updateResult;
                        }
                    }
                }

            }
            //check if tjFast can be done to prune result by structure
            //need to come up some better solution later
            if(joinOrder == 4){
                List<Integer> columnNos = Arrays.asList(3,9,12);
                doTjFast(result, columnNos,0);
            }
            if(joinOrder == 5){
                List<Integer> columnNos = Arrays.asList(9,15);
                doTjFast(result, columnNos,1);
            }

        }
    }

    public void doTjFast(List<Vector> table, List<Integer>columnNos, int queryN){
        List<Vector> prunedTable = new ArrayList<>();
        System.out.println("begin analysis query !");

        Query.setTwigTagNames(twigTagNamesList.get(queryN));


        Query.setRoot(rootList.get(queryN));

        utilities.DebugPrintln("Query root is " + Query.getRoot());

        System.out.println("begin analysis document !");

        try {
            DTDTable DTDInfor = loadDataSet.produceDTDInformation(basicDocuemntList.get(queryN));

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
            for(Vector v:table){
                Vector v1 = new Vector();
                for(int col=0; col<columnNos.size(); col++){
                    int colNo = columnNos.get(col);
                    v1.addAll(Arrays.asList(v.get(colNo), v.get(colNo+1+queryN)));
                }
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

                solutionCount = join.beginJoin();
//                System.out.println("solutionCount:"+solutionCount);
                joinendTime = System.currentTimeMillis();
                //System.out.println("join data time is " + (joinendTime - joinbeginTime));
                totalJoinTime += joinendTime - joinbeginTime;
                //tjFastbyAddTime = tjFastbyAddTime + joinendTime -loadbeginTime;

                //prune table
                if(solutionCount>0){
                    prunedTable.add(table.get(i));
                }
            }
            long tjFastEndTime = System.currentTimeMillis();
            long totalendTime = System.currentTimeMillis();
            result = prunedTable;
            System.out.println("solutionCount:"+result.size());
            //System.out.println("Total tjFast time is " + (tjFastEndTime-tjFastbeginTime));
            //System.out.println("Total tjFast by add time is " + tjFastbyAddTime);


            System.out.println("Total tjFast load data time is " + totalLoadTime);

            System.out.println("Total tjFast join data time is " + totalJoinTime);

            System.out.println("Total running time is " + (totalendTime - totalbeginTime));
        }
        catch (Exception e){
            System.out.println(e);
        }

    }

    public List<Vector> onlyOneTableJoin(List<Vector> table, int col, Boolean isRDB){
        List<Vector> prunedTable = new ArrayList<>();
        List<int[]> idList = new ArrayList<>();
        Vector firstRow = table.get(0);
        String value = firstRow.get(col).toString();
        int queryCursor = -1;
        if(!isRDB){
            queryCursor = (int)firstRow.get(firstRow.size()-1);
        }
        for(int rowNo=0; rowNo<table.size();) {
            String comPvalue = table.get(rowNo).get(col).toString();
            if(comPvalue.equals(value)) {
                if (!isRDB) {
                    int[] id = (int[]) table.get(rowNo).get(col + 1);
                    idList.add(id);
                }
                rowNo++;
                if(rowNo == table.size()){
                    Vector v = new Vector();
                    v.add(value);
                    //initialize
                    for(int qNo=0; qNo<queryNo; qNo++){
                        List<int[]> id = new ArrayList<>();
                        v.add(id);
                    }
                    if(!isRDB){
                        ((List<int[]>)v.get(queryCursor)).addAll(idList);
                    }
                    prunedTable.add(v);
                }
            }
            else{
                Vector v = new Vector();
                v.add(value);
                //initialize
                for(int qNo=0; qNo<queryNo; qNo++){
                    List<int[]> id = new ArrayList<>();
                    v.add(id);
                }
                if(!isRDB){
                    ((List<int[]>)v.get(queryCursor)).addAll(idList);
                }
                prunedTable.add(v);
                value = comPvalue;
            }
        }
        return prunedTable;
    }

    public List<Vector> selfJoinTable(List<List<Vector>> tablesToMerge, List<List<Integer>> tableColumns){
        List<Vector> selfJoinResult = new ArrayList<>();
        //initialize row cursor
        int tableNos = tableColumns.size();
        int[] rowCursor = new int[tableNos];
        List<Vector> baseTable = tablesToMerge.get(0);
        int baseTableColumn = tableColumns.get(0).get(0);
        //
        while(true){
            if(isEnd(tablesToMerge, rowCursor)){
                break;
            }
            Boolean noResult = false;
            List<List<Vector>> subTables = new ArrayList<>();
            //move result until value is not same
            String baseValue = baseTable.get(rowCursor[0]).get(baseTableColumn).toString();
            //update result row number
            int[] baseTableRowUpdate = moveCursorUntilNoEqual(baseTable, rowCursor[0], baseTableColumn, baseValue);
            rowCursor[0] = baseTableRowUpdate[1];
            //read and move tables in tablesToMerge until each next value new
            for(int tableCursor = 1; tableCursor < tableNos; tableCursor++) {
                List<Vector> thisTable = tablesToMerge.get(tableCursor);
                int rowNo = rowCursor[tableCursor];
                int colNo = tableColumns.get(tableCursor).get(0); // tagCombCursor: 0
                String thisValue = thisTable.get(rowNo).get(colNo).toString();
                //update row cursor
                int[] rowUpdate  = moveCursorUntilNoEqual(thisTable, rowNo, colNo, baseValue);

                rowNo = rowUpdate[0];
                int rowNoJumpEqual = rowUpdate[1];
                //get subTable
                if(rowNo == rowNoJumpEqual){
                    noResult = true;
                    break;
                }
                else{
                    rowCursor[tableCursor] = rowNoJumpEqual;
                    List<Vector> subTable = thisTable.subList(rowNo, rowCursor[tableCursor]);
                    subTables.add(subTable);
                }
            }
            //every table has a subTable-> everyTable has common value
            if(!noResult){
                //compare id
                //separate tables by their queryMark
                //initialize
                List<List<List<Vector>>> sepByQueryTables = new ArrayList<>();
                List<List<Integer>> sepByQueryColumn = new ArrayList<>();
                for(int queryCur=0; queryCur<queryNo; queryCur++){
                    List<List<Vector>> tempTable = new ArrayList<>();
                    List<Integer> tempCol = new ArrayList<>();
                    sepByQueryTables.add(tempTable);
                    sepByQueryColumn.add(tempCol);
                }
                //subBase Table
                List<Vector> subBaseTable = baseTable.subList(baseTableRowUpdate[0], rowCursor[0]);
                Vector firstRow = subBaseTable.get(0);
                //if base table is not rdb and has a queryMark
                if(firstRow.get(1) != null){
                    int queryMark = (int)firstRow.get(firstRow.size()-1);
                    int idColumn = baseTableColumn+1;
                    sepByQueryTables.get(queryMark).add(subBaseTable);
                    sepByQueryColumn.get(queryMark).add(idColumn);
                }

                //separate other tables by their queryMark
                for(int subTableCursor=0; subTableCursor<subTables.size(); subTableCursor++){
                    List<Vector> thisSubTable = subTables.get(subTableCursor);
                    //if not rdb
                    Vector thisFirstRow = thisSubTable.get(0);
                    if(thisFirstRow.get(1) != null){
                        int queryMark = (int)thisFirstRow.get(thisFirstRow.size()-1);//[value, id, queryMark]
                        int idColumn = tableColumns.get(subTableCursor+1).get(0)+1;
                        sepByQueryTables.get(queryMark).add(thisSubTable);
                        sepByQueryColumn.get(queryMark).add(idColumn);
                    }
                }

                //find common id
                Vector thisResultRow = new Vector();
                thisResultRow.add(baseValue);
                for(int queryCursor=0; queryCursor<queryNo; queryCursor++) {
                    List<List<Vector>> thisQuerySubTables = sepByQueryTables.get(queryCursor);
                    List<Integer> thisQuerySubTableColumns = sepByQueryColumn.get(queryCursor);
                    if (!thisQuerySubTables.isEmpty()) {
                        Vector v = getValueCommonIds(thisQuerySubTables, thisQuerySubTableColumns);
                        List<int[]> idLists = (List<int[]>) v.get(0);
                        thisResultRow.add(idLists);
                    }
                    else thisResultRow.add(new ArrayList<>());
                }
                selfJoinResult.add(thisResultRow);
            }
        }
        return selfJoinResult;
    }

    static List<Vector> subResultTable = new ArrayList<>();
    public List<Vector> joinTables(List<Vector> rbaseTable, List<Integer> resultColumns,List<List<Vector>> tablesToMerge, List<List<Integer>> tableColumns){
        List<Vector> prunedTable = new ArrayList<>();
        //initialize row cursor
        int tableNos = tableColumns.size();
        int[] rowCursor = new int[tableNos];
        List<Vector> baseTable = rbaseTable;
        int baseTableRow = 0;
        Boolean notOnlyRDB = false;
        //while result still has tag to join
        while(!resultColumns.isEmpty()){
            int baseTableColumn = resultColumns.get(0);

            if(baseTableRow == baseTable.size() || isEnd(tablesToMerge, rowCursor)){
                break;
            }
            subResultTable = new ArrayList<>();
            Boolean noResult = false;
            List<List<Vector>> subTables = new ArrayList<>();
            //move result until value is not same
            String baseValue = baseTable.get(baseTableRow).get(baseTableColumn).toString();
            //update result row number
            int[] baseTableRowUpdate = moveCursorUntilNoEqual(baseTable, baseTableRow, baseTableColumn, baseValue);
            baseTableRow = baseTableRowUpdate[1];
            //read and move tables in tablesToMerge until each next value new
            for(int tableCursor = 0; tableCursor < tableNos; tableCursor++) {
                List<Vector> thisTable = tablesToMerge.get(tableCursor);
                int rowNo = rowCursor[tableCursor];
                int colNo = tableColumns.get(tableCursor).get(0); // tagCombCursor: 0
                String thisValue = thisTable.get(rowNo).get(colNo).toString();
                //update row cursor
                int[] rowUpdate  = moveCursorUntilNoEqual(thisTable, rowNo, colNo, baseValue);

                rowNo = rowUpdate[0];
                int rowNoJumpEqual = rowUpdate[1];
                //get subTable
                if(rowNo == rowNoJumpEqual){
                    noResult = true;
                    break;
                }
                else{
                    rowCursor[tableCursor] = rowNoJumpEqual;
                    List<Vector> subTable = thisTable.subList(rowNo, rowCursor[tableCursor]);
                    subTables.add(subTable);
                }
            }
            //every table has a subTable-> everyTable has common value
            if(!noResult){
                //compare id
                //separate tables by their queryMark
                //initialize
                List<List<List<Vector>>> sepByQueryTables = new ArrayList<>();
                List<List<Integer>> sepByQueryColumn = new ArrayList<>();
                List<List<List<Integer>>> sepByQueryFullColumns = new ArrayList<>();
                for(int queryCur=0; queryCur<queryNo; queryCur++){
                    List<List<Vector>> tempTable = new ArrayList<>();
                    List<List<Integer>> tempFulCol = new ArrayList<>();
                    List<Integer> tempCol = new ArrayList<>();
                    sepByQueryTables.add(tempTable);
                    sepByQueryColumn.add(tempCol);
                    sepByQueryFullColumns.add(tempFulCol);
                }

                List<List<Vector>> rdbTables = new ArrayList<>();
                List<List<Integer>> rdbColumns = new ArrayList<>();
                notOnlyRDB = false;
                //separate tables by their queryMark
                for(int subTableCursor=0; subTableCursor<subTables.size(); subTableCursor++){
                    List<Vector> thisSubTable = subTables.get(subTableCursor);
                    //if not rdb
                    Vector thisFirstRow = thisSubTable.get(0);
                    if(thisFirstRow.get(1) != null){
                        int queryMark = (int)thisFirstRow.get(thisFirstRow.size()-1);//[value, id, queryMark]
                        List<Integer> thisTableColumn = tableColumns.get(subTableCursor);
                        int idColumn = thisTableColumn.get(0)+1;
                        List<Integer> subFullColumns = thisTableColumn.subList(1,thisTableColumn.size());
                        sepByQueryTables.get(queryMark).add(thisSubTable);
                        sepByQueryColumn.get(queryMark).add(idColumn);
                        sepByQueryFullColumns.get(queryMark).add(subFullColumns);
                        notOnlyRDB = true;
                    }
                    else{
                        rdbTables.add(thisSubTable);
                        List<Integer> thisRdbColumn = tableColumns.get(subTableCursor);
                        rdbColumns.add(thisRdbColumn.subList(1,thisRdbColumn.size()));
                    }
                }

                //subBase Table
                subResultTable = baseTable.subList(baseTableRowUpdate[0], baseTableRow);
                List<List<List<Vector>>> prunedSepByQueryTables = new ArrayList<>();

                //for next step joinTable
                List<List<Vector>> subTablesToMerge = new ArrayList<>();
                List<List<Integer>> subTablesColumns = new ArrayList<>();

                if(notOnlyRDB) {
                    //find common id one query by one query
                    for (int queryCursor = 0; queryCursor < queryNo; queryCursor++) {
                        int baseTableCol = baseTableColumn + 1 + queryCursor;
                        List<int[]> baseValueIdList = (List<int[]>) subResultTable.get(0).get(baseTableCol);

                        List<List<Vector>> thisQuerySubTables = sepByQueryTables.get(queryCursor);
                        List<Integer> thisQuerySubTableColumns = sepByQueryColumn.get(queryCursor);
                        //should never be empty. but we can check here.
                        if (!thisQuerySubTables.isEmpty()) {
                            //result id_list is empty, add tables id_list to result
                            if(baseValueIdList.isEmpty()){
                                Vector v = pruneTableByIdWithoutResult(thisQuerySubTables, thisQuerySubTableColumns);
                                thisQuerySubTables = (List<List<Vector>>)v.get(0);
                                subTablesToMerge.addAll(thisQuerySubTables);
                                subTablesColumns.addAll(sepByQueryFullColumns.get(queryCursor));
                                List<int[]> idLists = (List<int[]>)v.get(1);
                                for(int i=0; i<subResultTable.size(); i++){
                                    ((List<int[]>)subResultTable.get(i).get(baseTableCol)).addAll(idLists);
                                }
                            }
                            else {
                                thisQuerySubTables = pruneTableById(baseValueIdList, thisQuerySubTables, thisQuerySubTableColumns);
                                subTablesToMerge.addAll(thisQuerySubTables);
                                subTablesColumns.addAll(sepByQueryFullColumns.get(queryCursor));
                            }
                        }
                    }
                }

                //if have rdb tables
                if(! rdbTables.isEmpty()){
                    subTablesToMerge.addAll(rdbTables);
                    subTablesColumns.addAll(rdbColumns);
                }

                List<Vector> copyTable = joinTables(subResultTable, resultColumns.subList(1,resultColumns.size()), subTablesToMerge, subTablesColumns);
            }
            if(! subResultTable.isEmpty()){
                prunedTable.addAll(subResultTable);
            }
        }
        //if result do not have add-tag yet
        if(resultColumns.isEmpty() && ! tableColumns.get(0).isEmpty()){
            List<Vector> add_tagData = new ArrayList<>();
            if(tablesToMerge.size() > 1){
                add_tagData = selfJoinTable(tablesToMerge, tableColumns);
            }
            else{
                add_tagData = onlyOneTableJoin(tablesToMerge.get(0), tableColumns.get(0).get(0), !notOnlyRDB);
            }
            subResultTable = new ArrayList<>();
            for(int i=0; i<rbaseTable.size(); i++){
                for(int j=0; j<add_tagData.size(); j++){
                    Vector v = (Vector)rbaseTable.get(i).clone();
                    v.addAll(add_tagData.get(j));
                    subResultTable.add(v);
                }
            }
        }
        else{
            if(prunedTable.isEmpty())
                subResultTable = rbaseTable;
        }
        return prunedTable;
    }

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

    public Vector pruneTableByIdWithoutResult(List<List<Vector>> tables,List<Integer> columnNos){
        List<List<Vector>> prunedTable = new ArrayList<>();
        Vector v = new Vector();
        List<int[]> idLists = new ArrayList<>();
        int tableNo = tables.size();
        int[] rowCursor = new int[tableNo];
        while(true){
            if(isEnd(tables, rowCursor)) break;
            List<int[]> ids = new ArrayList<>();
            for(int tableCursor=0; tableCursor<tableNo; tableCursor++){
                ids.add((int[])tables.get(tableCursor).get(rowCursor[tableCursor]).get(columnNos.get(tableCursor)));
            }
            int compa = compareIds(ids);
            //compa-> -1 means the ids are equal. other values means to move corresponding cursor.
            if(compa==-1){
                for(int i=0; i<tableNo; i++){
                    prunedTable.add(tables.get(i).get(rowCursor[i]));
                    rowCursor[i] += 1;
                }
                idLists.add(ids.get(0));
            }
            else rowCursor[compa] += 1;
        }
        v.add(prunedTable);
        v.add(idLists);
        return v;
    }

    public List<List<Vector>> pruneTableById(List<int[]> baseTableIdList, List<List<Vector>> tables,List<Integer> columnNos){
        List<List<Vector>> prunedTable = new ArrayList<>();
        int tableNo = tables.size();
        for(int i=0; i<tableNo; i++){
            List<Vector> tempTable = new ArrayList<>();
            prunedTable.add(tempTable);
        }
        int[] rowCursor = new int[tableNo];
        while(true){
            if(isEnd(tables, rowCursor)) break;
            List<int[]> ids = new ArrayList<>();
            for(int tableCursor=0; tableCursor<tableNo; tableCursor++){
                ids.add((int[])tables.get(tableCursor).get(rowCursor[tableCursor]).get(columnNos.get(tableCursor)));
            }
            int compa = compareIds(ids);
            //compa-> -1 means the ids are equal. other values means to move corresponding cursor.
            if(compa==-1){
                if(baseTableIdList.contains(ids.get(0))){
                    for(int i=0; i<tableNo; i++){
                        prunedTable.get(i).add(tables.get(i).get(rowCursor[i]));
                        rowCursor[i] += 1;
                    }
                }
            }
            else rowCursor[compa] += 1;
        }
        return prunedTable;
    }



    public Vector getValueCommonIds(List<List<Vector>> tables, List<Integer> columnNos){
        Boolean notEnd = true;
        List<List<Vector>> prunedTable = new ArrayList<>();
        List<int[]> id_List = new ArrayList<>();
        int tableNo = tables.size();
        int[] rowCursor = new int[tableNo];
        while(notEnd){
            if(isEnd(tables, rowCursor)) break;
            List<int[]> ids = new ArrayList<>();
            for(int tableCursor=0; tableCursor<tableNo; tableCursor++){
                ids.add((int[])tables.get(tableCursor).get(rowCursor[tableCursor]).get(columnNos.get(tableCursor)));
            }
            int compa = compareIds(ids);
            //compa-> -1 means the ids are equal. other values means to move corresponding cursor.
            if(compa==-1){
                for(int i=0; i<tableNo; i++){
                    prunedTable.add(tables.get(i).get(rowCursor[i]));
                    rowCursor[i] += 1;
                }
                id_List.add(ids.get(0));
            }
            else rowCursor[compa] += 1;
        }
        Vector v = new Vector();
        v.add(id_List);
        v.add(prunedTable);
        return v;
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

    public int[] moveCursorUntilNoEqual(List<Vector> table, int rowNo, int columnNo, String thisValue){
        int row=rowNo;
        int[] startEqualRow = {row, row};
        for(; row<table.size();){
            Vector thisRow = table.get(row);
            String compareValue =  thisRow.get(columnNo).toString();
            int compareResult = thisValue.compareTo(compareValue);
            if(compareResult == 0){
                startEqualRow[0] = row;
                //find sub-list of equal values in table
                for(;row<table.size();){
                    Vector thisRow_fEqual = table.get(row);
                    String compareValue_fEqual =  thisRow_fEqual.get(columnNo).toString();
                    if(thisValue.equals(compareValue_fEqual)){
                        row++;
                    }
                    else break;
                }
                startEqualRow[1] = row;
            }
            else if(compareResult > 0){
                row++;
                startEqualRow[0] = row;
                startEqualRow[1] = row;
            }
            else break;
        }
        return startEqualRow;
    }

    public int moveCursorUntilNextNew(List<Vector> table, int rowNo, int columnNo, String thisValue){
        int row=rowNo;
        for(; row<table.size();){
            Vector thisRow = table.get(row);
            String compareValue =  thisRow.get(columnNo).toString();
            if(thisValue.equals(compareValue)){
                row++;
            }
            else break;
        }
        return row;
    }

    public boolean isAnySetEnd(List<List<List<Vector>>> tablesToMerge, List<int[]> tableRows){
        Boolean oneIsEnd = false;
        for(int setCursor=0; setCursor<unitJump; setCursor++){
            if(isEnd(tablesToMerge.get(setCursor), tableRows.get(setCursor))){
                oneIsEnd = true;
                break;
            }
        }
        return oneIsEnd;
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



    //get all possible tag combinations
    public List<List<String>> getJoinedTagComb(List<String> tagList, int curTagNo){
        List<List<String>> joinedTagComb = new ArrayList<>(new ArrayList<>());

        List<String> joinedTag = tagList.subList(0,curTagNo);
        int nCnt = joinedTag.size();
        //right shift, divide
        int nBit = (0xFFFFFFFF >>> (32 - nCnt));

        for (int i = 1; i <= nBit; i++) {
            List<String> combs = new ArrayList<>();
            for (int j = 0; j < nCnt; j++) {
                if ((i << (31 - j)) >> 31 == -1) {
                    combs.add(joinedTag.get(j));
                }
            }
            combs.add(tagList.get(curTagNo));
            joinedTagComb.add(combs);
        }
        joinedTagComb.sort(Comparator.comparing(List<String>::size).reversed());
        joinedTagComb.add(Arrays.asList(tagList.get(curTagNo)));
        return joinedTagComb;
    }

    //read RDB value and merge list to myTables.
    public void readRDB() throws Exception{
        long startTime = System.currentTimeMillis();
        List<List<Vector>> rdbTables = new ArrayList<>();
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
            rdbTables.add(rdb);
        }
        myTables.add(rdbTables);
        long endTime = System.currentTimeMillis();
        System.out.println("read rdb time:"+(endTime-startTime));
    }

    public List<List<List<Vector>>> getPCTables(List<String> tagList) throws Exception{
        //Analysis queries to get pc relations
        File queryFolder = new File("xjoin/src/multi_rdbs/queries/");
        File basicDocumnetFolder = new File("xjoin/src/multi_rdbs/invoices/");

        generateValueIdPair generate = new generateValueIdPair();
        //read query file
        File[] listOfFiles_query = queryFolder.listFiles();
        File[] listOfFiles_document = basicDocumnetFolder.listFiles();

        queryNo = listOfFiles_query.length;
        if( queryNo != listOfFiles_document.length){
            System.out.println("please check query and basic document files. Their sizes are not same.");
            System.exit(0) ;
        }
        //to store all pc tables from different queries
        List<List<List<Vector>>> myTables = new ArrayList<>();
        for(int i=0; i< queryNo; i++){
            File queryFile = listOfFiles_query[i];
            File documentFile = listOfFiles_document[i];
            basicDocuemnt = documentFile.getPath();
            basicDocuemntList.add(documentFile.getPath());
            List<String> currentTagList = new ArrayList<>();
            analysisQuery(queryFile.getPath());
            for(String s:tagList) {
                //xmlRelationTagSet -> pc relations
                if (allTags.contains(s)) {
                    //check tags in tagList contained in allTags in current query file
                    currentTagList.add(s);
                    if (!xmlRelationTagSet.contains(s)) {
                        Vector v = new Vector();
                        List<Vector> l = new ArrayList<>();
                        v.add(s);
                        l.add(v);
                        myTable.add(l);
                    }
                }
            }
            //analysis basic document
            HashMap<String, List<Vector>> tagMaps = generate.generateTagVId(currentTagList,basicDocuemnt, i);
            long startTime = System.currentTimeMillis();
            myTable = matchPC(tagMaps,myTable,i);
            long endTime = System.currentTimeMillis();
            System.out.println("generate p-c xml tables time:"+(endTime-startTime));
            myTables.add(myTable);
            myTable = new ArrayList<>();
            allTags.clear();
        }
        return myTables;
    }

    public List<List<Vector>> matchPC(HashMap<String, List<Vector>> tagMaps, List<List<Vector>> myTables, int queryNo){
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
                        //do not need to reserve place to store other queries' id_list, since solution will be stored in another list.
                        //add the query No after each line.
                        v.addAll(Arrays.asList(table_p.get(p).get(0).toString(), p_id, table_c.get(c).get(0).toString(),c_id, queryNo));
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

    public void analysisQuery(String queryFile)  throws Exception {
        if (queryFile == null) {
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
        xmlReader.setContentHandler(new queryAnalysis_multimulti());

        // Set an ErrorHandler before parsing
        xmlReader.setErrorHandler(new MyErrorHandler(System.err));

        // Tell the XMLReader to parse the XML document
        xmlReader.parse(queryFile);
    }

    private static void usage() {
        System.err.println("Usage: QueryAnalysis <file.xml>");
        System.exit(1);
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
            myTable.add(pc);
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
        allTags.add(currentTag);
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
        else{
            ROOT = currentTag;
            rootList.add(currentTag);
        }

        TagStack.push(currentTag);

    }//end startElement


    public void endElement(String namespaceURI, String localName,
                           String qName)
            throws SAXException {

        TagStack.pop();


    }//end endElement

    // Parser calls this once after parsing a document
    public void endDocument() throws SAXException {
        twigTagNamesList.add((Hashtable) twigTagNames.clone());
    }

    static public void main(String[] args) throws Exception {
        queryAnalysis_multimulti qbm = new queryAnalysis_multimulti();
        qbm.getSolution();
    }
}
