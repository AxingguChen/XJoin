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
import java.util.HashMap;
import java.util.List;

/**
 * Created by zzzhou on 2017-06-22.
 */
public class labelMatching_test {
    HashMap<Integer,String> mapL = new HashMap<>();
    HashMap<Integer,String> mapR = new HashMap<>();
    HashMap<String,String> tagMap = new HashMap<>();
    //read Table value from excel table
    public void readRDBValue(String twigL, String twigR) throws Exception{
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
                int rowNum = row.getRowNum();//row No.
                // if cell is numerical value, start from second row(first row is name of tags)
                if( rowNum != 0){
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
                    mapL.put(rowNum,left);
                    mapR.put(rowNum,right);
                }
                //tableMap.put(cellL.getStringCellValue(),cellR.getStringCellValue());
            }
        }
        else {System.out.println("The twig have not been found in RDB table.");}

    }

    public void matchValue(String tag, HashMap<Integer,String> map) throws Exception{
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
    }

}
