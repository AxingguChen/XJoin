package tjFast;

import java.util.*;
import java.io.*;
import produce.labelMatching;
import produce.labelMatching.Match;

public class loadDataSet {

    Hashtable allData;

    Hashtable allOriginalData;

    int totalElement = 0;
    labelMatching lm = new labelMatching();

    List<HashMap<String, String>> allTagIDValue = new ArrayList<>();
    public List<HashMap<String, String>> getAllTagIDValue() {
        return allTagIDValue;
    }
    Hashtable[] loadAllLeafData_naive(Vector leaves,DTDTable DTDInfor) throws Exception{


        allData = new Hashtable () ;

        allOriginalData = new Hashtable () ;


        for(int i=0;i<leaves.size();i++){
            Vector v [] = loadData_naive((String)leaves.elementAt(i),DTDInfor);
            allOriginalData.put( (String)leaves.elementAt(i), v[0]);
            allData.put( (String)leaves.elementAt(i), v[1]);

        }//end for

        Hashtable [] result = new Hashtable [2];//???[0] ????????,??	[1]??tag?
        result[0] = allOriginalData;
        result[1] = allData;


        System.out.println("Total number of elements scanned is "+totalElement);
        return result;

    }//end loadAllLeafData

    Vector []  loadData_naive (String tag,DTDTable DTDInfor ){	//???loaddata[0] ????????,??	loaddata[1]??tag?

        HashMap<String, String> tagMap = new HashMap();
        Vector [] loadedData = new Vector [2];
        loadedData[0] = new Vector();
        loadedData[1] = new Vector();

        RandomAccessFile r = null;
        RandomAccessFile r_v = null;
        try{
            r = new   RandomAccessFile("xjoin/src/produce/outputData\\"+tag,"rw");
            r_v = new RandomAccessFile("xjoin/src/produce/outputData/" + tag + "_v", "rw");//read value file
            r_v.seek(0);
            String value = null;
            //int count = 0;
            while ((value = r_v.readUTF()) != null )//&& count< 3000 )
            { 	byte len = r.readByte();
                //System.out.println("length is "+len);
                int [] data = new int [len];
                for(int i=0;i<len;i++)
                    data[i] = r.readUnsignedByte();

                int [] result = convertToIntegers (data);

                int [] tagInt = DTDInfor.getAllTags(result, DTDInfor.root);

                String id =  utilities.ArrayToString(result);
                tagMap.put(id, value);
			/*for(int i=0;i<tagInt.length;i++)
				System.out.print(" "+tagInt[i]);
			System.out.println();*/

                loadedData[0].addElement(result);
                loadedData[1].addElement(tagInt);

                totalElement++;
                //count++;

            }//end while

        }catch (EOFException eofex) {
            //do nothing
        }
        catch(Exception e){
            System.out.println("e is "+e);
        }//end catch
        finally {
            try {r.close();} catch (Exception e) {} }//end finally

        int [] terminal = {utilities.MAXNUM};

        loadedData[0].addElement(terminal);
        loadedData[1].addElement(terminal);

        allTagIDValue.add(tagMap);
        return loadedData ;

    }



    Hashtable[] loadAllLeafData(Vector candidate, DTDTable DTDInfor,List<String> tagList) throws Exception{


        allData = new Hashtable();

        allOriginalData = new Hashtable();
        //////
        //System.out.println("Row:"+m.toString());

        for(int tag=0;tag<tagList.size();tag++){
            Vector v[] = loadData((List<int[]>) candidate.get(tag*2+1),DTDInfor);
            allOriginalData.put(tagList.get(tag), v[0]);
            allData.put(tagList.get(tag), v[1]);
        }



        Hashtable[] result = new Hashtable[2];//???[0] ????????,??	[1]??tag?
        result[0] = allOriginalData;
        result[1] = allData;

        //////
        //System.out.println("Total number of elements scanned is " + totalElement);
        return result;

    }//end loadAllLeafData



