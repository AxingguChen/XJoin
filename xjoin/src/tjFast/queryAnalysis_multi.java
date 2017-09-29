package tjFast;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import produce.generateValueIdPair;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.PrintStream;
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
    static ArrayList<ArrayList<Vector>> PCTables = new ArrayList<>();

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
            ArrayList<Vector> pc = new ArrayList<>();
            Vector v = new Vector();
            v.add(parent);v.add(child);
            pc.add(v);
            PCTables.add(pc);
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
        System.out.println(PCTables);

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
        System.out.println("getSolution:"+PCTables);
        generateValueIdPair generate = new generateValueIdPair();
        //divide p-c relation in xml to RDBs.
        PCTables = generate.generatePCVId(PCTables);
        System.out.println(PCTables);

        //read RDB files

        //Merge all tables
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
