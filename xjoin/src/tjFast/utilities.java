package tjFast;

import java.util.*;

public class utilities { 
	
	static boolean  Debug = false;
	
	static final int MAXNUM = 200000000;
	
	static void printStringVector (Vector v,String promote){
		
		if  ((v==null ) || (v.size() == 0 )) return ;
		
		System.out.print(promote+":  " );
		for (int i=0;i<v.size();i++)
			System.out.print((String)v.elementAt(i)+" ");
		System.out.println("Finished! ");
		
		}//end printStringVector
	
	
	static void printStringIntegerHashtable (Hashtable h,String promote){
		
		if  ((h==null ) || (h.size() == 0 )) return ;
		
		System.out.print(promote+":  " );
		
		Enumeration e = h.keys();
		while (e.hasMoreElements())
			{	String tag = (String) e.nextElement();
				Vector v = (Vector)h.get(tag);
				printIntegerVector (v,"Tag name is "+tag);
				}//end while
			
		System.out.println("Finished! ");
		
	}//end printStringVector
	
	static void printIntegerVector (Vector v,String promote){
		
		if  ((v==null ) || (v.size() == 0 )) return ;
		
		System.out.print(promote+":  " );
		for (int i=0;i<v.size();i++)
			System.out.print(((Integer)v.elementAt(i)).intValue()+" ");
		System.out.println("Finished! ");
		
	}//end printStringVector
	
	static String printIntArrayVector (Vector v,String promote){
		String result = "";
		if  ((v !=null ) && (v.size() != 0 )){
		
		//System.out.print(promote+":  " );
		for (int i=0;i<v.size();i++)
			{ 	//System.out.print( " NO."+i+":  ");
				for(int j=0; j< ((int [])v.elementAt(i)).length; j++)
				{	//System.out.print(((int [])v.elementAt(i))[j]+" ");
					result = result + "/"+((int [])v.elementAt(i))[j];
				}
			}//end 	for
		//System.out.println();
		}
		return result;
		
	}//end printStringVector
	
	static void DebugPrintIntArray (int [] s,String promote){
		
		if  (s==null ) return ;
		
		if (Debug){
		System.out.print(promote+":  " );
		for (int i=0;i<s.length;i++)
			System.out.print(s[i]+" ");
		System.out.println("Finished! ");
	}//end if
	
	}//end DebugPrintIntArray
	
	static void PrintIntArray (int [] s,String promote){
		System.out.print(promote+":  " );
		for (int i=0;i<s.length;i++)
			System.out.print(s[i]+" ");
		System.out.print("  " );
	}//end PrintIntArray

	static void PrintIntArrayList(List<int[]> list,String promote){
		System.out.print(promote+":  " );
		for(int[] l:list){
			for (int i=0;i<l.length;i++)
				System.out.print(l[i]+" ");
			System.out.print(",");
		}
	}

	static void PrintPair(List<int[]> c_list,String currentNode, List<int[]> p_list, String pairNode){
		int pairCount = 0;
		for(int[] c_l:c_list){
			for(int[] p_l:p_list){
				PrintIntArray(c_l,currentNode);
				PrintIntArray(p_l,pairNode);
				pairCount ++;
				System.out.println();
			}
		}
		System.out.println("["+pairCount+" pair]. ");
	}

	static void DebugPrint (String s){
		
		if (Debug)
		System.out.print(s);
		
	}//end printStringVector
	
	static void DebugPrintln (String s){
		if (Debug)
		System.out.println(s);
		
		}//end printStringVector
    
    //�ҵ�һ������int [] label.
    	static int [] maxLabel (Vector v){
    		if (v==null)
    			{ System.out.println("Input vector is null in maxLabel function !!!"); return null ;}
    		
    		int [] max = (int [])v.elementAt(0);
    		
    		for(int i=1;i<v.size();i++)
    			{	int [] temp = (int [])v.elementAt(i);
    				if ( greater(temp,max,0))
    					max =temp;
    				}//end for
    		
    		return max;
    		
    	}//end maxLabel
    	
