
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.*;
import java.io.*;


public class initilizeDTDTable extends DefaultHandler {
   
    	Hashtable tagsHash ;
    	
    	Stack tagPathStack; 

 	DTDTable dtdTable ;
 	
 	String ROOT;
 	

    // Parser calls this once at the beginning of a document
    public void startDocument() throws SAXException {
       
        tagsHash = new Hashtable();
        
        tagPathStack = new Stack();
    }


	

    // Parser calls this for each element in a document
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts)
	throws SAXException
    {
        String child = localName;
        
        if (tagPathStack.size()==0){ //This is ROOT
        tagPathStack.push(child);
        ROOT =child;
        }//end if 
        else
      {  String parent =  (String) tagPathStack.peek();
       
       if  (tagsHash.containsKey(parent)){
       	Vector temp = (Vector)tagsHash.get(parent);
       	boolean found = false;
       	for (int i=0;i<temp.size();i++)
       		if (   ((String)temp.elementAt(i)).equalsIgnoreCase(child) ) found = true;
       	if (! found) 
       		temp.addElement(child);       			
       	}//end if
       	else {  Vector temp = new Vector();
       		temp.addElement(child);
       		tagsHash.put(parent,temp);
       	}//end else
       	
      	tagPathStack.push(child);
      	
      	}//end else 
       
      
        
    }//end startElement
    
    
     public void endElement(String namespaceURI, String localName,
                             String qName)
	throws SAXException
    {
    	
    	 tagPathStack.pop();
    	   
    	
}//end endElement

	int  getNumberofRules(){
	
		Enumeration e = tagsHash.keys();
    	
    		int numberOfRules = 0;
    	
    		while (e.hasMoreElements())
 			{numberOfRules++;
 			e.nextElement();
 			}//end while
 			
 		return numberOfRules;
 		
	}//end getNumberofRules
	
	Vector  getTagsVector(){
	
		Enumeration e = tagsHash.keys();
    	
    		Vector tags = new Vector ();
    	
    		while (e.hasMoreElements())
    		{	String tag = (String)e.nextElement();
    		
    			if (! tags.contains(tag))  tags.addElement(tag);
       				
    			Vector temp = (Vector)tagsHash.get(tag);
    			
    	     		for (int i=0;i<temp.size();i++)       			
       				if (! tags.contains(temp.elementAt(i)))  tags.addElement(temp.elementAt(i));
       				      			
    		}//end while
    		
 		return tags;
 		
	}//end getNumberofRules
	
	Hashtable mapNameToInteger(Vector v, Hashtable tagsHash){
		
		Hashtable map = new Hashtable();
		Enumeration e = tagsHash.keys();
    	
    		int k = 0;
    		
    		while (e.hasMoreElements())
    		{
    			String tag = (String)e.nextElement();
    			map.put(tag, new Integer(k++));
    			
		}//end while
		
		for(int i=0;i<v.size();i++)
		{	if     (! map.containsKey((String)v.elementAt(i)))
				map.put((String)v.elementAt(i), new Integer(k++));
			}//end for
		
		return map; 
		
		}//end mapNameToInteger


    public void endDocument() throws SAXException {
    		
    		int [] tagsNumberOfRule = new int [getNumberofRules()];
    		
    		
    		int DTDrule [][] = new int [getNumberofRules()][getTagsVector().size()] ;
	 	//	我们想 DTDrule position reference is integer and output is also integer;
	 	
	 	//first create a mapping relation between tag name and integer
		Hashtable map = mapNameToInteger(getTagsVector(),tagsHash);
		
    		Enumeration e = tagsHash.keys();
    		
    		while (e.hasMoreElements())
    		{	String tag = (String)e.nextElement();
    			Vector temp = (Vector)tagsHash.get(tag);
    			int reference = ((Integer)map.get(tag)).intValue();
    			
    			tagsNumberOfRule[reference]= temp.size();
    			//注意 DTDrule [reference][0] 这个整数， 我们没有做任何定义，也绝对不应该被访问到。
    			for (int i=1;i<=temp.size();i++){   	
    	   			
    	   			DTDrule [reference][i]= ((Integer)map.get((String)temp.elementAt(i-1))).intValue();
       				utilities.DebugPrintln(tag+"'child is "+ (String)temp.elementAt(i-1));
       				utilities.DebugPrintln(tag+"'child is "+ DTDrule [reference][i]);
       			
       			}//end for
       			
    		}//end while
    		
    		/* 通过设置以下的内容， 把结果传给类 dtdTable */
       		dtdTable = new DTDTable();
    	
    		dtdTable.setNumberOfRules(getNumberofRules());
    		dtdTable.setDTDrule(DTDrule);
    		dtdTable.setNumberOfTags(getTagsVector().size());
    		dtdTable.setTagsNumberOfRule(tagsNumberOfRule);
      		dtdTable.setMap(map);
    		dtdTable.setRoot(((Integer)map.get(ROOT)).intValue());	             
    	
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

     public DTDTable initilizeTable(String filename) throws Exception {
       
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
        initilizeDTDTable Table = new initilizeDTDTable();
        xmlReader.setContentHandler(Table);

        // Set an ErrorHandler before parsing
        xmlReader.setErrorHandler(new MyErrorHandler(System.err));

        // Tell the XMLReader to parse the XML document
        xmlReader.parse(convertToFileURL(filename));
        
        return Table.dtdTable ;
        
    }//end main
    
    

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
