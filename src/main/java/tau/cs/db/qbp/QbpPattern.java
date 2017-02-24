package tau.cs.db.qbp;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import tau.cs.db.utils.RDF;

import java.util.List;

/**
 * Created by efrat on 10/12/16.
 */
public class QbpPattern implements  QbpBasicPattern{

    Filterable filterPattern;
    Float IR;
    Op op;
    ExprList exprList;
    public QbpPattern(List<TriplePath> tpList, ExprList exprList) {

        Filterable fb = new FilterablePatternDefault(tpList,exprList);
        this.filterPattern = fb;
        int varsNum = 0;
        int pp = 0;

        for(TriplePath triple : this.filterPattern.getPattern()){
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
        this.IR = new Float(varsNum)/((fb.getPattern().size()*2));
    }
    public QbpPattern(Filterable filteredPattern) {

        this.filterPattern = filteredPattern;
        int varsNum = 0;
        int pp = 0;

        for(TriplePath triple : this.filterPattern.getPattern()){
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
        this.IR = new Float(varsNum)/((filteredPattern.getPattern().size()*2));
    }

    @Override
    public String toString() {
        return "QbpPattern{" +
                "filterPattern=" + filterPattern.toString() +
                '}';
    }

    public Float GetIR(){
        return  this.IR;
    }

//    public QbpPattern mergePattern(QbpPattern pat){
//        TripleMerger tm = new TripleMerger(this.filterPattern.getPattern().getList(),pat.filterPattern.getPattern().getList());
//        ElementPathBlock patt = tm.merge();
//        if(patt == null){
//            return  null;
//        }
//        else{
//            return new QbpPattern(patt);
//        }
//    }

    @Override
    public QbpBasicPattern merge(Filterable t) {
        TripleMerger tm = new TripleMerger(this.filterPattern,t);
        Filterable patt = tm.merge();
        if(patt == null){
            return null;
        }
        return new QbpPattern(patt);
    }

    @Override
    public List<TriplePath> getPattern() {
        return this.filterPattern.getPattern();
    }

    @Override
    public ExprList getExpList() {
        return this.filterPattern.getExpList();
    }


    public boolean isomorphicTo(QbpBasicPattern qbpPattern) {
        return utils.isBgpIsomorphic(this.getPattern(),qbpPattern.getPattern());
    }

    @Override
    public Float getIR() {
        return this.IR;
    }
}
