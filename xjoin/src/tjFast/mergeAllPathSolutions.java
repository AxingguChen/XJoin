package tjFast;

import java.util.*;
import java.io.*;


public class mergeAllPathSolutions  {
		
	static List<List<String>> solutionPairIDList = new ArrayList<>();
	static List<List<String>> solutionDoubleLayerIDList = new ArrayList<>();
	static int solutionPairCount = 0;
	static int getPathNumber(Hashtable finalResults,Vector leaves){
		
		int numberOfleaves = leaves.size();
		int pathnumber = 0;
		
		for(int i=0;i<numberOfleaves;i++)
		{ 	String s = (String)leaves.elementAt(i);
		 	Vector v =(Vector)finalResults.get(s);
		 	for(int j=0;j<v.size();j++)
			{ 
				int n =((solutionKey)v.elementAt(j)).solutionNumber ;
		 		pathnumber += n;
		 	}//end for
		}//end for
		
		return pathnumber;
		
	}//end mergeOneBranch
	static int solutionCount=0;
	static int mergeOneBranch(Hashtable pathsolutions){
		//solutionCount = 0;
		Vector leaves =	Query.getLeaves();

		int numberOfleaves = leaves.size();
		Vector [] data = new Vector [numberOfleaves];
		for(int i=0;i<numberOfleaves;i++)
		{ 	String s = (String)leaves.elementAt(i);
			Vector v =(Vector)pathsolutions.get(s);
			data[i] = v;
		}//end for

		int [] pointer = new int [numberOfleaves];
		merge (data,pointer,0);
		return solutionCount;
	}//end mergeOneBranch




	static void mergeTwoBranchs(Hashtable pathsolutions){

		String [] branches = Query.getBranchNode();//branches[0] is ganranteed to be an ancestor node

		Vector branchleaves = Query.getBranchLeaves(branches[1]);

		int numberOfleaves = branchleaves.size();

		Vector [] data = new Vector [numberOfleaves];

		for(int i=0;i<numberOfleaves;i++)
		{ 	String s = (String)branchleaves.elementAt(i);
			Vector v =(Vector)pathsolutions.get(s);
			data[i] = v;
		}//end for

		int [] pointer = new int [numberOfleaves];
		merge (data,pointer,1); //first merge nodes for the second branch node

		mergeOneBranch(pathsolutions);


	}//end mergeTwoBranch
		
	static List<List<String>> mergeOneBranch_naive(Hashtable pathsolutions){
		
		Vector leaves =	Query.getLeaves();
			
		int numberOfleaves = leaves.size();
		Vector [] data = new Vector [numberOfleaves];
		for(int i=0;i<numberOfleaves;i++)
		{ 	String s = (String)leaves.elementAt(i);
		 	Vector v =(Vector)pathsolutions.get(s);
		 	data[i] = v;
		}//end for
		
		int [] pointer = new int [numberOfleaves];
		merge_naive (data,pointer,0);


		//return id pair list
		return solutionPairIDList;

	}//end mergeOneBranch



	static List<List<String>> mergeTwoBranchs_naive(Hashtable pathsolutions){
			
			
		String [] branches = Query.getBranchNode();//branches[0] is ganranteed to be an ancestor node
		
		Vector branchleaves = Query.getBranchLeaves(branches[1]);
		
		int numberOfleaves = branchleaves.size();
		
		Vector [] data = new Vector [numberOfleaves];
		
		for(int i=0;i<numberOfleaves;i++)
		{ 	String s = (String)branchleaves.elementAt(i);
		 	Vector v =(Vector)pathsolutions.get(s);
		 	data[i] = v;
		}//end for
		
		int [] pointer = new int [numberOfleaves];
		merge_naive (data,pointer,1); //first merge nodes for the second branch node
		
		mergeOneBranch_naive(pathsolutions);

		//return id pair list
		return solutionPairIDList;

	}//end mergeTwoBranch


	static void mergeOneBranch_naiveDouble(Hashtable pathsolutions){

		Vector leaves =	Query.getLeaves();

		int numberOfleaves = leaves.size();
		Vector [] data = new Vector [numberOfleaves];
		for(int i=0;i<numberOfleaves;i++)
		{ 	String s = (String)leaves.elementAt(i);
			Vector v =(Vector)pathsolutions.get(s);
			data[i] = v;
		}//end for

		int [] pointer = new int [numberOfleaves];
		merge_naiveDouble (data,pointer,0);

	}//end mergeOneBranch



