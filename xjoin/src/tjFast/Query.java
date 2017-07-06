package tjFast;

import java.util.*;

public class Query { 
	
	
	static Hashtable twigTagName ;
	
	static String Root ;
	
	static int NumberOfNodes; 
	
	static String [] queryNodes;
	
	static String [] branch = new String [2];
	
	static int [] branchInt = new int [2];

	static Hashtable ChildrenTable;
	
	static Hashtable  PathTable;
	
	static Hashtable  BranchNodeTable;
	
	static Hashtable  ParentTable;
	
	
	static Hashtable  queryIgnoreWildcardsLastPart; // �����query �ַ������Ǻ��������е�wildcards "*" and "//" , 
	                                         
	static void preComputing(DTDTable DTDInfor){ //�����������Ҫ�ģ�����Ԥ�ȼ���һЩֵȻ��Ͳ���ÿ�ζ���������һ��
		
		
  calculateNumberofNodes();
  
    
	calculateQueryNodes();
	
	calculateParentTable();
	
	
	calculateChildrenTable();
	
	
	calculateBanchNodes();
	
	if (getLeaves().size() > 1) 
	calculateBanchNodesInt(DTDInfor);

	 
	calculatePathTable();
	
	calculateBranchNodesInPathTable(); 
	
	calculateIgnoreWildcardLastTable(DTDInfor);
	
	
	
	
		
	}//end preComputing
	
	static String [] getPathNames (String leave){ // ���������Ҫ���ڽ���ƥ�䣬�����ַ�������,û�� wildcards "//" and "*"
		
		return (String [])queryIgnoreWildcardsLastPart.get(leave);
		
	}//end getPathNames 
	
	static void calculateNumberofNodes(){
		
		// �������		NumberOfNodes
		int NumOfNodes = 1;
		
		Enumeration e = twigTagName.keys();
		while (e.hasMoreElements())
			{	String tag = (String) e.nextElement();
				Vector v = (Vector)twigTagName.get(tag);
				NumOfNodes += v.size();
			}//end while
			
		
		NumberOfNodes =  NumOfNodes ;
		
	}//end  calculateNumberofNodes
	
	
	static void calculateQueryNodes(){
		
		Vector tags = new Vector ();
		
		Enumeration e = twigTagName.keys();
		while (e.hasMoreElements())
			{	String tag = (String) e.nextElement();
				if (! tags.contains(tag)) tags.addElement(tag);
				Vector v = (Vector)twigTagName.get(tag);
				for(int i=0;i<v.size();i++)
				{
					String tempTag = ((QueryDataType)v.elementAt(i)).getTagName();
					if (! tags.contains(tempTag)) tags.addElement(tempTag);
				
				}//end for
			}//end while
	
		String [] result= new String [tags.size()];
		
		for(int i=0;i<tags.size();i++)
			result[i]= (String)tags.elementAt(i);
		
		queryNodes = result;
		
	}//end calculateQueryNodes
	
	static void calculateChildrenTable(){
	   
	 ChildrenTable = new Hashtable();
	 
	// System.out.println(" queryNodes is "+queryNodes.length);
	 
	 for (int i=0; i<queryNodes.length; i++){
	 	
	 	 Vector v =  calculateChildren(queryNodes[i]) ;
	 	 
	 	 if (v!= null)
	 	 ChildrenTable.put(queryNodes[i],v);
	 	  
	}//end for 
	
	}//end calculateChildrenTable
	
	static void calculateBranchNodesInPathTable(){
		
		BranchNodeTable = new Hashtable();
	 
	// System.out.println(" queryNodes is "+queryNodes.length);
	
	Vector leaves = getLeaves();
	 
	 for (int i=0; i<leaves.size(); i++){
	
	 	 String [] v =  calculateBranchNodesInPath((String)leaves.elementAt(i)) ;
	 
	 	 if (v!= null)
	 	 BranchNodeTable.put((String)leaves.elementAt(i),v);
	 	  
	}//end for 
	
}  //end calculateBranchNodesInPathTable

	
		static Vector calculateChildren(String q){
		
		if (! twigTagName.containsKey(q)) return null;
		
		Vector v= (Vector) twigTagName.get(q);
		
		Vector result = new Vector();
		
		
		for(int i=0;i<v.size();i++)
		result.addElement(((QueryDataType)v.elementAt(i)).getTagName());
		
		return result;
		
	}//end calculateChildren


