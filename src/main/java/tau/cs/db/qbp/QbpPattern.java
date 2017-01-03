package tau.cs.db.qbp;

import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;

/**
 * Created by efrat on 10/12/16.
 */
public class QbpPattern implements  Mergeable,Patternable{

    ElementPathBlock pattern;
    Float IR;

    public QbpPattern(ElementPathBlock pattern) {
        this.pattern = pattern;
        int varsNum = 0;
        int pp = 0;
        for(TriplePath triple : this.pattern.getPattern()){
            if(triple.getSubject().isVariable()){
                varsNum++;
            }
            if(!triple.isTriple()){
                pp++;
            }
            if(triple.getObject().isVariable()){
                varsNum++;
            }
        }

        this.IR = new Float(varsNum)/((pattern.getPattern().getList().size()*2)+pp*3);
    }

    @Override
    public String toString() {
        return this.pattern.toString();
    }

    public Float GetIR(){
        return  this.IR;
    }

    public QbpPattern mergePattern(QbpPattern pat){
        TripleMerger tm = new TripleMerger(this.pattern.getPattern().getList(),pat.pattern.getPattern().getList());
        ElementPathBlock patt = tm.merge();
        if(patt == null){
            return  null;
        }
        else{
            return new QbpPattern(patt);
        }
    }

    @Override
    public QbpPattern merge(Patternable t) {
        TripleMerger tm = new TripleMerger(this.pattern.getPattern().getList(),t.getPattern().getPattern().getList());
        ElementPathBlock patt = tm.merge();
        return new QbpPattern(patt);
    }

    @Override
    public ElementPathBlock getPattern() {
        return this.pattern;
    }
}
