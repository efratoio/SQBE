package tau.cs.db.qbp;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.syntax.ElementPathBlock;

/**
 * Created by efrat on 25/01/17.
 */
public interface Filterable extends Patternable {
    ExprList getExpList(Model model);
}
