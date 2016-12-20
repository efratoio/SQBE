package tau.cs.db.qbp;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tau.cs.db.App;

import java.util.*;
/**
 * Created by efrat on 12/12/16.
 */
public class QbpLearner {


    public static TreeMap<QbpPattern,Set<QbpExplanation>> ComputeMatching(List<QbpExplanation> explanations){
        Logger logger = LoggerFactory.getLogger(App.class);
//        Set<Node> nodes = new HashSet<>();
//        explanations.stream().map(qbpExplanation -> nodes.add(qbpExplanation.getExample()));

        TreeMap<QbpPattern,Set<QbpExplanation>> matching = new TreeMap<>(new Comparator<QbpPattern>() {
            @Override
            public int compare(QbpPattern qbpPattern, QbpPattern t1) {
                if(qbpPattern.toString().compareTo(t1.toString())==0) {
                    return qbpPattern.GetIR().compareTo(t1.GetIR());
                }
                else{
                    return qbpPattern.toString().compareTo(t1.toString());
                }
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
        return matching;
    }

    public static void LearnQuery(List<QbpExplanation> explanations){


    }


}
