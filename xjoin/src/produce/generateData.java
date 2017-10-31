package produce;

import java.io.EOFException;
import java.io.File;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class generateData {

    public static void buildRDBValue() throws  Exception{
        PrintWriter pw = new PrintWriter(new File("xjoin/src/multi_rbds/testTables/test.csv"));
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        for(int i=0; i<10000; i++){
            sb.append(rand.nextInt(50)+"");
            sb.append(',');
            sb.append(rand.nextInt(50)+"");
            sb.append('\n');
        }
        pw.write(sb.toString());
        pw.close();
        System.out.println("done!");
    }
    public static void main(String[] args) throws Exception{
        buildRDBValue();
    }
}
