package produce;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import tjFast.Query;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;


public class documentAnalysis extends DefaultHandler {

    private Stack tagPathStack;
    private Stack labelPathStack;
    private Stack maxSilblingStack;
    //temporary store the element value
    private Stack eleValueStack;

    DTDTable dtdTable;
    //xml file name
    static String filename;
    //store the name of left tag, right tag so that we only write needed value and ID to local file
    static List<String> tagList = new ArrayList<>();

    String ROOT;
    static String dir;

    static int elementNumber = 0;
    static int validEleNum = 0;

    // Parser calls this once at the beginning of a document
    public void startDocument() throws SAXException {

        tagPathStack = new Stack();

        labelPathStack = new Stack();
        maxSilblingStack = new Stack();
        eleValueStack = new Stack();

        initilizeDTDTable initilize = new initilizeDTDTable();

        try {
            dtdTable = initilize.initilizeTable(filename);
            System.out.println("start document");
        } catch (Exception e) {
            System.out.println(e);
        }

    }//end startDocument

    /**
     * Read value in xml
     */
    public void characters(char[] ch, int start, int length) {
        char[] chara = ch;
        String value = (new String(chara, start, length)).trim();
        if (value.length() != 0) {

            eleValueStack.push(value);
            //System.out.println("value:" + value);
            outputAssignedValue(tagPathStack.peek().toString(), eleValueStack.peek().toString());
        }

    }// end characters

    // Parser calls this for each element in a document
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts)
            throws SAXException {
        String child = localName;

        if (tagPathStack.size() == 0) { //This is ROOT
            ROOT = child;
            tagPathStack.push(child);
            labelPathStack.push(new Integer(-1));
            maxSilblingStack.push(new Integer(-1));
        }//end if
        else {
            String parent = (String) tagPathStack.peek();
            int maxleftSibling = ((Integer) maxSilblingStack.peek()).intValue();

            int newLabel = dtdTable.getLabel(parent, child, maxleftSibling);

            maxSilblingStack.pop();
            maxSilblingStack.push(new Integer(newLabel));

            tagPathStack.push(child);
            labelPathStack.push(new Integer(newLabel));


            maxSilblingStack.push(new Integer(-1));

        }//end else
        //in dataset3(treebank), "IN" is the name of an element. So line103-105 are annotated since I do not see the function of these lines in other cases.
//        if (child.equalsIgnoreCase("IN")) {
//            showAssignedLable(child, labelPathStack);
//        }//end if
        outputAssignedLable(child, labelPathStack);
        //System.out.println("current element: " + child + " Path: " + tagPathStack.toString());
        //System.out.println("label: " + labelPathStack.toString());
        elementNumber++;

    }//end startElement


    public void endElement(String namespaceURI, String localName,
                           String qName)
            throws SAXException {
        // removing top object
        tagPathStack.pop();
        //outputAssignedValue(tag, eleValueStack);

        labelPathStack.pop();
        maxSilblingStack.pop();
    }//end endElement

    // Parser calls this once after parsing a document
    public void endDocument() throws SAXException {

        System.out.println("Total element number is " + elementNumber);
    }//end document


    void showAssignedLable(String tag, Stack labelPathStack) {

        int labels[] = new int[labelPathStack.size() - 1];
        System.out.println();
        System.out.print(" Tag is " + tag + " :");

        for (int i = 0; i < tagPathStack.size(); i++)
            System.out.print(" " + (String) tagPathStack.elementAt(i));


        for (int i = 1; i < labelPathStack.size(); i++) {
            System.out.print(" " + ((Integer) labelPathStack.elementAt(i)).intValue());
            labels[i - 1] = ((Integer) labelPathStack.elementAt(i)).intValue();
        }
    }//end  showAssignedLable

    void outputAssignedValue(String tag, String value) {
        String tag_v = tag;
        //System.out.println("tag:"+tag+" value:"+(((String) eleValueStack.peek())));
        //int[] values = {Integer.parseInt((eleValueStack.peek().toString()))};
        if (tagList.contains(tag_v)) {
            outputLabel.outputUTF8_v(dir, tag_v, value);
        }
    }

    void outputAssignedLable(String tag, Stack labelPathStack) {

        int labels[] = new int[labelPathStack.size() - 1];

        for (int i = 1; i < labelPathStack.size(); i++)
            labels[i - 1] = ((Integer) labelPathStack.elementAt(i)).intValue();
        if (tagList.contains(tag)) {
            outputLabel.outputUTF8(dir, tag, labels);
            validEleNum ++;
        }

    }//end  outputAssignedLable


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

    public void doAnalysis(String xml_document_file) throws Exception {
        //filename = args[0];

        filename =  xml_document_file;
        if (filename == null) {
            usage();
        }

        SAXParserFactory spf = SAXParserFactory.newInstance();

        spf.setNamespaceAware(true);

        // Create a JAXP SAXParser
        SAXParser saxParser = spf.newSAXParser();

        // Get the encapsulated SAX XMLReader
        XMLReader xmlReader = saxParser.getXMLReader();

        // Set the ContentHandler of the XMLReader
        xmlReader.setContentHandler(new documentAnalysis());

        // Set an ErrorHandler before parsing
        xmlReader.setErrorHandler(new MyErrorHandler(System.err));

        // Tell the XMLReader to parse the XML document
        xmlReader.parse(convertToFileURL(filename));


        System.out.println("End of document Analysis");
    }

    public void runAnalysis(List<String> tags, String xmlDocumentFile, String streamStoreDir) throws Exception{
        documentAnalysis d = new documentAnalysis();
        tagList = tags;
        dir = streamStoreDir;
//        File directory = new File(dir);
//        for(File f: directory.listFiles())
//            f.delete();
        d.doAnalysis(xmlDocumentFile);
        System.out.println("Valid element:"+validEleNum);
    }

    static public void main(String[] args) throws Exception {
        documentAnalysis d = new documentAnalysis();

//        tagList.addAll(Arrays.asList("a","b","c","d","e"));
        tagList.addAll(Arrays.asList("Invoices","Invoice","OrderId","asin","price","Orderline","productId"));
        //delete previous files
        dir = "xjoin/src/produce/outputData";
        File directory = new File(dir);
        for(File f: directory.listFiles())
            f.delete();
        d.doAnalysis("xjoin/src/testCaseDataSet/testCase1/xml_document_file.xml");
        System.out.println("Valid element:"+validEleNum);
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
