package tau.cs.db.qbp;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_Variable;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.junit.Before;
import org.junit.Test;
import tau.cs.db.utils.Experiment;
import tau.cs.db.utils.RDF;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by efrat on 16/01/17.
 */
public class QbpExplanationTest {
    List<QbpExplanation> explanationList;
    static String explanationDir = "./files/%s/examples/%s/explanations/exp%d/prov.ttl";
    @Before
    public void setUp() throws Exception {
      //  this.explanationList = Experiment.LoadExplanations("q2","http://A");
    }

    @Test
    public void merge() throws Exception {
        QbpExplanation exp1 = new QbpExplanation(NodeFactory.createURI("http://A"),RDF.loadModel(String.format(explanationDir,"qs", "q2",1)));
        QbpExplanation exp2 = new QbpExplanation(NodeFactory.createURI("http://A"),RDF.loadModel(String.format(explanationDir,"qs", "q2",2)));
        QbpBasicPattern patt =exp1.merge(exp2);
        ElementPathBlock p_test = new ElementPathBlock();

        Node example = NodeFactory.createVariable("example");
        p_test.addTriplePath(new TriplePath(example, PathFactory.pathLink(NodeFactory.createURI("http://P")),NodeFactory.createVariable("a")));
        p_test.addTriplePath(new TriplePath(example,PathFactory.pathLink(NodeFactory.createURI("http://P")),NodeFactory.createURI("http://C")));
        p_test.addTriplePath(new TriplePath(example,PathFactory.pathLink(NodeFactory.createURI("http://P")),NodeFactory.createURI("http://D")));

        QbpPattern test = new QbpPattern(p_test.getPattern().getList());
        assertTrue(test.isomorphicTo(patt));
    }

    @Test
    public void merge2() throws Exception {
        QbpExplanation exp1 = new QbpExplanation(NodeFactory.createURI("http://A"),RDF.loadModel(String.format(explanationDir,"qs", "q2",3)));
        QbpExplanation exp2 = new QbpExplanation(NodeFactory.createURI("http://A"),RDF.loadModel(String.format(explanationDir, "qs","q2",4)));
        QbpBasicPattern patt =exp1.merge(exp2);
        ElementPathBlock p_test = new ElementPathBlock();

        Node example = NodeFactory.createVariable("example");
        p_test.addTriplePath(new TriplePath(example, PathFactory.pathLink(NodeFactory.createURI("http://P")),NodeFactory.createVariable("a")));
        p_test.addTriplePath(new TriplePath(example,PathFactory.pathLink(NodeFactory.createURI("http://P")),NodeFactory.createURI("http://B")));
        p_test.addTriplePath(new TriplePath(example,PathFactory.pathLink(NodeFactory.createURI("http://P")),NodeFactory.createURI("http://G")));

        QbpPattern test = new QbpPattern(p_test.getPattern().getList());


        assertTrue(test.isomorphicTo(patt));
    }

}