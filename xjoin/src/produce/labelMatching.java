package produce; /**
 * Created by zzzhou on 2017-06-20.
 * Control that the complexity of algorithm will not reach O(n^2)
 */

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import produce.KeyValueComparator.Type;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.Map.Entry;

public class labelMatching {

    public IdentityHashMap<String,String> readRDBValue(String twigL, String twigR) throws Exception{
        IdentityHashMap<String,String> tableMap = new IdentityHashMap<>(); // save matched value pair
        int count = 0;
        File file = new File("xjoin/src/table.xlsx");
        Integer twigL_n=null; //The position of sortleft tag and right tag in RDB table
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
                        left = String.valueOf(cellL.getNumericCellValue());
                    }
                    else if(cellL.getCellTypeEnum()== CellType.STRING){
                        left = cellL.getStringCellValue();
                    }
                    //right
                    if(cellR.getCellTypeEnum()== CellType.NUMERIC){
                        right = String.valueOf(cellR.getNumericCellValue());
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
        List<Entry<String, String>> list = new ArrayList<Entry<String, String>>(map.entrySet());
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
            r = new  RandomAccessFile("xjoin/src/outputData/"+tag,"rw");//read id file
            r_v = new  RandomAccessFile("xjoin/src/outputData/"+tag+"_v","rw");//read value file
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
        List<Entry<String, String>> list = new ArrayList<Entry<String, String>>(map.entrySet());
        Collections.sort(list, new KeyValueComparator(Type.KEY));//sort by key is to sort by value
        return list;
    }

    public void matchValue(List tableList, List tagList){
        for (Entry<String, String> entry : list1) {
            //System.out.println("\t" + entry.getKey() + "\t\t" + entry.getValue());
        }

    }

    public static void main(String[] args)  throws Exception{
        labelMatching m = new labelMatching();
        IdentityHashMap<String,String> map = new IdentityHashMap<String,String>(); // save matched value pair
        map = m.readRDBValue("b","c");
        List<Entry<String, String>> list = m.sortRDBValue(map,"right");
        System.out.println(list);

        IdentityHashMap<String,String> tagMap =m.getTagMap("b");
        List a = m.sortTagValue(tagMap);
        System.out.println("a "+a);
    }

}

class KeyValueComparator implements Comparator<Entry<String,String>>{
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