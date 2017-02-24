package tau.cs.db.qbp;

import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.syntax.ElementPathBlock;

import java.util.List;

/**
 * Created by efrat on 25/01/17.
 */
public class FilterablePatternDefault implements Filterable, Patternable {

    List<TriplePath> pattern;
    ExprList exprList;

    @Override
    public String toString() {
        return "FilterablePatternDefault{" +
                "pattern=" + pattern.toString() +
                '}';
    }

    public FilterablePatternDefault(List<TriplePath> pattern, ExprList exprList) {
        this.pattern = pattern;
        this.exprList = exprList;
    }

    @Override
    public List<TriplePath> getPattern() {
        return this.pattern;
    }

    @Override
    public ExprList getExpList() {
        return this.exprList;
    }
}
