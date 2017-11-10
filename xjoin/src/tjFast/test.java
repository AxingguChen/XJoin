package tjFast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class test {
    public static List<List<String>> tagCombine(List<String> addedTags){
        List<List<String>> combines = new ArrayList<>();
        int size = addedTags.size();
        for(int start=0; start<size; start++){
            List<String> currentSubList = addedTags.subList(start, size -1);
            for(int end = currentSubList.size()-1; end>=0; end--){
                combines.add(currentSubList.subList(0,end));
            }
        }
        return combines;
    }

    public static int compareIds(List<int[]> ids){
        int compareResult = 0;
        Boolean isEqual = true;
        Boolean sizeIsEqual = true;
        int size_sFlag = 0;
        int allSize = ids.size();
        int smallSize = ids.get(0).length;
        //sizes are also need to be compared
        int[] idSizes = new int[allSize];
        //compare if size are the same length
        for(int i=1; i< allSize; i++){
            int curSize = ids.get(i).length;
            if(curSize !=smallSize){
                sizeIsEqual = false;
                if(curSize<smallSize){
                    smallSize = curSize;
                    size_sFlag = i;
                }
            }
        }
        for(int i=0; i< smallSize;i++){
            int baseV = ids.get(0)[i];
            for(int j=1; j<allSize; j++){
                int compV = ids.get(j)[i];
                if(compV != baseV){
                    isEqual = false;
                    if(compV < baseV){
                        compareResult = j;
                        baseV = compV;
                    }
                }
            }
        }
        //if the size is same
        if(!sizeIsEqual && isEqual){
            return size_sFlag;

        }else{
            if(isEqual) return -1;
            else return compareResult;
        }
    }

    static public void main(String[] args){
       int[] a = {1,1,2};
       int[] b = {1,1};
       int[] c = {1,1};
       List<int[]> idList = new ArrayList<>(Arrays.asList(a,b,c));
       int i = compareIds(idList);
       System.out.println(i);
    }
}
