import java.util.*;
import java.io.*;


public class lazyMatching {

		
		static  void lazyProcess(String tag,Hashtable allOriginalData, Hashtable allTagData,Hashtable pathpatterns,Hashtable dataCursor,Hashtable setMinTable,Hashtable branchNextMatchElement){
			
			Vector Originaldata =  (Vector)allOriginalData.get(tag);

		 	Vector pat = (Vector)pathpatterns.get(tag);
		 	

      while (true){
      	
		 	int datacursor = ((Integer)dataCursor.get(tag)).intValue();
      
      if (datacursor==Originaldata.size())
      					break;
      					
		 	int Originaltext [] = (int [])Originaldata.elementAt(datacursor); //��һ��ȡ��current  Original Dewey
				 
			// ���¼���ֻ��һ��	branch �ڵ�
			if ( PrefixOfSet(setMinTable,Originaltext,Query.branch[0]) ) //��ǰ�Ľڵ��Ƿ��Ǽ����е���Сֵ��ǰ׺
						return ;
			
			if ( greatThanNextPossible(branchNextMatchElement,Originaltext,Query.branch[0],tag,allOriginalData,allTagData,pathpatterns, dataCursor)) //��ǰ�Ľڵ��Ƿ�����һ������ƥ�����Сֵ��ǰ׺
			   return;
			
			advanceStream(tag,dataCursor);
			//System.out.println(" skip string matching for "+tag);
			
		  }//end while
					
		}//end lazyProcess()
		
		static boolean PrefixOfSet(Hashtable setMinTable,int [] text, String branchNode){
			
			int [] minPrefix = (int [])setMinTable.get(branchNode);
			
			if (utilities.isPrefix(minPrefix,text,0))
					return true;
			else
					return false;
			
			
		}//end PrefixOfSet
		
		
		static boolean greatThanNextPossible(Hashtable branchNextMatchElement,int [] Originaltext,String branchNode, String tag,Hashtable allOriginalData,Hashtable allTagData,Hashtable pathpatterns,Hashtable dataCursor){
			
			int [] nextPossible = (int [])branchNextMatchElement.get(branchNode);
			//utilities.PrintIntArray(nextPossible,"next possible element:");
				
			
			if (utilities.greater(nextPossible,Originaltext,0))
				return false;
				
			int [] branchprefix = checkApproximateMatching(tag,allOriginalData,allTagData, pathpatterns,dataCursor);
			
			
		
			if ( branchprefix == null ) // ��ʾ������ɽ���ƥ��
				return false;
			
			if (utilities.greater(branchprefix,nextPossible,0)){ //�滻��nextPossible
				
				//System.out.println("begin replace" );
				 
				//utilities.PrintIntArray(nextPossible,"next possible element:");
				//utilities.PrintIntArray(branchprefix,"new element is:");
				
				 
				branchNextMatchElement.remove(branchNode);
				
				branchNextMatchElement.put(branchNode,branchprefix);
				
			}//end if 
			
			return true;
			
			
		}//end greatThanNextPossible
		
		static void advanceStream(String q,Hashtable dataCursor)
	
	{ 	int datacursor =   ((Integer)dataCursor.get(q)).intValue();
		
		dataCursor.remove(q);
		dataCursor.put(q,new Integer(++datacursor));
	}//end advanceStream
		
		static  int [] checkApproximateMatching(String tag,Hashtable allOriginalData,Hashtable allTagData, Hashtable pathpatterns,Hashtable dataCursor){
			
		Vector Originaldata =  (Vector)allOriginalData.get(tag);

		Vector tagdata =  (Vector)allTagData.get(tag);

		Vector pat = (Vector)pathpatterns.get(tag);

		int datacursor = ((Integer)dataCursor.get(tag)).intValue();

		int originaltext [] = (int [])Originaldata.elementAt(datacursor); //��һ��ȡ��current original data
		
		int tagtext [] = (int [])tagdata.elementAt(datacursor);
		//	utilities.PrintIntArray(originaltext," current  originaltextg is ");
					
			return StringMatching.performApproximateMatching(originaltext, tagtext, pat,tag); //���ؽ���ƥ��Ľ��
			// ����null ��ʾ ���ܽ���ƥ�䣬���򷵻�ƥ��BranchNode����Ӧǰ׺��
		
		}//end checkApproximateMatching
		
		// ����һ�������ҳ�ƥ��Branching Nodes
		static  Vector getApproximateBranchNodes(String tag,int [] text){
			
			/*String [] branches = Query.getBranchNodes(tag);
			
    if (branches.length==1)
    return getApproximateOneBranchNodes(tag,text,branches[0]);
    else
    if (branches.length==2)
     return getApproximateOneBranchNodes(tag,text,branches[0],branches[1]);
     else 
     {System.out.pritnln("Branching nodes number is errror!"); return null;}
    */
    
    return null;
    
	}//end getApproximateBranchNodes
	
	
}//end lazyMatching
