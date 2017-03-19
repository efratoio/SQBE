package tau.cs.db.qbp;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.primitives.Floats;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.Var;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

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
    Model model;
    public QbpLearner(List<? extends QbpBasicExplanation> explanations,Model model) throws InvalidPropertiesFormatException {
        this.basicPatterns = new ArrayList<>();
        for(QbpBasicExplanation exp: explanations){
            this.basicPatterns.add(new QbpSet(new QbpPattern(new QbpPattern(exp.getPattern()).getPattern())
                    ,Arrays.asList(exp)));
        }
        this.mergeSuggMap = new HashMap<>();
        this.model = model;
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



    public static Query CreateQuery(List<Pair<QbpSet,Set<Integer>>> lst,Model model){
        Op opQuery =null;
        for(Pair<QbpSet,Set<Integer>> patt : lst){
            if(opQuery == null){
                opQuery = utils.queryBuilder(patt.getLeft(),model);
            }
            else{
                Op op = utils.queryBuilder(patt.getLeft(),model);
                opQuery = OpUnion.create(opQuery,op);
            }
        }

        opQuery = new OpProject(opQuery, Arrays.asList(Var.alloc("example")));
        return OpAsQuery.asQuery(opQuery);

    }

    public static Collection<QbpQuerySuggestion> GetKQueries(List<QbpQuerySuggestion> accuQuerySuggestion, int k,
                                                             QbpLearner learner) throws InvalidPropertiesFormatException {
//        Logger logger = Logger.getLogger("QueryBuilder");

        NavigableSet<QbpQuerySuggestion> newQuerySuggestion  = new TreeSet<>(cmp);
        for(QbpQuerySuggestion existingQuery: accuQuerySuggestion){
//            logger.info("new loop");
//            logger.info(String.format("merged: cost: %f %s",computeCost(existingQuery),
//                    existingQuery.stream()
//                            .map(x->x.getRight().stream().map(y->y.toString()+",").reduce("(",(a,b)->a+b)+")")
//                            .reduce(String::concat)));
            PriorityQueue<Pair<QbpSet,Set<Integer>>> mergeSuggestionQueue = CreateSuggestion(existingQuery,learner);
            for(int i=0; i<k;i++)
            if(mergeSuggestionQueue.size()>0) {
                List<QbpQuerySuggestion> queries4MergeSuggestion = new ArrayList<>();
                Pair<QbpSet, Set<Integer>> mergeSuggestion = mergeSuggestionQueue.poll();
                QbpQuerySuggestion suggestion = new QbpQuerySuggestion();
                suggestion.add(mergeSuggestion);
                queries4MergeSuggestion.add(suggestion);
//                logger.info(String.format("Merge suggestion %s",mergeSuggestion.getRight().stream().map(x->x.toString()+",").reduce("(",(a,b)->a+b)+")"));
                for (Pair<QbpSet, Set<Integer>> queryPart : existingQuery) {
                    if (!mergeSuggestion.getRight().containsAll(queryPart.getRight())) {
                        if(mergeSuggestion.getLeft().checkAddToSet(queryPart.getLeft())){

//                            logger.info(String.format("merge suggestion %s add to set %s",
//                                    mergeSuggestion.getRight().stream().map(y->y.toString()+",").reduce("",(a,b)->a+b),
//                                    queryPart.getRight().stream().map(y->y.toString()+",").reduce("",(a,b)->a+b)));
                            Collection<QbpQuerySuggestion> tempAdd = new ArrayList<>();
                            for(QbpQuerySuggestion sug: queries4MergeSuggestion) {
                                QbpQuerySuggestion temp = new QbpQuerySuggestion(sug);
                                QbpSet tempSet = temp.get(0).getLeft();
                                tempSet.addToSet(queryPart.getLeft());
                                Set<Integer> tempInt = Sets.union(temp.get(0).getRight(), queryPart.getRight());
                                temp.remove(0);
                                temp.add(0,new Pair<QbpSet,Set<Integer>>(tempSet,tempInt));
//                                temp.add(new Pair<QbpSet, Set<Integer>>(tempSet, Sets.union(mergeSuggestion.getRight(), queryPart.getRight())));
                                sug.add(queryPart);
                                tempAdd.add(temp);

                            }
//                            tempAdd.forEach(z-> logger.info(String.format("adding: %s",
//                                            z.stream().map(x->x.getRight().stream().map(y->y.toString()+",")
//                                                    .reduce("(",(a,b)->a+b)+")")
//                                            .reduce(String::concat))));
                            queries4MergeSuggestion.addAll(tempAdd);
                        }else {
                            for(QbpQuerySuggestion sug: queries4MergeSuggestion)
                                sug.add(queryPart);
                        }
                    }
                }
                newQuerySuggestion.addAll(queries4MergeSuggestion);
            }
        }
        if(newQuerySuggestion.size() ==0 || accuQuerySuggestion.containsAll(newQuerySuggestion)){
            return  accuQuerySuggestion.subList(0, Math.min(k,accuQuerySuggestion.size()));
        }
        for(QbpQuerySuggestion qqs: newQuerySuggestion){
//            logger.info(String.format("\t\tnew queries: cost: %f %s",computeCost(qqs),
//                    qqs.stream()
//                            .map(x->x.getRight().stream().map(y->y.toString()+",").reduce("(",(a,b)->a+b)+")")
//                            .reduce(String::concat)));
        }
        newQuerySuggestion.addAll(accuQuerySuggestion);
//        List<QbpQuerySuggestion> res = new ArrayList<>();
//        for(int i=0; i<k;i++){
//            if(accuQuerySuggestion.size()>0) {
//                if(newQuerySuggestion.size()>0) {
//                    if (cmp.compare(accuQuerySuggestion.get(0), newQuerySuggestion.first()) > 0) {
//                        res.add(newQuerySuggestion.pollFirst());
//                    } else {
//                        res.add(accuQuerySuggestion.remove(0));
//                    }
//                }else{
//                    res.addAll(accuQuerySuggestion);
//                }
//            }else{
//                if(newQuerySuggestion.size()>0){
//                    res.add(newQuerySuggestion.pollFirst());
//                }
//            }
//        }
        return GetKQueries(newQuerySuggestion.stream().collect(Collectors.toList()).subList(0,Math.min(k,newQuerySuggestion.size())), k,learner);
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
    public static List<Query> LearnQuery(List<? extends  QbpBasicExplanation> explanations, int k,Model model) throws InvalidPropertiesFormatException {
//        Logger logger = Logger.getLogger("QueryBuilder");
//        logger.info("Examples:");
        QbpLearner learner = new QbpLearner(explanations,model);

        QbpQuerySuggestion qbpSetList = new QbpQuerySuggestion();
            for(int i=0;i< explanations.size();i++){
//                logger.info(String.format("%d explanation %s",i,explanations.get(i).getExample().getLiteral()));
                qbpSetList.add(new Pair(new QbpSet(new QbpPattern(explanations.get(i).getPattern())
                        ,Arrays.asList(explanations.get(i))), CreateSetInt(i)));
            }

        PriorityQueue<Pair<QbpSet,Set<Integer>>> queue = CreateSuggestion(qbpSetList,learner);
        List<QbpQuerySuggestion> lll = new ArrayList<>();
        lll.add(qbpSetList);

        Collection<QbpQuerySuggestion> KSuggestions = GetKQueries(lll,k,learner);
//        logger.info("results:");
        for(QbpQuerySuggestion q: KSuggestions){
//            logger.info(String.format("Result: cost: %f %s",computeCost(q),
//                    q.stream()
//                            .map(x->x.getRight().stream().map(y->y.toString()+",").reduce("(",(a,b)->a+b)+")")
//                            .reduce(String::concat)));
        }
        List<Query> kQueries = new ArrayList<>();
        for(QbpQuerySuggestion qbpS: KSuggestions){
            kQueries.add(CreateQuery(qbpS, learner.model));
        }
        return kQueries;
    }
}
