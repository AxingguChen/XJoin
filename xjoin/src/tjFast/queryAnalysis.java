package tjFast;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.*;
import java.io.*;


public class queryAnalysis extends DefaultHandler {

    Hashtable twigTagNames;

    static String filename;

    String ROOT;

    Stack TagStack;

    static String basicDocuemnt;

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

        //??????????query?????????????????????query?????
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

            List<Vector> re = lm.getSolution(tagList); // get xml value match table result
//            for(labelMatching.Match m:re){
//                try {
//                    BufferedWriter out = new BufferedWriter(new FileWriter("xjoin/src/xjoinAfterRemoveResult.txt",true));
//                    out.write(m.getL_v()+" "+m.getL_ID()+"\r\n"+m.getR_v()+" "+m.getR_ID()+"\r\n"+"\r\n");  //Replace with the string
//                    //you are trying to write
//                    out.close();
//                }
//                catch (IOException e)
//                {
//                    System.out.println("Exception ");
//
//                }
//            }
//            System.out.println("candidate:"+re);
            // start to calculate the running time
            //long totalbeginTime = System.currentTimeMillis();
            //long tjFastbeginTime = System.currentTimeMillis();
            //long tjFastbyAddTime = 0L;
            //System.out.println("start multi-times tjFast");
            int solutionCount = 0;

//            for(int i = 0;i<100;i++){
//                part0.add(o[0].get(i));
//                part1.add(o[1].get(i));
//            }
//            Vector partO[] = new Vector[2];
//            partO[0] = part0;
//            partO[1] = part1;
            for(int i=0;i<re.size();i++) {
                long loadbeginTime = System.currentTimeMillis();
                Hashtable[] alldata = d.loadAllLeafData(re.get(i), DTDInfor,tagList);

                //for double layer query only
//                alldata[0].put(tagList.get(2),partO[0]);
//                alldata[1].put(tagList.get(2), partO[1]);

//                alldata[0].put(tagList.get(2),o[0]);
//                alldata[1].put(tagList.get(2), o[1]);
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
            }
            long tjFastEndTime = System.currentTimeMillis();
            long totalendTime = System.currentTimeMillis();
            System.out.println("solutionCount:"+solutionCount);
            //System.out.println("Total tjFast time is " + (tjFastEndTime-tjFastbeginTime));
            //System.out.println("Total tjFast by add time is " + tjFastbyAddTime);


            System.out.println("Total tjFast load data time is " + totalLoadTime);

            System.out.println("Total tjFast join data time is " + totalJoinTime);

            System.out.println("Total running time is " + (totalendTime - totalbeginTime));

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
        System.err.println("Usage: QueryAnalysis <file.xml>");
        System.exit(1);
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
        xmlReader.setContentHandler(new queryAnalysis());

        // Set an ErrorHandler before parsing
        xmlReader.setErrorHandler(new MyErrorHandler(System.err));

        // Tell the XMLReader to parse the XML document
        xmlReader.parse(convertToFileURL(filename));
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