	static List<List<String>> mergeTwoBranchs_naiveDouble(Hashtable pathsolutions){


		String [] branches = Query.getBranchNode();//branches[0] is ganranteed to be an ancestor node

		Vector branchleaves = Query.getBranchLeaves(branches[1]);

		int numberOfleaves = branchleaves.size();

		Vector [] data = new Vector [numberOfleaves];

		for(int i=0;i<numberOfleaves;i++)
		{ 	String s = (String)branchleaves.elementAt(i);
			Vector v =(Vector)pathsolutions.get(s);
			data[i] = v;
		}//end for

		int [] pointer = new int [numberOfleaves];
		merge_naiveDouble (data,pointer,1); //first merge nodes for the second branch node
		mergeOneBranch_naiveDouble(pathsolutions);

		//return id pair list
		return solutionDoubleLayerIDList;

	}//end mergeTwoBranch

	static void merge( Vector [] data, int [] pointer, int branchref){ //since we have at most two branch nodes, branch ref is either 0 or 1

		int [] lastSameValue = new int [1];

		while (!endAll(data,pointer)){

			if (!equals(data,pointer,branchref)){
				int min = mindata (data,pointer,branchref);
				if ( !isCommonOne(data[min],pointer[min],lastSameValue, branchref))
					data[min].removeElementAt(pointer[min]);
				else
					pointer[min]++;

			}//end if
			else{	Vector v = ((solutionKey)data[0].elementAt(pointer[0])).branchnodes;
				lastSameValue = (int [])v.elementAt(branchref);
				for(int i=0;i<pointer.length;i++)
					pointer[i]++;
			}//end else

		}//end while


	}//end merge



	static void merge_naive( Vector [] data, int [] pointer, int branchref){ //since we have at most two branch nodes, branch ref is either 0 or 1

		int [] lastSameValue = new int [1];

		while (!endAll(data,pointer)){

			if (!equals_naive(data,pointer,branchref)){
				int min = mindata (data,pointer,branchref);
				if ( !isCommonOne(data[min],pointer[min],lastSameValue, branchref))
					data[min].removeElementAt(pointer[min]);
				else
					pointer[min]++;

			}//end if
			else{	Vector v = ((solutionKey)data[0].elementAt(pointer[0])).branchnodes;
				lastSameValue = (int [])v.elementAt(branchref);
				for(int i=0;i<pointer.length;i++)
					pointer[i]++;
			}//end else

		}//end while


	}//end merge

	static void merge_naiveDouble( Vector [] data, int [] pointer, int branchref){ //since we have at most two branch nodes, branch ref is either 0 or 1

		int [] lastSameValue = new int [1];

		while (!endAll(data,pointer)){

			if (!equals_naiveDouble(data,pointer,branchref)){
				int min = mindata (data,pointer,branchref);
				if ( !isCommonOne(data[min],pointer[min],lastSameValue, branchref))
					data[min].removeElementAt(pointer[min]);
				else
					pointer[min]++;

			}//end if
			else{	Vector v = ((solutionKey)data[0].elementAt(pointer[0])).branchnodes;
				lastSameValue = (int [])v.elementAt(branchref);
				for(int i=0;i<pointer.length;i++)
					pointer[i]++;
			}//end else

		}//end while


	}//end merge

	static boolean equals(Vector [] data,int [] pointer, int branchref){

		for(int i=0;i<data.length;i++)
			if (data[i].size() == pointer[i])
				return  false;

		Vector v0 =((solutionKey)data[0].elementAt(pointer[0])).branchnodes;

		int [] common = (int [])v0.elementAt(branchref);

		List<int []> currentNodeIDList = ((solutionKey)data[0].elementAt(pointer[0])).currentNode;
		for(int i=1;i<data.length;i++)
		{
			Vector v =((solutionKey)data[i].elementAt(pointer[i])).branchnodes;

			int [] datakey = (int [])v.elementAt(branchref);

			if (utilities.isEqual (datakey, common)){
				List<int []> pairNodeIdList = ((solutionKey)data[i].elementAt(pointer[i])).currentNode;
				for(int[] c:currentNodeIDList){
					for(int[] p:pairNodeIdList){
//						utilities.PrintIntArray(c,"current node");
//						utilities.PrintIntArray(p,"pair node");
						solutionCount++;
//						try{
//						BufferedWriter out = new BufferedWriter(new FileWriter("xjoin/src/xjoinResult.txt",true));
//						out.write(utilities.ArrayToString(c)+" "+utilities.ArrayToString(p)+"\r\n");  //Replace with the string
//						//you are trying to write
//						out.close();
//						}
//            			catch (IOException e)
//						{
//							System.out.println("Exception ");
//
//						}
					}
				}
			}
			else return false;

		}//end for

		return true;



	}//end equals

