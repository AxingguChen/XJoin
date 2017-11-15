package tjFast;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.PrintStream;
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
        File folder = new File("xjoin/src/multi_rdbs/queries/");
        basicDocuemnt = "xjoin/src/multi_rdbs/Invoice.xml";
        //add-order
        List<String> tagList = Arrays.asList("a","b","c","d","e");

        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                System.out.println(file);
                analysisQuery(file.getPath());
                for(String s:tagList){
                    //xmlRelationTagSet -> pc relations
                    if(! xmlRelationTagSet.contains(s) && allTags.contains(s)){
                        Vector v = new Vector();
                        List<Vector> l = new ArrayList<>();
                        v.add(s);
                        l.add(v);
                        myTables.add(l);
                    }
                }
            }
        }
        //get pc tables, read RDB and join tables
//        getSolution();

        //Verify query structure, multi-tjFast
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
