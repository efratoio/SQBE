package tau.cs.db.qbp;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;

/**
 * Created by efrat on 10/12/16.
 */
public class QbpPattern {

    BasicPattern pattern;
    Float IR;
    public QbpPattern(BasicPattern pattern) {
        this.pattern = pattern;
        int varsNum = 0;
        for(Triple triple : this.pattern){
            if(triple.getSubject().isVariable()){
                varsNum++;
            }
            if(triple.getObject().isVariable()){
                varsNum++;
            }
        }

        this.IR = new Float(varsNum)/pattern.size()*2;
    }



    public Float GetIR(){
        return  this.IR;
    }
}
