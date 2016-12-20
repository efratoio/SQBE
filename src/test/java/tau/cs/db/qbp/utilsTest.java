package tau.cs.db.qbp;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.sparql.core.BasicPattern;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tau.cs.db.utils.RDF;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.Assert.assertTrue;
/**
 * Created by efrat on 19/12/16.
 */


public class utilsTest {

    private  String patternTest="./files/tests/pattern/";
    private String provTest="./files/tests/provenance/prov%d.ttl";
    private List<BasicPattern> provenanceList=null;
    private  List<Pair<BasicPattern,Pair<Integer,Integer>>> patternList=null;
    @Before
    public void setUp() throws Exception {
        provenanceList=new ArrayList<>();
        for(int i=1;i<5;i++){
            provenanceList.add(RDF.model2Basicpattern(RDF.loadModel(String.format(provTest, i))));
        }
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

    @After
    public void tearDown() throws Exception {
    }




    @Test
    public void findBestMerge() throws Exception {
        for(Pair<BasicPattern,Pair<Integer,Integer>> pattern: patternList.subList(1,2)) {

            BasicPattern p_merge = utils.FindBestMerge(provenanceList.get(pattern.getRight().getLeft()-1),
                    provenanceList.get(pattern.getRight().getRight()-1));
            assertTrue(utils.isBgpIsomorphic(pattern.getLeft(),p_merge));
        }


    }



}
