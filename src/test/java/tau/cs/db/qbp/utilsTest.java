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
    private  String patternTest="./files/tests/pattern/";
    private String isoTest="./files/tests/isomorphism/pat%d.ttl";
    private String provTest="./files/tests/provenance/prov%d.ttl";
    private List<BasicPattern> provenanceList=null;
    private  List<Pair<BasicPattern,Pair<Integer,Integer>>> patternList=null;
    private Map<Integer,BasicPattern> isoTestList=null;
    private Map<Pair<Integer,Integer>,Boolean> isoTestAnswers = null;
    @Before
    public void setUp() throws Exception {
        provenanceList=new ArrayList<>();
        for(int i=1;i<8;i++){
            provenanceList.add(RDF.model2Basicpattern(RDF.loadModel(String.format(provTest, i))));
        }

        isoTestList=new HashMap<Integer,BasicPattern>();
        for(int i=1;i<5;i++){
            isoTestList.put(i, RDF.file2BasicpatternWithVars(String.format(isoTest, i)));
        }

        isoTestAnswers = new HashMap<Pair<Integer,Integer>,Boolean>();
        isoTestAnswers.put(new Pair<Integer,Integer>(1,2),true);
        isoTestAnswers.put(new Pair<Integer,Integer>(1,3),false);
        isoTestAnswers.put(new Pair<Integer,Integer>(3,4),false);
        isoTestAnswers.put(new Pair<Integer,Integer>(2,4),false);

        File folder = new File(patternTest);
        File[] listOfFiles = folder.listFiles();
        patternList=new ArrayList<>();
        String pattern  = ".*(\\d+)_(\\d+).ttl";
        Pattern r = Pattern.compile(pattern,Pattern.DOTALL);
        for(File f : listOfFiles){

            Matcher m = r.matcher(f.getName());
            if(m.find()) {
                Integer p1_i = new Integer(m.group(1));
                Integer p2_i = new Integer(m.group(2));
                BasicPattern p = RDF.file2BasicpatternWithVars(f.getPath());

                patternList.add(new Pair<BasicPattern, Pair<Integer, Integer>>
                        (p, new Pair<Integer,Integer>(p1_i,p2_i)));




            }


        }


    }
    @Test
    public void isBgpIsomorphic() throws Exception {
        for(Map.Entry<Pair<Integer,Integer>,Boolean> entry : isoTestAnswers.entrySet()){
            Boolean b = utils.isBgpIsomorphic(new ElementPathBlock(isoTestList.get(entry.getKey().getLeft())),
                    new ElementPathBlock(isoTestList.
                    get(entry.getKey().getRight())));

            assertTrue(String.format("Test failed %d %d", entry.getKey().getLeft(),entry.getKey().getRight()),
                    b.compareTo(entry.getValue())==0);
        }
    }

    @After
    public void tearDown() throws Exception {
    }







}
