package tau.cs.db.qbp;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Variable;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;

/**
 * Created by efrat on 10/12/16.
 */
public class QbpExplanation {
    BasicPattern explanation;
    Node example;

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
                    this.explanation.add(triple);
                }
            }
        }

    }


    public QbpPattern MergeExplanations(QbpExplanation other) {
        return new QbpPattern(utils.FindBestMerge(this.explanation, other.explanation));
    }
}
