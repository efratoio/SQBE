package tau.cs.db.qbp;

import com.google.common.collect.Sets;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.AbstractDateTime;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.datatypes.xsd.impl.XSDBaseNumericType;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Variable;
import org.apache.jena.graph.Triple;
import org.apache.jena.reasoner.rulesys.builtins.LessThan;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.*;
import org.apache.jena.sparql.expr.nodevalue.NodeValueDT;
import org.apache.jena.sparql.expr.nodevalue.NodeValueInteger;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_OneOrMore1;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;
import java.util.logging.Filter;

/**
 * Created by efrat on 26/12/16.
 */
public class TripleMerger {

//    List<TriplePath> l1;
//    List<TriplePath> l2;
    Filterable filteredPattern1;
    Filterable filteredPattern2;

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
            TriplePath t1 = filteredPattern1.getPattern().get(this.x);
            TriplePath t2= filteredPattern2.getPattern().get(this.y);
            boolean flag=false;
            if(!mark1[this.x]){
                cost/=15;
            }
            if(!mark2[this.y]){
                cost/=15;
            }
            if(!mark1[this.x] && !mark2[this.y]){
                cost/=20;
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
                cost/=10000;
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

    public TripleMerger(Filterable fp1, Filterable fp2) {
        this.filteredPattern1 = fp1;
        this.filteredPattern2 = fp2;
        this.mark1 = new Boolean[fp1.getPattern().size()];
        this.mark2 = new Boolean[fp2.getPattern().size()];
        this.matching = new ArrayList<MergeSuggestion>();
        this.varsMerged = new HashMap<Pair<Node, Node>, Integer>();

    }

    /***
     * Clears all fields that influence creation of new suggestions
     */
    public void fillSuggestion(){
        this.minimalQueue = new PriorityQueue<MergeSuggestion>();
        Arrays.fill(this.mark1, false);
        Arrays.fill(this.mark2, false);
        this.matching.clear();
        this.varsMerged.clear();
        for (int i = 0; i < filteredPattern1.getPattern().size(); i++) {
            for (int j = 0; j < filteredPattern2.getPattern().size(); j++) {
                this.minimalQueue.add(new MergeSuggestion(i, j));
            }
        }
    }

    /***
     * recursive function greedily adds best merege suggestion
     * @return list of triples that being matched
     */
    public List<MatchTriplePath> mergeHandler() {
        while (this.minimalQueue.size()>0 && (!utils.testBoolArr(this.mark1) || !utils.testBoolArr(this.mark2))) {
            reorderQueue();
            MergeSuggestion sug = this.minimalQueue.poll();
            TriplePath t1 = TripleMerger.this.filteredPattern1.getPattern().get(sug.x);
            TriplePath t2 = TripleMerger.this.filteredPattern2.getPattern().get(sug.y);
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

        }

        List<MatchTriplePath> match = new ArrayList<MatchTriplePath>();
        for (MergeSuggestion suggestion : this.matching){
            match.add(new MatchTriplePath(filteredPattern1.getPattern().get(suggestion.x)
                    ,filteredPattern2.getPattern().get(suggestion.y)));
        }
        return match;

    }

    /***
     * The java priority queue doesn't support changes of comparisons between existing elements
     */
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

    /***
     * Handle the cases where one triplepath is atched to several.
     * Creates property paths and handles Expression lists
     * @param matching
     */
    private void handleClusters(List<MatchTriplePath> matching){

        Map<Pair<TriplePath,Boolean>,Set<TriplePath>> clustered = new HashMap<>();


        for(MatchTriplePath pair : matching){
            Set<TriplePath> s1= clustered.getOrDefault(new Pair<TriplePath,Boolean>(pair.getLeft(),false),new HashSet<TriplePath>());
            s1.add(pair.getRight());
            clustered.put(new Pair<TriplePath,Boolean>(pair.getLeft(),false),s1);
            Set<TriplePath> s2= clustered.getOrDefault(new Pair<TriplePath,Boolean>(pair.getRight(),true),new HashSet<TriplePath>());
            s2.add(pair.getLeft());
            clustered.put(new Pair<TriplePath,Boolean>(pair.getRight(),true),s2);
        }


        for(Map.Entry<Pair<TriplePath,Boolean>,Set<TriplePath>> entry : clustered.entrySet()){
            if(entry.getValue().size()>1){
                if(utils.TestConnectivityWithPath(entry.getValue())) {
                    Path path = PathFactory.pathOneOrMore1(PathFactory.pathLink(entry.getKey().
                            getLeft().getPredicate()));


                    Pair<Node,Node> s_s = utils.GetSourceSink(entry.getValue());


                    if (entry.getKey().getRight()) {
                        for (TriplePath t :
                                entry.getValue()) {
                            matching.remove(new MatchTriplePath(t, entry.getKey().getLeft()));

                        }
                        matching.add(new MatchTriplePath(new TriplePath(s_s.getLeft(),path,
                                s_s.getRight())
                                , entry.getKey().getLeft()));
                    } else {
                        for (TriplePath t :
                                entry.getValue()) {
                            matching.remove(new Pair<TriplePath, TriplePath>(entry.getKey().getLeft(), t));
                        }
                        matching.add(new MatchTriplePath(entry.getKey().getLeft(),
                                new TriplePath(s_s.getLeft(),path,
                                        s_s.getRight())
                        ));
                    }
                }
            }
        }

    }

    /***
     * creates a new Filterable object that represents the pattern of the merged patterns
     * @param matching
     * @return
     */
    public Filterable createMergedPattern(List<MatchTriplePath> matching){

        ElementPathBlock result = new ElementPathBlock();
        Iterator<String> var_itr = (new Alphabet()).iterator();


        ExprList exprsList = new ExprList();

        Variable2Nodes var1 = new Variable2Nodes();
        Variable2Nodes var2 = new Variable2Nodes();



        for(MatchTriplePath match : matching){
            Node sub = null;
            Node obj =null;


            //match subject if they match
            if(match.testSubjectMatches()) {

                sub = match.getLeftSubject();
            }
            //try to find variable that fits both
            else{
                if(var1.containsKey(match.getLeftSubject()) && var2.containsKey(match.getRightSubject())){
                    Set<String> var = var1.get(match.getLeftSubject());
                    var.retainAll(var2.get(match.getRightSubject()));
                    if(var.size()>0){
                        sub = new Node_Variable(var.iterator().next());
                    }
                }

            }
            //if no match so far, add new variable
            if(sub==null) {
                String v = var_itr.next();
                sub= new Node_Variable(v);
                Set<String> var1_set = var1.getOrDefault(match.getLeftSubject(),new HashSet<String>());
                var1_set.add(v);
                var1.put(match.getLeft().getSubject(),var1_set);

                Set<String> var2_set = var2.getOrDefault(match.getRightSubject(),new HashSet<String>());
                var2_set.add(v);
                var2.put(match.getRight().getSubject(),var2_set);


            }

            //match object
            if(match.testObjectMatches()) {

                obj = match.getLeftObject();
            }
            else{



                if(var1.containsKey(match.getLeft().getObject()) && var2.containsKey(match.getRight().getObject())){
                    Set<String> var = var1.get(match.getLeft().getObject());
                    var.retainAll(var2.get(match.getRight().getObject()));
                    if(var.size()>0){
                        obj = new Node_Variable(var.iterator().next());
                    }
                }

            }
            if(obj==null) {
                String v = var_itr.next();
                obj= new Node_Variable(v);
                Set<String> var1_set = var1.getOrDefault(match.getLeft().getObject(),new HashSet<String>());
                var1_set.add(v);
                var1.put(match.getLeft().getObject(),var1_set);

                Set<String> var2_set = var2.getOrDefault(match.getRight().getObject(),new HashSet<String>());
                var2_set.add(v);
                var2.put(match.getRight().getObject(),var2_set);



                //add expression of range if datatypes matches
                if(match.getLeftObject().isLiteral() && match.getRightObject().isLiteral()
                        && match.getLeftObject().getLiteral().getDatatype().equals(match.getRightObject().getLiteral().getDatatype())){

                    RDFDatatype dtype = match.getLeftObject().getLiteral().getDatatype();

                    if(dtype.getJavaClass().equals(java.math.BigInteger.class)){
                        int cmp = ((Integer)match.getLeftObject().getLiteral().getValue())
                        .compareTo((Integer)match.getRightObject().getLiteral().getValue());
                        if(cmp < 0){
                            exprsList.add(new E_LessThanOrEqual(new ExprVar(obj),
                                    new NodeValueInteger((new BigInteger(match.getRightObject().getLiteral().getValue().toString())),
                                            match.getRightObject())));
                            exprsList.add(new E_GreaterThanOrEqual(new ExprVar(obj),
                                    new NodeValueInteger((new BigInteger(match.getLeftObject().getLiteral().getValue().toString())),
                                            match.getLeftObject())));

                        }else{
                            exprsList.add(new E_LessThanOrEqual(new ExprVar(obj),
                                    new NodeValueInteger((new BigInteger(match.getLeftObject().getLiteral().getValue().toString())),
                                            match.getLeftObject())));
                            exprsList.add(new E_GreaterThanOrEqual(new ExprVar(obj),
                                    new NodeValueInteger((new BigInteger(match.getRightObject().getLiteral().getValue().toString())),
                                            match.getRightObject())));

                        }
                    }

                    if(dtype instanceof AbstractDateTime){
                        int cmp = ((AbstractDateTime)match.getLeftObject().getLiteral().getValue())
                                .compare((AbstractDateTime)match.getRightObject().getLiteral().getValue());
                        if(cmp == AbstractDateTime.LESS_THAN){
                            exprsList.add(new E_LessThanOrEqual(new ExprVar(sub),
                                    new NodeValueDT(match.getRightObject().getLiteral().getValue().toString(),
                                            match.getRightObject())));
                            exprsList.add(new E_GreaterThanOrEqual(new ExprVar(sub),
                                    new NodeValueDT(match.getLeftObject().getLiteral().getValue().toString(),
                                            match.getLeftObject())));

                        }else{
                            exprsList.add(new E_LessThanOrEqual(new ExprVar(sub),
                                    new NodeValueDT(match.getLeftObject().getLiteral().getValue().toString(),
                                            match.getLeftObject())));
                            exprsList.add(new E_GreaterThanOrEqual(new ExprVar(sub),
                                    new NodeValueDT(match.getRightObject().getLiteral().getValue().toString(),
                                            match.getRightObject())));

                        }
                    }

                }

            }
            if(match.getLeft().isTriple() && match.getRight().isTriple()) {
                if(match.getLeft().getPredicate().matches(match.getRight().getPredicate())) {
                    result.addTriple(new Triple(sub, match.getLeft().getPredicate(), obj));
                }
                else{
                    result.addTriple(new TriplePath(sub,
                            PathFactory.pathAlt(PathFactory.pathLink(match.getLeft().getPredicate()),
                                    PathFactory.pathLink(match.getRight().getPredicate())),
                            obj));
                }

            }else{

                if(match.getLeft().getPath() instanceof P_OneOrMore1)
                {
                    P_OneOrMore1 link = (P_OneOrMore1)match.getLeft().getPath();
                    if(((P_Link)link.getSubPath()).getNode().matches(match.getRight().getPredicate())) {
                        result.addTriplePath(new TriplePath(sub,link,obj));
                    }
                }
                else{
                    if(match.getRight().getPath() instanceof P_OneOrMore1)
                    {
                        P_OneOrMore1 link = (P_OneOrMore1)match.getRight().getPath();
                        if(((P_Link)link.getSubPath()).getNode().matches(match.getLeft().getPredicate())) {
                            result.addTriplePath(new TriplePath(sub,link,obj));
                        }
                    }
                }

            }


        }
        for(Expr expr1 : this.filteredPattern1.getExpList()){
            if(expr1 instanceof E_NotEquals) {
                Node vl1 = ((E_NotEquals) expr1).getArg1().getConstant().asNode();
                Node vr1 = ((E_NotEquals) expr1).getArg2().getConstant().asNode();

                Set<String> strSetl1 = var1.get(vl1);
                Set<String> strSetr1 = var1.get(vr1);

                for (Expr expr2 : this.filteredPattern2.getExpList()) {
                    if (expr2 instanceof E_NotEquals) {
                        Node vl2 = ((E_NotEquals) expr2).getArg1().getConstant().asNode();
                        Node vr2 = ((E_NotEquals) expr2).getArg2().getConstant().asNode();

                        Set<String> strSetl2 = var2.get(vl2);
                        Set<String> strSetr2 = var2.get(vr2);

                        if (strSetl1 == null) {

                            //found an expression matching in first arg
                            if (vl1.matches(vl2)) {

                                if (strSetr1 == null) {
                                    if (vr1.matches(vr2)) {
                                        exprsList.add(expr1);
                                        break;
                                    }
                                } else {
                                    if (strSetr2 == null) {
                                        continue;
                                    }
                                    Set<String> iset2 = Sets.intersection(strSetr1, strSetr2);
                                    if (iset2.size() > 0) {
                                        Expr temp = new E_NotEquals(new NodeValueNode(vl1),
                                                new NodeValueNode(new Node_Variable
                                                        (iset2.iterator().next())));
                                        exprsList.add(temp);
                                        break;
                                    }
                                }

                            }
                            } else {
                                if (strSetl2 == null) {
                                    continue;
                                }
                                Set<String> iset1 = Sets.intersection(strSetl1, strSetl2);
                                if (iset1.size() > 0) {
                                    if (strSetr1 == null) {
                                        if (vr1.matches(vr2)) {
                                            Expr temp = new E_NotEquals(new NodeValueNode(new Node_Variable
                                                    (iset1.iterator().next())), new NodeValueNode(vr1));
                                            exprsList.add(temp);
                                            break;
                                        }
                                    } else {
                                        if (strSetr2 == null) {
                                            continue;
                                        }
                                        Set<String> iset2 = Sets.intersection(strSetr1, strSetr2);
                                        if (iset2.size() > 0) {
                                            Expr temp = new E_NotEquals(new NodeValueNode(new Node_Variable
                                                    (iset1.iterator().next())), new NodeValueNode(new Node_Variable
                                                    (iset2.iterator().next())));
                                            exprsList.add(temp);
                                            break;
                                        }
                                    }

                                }
                            }
                        }
                    }
                }


        }

        return new FilterablePatternDefault(result.getPattern().getList(),exprsList);
    }


    private static int PatternCost(Filterable epb){
        int cost = utils.varsCount(epb);
        for(TriplePath tp : epb.getPattern()){
            if(!tp.isTriple()){
                cost+=5;
            }
        }
        return cost;
    }

    public Filterable merge(){
        Logger logger = LoggerFactory.getLogger(TripleMerger.class);

//        logger.info(String.format("Merge %s with %s",this.filteredPattern1.toString(),filteredPattern2.toString() ));
        fillSuggestion();

        int l =this.minimalQueue.size();

        Filterable minMatching = null;
        float pattCost = 1000;
        for (int i=0; i<Math.max(l-5,2);i++){
            fillSuggestion();


            for(int j=0;j<i;j++){
                this.minimalQueue.poll();
            }

            List<MatchTriplePath> matching = mergeHandler();
//            logger.info(String.format("matching: %s", matching.toString()));
            Filterable p = createMergedPattern(matching);
//            logger.info(String.format("pattern: %s",p.toString()));
//            logger.info("****************");
            if(p!=null && utils.testBasicPatten(p)){
                if(PatternCost(p)<pattCost && matching.size()<=filteredPattern1.getPattern().size()+
                        filteredPattern2.getPattern().size() &&
                        matching.size()>=Integer.min(filteredPattern1.getPattern().size(),
                                filteredPattern2.getPattern().size())){
                    minMatching = p;
                    pattCost=utils.varsCount(p);

                }
            }
        }
        return minMatching;
    }





}
