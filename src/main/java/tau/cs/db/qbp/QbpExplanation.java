package tau.cs.db.qbp;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Variable;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.BasicPattern;

import java.util.Iterator;

/**
 * Created by efrat on 10/12/16.
 */
public class QbpExplanation {
    BasicPattern explanation;
    Node example;

    public Node getExample() {
        return example;
    }

    public QbpExplanation(Node example, Model model) {

        this.example = example;
        this.explanation = new BasicPattern();

        Iterator<Statement> stItr = model.listStatements();

        while(stItr.hasNext()){
            Statement stmnt = stItr.next();
            Triple triple = stmnt.asTriple();
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

    public QbpExplanation(BasicPattern explanation, Node example) {

        this.example = example;
        this.explanation = new BasicPattern();
        for(Triple triple: explanation){
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
    public String toString() {
        return this.explanation.toString();
    }

    public QbpPattern MergeExplanations(QbpExplanation other) {
        return new QbpPattern(utils.FindBestMerge(this.explanation, other.explanation));
    }
}
