import java.util.*;
import java.io.*;


public class solutionKey  {
	
	
	Vector branchnodes ; 
	
	String leaf ; 
	
	public int solutionNumber ;
	
	String branch ; 
	
	public solutionKey(Vector branchnodes, String leaf, String branch){
		
		this.leaf = leaf;
		
		this.branch = branch;
		
		this.branchnodes = branchnodes;
		
		solutionNumber = 1;
		
		}//end solutionKey

}//end solutionKey
