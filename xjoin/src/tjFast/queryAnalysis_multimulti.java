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
    static List<List<List<Vector>>> myTables = new ArrayList<>();
    static int queryNo;

    public void getSolution() throws Exception{
        //add-order
        List<String> joinOrderList = Arrays.asList("a","b","c","d","e","f");
        //get p-c relation table list
        myTables = getPCTables(joinOrderList);
        //read rdb tables
        readRDB();
        //join tables
        joinTablesByOrder(joinOrderList);
    }

    public void joinTablesByOrder(List<String> joinOrderList){
        for(int joinOrder=0; joinOrder<joinOrderList.size(); joinOrder++){
            List<List<Integer>> tables = new ArrayList<>();
            //the tag that is going to be added to Result
            String addTag = joinOrderList.get(joinOrder);
            System.out.println("add tag:"+addTag);

            //join add_tag with join_result
            List<List<String>> tagCombs = getJoinedTagComb(joinOrderList,joinOrder+1);
            //check joined tags combinations one by one
            for(List<String> tagComb:tagCombs) {
                List<List<Vector>> tablesToMerge = new ArrayList<>();
                List<List<Integer>> tableColumns = new ArrayList<>();
                for(int setOrder=0; setOrder<myTables.size(); setOrder++){
                    List<List<Vector>> thisSet = myTables.get(setOrder);
                    for(int tableCursor=0; tableCursor<thisSet.size(); tableCursor++){
                        List<Vector> thisTable = thisSet.get(tableCursor);
                        Vector tableTag = thisTable.get(0);
                        if (tableTag.containsAll(tagComb)) {
                            List<Integer> tableColumn = new ArrayList<>();
                            //find common tags column number
                            for (String tag : tagComb) {
                                int table_column = getColumn(tableTag, tag);
                                tableColumn.add(table_column);
                            }

                            //how to remove the already joined table???
                            //...

                            List<Vector> table_removeFirstRow = thisTable.subList(1, thisTable.size());
                            //sort current table according to column order
                            Collections.sort(table_removeFirstRow, new MyComparator(tableColumn));
                            //add table
                            tablesToMerge.add(table_removeFirstRow);
                            //add column number to list
                            tableColumns.add(tableColumn);
                        }
                    }
                }
                System.out.println("check point");
                //here all the tables that contain the join-tag combinations have been added to tablesToMerge
                //their column numbers has been added to tableColumns(List<List<Integer>>)
                //start to join these tables
                gotoJoinTables(joinOrder, tagComb, joinOrderList, tablesToMerge, tableColumns);

            }
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

    public void gotoJoinTables(int joinOrder, List<String> tagComb, List<String> joinOrderList,List<List<Vector>> tablesToMerge, List<List<Integer>> tableColumns){
        //case 1: result has no common tag to join with tables
        List<Integer> resultColumn = new ArrayList<>();
        if(result.isEmpty()){
            //pick up the last table and let it be the baseTable
            int lastTableIndex = tablesToMerge.size()-1;
            //table
            List<Vector> baseTable = tablesToMerge.get(lastTableIndex);
            tablesToMerge.remove(lastTableIndex);
            //column
            List<Integer> baseTableColumns = tableColumns.get(lastTableIndex);
            tableColumns.remove(lastTableIndex);
            joinTable(0,baseTable,baseTableColumns, tablesToMerge, tableColumns);

        }
        //case 2: result has add-tag to join
        else if(result.get(0).size() < (joinOrder+1)*2){
            //find tag columns in result
            List<String> tagComb_sub = tagComb.subList(0,tagComb.size()-1);
            for (int i = 0; i < joinOrderList.size(); i++) {
                if (tagComb_sub.contains(joinOrderList.get(i))) {
                    resultColumn.add(i*queryNo);
                }
            }
            //sort result table
            Collections.sort(result, new MyComparator(resultColumn));
            //join result, and tables
            joinTable(0, result, resultColumn, tablesToMerge, tableColumns);
        }
        //case 3: result does not have add-tag yet
        else{
            //find tag columns in result
            for (int i = 0; i < joinOrderList.size(); i++) {
                if (tagComb.contains(joinOrderList.get(i))) {
                    resultColumn.add(i*queryNo);
                }
            }
            //sort result table
            Collections.sort(result, new MyComparator(resultColumn));
            //join result, and tables
            joinTable(0, result, resultColumn, tablesToMerge, tableColumns);
        }
    }

    Vector transRoom = new Vector();
    public void joinTable(int tagCombCursor, List<Vector> baseTable, List<Integer> baseTableColumns, List<List<Vector>> tablesToMerge, List<List<Integer>> tableColumns){
        Boolean noResult;
        //recursion by tagCombCursor
        if(tableColumns.get(0).size() != 0) {
            //match same value
            Boolean notEnd = true;
            int tableNos = tableColumns.size();
            int[] rowCursor = new int[tableNos];
            int baseTableRow = 0;
            int baseTableColumn = baseTableColumns.get(0);
            while (notEnd) {
                //result or any one of the tables has gone to the end
                if (baseTableRow == baseTable.size() || isEnd(tablesToMerge, rowCursor)) {
                    break;
                }
                noResult = false;
                //move result until value is not same
                String resultValue = baseTable.get(baseTableRow).get(baseTableColumn).toString();
                //update result row number
                int[] baseTableRowUpdate = moveCursorUntilNoEqual(baseTable, baseTableRow, baseTableColumn, resultValue);
                baseTableRow = baseTableRowUpdate[1];
                //read and move tables in tablesToMerge until each next value new
                List<List<Vector>> subTables = new ArrayList<>();
                for(int tableCursor = 0; tableCursor < tableNos; tableCursor++) {
                    List<Vector> thisTable = tablesToMerge.get(tableCursor);
                    int rowNo = rowCursor[tableCursor];
                    int colNo = tableColumns.get(tableCursor).get(0); // tagCombCursor: 0
                    String thisValue = thisTable.get(rowNo).get(colNo).toString();
                    //update row cursor
                    int[] rowUpdate  = moveCursorUntilNoEqual(thisTable, rowNo, colNo, resultValue);

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
                    List<List<List<Vector>>> querySubTables = new ArrayList<>();
                    List<List<Integer>> querySubTableColumns = new ArrayList<>();
                    //common value is result value
                    //match same id
                    //divide subTables according to their origin queries
//                    for(int queryCursor=0; queryCursor<queryNo; queryCursor++){
//                        List<List<Vector>> querySubTable = new ArrayList<>();
//                        List<Integer> querySubTableColumn = new ArrayList<>();
//                        for(int subTableCursor=0; subTableCursor<subTables.size(); subTableCursor++){
//                            int queryMark = (int)subTables.get(subTableCursor).get(0).get(2);//[value, id, queryMark]
//                            if(queryMark == queryCursor) {
//                                querySubTable.add(subTables.get(subTableCursor));
//                                //not sure if this can get the correct column number
//                                querySubTableColumn.add(tableColumns.get(subTableCursor).get(0)+1);
//                            }
//                        }
//                        querySubTables.add(querySubTable);
//                        querySubTableColumns.add(querySubTableColumn);
//                    }
                    //following solution we have to nest loop, but by using hashMap we only need to loop all tables once
                    HashMap<Integer, List<Vector>> hashMap = new HashMap<>();
                    for(int subTableCursor=0; subTableCursor<subTables.size(); subTableCursor++){
                        List<Vector> thisSubTable = subTables.get(subTableCursor);
                        int queryMark = (int)thisSubTable.get(0).get(2);//[value, id, queryMark]
                        hashMap.put(queryMark, thisSubTable);
                    }

                    //compare id one by one and result ids
                    Vector solutionRow = new Vector();
                    for(int queryCursor=0; queryCursor<queryNo; queryCursor++){
                        List<List<Vector>> thisQuerySubTables = querySubTables.get(queryCursor);
                        List<Integer> thisQuerySubTableColumns = querySubTableColumns.get(queryCursor);
                        List<int[]> idLists = getValueCommonIds(thisQuerySubTables, thisQuerySubTableColumns);
                        if(! idLists.isEmpty()){
                            if(queryCursor == 0){
                                //add value and id
                                solutionRow.add(resultValue);
                            }
                            solutionRow.add(idLists);
                        }

                    }

                }
            }


        }
        else{
            //return the result
        }
    }

    public List<int[]> getValueCommonIds(List<List<Vector>> tables, List<Integer> columnNos){
        Boolean notEnd = true;
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
                    rowCursor[i] += 1;
                }
                id_List.add(ids.get(0));
            }
            else rowCursor[compa] += 1;
        }
        return id_List;
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
        List<List<String>> joinedTagComb = new ArrayList<>();

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
            joinedTagComb.add(combs);
        }
        joinedTagComb.sort(Comparator.comparing(List<String>::size).reversed());
        return joinedTagComb;
    }

    //read RDB value and merge list to myTables.
    public void readRDB() throws Exception{
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

            myTable = matchPC(tagMaps,myTable,i);

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
    public void endDocument() throws SAXException {

    }

    static public void main(String[] args) throws Exception {
        queryAnalysis_multimulti qbm = new queryAnalysis_multimulti();
        qbm.getSolution();
    }
}
