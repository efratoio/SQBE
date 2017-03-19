package tau.cs.db.qbp;

import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.syntax.ElementPathBlock;

import java.util.List;

/**
 * Created by efrat on 25/01/17.
 */
public class PatternableDefault implements  Patternable {

    List<TriplePath> pattern;
//    ExprList exprList;

    @Override
    public String toString() {
        return "FilterablePatternDefault{" +
                "pattern=" + pattern.toString() +
                '}';
    }

    public PatternableDefault(List<TriplePath> pattern) {
        this.pattern = pattern;
//        this.exprList = exprList;
    }

    @Override
    public List<TriplePath> getPattern() {
        return this.pattern;
    }


}
