package produce;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * Created by zzzhou on 2017-06-27.
 * Final version of label Matching.
 */
public class labelMatchingListSort {
    private static List<Match> result = new ArrayList<>();
    public static class Match {
        private String leftTagValue;
        private String rightTagValue;
        private List leftTagID;
        private List rightTagID;
        Match(String l_v, String r_v, List l_id, List r_id) {
            this.leftTagValue = l_v;
            this.rightTagValue = r_v;
            this.leftTagID = l_id;
            this.rightTagID = r_id;
        }
        void set_LID(List l_id){this.leftTagID = l_id;}
        void set_RID(List r_id){this.rightTagID = r_id;}
        public String toString() {
            return String.format("{%s , %s , %s , %s}", leftTagValue, rightTagValue, leftTagID,rightTagID);
        }
        String getL_v() { return leftTagValue; }
        String getR_v() { return rightTagValue; }
        List getL_ID(){ return leftTagID;}
        List getR_ID(){ return rightTagID;}
    }

    public void readRDBValue(String twigL, String twigR) throws Exception{
        IdentityHashMap<String,String> tableMap = new IdentityHashMap<>(); // save matched value pair
        File file = new File("xjoin/src/table.xlsx");
        Integer twigL_n=null; //The position of left tag and right tag in RDB table
        Integer twigR_n=null;
        FileInputStream fIP = new FileInputStream(file);//read excel file
        XSSFWorkbook workbook = new XSSFWorkbook(fIP);
        Sheet sheet = workbook.getSheetAt(0);
        //Find specified columns by comparing column names in first row
        Row first_row = sheet.getRow(0);
        for (int cn=first_row.getFirstCellNum(); cn<first_row.getLastCellNum(); cn++) {
            Cell c = first_row.getCell(cn);
            if(c.toString().equals(twigL)){
                twigL_n = cn;
            }
            if(c.toString().equals(twigR)){
                twigR_n = cn;
            }
        }
        // if left child and right child of twig exists in RDB table
        if(twigL_n != null && twigR_n != null){
            //Compare values
            for (Row row:sheet){
                int i = row.getRowNum();
                // if cell is numerical value, start from second row(first row is name of tags)
                if(i != 0){
                    Cell cellL = row.getCell(twigL_n);
                    Cell cellR = row.getCell(twigR_n);
                    String left = null;
                    String right = null;
                    // check value type since different type value need to be read by different method
                    //left
                    if(cellL.getCellTypeEnum()== CellType.NUMERIC){
                        left = String.valueOf((int)cellL.getNumericCellValue());
                    }
                    else if(cellL.getCellTypeEnum()== CellType.STRING){
                        left = cellL.getStringCellValue();
                    }
                    //right
                    if(cellR.getCellTypeEnum()== CellType.NUMERIC){
                        right = String.valueOf((int)cellR.getNumericCellValue());
                    }
                    else if(cellR.getCellTypeEnum()== CellType.STRING){
                        right = cellR.getStringCellValue();
                    }

                    //Add table value to map
                    result.add(new Match(left,right,null,null));

                }
                //tableMap.put(cellL.getStringCellValue(),cellR.getStringCellValue());
            }
        }
        else {System.out.println("The twig have not been found in RDB table.");}
    }

    public List<List<String>> getTagMap(String tag)  throws Exception{
        List<List<String>> tagList = new ArrayList<>();
        try{
            RandomAccessFile r = null;
            RandomAccessFile r_v = null;
            r = new  RandomAccessFile("xjoin/src/produce/outputData/"+tag,"rw");//read id file
            r_v = new  RandomAccessFile("xjoin/src/produce/outputData/"+tag+"_v","rw");//read value file
            while (true)
            { 	byte len = r.readByte();
                byte len_v = r_v.readByte();
                int [] data = new int [len];
                int [] data_v = new int [len_v];
                String value = null;
                String id = "";
                for(int i=0;i<len;i++){
                    data[i] = r.readUnsignedByte();
                    id = id+"/"+data[i];
                }
                for(int j=0;j<len_v;j++){
                    data_v[j] = r_v.readUnsignedByte();
                    value = String.valueOf(data_v[j]);
                }
                List<String> l = new ArrayList<>();//every row [value, id]
                l.add(value);l.add(id);
                tagList.add(l);//value, id
            }}
        catch (EOFException eofex) {
            //do nothing
        }
        catch(Exception e){
            System.out.println("e is "+e);
        }
        Collections.sort(tagList,new Comparator<List<String>>(){
            public int compare(List<String> l1, List<String> l2){
                return l1.get(0).compareTo(l2.get(0));
            }}
        );
        return tagList;
    }

