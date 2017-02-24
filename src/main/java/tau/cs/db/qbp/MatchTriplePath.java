package tau.cs.db.qbp;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.TriplePath;


/**
 * Created by efrat on 25/01/17.
 */
public class MatchTriplePath extends Pair<TriplePath,TriplePath> {

    public MatchTriplePath(TriplePath triplePath, TriplePath triplePath2) {
        super(triplePath, triplePath2);

    }

    public Node getLeftSubject(){
        return this.getLeft().getSubject();
    }
    public Node getRightSubject(){
        return this.getRight().getSubject();
    }

    public Node getLeftObject(){
        return this.getLeft().getObject();
    }
    public Node getRightObject(){
        return this.getRight().getObject();
    }
    public boolean testSubjectMatches(){
        return this.getLeft().getSubject().matches(this.getRight().getSubject());
    }

    public boolean testObjectMatches(){
        return this.getLeft().getObject().matches(this.getRight().getObject());
    }
    public boolean subjectMatches(){
        return this.getLeft().getSubject().matches(this.getRight().getSubject());
    }
}
