package tjFast;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;
import produce.generateValueIdPair;


/**
 * Created by zzzhou on 2017-11-13.
 */
public class queryAnaylsis_bothMulti  extends DefaultHandler {
    Hashtable twigTagNames;

    String ROOT;

    Stack TagStack;
    static long totalSortTableTime = 0L;

    static String basicDocuemnt;
    static List<List<Vector>> myTables = new ArrayList<>();
    static List<Vector> tjFastTable = new ArrayList<>();
    static Set<String> xmlRelationTagSet = new HashSet<>();
    static List<String> allTags = new ArrayList<>();
    static List<Vector> result = new ArrayList<>();



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
        xmlReader.setContentHandler(new queryAnaylsis_bothMulti());

        // Set an ErrorHandler before parsing
        xmlReader.setErrorHandler(new MyErrorHandler(System.err));

        // Tell the XMLReader to parse the XML document
        xmlReader.parse(queryFile);
    }

    public void do_tjFast(){
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

                loadendTime = System.currentTimeMillis();
                //System.out.println("load data time is " + (loadendTime - loadbeginTime));
                totalLoadTime += loadendTime - loadbeginTime;


                joinbeginTime = System.currentTimeMillis();

                TwigSet join = new TwigSet(DTDInfor, alldata[1], alldata[0]);

                solutionCount += join.beginJoin();

                joinendTime = System.currentTimeMillis();

                totalJoinTime += joinendTime - joinbeginTime;

            }
            long tjFastEndTime = System.currentTimeMillis();
            long totalendTime = System.currentTimeMillis();
            System.out.println("solutionCount:"+solutionCount);

            System.out.println("Total tjFast load data time is " + totalLoadTime);

            System.out.println("Total tjFast join data time is " + totalJoinTime);

            System.out.println("Total running time is " + (totalendTime - totalbeginTime));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void getSolution() throws Exception{

        //Analysis queries to get pc relations
        File queryFolder = new File("xjoin/src/multi_rdbs/queries/");
        File basicDocumnetFolder = new File("xjoin/src/multi_rdbs/invoices/");

        //add-order
        List<String> tagList = Arrays.asList("a","b","c","d","e");
        generateValueIdPair generate = new generateValueIdPair();
        //read query file
        File[] listOfFiles_query = queryFolder.listFiles();
        File[] listOfFiles_document = basicDocumnetFolder.listFiles();

        if( listOfFiles_query.length != listOfFiles_document.length){
            System.out.println("please check query and basic document files. Their sizes are not same.");
            return;
        }
        for (int i=0; i<listOfFiles_query.length; i++) {
            File queryFile = listOfFiles_query[i];
            File documentFile = listOfFiles_document[i];
            basicDocuemnt = documentFile.getPath();
            List<String> currentTagList = new ArrayList<>();
            if (queryFile.isFile() && documentFile.isFile()) {
                //analysis query
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
                            myTables.add(l);
                        }
                    }
                }

                //analysis basic document
                HashMap<String, List<Vector>> tagMaps = generate.generateTagVId(currentTagList,basicDocuemnt);

                myTables = getPCTables(tagMaps,myTables);

                //read RDB files, add rdb_tables to myTables.
                if(i==0){
                    System.out.println("read RDB");
                    readRDB();
                }

                //join tables
                joinTables(tagList,myTables);

                //Verify query structure, multi-tjFast
                allTags.clear();
                myTables.clear();
                xmlRelationTagSet.clear();
            }
        }

    }

    public void joinTables(List<String> tagList, List<List<Vector>> AllTables) {
        List<Vector> result = new ArrayList<>();
        //join order
        for(int joinOrder=0; joinOrder<tagList.size(); joinOrder++){
            List<List<Vector>> tables = AllTables;
            String addTag = tagList.get(joinOrder);
            System.out.println("add tag:"+addTag);

            //join add_tag with join_result
            List<List<String>> tagCombs = getJoinedTagComb(tagList,joinOrder+1);
            //check joined tags combinations one by one
            for(List<String> tagComb:tagCombs) {
                List<List<Vector>> tablesToMerge = new ArrayList<>();
                List<List<Integer>> tableColumns = new ArrayList<>();
                for (int tableCursor = 0; tableCursor < tables.size(); tableCursor++) {
                    List<Vector> thisTable = tables.get(tableCursor);
                    Vector tableTag = thisTable.get(0);
                    if (tableTag.containsAll(tagComb)) {
                        List<Integer> tableColumn = new ArrayList<>();
                        //find common tags column number
                        for (String tag : tagComb) {
                            int table_column = getColumn(tableTag, tag);
                            tableColumn.add(table_column);
                        }
                        //remove this table from tables
                        tables.remove(tableCursor);
                        tableCursor--;
                        List<Vector> table_removeFirstRow = thisTable.subList(1, thisTable.size());
                        //sort current table according to column order
                        Collections.sort(table_removeFirstRow, new MyComparator(tableColumn));
                        //add table
                        tablesToMerge.add(table_removeFirstRow);
                        //add column number to list
                        tableColumns.add(tableColumn);
                    }
                }
                //if tablesToMerge has table contains this tag combination to join with Result
                if (!tablesToMerge.isEmpty()) {
                    if (result.isEmpty() || true) {
                        //if result is empty or has no added-tag to join with tables
                        //the tables joins by them selves
                        joinTable(tagComb.size(), tableColumns, tablesToMerge);
                    }
                    //else join with result
                    else{
                        //sort result table
                        //first, find column Nos in result table
                        List<Integer> resultColumn = new ArrayList<>();
                        for (int i = 0; i < tagList.size(); i++) {
                            if (tagComb.contains(tagList.get(i))) {
                                resultColumn.add(i);
                            }
                        }
                        //sort result table
                        Collections.sort(result, new MyComparator(resultColumn));

                    }
                }
                //else go to next loop
            }

        }
    }

    public List<Vector> joinTable(int tagCombSize, List<List<Integer>> tableColumns, List<List<Vector>> tablesToMerge){
        Boolean notEnd = true;
        int tableNos = tableColumns.size();
//        int tagCombCursor = 0;
        int[] rowCursor = new int[tableNos];
        while(notEnd){
            //any one of the tables has gone to the end
            if(isEnd(tablesToMerge,rowCursor)){
                break;
            }
            List<String> tagValues = new ArrayList<>();
            int[] rowCursor_before = rowCursor.clone();
            //read tables current row value, and move row cursor until next value is different with current.
            for(int tableCursor = 0; tableCursor < tableNos; tableCursor++){
                List<Vector> thisTable = tablesToMerge.get(tableCursor);
                int rowNo = rowCursor[tableCursor];
                int colNo = tableColumns.get(tableCursor).get(0); // tagCombCursor: 0
                String thisValue = thisTable.get(rowNo).get(colNo).toString();
                tagValues.add(thisValue);
                //update row cursor
                rowCursor[tableCursor]  = moveCursorUntilNextNew(thisTable, rowNo, colNo, thisValue);
            }
            //compare values
            int compareResult = makeComparision(tagValues);
            //if equals
            if(compareResult == -1){
                //load this part table rows to compare id and further tag value
                List<List<Vector>> rdbTables = new ArrayList<>();
                List<List<Vector>> joinIdTables = new ArrayList<>();
                List<Integer> idColumns = new ArrayList<>();
                for(int tableCursor = 0; tableCursor < tableNos; tableCursor++){
                    List<Vector> thisTable = tablesToMerge.get(tableCursor);
                    if(thisTable.get(0).get(1) != null){
                        joinIdTables.add(thisTable.subList(rowCursor_before[tableCursor],rowCursor[tableCursor]));
                        idColumns.add(tableColumns.get(tableCursor).get(0)+1);//plus 1 is id column number, tagCombCursor: 0
                    }
                    else{
                        rdbTables.add(thisTable.subList(rowCursor_before[tableCursor],rowCursor[tableCursor]));
                    }
                }
                //join ids
                if(joinIdTables.size() > 1){
                    //pruned xml tables(contain id)
                    List<List<Vector>> partTables = getIdCommonRows(joinIdTables, idColumns);
                    //check if tagComb goes to the end
                    if(tagCombSize == 0){
                        result = partTables.get(partTables.size()-1);
                    }
                    //add rdbTables for next join
                    List<Vector> resultTable = partTables.get(partTables.size()-1);
                    //@@@@@@calculate remove time
                    partTables.remove(partTables.size()-1);
                    partTables.addAll(rdbTables);
                    partTables.add(resultTable);
                    //remove first column number for tableColumns
                    //@@@@@@calculate remove time
                    for(int i=0; i<tableColumns.size(); i++){
                        tableColumns.get(i).remove(0);
                    }
                    joinTable(tagCombSize-1, tableColumns, partTables);

                }
                else if(joinIdTables.size() == 0){
                    System.out.println("Has no solution! Message from 'queryAnalysis_bothMulti'. ");
                    return null;
                }

            }
            //else move the table which value is minimal
            else rowCursor[compareResult] = rowCursor[compareResult]+1;
        }
        return result;
    }

    public List<List<Vector>> getIdCommonRows(List<List<Vector>> tables, List<Integer> idColumns){
//        List<int[]> commonTagIdList = new ArrayList<>();
        Boolean notEnd = true;
        int tableNo = tables.size();
        List<List<Vector>> partTables = new ArrayList<>();
        for(int i=0;i<tableNo; i++){
            partTables.add(new ArrayList<>());
        }
        int[] rowCursor = new int[tableNo];
//        List<int[]> commonIds = new ArrayList<>();
        while(notEnd){
            if(isEnd(tables, rowCursor)) break;
            List<int[]> ids = new ArrayList<>();
            for(int tableCursor=0; tableCursor<tableNo; tableCursor++){
                ids.add((int[])tables.get(tableCursor).get(rowCursor[tableCursor]).get(idColumns.get(tableCursor)));
            }
            int compa = compareIds(ids);
            //compa-> -1 means the ids are equal. other values means to move corresponding cursor.
            if(compa==-1){
//                commonIds.add(ids.get(0));
//                commonTagIdList.add(ids.get(0));
                //since id will not duplicate in the same list, so move each idList to the next row
                for(int i=0; i<tableNo; i++){
                    partTables.get(i).add(tables.get(i).get(rowCursor[i]));
                    rowCursor[i] += 1;
                }
            }
            else rowCursor[compa] += 1;
        }

        return partTables;
    }

    /**
     * Compare ids in a list
     * @param ids
     * @return compare result
     */
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

    /**move rowNo until the next value is not the same, also add the same values' id to a list and return it.
     *
     * @param table, the table to move row cursor
     * @param rowNo, initial row number, before move
     * @param columnNo, compare-value columnNo in table
     * @param thisValue, initial rowNo table value, as basic value
     * @return Vector, Vector[0]: after move row number.
     *                  Vector[1]: moved-rows of table, use to compare id/ next value with other tables
     */
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

    /**Compare a list of strings
     *
     * @param values
     * @return table cursor, which value is the smallest one and needs to go to next row. if the values are all the same, return -1
     */

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
                        //do not need to reserve place to store other queries' id_list, since structure will be checked right after analysis each query.
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


    static public void main(String[] args) throws Exception {
        queryAnaylsis_bothMulti qbm = new queryAnaylsis_bothMulti();
        qbm.getSolution();
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
}
