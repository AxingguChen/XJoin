package produce; /**
 * Created by zzzhou on 2017-06-20.
 * Control that the complexity of algorithm will not reach O(n^2)
 */

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.*;
import java.io.*;
import java.util.Map.Entry;
import java.util.Comparator;
import produce.KeyValueComparator.Type;

public class labelMatching_hashMapSort {
    static Map<Integer, ArrayList<String>> map_left= new HashMap<>();
    static Map<Integer, ArrayList<String>> map_right= new HashMap<>();
    public IdentityHashMap<String,String> readRDBValue(String twigL, String twigR) throws Exception{
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
                // if cell is numerical value, start from second row(first row is name of tags)
                if(row.getRowNum() != 0){
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
                    tableMap.put(left,right);

                    }
                    //tableMap.put(cellL.getStringCellValue(),cellR.getStringCellValue());
                }
            }
        else {System.out.println("The twig have not been found in RDB table.");}
        return tableMap;
    }

    // sort the value of the table according to the passing parameter <order> :{left, right}
    public List sortRDBValue(IdentityHashMap<String,String> map, String order){
        List<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String, String>>(map.entrySet());
        if(order.equals("left")){
            Collections.sort(list, new KeyValueComparator(Type.KEY));
            /**
            for (Map.Entry<String, String> entry : list1) {
                //System.out.println("\t" + entry.getKey() + "\t\t" + entry.getValue());
            }
            System.out.println(list1);*/
        }
        if(order.equals("right")){
            Collections.sort(list, new KeyValueComparator(Type.VALUE));
        }
        return list;
    }

    public IdentityHashMap<String,String> getTagMap(String tag)  throws Exception{
        IdentityHashMap<String,String> tagMap = new IdentityHashMap<>();
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
                tagMap.put(value, id);//value, id
            }}
        catch (EOFException eofex) {
            //do nothing
        }
        catch(Exception e){
            System.out.println("e is "+e);
        }
        return tagMap;
    }

    public List sortTagValue(IdentityHashMap<String,String> map){
        List<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String, String>>(map.entrySet());
        Collections.sort(list, new KeyValueComparator(Type.KEY));//sort by Type.KEY is to sort by value
        //System.out.println("list:"+list.get(0).getKey());
        return list;
    }

    // tFlag{left,right} -> left column value or right column value of table
    public void matchValue(List <Map.Entry<String, String>> tableList, List <Map.Entry<String, String>> tagList, String tFlag){
        if(tFlag.equals("left")) {
            int i = 0; // table iterator
            int j = 0; // tag iterator
            while (i != tableList.size() && j != tagList.size()) {
                String table_value = tableList.get(i).getKey();
                String tag_value = tagList.get(j).getKey();
                ArrayList<String> id_list = new ArrayList<>();//To store id list for every row
                //System.out.println("table:"+tableList.get(0)+" tag:"+tagList.get(0));
                // table_value = tag_value, add it to result list
                int compare_result = table_value.compareTo(tag_value);
                if (compare_result == 0) { //equals
                    if(map_left.containsKey(i))
                        id_list = map_left.get(i);
                    id_list.add(tagList.get(j).getValue());// add corresponding tag id
                    map_left.put(i,id_list);
                    j++;
                }
                else if (compare_result < 0){ // table_value < tag_value
                    i++;
                    //previous table value equals current table value
                    if(table_value.equals(tableList.get(i).getKey())){
                        id_list = map_left.get(i-1);
                        map_left.put(i,id_list);
                        i++;
                    }
                }
                else if(compare_result > 0){ // table_value > tag_value
                        j++;
                }
                //System.out.println(list_left);
            }
        }
        //compare right side value of the table with corresponding tag
        else if(tFlag.equals("right")) {
            int i = 0; // table iterator
            int j = 0; // tag iterator
            while (i != tableList.size() && j != tagList.size()) {
                String table_value = tableList.get(i).getValue();
                String tag_value = tagList.get(j).getKey();
                ArrayList<String> id_list = new ArrayList<>();//To store id list for every row
                //System.out.println("table:"+tableList.get(0)+" tag:"+tagList.get(0));
                // table_value = tag_value, add it to result list
                int compare_result = table_value.compareTo(tag_value);
                if (compare_result == 0) { //equals
                    if(map_right.containsKey(i))
                        id_list = map_right.get(i);
                    id_list.add(tagList.get(j).getValue());// add corresponding tag id
                    map_right.put(i,id_list);
                    j++;
                }
                else if (compare_result < 0){ // table_value < tag_value
                    i++;
                    String previous_value = table_value;
                    if(previous_value.equals(tableList.get(i).getKey())){
                        id_list = map_right.get(i-1);
                        map_right.put(i,id_list);
                        i++;
                    }
                }
                else if(compare_result > 0){ // table_value > tag_value
                    j++;
                }
                //System.out.println(list_left);
            }
        }
    }

    public static void main(String[] args)  throws Exception{
        labelMatching_hashMapSort m = new labelMatching_hashMapSort();
        IdentityHashMap<String,String> map = new IdentityHashMap<String,String>(); // save matched value pair
        map = m.readRDBValue("b","c");
        List<Map.Entry<String, String>> table_left = m.sortRDBValue(map,"left");
        System.out.println("table sort by b:" + table_left);

        IdentityHashMap<String,String> tagMap_b =m.getTagMap("b");
        List<Map.Entry<String, String>> tag_left = m.sortTagValue(tagMap_b);
        System.out.println("tag b:"+tag_left);
        m.matchValue(table_left,tag_left,"left");
        System.out.println("result left:"+map_left);

        List<Map.Entry<String, String>> table_right = m.sortRDBValue(map,"right");
        System.out.println("table sort by c:" + table_right);

        IdentityHashMap<String,String> tagMap_c =m.getTagMap("c");
        List<Map.Entry<String, String>> tag_right = m.sortTagValue(tagMap_c);
        System.out.println("tag c:"+tag_right);
        m.matchValue(table_right,tag_right,"right");
        System.out.println("result right:"+map_right);
    }

}

class KeyValueComparator implements Comparator<Map.Entry<String,String>>{
    enum Type{
        KEY, VALUE
    }
    private Type type;
    public KeyValueComparator(Type type){
        this.type = type;
    }
    @Override
    public int compare(Entry<String ,String> o1, Entry<String,String> o2){
        switch (type){
            case KEY:
                return o1.getKey().compareTo(o2.getKey());
            case VALUE:
                return o1.getValue().compareTo(o2.getValue());
            default:
                throw new RuntimeException("Wrong parameter type");
        }
    }
}