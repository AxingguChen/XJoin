package tjFast;

import java.util.*;
import java.io.*;


public class mergeAllPathSolutions  {
		
	static List<List<String>> solutionPairIDList = new ArrayList<>();
	static List<List<String>> solutionDoubleLayerIDList = new ArrayList<>();
	//store the solution of b,c,[d,e]. branchPair[0] = solution of b. branchPair[1] = solution of c
	static Vector [] branchPair = new Vector[2];
	//tag d,e
	static Vector buttonLeaves1 = new Vector();
	static Vector buttonLeaves2 = new Vector();
	//tag c
	static Vector branchUnderRoot = new Vector();
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




	static int mergeTwoBranchs(Hashtable pathsolutions){

		String [] branches = Query.getBranchNode();//branches[0] is ganranteed to be an ancestor node

		Vector branchleaves = Query.getBranchLeaves(branches[1]);

		Vector allLeaves = Query.getLeaves();

		//the tag name of single leave node of root
		String singleLeave = Query.getRootSingleChild();


		int numberOfleaves = branchleaves.size();

		Vector [] data = new Vector [numberOfleaves];

		for(int i=0;i<numberOfleaves;i++)
		{ 	String s = (String)branchleaves.elementAt(i);
			Vector v =(Vector)pathsolutions.get(s);
			data[i] = v;
		}//end for

		// solutions of single leave:b
		branchPair = new Vector[2];
		buttonLeaves1.clear();
		buttonLeaves2.clear();
		buttonLeaves2.clear();
		branchPair[0] = (Vector)pathsolutions.get(singleLeave);

		int [] pointer = new int [numberOfleaves];
		merge_double (data,pointer,1); //first merge nodes for the second branch node
		//branchPair[1] = branchUnderRoot;
		branchPair[1] = buttonLeaves1;

		//branchPair[3] = buttonLeaves2;
//		mergeOneBranch_naiveDouble(pathsolutions);
		merge_double (branchPair,new int [2],0);
		//return id pair list
		return solutionCount;


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

		Vector allLeaves = Query.getLeaves();

		//the tag name of single leave node of root
		String singleLeave = Query.getRootSingleChild();


		int numberOfleaves = branchleaves.size();

		Vector [] data = new Vector [numberOfleaves];

		for(int i=0;i<numberOfleaves;i++)
		{ 	String s = (String)branchleaves.elementAt(i);
			Vector v =(Vector)pathsolutions.get(s);
			data[i] = v;
		}//end for

		// solutions of single leave:b
		branchPair[0] = (Vector)pathsolutions.get(singleLeave);

		int [] pointer = new int [numberOfleaves];
		merge_naiveDouble (data,pointer,1); //first merge nodes for the second branch node
		//branchPair[1] = branchUnderRoot;
		branchPair[1] = buttonLeaves1;
		//branchPair[3] = buttonLeaves2;
//		mergeOneBranch_naiveDouble(pathsolutions);
		merge_naiveDouble (branchPair,new int [2],0);
		//return id pair list
		return solutionPairIDList;

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
		if(branchref == 1) {
			while (!endAll(data, pointer)) {

				if (!equals_naiveDouble(data, pointer, branchref)) {
					int min = mindata(data, pointer, branchref);
					if (!isCommonOne(data[min], pointer[min], lastSameValue, branchref))
						data[min].removeElementAt(pointer[min]);
					else
						pointer[min]++;

				}//end if
				else {
					Vector v = ((solutionKey) data[0].elementAt(pointer[0])).branchnodes;
					lastSameValue = (int[]) v.elementAt(branchref);
					for (int i = 0; i < pointer.length; i++)
						pointer[i]++;
				}//end else

			}//end while
		}
		else if(branchref == 0){
			while (!endAll(data, pointer)) {
				if (!equals_naiveDouble(data, pointer, branchref)) {
					int min = mindata(data, pointer, branchref);
					if (!isCommonOne(data[min], pointer[min], lastSameValue, branchref))
						data[min].removeElementAt(pointer[min]);
					else
						pointer[min]++;
				}//end if
				else {
					Vector v = ((solutionKey) data[0].elementAt(pointer[0])).branchnodes;
					lastSameValue = (int[]) v.elementAt(branchref);
					for (int i = 1; i < pointer.length; i++)
						pointer[i]++;
				}//end else
			}
		}

	}//end merge