    	static int [] MaxMinLabelPosition(Vector v) // int[0] is max position and int[1] is min position
  	{
  		if (v==null)
    			{ System.out.println("Input vector is null in MaxMinLabelPosition function !!!"); return null ;}
    		
    		int [] max = (int [])v.elementAt(0);
    		int [] min = (int [])v.elementAt(0);
    		int maxposition = 0;
    		int minposition = 0;
    		
    		for(int i=1;i<v.size();i++)
    			{	int [] temp = (int [])v.elementAt(i);
    				
    				if ( greater(temp,max,0))
    					{ max =temp; maxposition = i; }
    				if ( greater(min,temp,0))
    					{ min =temp; minposition = i; }
    					
    				}//end for
    		
  		int result [] = new int [2];
  		
  		result[0] = maxposition ;
  		result[1] = minposition ;
  		
  		return result;
  	
	}//end MaxMinLabelPosition


	/*static boolean greater (int [] v1, int [] v2, int pointer){ // compare two strings according to lexicography order
		
		if (pointer == v1.length) return false;
		
		if (pointer == v2.length) return true;
		
		if (v1[pointer] > v2[pointer]) return true;
		
		if (v1[pointer] < v2[pointer]) return false;
		
		return greater(v1,v2,++pointer);
				
		
    	}//end greater
    */
    	
    	static boolean greater (int [] v1, int [] v2,int temp){ // compare two strings according to lexicography order
		
		
		for(int pointer=0;pointer<v1.length;pointer++){
			
			if (v1[pointer] > v2[pointer]) return true;
		
		  else if (v1[pointer] < v2[pointer]) return false;
		  
		  else if (pointer==(v2.length-1)) return true;
		
		}//end for
		
		
		return false;
				
		
    	}//end greater
    	
    	static boolean isPrefix (int [] shorter, int [] longer, int pointer){ //һ��ʼ����,pointer����Ϊ0
		
		if (pointer == shorter.length) return true;
		
		if (pointer == longer.length) return false;
		
		if (shorter[pointer] != longer[pointer]) return false;
		
		return isPrefix(shorter,longer,++pointer);
		
    	}//end greater
    	
    	static boolean isEqual (int [] v1, int [] v2){
		
		if (v1.length != v2.length ) return false;
		
		for(int i=0;i<v1.length;i++)
			if (v1[i] != v2[i])
				return false;
		
		return true;
		
    	}//end greater
    	
    	static List<String> DebugPrintSolutionlist (String leaf, Hashtable  finalResults ){
		List<String> branchIDList = new ArrayList<>();
		if  (finalResults!=null )
		{
		
		Vector v = (Vector)finalResults.get(leaf);


		for(int i=0;i<v.size();i++)
			{ 
			//System.out.print("The "+i+"'th solution ");
			solutionKey solution = (solutionKey)v.elementAt(i);
			Vector branches = solution.branchnodes;
			String branch = solution.branch;
			String leafs = solution.leaf;
			//System.out.print(" branch: "+branch);


		
			}//end for
		
	}//end if
			return branchIDList;
	
	}//end DebugPrintSolutionlist
    	
    	 static public void main(String[] args) {
	 	
	 	int v1 [] = {2,6,3};
	 	int v2 [] = {2,6,3};
     		int v3 [] = {2};
     		
     		Vector v =new Vector();
     		
     		v.addElement(v1);
     		v.addElement(v2);
     		v.addElement(v3);
     		
     		int [] result =MaxMinLabelPosition(v);
     		//DebugPrintln(" "+(result[0]+1));
     		//DebugPrintln(" "+(result[1]+1));
     		
     		DebugPrintln(" "+isEqual ( v2, v1));
     		
              }//end main
}//end class