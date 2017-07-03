package produce;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

/**
 * Created by zzzhou on 2017-06-27.
 * Final version of label Matching.
 */
public class labelMatching {

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
        public List getL_ID(){ return leftTagID;}
        public List getR_ID(){ return rightTagID;}
    }

    // this function still need modification to meet analysis the tag name automatically
    public List<Match> readRDBValue_line(String twigL, String twigR) throws Exception{
        List<Match> result = new ArrayList<>();
        String csvFile = "xjoin/src/table.csv";
        String line = "";
        String cvsSplitBy = ",";
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] str = line.split(cvsSplitBy);

                List<String> list = Arrays.asList(str[0].split("\\|"));
                if(str.length > 5 && list.size() > 2){
                //System.out.println("asin: " + list.get(0) + " price:" + list.get(2) );
                count++;
                result.add(new Match(list.get(0),list.get(2),null,null));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("valid RDB row:"+count);
        return  result;
    }

    public List<Match> readRDBValue(String twigL, String twigR) throws Exception{
        List<Match> result = new ArrayList<>();
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
        return  result;
    }

    public List<List<String>> getTagMap(String tag)  throws Exception{
        List<List<String>> tagList = new ArrayList<>();
        try{
            //outputLabel.readUTF8_v(tag);
            RandomAccessFile r = null;
            RandomAccessFile r_v = null;
            r = new  RandomAccessFile("xjoin/src/produce/outputData/"+tag,"rw");//read id file
            r_v = new  RandomAccessFile("xjoin/src/produce/outputData/"+tag+"_v","rw");//read value file
            r_v.seek(0);
            String value = null;
            while ((value=r_v.readUTF()) != null)
            { 	byte len = r.readByte();
                int [] data = new int [len];
                //String data_v = null;

                String id = "";
                for(int i=0;i<len;i++){
                    data[i] = r.readUnsignedByte();
                    id = id+"/"+data[i];
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
                    Collections.sort(id_list);
                    result.get(i).set_LID(id_list);
                    j++;
                }
                else if (compare_result < 0){ // table_value < tag_value
                    i++;
                    //previous table value equals current table value
                    if(i!= result.size() && table_value.equals(result.get(i).getL_v())){
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
                    Collections.sort(id_list);
                    result.get(i).set_RID(id_list);
                    j++;
                }
                else if (compare_result < 0){ // table_value < tag_value
                    i++;
                    //previous table value equals current table value
                    if(i!= result.size() && table_value.equals(result.get(i).getR_v())){
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

    public List<Match> getSolution(String leftTag, String rightTag)  throws Exception{
        labelMatching m = new labelMatching();
        //left_tag/right_tag -> left/right id list
        long loadbeginTime = 0L;
        long loadendTime = 0L;
        long loadRDBbeginTime = 0L;
        long loadRDBendTime = 0L;
        long sortbeginTime = 0L;
        long sortendTime = 0L;
        long totalSortTime = 0L;
        long matchbeginTime = 0L;
        long matchendTime = 0L;
        long totalMatchTime = 0L;

        //Load tag value and id

        loadbeginTime = System.currentTimeMillis();
        List<List<String>> left_tag = m.getTagMap(leftTag);
        List<List<String>> right_tag = m.getTagMap(rightTag);
        loadendTime = System.currentTimeMillis();
        System.out.println("Total load tag value and ID time is " + (loadendTime-loadbeginTime ));
        //System.out.println(leftTag+" "+left_tag);
        //System.out.println(rightTag+" "+right_tag);

        //Load RDB value
        loadRDBbeginTime = System.currentTimeMillis();
        List<Match> result =m.readRDBValue_line(leftTag,rightTag);
        loadRDBendTime = System.currentTimeMillis();
        System.out.println("Total load RDB tag value time is " + (loadRDBendTime-loadRDBbeginTime ));

        sortbeginTime = System.currentTimeMillis();
        Comparator<Match> comparator = Comparator.comparing(Match::getL_v);
        result.sort(comparator);
        sortendTime = System.currentTimeMillis();
        totalSortTime = sortendTime-sortbeginTime;

        matchbeginTime = System.currentTimeMillis();
        m.matchValue(result,left_tag,"left");
        matchendTime = System.currentTimeMillis();
        totalMatchTime = matchendTime - matchbeginTime;

        sortbeginTime = System.currentTimeMillis();
        comparator = Comparator.comparing(Match::getR_v);
        result.sort(comparator);
        sortendTime = System.currentTimeMillis();
        totalSortTime = totalSortTime + sortendTime-sortbeginTime;

        matchbeginTime = System.currentTimeMillis();
        m.matchValue(result,right_tag,"right");
        matchendTime = System.currentTimeMillis();
        totalMatchTime = totalMatchTime + matchendTime - matchbeginTime;
        System.out.println("total sort time: " + totalSortTime);
        System.out.println("total match xml and RDB value time: " + totalMatchTime);
        //System.out.println(result + " size:"+result.size());
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
        labelMatching lm = new labelMatching();
        List<Match> re = lm.getSolution("asin","price");
        //System.out.println(re);
        //lm.readRDBValue_line("asin","price");
    }
}
