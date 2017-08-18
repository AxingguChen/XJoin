package tjFast;

import java.util.*;
import java.io.*;
import tjFast.naiveMethod;


public class TwigSet {

    Hashtable allData; //���е�join���ݷ�������

    Hashtable allOriginalData;

    Hashtable dataCursor;

    int numberOfleaves;
    int numberOfFinishedLeaves = 0;
    Vector leaves;

    //Hashtable decomposedpathpatterns ;

    Hashtable pathMatchingResults;

    Hashtable branchPointPosition;

    Hashtable pathpatterns;

    Hashtable set;

    int numberOfSolutions = 0;

    Vector solutionlist;

    Hashtable finalResults;

    Hashtable setMinTable; //���������Ҫ������¼�����е���Сֵ���Ӷ����н���ƥ��

    Hashtable branchNextMatchElement; //���������Ҫ������¼��һ������ƥ���ֵ

    Hashtable subtreeMatchingHash;

    public TwigSet(DTDTable DTDInfor, Hashtable allData, Hashtable allOriginalData) {// allOriginalDataָ����Dewey ֵ����allDataָ����tag����

        dataCursor = new Hashtable();

        pathpatterns = new Hashtable();

        set = new Hashtable();

        finalResults = new Hashtable();

        subtreeMatchingHash = new Hashtable();

        setMinTable = new Hashtable();

        branchNextMatchElement = new Hashtable();

        solutionlist = new Vector();

        leaves = Query.getLeaves();

        numberOfleaves = leaves.size();

        for (int i = 0; i < numberOfleaves; i++) {
            String s = (String) leaves.elementAt(i);
            dataCursor.put(s, new Integer(0));
        }//end for

        for (int i = 0; i < numberOfleaves; i++) {
            Vector v = new Vector();
            String s = (String) leaves.elementAt(i);

            finalResults.put(s, v);
        }//end for

        String[] nodes = Query.getQueryNodes();

        for (int i = 0; i < nodes.length; i++) {
            Boolean b = new Boolean(true);
            String s = nodes[i];

            subtreeMatchingHash.put(s, b);
        }//end for

        if (numberOfleaves > 1) // otherwise it is a path pattern
        {
            String[] branches = Query.getBranchNode();

            for (int i = 0; i < branches.length; i++) {
                Vector v = new Vector();
                set.put(branches[i], v);

            }//end for

            branchPointPosition = Query.calculateBranchPosition();

        }//end if


        if (numberOfleaves > 1) // otherwise it is a path pattern
        {
            String[] branches = Query.getBranchNode();

            for (int i = 0; i < branches.length; i++) {
                int[] v = new int[1];

                setMinTable.put(branches[i], v);

            }//end for

        }//end if

        if (numberOfleaves > 1) // otherwise it is a path pattern
        {
            String[] branches = Query.getBranchNode();

            for (int i = 0; i < branches.length; i++) {
                int[] v = new int[1];

                branchNextMatchElement.put(branches[i], v);

            }//end for

        }//end if

        pathMatchingResults = new Hashtable();

        this.allData = allData;

        this.allOriginalData = allOriginalData;

        producePathPattern(DTDInfor);

        //��Ҫ��������branchpoint��ƫ������object��int [2]�� ��һ�������ĸ��ֽ��pattern�У��ڶ�����λ��ƫ��


    }//end TwigSet

