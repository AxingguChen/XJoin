package tjFast;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import produce.generateValueIdPair;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;

/**
 * Created by zzzhou on 2017-09-22.
 */
public class queryAnalysis_multi extends DefaultHandler{

    Hashtable twigTagNames;

    static String filename;

    String ROOT;

    Stack TagStack;

    static String basicDocuemnt;
    static List<List<Vector>> myTables = new ArrayList<>();
    static int PCCount = 0;

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
    public void endDocument() throws SAXException {
        System.out.println(myTables);

        //do tjFast here

    }
    //end document

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

    public void getSolution() throws Exception{
        System.out.println("getSolution:"+ myTables);
        generateValueIdPair generate = new generateValueIdPair();
        //divide p-c relation in xml to RDBs.
        myTables = generate.generatePCVId(myTables);
        System.out.println(myTables);
        //PCCount--the count of pc relations, will be use to divide pc_table and rdb_table in myTables
        PCCount = myTables.size();
        //read RDB files, add rdb_tables to myTables.
        readRDB();
        System.out.println(myTables);
        //Merge all tables by given merge order
        mergeTable(Arrays.asList("a","b","c","d","e"));

    }

    public List<List<String>> findAllCombination(List<String> myList,String newIn){
        final int maxbit = 1 << myList.size();
        List<List<String>> mergeLists = new ArrayList<>();
        //for each combination given by a (binary) number 'p'...
        for (int p = 0; p < maxbit; p++) {
            final List<String> res = new ArrayList<String>();

            //evaluate if array 'a' element at index 'i' is present in combination (and include it if so)
            for (int i = 0; i < myList.size(); i++) {
                if ((1 << i & p) > 0) {
                    res.add(myList.get(i));
                }
            }
            res.add(newIn);
            mergeLists.add(res);
        }
        return mergeLists;
    }

    public int getColumn(Vector v, String tag){
        for(int i=0;i<v.size();i++){
            if(v.get(i).toString().equals(tag)){
                return i;
            }
        }
        return -1;
    }