	static String getRoot(){
		return Root;
	}//end root
	
	static void setTwigTagNames(Hashtable twigTagNames ){
		
		twigTagName = twigTagNames ; 
		
	}//end setTwigTagNames
	
	
	static int getNumberofNodes(){
			
		return NumberOfNodes ;
		
	}//end getNumberofNodes
	
	static void setRoot(String root){
		
		Root = root ; 
		
		}//end setRoot
	
	
	
	static String [] getQueryNodes(){
		
		return queryNodes;
		
		}//end getQueryNodes
	
	
	
	
	static Vector getChildren(String q){
		
		return (Vector)ChildrenTable.get(q);
		
	}//end getChildren

  static void calculateParentTable(){
  	
  	ParentTable = new Hashtable();
	 
	// System.out.println(" queryNodes is "+queryNodes.length);
	 
	 for (int i=0; i<queryNodes.length; i++){
	 	
	 	String p =  calculateParent(queryNodes[i]) ;
	 	//System.out.println(" queryNodes[i] is "+queryNodes[i]);
	 	//System.out.println(" p is "+p);
	 	 
	 	 if (p!= null)
	 	 ParentTable.put(queryNodes[i],p);
	 	  
	}//end for 

  }//end calculateParenteTable()
  
  	static String calculateParent(String q){
  		
  	Enumeration e = twigTagName.keys();
		while (e.hasMoreElements())
			{	String tag = (String) e.nextElement();
				Vector v = (Vector)twigTagName.get(tag);
				for(int i=0;i<v.size();i++)
				{
					String tempTag = ((QueryDataType)v.elementAt(i)).getTagName();
					if (q.equalsIgnoreCase(tempTag))
						return tag;
				}//end for
			}//end while
				
		return null;	
			
  	}//end calculateParent
	
	static String getParent(String q){
		
		//System.out.println(" q is "+q);
		return (String)ParentTable.get(q);
		
	}//end getParent 
	
	static int [] getIgnoreWildcardQuery(String leave){
		
		return (int [])queryIgnoreWildcardsLastPart.get(leave);
		
}//end 
	
	static void calculateIgnoreWildcardLastTable(DTDTable DTDInfor){
		
		 queryIgnoreWildcardsLastPart = new Hashtable();
	   Vector leaves= getLeaves();
	 
	  for (int i=0; i<leaves.size(); i++){
	 	
	 	 int [] s =  calculateIgnoreWildcardsLastPart((String)leaves.elementAt(i),DTDInfor) ;
	 	 
	 	 queryIgnoreWildcardsLastPart.put((String)leaves.elementAt(i),s);
	 	  
	}//end for 
	
	}//end calculateIgnoreWildcardLastTable
	
	 static int [] calculateIgnoreWildcardsLastPart(String leave,DTDTable DTDInfor){
	 	
	 	 Vector v = getPath(leave);
	 	 
	 	 Vector path = new Vector();
	 	 
	 	 for(int i=0;i<v.size();i++){
	 	 	
	 	 	String s = (String)v.elementAt(i);
	 	 	
	 	 	if (!s.equalsIgnoreCase("//") && !s.equalsIgnoreCase("*") )
	 	 			path.addElement((Integer)DTDInfor.map.get(s));
	 	 	  
	 	}//end for
	 	
	 	// ����Vector ת����int [] 
	 	
	 	int [] pathInt = new int [path.size()];
	 	
	 	for(int i=0;i<pathInt.length;i++)
	 			pathInt[pathInt.length-i-1]= ((Integer)path.elementAt(i)).intValue(); 
	 			
	 			//��������д ��Ҫ���ǵ�getPath���ص��Ǵ�Ҷ�ӵ��������ڱ���ߵ�����
	 	
	 	return pathInt;
	 	
	}//end calculateIgnoreWildcardsLastPart
	
	static void calculatePathTable(){
	
	 PathTable = new Hashtable();
	 Vector leaves= getLeaves();
	 
	 for (int i=0; i<leaves.size(); i++){
	 	
	 	 Vector v =  calculatePath((String)leaves.elementAt(i)) ;
	 	 
	 	 PathTable.put((String)leaves.elementAt(i),v);
	 	  
	}//end for 
	
	
}//end calculatePathTable
	