    int beginJoin(){
        //System.out.println("begin join");
        long joinBeginTime = System.currentTimeMillis();
        List<List<String>> idList_all = new ArrayList<>();
        //�������ڲ���
        /*for(int i=0;i<leaves.size();i++)
		{ String leave = (String)leaves.elementAt(i);
      boolean match  = lazyMatching.checkApproximateMatching(leave,allData,pathpatterns,dataCursor);
      System.out.println("match is "+match);
		}*/

        //�������ڲ���

        //long begintime = System.currentTimeMillis();
        Vector leaves = Query.getLeaves();
        for (int i = 0; i < leaves.size(); i++) {
            String s = (String) leaves.elementAt(i);
            locateMatchedLabel(s);
        }//end for

        int totalSolutionCount = 0;
        while (numberOfFinishedLeaves != numberOfleaves) { //��ʾstreamû�н���

            String qact;

            if (leaves.size() == 1)
                qact = (String) leaves.elementAt(0);
            else
                qact = getNext(Query.getRoot());

            //utilities.DebugPrintln("return leaf is "+qact);

            if (leaves.size() > 1) {
                outputSolutions(qact);
                //System.out.println("qact "+qact);
            }
            advanceStream(qact);


            locateMatchedLabel(qact);
        }//end while

        if (leaves.size() > 1)
            emptySets();

        //long joinEndtime = System.currentTimeMillis();
        //System.out.println("tjFast Join Time:"+(joinEndtime-joinBeginTime));
        //////
        //System.out.println(" Total CPU time is "+ (endtime-begintime)+" ms.");

        if (leaves.size() > 1) {
            //System.out.println(" Number of path solutions is " + numberOfSolutions);
            if (numberOfSolutions > 0) {
                if (Query.getBranchNode().length == 1)
                    totalSolutionCount = mergeAllPathSolutions.mergeOneBranch(finalResults);
                else {
                    //mergeAllPathSolutions.mergeTwoBranchs(finalResults);
                    totalSolutionCount = mergeAllPathSolutions.mergeTwoBranchs(finalResults);
                    //System.out.println("solution pair number is:" + solutionPairIDList.size());
                    //totalSolutionCount = totalSolutionCount + solutionPairIDList;
                }

            }
        }//end if (leaves.size() > 1)
        return totalSolutionCount;
    }//end  beginJoin()



    void beginJoin_naive(List<HashMap<String, String>> allTagIDValue) throws Exception{
        System.out.println("begin join");
        List<List<String>> idList_all = new ArrayList<>();
        //�������ڲ���
        /*for(int i=0;i<leaves.size();i++)
		{ String leave = (String)leaves.elementAt(i);
      boolean match  = lazyMatching.checkApproximateMatching(leave,allData,pathpatterns,dataCursor);
      System.out.println("match is "+match);
		}*/

        //�������ڲ���

        long begintime = System.currentTimeMillis();
        Vector leaves = Query.getLeaves();
        for (int i = 0; i < leaves.size(); i++) {
            String s = (String) leaves.elementAt(i);
            locateMatchedLabel(s);
        }//end for


        while (numberOfFinishedLeaves != numberOfleaves) { //��ʾstreamû�н���

            String qact;

            if (leaves.size() == 1)
                qact = (String) leaves.elementAt(0);
            else
                qact = getNext(Query.getRoot());

            //utilities.DebugPrintln("return leaf is "+qact);

            if (leaves.size() > 1) {
                outputSolutions(qact);
                //System.out.println("qact "+qact);
            }
            advanceStream(qact);


            locateMatchedLabel(qact);
        }//end while

        if (leaves.size() > 1)
            emptySets();

        long endtime;
        //////
        //System.out.println(" Total CPU time is "+ (endtime-begintime)+" ms.");

        if (leaves.size() > 1) {
            System.out.println(" Number of path solutions(points count) is " + numberOfSolutions);
            if (numberOfSolutions > 0) {
                if (Query.getBranchNode().length == 1){
                    //merge solution
                    List<List<String>> solutionPairIDList = mergeAllPathSolutions.mergeOneBranch_naive(finalResults);
                    endtime = System.currentTimeMillis();
                    System.out.println(" tjFast time is "+ (endtime-begintime)+" ms.");
                    //get solution pair value
                    naiveMethod naive = new naiveMethod();
                    int result = naive.getResult(solutionPairIDList,allTagIDValue);
                    System.out.println("Final solution number is:"+result);
                }
                else{
                    //mergeAllPathSolutions.mergeTwoBranchs(finalResults);
                    List<List<String>> solutionPairIDList = mergeAllPathSolutions.mergeTwoBranchs_naiveDouble(finalResults);
                    System.out.println("solution pair number is:"+solutionPairIDList.size());
                    endtime = System.currentTimeMillis();
                    System.out.println(" tjFast time is "+ (endtime-begintime)+" ms.");
                    //get solution pair value
                    naiveMethod naive = new naiveMethod();
                    int result = naive.getResult(solutionPairIDList,allTagIDValue);
                    System.out.println("Final solution number is:"+result);
                }
                //System.out.println("final result:"+finalResults.get("b"));
                //System.out.println(" Final path solutions is " + mergeAllPathSolutions.getPathNumber(finalResults, leaves));

                //System.out.println(showAllPathSolutions());
            }
        }//end if (leaves.size() > 1)

    }//end  beginJoin()

