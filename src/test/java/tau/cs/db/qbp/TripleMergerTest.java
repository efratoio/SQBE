package tau.cs.db.qbp;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.expr.E_NotEquals;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;
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
    private  List<BasicPattern> provenanceList=null;
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

    @After
    public void tearDown() throws Exception {
    }
    @Test
    public void mergeBasic() throws Exception {
        List<TriplePath> l1 = new ArrayList<TriplePath>();
        List<TriplePath> l2 = new ArrayList<TriplePath>();
        l1.add(new TriplePath(NodeFactory.createURI("http://A"), PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://C")));
        l2.add(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createURI("http://D")));

        ExprList exp1 = new ExprList(new E_NotEquals(new NodeValueNode(NodeFactory.createURI("http://A")),
                new NodeValueNode(NodeFactory.createURI("http://C"))));

        ExprList exp2 = new ExprList(new E_NotEquals(new NodeValueNode(NodeFactory.createURI("http://A")),
                new NodeValueNode(NodeFactory.createURI("http://D"))));
        TripleMerger tm = new TripleMerger(new FilterablePatternDefault(l1,exp1),
                new FilterablePatternDefault(l2,exp2));
        Filterable p_merge = tm.merge();
        ElementPathBlock p_test = new ElementPathBlock();
        p_test.addTriplePath(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B")),NodeFactory.createVariable("a")));
        assertTrue("Test failed with null ",p_merge!=null);
        assertTrue("Test failed"
                ,utils.isBgpIsomorphic(p_test.getPattern().getList(),p_merge.getPattern()));

    }
    /***
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
    public void mergeBasic3() throws Exception {
        List<TriplePath> l1 = new ArrayList<TriplePath>();
        List<TriplePath> l2 = new ArrayList<TriplePath>();
        l1.add(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B1")),NodeFactory.createURI("http://C")));
        l1.add(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B1")),NodeFactory.createURI("http://D")));
        l1.add(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B2")),NodeFactory.createURI("http://E")));
        l2.add(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B1")),NodeFactory.createURI("http://F")));
        l2.add(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B2")),NodeFactory.createURI("http://H")));
        l2.add(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B2")),NodeFactory.createURI("http://J")));

        TripleMerger tm = new TripleMerger(l1,l2);
        ElementPathBlock p_merge = tm.merge();
        ElementPathBlock p_test = new ElementPathBlock();
        p_test.addTriplePath(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B1")),NodeFactory.createVariable("a")));
        p_test.addTriplePath(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B1")),NodeFactory.createVariable("b")));
        p_test.addTriplePath(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B2")),NodeFactory.createVariable("c")));
        p_test.addTriplePath(new TriplePath(NodeFactory.createURI("http://A"),PathFactory.pathLink(NodeFactory.createURI("http://B2")),NodeFactory.createVariable("d")));
        assertTrue("Test failed with null ",p_merge!=null);
        assertTrue("Test failed"
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
        p_test.addTriplePath(new TriplePath(NodeFactory.createURI("http://A"),
                PathFactory.pathOneOrMore1(PathFactory.pathLink(NodeFactory.createURI("http://B"))),NodeFactory.createVariable("c")));
        p_test.addTriplePath(new TriplePath(NodeFactory.createVariable("c"),PathFactory.pathLink(NodeFactory.createURI("http://B2")),NodeFactory.createVariable("d")));


        assertTrue("Test failed with null ",p_merge!=null);
        assertTrue("Test failed %d %d"
                ,utils.isBgpIsomorphic(p_test,p_merge));


    }
    ***/

}