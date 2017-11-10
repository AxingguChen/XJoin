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
    static List<Vector> tjFastTable = new ArrayList<>();

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
//        System.out.println(myTables);
        //tjFastTable stores the result

        //do tjFast here
//        System.out.println("begin analysis query !");
//
//        Query.setTwigTagNames(twigTagNames);
//
//
//        Query.setRoot(ROOT);
//
//        utilities.DebugPrintln("Query root is " + Query.getRoot());
//
//        System.out.println("begin analysis document !");
//
//        try {
//            DTDTable DTDInfor = loadDataSet.produceDTDInformation(basicDocuemnt);
//
//            long totalbeginTime = System.currentTimeMillis();
//            long loadendTime = 0L;
//            long loadQueryEndTime = 0L;
//            long joinbeginTime = 0L;
//            long joinendTime = 0L;
//            long totalLoadTime = 0L;
//            long totalJoinTime = 0L;
//
//            Query.preComputing(DTDInfor);
//
//            loadDataSet d = new loadDataSet();
//            System.out.println("begin load data !");
//
//
//            List<Hashtable[]> ALLData = new ArrayList<Hashtable[]>();
//            labelMatching lm = new labelMatching();
//            List<String> tagList = new ArrayList<>();
//            for(int i=0;i< Query.getLeaves().size();i++){
//                tagList.add((String) Query.getLeaves().elementAt(i)); // get query leaves
//            }
//
//            int solutionCount = 0;
//
////            for(int i = 0;i<100;i++){
////                part0.add(o[0].get(i));
////                part1.add(o[1].get(i));
////            }
////            Vector partO[] = new Vector[2];
////            partO[0] = part0;
////            partO[1] = part1;
//            for(int i=0;i<tjFastTable.size();i++) {
//                long loadbeginTime = System.currentTimeMillis();
//                Hashtable[] alldata = d.loadAllLeafData(tjFastTable.get(i), DTDInfor,tagList);
//
//                //for double layer query only
////                alldata[0].put(tagList.get(2),partO[0]);
////                alldata[1].put(tagList.get(2), partO[1]);
////
////                alldata[0].put(tagList.get(2),o[0]);
////                alldata[1].put(tagList.get(2), o[1]);
//                //System.out.println("Query leaves:" + Query.getLeaves());
//
////                System.out.println("i:"+i);
//                loadendTime = System.currentTimeMillis();
//                //System.out.println("load data time is " + (loadendTime - loadbeginTime));
//                totalLoadTime += loadendTime - loadbeginTime;
//
//                //join
//                //System.out.println("begin join !");
//
//                joinbeginTime = System.currentTimeMillis();
//
//                TwigSet join = new TwigSet(DTDInfor, alldata[1], alldata[0]);
//
//                solutionCount += join.beginJoin();
////                System.out.println("solutionCount:"+solutionCount);
//                joinendTime = System.currentTimeMillis();
//                //System.out.println("join data time is " + (joinendTime - joinbeginTime));
//                totalJoinTime += joinendTime - joinbeginTime;
//                //tjFastbyAddTime = tjFastbyAddTime + joinendTime -loadbeginTime;
//            }
//            long tjFastEndTime = System.currentTimeMillis();
//            long totalendTime = System.currentTimeMillis();
//            System.out.println("solutionCount:"+solutionCount);
//            //System.out.println("Total tjFast time is " + (tjFastEndTime-tjFastbeginTime));
//            //System.out.println("Total tjFast by add time is " + tjFastbyAddTime);
//
//
//            System.out.println("Total tjFast load data time is " + totalLoadTime);
//
//            System.out.println("Total tjFast join data time is " + totalJoinTime);
//
//            System.out.println("Total running time is " + (totalendTime - totalbeginTime));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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
//        myTables = generate.generatePCVId(myTables);
        System.out.println(myTables);
        //PCCount--the count of pc relations, will be use to divide pc_table and rdb_table in myTables
        //PCCount = myTables.size();
        //read RDB files, add rdb_tables to myTables.
        readRDB();
        System.out.println(myTables);
        //Merge all tables by given merge order
        List<Vector> myResult = mergeTableV3(Arrays.asList("Invoices","OrderId","Orderline","asin","price"));
        tjFastTable =  myResult;
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
        Vector columnNos;
        public MyComparator(Vector columnNos) {
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



    public List<Vector> mergeTableV3(List<String> mergeOrder) throws Exception{
        List<Vector> myResult = new ArrayList<>();

        //add tag one by one to myResult
        for(int order=0; order<mergeOrder.size(); order++){
            String addTag = mergeOrder.get(order);
            HashMap<String, List<int[]>> addTagValueIdMap = new HashMap();
            List<List<Vector>> tablesToMerge = new ArrayList<>();
            List<Vector> tableColumns = new ArrayList<>();
            List<Vector> tableTags = new ArrayList<>();
            //add tables that contains current add-tag to a list(tablesToMerge)
            for(List<Vector> table:myTables){
                Vector tagVector = table.get(0);
                Vector columnNos = new Vector();
                if(tagVector.contains(addTag)){

                    int table_column = getColumn(tagVector,addTag);
                    columnNos.add(table_column*2);//since tagVector is [v,id,v,id...] except the first r
                    //@@@@@here tableColumns can be replaced by columnNos.
                    //add table columns to list so that we can allocate the corresponding tags in each table
                    tableColumns.add(columnNos);
                    //remove first row of table since the first row is the names of tags
                    List<Vector> table_removeFirstRow = table.subList(1,table.size());
                    //sort table by addTag
                    Collections.sort(table_removeFirstRow,new MyComparator(columnNos));
                    tablesToMerge.add(table_removeFirstRow);
                    tableTags.add(tagVector);
                }
            }

            if(!tablesToMerge.isEmpty()){
                if(tablesToMerge.size() > 1) {
                    //join table
                    Vector v = joinTable(tablesToMerge, tableColumns);
                    tablesToMerge = (List<List<Vector>>) v.get(0);
                    addTagValueIdMap = (HashMap<String, List<int[]>>) v.get(1);
                    //Here we have pruned tables in tablesToMerge
                    //if it is the first tag add to the result, no need to join with result
                    if (order == 0) {
                        //traverse tagValueIdMap, add all key,value to result.
                        for (Map.Entry<String, List<int[]>> entry : addTagValueIdMap.entrySet()) {
                            Vector resultRow = new Vector(Arrays.asList(entry.getKey(), entry.getValue()));
                            myResult.add(resultRow);
                        }
                        System.out.println("check");
                    }

                    //else, join with the value in result
                    //here only consider table has two columns. if more than 2 columns, need to modify the code, lets just get some result first.
                    else {
                        List<String> addedTags = mergeOrder.subList(0, order);
                        myResult = joinWithResultTable(order,myResult, tablesToMerge, tableTags, addedTags, tableColumns, addTagValueIdMap);
                    }
                }
                else{
                    //has only one table contains the tag, just need to join this table to result list
                    List<Vector> joinTable = tablesToMerge.get(0);
                    //does not have a tag to join with previous tag situation@@@
                    //only for 'DE'
                    // DE table has no duplicate value in this version
                    List<Vector> myNewResult = new ArrayList<>();
                    for(int i=0; i<joinTable.size(); i++){
                        String value = joinTable.get(i).get((int)tableColumns.get(0).get(0)).toString();
                        int[] id = (int[])joinTable.get(i).get((int)tableColumns.get(0).get(0)+1);
                        for(int j=0; j<myResult.size(); j++){
                            Vector v = (Vector)myResult.get(j).clone();

                            v.addAll(Arrays.asList(value, Arrays.asList(id)));
                            myNewResult.add(v);
                        }
                    }
                    myResult = myNewResult;

                }

                }
            //else add null value?
            else{
                System.out.println("now we comes to the else situation, do something!");
            }
        }
        return myResult;
    }

    public List<Vector> joinWithResultTable(int order, List<Vector> myResult, List<List<Vector>> tablesToMerge, List<Vector> tableTags, List<String> addedTags, List<Vector> tableColumns, HashMap<String, List<int[]>> vIdMap){
        //addedTags is a,b,c.. one by one
        int addedTgasSize = addedTags.size();
        for(int t=0; t<addedTgasSize; t++) {
            String tag = addedTags.get(t);
            List<List<Vector>> tablesToMerge_r = new ArrayList<>();
            List<Vector> tableColumns_r = new ArrayList<>();
            //if combine.size =1, means we need to sort all the tables by current tag, includes the result
            //else, we just need join all the tables that contains the tag.
            for (int tableCursor = 0; tableCursor < tableTags.size(); tableCursor++) {
                Vector tagVector = tableTags.get(tableCursor);
                Vector columnNos = new Vector();
                if (tagVector.contains(tag)) {
                    int table_column = getColumn(tagVector, tag);
                    columnNos.addAll(Arrays.asList(table_column * 2, tableColumns.get(tableCursor).get(0)));//since tagVector is [v,id,v,id...] except the first row
                    List<Vector> table = tablesToMerge.get(tableCursor);
                    Collections.sort(table, new MyComparator(columnNos));
                    tablesToMerge_r.add(table);
                    //add table columns to list so that we can allocate the corresponding tags in each table
                    tableColumns_r.add(columnNos);
                }
            }
            //if list is not empty, join it with current tag
            if (!tablesToMerge_r.isEmpty()) {
                //sort result by current tag
                Vector columnN = new Vector();
                columnN.add(t*2);
                Collections.sort(myResult, new MyComparator(columnN));
                //here we have the sorted result. attach it to the end of tablesToMerge

                //join all the tables
                myResult = joinResult( order,myResult,columnN,tablesToMerge_r,tableColumns_r, vIdMap);
            }
                //else, no relation found for result added tags. add each tag_vId in map to each row of result
            else if(t==addedTgasSize-1){
                    for (Map.Entry<String, List<int[]>> entry : vIdMap.entrySet()) {
                        for(Vector v:myResult){
                            v.add(Arrays.asList(entry.getKey(),entry.getValue()));
                        }
                    }
            }
        }
        return myResult;
    }

    public List<Vector> joinResult( int order, List<Vector> myResult, Vector columnN, List<List<Vector>> tablesToMerge_r, List<Vector> tableColumns_r, HashMap<String, List<int[]>> vIdMap){
        Boolean notEnd = true;
        int tableToMergeSize = tablesToMerge_r.size();
        List<Vector> newResult = new ArrayList<>();
        for(int tableCursor=0; tableCursor<tableToMergeSize; tableCursor++){

            List<Vector> tableToMerge = tablesToMerge_r.get(tableCursor);
            int i=0; int j = 0;

            while (i != myResult.size() && j != tableToMerge.size()) {
                String result_value = myResult.get(i).get((int)columnN.get(0)).toString();
                String table_value = tableToMerge.get(j).get((int)tableColumns_r.get(tableCursor).get(0)).toString();
                int compare_result = result_value.compareTo(table_value);
                if (compare_result == 0) { //equals
                    System.out.println("checkPoint");
                    //add addTag value and id to result
                    //if never add before
                    if(myResult.get(0).size()<(order+1)*2){
                        String addTagValue = tableToMerge.get(j).get((int)tableColumns_r.get(tableCursor).get(1)).toString();
                        List<int[]> ids = vIdMap.get(addTagValue);
                        Vector row = (Vector)myResult.get(i).clone();
                        row.addAll(Arrays.asList(addTagValue,ids));
                        newResult.add(row);
                    }
                    //if addTag already exist in the result, add tag also need to be joined
                    else{
                        String addTagValue = tableToMerge.get(j).get((int)tableColumns_r.get(tableCursor).get(1)).toString();
                        String resultAddTagValue = myResult.get(i).get(myResult.get(0).size()-2).toString();
                        if(addTagValue.equals(resultAddTagValue)){
                            Vector row = myResult.get(i);
                            newResult.add(row);
                        }
                    }
                    if ((j + 1) != tableToMerge.size())
                        j++;
                    else {//tag list has goes to the end, so add the iterator of the table list
                        i++;
                    }
                } else if (compare_result < 0) { // table_value < tag_value
                    //previous table value equals current table value
                    if (i != myResult.size() && i > 0 && result_value.equals(myResult.get(i-1).get((int)columnN.get(0)).toString())) {
//                        if(tableCursor==0){
//                            System.out.println("need modify later, now let us just simply move down");
//                        }

                    }
                    i++;

                } else if (compare_result > 0) { // table_value > tag_value
                    j++;
                }
            }
        }

        return newResult;

    }

    public Vector joinTable(List<List<Vector>> tablesToMerge, List<Vector> tableColumns){
        Boolean notEnd = true;
        //@@@Vector is slower than list. When optimize the running time ,we may try to change all the vector to list.
        Vector returnV = new Vector();

        HashMap tagValueIdMap = new HashMap();
        List<List<Vector>> prunedTables = new ArrayList<>();
        //initialize the structure of pruneTable
        for(int i=0; i<tablesToMerge.size(); i++){
            List<Vector> pruneTable = new ArrayList<>();
            prunedTables.add(pruneTable);
        }
        int[] rowCursor = new int[tablesToMerge.size()];
        while(notEnd){
            //any one of the tables has gone to the end
            if(isEnd(tablesToMerge,rowCursor)){
                break;
            }
            List<String> tagValues = new ArrayList<>();
            for(int tableCursor = 0; tableCursor < tablesToMerge.size(); tableCursor++){
                tagValues.add(tablesToMerge.get(tableCursor).get(rowCursor[tableCursor]).get((int) tableColumns.get(tableCursor).get(0)).toString());
            }
            int compareResult = makeComparision(tagValues);
            //if the first column values equal, add this row to prunedTables one by one
            if(compareResult == -1){
                String commonValue = tagValues.get(0);
                List<int[]> idList = new ArrayList<>();
                //take the same value from following rows
                //move cursor
                for(int tableCursor=0; tableCursor<tablesToMerge.size(); tableCursor++){
                    //get current table
                    List<Vector> table=tablesToMerge.get(tableCursor);
                    //move cursor until current value is not same
                    for(int row=rowCursor[tableCursor]; row<table.size(); row++){
                        Vector tableRow = table.get(row);
                        //compared with itself, main purpose is to add its id to list
                        String value=tableRow.get((int) tableColumns.get(tableCursor).get(0)).toString();
                        if(! value.equals(commonValue)) {
                            break;
                        }
                        else{
                            rowCursor[tableCursor]++;
                            prunedTables.get(tableCursor).add(tableRow);
                            int[] id= (int[])tableRow.get((int) tableColumns.get(tableCursor).get(0)+1);
                            //here id may have duplicate values@@@@@@@@@@@@
                            if(id !=null){
                                idList.add(id);
                            }
                        }
                    }
                }
                //add common values' id to one list, duplicate the first tables' id, maybe we can use "set" to avoid duplicate later
                //add the id list to the first table's last common value row
                if(idList != null){
//                    ((List<int[]>)prunedTables.get(0).get(rowCursor[0]-1).get((int)tableColumns.get(0).get(0)+1)).addAll(idList);
                    tagValueIdMap.put(commonValue,idList);
                }
            }

            //add one to the row cursor number of the smallest table, then make comparision in next turn
            else{
                rowCursor[compareResult] = rowCursor[compareResult]+1;
            }

        }
        returnV.addAll(Arrays.asList(prunedTables,tagValueIdMap));
        return returnV;
    }

    //return true means is end.
    public boolean isEnd(List<List<Vector>> tablesLists, int[] rowCursor){
        for(int i=0;i<tablesLists.size();i++){
            // the last element of this table
            if(tablesLists.get(i).size() == rowCursor[i]){
                return true;
            }
        }
        return false;
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
            }
            myTables.add(rdb);
        }
    }

    static public void main(String[] args) throws Exception {
        //filename = args[0];
        filename = "xjoin/src/tjFast/simplePathPattern.xml";
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
        xmlReader.setContentHandler(new queryAnalysis_multi());

        // Set an ErrorHandler before parsing
        xmlReader.setErrorHandler(new MyErrorHandler(System.err));

        // Tell the XMLReader to parse the XML document
        xmlReader.parse(convertToFileURL(filename));
        queryAnalysis_multi qm = new queryAnalysis_multi();
        qm.getSolution();

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