    void printPathSolutions() {
        for (int i = 0; i < numberOfleaves; i++) {
            String s = (String) leaves.elementAt(i);
            System.out.println("leaves:" + s);
        }
        //System.out.println("finalResult valueset:"+finalResults.get("b"));
    }

    List<List<String>> showAllPathSolutions() {
        List<List<String>> idList = new ArrayList<>();
        for (int i = 0; i < numberOfleaves; i++) {
            String s = (String) leaves.elementAt(i);
            System.out.print(s + ": ");
            idList.add(utilities.DebugPrintSolutionlist(s, finalResults));
            System.out.println(utilities.DebugPrintSolutionlist(s, finalResults));
        }//end for
        return idList;
    }//end showAllPathSolutions

    void emptySets() {

        int terminal[] = new int[1];
        terminal[0] = utilities.MAXNUM;

        String[] branches = Query.getBranchNode();

        for (int i = branches.length - 1; i > -1; i--) {
            String s = branches[i];
            moveToSet(terminal, s);

        }//end for

    }//end emptySets()

    String getNext(String q) {


        if (Query.isLeaf(q)) return q;

        Vector children = Query.getChildren(q);

        if (children.size() == 1) return getNext((String) children.elementAt(0));

        //����˵��q��һ��branchPoint

        Vector prefixes = new Vector();
        String[] leafVector = new String[children.size()];

        Hashtable singleleaf_matchedprefixes = new Hashtable();

        for (int i = 0; i < children.size(); i++) {
            String child = (String) children.elementAt(i);
            String leaf = getNext(child);

            if (!subtreeMatching(child)) {    //System.out.println("skip child is "+child); System.out.println("leaf is "+leaf);
                return leaf;
            }

            leafVector[i] = leaf;
            //utilities.printIntArrayVector(MatchedPrefixes(leaf,q)," leaf and branch is: "+leaf+" "+q);
            //if the leaf stream ends, return the MAX value vector
            Vector singleleaf_prefixes = MatchedPrefixes(leaf, q);
            singleleaf_matchedprefixes.put(leaf, singleleaf_prefixes);
            int[] max = utilities.maxLabel(singleleaf_prefixes);
            //utilities.DebugPrintIntArray(max , " leaf and branch is: "+leaf+" "+q);
            prefixes.addElement(max);
        }//end for

        int[] maxmin = utilities.MaxMinLabelPosition(prefixes); // int[0] is max and int[1] is min

        String nmin = leafVector[maxmin[1]];


        int[] maxlabel = (int[]) prefixes.elementAt(maxmin[0]);

        int[] minlabel = (int[]) prefixes.elementAt(maxmin[1]);

        if (!utilities.isPrefix(minlabel, maxlabel, 0))

            setSubtreeMatchingFalse(q);

        else {

            Vector minV = (Vector) singleleaf_matchedprefixes.get(nmin);


            for (int i = 0; i < minV.size(); i++) {
                if (utilities.isPrefix((int[]) minV.elementAt(i), maxlabel, 0))
                    moveToSet((int[]) minV.elementAt(i), q);


            }//end for

            setSubtreeMatchingTrue(q);

        }//end else

        return nmin;

    }//end getNext


    boolean subtreeMatching(String node) {

        return ((Boolean) subtreeMatchingHash.get(node)).booleanValue();

    }//end

    void setSubtreeMatchingFalse(String node) {

        subtreeMatchingHash.remove(node);

        subtreeMatchingHash.put(node, new Boolean(false));

    }//end

    void setSubtreeMatchingTrue(String node) {

        subtreeMatchingHash.remove(node);

        subtreeMatchingHash.put(node, new Boolean(true));

    }//end

    void moveToSet(int[] candidate, String branch) {

        Vector aset = (Vector) set.get(branch);

        Vector deletedNodes = new Vector();

        for (int i = 0; i < aset.size(); i++) {
            int[] temp = (int[]) aset.elementAt(i);
            if (utilities.isEqual(temp, candidate))
                return;
            if ((!utilities.isPrefix(temp, candidate, 0)) && (!utilities.isPrefix(candidate, temp, 0)))
                addTodeletedNodes(deletedNodes, temp);

        }//end for


        for (int i = 0; i < deletedNodes.size(); i++) //�Ѳ���AD��ϵ�Ľڵ�ͳһɾ��
        {
            int[] temp = (int[]) deletedNodes.elementAt(i);
            aset.removeElement(temp);

            outputRelativeSolution(temp, branch); //�����ص�ƥ����

        }//end for


        addElementToSet(candidate, branch, aset);

    }//end moveToSet

