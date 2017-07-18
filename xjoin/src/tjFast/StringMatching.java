package tjFast;

import java.util.*;
import java.io.*;


public class StringMatching  {
	
	
	//����һ��ȫ�ֿ��Ʊ������ڵݹ����õ��������ս����
	static int result [];
	
	//����һ��ȫ�ֿ��Ʊ����õ����ս����
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
		
		result =  new int [decomposedQuery.size()]; //result��һ��ȫ�ֱ�������Ž��
		 
		 for (int i=0;i<solutionArray [0].size();i++)
			showSolutions (0,i,solutionArray,decomposedQuery,text.length);
 
	}//end StringMatching


	static void showSolutions (int index,int position,Vector solutionArray [], Vector decomposedQuery,int textlength){
		
		result[index]=((Integer)solutionArray[index].elementAt(position)).intValue();
		
		if(index== decomposedQuery.size()-1)
			{	//���¼��һ�����һ��Ԫ���Ƿ����ƥ��
				int [] lastsubpattern  = (int []) decomposedQuery.lastElement();
				
				if (  (lastsubpattern.length + result[decomposedQuery.size()-1]) != textlength )
					return;
				
			 	//���¿���ֵ
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

	// ���������� -1 ���� wildcard "*"
	// ���������� -2 ���� AD ��ϵ 
	
	//�ֽ�pattern�� ʹÿ����pattern������ AD ��ϵ �� -2
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
		
		//���²���ת��query�ı�����ʽ
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

	
	 static public void main(String[] args) {
	 	
	 	// ���������� -1 ���� wildcard "*"
		// ���������� -2 ���� AD ��ϵ 
	
	 	int pattern [] = {1,-2,2,-1,3};
	 	int text [] = {1,1,2,4,3,3};
     		
     		StringMatching.performStringMatching(text , StringMatching.decomposePattern(pattern));
     		
              }//end main

}//end StringMatching