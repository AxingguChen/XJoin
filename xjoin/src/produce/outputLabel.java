package produce;

import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Vector;
import java.io.*;

public class outputLabel {

    public outputLabel() {

    }//end outputLabel

    static void outputUTF8(String tag, int[] labels) {

        try {

            RandomAccessFile r = new RandomAccessFile("xjoin/src/produce/outputData\\" + tag, "rw");

            long fileLength = r.length();
            r.seek(fileLength);

            Vector v = new Vector();
            int totalLength = 0;

            for (int i = 0; i < labels.length; i++) {
                int outputByte[] = IntegerToUTF8(labels[i]);
                totalLength += outputByte.length;
                v.addElement(outputByte);

                //System.out.print(" "+labels[i]+" ");
            }//end for

            if (totalLength > 250) System.out.println(" Too large label size !!!" + totalLength);

            r.writeByte(totalLength);

            for (int i = 0; i < v.size(); i++) {
                int outputByte[] = (int[]) v.elementAt(i);
                for (int j = 0; j < outputByte.length; j++)
                    r.writeByte(outputByte[j]);
            }//end for

            r.close();

        } catch (Exception e) {
            System.out.println("e is " + e);

        }//end catch


    }//end output

    static void outputUTF8_V_byte(String tag, String value){
        byte[] array = value.getBytes();
    }

    static void outputUTF8_v(String tag, String value) {

        try {

            RandomAccessFile r = new RandomAccessFile("xjoin/src/produce/outputData\\" + tag +"_v", "rw");

            long fileLength = r.length();
            r.seek(fileLength);

            r.writeUTF(value);

            r.close();

        } catch (Exception e) {
            System.out.println("e is " + e);

        }//end catch


    }//end output

    static public void readUTF8_v(String tag) throws Exception{
        try{
            RandomAccessFile r = new RandomAccessFile("xjoin/src/produce/outputData\\" + tag +"_v", "rw");
            r.seek(0);
            String tmp = null;
            while ((tmp=r.readUTF()) != null){
                System.out.println("read v:"+tmp);
            }

        }
        catch (Exception e){System.out.println("e:"+e);}
    }

    static int[] IntegerToUTF8(int value) {

        if (value <= 127) {
            int[] r = new int[1];
            r[0] = value;
            return r;
        }//end
        else if (value <= 2047) {
            int[] r = new int[2];
            String s = decimalToBinary(value);
            if (s.length() < 11)
                s = addZeroesAtFront(s, 11);
            String front = s.substring(0, 5);
            String rear = s.substring(5, 11);

            r[0] = binaryToDecimal("110" + front);
            r[1] = binaryToDecimal("10" + rear);

            return r;
        }//end else
        else if (value <= 65535) {
            int[] r = new int[3];
            String s = decimalToBinary(value);
            if (s.length() < 16)
                s = addZeroesAtFront(s, 16);
            String front = s.substring(0, 4);
            String middle = s.substring(4, 10);
            String rear = s.substring(10, 16);

            r[0] = binaryToDecimal("1110" + front);
            r[1] = binaryToDecimal("10" + middle);
            r[2] = binaryToDecimal("10" + rear);

            return r;
        }//end else
        else if (value <= 2097151) {
            int[] r = new int[4];
            String s = decimalToBinary(value);
            if (s.length() < 21)
                s = addZeroesAtFront(s, 21);
            String front = s.substring(0, 3);
            String middle0 = s.substring(3, 9);
            String middle1 = s.substring(9, 15);
            String rear = s.substring(15, 21);

            r[0] = binaryToDecimal("11110" + front);
            r[1] = binaryToDecimal("10" + middle0);
            r[2] = binaryToDecimal("10" + middle1);
            r[3] = binaryToDecimal("10" + rear);

            return r;
        }//end else
        else if (value <= 67108863) {
            int[] r = new int[5];
            String s = decimalToBinary(value);
            if (s.length() < 26)
                s = addZeroesAtFront(s, 26);
            String front = s.substring(0, 2);
            String middle0 = s.substring(2, 8);
            String middle1 = s.substring(8, 14);
            String middle2 = s.substring(14, 20);
            String rear = s.substring(20, 26);

            r[0] = binaryToDecimal("111110" + front);
            r[1] = binaryToDecimal("10" + middle0);
            r[2] = binaryToDecimal("10" + middle1);
            r[3] = binaryToDecimal("10" + middle2);
            r[4] = binaryToDecimal("10" + rear);

            return r;
        }//end else
        else
            System.out.println("The integer " + value + " is too large so that it cannot be processed!");
        return null;
    }//end convertToUTF8

    static String addZeroesAtFront(String s, int targetlength) {


        if (s.length() > targetlength) {
            System.out.println("Length Wrong!");
            return null;
        }

        if (s.length() == targetlength) return s;

        String result = s;

        String zero = "0";

        for (int i = 0; i < targetlength - s.length(); i++)

            result = zero.concat(result);

        return result;


    }//end addZeroesAtFront


    static String decimalToBinary(int decimal) {


        int quotient = decimal / 2;
        int remainder = decimal % 2;

        String s = String.valueOf(remainder);

        while (quotient > 0)

        {
            remainder = quotient % 2;
            quotient = quotient / 2;
            String temp = String.valueOf(remainder);
            s = s.concat(temp);

        }//end while
        return converse(s);

    }//end decimalToBinary

    static String converse(String s) {

        String r = "";

        for (int i = s.length(); i > 0; i--)
            r = r.concat(s.substring(i - 1, i));

        return r;

    }//end converse

    static int binaryToDecimal(String binary) {

        byte[] b = binary.getBytes();
        double sum = 0;

        for (int i = 0; i < b.length; i++)
            if (b[i] == '1')
                sum = sum + Math.pow(2.0, (b.length - 1 - i) * 1.0);

        return (new Double(sum)).intValue();


    }//end binaryToDecimal

    static public void main(String[] args) throws Exception {
        /**
        FileInputStream in = null;
        in = new FileInputStream("xjoin/src/produce/outputData/a");
        int c;
        System.out.println("start");
        while ((c = in.read()) != -1) {
            System.out.print(c);
        }
        in.close();*/
        outputLabel o = new outputLabel();
        o.readUTF8_v("c");

    }//end main

}//end class
  
  
 