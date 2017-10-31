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

    static public void main(String[] args){
        List<String> list = new ArrayList<>(Arrays.asList("a","b","c"));
        List<List<String>> combines = tagCombine(list);
        System.out.println(combines);
    }
}