	static void merge_double( Vector [] data, int [] pointer, int branchref){ //since we have at most two branch nodes, branch ref is either 0 or 1

		int [] lastSameValue = new int [1];
		if(branchref == 1) {
			while (!endAll(data, pointer)) {

				if (!equals_double(data, pointer, branchref)) {
					int min = mindata(data, pointer, branchref);
					if (!isCommonOne(data[min], pointer[min], lastSameValue, branchref))
						data[min].removeElementAt(pointer[min]);
					else
						pointer[min]++;

				}//end if
				else {
					Vector v = ((solutionKey) data[0].elementAt(pointer[0])).branchnodes;
					lastSameValue = (int[]) v.elementAt(branchref);
					for (int i = 0; i < pointer.length; i++)
						pointer[i]++;
				}//end else

			}//end while
		}
		else if(branchref == 0){
			while (!endAll(data, pointer)) {

				if (!equals_double(data, pointer, branchref)) {
					int min = mindata(data, pointer, branchref);
					if (!isCommonOne(data[min], pointer[min], lastSameValue, branchref))
						data[min].removeElementAt(pointer[min]);
					else
						pointer[min]++;
				}//end if
				else {
					Vector v = ((solutionKey) data[0].elementAt(pointer[0])).branchnodes;
					lastSameValue = (int[]) v.elementAt(branchref);
					for (int i = 1; i < pointer.length; i++)
						pointer[i]++;
				}//end else
			}
		}

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
//						BufferedWriter out = new BufferedWriter(new FileWriter("xjoin/src/xjoinSingleLayerSolutionCount.txt",true));
//						out.write(solutionCount+". "+utilities.ArrayToString(c)+" "+utilities.ArrayToString(p)+"\r\n");  //Replace with the string
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
						//convert ID int array to string
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

	static boolean equals_double(Vector [] data,int [] pointer, int branchref){
		//@@@
		for(int i=0;i<data.length;i++)
			if (data[i].size() == pointer[i])
				return  false;

		Vector v0 =((solutionKey)data[0].elementAt(pointer[0])).branchnodes;

		int [] common = (int [])v0.elementAt(branchref);

		List<int []> currentNodeIDList = ((solutionKey)data[0].elementAt(pointer[0])).currentNode;

		if(branchref == 1){
			for(int i=1;i<data.length;i++)
			{
				//for(int j=pointer[i];j<data[i].size();j++) {
				Vector v = ((solutionKey) data[i].elementAt(pointer[i])).branchnodes;

				int[] datakey = (int[]) v.elementAt(branchref);

				if (utilities.isEqual(datakey, common)) {
					List<int[]> pairNodeIdList = ((solutionKey) data[i].elementAt(pointer[i])).currentNode;

					for (int[] c : currentNodeIDList) {
						for (int[] p : pairNodeIdList) {
							List<String> pairIDList = new ArrayList<>();
							buttonLeaves1.add(data[0].elementAt(pointer[0]));//tag d
							buttonLeaves2.add(p);//tag e

						}
					}
				} else
					return false;

			}}//}//end for
		else if(branchref == 0){

			for(int i=1;i<data.length;i++)
			{
				//for(int j=pointer[i];j<data[i].size();j++) {
				//branch nodes of compare node
				Vector v = ((solutionKey) data[i].elementAt(pointer[i])).branchnodes;

				int[] datakey = (int[]) v.elementAt(branchref);

				if (utilities.isEqual(datakey, common)) {
					List<int[]> pairNodeIdList = ((solutionKey) data[i].elementAt(pointer[i])).currentNode;

					for (int[] c : currentNodeIDList) {
						for (int[] p : pairNodeIdList) {
							solutionCount++;
							try{
								BufferedWriter out = new BufferedWriter(new FileWriter("xjoin/src/xjoinDoubleLayerResultCount100.txt",true));
								out.write(solutionCount+". "+utilities.ArrayToString(c)+","+utilities.ArrayToString(p)+"\r\n");  //Replace with the string
								//you are trying to write
								out.close();
							}
							catch (IOException e)
							{
								System.out.println("Exception ");

							}
						}}

				} else
					return false;

			}}

		return true;

	}//end equals

	static boolean equals_naiveDouble(Vector [] data,int [] pointer, int branchref){
		//@@@
		for(int i=0;i<data.length;i++)
			if (data[i].size() == pointer[i])
				return  false;

		Vector v0 =((solutionKey)data[0].elementAt(pointer[0])).branchnodes;

		int [] common = (int [])v0.elementAt(branchref);

		List<int []> currentNodeIDList = ((solutionKey)data[0].elementAt(pointer[0])).currentNode;
		String currentNode = ((solutionKey)data[0].elementAt(pointer[0])).leaf;

		if(branchref == 1){
		for(int i=1;i<data.length;i++)
		{
			//for(int j=pointer[i];j<data[i].size();j++) {
				Vector v = ((solutionKey) data[i].elementAt(pointer[i])).branchnodes;

				int[] datakey = (int[]) v.elementAt(branchref);

				if (utilities.isEqual(datakey, common)) {
					List<int[]> pairNodeIdList = ((solutionKey) data[i].elementAt(pointer[i])).currentNode;

					for (int[] c : currentNodeIDList) {
						for (int[] p : pairNodeIdList) {
							List<String> pairIDList = new ArrayList<>();
							buttonLeaves1.add(data[0].elementAt(pointer[0]));//tag d
							buttonLeaves2.add(p);//tag e
//							try {
//								BufferedWriter out = new BufferedWriter(new FileWriter("xjoin/src/tjFastPairResult.txt", true));
//								out.write(utilities.ArrayToString(common)+","+utilities.ArrayToString(c)+","+utilities.ArrayToString(p) + "\r\n");  //Replace with the string
//								//you are trying to write
//								out.close();
//							} catch (IOException e) {
//								System.out.println("Exception ");
//
//							}

						}
					}
				} else
					return false;

			}}//}//end for
			else if(branchref == 0){

			for(int i=1;i<data.length;i++)
			{
				//for(int j=pointer[i];j<data[i].size();j++) {
				//branch nodes of compare node
				Vector v = ((solutionKey) data[i].elementAt(pointer[i])).branchnodes;

				int[] datakey = (int[]) v.elementAt(branchref);

				if (utilities.isEqual(datakey, common)) {
					List<int[]> pairNodeIdList = ((solutionKey) data[i].elementAt(pointer[i])).currentNode;

					for (int[] c : currentNodeIDList) {
						for (int[] p : pairNodeIdList) {
								List<String> pairIDList = new ArrayList<>();
								pairIDList.add(utilities.ArrayToString(p));
								pairIDList.add(utilities.ArrayToString((int[]) buttonLeaves2.get(pointer[i])));
								//the next line is to add the solution id of b to solutionList. Since the rdb table does not contains
								pairIDList.add(utilities.ArrayToString(c));

								solutionPairIDList.add(pairIDList);
								solutionPairCount++;
//								try {
//									BufferedWriter out = new BufferedWriter(new FileWriter("xjoin/src/NAIVEtjFastPairFinalResult.txt", true));
//									out.write(pairIDList + "\r\n");  //Replace with the string
//									//you are trying to write
//									out.close();
//								} catch (IOException e) {
//									System.out.println("Exception ");
//
//								}
						}}
					} else
						return false;

				}}

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