package tau.cs.db.qbp;

import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;

import java.util.List;

/**
 * Created by efrat on 21/12/16.
 */
public interface Patternable {
    public List<TriplePath> getPattern();
}
