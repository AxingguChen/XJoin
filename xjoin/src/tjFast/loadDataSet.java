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

    Hashtable[] loadAllLeafData(Match m, DTDTable DTDInfor,List<String> tagList) throws Exception{


        allData = new Hashtable();

        allOriginalData = new Hashtable();
        System.out.println("Row:"+m.toString());
        //tag list -- b,c (only consider the simplest case)
        Vector l[] = loadData(tagList.get(0), m.getL_ID(), DTDInfor);
        allOriginalData.put(tagList.get(0), l[0]);
        allData.put(tagList.get(0), l[1]);

        Vector r[] = loadData(tagList.get(1), m.getR_ID(),DTDInfor);
        allOriginalData.put(tagList.get(1), r[0]);
        allData.put(tagList.get(1), r[1]);


        Hashtable[] result = new Hashtable[2];//ע��[0] �Ƿ�����ֵ,��	[1]��tagֵ
        result[0] = allOriginalData;
        result[1] = allData;


        System.out.println("Total number of elements scanned is " + totalElement);
        return result;

    }//end loadAllLeafData

    int[] convertStrToInt(String ID){
        String id = ID.substring(1);
        String[] strArray =  id.split("/");
        int[] intArray = new int[strArray.length];
        for(int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
            System.out.println("hi:"+intArray[i]);
        }
        return intArray;
    }


        Vector[] loadData(String tag, List idList, DTDTable DTDInfor) {    //ע��loaddata[0] �Ƿ�����ֵ,��	loaddata[1]��tagֵ

        Vector[] loadedData = new Vector[2];
        loadedData[0] = new Vector();
        loadedData[1] = new Vector();

        try {
            System.out.println("now load data:" + tag);
            for(int i=0;i< idList.size();i++) {

                int[] data = convertStrToInt(idList.get(i).toString());

                int[] result = convertToIntegers(data); // result--tag ID

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

    //���������Ҫ��������һ������dtdtable �������������б�������������һ����Ӧ�Ĺ�ϵ, store them in variable map in class DTDTAble��

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