package tau.cs.db.qbp;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.primitives.Floats;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.Var;

import java.util.*;

/**
 * Created by efrat on 12/12/16.
 */
public class QbpLearner {

    public static float W1=100;
    public static float W2=10;
    public static  Comparator<QbpQuerySuggestion> cmp = new Comparator<QbpQuerySuggestion>() {
        @Override
        public int compare(QbpQuerySuggestion qbpSets, QbpQuerySuggestion t1) {
            return Floats.compare(computeCost(qbpSets),computeCost(t1));
        }
    };
    Map<Set<Integer>,QbpSet> mergeSuggMap;
    List<QbpSet> basicPatterns;
    public QbpLearner(List<? extends QbpBasicExplanation> explanations) throws InvalidPropertiesFormatException {
        this.basicPatterns = new ArrayList<>();
        for(QbpBasicExplanation exp: explanations){
            this.basicPatterns.add(new QbpSet(new QbpPattern(new QbpPattern(exp))
                    ,Arrays.asList(exp)));
        }
        this.mergeSuggMap = new HashMap<>();
        this.InitSuggestions();
    }

    public void InitSuggestions() throws InvalidPropertiesFormatException {
        for(int i=0; i<this.basicPatterns.size();i++) {
            for (int j = i + 1; j < this.basicPatterns.size(); j++) {
                QbpSet res = this.basicPatterns.get(i).mergeSets(this.basicPatterns.get(j));
                if (res != null) {
                    this.mergeSuggMap.put(CreateSetInt(i,j), res);
                }
            }

        }
    }

    public static float computeCost(List<Pair<QbpSet,Set<Integer>>> pats){
        float sum_IR=0;
        for(Pair<QbpSet,Set<Integer>> p: pats){
            sum_IR+=p.getLeft().GetIR();
        }
        return W2*pats.size()+W1*sum_IR;
    }



    public static Query CreateQuery(List<Pair<QbpSet,Set<Integer>>> lst){
        Op opQuery =null;
        for(Pair<QbpSet,Set<Integer>> patt : lst){
            if(opQuery == null){
                opQuery = utils.queryBuilder(patt.getLeft());
            }
            else{
                Op op = utils.queryBuilder(patt.getLeft());
                opQuery = OpUnion.create(opQuery,op);
            }
        }

        opQuery = new OpProject(opQuery, Arrays.asList(Var.alloc("example")));
        return OpAsQuery.asQuery(opQuery);

    }

    public static Collection<QbpQuerySuggestion> GetKQueries(NavigableSet<QbpQuerySuggestion> qbpSetSuggestion,int k,
                                                 QbpLearner learner) throws InvalidPropertiesFormatException {


        NavigableSet<QbpQuerySuggestion> newSuggestions  = new TreeSet<>(cmp);
        for(QbpQuerySuggestion qbpSetList: qbpSetSuggestion){
            PriorityQueue<Pair<QbpSet,Set<Integer>>> queue = CreateSuggestion(qbpSetList,learner);
            for(int i=0;i<k;i++ ){
                if(queue.size()>0) {
                    QbpQuerySuggestion sug = new QbpQuerySuggestion();

                    Pair<QbpSet, Set<Integer>> mergeSugg = queue.poll();
                    for (Pair<QbpSet, Set<Integer>> pair : qbpSetList) {


                        if (!mergeSugg.getRight().containsAll(pair.getRight())) {
                            if(mergeSugg.getLeft().checkAddToSet(pair.getLeft())){
                                mergeSugg.getLeft().addToSet(pair.getLeft());
                                List<Integer> newList = new LinkedList<Integer>();
                                mergeSugg = new Pair<>(mergeSugg.getLeft(), Sets.union(mergeSugg.getRight(),
                                        pair.getRight()));


                            }
                            else {
                                sug.add(pair);
                            }

                        }
                    }

                    sug.add(mergeSugg);
                    newSuggestions.add(sug);

                }
            }
        }
//        qbpSetSuggestion.sort(cmp);
//        newSuggestions.sort(cmp);
        if(newSuggestions.size() ==0 || qbpSetSuggestion.containsAll(newSuggestions) || computeCost(newSuggestions.first())>=
                computeCost(Iterables.get(qbpSetSuggestion,Integer.min(k-1,qbpSetSuggestion.size()-1)))){
             return  qbpSetSuggestion.headSet(Iterables.get(qbpSetSuggestion,Integer.min(k,qbpSetSuggestion.size()-1)));
        }
        qbpSetSuggestion.addAll(newSuggestions);

        return GetKQueries(qbpSetSuggestion.headSet(Iterables.get(qbpSetSuggestion,Integer.min(k,qbpSetSuggestion.size()-1)),true),k,learner);



    }
    public static PriorityQueue<Pair<QbpSet,Set<Integer>>> CreateSuggestion(QbpQuerySuggestion qbpSet,
            QbpLearner learner)    throws InvalidPropertiesFormatException {

        PriorityQueue<Pair<QbpSet,Set<Integer>>> queue = new PriorityQueue<>(new Comparator<Pair<QbpSet, Set<Integer>>>() {
            @Override
            public int compare(Pair<QbpSet,Set<Integer>> qbpSetPairPair, Pair<QbpSet, Set<Integer>> t1) {
                return qbpSetPairPair.getLeft().compareTo(t1.getLeft()) ;
            }
        });

        for(int i=0; i<qbpSet.size();i++){
            for(int j=i+1; j<qbpSet.size();j++){
                Set<Integer> indices = Sets.union(qbpSet.get(i).getRight(),qbpSet.get(j).getRight());
                QbpSet res = learner.mergeSuggMap.get(indices);
                if(res==null){
                    try {
                        res = qbpSet.get(i).getLeft().mergeSets(qbpSet.get(j).getLeft());
                        if(res!=null)
                            learner.mergeSuggMap.put(Sets.union(qbpSet.get(i).getRight(),
                                qbpSet.get(j).getRight() ),res);
                    }
                    catch(Exception e){
                        res=null;
                    }
                }

                if(res!=null){
                queue.add(new Pair<QbpSet,Set<Integer>>(res, Sets.union(qbpSet.get(i).getRight(),
                        qbpSet.get(j).getRight())));
                }
            }
        }
        return queue;
    }
    public static Set<Integer> CreateSetInt(int i){
            Set<Integer> re = new HashSet<>();
            re.add(i);
            return re;
    }
    public static Set<Integer> CreateSetInt(int i,int j){
        Set<Integer> re = CreateSetInt(i);
        re.add(j);
        return re;
    }
    public static List<Query> LearnQuery(List<? extends  QbpBasicExplanation> explanations, int k) throws InvalidPropertiesFormatException {
        QbpLearner learner = new QbpLearner(explanations);

        QbpQuerySuggestion qbpSetList = new QbpQuerySuggestion();
            for(int i=0;i< explanations.size();i++){
                qbpSetList.add(new Pair(new QbpSet(new QbpPattern(explanations.get(i))
                        ,Arrays.asList(explanations.get(i))), CreateSetInt(i)));
            }

        PriorityQueue<Pair<QbpSet,Set<Integer>>> queue = CreateSuggestion(qbpSetList,learner);
        NavigableSet<QbpQuerySuggestion> lll = new TreeSet<>(cmp);
        lll.add(qbpSetList);

        Collection<QbpQuerySuggestion> KSuggestions = GetKQueries(lll,k,learner);

        List<Query> kQueries = new ArrayList<>();
        for(QbpQuerySuggestion qbpS: KSuggestions){
            kQueries.add(CreateQuery(qbpS));
        }
        return kQueries;


    }




}