    void addElementToSet(int[] candidate, String branch, Vector aset) {

        if (candidate[0] != utilities.MAXNUM) //��һ����Ҫ������ǵ�stream����
        {    // ���µĲ�����Ҫ�������ü����е���Сֵ��������������ƥ��ʹ�á�
            if (aset.size() == 0) {
                setMinTable.remove(branch);
                setMinTable.put(branch, candidate);
            }//end if
            else {
                int[] min = (int[]) setMinTable.get(branch);
                if (utilities.greater(min, candidate, 0)) //�²����ֵ��С�����Խ����滻
                {
                    setMinTable.remove(branch);
                    setMinTable.put(branch, candidate);
                }//end if

            }//end else

            aset.addElement(candidate);
        }//end if


    }//end addElementToSet

    void addTodeletedNodes(Vector deletedNodes, int[] node) {

        int i = 0;
        for (; i < deletedNodes.size(); i++)
            if (((int[]) deletedNodes.elementAt(i)).length < node.length)
                break;


        deletedNodes.insertElementAt(node, i);


    }//end addTodeletedNodes

    void outputRelativeSolution(int[] deletedbranch, String branch) {

        boolean isTopbranch = Query.isTopBranch(branch);

        for (int i = 0; i < numberOfleaves; i++) {
            String leaf = (String) leaves.elementAt(i);
            if (isTopbranch) //then we can add this path solution to the final result set
            {
                Vector solutionkeys = listContainMatchingNodeAndDelete(deletedbranch, leaf, branch);
                if (solutionkeys != null) {
                    Vector v = (Vector) finalResults.get(leaf);
                    for (int k = 0; k < solutionkeys.size(); k++)
                        v.addElement(solutionkeys.elementAt(k)); //add them to final path solutions

                }//end if
            }//end if
            else //����Ļ�����Ҫת���������һ��branch points����ȥ
            {
                String newbranch = Query.getBranchNodes(leaf)[0];
                changeBranchNode(deletedbranch, branch, newbranch, leaf);
            }

        }//end for

    }//end outputRelativeSolution

    void outputSolutions(String qleaf) {

        //System.out.println("begin output qleaf is "+qleaf);

        Vector matchingresult = (Vector) pathMatchingResults.get(qleaf);

        Vector data = (Vector) allOriginalData.get(qleaf);

        int datacursor = ((Integer) dataCursor.get(qleaf)).intValue();

        int text[] = (int[]) data.elementAt(datacursor);

        //System.out.println("text length is "+text.length);

        for (int i = 0; i < matchingresult.size(); i++) {
            //System.out.println("begin output qleaf is2 "+qleaf);

            int[] solution = (int[]) matchingresult.elementAt(i);

            //System.out.println("begin output qleaf is3 "+qleaf);

            //for double layer query

            String branches [] =Query.getBranchNodes(qleaf);

            //only for path query

//            String branches[] = new String[1];
//
//            branches[0] = Query.getRoot();

            boolean isSolution = true;
            for (int j = 0; j < branches.length; j++)
                if (!setContainsSolution(branches[j], qleaf, text, solution)) //
                {
                    isSolution = false;
                    break;
                }
            if (isSolution) {
                //add final path solution to solution list. text->id, s
                addToSolutionList(text, solution, branches, qleaf);
//                utilities.DebugPrintIntArray(solution, "Final solutions: ");
//                for (int j = 0; j < text.length; j++) {
//                    System.out.print(text[j] + " ");
//                }
//                System.out.println();
                numberOfSolutions++;
            }//end if

            //System.out.println("begin output qleaf is4 "+qleaf);

        }//end for

    }//end outputSolutions

