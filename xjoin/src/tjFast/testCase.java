package tjFast;

import java.util.Arrays;
import java.util.List;

/**
 * Created by zzzhou on 2018-01-15.
 */
public class testCase {

    public void xjoinTestCase1() throws Exception {
        //xjoin test case 1: PC
        String xml_query_file = "xjoin/src/testCaseDataSet/testCase1/xml_query_file.xml";
        String xml_document_file = "xjoin/src/testCaseDataSet/testCase1/xml_document_file.xml";
        String rdb_table = "xjoin/src/testCaseDataSet/testCase1/rdb_table.csv";
        List<String> tagList = Arrays.asList("asin","price");
        queryAnalysis testCase1 = new queryAnalysis();
        testCase1.runTest(xml_query_file, xml_document_file, rdb_table,tagList);
    }

    public void xjoinTestCase2() throws Exception {
        //xjoin test case 2: AD-1
        String xml_query_file = "xjoin/src/testCaseDataSet/testCase2/xml_query_file.xml";
        String xml_document_file = "xjoin/src/testCaseDataSet/testCase2/xml_document_file.xml";
        String rdb_table = "xjoin/src/testCaseDataSet/testCase2/rdb_table.csv";
        List<String> tagList = Arrays.asList("asin","price");
        queryAnalysis testCase2 = new queryAnalysis();
        testCase2.runTest(xml_query_file, xml_document_file, rdb_table,tagList);
    }

    public void xjoinTestCase3() throws Exception {
        //xjoin test case 3: AD-2
        String xml_query_file = "xjoin/src/testCaseDataSet/testCase3/xml_query_file.xml";
        String xml_document_file = "xjoin/src/testCaseDataSet/testCase3/xml_document_file.xml";
        String rdb_table = "xjoin/src/testCaseDataSet/testCase3/rdb_table.csv";
        List<String> tagList = Arrays.asList("asin","price");
        queryAnalysis testCase3 = new queryAnalysis();
        testCase3.runTest(xml_query_file, xml_document_file, rdb_table,tagList);
    }

    public void naiveTestCase1() throws Exception{
        //naive test case 1: PC
        Boolean doubleAD = false;
        String xml_query_file = "xjoin/src/testCaseDataSet/testCase1/xml_query_file.xml";
        String xml_document_file = "xjoin/src/testCaseDataSet/testCase1/xml_document_file.xml";
        String rdb_table = "xjoin/src/testCaseDataSet/testCase1/rdb_table.csv";
        List<String> tagList = Arrays.asList("asin","price");
        queryAnalysis_naive testCase1 = new queryAnalysis_naive();
        testCase1.runTest(xml_query_file, xml_document_file, rdb_table,tagList,doubleAD);
    }

    public void naiveTestCase2() throws Exception{
        //naive test case 2: AD-1
        Boolean doubleAD = false;
        String xml_query_file = "xjoin/src/testCaseDataSet/testCase2/xml_query_file.xml";
        String xml_document_file = "xjoin/src/testCaseDataSet/testCase2/xml_document_file.xml";
        String rdb_table = "xjoin/src/testCaseDataSet/testCase2/rdb_table.csv";
        List<String> tagList = Arrays.asList("asin","price");
        queryAnalysis_naive testCase2 = new queryAnalysis_naive();
        testCase2.runTest(xml_query_file, xml_document_file, rdb_table,tagList,doubleAD);
    }

    public void naiveTestCase3() throws Exception{
        //naive test case 1: PC
        String xml_query_file = "xjoin/src/testCaseDataSet/testCase3/xml_query_file.xml";
        String xml_document_file = "xjoin/src/testCaseDataSet/testCase3/xml_document_file.xml";
        String rdb_table = "xjoin/src/testCaseDataSet/testCase3/rdb_table.csv";
        List<String> tagList = Arrays.asList("asin","price");
        queryAnalysis testCase3 = new queryAnalysis();
        testCase3.runTest(xml_query_file, xml_document_file, rdb_table, tagList);
    }