    //compare by column numbers
    public class MyComparator implements Comparator<Vector> {
        List<Integer> columnNos;
        public MyComparator(List<Integer> columnNos) {
            this.columnNos = columnNos;
        }
        @Override
        public int compare(Vector l1, Vector l2){
            int result = 0;
            for(int i=0; i<columnNos.size(); i++){

                int compa = (l1.get(columnNos.get(i)*2).toString()).compareTo(l2.get(columnNos.get(i)*2).toString());
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

//    public void mergeTable2(List<String> mergeOrder) throws Exception{
//        //merge order[A,B,C,D,E]
//        for(int order=0;order<mergeOrder.size();order++){
//            //orderLists--all relations that need to be fulfilled
//            List<List<String>> orderLists =findAllCombination(mergeOrder.subList(0,order),mergeOrder.get(order));
//            //for every relation, eg, [a,b]
//            for(List<String> checkTags:orderLists){
//                int tableCount = 0;
//                int pcTable_toBeMergeCount = 0;
////                List<Vector> mergeTables = new ArrayList<>();
//                Vector tableTags_ToMerge = new Vector();
//                //for every table
//                for(List<Vector> table: myTables){
//                    tableCount++;
//                    //the first row of each table, contains the tags of this table
//                    Vector tags_vector = table.get(0);
//                    //if the table contains tags/relations need to be fulfilled
//                    if(tags_vector.containsAll(checkTags)){
////                        List<Integer> columnNos = new ArrayList<>();
//                        List<Vector> tableCheckTags = new ArrayList<>();
//                        for(String tag:checkTags){
//                            int table_column = getColumn(tags_vector,tag);
//
//                            //add table columns to list so that we can allocate the corresponding tags in each table
//                            //columnNos.add(table_column);
//                            tableCheckTags.add(table.get(table_column));
//                            //if this table is pc_table[p_v,c_id,c_v]
//                            if(tableCount<=PCCount && table_column ==1){
//                                //if table_column=1 -> c_id, which actually refers to c_v. plus 1 -> c_v
//                                table_column++;
//                                //add p_c relation table's id list to checkTags
//                                tableCheckTags.add(table.get(table_column));
//                            }
//                            //else it is a rdb_table[tag1_v, tag2_v, ...], nothing needs to be done with column number.
//                        }
//                        //sort table according to corresponding table column one by one
////                        Collections.sort(table,new MyComparator(columnNos));
//                        Collections.sort(tableCheckTags,new Comparator<List<Vector>>(){
//                            public int compare(List<Vector> l1, List<Vector> l2){
//                                int length = l1.size();
//                                int result = 0;
//                                for(int i=0; i<length; i++){
//                                    int compa = (l1.get(i).get(0).toString()).compareTo(l2.get(i).get(0).toString());
//                                    if(compa < 0){
//                                        result = -1;
//                                        break;
//                                    }
//                                    else if(compa == 0)
//                                        result = 0;
//                                    else {result = 1;break;}
//                                }
//                                return result;
//                            }}
//                        );
//
//                        tableTags_ToMerge.add(tableCheckTags);
////                        tableAndColumn.add(columnNos);
////                        mergeTables.add(tableAndColumn);
//                    }
//                }
//
//                //MergeTables: now we have the list of tables and their column numbers which contains the checkTags
//                //Now let us merge these tables
//                if(! tableTags_ToMerge.isEmpty()){
//                    for(int i=0;i<checkTags.size();i++){
//
//                    }
//                }
//                //if it is first table which has nothing to join(result list is null), add to result list
//
//                //else join current tag with result list tags
//            }
//
//        }
//    }

    public void mergeTable(List<String> mergeOrder) throws Exception{
        List<List<Vector>> finalResult = new ArrayList<>();
        //merge order[A,B,C,D,E]
        for(int order=0;order<mergeOrder.size();order++){

            //orderLists--all relations that need to be fulfilled
            List<List<String>> orderLists =findAllCombination(mergeOrder.subList(0,order),mergeOrder.get(order));
            //for every relation, eg, [a,b]
            for(List<String> checkTags:orderLists){
                //mergeResult: [a, a_id, b, b_id,...]
                List<Vector> mergeResult = new ArrayList<>();
                int tableCount = 0;
//                List<Vector> mergeTables = new ArrayList<>();
//                Vector tableTags_ToMerge = new Vector();
                List<List<Integer>> tableColumns = new ArrayList<>();
                List<List<Vector>> tablesToMerge = new ArrayList<>();
                //for every table
                for(List<Vector> table: myTables){
                    tableCount++;
                    //the first row of each table, contains the tags of this table
                    Vector tags_vector = table.get(0);
                    //if the table contains tags/relations need to be fulfilled
                    if(tags_vector.containsAll(checkTags)){
                        List<Integer> columnNos = new ArrayList<>();
//                        List<Vector> tableCheckTags = new ArrayList<>();
                        for(String tag:checkTags){
                            int table_column = getColumn(tags_vector,tag);

                            //add table columns to list so that we can allocate the corresponding tags in each table
                            columnNos.add(table_column);
//                            tableCheckTags.add(table.get(table_column));
                        }
                        tableColumns.add(columnNos);
                        List<Vector> tr = table.subList(1,table.size());
                        //remove duplicate
//                        @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@??????????????


                        Collections.sort(tr,new MyComparator(columnNos));
                        tablesToMerge.add(tr);
                    }
                }

                //MergeTables: now we have the list of tables and their column numbers which contains the checkTags
                //Now let us merge these tables
                //if only one table, add this table's column tag to intermediate result
                if(tablesToMerge.size() == 1){

                    List<Vector> table = tablesToMerge.get(0);
                    List<Integer> tableColumn = tableColumns.get(0);
                    //add tag name to the first row of merge Result
                    mergeResult.add(new Vector<>(checkTags));
                    for(int row=0; row<table.size(); row++){
                        Vector v = new Vector();
                        for(int column=0; column<tableColumn.size(); column++){
                            v.add(table.get(row).get(tableColumn.get(column)*2));
                            v.add(table.get(row).get(tableColumn.get(column)*2+1));
                        }
                        mergeResult.add(v);
                    }
                }

                //if at least two
                else if(tablesToMerge.size()>1){
                    //add tag name to the first row of merge Result
                    mergeResult.add(new Vector<>(checkTags));

                    if(! tablesToMerge.isEmpty()){
                        int[] rowCursor = new int[tablesToMerge.size()];
                        Boolean notEnd = true;
                        while(notEnd){
                            List<String> tagValues = new ArrayList<>();
                            for(int tableCursor = 0; tableCursor < tablesToMerge.size(); tableCursor++){
                                tagValues.add(tablesToMerge.get(tableCursor).get(rowCursor[tableCursor]).get(tableColumns.get(tableCursor).get(0)*2).toString());
                            }
                            int compareResult = makeComparision(tagValues);
                            //if the first column values equal, need to check if all other values are the same
                            if(compareResult == -1){
                                //if only one tag need to be compare, add current row to result list
                                if(tableColumns.get(0).size() == 1){
                                    Vector v = new Vector();
                                    //add value
                                    v.add(tagValues.get(0));
                                    //add IDs
                                    List<int[]> ids = new ArrayList<>();
                                    for(int tCursor=0; tCursor<tablesToMerge.size(); tCursor++){
//                                        int[] id = (int[])tablesToMerge.get(tCursor).get(rowCursor[tCursor]).get(1);
                                        int[] id = (int[])tablesToMerge.get(tCursor).get(rowCursor[tCursor]).get(tableColumns.get(tCursor).get(0)*2+1);
                                        //add id
                                        if( id != null && (! ids.contains(id))) ids.add(id);
                                    }
                                    //add the value and id list to result list
                                    Vector row = new Vector();
                                    row.add(tagValues.get(0));//add value
                                    row.add(ids);//add value's id list
                                    mergeResult.add(row);
                                    //move to the next row to compare
                                    if(isEnd(tablesToMerge,rowCursor)){
                                        break;
                                    }
                                    else rowCursor = nextMove(tablesToMerge, rowCursor,tableColumns);

                                }
                                //else compare other tags values of current row
                                else {
                                    //first table's values, use to compare with other tables
                                    List<String> baseValue = new ArrayList<>();
                                    //first table
                                    List<Vector> baseTable = tablesToMerge.get(0);
                                    for (int cursor = 1; cursor < tableColumns.get(0).size(); cursor++) {
                                        //baseTables.get(tableRow).get(tableColumn)
                                        baseValue.add(baseTable.get(rowCursor[0]).get(tableColumns.get(0).get(cursor)).toString());
                                    }
                                    //for each table
                                    Boolean allEqual = true;
                                    for (int tableCursor = 1; tableCursor < tablesToMerge.size(); tableCursor++) {
                                        //each tag/column, columnCursor smaller than the first tables column count
                                        for(int columnCursor = 1; columnCursor < tableColumns.get(tableCursor).size();columnCursor++){
                                            List<Vector> currentTable = tablesToMerge.get(tableCursor);
                                            String currentValue = currentTable.get(rowCursor[tableCursor]).get(tableColumns.get(tableCursor).get(columnCursor)).toString();
                                            //if currentValue is not equal to corresponding base value
                                            if(currentValue.compareTo(baseValue.get(columnCursor-1)) !=0){
                                                allEqual = false;
                                                break;
                                            }
                                        }
                                        if(!allEqual) break;
                                    }
                                    //if all values are the same, add this row to intermediate result. Several tags(value[can get from baseValue], id)
                                    //Since baseValue is used to compare if
                                    if(allEqual) {
                                        Vector v = new Vector();
                                        for(int i=0; i< baseValue.size(); i++){
                                            v.add(baseValue.get(i)); //i-th value in baseValue
//                                            v.add();//i-th id
                                            List<int[]> ids = new ArrayList<>();
                                            for(int tCursor=0; tCursor<tablesToMerge.size(); tCursor++){
                                                int[] id = (int[])tablesToMerge.get(tCursor).get(rowCursor[tCursor]).get(1);
                                                //add id
                                                if( id != null && (! ids.contains(id))) ids.add(id);
                                            }
                                            v.add(ids);
                                        }
                                        mergeResult.add(v);
//                                        next move
                                    }
//                                    else{
//                                        //Maybe miss correct result here, Need to modify later, cannot simply move the first table's row count
//    //                                            @@@@@@@@@@@@@@@@@@@@@@@!!!!!!!!!!!!!!!!!!!!!!
//                                        rowCursor[0] = rowCursor[0] +1;
//                                    }
                                    //move to the next row to compare
                                    if(isEnd(tablesToMerge,rowCursor)){
                                        break;
                                    }
                                    else rowCursor = nextMove(tablesToMerge, rowCursor,tableColumns);
                                }
                            }
                            //add one to the row cursor number of the smallest table, then make comparision
                            else{
                                rowCursor[compareResult] = rowCursor[compareResult]+1;
                                //any one of the tables has gone to the end
                                if(isEnd(tablesToMerge,rowCursor)){
                                    break;
                                }
                            }
                        }
                    }
                }
                finalResult.add(mergeResult);
                //join with final final result
            }
                //if it is first table which has nothing to join(result list is null), add to result list
            System.out.println("first done");
                //else join current tag with result list tags

        }

    }

    public int[] nextMove(List<List<Vector>> tablesToMerge, int[] rowCursor, List<List<Integer>> tableColumns){
        //1. read next row's value. 2. compare the values and get the min next value. 3. move min value row's cursor
        List<String> tempValues = new ArrayList<>();
        for(int tableCursor = 0; tableCursor < tablesToMerge.size(); tableCursor++){
            tempValues.add(tablesToMerge.get(tableCursor).get(rowCursor[tableCursor]+1).get(tableColumns.get(tableCursor).get(0)*2).toString());
        }
        int tempCompareResult = makeComparision(tempValues);
        if(tempCompareResult == -1){
            for(int i=0;i<rowCursor.length;i++){
                rowCursor[i]++;
            }

        }else{
            rowCursor[tempCompareResult] = rowCursor[tempCompareResult]+1;
        }
        return rowCursor;
    }
    //return true means is end.
    public boolean isEnd(List<List<Vector>> tablesLists, int[] rowCursor){
        for(int i=0;i<tablesLists.size();i++){
            // the last element of this table
            if(tablesLists.get(i).size() == rowCursor[i]+1){
                return true;
            }
        }
        return false;
    }

    public boolean isEqual(List<List<Vector>> tablesToMerge, int[] rowCursor){
//        List<String> baseValue =
        for(int tableNo=0; tableNo<tablesToMerge.size();tableNo++){

        }
        return true;
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

    //read RDB value and merge list to myTables.
    public void readRDB() throws Exception{
        File directory = new File("xjoin/src/multi_rbds");
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
            }
            myTables.add(rdb);
        }
    }

    static public void main(String[] args) throws Exception {
        //filename = args[0];
        filename = "xjoin/src/tjFast/simplePathPattern.xml";
        //basicDocuemnt = args[1];
        basicDocuemnt = "xjoin/src/test.xml";

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
        xmlReader.setContentHandler(new queryAnalysis_multi());

        // Set an ErrorHandler before parsing
        xmlReader.setErrorHandler(new MyErrorHandler(System.err));

        // Tell the XMLReader to parse the XML document
        xmlReader.parse(convertToFileURL(filename));

        queryAnalysis_multi qa_m = new queryAnalysis_multi();
        qa_m.getSolution();
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
}
