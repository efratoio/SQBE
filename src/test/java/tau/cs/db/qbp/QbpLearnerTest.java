package tau.cs.db.qbp;

import org.apache.jena.query.Query;
import org.junit.Before;
import org.junit.Test;
import tau.cs.db.utils.Experiment;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by efrat on 16/01/17.
 */
public class QbpLearnerTest {

    List<QbpExplanation> explanationListQ1;
    @Before
    public void setUp() throws Exception {

        explanationListQ1 = Experiment.LoadExplanations("sp2b","q1","http://A");
    }

//    @Test
//    public void learnQuery() throws Exception {
//
//        List<Query> queryList = QbpLearner.LearnQuery(explanationListQ1,4);
//    }
//
//    @Test
//    public void learnQuery2() throws Exception {
//        List<QbpExplanation> explanationList = Experiment.LoadExplanations("sp2b","q2","http://A");
//        List<Query> queryList = QbpLearner.LearnQuery(explanationList,4);
//    }
}