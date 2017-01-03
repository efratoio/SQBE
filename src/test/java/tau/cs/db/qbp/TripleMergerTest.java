package tau.cs.db.qbp;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.path.PathFactory;
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
 * Created by efrat on 26/12/16.
 */
public class TripleMergerTest {
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

    @After
    public void tearDown() throws Exception {
    }
    @Test
    public void mergeBasic() throws Exception {
        List<TriplePath> l1 = new ArrayList<TriplePath>();
        List<TriplePath> l2 = new ArrayList<TriplePath>();
        l1.add(new TriplePath(NodeFactory.createURI("http://A"), PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://C")));
        l2.add(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://D")));

        TripleMerger tm = new TripleMerger(l1,l2);
        ElementPathBlock p_merge = tm.merge();
        ElementPathBlock p_test = new ElementPathBlock();
        p_test.addTriplePath(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createVariable("a")));
        assertTrue("Test failed with null ",p_merge!=null);
        assertTrue("Test failed %d %d"
                ,utils.isBgpIsomorphic(p_test,p_merge));

    }
    @Test
    public void mergeBasic2() throws Exception {
        List<TriplePath> l1 = new ArrayList<TriplePath>();
        List<TriplePath> l2 = new ArrayList<TriplePath>();
        l1.add(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://C")));
        l1.add(new TriplePath(NodeFactory.createURI("http://D"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://A")));
        l2.add(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://D")));
        l2.add(new TriplePath(NodeFactory.createURI("http://D"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://A")));

        TripleMerger tm = new TripleMerger(l1,l2);
        ElementPathBlock p_merge = tm.merge();
        ElementPathBlock p_test = new ElementPathBlock();
        p_test.addTriplePath(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createVariable("a")));
        p_test.addTriplePath(new TriplePath(NodeFactory.createURI("http://D"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://A")));
        assertTrue("Test failed with null ",p_merge!=null);
        assertTrue("Test failed %d %d"
                ,utils.isBgpIsomorphic(p_test,p_merge));

    }
    @Test
    public void mergeTestPriority() throws Exception {
        List<TriplePath> l1 = new ArrayList<TriplePath>();
        List<TriplePath> l2 = new ArrayList<TriplePath>();
        l1.add(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://C")));
        l1.add(new TriplePath(NodeFactory.createURI("http://D"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://A")));
        l2.add(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://D")));
        l2.add(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://A")));

        TripleMerger tm = new TripleMerger(l1,l2);
        ElementPathBlock p_merge = tm.merge();
        ElementPathBlock p_test = new ElementPathBlock();
        p_test.addTriplePath(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createVariable("a")));
        p_test.addTriplePath(new TriplePath(NodeFactory.createVariable("b"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://A")));
        assertTrue("Test failed with null ",p_merge!=null);
        assertTrue("Test failed %d %d"
                ,utils.isBgpIsomorphic(p_test,p_merge));

    }

    @Test
    public void mergeTestCycle() throws Exception {
        List<TriplePath> l1 = new ArrayList<TriplePath>();
        List<TriplePath> l2 = new ArrayList<TriplePath>();
        l1.add(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://C")));
        l1.add(new TriplePath(NodeFactory.createURI("http://D"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://C")));
        l1.add(new TriplePath(NodeFactory.createURI("http://D"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://A")));
        l2.add(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://E")));
        l2.add(new TriplePath(NodeFactory.createURI("http://F"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://D")));
        l2.add(new TriplePath(NodeFactory.createURI("http://F"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://A")));

        TripleMerger tm = new TripleMerger(l1,l2);
        ElementPathBlock p_merge = tm.merge();
        ElementPathBlock p_test = new ElementPathBlock();
        p_test.addTriplePath(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createVariable("a")));
        p_test.addTriplePath(new TriplePath(NodeFactory.createVariable("b"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createVariable("c")));
        p_test.addTriplePath(new TriplePath(NodeFactory.createVariable("b"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://A")));

        assertTrue("Test failed with null ",p_merge!=null);
        assertTrue("Test failed %d %d"
                ,utils.isBgpIsomorphic(p_test,p_merge));


    }

    @Test
    public void merge() throws Exception {
        for(Pair<BasicPattern,Pair<Integer,Integer>> pattern: patternList) {
            TripleMerger tm = new TripleMerger(
                    new ElementPathBlock(provenanceList.get(pattern.getRight().getLeft()-1)).getPattern().getList(),
                    new ElementPathBlock(provenanceList.get(pattern.getRight().getRight()-1)).getPattern().getList());

            ElementPathBlock p_merge = tm.merge();
            assertTrue(String.format("Test failed with null %d %d",pattern.getRight().getLeft(),pattern.getRight().getRight()),
                       p_merge!=null);
            assertTrue(String.format("Test failed %d %d",pattern.getRight().getLeft(),pattern.getRight().getRight())
                    ,utils.isBgpIsomorphic(new ElementPathBlock(pattern.getLeft()),p_merge));
        }
    }
    @Test
    public void mergeTransitive() throws Exception {
        List<TriplePath> l1 = new ArrayList<TriplePath>();
        List<TriplePath> l2 = new ArrayList<TriplePath>();
        l1.add(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://C")));
        l1.add(new TriplePath(NodeFactory.createURI("http://C"),PathFactory.pathLink(NodeFactory.createURI("http://B2")),NodeFactory.createURI("http://D")));
        l2.add(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://E")));
        l2.add(new TriplePath(NodeFactory.createURI("http://E"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://D")));
        l2.add(new TriplePath(NodeFactory.createURI("http://D"),PathFactory.pathLink(NodeFactory.createURI("http://B2")),NodeFactory.createURI("http://P")));

        TripleMerger tm = new TripleMerger(l1,l2);
        ElementPathBlock p_merge = tm.merge();
        ElementPathBlock p_test = new ElementPathBlock();
        p_test.addTriplePath(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createVariable("a")));
        p_test.addTriplePath(new TriplePath(NodeFactory.createVariable("b"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createVariable("c")));
        p_test.addTriplePath(new TriplePath(NodeFactory.createVariable("b"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://A")));

        assertTrue("Test failed with null ",p_merge!=null);
        assertTrue("Test failed %d %d"
                ,utils.isBgpIsomorphic(p_test,p_merge));


    }

}