	static boolean equals_naive(Vector [] data,int [] pointer, int branchref){

		for(int i=0;i<data.length;i++)
			if (data[i].size() == pointer[i])
				return  false;

		Vector v0 =((solutionKey)data[0].elementAt(pointer[0])).branchnodes;

		int [] common = (int [])v0.elementAt(branchref);

		List<int []> currentNodeIDList = ((solutionKey)data[0].elementAt(pointer[0])).currentNode;
		String currentNode = ((solutionKey)data[0].elementAt(pointer[0])).leaf;

		for(int i=1;i<data.length;i++)
		{
			Vector v =((solutionKey)data[i].elementAt(pointer[i])).branchnodes;

			int [] datakey = (int [])v.elementAt(branchref);

			if (utilities.isEqual (datakey, common)) {
				//System.out.println("pair:");
				//String pairNode = ((solutionKey)data[i].elementAt(pointer[i])).leaf;
				List<int []> pairNodeIdList = ((solutionKey)data[i].elementAt(pointer[i])).currentNode;
				//utilities.PrintPair(currentNodeIDList, currentNode,pairNodeIdList,pairNode);


				for(int[] c:currentNodeIDList){
					for(int[] p:pairNodeIdList){
						List<String> pairIDList = new ArrayList<>();
						pairIDList.add(utilities.ArrayToString(c));
						pairIDList.add(utilities.ArrayToString(p));
						solutionPairIDList.add(pairIDList);
					}
				}
			}
			else
				return false;

		}//end for

		return true;



	}//end equals

	static boolean equals_naiveDouble(Vector [] data,int [] pointer, int branchref){

		for(int i=0;i<data.length;i++)
			if (data[i].size() == pointer[i])
				return  false;

		Vector v0 =((solutionKey)data[0].elementAt(pointer[0])).branchnodes;

		int [] common = (int [])v0.elementAt(branchref);

		List<int []> currentNodeIDList = ((solutionKey)data[0].elementAt(pointer[0])).currentNode;
		String currentNode = ((solutionKey)data[0].elementAt(pointer[0])).leaf;

		for(int i=1;i<data.length;i++)
		{
			Vector v =((solutionKey)data[i].elementAt(pointer[i])).branchnodes;

			int [] datakey = (int [])v.elementAt(branchref);

			if (utilities.isEqual (datakey, common)) {
				//System.out.println("pair:");
				//String pairNode = ((solutionKey)data[i].elementAt(pointer[i])).leaf;
				List<int []> pairNodeIdList = ((solutionKey)data[i].elementAt(pointer[i])).currentNode;
				//utilities.PrintPair(currentNodeIDList, currentNode,pairNodeIdList,pairNode);


				for(int[] c:currentNodeIDList){
					for(int[] p:pairNodeIdList){
						List<String> pairIDList = new ArrayList<>();
						pairIDList.add(utilities.ArrayToString(c));
						pairIDList.add(utilities.ArrayToString(p));
						solutionPairIDList.add(pairIDList);
						solutionPairCount++;
						if(solutionPairCount>1){
							String previousID = solutionPairIDList.get(solutionPairCount-2).get(0);
							String previousPairID = solutionPairIDList.get(solutionPairCount-2).get(1);
							String currentID = solutionPairIDList.get(solutionPairCount-1).get(0);
							String currentPairID = solutionPairIDList.get(solutionPairCount-1).get(1);
							if(previousID.equals(currentID) && (! previousPairID.equals(currentPairID))){
								solutionDoubleLayerIDList.add(Arrays.asList(currentID,previousPairID,currentPairID));
							}
							else if (previousPairID.equals(currentPairID) && (! previousID.equals(currentID))){
								solutionDoubleLayerIDList.add(Arrays.asList(currentPairID,previousID,currentID));
							}

						}

					}
				}
			}
			else
				return false;

		}//end for

		return true;



	}//end equals

	static boolean  endAll(Vector [] data, int [] pointer){
		
		for(int i=0;i<data.length;i++)
			if (data[i].size() > pointer[i])
				return  false;
		
		return true;	
		
	}//end endAll
	
	static int mindata (Vector [] data, int [] pointer, int branchref){
		
		int [] minkey = new int [1];
		minkey[0] = utilities.MAXNUM;
		
		int minposition =0;
		
		for(int i=0;i<data.length;i++)
			if (data[i].size() > pointer[i]) //this shows it does not end
				{int [] temp = (int [])(((solutionKey)data[i].elementAt(pointer[i])).branchnodes).elementAt(branchref);
				if ( smallerPostOrder(temp,minkey))
				{minposition=i; minkey = temp;}
			}//end if
			
			
		return minposition;
		
	}//end mindata
	
	static boolean smallerPostOrder(int [] s1,int [] s2){
		
		if (utilities.isPrefix (s1, s2, 0))
			return false;
			
		if (utilities.isPrefix (s2, s1, 0))
			return true;
			
		if (utilities.greater (s1, s2, 0))
			return false;
		else
			return true;
		
	}//end smallerPostOrder
	
	
	static boolean isCommonOne(Vector data , int pointerref,int [] common, int branchref){
		
		Vector v =((solutionKey)data.elementAt(pointerref)).branchnodes;
		
		int [] datakey = (int [])v.elementAt(branchref);
		
		if (  utilities.isEqual (datakey, common))
			return true;
		else
			return false;
		
		}//end isCommonOne
	
}//end  class