    // tFlag{left,right} -> left column value or right column value of table
    public void matchValue(List<Match> result, List<List<String>> tagList, String tFlag){
        if(tFlag.equals("left")) {
            int i=0; int j = 0;
            while(i != result.size() && j != tagList.size()){
                String table_value = result.get(i).getL_v();
                //String rightValue = result.get(i).getR_v();
                List id_list = new ArrayList<>();//To store id list for every row
                String tag_value = tagList.get(j).get(0); // 0-value, 1-id
                int compare_result = table_value.compareTo(tag_value);
                if (compare_result == 0) { //equals
                    if(result.get(i).getL_ID() != null)
                        id_list = result.get(i).getL_ID();
                    id_list.add(tagList.get(j).get(1));// add corresponding tag id
                    result.get(i).set_LID(id_list);
                    j++;
                }
                else if (compare_result < 0){ // table_value < tag_value
                    i++;
                    //previous table value equals current table value
                    if(table_value.equals(result.get(i).getL_v())){
                        id_list = result.get(i-1).getL_ID();
                        result.get(i).set_LID(id_list);
                        i++;
                    }
                }
                else if(compare_result > 0){ // table_value > tag_value
                    j++;
                }
            }
        }

        else if(tFlag.equals("right")) {
            int i=0; int j = 0;
            while(i != result.size() && j != tagList.size()){
                String table_value = result.get(i).getR_v();
                List<String> id_list = new ArrayList<>();//To store id list for every row
                String tag_value = tagList.get(j).get(0); // 0-value, 1-id
                int compare_result = table_value.compareTo(tag_value);
                if (compare_result == 0) { //equals
                    if(result.get(i).getR_ID() != null)
                        id_list = result.get(i).getR_ID();
                    id_list.add(tagList.get(j).get(1));// add corresponding tag id
                    result.get(i).set_RID(id_list);
                    j++;
                }
                else if (compare_result < 0){ // table_value < tag_value
                    i++;
                    //previous table value equals current table value
                    if(table_value.equals(result.get(i).getR_v())){
                        id_list = result.get(i-1).getR_ID();
                        result.get(i).set_RID(id_list);
                        i++;
                    }
                }
                else if(compare_result > 0){ // table_value > tag_value
                    j++;
                }
            }
        }
    }

    public List<Match> getSolution(List<Match> result)  throws Exception{
        labelMatchingListSort m = new labelMatchingListSort();
        List<List<String>> b_tag = m.getTagMap("b");
        List<List<String>> c_tag = m.getTagMap("c");
        System.out.println("b "+b_tag);
        System.out.println("c "+c_tag);

        m.readRDBValue("b","c");

        Comparator<Match> comparator = Comparator.comparing(Match::getL_v);
        result.sort(comparator);
        m.matchValue(result,b_tag,"left");

        comparator = Comparator.comparing(Match::getR_v);
        result.sort(comparator);
        m.matchValue(result,c_tag,"right");
        System.out.println(result + " size:"+result.size());
        int i = 0;
        while(i != result.size()){
            //System.out.println("i:"+i+" l:" + result.get(i).getL_ID() + " r:"+result.get(i).getR_ID());
            if(result.get(i).getL_ID() == null || result.get(i).getR_ID() == null)
            {
                result.remove(i);
                i--;
            }
            i++;
        }
        return result;
    }

    public static void main(String[] args) throws Exception{
        labelMatchingListSort lm = new labelMatchingListSort();
        List<Match> re = lm.getSolution(result);

        System.out.println(re);
    }
}
