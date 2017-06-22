package produce;

import java.util.Hashtable;
import java.util.Stack;


public class FileID2PathTable {

    static Hashtable ID2PathHash = new Hashtable();

    static int ID = 1;

    static String createFileID(Stack tagPathStack) {

        String path = convertPath2String(tagPathStack);

        if (ID2PathHash.containsKey(path))
            return (String) ID2PathHash.get(path);
        else {

            String s = ID + "";

            ID2PathHash.put(path, s);

            System.out.println(" path is " + path);

            ID++;


            return s;

        }


    }//end createFileID


    static String convertPath2String(Stack tagPathStack) {

        String s = "";

        for (int i = 0; i < tagPathStack.size() - 1; i++)

            s = s + (String) tagPathStack.elementAt(i) + "%"; // 这样每个名字之间用%隔开。

        s = s + (String) tagPathStack.elementAt(tagPathStack.size() - 1);

        return s;


    }//end convertPath2String

}//end class FileID2PathTable