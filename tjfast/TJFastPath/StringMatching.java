import java.util.*;
import java.io.*;


public class StringMatching  {
	
	
	//这是一个全局控制变量用于递归程序得到部分最终结果。
	static int result [];
	
	//这是一个全局控制变量得到最终结果。
	static Vector results ;

	static void  performStringMatching (int [] text, Vector decomposedQuery ){
		
		results = new Vector();
		
		Vector [] solutionArray  = new Vector [decomposedQuery.size()];
		
		for(int i=0;i<decomposedQuery.size();i++)
		{	int [] smallQuery = (int [])decomposedQuery.elementAt(i);
			if (canMatch(smallQuery,text))
				solutionArray [i] =  matchResult(smallQuery, text);
			else return ;
		}//end for	
		
		result =  new int [decomposedQuery.size()]; //result是一个全局变量，存放结果
		 
		 for (int i=0;i<solutionArray [0].size();i++)
			showSolutions (0,i,solutionArray,decomposedQuery,text.length);
 
	}//end StringMatching
	
	static int []  performApproximateMatching (int [] originaltext, int [] tagtext, Vector decomposedQuery ,String leave){ //这里主要完成近似匹配
		// 返回null 表示 不能近似匹配，否则返回匹配BranchNode的相应前缀；
		
		//第一检查最后一个的分量是否匹配
		
		int [] lastQuery = (int [])decomposedQuery.elementAt(decomposedQuery.size()-1);
			
		//utilities.PrintIntArray(lastQuery," lastQuery is ");
   //	utilities.PrintIntArray(originaltext,"originaltext is ");
  // utilities.PrintIntArray(tagtext," tagtext is ");
   //System.out.println(" leave is "+leave);
	// System.out.println(" tagtext 0 is "+tagtext[0]);
	 
	  for(int i=0;i<lastQuery.length;i++)
		if ( ! (  (lastQuery[i]==tagtext[i+(tagtext.length-lastQuery.length)])  ||  lastQuery[i]== -1 )) // -1 表示wildcards 在query中
			return null; // 返回null 表示最后一个的分量能够匹配
				
		//第二检查是否query每个分量是否都出现在pattern中
		
	  int [] queryNames = Query.getIgnoreWildcardQuery(leave);
	//utilities.PrintIntArray(queryNames," query path is ");
 // utilities.PrintIntArray(tagtext," tagtext is ");
   	
	  int queryPointer = 0;
	  
	  String branch = Query.getBranchNode()[0];
	  // 注意：这里假设只有一个branching node!!!
	  
	  int firstBranchPosition = -1;//这个变量主要要来记录最早的Branch的位置，用来返回一个可能匹配的前缀
	  
	  for(int j=0;j<tagtext.length;j++)
	  		{if (tagtext[j]==queryNames[queryPointer])
	  			{
	  			
	  			if (firstBranchPosition==-1)
	  			 if (queryNames[queryPointer]==Query.getBranchNodeInt()[0]) //这里假设只有一个branch结点。
	  			 	firstBranchPosition=j;
	  			
	  			queryPointer++;
	  			
	  			if (queryPointer== queryNames.length-1)
	  						break; // 这表明该字符能够近似匹配，所以跳出
	  		 	}//endif
	  		}//end for
	  		
	  		
		if (queryPointer<queryNames.length-1) 
				return null; //表明不能近似匹配
		else{ //表明能近似匹配，返回一个匹配Branch Node的前缀
		int [] branchPrefix = new int [firstBranchPosition];
		
		for(int j=0;j<firstBranchPosition;j++) 
				branchPrefix[j]=originaltext[j];
		
		
		 	//utilities.PrintIntArray(text," text is ");
  
  		//utilities.PrintIntArray(branchPrefix," branchPrefix is ");
  		
		return branchPrefix;
	  }//end else
			
		
		
	 
	}//end performApproximateMatching

