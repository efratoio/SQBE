package tau.cs.db.qbp;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.junit.Before;
import org.junit.Test;
import tau.cs.db.utils.RDF;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Created by efrat on 09/01/17.
 */
public class QbpSetTest {

    private List<BasicPattern> provenanceList=null;
    private List<Pair<BasicPattern,Pair<Integer,Integer>>> patternList=null;
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
    public void testValidSetSimple() throws Exception {
        BasicPattern bp1 = new BasicPattern();
        bp1.add(new Triple(NodeFactory.createURI("http://A"), NodeFactory.createURI("http://B"),
                NodeFactory.createURI("http://C")));
        BasicPattern bp2 = new BasicPattern();
        bp2.add(new Triple(NodeFactory.createURI("http://A"), NodeFactory.createURI("http://B"),
                NodeFactory.createURI("http://D")));

        QbpExplanation e1 = new QbpExplanation(bp1,bp1.get(0).getSubject());
        QbpExplanation e2 = new QbpExplanation(bp2,bp2.get(0).getSubject());

        QbpBasicPattern patt = e1.merge(e2);

        new QbpSet(patt,e1,e1);

    }


    @Test
    public void testValidSet() throws Exception {
        for(Pair<BasicPattern,Pair<Integer,Integer>> p : patternList){
            QbpExplanation e1 = new QbpExplanation(provenanceList.get(p.getRight().getLeft()-1),
                    provenanceList.get(p.getRight().getLeft()-1).get(0).getSubject() );
            QbpExplanation e2 = new QbpExplanation(provenanceList.get(p.getRight().getRight()-1),
                    provenanceList.get(p.getRight().getRight()-1).get(0).getSubject() );

            QbpBasicPattern patt = e1.merge(e2);
            try {
                new QbpSet(patt, e1, e2);
            }
            catch(Exception e){
                assertTrue(String.format("Failed %d %d",p.getRight().getLeft(),p.getRight().getRight() ),false);
;            }
        }
    }


}