	static Vector calculatePath (String leave){
		
		Vector v = new Vector();
		
		String tag =leave;
		
		while (! tag.equalsIgnoreCase(Root))
			{v.addElement(tag);
			tag= getParent(tag);
		}//end while
		
		v.addElement(Root);
		
		return v;
		
	}//end  calculatePath 
	
	static Vector getPath(String leave){ // ע�⣺getPath���ص�˳���Ǵ�Ҷ�ӵ���!
		
		return (Vector)PathTable.get(leave);
		
	}//end getPath
	
	static Vector getPCChildren(String q){
		
		if (! twigTagName.containsKey(q)) return null;
		
		Vector v= (Vector) twigTagName.get(q);
		
		Vector result = new Vector();
		
		for(int i=0;i<v.size();i++)
			 if (((QueryDataType)v.elementAt(i)).getIsPCEdge()) 
				result.addElement(((QueryDataType)v.elementAt(i)).getTagName());
		
			
		return result;
				
	}//end getPCChildren
	
	static boolean  isPCRelationship (String parent,String child){
		
		boolean find = false;
		
		for(int i=0;i<getPCChildren(parent).size();i++)
  		{	String PCchild =(String)Query.getPCChildren(parent).elementAt(i);
  			
  			if (PCchild.equalsIgnoreCase(child)){
  				find = true;
  				break;
  				}//end if
  		
		}//end for
	
		return find;
	}//end isPCRelationship 
	
	static Vector  getPathPattern (String leave){ //���������ʾ����·��,AD��ϵ��string "//"����ʾ��
		
		Vector v = new Vector();
		
		String tag =leave;
		
		while (! tag.equalsIgnoreCase(Root))
			{v.addElement(tag);
			String parent= getParent(tag);
			if (! isPCRelationship (parent,tag))
				v.addElement("//");
			tag=parent;
		}//end while
		
		v.addElement(Root);
		
		
		//�������ߵ���������Ϊ������ʾ���Ǵ�Ҷ�ӵ�����
		
		Vector reverse = new Vector ();
		for(int i=v.size()-1;i>=0;i--)
		reverse.addElement(v.elementAt(i));
			
		
		return reverse;
		
	}//end getPathPattern 
	
	
	static boolean isLeaf(String q){
		
		if (twigTagName.containsKey(q)) return false;
		else return true;
		
	}//end isLeaf
	
	static boolean isBranchNode(String q){
		
		String [] b = getBranchNode();
		
		for(int i=0;i<b.length;i++)
			if (q.equalsIgnoreCase(b[i]))
				return true;
		
		return false;
	}//end isLeaf
	
	
	static Vector getLeaves(){
		
		String [] nodes = getQueryNodes();
		
		Vector leaves = new Vector();
		
		for(int i=0;i<nodes.length;i++)
			if ( isLeaf(nodes[i]))
				leaves.addElement(nodes[i]);
		
		return leaves;
		
	}//end getLeaves()
	
	 static String  getLeafBranchNode(String qleaf){
	 	
	 	String [] branches = getBranchNodes(qleaf);
	 	
	 	if (branches.length ==1)
	 	return branches[0];
	 	else
	 	return  branches[1];
	 	
	 	}//end 
	
	static String [] getBranchNodes(String leaf){//assume there is only at most two branch nodes
	// if there are two branching nodes , the first returned value is ancestor.
	
		return (String [])BranchNodeTable.get(leaf);
		
	}//end getBranchNodes
	
	 static String [] calculateBranchNodesInPath(String leaf){
	 	
	 	//System.out.println("leaf is "+leaf);
	 		
		String branches [] = getBranchNode();
		Vector result = new Vector();
		
	
		for(int i=0;i<branches.length;i++)
		if (isAncester(branches[i],leaf))
			result.addElement(branches[i]);
		
		String r [] = new String [result.size()];
		for(int i=0;i<result.size();i++)
			r[i] = (String)result.elementAt(i);
			
			
		if (r.length>1)
			if (! isAncester(r[0],r[1])) //���໥����
			{	String temp = r[1];
				r[1]=r[0];
				r[0]=temp; 
			}//end if
		
		return r;
	 	
	}//end  calculateBranchNodesInPath
	