	static void showSolutions (int index,int position,Vector solutionArray [], Vector decomposedQuery,int textlength){
		
		result[index]=((Integer)solutionArray[index].elementAt(position)).intValue();
		
		if(index== decomposedQuery.size()-1)
			{	//以下检查一下最后一个元素是否参与匹配
				int [] lastsubpattern  = (int []) decomposedQuery.lastElement();
				
				if (  (lastsubpattern.length + result[decomposedQuery.size()-1]) != textlength )
					return;
				
			 	//以下拷贝值
			 	int temp [] = new int [decomposedQuery.size()];
			 	for(int i=0;i<decomposedQuery.size();i++) 
			 		temp[i] = result[i]; 
			 	results.addElement(temp);
			}//end if
		else
			for(int i=0;i<solutionArray[index+1].size();i++)
				if (   ((Integer)solutionArray[index+1].elementAt(i)).intValue() >= result[index]+((int [])decomposedQuery.elementAt(index)).length)
				showSolutions(index+1,i,solutionArray,decomposedQuery,textlength);
		
	}//end showSolutions

	// 我们用整数 -1 代表 wildcard "*"
	// 我们用整数 -2 代表 AD 关系 
	
	//分解pattern， 使每个子pattern不含有 AD 关系 即 -2
	 static Vector decomposePattern(int [] pattern){
		
		Vector  partQuery = new Vector ();
			
		int previousPosition = 0;
		
		
		for(int i=0;i< pattern.length; i++)
			if (pattern[i]== -2){
			Vector smallPart = new Vector ();
			for(int j=previousPosition;j<i;j++)
				smallPart.addElement(new Integer(pattern[j]));
			partQuery.addElement(smallPart);
			previousPosition = i+1;
			}//end if
		
		Vector smallPart = new Vector ();
				
		for(int j=previousPosition, k=0;j<pattern.length;j++,k++)
				smallPart.addElement(new Integer(pattern[j]));
		partQuery.addElement(smallPart);
		
		//以下步骤转化query的表现形式
		Vector returnQuery = new Vector(); 
		
		for(int i=0;i< partQuery.size(); i++)
		{	int [] temp = new int [((Vector)partQuery.elementAt(i)).size()];
			for(int j=0;j<((Vector)partQuery.elementAt(i)).size();j++)
				temp[j]=((Integer)((Vector)partQuery.elementAt(i)).elementAt(j)).intValue();
			returnQuery.addElement(temp);
			}//end for
		
		return returnQuery ;
		
	}//end decomposePattern
	

	 
		
	
	 static boolean  canMatch (int [] pattern, int [] text){
		
		boolean found = false;
		
		int i=0;
		
		while(i<=text.length-pattern.length){
			int partText [] =new int [pattern.length];
			for(int j=0;j<pattern.length;j++)
				partText [j] = text[i+j];
			
			if   (canMatchWithWildcards(pattern,partText))
				{found = true;
				break;}//end if		
			i++;
		
		}//end while
	
		return found;
		
		
	}//end  canMatch 
	
	
	 static boolean canMatchWithWildcards(int [] pattern,int [] partText){
			
		
		boolean canMatch =true;
		
		for(int i=0;i<pattern.length;i++)
		
		if ( ! (  (pattern[i]==partText[i])  ||  pattern[i]== -1 ))
			canMatch = false;
		
		return canMatch;
		
		
	}//end canMatchWithWildcards
	
	
	
	 static Vector  matchResult (int [] pattern, int [] text){
		
		Vector resultIndex = new Vector();
		
		int i=0;
		
		while(i<=text.length-pattern.length){
		
		int partText [] =new int [pattern.length];
		for(int j=0;j<pattern.length;j++)
				partText [j] = text[i+j];
			
		if   (canMatchWithWildcards(pattern,partText))
		{
			resultIndex.addElement(new Integer(i));
			}//end if
		
		i++;
		
		}//end while
	
		
		return resultIndex ;
		
	}//end  matchResult 
	
	 void printIntegerVector (Vector v){
		
		for(int i=0;i<v.size();i++)
		System.out.println(((Integer)v.elementAt(i)).intValue());
		
	}//end printIntegerVector 
		
	
	 static public void main(String[] args) {
	 	
	 	// 我们用整数 -1 代表 wildcard "*"
		// 我们用整数 -2 代表 AD 关系 
	
	 	int pattern [] = {1,-2,2,-1,3};
	 	int text [] = {1,1,2,4,3,3};
     		
     		StringMatching.performStringMatching(text , StringMatching.decomposePattern(pattern));
     		
              }//end main

}//end StringMatching