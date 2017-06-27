package produce;

/**
 * Created by zzzhou on 2017-06-27.
 */
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.*;
import java.io.*;
import java.util.Map.Entry;

public class lableMatching_hashmapNotSort {
    static IdentityHashMap<String,Integer> leftTable_map = new IdentityHashMap<>();
    static IdentityHashMap<String,Integer> rightTable_map = new IdentityHashMap<>();
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
                    leftTable_map.put(left,i);
                    rightTable_map.put(right,i);

                }
                //tableMap.put(cellL.getStringCellValue(),cellR.getStringCellValue());
            }
        }
        else {System.out.println("The twig have not been found in RDB table.");}
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

    public void matchValue(IdentityHashMap<String,String> tagMap, IdentityHashMap<String,Integer> tableMap){

        for (Entry<String, Integer> entry : tableMap.entrySet()) {
            String tableKey = entry.getKey(); // table value
            System.out.println("table value:"+tableKey);
            if(tagMap.containsKey(tableKey)){
                String tagID = tagMap.get(tableKey);
                System.out.println(tagID);
            }
            //entry.getValue();

        }

    }
    public static void main(String[] args)  throws Exception{
        lableMatching_hashmapNotSort m = new lableMatching_hashmapNotSort();
        m.readRDBValue("b","c");
        System.out.println("table_b:"+leftTable_map);
        System.out.println("table_c:"+rightTable_map);
        IdentityHashMap<String,String> tag_b = m.getTagMap("b");
        IdentityHashMap<String,String> tag_c = m.getTagMap("c");
        System.out.println("tag b:" + tag_b);
        System.out.println("tag c:" + tag_c);
        m.matchValue(tag_b,leftTable_map);
        System.out.println(tag_b.containsKey("14"));
        IdentityHashMap<String,String> n = new IdentityHashMap<>();
        String a=new String("12");
        String b = new String("12");
        n.put("18","12");
        n.put(a,"12");
        n.put("14","13");
        n.put(b,"13");
        System.out.print(n + " " +n.containsKey("12"));

    }

}
