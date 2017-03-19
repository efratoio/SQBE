package tau.cs.db.qbp;

/**
 * Created by efrat on 26/01/17.
 */
public interface QbpBasicPattern extends  Patternable,Mergeable {
    Float getIR();
    QbpBasicPattern clone();
}
