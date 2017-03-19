package tau.cs.db.qbp;

import javax.annotation.processing.Filer;
import java.util.logging.Filter;

/**
 * Created by efrat on 21/12/16.
 */
public interface Mergeable {
    public QbpBasicPattern merge(Patternable t);
}
