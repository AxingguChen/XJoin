package tjFast;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.*;
import java.io.*;


public class documentAnalysis extends DefaultHandler {

  private Stack tagPathStack;
 	private Stack labelPathStack;
 	private Stack maxSilblingStack;

 	DTDTable dtdTable ;

 	static String filename;

 	String ROOT;

 	int elementNumber = 0;

    // Parser calls this once at the beginning of a document
    public void startDocument() throws SAXException {

        tagPathStack = new Stack();
        labelPathStack = new Stack();
        maxSilblingStack = new Stack();

        initilizeDTDTable initilize = new  initilizeDTDTable();

        try{
        dtdTable = initilize.initilizeTable(filename);

        }catch (Exception e){System.out.println(e);}

    }//end startDocument


	public void characters(char[] ch, int start,int length )
       {
    //   	char [] chara =ch;

     //  	System.out.println(new String (chara,start,length));

         }// end characters

    // Parser calls this for each element in a document
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts)
	throws SAXException
    {
        String child = localName;

        if (tagPathStack.size()==0){ //This is ROOT
        ROOT = child;
        tagPathStack.push(child);
        labelPathStack.push(new Integer(-1));
        maxSilblingStack.push(new Integer(-1));
        }//end if
        else
      {  String parent =  (String) tagPathStack.peek();

      	int maxleftSibling = ((Integer)maxSilblingStack.peek()).intValue();

      	int newLabel = dtdTable.getLabel (parent, child, maxleftSibling);

      	maxSilblingStack.pop();
      	maxSilblingStack.push(new Integer(newLabel));

      	tagPathStack.push(child);
      	labelPathStack.push(new Integer(newLabel));


       	maxSilblingStack.push(new Integer(-1));

      	}//end else

      	//showAssignedLable(child,labelPathStack);

       outputAssignedLable(child,labelPathStack);

       // outputNewAssignedLable(tagPathStack,labelPathStack); //�������µķ����������������Dewey ID ������㷨��

       	elementNumber++;

    }//end startElement


     public void endElement(String namespaceURI, String localName,
                             String qName)
	throws SAXException
    {

    	String tag = (String) tagPathStack.pop();


       labelPathStack.pop();
       maxSilblingStack.pop();


  	}//end endElement

    // Parser calls this once after parsing a document
    public void endDocument() throws SAXException {
    	System.out.println("Total element number is "+ elementNumber);
        }//end document



    void showAssignedLable(String tag, Stack labelPathStack){

    	int labels [] = new int [labelPathStack.size()-1];
    	System.out.println();
    	System.out.print(" Tag is "+tag+" :");

    	for (int i=1;i<labelPathStack.size();i++){
    	utilities.DebugPrint(" "+((Integer)labelPathStack.elementAt(i)).intValue());
    	labels[i-1]=((Integer)labelPathStack.elementAt(i)).intValue();
	}//end for

	int result [] =dtdTable.getAllTags(labels,dtdTable.root);

     	for(int i=0;i<result.length;i++)
     	System.out.print(" "+result[i]);


	}//end  showAssignedLable


 	void outputAssignedLable(String tag, Stack labelPathStack){

    	int labels [] = new int [labelPathStack.size()-1];

    	for (int i=1;i<labelPathStack.size();i++)
    		labels[i-1]=((Integer)labelPathStack.elementAt(i)).intValue();

    	outputLabel.outputUTF8 (tag,labels);


    	}//end  outputAssignedLable

    void  outputNewAssignedLable(Stack tagPathStack, Stack labelPathStack){

    	int labels [] = new int [labelPathStack.size()-1];

    	for (int i=1;i<labelPathStack.size();i++)
    		labels[i-1]=((Integer)labelPathStack.elementAt(i)).intValue();

     String fileID =  FileID2PathTable.createFileID(tagPathStack);

    	outputLabel.outputUTF8 (fileID,labels);


    	}//end   outputNewAssignedLable





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

         filename = args[0];

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
    }



    // Error handler to report errors and warnings
    private static class MyErrorHandler implements ErrorHandler {
        /** Error handler output goes here */
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
