package tau.cs.db.qbp;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Variable;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.expr.E_NotEquals;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import tau.cs.db.utils.RDF;

import java.util.*;

/**
 * Created by efrat on 10/12/16.
 */
public class QbpExplanation implements QbpBasicExplanation{
    BasicPattern explanation;
    Node example;
    Model model;

    @Override
    public String toString() {
        return "QbpExplanation{" +
                "explanation=" + explanation.toString() +
                '}';
    }

    @Override
    public Model getModel() {
        return this.model;
    }

    public Node getExample() {
        return example;
    }

    public QbpExplanation(Node example, Model model) {

        this.example = example;
        this.explanation = new BasicPattern();
        this.model = model;
        Iterator<Statement> stItr = model.listStatements();

        while(stItr.hasNext()) {
            Statement stmnt = stItr.next();
            Triple triple = stmnt.asTriple();
            Node sub=null;
            Node obj=null;

            if (triple.getSubject().isLiteral() &&
                    (triple.getSubject().getLiteral().toString().compareTo(example.toString()) == 0)) {
                    sub = new Node_Variable("example");

            } else if (triple.subjectMatches(example)) {
                sub = new Node_Variable("example");
            }
            if(sub==null){
                sub = triple.getSubject();
            }

            if (triple.getObject().isLiteral() &&
                    (triple.getObject().getLiteral().toString().compareTo(example.toString()) == 0)) {
                obj = new Node_Variable("example");

            } else if (triple.objectMatches(example)) {
                obj = new Node_Variable("example");
            }
            if(obj==null){
                obj = triple.getObject();
            }


            this.explanation.add(new Triple(sub, triple.getPredicate(),obj));

        }


    }

    public QbpExplanation(BasicPattern explanation, Node example) {

        this.example = example;
        this.explanation = new BasicPattern();
        this.model = ModelFactory.createDefaultModel();
        for(Triple triple: explanation){
            Statement stmt = RDF.triple2Statement(triple);
            this.model.add(stmt);

            if(triple.subjectMatches(example)){
                this.explanation.add(new Triple(new Node_Variable("example"),triple.getPredicate(),triple.getObject()));
            }
            else{
                if(triple.subjectMatches(example)){
                    this.explanation.add(new Triple(new Node_Variable("example"),triple.getPredicate(),
                            triple.getObject()));
                }else{
                    if(triple.objectMatches(example)){
                        this.explanation.add(new Triple(triple.getSubject(),triple.getPredicate(),
                                new Node_Variable("example")));
                    }else {
                        this.explanation.add(triple);
                    }
                }
            }
        }


    }


    @Override
    public List<TriplePath> getPattern() {
        return new ElementPathBlock(this.explanation).getPattern().getList();
    }

    @Override
    public QbpBasicPattern merge(Patternable t) {
        TripleMerger tm = new TripleMerger(this,t);
        Patternable patt = tm.merge();
        if(patt == null){
            return  null;
        }
        else{
            return new QbpPattern(patt.getPattern());
        }
    }
}
