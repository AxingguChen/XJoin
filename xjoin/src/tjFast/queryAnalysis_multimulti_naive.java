package tjFast;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;
import produce.documentAnalysis;

/**
 * Created by zzzhou on 2017-07-18.
 */
public class queryAnalysis_multimulti_naive extends DefaultHandler {
    Hashtable twigTagNames;

    static String filename;
    static String rdbTable;
    static List<String> tagList;
    static Boolean doubleAD;
    static List<List<List<String>>> tjFastResult;
    static List<List<String>> tjFastResultTags = new ArrayList<>();
    static long sortTotalTime = 0L;
    static long joinTotalTime = 0L;

    String ROOT;

    Stack TagStack;
    static String xmlStreamDir;
    static int queryM;

    static String basicDocument;

    // Parser calls this once at the beginning of a document
    public void startDocument() throws SAXException {

        twigTagNames = new Hashtable();

        TagStack = new Stack();

    }//end startDocument


    public void characters(char[] ch, int start, int length) {
        String value = new String(ch, start, length);

        if (value.equalsIgnoreCase("1")) { //is PC relationship
            String child = (String) TagStack.peek();
            String parent = (String) TagStack.elementAt(TagStack.size() - 2);
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
        System.out.println("begin analysis query !");

        Query.setTwigTagNames(twigTagNames);


        Query.setRoot(ROOT);

        Query.getQueryNodes();

        utilities.DebugPrintln("Query root is " + Query.getRoot());


    	 /*Hashtable h =Query.calculateBranchPosition();


   	Enumeration e = h.keys();
    	while (e.hasMoreElements())
    	{	String s = (String)e.nextElement();
    		int [] pos = (int [] )h.get(s);
    		utilities.DebugPrintln(s+ "position is "+pos[0]+" ; "+pos[1]);
    	}//end while
    	*/
        System.out.println("begin analysis document !");

        try {
            DTDTable DTDInfor = loadDataSet.produceDTDInformation(basicDocument);

            long totalbeginTime = System.currentTimeMillis();


            Query.preComputing(DTDInfor);

            loadDataSet d = new loadDataSet();
            List<String> leaves = Query.getLeaves();
            List<String> branches = Arrays.asList(Query.getBranchNode());
            System.out.println("begin load data !"+leaves+" "+branches);

            //do analysis document
            List<String> allNodes = leaves;
            allNodes.addAll(branches);
//            documentAnalysis da = new documentAnalysis();
            String dir = xmlStreamDir + "_"+queryM;
//            da.runAnalysis(allNodes,basicDocument,dir);
//            System.out.println("queryM:"+queryM);
            long loadbeginTime = System.currentTimeMillis();

            Hashtable [] alldata = d.loadAllLeafData_naive (dir, Query.getLeaves(),DTDInfor);

            long loadendTime = System.currentTimeMillis();
            System.out.println("load tjFast input data(include all tag id value) time is "+(loadendTime-loadbeginTime));

            //begin join
            //System.out.println( "begin join !");

            long joinbeginTime = System.currentTimeMillis();

            TwigSet join = new TwigSet(DTDInfor,alldata[1],alldata[0] );

            List<HashMap<String, String>> allTagIDValue = d.getAllTagIDValue();
            //other tags'
            for(String tag:branches){
                allTagIDValue.add(d.loadData_naiveMulti(dir,tag, DTDInfor));
            }

            List<List<String>> addResult = join.beginJoin_naiveMulti(allTagIDValue);

            tjFastResult.add(addResult);
            tjFastResultTags.add(allNodes);

            long joinendTime = System.currentTimeMillis();

            long totalendTime = System.currentTimeMillis();

//            System.out.println("tjFast join time is(need minus find xml by id time) "+(joinendTime-joinbeginTime));

            System.out.println("tjFast total running time is "+(totalendTime-totalbeginTime));

        } catch (Exception e) {
            e.printStackTrace();
        }//end catch
    	/*join.locateMatchedLabel("c");
    	join.advanceStream("d");
    	join.locateMatchedLabel("d");
    	join.advanceStream("b");
    	join.locateMatchedLabel("b");*/

        //join.MatchedPrefixes ("c","a" );// parameter format(leaf,branch)


    }//end document


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
        System.err.println("Usage: QueryAnalysis_multiNaive <file.xml>");
        System.exit(1);
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

    public void removeDuplicateRow(){
        List<List<List<String>>> tableList = new ArrayList<>();
        for(int tableCursor=0; tableCursor<tjFastResult.size(); tableCursor++){
            List<List<String>> al = new ArrayList<>();
            Set<List<String>> hs = new HashSet<>();
            hs.addAll(tjFastResult.get(tableCursor));
            al.addAll(hs);
            tableList.add(al);
        }
        tjFastResult = tableList;
    }

    public Vector calculateCols(List<String> baseTags, List<String> addTags){
        //find common tags based on the first query's
        List<Integer> baseCols = new ArrayList<>();
        List<Integer> addCols = new ArrayList<>();
        //calculate addTable different tag column number
        List<Integer> addTableAddTagCols = new ArrayList<>();
        for(int addTagCursor=0; addTagCursor<addTags.size(); addTagCursor++){
            String addTag=addTags.get(addTagCursor);
            Boolean containThisAddTag = false;
            for(int baseTagCursor=0; baseTagCursor<baseTags.size(); baseTagCursor++){
                String baseTag=baseTags.get(baseTagCursor);
                if(baseTag.compareTo(addTag)==0){
                    baseCols.add(baseTagCursor);
                    addCols.add(addTagCursor);
                    containThisAddTag = true;
                }
            }
            if(!containThisAddTag){
                addTableAddTagCols.add(addTagCursor);
                baseTags.add(addTag);
            }
        }
        //return values,0:baseCols, 1:addCols, 2:addTableAddTagCols, 3:baseTags
        Vector v = new Vector();
        v.add(baseCols);
        v.add(addCols);
        v.add(addTableAddTagCols);
        v.add(baseTags);
        return v;
    }

    public Vector joinTJFastTables(){
        List<String> baseTags = tjFastResultTags.get(0);
        List<List<String>> baseTable = tjFastResult.get(0);
        //find common columns. if query numbers are bigger than 1, at least 2. multi xml join multi rdb
        if(tjFastResultTags.size()>1){
            for(int queryCursor=1; queryCursor<tjFastResultTags.size(); queryCursor++){
                List<String> addTags = tjFastResultTags.get(queryCursor);
                //find common tags based on the first query's
                Vector v = calculateCols(baseTags, addTags);
                List<Integer> baseCols = (List<Integer>) v.get(0);
                List<Integer> addCols = (List<Integer>) v.get(1);
                //calculate addTable different tag column number
                List<Integer> addTableAddTagCols = (List<Integer>) v.get(2);
                baseTags = (List<String>)v.get(3);

                //sort tables on common tags one by one
                List<List<String>> addTable = tjFastResult.get(queryCursor);
                long startSortTime = System.currentTimeMillis();
                Collections.sort(baseTable, new MyComparator(baseCols));
                Collections.sort(addTable, new MyComparator(addCols));
                long endSortTime = System.currentTimeMillis();
                sortTotalTime += endSortTime-startSortTime;
                //join tables
                System.out.println("before join:"+baseTable.size()+", "+addTable.size());
                baseTable = joinTables(baseTable,addTable,baseCols,addCols,addTableAddTagCols);
                System.out.println("after join:"+baseTable.size());
            }
        }
        //else directly join with rdbs

        Vector v = new Vector();
        v.add(baseTable);
        v.add(baseTags);
        return v;
    }

    public void getSolution(String xml_query_folder, String xml_document_folder, String rdb_table, String xmlStreamStoreDir) throws Exception {
        File queryFolder = new File(xml_query_folder);
        File basicDocumnetFolder = new File(xml_document_folder);
        xmlStreamDir = xmlStreamStoreDir;
        //read query file, get tjFast result
        File[] listOfFiles_query = queryFolder.listFiles();
        File[] listOfFiles_document = basicDocumnetFolder.listFiles();
        int queryNo = listOfFiles_query.length;
        if( queryNo != listOfFiles_document.length){
            System.out.println("please check query and basic document files. Their sizes are not same.");
            System.exit(0) ;
        }
        for(int i=0; i< queryNo; i++){
            queryM = i;
            String xml_query_file = listOfFiles_query[i].getPath();
            String xml_document_file =listOfFiles_document[i].getPath();
            runTJFast(xml_query_file, xml_document_file);
            Query.clearQuery();
            mergeAllPathSolutions.clearAllStaticData();
        }
        //remove duplicate rows in tjFastResult
        removeDuplicateRow();

        //join tjFastTables
        Vector v  =  joinTJFastTables();
        List<List<String>> tjFastTable = (List<List<String>>) v.get(0);
        List<String> tjFastTableTags = (List<String>)v.get(1);

        //read rdb tables
        Vector v_rdb = readRDB(rdb_table);
        List<List<List<String>>> rdbs = (List<List<List<String>>>)v_rdb.get(0);
        List<List<String>> rdbTags = (List<List<String>>)v_rdb.get(1);
        for(int rdbCursor=0; rdbCursor<rdbs.size(); rdbCursor++){
            List<List<String>> rdb = rdbs.get(rdbCursor);
            List<String> rdbTag = rdbTags.get(rdbCursor);
            Vector tagInfo = calculateCols(tjFastTableTags, rdbTag);
            //return values,0:baseCols, 1:addCols, 2:addTableAddTagCols, 3:baseTags_update
            List<Integer> baseCols = (List<Integer>) tagInfo.get(0);
            List<Integer> addCols = (List<Integer>) tagInfo.get(1);
            long sortStartTime = System.currentTimeMillis();
            Collections.sort(tjFastTable, new MyComparator(baseCols));
            Collections.sort(rdb, new MyComparator(addCols));
            long sortEndTime = System.currentTimeMillis();
            sortTotalTime += sortEndTime-sortStartTime;
            tjFastTable = joinTables(tjFastTable, rdb, baseCols, addCols, (List<Integer>) tagInfo.get(2));
            tjFastTableTags = (List<String>) tagInfo.get(3);
        }
        System.out.println("a:"+tjFastTable);
        System.out.println("total sort table time:"+sortTotalTime);
        System.out.println("total join table time:"+joinTotalTime);
    }

    //read RDB value and merge list to myTables.
    public Vector readRDB(String rdb_tables_dir) throws Exception{
        long startTime = System.currentTimeMillis();
        List<List<List<String>>> myRDBs = new ArrayList<>();
        List<List<String>> rdbTags = new ArrayList<>();
        File directory = new File(rdb_tables_dir);
        for(File f: directory.listFiles()){
            String line = "";
            Boolean firstLine = true;
            List<List<String>> rdb = new ArrayList<>();
            List<Integer> rdbCols = new ArrayList<>();
            List<Integer> rdbAddCols = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                while ((line = br.readLine()) != null) {
                    List<String> row = new ArrayList<>();
                    row.addAll(Arrays.asList(line.split("\\s*,\\s*")));
                    if(firstLine){
                        firstLine = false;
                        rdbTags.add(row);
                    }
                    else{
                        rdb.add(row);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            myRDBs.add(rdb);
        }
        Vector v = new Vector();
        v.add(myRDBs);
        v.add(rdbTags);
        long endTime = System.currentTimeMillis();
        System.out.println("read rdb time:"+(endTime-startTime));
        return v;
    }


    public int moveTableUntilEoB(List<List<String>> table, int row, int col, String value){
        while(row<table.size()){
            String thisValue = table.get(row).get(col);
            if(thisValue.compareTo(value)<0) row++;
            else break;
        }
        return row;
    }

    public int moveTableUntilB(List<List<String>> table, int row, int col, String value){
        while(row<table.size()){
            String thisValue = table.get(row).get(col);
            if(!(thisValue.compareTo(value)>0)) row++;
            else break;
        }
        return row;
    }

    public List<List<String>> joinTables(List<List<String>> baseTable, List<List<String>> addTable, List<Integer> baseCols, List<Integer> addCols, List<Integer> addTableAddTagCols){
        long joinStartTime = System.currentTimeMillis();
        List<List<String>> newTable = new ArrayList<>();
        int baseRow = 0;
        int addRow = 0;
        while(baseRow!=baseTable.size() && addRow!=addTable.size()){
//            System.out.println("baseRow:"+baseRow+" addRow:"+addRow);
            int baseCol = baseCols.get(0);
            int addCol = addCols.get(0);
            String baseValue = baseTable.get(baseRow).get(baseCol);
            String addValue = addTable.get(addRow).get(addCol);
            int compareResult=baseValue.compareTo(addValue);
            if(compareResult==0){
                //compare other values of possible cols
                Boolean allEqual = true;
                int bRowUpdate = moveTableUntilB(baseTable,baseRow,baseCol,baseValue);
                int aRowUpdate = moveTableUntilB(addTable,addRow,addCol,addValue);
                List<List<String>> baseSubTable = baseTable.subList(baseRow, bRowUpdate);
                List<List<String>> addSubTable = addTable.subList(addRow,aRowUpdate);
                if(baseCols.size()>1){
                    //todo need to update later for complex situation
                    //this situation means we need to compare other values to match same value.
//                    System.out.println("never goes to here...from queryAnalysisMultiMultiNaive line 319");
                    List<Integer> baseSubCols = baseCols.subList(1,baseCols.size());
                    List<Integer> addSubCols = addCols.subList(1,addCols.size());
                    newTable.addAll(joinTables(baseSubTable, addSubTable, baseSubCols, addSubCols, addTableAddTagCols));
                }
                //if has result
                else if(allEqual){
                    //if baseTable contains all tag that addTable has
                    if(addTableAddTagCols.isEmpty()){
                        newTable.addAll(baseSubTable);
                    }
                    else{
                        for(int baseSubRow=0; baseSubRow<baseSubTable.size(); baseSubRow++){
                            List<String> baseRowValues = baseSubTable.get(baseSubRow);
                            for(int addSubRow=0; addSubRow<addSubTable.size(); addSubRow++){
                                List<String> newTableRow = new ArrayList<>();
                                newTableRow.addAll(baseRowValues);
                                for(int addTagCol=0; addTagCol<addTableAddTagCols.size(); addTagCol++){
                                    newTableRow.add(addSubTable.get(addSubRow).get(addTableAddTagCols.get(addTagCol)));
                                }
                                newTable.add(newTableRow);
                            }
                        }
                    }

                }
                baseRow = bRowUpdate;
                addRow = aRowUpdate;

            }
            //base value > add value, move rowNo of addTable
            else if(compareResult>0){
                addRow = moveTableUntilEoB(addTable, addRow, addCols.get(0), baseValue);
            }
            else baseRow = moveTableUntilEoB(baseTable, baseRow, baseCols.get(0), addValue);

        }
        long joinEndTime = System.currentTimeMillis();
        joinTotalTime += joinEndTime-joinStartTime;
        return newTable;
    }

    public void runTJFast(String xml_query_file, String xml_document_file) throws Exception{
        long startTJFastTime = System.currentTimeMillis();
        //filename = args[0];
        filename = xml_query_file;
        //basicDocument = args[1];
        basicDocument = xml_document_file;

//        doubleAD = ifDoubleAD;

        if (filename == null) {
            usage();
        }

        if (basicDocument == null) {
            usage();
        }

        SAXParserFactory spf = SAXParserFactory.newInstance();

        spf.setNamespaceAware(true);

        // Create a JAXP SAXParser
        SAXParser saxParser = spf.newSAXParser();

        // Get the encapsulated SAX XMLReader
        XMLReader xmlReader = saxParser.getXMLReader();

        // Set the ContentHandler of the XMLReader
        xmlReader.setContentHandler(new queryAnalysis_multimulti_naive());

        // Set an ErrorHandler before parsing
        xmlReader.setErrorHandler(new MyErrorHandler(System.err));

        // Tell the XMLReader to parse the XML document
        xmlReader.parse(convertToFileURL(filename));
        long endTJFastTime = System.currentTimeMillis();
        System.out.println("This tjFastTime:"+(endTJFastTime-startTJFastTime));
    }

    static public void main(String[] args) throws Exception {
        long startRunTime = System.currentTimeMillis();
        tjFastResult = new ArrayList<>();
        String xml_query_folder = "xjoin/src/multi_rdbs/queries/";
        String xml_document_folder = "xjoin/src/multi_rdbs/invoices/";
        String rdb_document_file = "xjoin/src/multi_rdbs/testTables";
        String xmlStreamStoreDir = "xjoin/src/xmlStreams";
        queryAnalysis_multimulti_naive a = new queryAnalysis_multimulti_naive();
        a.getSolution(xml_query_folder,xml_document_folder,rdb_document_file,xmlStreamStoreDir);
        long endRunTime = System.currentTimeMillis();
        System.out.println("Total run time:"+(endRunTime-startRunTime));
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
