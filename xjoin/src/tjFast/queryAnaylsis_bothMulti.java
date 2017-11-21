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
//                joinTables();

                //Verify query structure, multi-tjFast
                allTags.clear();
                myTables.clear();
                xmlRelationTagSet.clear();
            }
        }

    }

    public void joinTables(List<String> tagList, List<List<Vector>> AllTables) {
        List<List<Vector>> result = new ArrayList<>();
        //join order
        for(int joinOrder=0; joinOrder<tagList.size(); joinOrder++){
            List<List<Vector>> tables = AllTables;
            String addTag = tagList.get(joinOrder);
            System.out.println("add tag:"+addTag);

            //join add_tag with join_result
            List<List<String>> tagCombs = getJoinedTagComb(tagList,joinOrder+1);
            //check joined tags combinations one by one
            for(List<String> tagComb:tagCombs){
                List<List<Vector>> tablesToMerge = new ArrayList<>();
                List<List<Integer>> tableColumns = new ArrayList<>();
                for(int tableCursor=0; tableCursor<tables.size(); tableCursor++){
                    List<Vector> thisTable = tables.get(tableCursor);
                    Vector tableTag = thisTable.get(tableCursor);
                    if(tableTag.containsAll(tagComb)){
                        List<Integer> tableColumn = new ArrayList<>();
                        //find common tags column number
                        for(String tag:tagComb){
                            int table_column = getColumn(tableTag,tag);
                            tableColumn.add(table_column);
                        }

                        List<Vector> table_removeFirstRow = thisTable.subList(1,thisTable.size());

                        //remove this table. @@@@@calculate time spend here to make sure this step will not cost too many time
                        tables.remove(tableCursor);
                        //sort current table according to column order
                        Collections.sort(table_removeFirstRow,new MyComparator(tableColumn));
                        //add table
                        tablesToMerge.add(table_removeFirstRow);
                        //add column number to list
                        tableColumns.add(tableColumn);
                    }
                }
                //if tablesToMerge has table contains this tag combination, join with Result
                if(!tablesToMerge.isEmpty()){
                    //sort result table
                    //first, find column Nos in result table

                }
            }

        }
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