    void addToSolutionList(int[] text, int[] solution, String[] branches, String qleaf) {

        //�������Ǽ������������branch points

        Vector temp = new Vector();

        for (int j = 0; j < branches.length; j++)

        {
            int[] offset = (int[]) branchPointPosition.get(branches[j] + "%" + qleaf);
            int newprefix[] = SolutionMatchedPrefixes(text, offset, solution);
            temp.addElement(newprefix);
        }//end for

        String leafbranchnode = Query.getLeafBranchNode(qleaf);

        solutionKey akey = listContainsolutionKey(temp, qleaf, leafbranchnode);
        //changed the code to output all the solutions

        if (akey != null) {
            akey.solutionNumber++;
            akey.addNodeID(text);
        } else {
            solutionKey newkey = new solutionKey(temp, qleaf, leafbranchnode, text);
            solutionlist.addElement(newkey);
        }//end else


    }//end addToSolutionList

    //This function test whether the solution list contain this solution key
    solutionKey listContainsolutionKey(Vector temp, String leaf, String branchnode) {

        for (int i = 0; i < solutionlist.size(); i++) {
            solutionKey s = (solutionKey) solutionlist.elementAt(i);
            boolean nodesequal = true;

            if (s.branchnodes.size() != temp.size())//if two nodes are same then their size must be first equal
                nodesequal = false;
            else
                for (int j = 0; j < s.branchnodes.size(); j++) {
                    int solution[] = (int[]) s.branchnodes.elementAt(j);
                    if (!utilities.isEqual(solution, (int[]) temp.elementAt(j)))
                        nodesequal = false;
                }//end for
            if (nodesequal)
                if ((leaf.equalsIgnoreCase(s.leaf)) && (branchnode.equalsIgnoreCase(s.branch)))

                    return s;
        }//end for


        return null;

    }//end listContainsolutionKey

    //This function test whether the solution list contain this solution key

    Vector listContainMatchingNodeAndDelete(int[] topmatchingnode, String leaf, String Topbranch) {

        Vector result = new Vector();

        for (int i = 0; i < solutionlist.size(); i++) {
            solutionKey s = (solutionKey) solutionlist.elementAt(i);

            if (!((leaf.equalsIgnoreCase(s.leaf)) && (Topbranch.equalsIgnoreCase(s.branch))))
                continue;

            int solution[] = (int[]) s.branchnodes.elementAt(0);

            if (!utilities.isEqual(solution, topmatchingnode))
                continue;

            result.addElement(s);

            solutionlist.removeElementAt(i--); // delete this element from solutionlist

        }//end for

        if (result.size() == 0)
            return null;
        else
            return result;

    }//end listContainMatchingNode


    void changeBranchNode(int[] leafMatchingNode, String oldbranch, String newbranch, String leaf) {

        solutionKey s = null;

        for (int i = 0; i < solutionlist.size(); i++) {
            s = (solutionKey) solutionlist.elementAt(i);

            if (!((leaf.equalsIgnoreCase(s.leaf)) && (oldbranch.equalsIgnoreCase(s.branch))))
                continue;

            //int solution [] = (int [])s.branchnodes.elementAt(0);
            int solution[] = (int[]) s.branchnodes.elementAt(1);

            //solution equals leafMatchingNode continue following steps
            if (!utilities.isEqual(solution, leafMatchingNode))
                continue;

            s.branch = newbranch; //change branch node here !

            solutionlist.removeElementAt(i--);

            solutionlist.addElement(s); // move s to the end this solutionlist

        }//end for

    }//end changeBranchNode

    boolean setContainsSolution(String qbranch, String qleaf, int[] text, int[] solution) {

        int[] offset = (int[]) branchPointPosition.get(qbranch + "%" + qleaf);
        //System.out.println(" 0 text length is "+ text.length);

        Vector aset = (Vector) set.get(qbranch);

        int newprefix[] = SolutionMatchedPrefixes(text, offset, solution);

        for (int i = 0; i < aset.size(); i++)
            if (utilities.isEqual((int[]) aset.elementAt(i), newprefix))
                return true;

        return false;


    }//end setContainsSolution

    public int[] SolutionMatchedPrefixes(int[] text, int[] offset, int[] solution) {


        int endpostiion = solution[offset[0]] + offset[1];

        int newprefix[] = new int[endpostiion];
        //System.out.println(" endposition is "+endpostiion);
        //System.out.println(" text length is "+text.length);

        for (int j = 0; j < endpostiion; j++)
            newprefix[j] = text[j];

        return newprefix;

    }//end SolutionMatchedPrefixes

