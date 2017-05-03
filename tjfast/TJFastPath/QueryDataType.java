import java.util.*;

public class QueryDataType { 
	
	String tagName ;
	boolean isPCEdge;
	
	public QueryDataType (String tagName,boolean isPCEdge){
		this.tagName = tagName;
		this.isPCEdge = isPCEdge;
		
	}//end QueryDataType
	
	String getTagName(){
		return tagName;
	}//end getTagName
	
	boolean getIsPCEdge() {
		return isPCEdge;
	}//end getIsPCEdge
	
	void setPCEdge() {
		 isPCEdge= true;
	}//end getIsPCEdge
	
}//end class