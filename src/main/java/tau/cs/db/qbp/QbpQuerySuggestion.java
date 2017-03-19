package tau.cs.db.qbp;

import com.google.common.primitives.Floats;
import org.apache.jena.atlas.lib.Pair;

import java.util.*;

/**
 * Created by efrat on 13/02/17.
 */
public class QbpQuerySuggestion extends ArrayList<Pair<QbpSet,Set<Integer>>> {
    public QbpQuerySuggestion(Collection<? extends Pair<QbpSet, Set<Integer>>> collection) {
        super(collection);
    }

    public QbpQuerySuggestion() {
        super();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof QbpQuerySuggestion) {


            if (this.size() != ((QbpQuerySuggestion) o).size())
                return false;

            for (Pair<QbpSet, Set<Integer>> x : (QbpQuerySuggestion) o) {
                if(!this.stream().map(y -> y.getRight()
                        .containsAll(x.getRight())&&x.getRight().containsAll(y.getRight()))
                        .reduce(false, (a, b) -> a || b)) {
                    return false;
                }
            }

            return true;

        }
//
//            Comparator<Pair<QbpSet, Set<Integer>>> cmp = new Comparator<Pair<QbpSet, Set<Integer>>>() {
//                @Override
//                public int compare(Pair<QbpSet, Set<Integer>> qbpSets, Pair<QbpSet, Set<Integer>> t1) {
//                    return Integer.compare(Collections.min(qbpSets.getRight()),Collections.min(t1.getRight()));
//                }
//            };
//            this.sort(cmp);
//            ((QbpQuerySuggestion) o).sort(cmp);
//
//            for(int i=0; i<this.size(); i++){
//                if(!this.get(i).getRight().equals(((QbpQuerySuggestion) o).get(i).getRight())){
//                    return false;
//                }
//
//            }
//
//            return true;


        return false;
    }
}
