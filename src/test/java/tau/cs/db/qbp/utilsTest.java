package tau.cs.db.qbp;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tau.cs.db.utils.RDF;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;
/**
 * Created by efrat on 19/12/16.
 */


public class utilsTest {

    private List<BasicPattern> provenanceList=null;
    private  List<Pair<BasicPattern,Pair<Integer,Integer>>> patternList=null;
    private Map<Integer,BasicPattern> isoTestList=null;
    private Map<Pair<Integer,Integer>,Boolean> isoTestAnswers = null;
    @Before
    public void setUp() throws Exception {
        provenanceList=TestUtils.setProvList();
        patternList = TestUtils.setPatternList();
        isoTestList=TestUtils.setIsoTestist();
        isoTestAnswers=TestUtils.setIsoTestAnswers();


    }
    @Test
    public void isBgpIsomorphic() throws Exception {
        for(Map.Entry<Pair<Integer,Integer>,Boolean> entry : isoTestAnswers.entrySet()){
            Boolean b = utils.isBgpIsomorphic(new ElementPathBlock(isoTestList.get(entry.getKey().getLeft())).getPattern().getList(),
                    new ElementPathBlock(isoTestList.
                    get(entry.getKey().getRight())).getPattern().getList());

            assertTrue(String.format("Test failed %d %d", entry.getKey().getLeft(),entry.getKey().getRight()),
                    b.compareTo(entry.getValue())==0);
        }
    }

    @After
    public void tearDown() throws Exception {
    }







}
