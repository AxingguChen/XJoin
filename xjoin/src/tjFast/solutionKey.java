package tjFast;

import java.util.*;
import java.io.*;


public class solutionKey  {
	
	
	Vector branchnodes ;

	List<int[]> currentNode;
	
	String leaf ; 
	
	public int solutionNumber ;
	
	String branch ; 
	
	public solutionKey(Vector branchnodes, String leaf, String branch, int[] currentNodeID){
		
		this.leaf = leaf;
		
		this.branch = branch;
		
		this.branchnodes = branchnodes;

		this.currentNode = new ArrayList<>();
		currentNode.add(currentNodeID);
		
		solutionNumber = 1;
		
		}//end solutionKey

	public void addNodeID(int[] id){
		this.currentNode.add(id);
	}

}//end solutionKey
