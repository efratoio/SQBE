package tau.cs.db.qbp;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by efrat on 04/01/17.
 */
public class QBPHandlerTest {
    QBPHandler handler;

    @Before
    public void setUp() throws Exception {
        handler = new QBPHandler("./files/%s/ontology/sp2b.n3");
    }
//
//    @Test
//    public void findNodes() throws Exception {
//        for(String s : handler.FindNodes("Er")){
//            assert true;
//        }
//    }



}