	static boolean isTopBranch(String branch){
		
		String b [] = getBranchNode();
		
		if (b.length ==1)
			return true;
		else if (branch.equalsIgnoreCase(b[0]))
				return true;
			else
				return false;
		}//end isTopBranch
		
		static void calculateBanchNodes(){ //assume there is only at most two branch nodes
			
			
			String [] nodes = getQueryNodes();
		
		int j=0;
		
		for(int i=0;i<nodes.length;i++)
			if   (((Vector)getChildren(nodes[i]))!=null && (((Vector)getChildren(nodes[i])).size()>1))
				branch[j++]=nodes[i];
				
		if (branch[1]==null)//�������һ��branch�������´���
		{	String [] onebranch = new String [1];
			onebranch[0]= branch[0];
			branch =  onebranch;
			return;
		}
		 
		if (! isAncester(branch[0],branch[1])) 
		 {	String temp = branch[0];
			branch[0] = branch[1];
			branch[1] = temp;
		}//end if
		

		}//end calculateBanchNodes
	
	static void calculateBanchNodesInt(DTDTable DTDInfor){ // ���ﷵ��������branching�ڵ��ֵ
		
		    
	      Integer IntegetValue = (Integer)DTDInfor.map.get(branch[0]);
	      //System.out.println("IntegetValue is "+IntegetValue); 
				branchInt[0]= IntegetValue.intValue();
	
		if (branch.length>1)
		{
			  IntegetValue = (Integer)DTDInfor.map.get(branch[1]);
				branchInt[1]= IntegetValue.intValue();
	
		}//end if
		
		
	}//end calculateBanchNodesInt()
	
	
	static String [] getBranchNode(){ //assume there is only at most two branch nodes
	
			return branch;
		
	}//end 
	
	static int [] getBranchNodeInt(){ //assume there is only at most two branch nodes
	
			return branchInt;
		
	}//end 
	
	
	static boolean isAncester(String ancestor,String descendant){
		
		if (descendant.equalsIgnoreCase(Root))
			return false;
		
		String parent = getParent(descendant);
		
		while (true){
			if (parent.equalsIgnoreCase(ancestor))
				return true;
				
			if (parent.equalsIgnoreCase(Root))
				break;
			else parent = getParent(parent);
			
		}//end while
		
		return false;
		
	}//end isAncester
	
	
	static int getPathLength(String tag){
		
		
		int length = 1;
		
		while (! tag.equalsIgnoreCase(Root))
			{length++;
			tag= getParent(tag);
		}//end while
		
		
		return length;
		
		
		
	}//end isLeaf
	
	
	static Vector getBranchLeaves(String branchNode){
		
		Vector leaves = getLeaves();
		
		Vector branchleaves = new Vector();
		
		for(int i=0;i<leaves.size();i++){
			
			if ( isAncester(branchNode,(String)leaves.elementAt(i)) )
				branchleaves.addElement((String)leaves.elementAt(i));
			
			
		}//end for
		
		return branchleaves;
		
		}//end getBranchLeaves()
	
	
	//��Ҫ��������branchpoint��ƫ������key ��String, ��ʽ��branch node%leaf node
	//object��int [2]�� ��һ�������ĸ��ֽ��pattern�У��ڶ�����λ��ƫ��
		static Hashtable calculateBranchPosition(){
			
			 Hashtable result = new Hashtable ();
			String [] branches = getBranchNode();
			Vector leaves = getLeaves();
				
			for(int i=0;i<branches.length; i++)
				for (int j=0;j<leaves.size();j++)
					{ 	String leave = (String)leaves.elementAt(j);
						Vector path = getPathPattern (leave);
						int [] pos = new int [2];
						pos[0] =0;
						pos[1] =0;
						boolean find = false;
						for(int k=0;k<path.size();k++)
							{String s = (String)path.elementAt(k);
							if ( s.equalsIgnoreCase(branches[i]))
								{ find = true; break; }
								
							if (! s.equalsIgnoreCase("//"))
								pos[1]++;
							else {  pos[0]++;
								pos[1] = 0;
								}//end else
							
							}//end for
						String keys = branches[i]+"%"+leave;
						if (find) result.put(keys,pos);
						
					}//end for
			
			return result;
			
			}//end calculateBranchPosition
		
	
}//end class