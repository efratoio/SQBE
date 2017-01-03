package tau.cs.db.qbp;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.PathBlock;
import org.apache.jena.sparql.core.TriplePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
/**
 * Created by efrat on 12/12/16.
 */
public class QbpLearner {

    static float W1=5;
    static float W2=20;


    public static TreeMap<QbpPattern,Set<QbpPattern>> ComputeMatchingPatterns(List<QbpPattern> patterns){
        if(patterns.size()<2){
            return new TreeMap<QbpPattern,Set<QbpPattern>>();
        }
        TreeMap<QbpPattern,Set<QbpPattern>> matching = new TreeMap<>(new Comparator<QbpPattern>() {
            @Override
            public int compare(QbpPattern qbpPattern, QbpPattern t1) {
                if(utils.isBgpIsomorphic(qbpPattern.pattern,t1.pattern)){
                    return 0;
                }
                return -1;
            }

        });
        if(patterns.size()==2){
            QbpPattern m_p = patterns.get(0).mergePattern(patterns.get(1));
            if(m_p!=null) {
                Set<QbpPattern> hs = new HashSet<QbpPattern>();
                hs.addAll(patterns);
                matching.put(m_p, hs);
            }
            return matching;
        }
        Iterator<int[]> itr = CombinatoricsUtils.combinationsIterator(patterns.size()-1,2);

        while(itr.hasNext()){
            int[] indices = itr.next();
                QbpPattern pattern = patterns.get(indices[0]).mergePattern(patterns.get(indices[1]));
                if(pattern!=null) {
                    if (!matching.containsKey(pattern)) {
                        Set<QbpPattern> hs = new HashSet<QbpPattern>();
                        matching.put(pattern, hs);
                    }

                    matching.get(pattern).add(patterns.get(indices[0]));
                    matching.get(pattern).add(patterns.get(indices[1]));
                }
        }
        for(QbpPattern patt : matching.keySet()){
            for(QbpPattern exp: patterns){
                if(utils.isBgpIsomorphic(exp.merge(patt).getPattern(),patt.getPattern())){
                    matching.get(patt).add(exp);
                }
            }
        }
        return matching;
    }

    public static float computeCost(int explanations,List<QbpPattern> pats){
        float sum_IR=0;
        for(QbpPattern p: pats){
            sum_IR+=p.GetIR();
        }
        return W2*explanations+W2*pats.size()+W1*sum_IR;
    }
    public static Query unifyBest(List<QbpExplanation> explanations){
        List<QbpPattern> bgp2Union = new ArrayList<QbpPattern>();
        float currCost = explanations.size()*W2;
        float newCost = currCost;
        do{
            currCost=newCost;

            TreeMap<QbpPattern,Set<QbpExplanation>> matching = ComputeMatching(explanations);
//            TreeMap<QbpPattern,Set<QbpPattern>> patMatching = ComputeMatchingPatterns(bgp2Union);
            Set<QbpExplanation> exp = new HashSet<QbpExplanation>(explanations);
            QbpPattern pat=null;
            for(Map.Entry<QbpPattern,Set<QbpExplanation>> entry: matching.entrySet()){
                Set<QbpExplanation> rexp = new HashSet<QbpExplanation>(entry.getValue());
                rexp.retainAll(exp);

                List<QbpPattern> pats = new ArrayList<QbpPattern>(bgp2Union);
                pats.add(entry.getKey());
                float cost = computeCost(exp.size()-rexp.size(),pats);
                if(cost<newCost){
                    newCost=cost;
                    pat = entry.getKey();
                }

            }

//            for(Map.Entry<QbpPattern,Set<QbpPattern>> entry : patMatching.entrySet()){
//                float cost=currCost;
//                for (QbpPattern patt : entry.getValue()){
//                    cost-=patt.GetIR()*W1 -W2;
//                }
//                cost+=W2+entry.getKey().GetIR();
//                if(cost<newCost){
//                    newCost=cost;
//                    pat = entry.getKey();
//                }
//            }
            if(currCost>newCost && pat!=null){
                bgp2Union.add(pat);
                if(matching.containsKey(pat)) {
                    Set<QbpExplanation> h = new HashSet<QbpExplanation>(explanations);
                    h.retainAll(matching.get(pat));
                    explanations.removeAll(h);
                    matching.remove(pat);
                }
            }
        }while(currCost>newCost);
        Op opQuery =null;
        for(QbpPattern patt : bgp2Union){
            if(opQuery == null){
                opQuery = utils.pathToTriples(patt.pattern.getPattern());
            }
            else{
                Op op = utils.pathToTriples(patt.pattern.getPattern());
                opQuery = OpUnion.create(opQuery,op);
            }
        }

        return OpAsQuery.asQuery(opQuery);

    }



    public static TreeMap<QbpPattern,Set<QbpExplanation>> ComputeMatching(List<QbpExplanation> explanations){
        Logger logger = LoggerFactory.getLogger(QbpLearner.class);
//        Set<Node> nodes = new HashSet<>();
//        explanations.stream().map(qbpExplanation -> nodes.add(qbpExplanation.getExample()));
        TreeMap<QbpPattern,Set<QbpExplanation>> matching = new TreeMap<>(new Comparator<QbpPattern>() {
            @Override
            public int compare(QbpPattern qbpPattern, QbpPattern t1) {
                if(utils.isBgpIsomorphic(qbpPattern.pattern,t1.pattern)){
                    return 0;
                }
                return -1;
            }

        });
        Iterator<int[]> itr = CombinatoricsUtils.combinationsIterator(explanations.size()-1,2);

        while(itr.hasNext()){
            int[] indices = itr.next();
            if(0!=explanations.get(indices[0]).getExample().toString().compareTo(explanations.get(indices[1]).getExample().toString())) {
                QbpPattern pattern = explanations.get(indices[0]).MergeExplanations(explanations.get(indices[1]));
                if(pattern!=null) {
                    if (!matching.containsKey(pattern)) {
                        Set<QbpExplanation> hs = new HashSet<QbpExplanation>();
                        matching.put(pattern, hs);
                    }

                    matching.get(pattern).add(explanations.get(indices[0]));
                    matching.get(pattern).add(explanations.get(indices[1]));

                }
            }
        }
//        for(QbpPattern patt : matching.keySet()){
//            for(QbpExplanation exp: explanations){
//                QbpPattern mer_p = exp.merge(patt);
//
//                if(mer_p!=null && utils.isBgpIsomorphic(mer_p.getPattern(),patt.getPattern())){
//                    matching.get(patt).add(exp);
//                }
//            }
//        }
        return matching;
    }

    public static void LearnQuery(List<QbpExplanation> explanations){


    }


}
