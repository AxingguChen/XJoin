package produce; /**
 * Created by zzzhou on 12/06/2017.
 */
import java.io.RandomAccessFile;
import java.util.Hashtable;
import java.util.Vector;


public class loadDataSet {

    Hashtable allData;

    Hashtable allOriginalData;

    int totalElement = 0;





    Vector[] loadData(String tag, DTDTable DTDInfor) {    //注意loaddata[0] 是放整数值,而	loaddata[1]放tag值


        Vector[] loadedData = new Vector[2];
        loadedData[0] = new Vector();
        loadedData[1] = new Vector();

        RandomAccessFile r = null;

        try {
            r = new RandomAccessFile("src/produce/outputData\\" + tag, "rw");

            while (true) {
                byte len = r.readByte();
                //System.out.println("length is "+len);
                int[] data = new int[len];
                for (int i = 0; i < len; i++)
                    data[i] = r.readUnsignedByte();

                int[] result = convertToIntegers(data);

                int[] tagInt = DTDInfor.getAllTags(result, DTDInfor.root);

			/*for(int i=0;i<tagInt.length;i++)
				System.out.print(" "+tagInt[i]);
			System.out.println();*/

                loadedData[0].addElement(result);
                loadedData[1].addElement(tagInt);

                totalElement++;

            }//end while

        } catch (Exception e) {
            System.out.println("e is " + e);
        }//end catch
        finally {
            try {
                r.close();
            } catch (Exception e) {
            }
        }//end finally

        int[] terminal = {utilities.MAXNUM};

        loadedData[0].addElement(terminal);
        loadedData[1].addElement(terminal);

        return loadedData;

    }//end loadData

    static int[] convertToIntegers(int[] data) {

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

    //这个方法主要是用来（一）产生dtdtable ；（二）把所有变量和整数产生一个对应的关系, store them in variable map in class DTDTAble；

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
        try{
            RandomAccessFile r = null;
            r = new  RandomAccessFile("xjoin/src/produce/outputData/a","rw");
            while (true)
            { 	byte len = r.readByte();
                //System.out.println("length is "+len);
                int [] data = new int [len];
                for(int i=0;i<len;i++){
                    data[i] = r.readUnsignedByte();
                    System.out.print(data[i]);}
                System.out.println();
            }}catch(Exception e){
            System.out.println("e is "+e);
        }
        //end catch

    }



}//end loadDataSet