    public void xjoinTestCase1_double() throws Exception {
        //xjoin test case 3: AD-2
        String xml_query_file = "xjoin/src/testCaseDataSet/testCase1_double/xml_query_file.xml";
        String xml_document_file = "xjoin/src/testCaseDataSet/testCase1_double/xml_document_file.xml";
        String rdb_table = "xjoin/src/testCaseDataSet/testCase1_double/rdbTable1wSmallResult.csv";
//        String rdb_table = "xjoin/src/testCaseDataSet/testCase1_double/rdbTable20wSmallResult.csv";
//        String rdb_table = "xjoin/src/testCaseDataSet/testCase1_double/rdbTable100wSmallResult.csv";
        List<String> tagList = Arrays.asList("asin","price","OrderId");
        queryAnalysis testCase1_d = new queryAnalysis();
        testCase1_d.runTest(xml_query_file, xml_document_file, rdb_table,tagList);
    }

    public void naiveTestCase1_double() throws Exception{
        //naive test case 1: PC
        Boolean doubleAD = false;
        String xml_query_file = "xjoin/src/testCaseDataSet/testCase1_double/xml_query_file.xml";
        String xml_document_file = "xjoin/src/testCaseDataSet/testCase1_double/xml_document_file.xml";
        String rdb_table = "xjoin/src/testCaseDataSet/testCase1_double/rdbTable1wSmallResult.csv";
//        String rdb_table = "xjoin/src/testCaseDataSet/testCase1_double/rdbTable20wSmallResult.csv";
//        String rdb_table = "xjoin/src/testCaseDataSet/testCase1_double/rdbTable100wSmallResult.csv";
        List<String> tagList = Arrays.asList("asin","price","OrderId");
        queryAnalysis_naive testCase1 = new queryAnalysis_naive();
        testCase1.runTest(xml_query_file, xml_document_file, rdb_table,tagList,doubleAD);
    }

    public void xjoinTestCase2_double() throws Exception {
        //P.S. in ppt, the experimental result comes from part of the full tag. asin_count:3000+ price_count: 3000,
        // but following setting is full size, thus the solution count may be different with ppt.
        //xjoin test case 2: AD
        String xml_query_file = "xjoin/src/testCaseDataSet/testCase2_double/xml_query_file.xml";
        String xml_document_file = "xjoin/src/testCaseDataSet/testCase2_double/xml_document_file.xml";
        String rdb_table = "xjoin/src/testCaseDataSet/testCase2_double/rdbTable1wSmallResult.csv";
//        String rdb_table = "xjoin/src/testCaseDataSet/testCase1_double/rdbTable20wSmallResult.csv";
//        String rdb_table = "xjoin/src/testCaseDataSet/testCase1_double/rdbTable100wSmallResult.csv";
        List<String> tagList = Arrays.asList("asin","price","OrderId");
        queryAnalysis testCase2_d = new queryAnalysis();
        testCase2_d.runTest(xml_query_file, xml_document_file, rdb_table,tagList);
    }

    public void naiveTestCase2_double() throws Exception {
        //P.S. in ppt, the experimental result comes from part of the full tag. asin_count:3000+ price_count: 3000,
        // but following setting is full size, thus the solution count may be different with ppt.
        //xjoin test case 2: AD
        Boolean doubleAD = true;
        String xml_query_file = "xjoin/src/testCaseDataSet/testCase2_double/xml_query_file.xml";
        String xml_document_file = "xjoin/src/testCaseDataSet/testCase2_double/xml_document_file.xml";
        String rdb_table = "xjoin/src/testCaseDataSet/testCase2_double/rdbTable1wSmallResult.csv";
//        String rdb_table = "xjoin/src/testCaseDataSet/testCase1_double/rdbTable20wSmallResult.csv";
//        String rdb_table = "xjoin/src/testCaseDataSet/testCase1_double/rdbTable100wSmallResult.csv";
        List<String> tagList = Arrays.asList("asin","price","OrderId");
        queryAnalysis_naive testCase2_d = new queryAnalysis_naive();
        testCase2_d.runTest(xml_query_file, xml_document_file, rdb_table,tagList,doubleAD);
    }

    static public void main(String[] args) throws Exception {
        //Single branching node
        testCase single_t = new testCase();
        //test case 1:PC
//        single_t.xjoinTestCase1();
//        single_t.naiveTestCase1();
//        //test case 2:AD-1
//        single_t.xjoinTestCase2();
//        single_t.naiveTestCase2();
//        //test case 3:AD-2
//        single_t.xjoinTestCase3();
//        single_t.naiveTestCase3();


        //Double branching nodes
        testCase double_t = new testCase();
        // test case 1: PC
//        double_t.xjoinTestCase1_double();
//        double_t.naiveTestCase1_double();
        //test case 2: AD
//        double_t.xjoinTestCase2_double();
        double_t.naiveTestCase2_double();

    }
}
