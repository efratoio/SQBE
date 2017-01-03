package tau.cs.db.qbp;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by efrat on 26/12/16.
 */
public class TripleMerger {

    List<TriplePath> l1;
    List<TriplePath> l2;


    class MergeSuggestion implements Comparable<MergeSuggestion>{
        Integer x;
        Integer y;

//        public boolean isMinimal() {
//            return minimal;
//        }

//        public void setMinimal() {
//            this.minimal = true;
//        }

//        boolean minimal = false;
        public MergeSuggestion(Integer x, Integer y) {
            this.x = x;
            this.y = y;
        }
        public float getCost(){
//            if(this.isMinimal()){
//                return 0;
//            }
            float cost = 1;
            TriplePath t1 = l1.get(this.x);
            TriplePath t2= l2.get(this.y);
            boolean flag=false;
            if(!mark1[this.x]){
                cost/=1500;
            }
            if(!mark2[this.y]){
                cost/=1500;
            }
            if(!mark1[this.x] && !mark2[this.y]){
                cost/=2000;
            }
            if(t1.getSubject().matches(t2.getSubject())){
                flag=true;
                cost/=3;
            }
            else{
                float a = varsMerged.getOrDefault(new Pair<Node,Node>(t1.getSubject(),t2.getSubject()),1);
                cost /= a;
                if(a>1)
                    flag=true;
            }
            if(t1.getPredicate().matches(t2.getPredicate())){
                cost/=50;
                flag=true;
            }else{
                float a = varsMerged.getOrDefault(new Pair<Node,Node>(t1.getPredicate(),t2.getPredicate()),1);
                cost /= a;
                flag=true;
            }
            if(t1.getObject().matches(t2.getObject())){
                cost/=3;
                flag=true;
            }else{
                float a = varsMerged.getOrDefault(new Pair<Node,Node>(t1.getObject(),t2.getObject()),1);
                cost /= a;
                if(a>1)
                    flag=true;
            }
            if(flag)
                return cost;
            return 1;
        }



        @Override
        public int compareTo(MergeSuggestion mergeSuggestion) {

            return Float.compare(this.getCost(),mergeSuggestion.getCost());
        }
    }

    Map<Pair<Node, Node>, Integer> varsMerged;
    Boolean[] mark1;
    Boolean[] mark2;
    ArrayList<MergeSuggestion> matching;
    PriorityQueue<MergeSuggestion> minimalQueue;

    public TripleMerger(List<TriplePath> l1, List<TriplePath> l2) {
        this.l1 = l1;
        this.l2 = l2;
        this.mark1 = new Boolean[l1.size()];
        this.mark2 = new Boolean[l2.size()];
        this.matching = new ArrayList<MergeSuggestion>();
        this.varsMerged = new HashMap<Pair<Node, Node>, Integer>();

    }

    public void fillSuggestion(){
        this.minimalQueue = new PriorityQueue<MergeSuggestion>();
        Arrays.fill(this.mark1, false);
        Arrays.fill(this.mark2, false);
        this.matching.clear();
        this.varsMerged.clear();
        for (int i = 0; i < l1.size(); i++) {
            for (int j = 0; j < l2.size(); j++) {
                this.minimalQueue.add(new MergeSuggestion(i, j));
            }
        }
    }
    public List<Pair<TriplePath, TriplePath>> mergeHandler() {
//        MergeSuggestion minimalSug = new MergeSuggestion(-1,-1);
//        minimalSug.setMinimal();
        while (minimalQueue.size()>0 && (!utils.testBoolArr(this.mark1) || !utils.testBoolArr(this.mark2))) {
            reorderQueue();
            logQueue();
            MergeSuggestion sug = this.minimalQueue.poll();
            TriplePath t1 = TripleMerger.this.l1.get(sug.x);
            TriplePath t2 = TripleMerger.this.l2.get(sug.y);
            this.mark1[sug.x]=true;
            this.mark2[sug.y]=true;
            if (!t1.getSubject().matches(t2.getSubject())) {
                int a = this.varsMerged.getOrDefault(new Pair<Node, Node>(t1.getSubject(), t2.getSubject()), 1);
                this.varsMerged.put(new Pair<Node, Node>(t1.getSubject(), t2.getSubject()), a + 1);
            }
            if (!t1.getPredicate().matches(t2.getPredicate())) {
                int a = this.varsMerged.getOrDefault(new Pair<Node, Node>(t1.getPredicate(), t2.getPredicate()), 1);
                this.varsMerged.put(new Pair<Node, Node>(t1.getPredicate(), t2.getPredicate()), a + 1);
            }
            if (!t1.getObject().matches(t2.getObject())) {
                int a = this.varsMerged.getOrDefault(new Pair<Node, Node>(t1.getObject(), t2.getObject()), 1);
                this.varsMerged.put(new Pair<Node, Node>(t1.getObject(), t2.getObject()), a + 1);
            }
            this.matching.add(sug);
//            this.minimalQueue.add(minimalSug);
//            this.minimalQueue.remove(minimalSug);

        }

        List<Pair<TriplePath, TriplePath>> match = new ArrayList<Pair<TriplePath,TriplePath>>();
        for (MergeSuggestion suggestion : this.matching){
            match.add(new Pair<TriplePath,TriplePath>(l1.get(suggestion.x),l2.get(suggestion.y)));
        }
        return match;

    }

    public void reorderQueue(){
        List<MergeSuggestion> msList = new ArrayList<MergeSuggestion>();
       for(MergeSuggestion ms:  this.minimalQueue){
           msList.add(ms);

       }
       this.minimalQueue.clear();
        this.minimalQueue.addAll(msList);
    }

    public void logQueue(){
        Logger logger = LoggerFactory.getLogger(TripleMerger.class);
        logger.info("Logging minimal queue");
        logger.info(String.format("First: %d %d",this.minimalQueue.peek().x,this.minimalQueue.peek().y));
        for (MergeSuggestion ms : this.minimalQueue) {
            logger.info(String.format("suggestion %d %d cost %f",ms.x,ms.y,ms.getCost()));
        }
    }

    public ElementPathBlock merge(){
        fillSuggestion();
        int l =minimalQueue.size();
        ElementPathBlock minMatching = null;
        float minVars = 1000;
        for (int i=0; i<l;i++){
            fillSuggestion();
            for(int j=0;j<i;j++){
                minimalQueue.poll();
            }

            List<Pair<TriplePath, TriplePath>> matching = mergeHandler();
            ElementPathBlock p = utils.createMergedPattern(matching);
            if(p!=null && utils.testBasicPatten(p)){
                if(utils.varsCount(p)<minVars && matching.size()<=l1.size()+l2.size() && matching.size()>=Integer.min(l1.size(),l2.size())){
                    minMatching = p;
                    minVars=utils.varsCount(p);

                }
            }
        }
        return minMatching;
    }





}
