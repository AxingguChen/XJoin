package tjFast;

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
    //todo List<List<List<Vector>>> myTables -> List<Vector>myTables <t,t,f,f,t,f>
    static List<List<Vector>> myTables = new ArrayList<>();
    static List<int[]> myTablesTagList = new ArrayList<>();
    static int queryNo;
    static int unitJump;
    static boolean joinContainResultTable = true;
    static List<String> basicDocuemntList = new ArrayList<>();
    static List<Hashtable> twigTagNamesList = new ArrayList<>();
    static List<String> rootList = new ArrayList<>();
    long sortTotalTime = 0L;
    int resultInt = 2;//result[value, idVector, value, idVector...], so values are always in even numbers

    public void getSolution(List<String> joinOrderList,String xml_query_file,String xml_document_file, String rdb_document_file) throws Exception{
        //add-order
//        List<String> joinOrderList = Arrays.asList("a","b","c","d","e","f");
//        List<String> joinOrderList = Arrays.asList("Invoice","OrderId","Orderline","asin","price","productId");
        //get p-c relation table list
        getPCTables(joinOrderList, xml_query_file, xml_document_file);
        //read rdb tables
        readRDB(rdb_document_file, joinOrderList);
        //join tables
        long startTime1 = System.currentTimeMillis();
        joinTablesByOrder(joinOrderList);
        long endTime1 = System.currentTimeMillis();
        System.out.println("join tables total time:"+(endTime1-startTime1));

    }

    public void joinTablesByOrder(List<String> joinOrderList){
        for(int joinOrder=0; joinOrder<joinOrderList.size(); joinOrder++){
            String addTag = joinOrderList.get(joinOrder);
            System.out.println("Add tag:"+addTag);
            List<List<Vector>> tablesToMerge = new ArrayList<>();
            List<int[]> tablesTagList = new ArrayList<>();

            //get tables that contains this tag
            for(int tableCursor=0; tableCursor<myTables.size(); tableCursor++){
                //if current table contains addTag
                if(myTablesTagList.get(tableCursor)[joinOrder]==1){
                    tablesToMerge.add(myTables.get(tableCursor));
                    tablesTagList.add(myTablesTagList.get(tableCursor));
                }
            }
            //here we have all the tables that need to be joined
            //Join based on each line of result table
            //If result is empty, it is to join the first tag, no result table to base.
            if(result.isEmpty()){
                //join all tables
                //p.s. The tags of tables are assumed to appear in the same order as joinOrderList
                //so if we would like to join the first tag, it will always be column 0 in tables from tablesToMerge
                int[] colNumbers = new int[tablesToMerge.size()];
                result = joinOfTheFirstTag(tablesToMerge,colNumbers);
            }
            //else the join is based on result table.
            else{
                result = joinOfRestTags(joinOrder, tablesToMerge, tablesTagList);
            }
            System.out.println("result size:"+result.size() + " after the "+joinOrder+" join");
//            for(int i=0;i<result.size();i++){
//                for(int j=0; j<result.get(0).size();j=j+2)
//                    System.out.print(result.get(i).get(j)+",");
//                System.out.println("");
//            }
//            System.out.println("result first row:"+result.get(0));
            //do tjFast here SET1
//            if(joinOrder==4){
////                System.out.println("before tjFast 1");
//
//                doTjFast(result, Arrays.asList(6,8,2), 0);
////                for(int i=0;i<result.size();i++)
////                    System.out.println(result.get(i).get(0)+","+result.get(i).get(2)+","+result.get(i).get(4)
////                            +","+result.get(i).get(6)+","+result.get(i).get(8));
//
//            }
//            if(joinOrder==5){
////                System.out.println("before tjFast 2");
//                doTjFast(result, Arrays.asList(6,10), 1);
////                for(int i=0;i<result.size();i++)
////                    System.out.println(result.get(i).get(0)+","+result.get(i).get(2)+","+result.get(i).get(4)
////                            +","+result.get(i).get(6)+","+result.get(i).get(8)+","+result.get(i).get(10));
//            }

            if(joinOrder==4){
                doTjFast(result, Arrays.asList(6,8,2), 0);
            }
            if(joinOrder==5){
//                System.out.println(result.get(0).get(0)+","+result.get(0).get(2)+","+result.get(0).get(4)
//                        +","+result.get(0).get(6)+","+result.get(0).get(8)+","+result.get(0).get(10));
                doTjFast(result, Arrays.asList(2,10), 1);
//                System.out.println(result.get(0).get(0)+","+result.get(0).get(2)+","+result.get(0).get(4)
//                            +","+result.get(0).get(6)+","+result.get(0).get(8)+","+result.get(0).get(10));

            }
        }}

    public List<Vector> joinOfRestTags(int joinedTagNo, List<List<Vector>> tablesToMerge, List<int[]> tablesTagList){
        //extend result
        List<Vector> updatedResult = new ArrayList<>();
        //if only add on addTag
        Boolean onlyAddTagJoin = true;
        //calculate colNo for each table in tablesToMerge
        List<List<Integer>> tablesToMergeCols = new ArrayList<>();
        List<List<Integer>> coresResultCols = new ArrayList<>();
        for(int tableCursor=0; tableCursor<tablesToMerge.size(); tableCursor++){
            int[] tableTagList = tablesTagList.get(tableCursor);
            List<Integer> tableCols = new ArrayList<>();
            List<Integer> thisCoresResultCols = new ArrayList<>();
            //for each added tag
            for(int tagNo=0; tagNo<joinedTagNo; tagNo++){
                //if this table contains this added tag
                if(tableTagList[tagNo] == 1){
                    onlyAddTagJoin = false;
                    int colNo = getColNo(tableTagList, tagNo);
                    tableCols.add(colNo);
                    thisCoresResultCols.add(tagNo*2);
                }
            }
            tableCols.add(getColNo(tableTagList,joinedTagNo));
            tablesToMergeCols.add(tableCols);
            //result table corresponding col numbers
            coresResultCols.add(thisCoresResultCols);
        }
        //if all the tables only has addTag to join, n*n -> do not need to extend result row by row
        if(onlyAddTagJoin){
            //join on addTag
            //calculate each colNumber
            int tableCount = tablesTagList.size();
            int[] colNumbers = new int[tableCount];
            for(int tableCursor=0; tableCursor<tableCount; tableCursor++){
                int colNo = getColNo(tablesTagList.get(tableCursor), joinedTagNo);
                colNumbers[tableCursor] = colNo;
            }
            //join on itself
            List<Vector> thisResult = joinOfTheFirstTag(tablesToMerge, colNumbers);
            for(int rowNo=0; rowNo<result.size(); rowNo++){
                for(int addRowN=0; addRowN<thisResult.size(); addRowN++){
                    Vector updateRow = new Vector();
                    Vector thisRow = result.get(rowNo);
                    Vector addToRow = thisResult.get(addRowN);
                    updateRow.addAll(thisRow);
                    updateRow.addAll(addToRow);
                    updatedResult.add(updateRow);
                }
            }
        }
        else{
            for(int resultRowNo=0; resultRowNo<result.size(); resultRowNo++){
//                System.out.println("row no:"+resultRowNo);
                Boolean noResult = false;
                Vector resultRow = result.get(resultRowNo);
                int tableNos = tablesToMerge.size();
                List<List<Vector>> addTagSubTables = new ArrayList<>();
//                List<Integer> addTagCols = new ArrayList<>();
                int[] addTagSubTablesCols = new int[tableNos];
                for(int tableCursor=0; tableCursor<tableNos; tableCursor++){
                    List<Vector> thisTable = tablesToMerge.get(tableCursor);
                    List<Integer> thisTableCols = tablesToMergeCols.get(tableCursor);
                    List<Integer> thisTcoresRCols = coresResultCols.get(tableCursor);
                    if(thisTcoresRCols.size()>0){
                        for(int tagCursor=0; tagCursor<thisTcoresRCols.size(); tagCursor++){
                            int colNo_t = thisTableCols.get(tagCursor);
                            int colNo_r = thisTcoresRCols.get(tagCursor);
                            String value_r = resultRow.get(colNo_r).toString();
                            //search on subTables
                            int[] rowNos = binarySearch(thisTable, colNo_t, value_r);
                            //value has common part
                            if(rowNos[0] >= 0){
                                //same value subTable
                                thisTable = thisTable.subList(rowNos[0], rowNos[1]);
                                //this tag value same, compare id if it is table from xml. If it is a rdb Table, skip.
                                //calculate queryNo
//                                int colCount_t = thisTable.get(0).size();
//                                //if colCount is odd, the table is from xml and has a queryNo. Otherwise, the table is rdb has no id need to compare
//                                if((colCount_t & 1) != 0){
//                                    int thisTQueryNo = (int)thisTable.get(0).get(thisTable.get(0).size()-1);//this table queryNo
//                                    List<List<int[]>> resultIDList = (List<List<int[]>>) resultRow.get(colNo_r+1);
//                                    List<int[]> resultCoresQueryIDList = resultIDList.get(thisTQueryNo);
//                                    //??if result cores queryID list is empty??
//                                    if(!resultCoresQueryIDList.isEmpty()){
//                                    Vector reData = matchIDInList(resultCoresQueryIDList,thisTable, colNo_t+1);
//                                    resultCoresQueryIDList = (List<int[]>)reData.get(0);
//                                    //if idList has valid match
//                                    if(!resultCoresQueryIDList.isEmpty()){
//                                        thisTable = (List<Vector>) reData.get(1);
//                                        //todo accutually we need to update result id list here ?
//                                    }
//                                    //else this result row can be skipped
//                                    else{
//                                        noResult = true;
//                                        break;}
//                                }}
                            }
                        }
                    }
                    //if this table has result
                    if(!noResult){
                        addTagSubTables.add(thisTable);
                        //column number of addTag
                        addTagSubTablesCols[tableCursor] = thisTableCols.get(thisTableCols.size()-1);
                    }
                    else{
                        break;
                    }
                }
                //if has result
                if(!noResult){
                    //merge table's on addTag
                    List<Vector> addTagResult = joinOfTheFirstTag(addTagSubTables, addTagSubTablesCols);
                    if(!addTagResult.isEmpty()){
                        for(int row=0; row<addTagResult.size(); row++){
                            Vector addContent = addTagResult.get(row);
                            Vector extendResultRow = new Vector();
                            extendResultRow.addAll(resultRow);
                            extendResultRow.addAll(addContent);
                            updatedResult.add(extendResultRow);
                        }
                    }
                }
                //else skip this row.
            }
        }
        return updatedResult;
    }

    public Vector matchIDInList(List<int[]> idList, List<Vector> table, int colNo){
        List<int[]> idList_updated = new ArrayList<>();
        List<Vector> table_updated = new ArrayList<>();
        for(int rowCursor=0; rowCursor<table.size(); rowCursor++){
            int[] thisId = (int[])table.get(rowCursor).get(colNo);
            if(idList.contains(thisId)){
                idList_updated.add(thisId);
                table_updated.add(table.get(rowCursor));
            }
        }
        Vector reData = new Vector();
        reData.add(idList_updated);
        reData.add(table_updated);
        return reData;
    }


    public int[] binarySearch(List<Vector> table, int colNo, String value){
        int tableSize = table.size();
        int start = 0;//start position
        int end = tableSize;//end position
        int mid = 0;
        int[] rowNos = new int[2];
        Boolean found = false;
        while(start<=end){
            mid = start + (end-start)/2;
            String midValue = table.get(mid).get(colNo).toString();
            int compResult = midValue.compareTo(value);
            //if midValue > value
            if(compResult>0) end = mid - 1;
            else if(compResult<0) start = mid + 1;
            else{
                found = true;
                break;
            }
        }
        //find same value that may exist in previous and subsequent positions
        int sp = mid;
        int ep = mid;
        if(found == true) {
            //previous
            while (sp >= 0) {
                String thisValue = table.get(sp).get(colNo).toString();
                if (thisValue.compareTo(value) == 0 ) sp--;
                else {
                    break;
                }
            }
            //subsequent
            while (ep < tableSize) {
                String thisValue = table.get(ep).get(colNo).toString();
                if (thisValue.compareTo(value) == 0) ep++;
                else{
                    break;}
            }
        }
        else{
            sp = -2;
        }
        rowNos[0] = sp+1;
        rowNos[1] = ep;
        return rowNos;
    }

    public int getColNo(int[] tagMarks, int thisTagNo){
        int count = 0;
        int endPoint = thisTagNo+1;
        for(int tag=0; tag<endPoint; tag++){
            if(tagMarks[tag] == 1) count++;
        }
        return (count-1)*2;
    }


    public List<Vector> joinOfTheFirstTag(List<List<Vector>> tablesToMerge, int[] colNumbers){
        List<Vector> thisResult = new ArrayList<>();
        int tableNos = tablesToMerge.size();
        int[] rowCursor = new int[tableNos];
        while(true){
            if(isEnd(tablesToMerge, rowCursor)){
                break;
            }
            List<String> tagValues = new ArrayList<>();
            for(int tableCursor=0; tableCursor<tableNos; tableCursor++){
                List<Vector> table = tablesToMerge.get(tableCursor);
                int rowNo = rowCursor[tableCursor];
                String tagValue = table.get(rowNo).get(colNumbers[tableCursor]).toString();
                tagValues.add(tagValue);
            }
            int compareResult = makeComparison_FBV(tagValues);
            //if values are equal
            if(compareResult == -1){
                String commonValue = tagValues.get(0);
                //compare common values' ids, and update rowCursor
                Vector reData = moveCursorWhenEqual(tablesToMerge, tableNos, rowCursor, colNumbers, commonValue);
                rowCursor = (int[])reData.get(0);
                List<List<int[]>> ids = (List<List<int[]>>)reData.get(1);
                if(!ids.isEmpty()){
                    //add this row to result
                    Vector resultRow = new Vector();
                    resultRow.add(commonValue);
                    resultRow.add(ids);
                    thisResult.add(resultRow);
                }
            }
            //if not equal, update the row cursor of tables until their value is-
            //-equal or bigger than the biggest value of last comparison
            else{
                String biggestValue = tagValues.get(compareResult);
                rowCursor = moveCursorUntilEoBthanBv(tablesToMerge, tableNos, rowCursor, colNumbers, biggestValue);
            }
        }
        return thisResult;
    }

    //return 0: updated rowCursor. 1: same value's common ids for different query
    public Vector moveCursorWhenEqual(List<List<Vector>> tablesToMerge, int tableNos, int[] rowCursor, int[] colNumbers,String value){
        List<List<int[]>> ids = new ArrayList<>();
        List<List<List<Vector>>> subTablesToMerge = new ArrayList<>();
        //initialize ids and subTables
        for(int queryCursor=0; queryCursor<queryNo; queryCursor++){
            List<List<Vector>> tables = new ArrayList<>();
            subTablesToMerge.add(tables);
        }
        //separate tables from different queries, update rowCursors
        for(int tableCursor=0; tableCursor<tableNos; tableCursor++){
            List<Vector> table = tablesToMerge.get(tableCursor);
            int rowNo = rowCursor[tableCursor];
            int colNo = colNumbers[tableCursor];
            int colCount = table.get(0).size();
            rowCursor[tableCursor] = moveCursorUntilNextNew(table, rowNo, colNo, value);
            //if colCount is odd, the table is from xml and has a queryNo. Otherwise, the table is rdb has no id need to compare
            if((colCount & 1) != 0){
                int queryNo = (int)table.get(rowNo).get(colCount-1);
                List<Vector> subTable = table.subList(rowNo, rowCursor[tableCursor]);
                subTablesToMerge.get(queryNo).add(subTable);
            }
        }
        //check ids for each query
        //keep the id if the id is exist in each table which comes from the same query
        for(int queryCursor=0; queryCursor<queryNo; queryCursor++){
            List<List<Vector>> thisQuerySubTables = subTablesToMerge.get(queryCursor);
            List<int[]> idList = new ArrayList<>();
            int tableNo = thisQuerySubTables.size();
            //column number is "1" when join first tag
            if(tableNo > 1){
                //note: this method seems still cannot avoid id duplicate
                int[] thisQueryRowCursor = new int[tableNo];
                while(true){
                    if(isEnd(thisQuerySubTables, thisQueryRowCursor)) break;
                    List<int[]> compareIdList = new ArrayList<>();
                    for(int tableCursor=0; tableCursor<tableNo; tableCursor++){
                        int colNo = colNumbers[tableCursor]+1;
                        compareIdList.add((int[])thisQuerySubTables.get(tableCursor).get(thisQueryRowCursor[tableCursor]).get(colNo));
                    }
                    int compa = compareIds(compareIdList);
                    if(compa==-1){
                        idList.add(compareIdList.get(0));
                        for(int i=0; i<tableNo; i++) thisQueryRowCursor[i] += 1;
                    }
                    else thisQueryRowCursor[compa] += 1;
                }
            }
            //if only has one table, no comparison of the ids need to be made
            else if(tableNo == 1){
                List<Vector> thisQuerySubTable = thisQuerySubTables.get(0);
                int colNo = colNumbers[0]+1;
                for(int rowNo=0; rowNo<thisQuerySubTable.size(); rowNo++){
                    idList.add((int[])thisQuerySubTable.get(rowNo).get(colNo));
                }
            }

            ids.add(idList);

        }
        Vector reData = new Vector();
        reData.add(rowCursor);
        reData.add(ids);
        return reData;
    }


    //update rowCursor until each table's value is equal or bigger than the biggest value.
    public int[] moveCursorUntilEoBthanBv(List<List<Vector>> tablesToMerge, int tableNos, int[] rowCursor, int[] colNumbers,String biggestValue){
        for(int tableCursor=0; tableCursor<tableNos; tableCursor++){
            List<Vector> thisTable = tablesToMerge.get(tableCursor);
            int row = rowCursor[tableCursor];
            int col = colNumbers[tableCursor];
            for(; row<thisTable.size();){
                String compareV = thisTable.get(row).get(col).toString();//0 is the column number when join the first tag
                if(compareV.compareTo(biggestValue) < 0) row++;
                else break;
            }
            rowCursor[tableCursor] = row;
        }
        return rowCursor;
    }

    //make comparision of a list of values, and find the biggest value among them
    public int makeComparison_FBV(List<String> values){
        String bigValue = values.get(0);
        int bigValueCursor = 0;
        Boolean equals = true;
        for(int i=1;i<values.size();i++){
            String currentValue = values.get(i);
            int compare = bigValue.compareTo(currentValue);
            if(compare < 0){
                bigValue = values.get(i);
                bigValueCursor = i;
            }
            if(compare != 0){
                equals = false;
            }
        }
        if(equals) return -1;

        return bigValueCursor;
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
                    v1.addAll(Arrays.asList(v.get(colNo), ((List<List<int[]>>)v.get(colNo+1)).get(queryN)));
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

    //only when this table has value that equals to baseValue, the startEqualRow[0] != startEqualRow[1]
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

    public int moveCursorUntilNextNew(List<Vector> table, int rowNo, int colNo, String thisValue){
        int row=rowNo;
        for(; row<table.size();){
            Vector thisRow = table.get(row);
            String compareValue =  thisRow.get(colNo).toString();
            if(thisValue.equals(compareValue)){
                row++;
            }
            else break;
        }
        return row;
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
        @Override
        public int compare(Vector l1, Vector l2){
            int result = 0;
            int length = l1.size();
            for(int i=0; i<l1.size();i = i+2){
                int compa = (l1.get(i).toString()).compareTo(l2.get(i).toString());
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
    public void readRDB(String rdb_tables_dir, List<String> orderList) throws Exception{
        long startTime = System.currentTimeMillis();
        File directory = new File(rdb_tables_dir);
        for(File f: directory.listFiles()){
            String line = "";
            Boolean firstLine = true;
            List<Vector> rdb = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                while ((line = br.readLine()) != null) {
                    Vector vec = new Vector();
                    if(firstLine){
                        //read first line to tablesTags
                        vec.addAll(Arrays.asList(line.split("\\s*,\\s*")));
                        firstLine = false;
                        int orderLength = orderList.size();
                        myTablesTagList.add(getTagLocation(orderList, vec));

                    }
                    else{
                        String[] values = line.split("\\s*,\\s*");
                        for(String s:values){
                            vec.add(s);
                            vec.add(null);
                        }
                        rdb.add(vec);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            long sortStartTime = System.currentTimeMillis();
            Collections.sort(rdb, new MyComparator());
            long sortEndTime = System.currentTimeMillis();
            sortTotalTime += sortEndTime - sortStartTime;
            myTables.add(rdb);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("read rdb time:"+(endTime-startTime));
    }


    public int[] getTagLocation(List<String> orderList, Vector vec){
        int orderLength = orderList.size();
        int[] tagFlag = new int[orderLength];
        for(int orderCusor=0; orderCusor<orderLength; orderCusor++){
            String tag = orderList.get(orderCusor);
            if(vec.contains(tag)){
                tagFlag[orderCusor] = 1;
            }
            else tagFlag[orderCusor] = 0;
        }
        return tagFlag;
    }

    public void getPCTables(List<String> tagList,String xml_query_file,String xml_document_file) throws Exception{
        //Analysis queries to get pc relations
        File queryFolder = new File(xml_query_file);
        File basicDocumnetFolder = new File(xml_document_file);

        generateValueIdPair generate = new generateValueIdPair();
        //read query file
        File[] listOfFiles_query = queryFolder.listFiles();
        File[] listOfFiles_document = basicDocumnetFolder.listFiles();

        queryNo = listOfFiles_query.length;
        unitJump = queryNo + 1;
        if( queryNo != listOfFiles_document.length){
            System.out.println("please check query and basic document files. Their sizes are not same.");
            System.exit(0) ;
        }
        //to store all pc tables from different queries
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
            myTables.addAll(matchPC(tagList, tagMaps,myTable,i));
            long endTime = System.currentTimeMillis();

            System.out.println("The "+ i +" query generate p-c xml tables time:"+(endTime-startTime));
            myTable = new ArrayList<>();
            allTags.clear();
        }
    }

    public List<List<Vector>> matchPC(List<String> tagList, HashMap<String, List<Vector>> tagMaps, List<List<Vector>> myTables, int queryNo){
        List<List<Vector>> pcTables = new ArrayList<>();
        for(int tableCursor=0; tableCursor<myTables.size(); tableCursor++){
            List<Vector> pcTable = new ArrayList<>();
            Vector pc = myTables.get(tableCursor).get(0);
            myTablesTagList.add(getTagLocation(tagList, pc));//first row indicates tag names.
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
            long sortStartTime = System.currentTimeMillis();
            Collections.sort(pcTable, new MyComparator());
            long sortEndTime = System.currentTimeMillis();
            System.out.println("sort this table Time:"+(sortEndTime - sortStartTime));
            sortTotalTime += sortEndTime - sortStartTime;
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
        long startRunTime = System.currentTimeMillis();
        queryAnalysis_multimulti qbm = new queryAnalysis_multimulti();

        String xml_query_file = "xjoin/src/multi_rdbs/queries/";
        String xml_document_file = "xjoin/src/multi_rdbs/invoices/";
        String rdb_document_file = "xjoin/src/multi_rdbs/testTables";

//        String xml_query_file1 = "xjoin/src/multi_rdbs/queries/query1";
//        String xml_document_file1 = "xjoin/src/multi_rdbs/invoices/invoices1";
//        List<String,String>  = [[xml_query_file1,xml_query_file1],[q2,d2]]



//        List<String> joinOrderList = Arrays.asList("Invoice","OrderId","Orderline","asin","price","productId");
//        List<String> joinOrderList = Arrays.asList("a","b","c","d","e","f");
        List<String> joinOrderList = Arrays.asList("item","location","mail","from","date","quantity");

        qbm.getSolution(joinOrderList,xml_query_file,xml_document_file,rdb_document_file);
        long endRunTime = System.currentTimeMillis();
        System.out.println("total run time is:"+(endRunTime-startRunTime));

    }
}