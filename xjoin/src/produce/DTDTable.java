package produce; /**
 * Used in initilizeDTDTable and documentAnalysis class
 * tag:
 */
import java.util.Hashtable;

public class DTDTable {
    int numberOfTags;
    int numberOfRules;

    int[] tagsNumberOfRule;

    Hashtable map; //这个变量匹配从string name 到达 integer
    /* DTDrule 这个变量的数据结构是; DTDrule [][0]我们不使用， 下面若不是为空的， 则全是其孩子的 对应的int ，可以查 map 变量*/
    int DTDrule[][];

    int root;

    void setNumberOfTags(int numberOfTags) {
        this.numberOfTags = numberOfTags;
    }


    void setNumberOfRules(int numberOfRules) {
        this.numberOfRules = numberOfRules;
    }

    void setDTDrule(int[][] DTDrule) {
        this.DTDrule = DTDrule;
    }

    void setMap(Hashtable map) {
        this.map = map;
    }

    void setTagsNumberOfRule(int[] tagsNumberOfRule) {
        this.tagsNumberOfRule = tagsNumberOfRule;
    }

    void setRoot(int root) {
        this.root = root;
    }

    int getTag(int parent, int value) {
        int tagID = -1;
        int remainder = value % tagsNumberOfRule[parent];
        if (remainder == 0)
            tagID = tagsNumberOfRule[parent];
        else
            tagID = remainder;
        return DTDrule[parent][tagID];
    }

    int[] getAllTags(int[] value, int head) {
        int num = value.length + 1;
        int result[] = new int[num];
        result[0] = head;
        for (int i = 1; i < num; i++)
            result[i] = getTag(result[i - 1], value[i - 1]);
        return result;
    }

    /**
     * Calculate Extended Dewey.
     */
    int getLabel(String parent, String child, int leftSilbling) {

        int parentID = ((Integer) map.get(parent)).intValue();
        int childID = ((Integer) map.get(child)).intValue();
        int tagPosiitonInRule = -1;
        for (int i = 1; i <= tagsNumberOfRule[parentID]; i++)
            if (DTDrule[parentID][i] == childID) {
                tagPosiitonInRule = i;
                break;
            }//end if

        int leftSiblingPosition = leftSilbling % tagsNumberOfRule[parentID];
        if (leftSilbling % tagsNumberOfRule[parentID] == 0) leftSiblingPosition = tagsNumberOfRule[parentID];

        if (leftSilbling == -1) // child is the first child
            return tagPosiitonInRule;
        else
            return calculateX(leftSiblingPosition, tagPosiitonInRule, leftSilbling, tagsNumberOfRule[parentID]);

    }//end getLabel


    int calculateX(int w, int k, int y, int r) {
        int floorValue = y / r;
        int ceilValue ;
        if (y % r != 0)
            ceilValue = y / r + 1;
        else
            ceilValue = y / r;
        if (w < k)
            return floorValue * r + k;
        else
            return ceilValue * r + k;
    }//end calculateX

    static public void main(String[] args) {
        DTDTable test = new DTDTable();
        //test.initilizeTable();
        int[] value = {5, 2, 2, 1, 6};
        //int result[] = test.getAllTags(value, 1);
        //for(int i=0;i<result.length;i++)
        //System.out.println(result[i]);
        int testLabel = test.getLabel("c", "a", -1);
        System.out.println("Assigned value is " + testLabel);


    }//end main


}//end DTDTable