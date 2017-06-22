package produce;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.*;

public class matchValue_XML_Table{

    public Map<Integer,int[]> matchRDB(String twigL, String twigR) throws Exception{
        Map<Integer,int[]> resultValueMap = new HashMap<>(); // save matched value pair
        int count = 0;
        File file = new File("xjoin/src/table.xlsx");
        Integer twigL_n=null; //The position of left tag and right tag in RDB table
        Integer twigR_n=null;
        FileInputStream fIP = new FileInputStream(file);//read excel file
        XSSFWorkbook workbook = new XSSFWorkbook(fIP);
        Sheet sheet = workbook.getSheetAt(0);
        //Iterator<Row> iterator = sheet.iterator();
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
                try{
                RandomAccessFile rL = new RandomAccessFile("xjoin/src/outputData/"+twigL+"_v","rw");
                RandomAccessFile rR = new RandomAccessFile("xjoin/src/outputData/"+twigR+"_v","rw");
                while (true)
                { 	// different 'x'_v file need to be read in different byte length
                    byte lenL = rL.readByte();
                    byte lenR = rR.readByte();
                    //read tag value in xml
                    int [] dataL = new int [lenL];
                    int [] dataR = new int [lenR];
                    for(int i=0;i<lenL;i++){
                        dataL[i] = rL.readUnsignedByte();//value in 'L'_v file
                        //System.out.print(dataL[i]);
                        if(dataL[i] == cellL.getNumericCellValue()){
                            for(int j=0; j<lenR; j++){
                                dataR[j] = rR.readUnsignedByte();//value in 'R'_v file
                                if(dataR[j] == cellR.getNumericCellValue()){
                                    int[] positionPair  = {dataL[i],dataR[j]};
                                    resultValueMap.put(count,positionPair);
                                    count++ ;
                                    //System.out.print(twigL +" :"+dataL[i] + " i:"+i+" "+twigR+" :"+dataR[j]+" j:"+j);
                                }
                            }
                        }
                    }

                    //System.out.println();
                }}
                catch (EOFException eofex) {
                    //do nothing
                }
                catch(Exception e){
                System.out.println("e is "+e);
            }
            }
        }}
        else {System.out.println("The twig have not been found in RDB table.");}
        return resultValueMap;
    }

    //find out all the ids that match the value in XML
    public List<int[]> getIDList(String tag, int value)  throws Exception{
        List<int[]> idList = new ArrayList<>();
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
                for(int i=0;i<len;i++){
                    data[i] = r.readUnsignedByte();
                }
                for(int j=0;j<len_v;j++){
                    data_v[j] = r_v.readUnsignedByte();
                    if(data_v[j] == value){
                        idList.add(data);
                        //System.out.println("tag:"+tag + " value:"+ Arrays.toString(data_v) +" id:"+Arrays.toString(data));
                    }
                }
                System.out.println();
            }}
        catch (EOFException eofex) {
            //do nothing
        }
            catch(Exception e){
            System.out.println("e is "+e);
        }
        //end catch
        //for(int[] i:idList){
        //    System.out.println("All id:"+Arrays.toString(i));
        //}
        return idList;
    }
    // @@@@@@@@@@@need to add function to check whether list is null
    //get all the id streams that match the value(XML & RDB), value is already matched in matchRDB step
    public void matchIDList(String twigL, String twigR) throws Exception{
        try{
            Map<Integer,int[]> map = matchRDB(twigL,twigR);
            for(Integer key:map.keySet()){
                //System.out.println(key + ":" + map.get(key)[0]+","+map.get(key)[1]);
                List<int[]> idL = getIDList(twigL,map.get(key)[0]);//result
                List<int[]> idR = getIDList(twigR,map.get(key)[1]);//result

                //following code is to print out the result to see if the result is correct
                System.out.println("tag:"+twigL+" value:"+map.get(key)[0]);
                for(int[] i:idL){System.out.println("Id:"+Arrays.toString(i));}

                System.out.println("tag:"+twigR+" value:"+map.get(key)[1]);
                for(int[] i:idR){System.out.println("Id:"+Arrays.toString(i));}
            }
        }
        catch (Exception e){
            System.out.println("e is "+e);
        }

    }


    public static void main(String[] args)  throws Exception{
        /**

        Map<Integer,int[]> map = new matchValue_XML_Table().matchRDB("b","c");
        for(Integer key:map.keySet()){
            System.out.println(key + ":" + map.get(key)[0]+","+map.get(key)[1]);
        }

        List<int[]> id = new matchValue_XML_Table().getIDList("b",18);

         */
        matchValue_XML_Table m = new matchValue_XML_Table();
        m.matchIDList("b","c");
    }
}