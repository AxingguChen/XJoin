package tjFast;

import java.util.*;
import java.io.*;


public class DTDTable  {
	
	
	int numberOfTags ;
	int numberOfRules ;
	
	int [] tagsNumberOfRule ;
	
	Hashtable map; //�������ƥ���string name ���� integer
	/* DTDrule ������������ݽṹ��; DTDrule [][0]���ǲ�ʹ�ã� ����������Ϊ�յģ� ��ȫ���亢�ӵ� ��Ӧ��int �����Բ� map ����*/
	int DTDrule [][] ;
	
	int root;
	
	void setNumberOfTags(int numberOfTags){
		
		this.numberOfTags = numberOfTags;
		
	}//end setNumberOfTags
	
	
	void setNumberOfRules(int numberOfRules){
		
		this.numberOfRules = numberOfRules;
		
	}//end setNumberOfRules
	
	void setDTDrule(int  [][] DTDrule){
		
		this.DTDrule = DTDrule;
		
	}//end setDTDrule
	
	void setMap (Hashtable map){
		
		this.map = map;
		
	}//end setDTDrule
	
	
	void setTagsNumberOfRule(int  [] tagsNumberOfRule){
		
		this.tagsNumberOfRule = tagsNumberOfRule;
		
	}//end setTagsNumberOfRule
	
	
	void	setRoot(int root){
		
		this.root = root;
		 
		}//end setRoot
		
	int getTag(int parent, int value){
		
		int tagID = -1;
		int remainder = value % tagsNumberOfRule[parent];
		
		if (remainder == 0)
			tagID = tagsNumberOfRule[parent];
		else
			tagID = remainder;
		
		return DTDrule[parent][tagID];
		
	}//end getTag
	
	
	int [] getAllTags( int []  value, int head){
		
		int num = value.length +1;
		
		int result [] = new int [num];
		
		result[0] = head;
		
		for(int i=1;i<num;i++)
		result[i]= getTag(result[i-1],value[i-1]);


		
		return result;
		 
		
	}//end getAllTags
	
		
	int getLabel (String parent, String child, int leftSilbling){
		
		int parentID = ((Integer)map.get(parent)).intValue();
		int childID = ((Integer)map.get(child)).intValue();
		
		int tagPosiitonInRule = -1;
			
		for(int i=1;i<=tagsNumberOfRule[parentID];i++)
			if  (DTDrule[parentID][i]==childID)
				{tagPosiitonInRule =i;
				break;
			}//end if
			
		int leftSiblingPosition = leftSilbling % tagsNumberOfRule[parentID];
		if ( leftSilbling % tagsNumberOfRule[parentID] ==0) leftSiblingPosition=tagsNumberOfRule[parentID];
		
		
		if (leftSilbling == -1) // child is the first child
		return tagPosiitonInRule;
		else 
		return calculateX(leftSiblingPosition,tagPosiitonInRule,leftSilbling,tagsNumberOfRule[parentID]);
	
		
	}//end getLabel
	
	
	int calculateX(int w,int k,int y,int r){
		
		
		int floorValue = y/r;
		int ceilValue =0;
		if  (y%r !=0)
		ceilValue = y/r+1;
		else
		ceilValue = y/r;
		
		if (w<k)
		return floorValue*r+k;
		else
		return ceilValue*r+k;
		
		}//end calculateX
	
	static public void main(String[] args) {
	 	
	 	
	 	
	 	DTDTable test = new DTDTable();
	 	//test.initilizeTable();
	 	
	 	int [] value ={5,2,2,1,6};
	 	
     		int result [] =test.getAllTags( value,1);
     		
     		//for(int i=0;i<result.length;i++)
     		//System.out.println(result[i]);
     		
     		int testLabel = test.getLabel ("c", "a", -1);
     		System.out.println("Assigned value is "+testLabel);
	
     		
              }//end main
	
	
	

}//end DTDTable