    public Vector MatchedPrefixes(String qleaf, String qbranch) { // return ƥ���ǰ׺�� ���� Vector�� ��ÿ�������� int []

        Vector prefixes = new Vector();

        Vector data = (Vector) allOriginalData.get(qleaf);

        int datacursor = ((Integer) dataCursor.get(qleaf)).intValue();


        int text[] = (int[]) data.elementAt(datacursor);

        if (text[0] == utilities.MAXNUM) //���Ǹ�leaf stream �Ѿ������Ĵ���
        {
            prefixes.addElement(text);
            return prefixes;
        } //end if

        Vector matchingresult = (Vector) pathMatchingResults.get(qleaf);
        // ÿ��ƥ������һ��vector�� object �� int []�������Ƿֽ��Ժ��pattern����
        //offset ��һ�������ĸ��ֽ��pattern�У��ڶ�����λ��ƫ��

        int[] offset = (int[]) branchPointPosition.get(qbranch + "%" + qleaf);

        for (int i = 0; i < matchingresult.size(); i++) {
            prefixes.addElement(SolutionMatchedPrefixes(text, offset, (int[]) matchingresult.elementAt(i)));

        }//end for

        return prefixes;

    }//end MatchedPrefixes


    public void locateMatchedLabel(String tag) {  // �����ҵ���һ��ƥ���Ԫ��

        Vector data = (Vector) allData.get(tag);

        Vector pat = (Vector) pathpatterns.get(tag);

        int datacursor = ((Integer) dataCursor.get(tag)).intValue();

        while (datacursor < data.size() - 1) {
            int text[] = (int[]) data.elementAt(datacursor);

            //utilities.PrintIntArray(text," current  tag text is ");

            //lazyMatching.lazyProcess( tag, allOriginalData, allData, pathpatterns, dataCursor, setMinTable, branchNextMatchElement);

            //utilities.PrintIntArray(text," matching tag text is ");
            StringMatching.performStringMatching(text, pat);


            if (StringMatching.results.size() > 0)

            {
                dataCursor.remove(tag);
                dataCursor.put(tag, (new Integer(datacursor)));
                if (pathMatchingResults.containsKey(tag)) {

                    pathMatchingResults.remove(tag);
                    pathMatchingResults.put(tag, StringMatching.results);
                }//end if
                else {
                    pathMatchingResults.put(tag, StringMatching.results);
                }//else

                utilities.DebugPrintln("Current stream " + tag + " is the " + datacursor + "th element ");

                return; // �ҵ�ƥ���Ԫ�أ����̷���
            }

            datacursor++;

        }//end while

        dataCursor.remove(tag);
        dataCursor.put(tag, (new Integer(data.size() - 1))); //����һֱ�����Ҳ�Ҳ���ƥ��Ԫ�أ�����Stream ��ֹ
        numberOfFinishedLeaves++;
        utilities.DebugPrintln(" Stream " + tag + " finished! ");


    }//end locateMatchedLabel

    void advanceStream(String q)

    {
        int datacursor = ((Integer) dataCursor.get(q)).intValue();

        dataCursor.remove(q);
        dataCursor.put(q, new Integer(++datacursor));
    }//end advanceStream


    void producePathPattern(DTDTable DTDInfor) {

        Vector leaves = Query.getLeaves();
        int pattern[];
        for (int i = 0; i < leaves.size(); i++) {
            Vector v = Query.getPathPattern((String) leaves.elementAt(i));

            for (int j = 0; j < v.size(); j++)
                utilities.DebugPrintln(" path is " + (String) v.elementAt(j));


            pattern = new int[v.size()];

            for (int j = 0; j < v.size(); j++) {

                String s = (String) v.elementAt(j);

                if (s.equalsIgnoreCase("//"))
                    pattern[j] = -2;
                else if (s.equalsIgnoreCase("*"))
                    pattern[j] = -1;
                else
                    pattern[j] = ((Integer) DTDInfor.map.get(s)).intValue();
            }//end for

            for (int j = 0; j < v.size(); j++)
                utilities.DebugPrintln(" path is " + pattern[j]);


            pathpatterns.put((String) leaves.elementAt(i), StringMatching.decomposePattern(pattern));


        }//end for


    }//end producePathPattern()


    static public void main(String[] args) {


    }//end main

}//end TwigSet