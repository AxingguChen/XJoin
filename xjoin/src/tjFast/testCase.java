package tjFast;

/**
 * Created by zzzhou on 2018-01-15.
 */
public class testCase {

    public void xjoinTestCase1() throws Exception {
        //xjoin test case 1: PC
        String xml_query_file = "xjoin/src/testCaseDataSet/testCase1/xml_query_file.xml";
        String xml_document_file = "xjoin/src/testCaseDataSet/testCase1/xml_document_file.xml";
        String rdb_table = "xjoin/src/testCaseDataSet/testCase1/rdb_table.csv";
        queryAnalysis testCase1 = new queryAnalysis();
        testCase1.runTest(xml_query_file, xml_document_file, rdb_table);
    }

    public void xjoinTestCase2() throws Exception {
        //xjoin test case 2: AD-1
        String xml_query_file = "xjoin/src/testCaseDataSet/testCase2/xml_query_file.xml";
        String xml_document_file = "xjoin/src/testCaseDataSet/testCase2/xml_document_file.xml";
        String rdb_table = "xjoin/src/testCaseDataSet/testCase2/rdb_table.csv";
        queryAnalysis testCase2 = new queryAnalysis();
        testCase2.runTest(xml_query_file, xml_document_file, rdb_table);
    }

    public void xjoinTestCase3() throws Exception {
        //xjoin test case 3: AD-2
        String xml_query_file = "xjoin/src/testCaseDataSet/testCase3/xml_query_file.xml";
        String xml_document_file = "xjoin/src/testCaseDataSet/testCase3/xml_document_file.xml";
        String rdb_table = "xjoin/src/testCaseDataSet/testCase3/rdb_table.csv";
        queryAnalysis testCase2 = new queryAnalysis();
        testCase2.runTest(xml_query_file, xml_document_file, rdb_table);
    }

    public void naiveTestCase1() throws Exception{
        //naive test case 1: PC
        String xml_query_file = "xjoin/src/testCaseDataSet/testCase1/xml_query_file.xml";
        String xml_document_file = "xjoin/src/testCaseDataSet/testCase1/xml_document_file.xml";
        String rdb_table = "xjoin/src/testCaseDataSet/testCase1/rdb_table";
        queryAnalysis testCase1 = new queryAnalysis();
        testCase1.runTest(xml_query_file, xml_document_file, rdb_table);
    }

    public void naiveTestCase2() throws Exception{
        //naive test case 1: PC
        String xml_query_file = "xjoin/src/testCaseDataSet/testCase1/xml_query_file.xml";
        String xml_document_file = "xjoin/src/testCaseDataSet/testCase1/xml_document_file.xml";
        String rdb_table = "xjoin/src/testCaseDataSet/testCase1/rdb_table";
        queryAnalysis testCase1 = new queryAnalysis();
        testCase1.runTest(xml_query_file, xml_document_file, rdb_table);
    }



    static public void main(String[] args) throws Exception {
        testCase t = new testCase();
        //test case 1
        t.xjoinTestCase1();
//        t.naiveTestCase1();


    }
}