    int[] convertStrToInt(String ID){
        String id = ID.substring(1);
        String[] strArray =  id.split("/");
        int[] intArray = new int[strArray.length];
        for(int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
            //System.out.println("hi:"+intArray[i]);
        }
        return intArray;
    }


        Vector[] loadData(List<int[]> idList, DTDTable DTDInfor) {    //???loaddata[0] ????????,??	loaddata[1]??tag?

        Vector[] loadedData = new Vector[2];
        loadedData[0] = new Vector();
        loadedData[1] = new Vector();

        try {
            //System.out.println("now load data:" + tag);
            for(int i=0;i< idList.size();i++) {

                int[] result = idList.get(i);

                //int[] result = convertToIntegers(data); // result--tag ID

                int[] tagInt = DTDInfor.getAllTags(result, DTDInfor.root); // tag
                /**
                System.out.println("tag:");
                for (int i = 0; i < tagInt.length; i++)
                    System.out.print(" " + tagInt[i]);
                System.out.println();

                System.out.println("ID:");
                for (int i = 0; i < result.length; i++)
                    System.out.print(" " + result[i]);
                System.out.println();*/

                loadedData[0].addElement(result);//all original data
                loadedData[1].addElement(tagInt);//

                totalElement++;

            }//end while

        } catch (Exception e) {
            System.out.println("e is " + e);
        }//end catch


        int[] terminal = {utilities.MAXNUM};

        loadedData[0].addElement(terminal);
        loadedData[1].addElement(terminal);

        return loadedData;

    }//end loadData

    int[] convertToIntegers(int[] data) {

        int inputi = 0, outputi = 0;

        int output[] = new int[data.length];

        while (inputi < data.length)
            if (data[inputi] < 128) {
                int a[] = new int[1];
                a[0] = data[inputi++];
                output[outputi++] = outputLabel.UTF8ToInteger(a);
            } else if (data[inputi] < 224) {
                int a[] = new int[2];
                a[0] = data[inputi++];
                a[1] = data[inputi++];
                output[outputi++] = outputLabel.UTF8ToInteger(a);
            } else if (data[inputi] < 240) {
                int a[] = new int[3];
                a[0] = data[inputi++];
                a[1] = data[inputi++];
                a[2] = data[inputi++];
                output[outputi++] = outputLabel.UTF8ToInteger(a);
            } else if (data[inputi] < 248) {
                int a[] = new int[4];
                a[0] = data[inputi++];
                a[1] = data[inputi++];
                a[2] = data[inputi++];
                a[3] = data[inputi++];
                output[outputi++] = outputLabel.UTF8ToInteger(a);
            } else if (data[inputi] < 252) {
                int a[] = new int[5];
                a[0] = data[inputi++];
                a[1] = data[inputi++];
                a[2] = data[inputi++];
                a[3] = data[inputi++];
                a[4] = data[inputi++];
                output[outputi++] = outputLabel.UTF8ToInteger(a);
            }
        int[] result = new int[outputi];

        for (int i = 0; i < outputi; i++)
            result[i] = output[i];

        return result;

    }//end convertToIntegers

    //?????????????????????????dtdtable ?????????????งา???????????????????????, store them in variable map in class DTDTAble??

    static DTDTable produceDTDInformation(String basicDocuemnt) {

        initilizeDTDTable initilize = new initilizeDTDTable();
        DTDTable dtdTable = null;

        try {
            dtdTable = initilize.initilizeTable(basicDocuemnt);

        } catch (Exception e) {
            System.out.println(e);
        }

        return dtdTable;

    }//end produceDTDInformation


    static public void main(String[] args) throws Exception {


        loadDataSet load = new loadDataSet();


        //load.loadData("POSS");

        //System.out.println("a is "+args[0]+" b is "+b2);


    }//end main


}//end loadDataSet