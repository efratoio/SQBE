package tau.cs.db.qbp;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Variable;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tau.cs.db.App;

import java.util.*;

/**
 * Created by efrat on 10/12/16.
 */
public class utils {



    public static <T> List<List<T>> permute(List<T> arr, int k, List<List<T>> res){
        if(res == null){
            res = new ArrayList<List<T>>();
        }
        for(int i = k; i < arr.size(); i++){
            java.util.Collections.swap(arr, i, k);
            permute(arr, k+1,res);
            java.util.Collections.swap(arr, k, i);
        }
        if (k == arr.size() -1){
            res.add(new ArrayList<T>(arr));

        }
        return res;
    }

    public static boolean isBgpIsomorphic(BasicPattern p1, BasicPattern p2){
        Set<Node_Variable> variablesSet1 = new HashSet<Node_Variable>();
        Set<Node_Variable> variableSet2 = new HashSet<Node_Variable>();

        Comparator<Triple> cmp = new Comparator<Triple>() {
            @Override
            public int compare(Triple node, Triple other) {
                if(node.getSubject().toString().compareTo(other.getSubject().toString())==0){
                    if(node.getPredicate().toString().compareTo(other.getPredicate().toString())==0){
                        return node.getPredicate().toString().compareTo(other.getPredicate().toString());
                    }else{
                        return node.getObject().toString().compareTo(other.getObject().toString());
                    }
                }else{
                    return node.getSubject().toString().compareTo(other.getSubject().toString());
                }
            }
        };
        SortedSet<Triple> l1 = new TreeSet<Triple>(cmp);
        l1.addAll(p1.getList());


        for(Triple t1: p1){
            Node s = t1.getSubject();
            Node p = t1.getPredicate();
            Node o = t1.getObject();
            if(s.isVariable()) {
                variablesSet1.add((Node_Variable) s);
            }
            if(p.isVariable()) {
                variablesSet1.add((Node_Variable) p);
            }
            if(o.isVariable()) {
                variablesSet1.add((Node_Variable) o);
            }
        }
        for(Triple t2: p2){
            Node s = t2.getSubject();
            Node p = t2.getPredicate();
            Node o = t2.getObject();
            if(s.isVariable()) {
                variableSet2.add((Node_Variable) s);
            }
            if(p.isVariable()) {
                variableSet2.add((Node_Variable)p);
            }
            if(o.isVariable()) {
                variableSet2.add((Node_Variable)o);
            }
        }
        if(variableSet2.size()!=variablesSet1.size())
            return false;
        SortedSet<Triple> l2 = new TreeSet<Triple>(cmp);
//        l2.addAll(p2.getList());

//        Map<String,Node> nodeList = new HashMap<String,Node>();
//
//        Set<Pair<Node,Node>> nodeMatch = new HashSet<Pair<Node,Node>>();
//        if(variablesSet1.size()!=variableSet2.size()){
//            return false;
//        }
        if(variablesSet1.size()==0)
            return true;

        List<Node_Variable> lst1 = new ArrayList<Node_Variable>(variablesSet1);
        List<Node_Variable> lst2 = new ArrayList<Node_Variable>(variableSet2);
//
        for(List<Node_Variable> perm : utils.<Node_Variable>permute(lst1,0,null)){
            for(Triple t : p2){
                Node[] nodes=new Node[3];
                if(t.getSubject().isVariable()){
                    nodes[0]=lst1.get(lst2.indexOf(t.getSubject()));
                }else{
                    nodes[0] = t.getSubject();
                }
                if(t.getPredicate().isVariable()){
                    nodes[0]=lst1.get(lst2.indexOf(t.getPredicate()));
                }else{
                    nodes[0] = t.getPredicate();
                }
                if(t.getObject().isVariable()){
                    nodes[0]=lst1.get(lst2.indexOf(t.getObject()));
                }else{
                    nodes[0] = t.getObject();
                }

            }
            if(equiv(l1,l2,cmp))
                return true;
        }
        return false;

    }

    public static <T> boolean equiv(SortedSet<T> s1, SortedSet<T> s2, Comparator cmp){
        if(s1.size()!=s2.size()){
            return false;
        }
        Iterator<T> itr = s2.iterator();
        for(T t:s1){
            if(cmp.compare(t,itr.next())!=0){
                return false;
            }
        }
        return true;
    }
    public static SortedSet<Pair<Pair<Integer,Integer>,Float>> kMin(Float[][] arr,Integer k){
        SortedSet<Pair<Pair<Integer,Integer>,Float>> res = new TreeSet<>();
        for(int i=0;i<arr.length;i++){
            for(int j=0;j<arr[0].length;j++){
                if(res.last().getRight()>arr[i][j]){
                    res.add(new Pair<>(new Pair<Integer, Integer>(i,j),arr[i][j]));
                }
                while(res.size()>k){
                    res.remove(res.last());
                }
            }

        }
        return res;

    }


    public static Pair<Integer,Integer> argmin(float[][] arr){
        Float m_v = Float.MAX_VALUE;
        Integer m_i = -1,m_j = -1;
        for(int i=0;i<arr.length;i++){
            for(int j=0;j<arr[0].length;j++){
                if(arr[i][j]<m_v){
                    m_v = arr[i][j];
                    m_i=i;
                    m_j=j;
                }
            }
        }
        if(m_v == 1){
            return new Pair<Integer,Integer>(-1,-1);
        }
        return new Pair<Integer,Integer>(m_i,m_j);
    }

    public  static  BasicPattern CreateMergedPattern(ArrayList<Pair<Triple,Triple>> matching){

        BasicPattern result = new BasicPattern();

        Iterator<String> var_itr = (new Alphabet('a','z')).iterator();

        Map<Node,Set<String>> var1 = new HashMap<Node,Set<String>>();
        Map<Node,Set<String>> var2 = new HashMap<Node,Set<String>>();



        for(Pair<Triple,Triple> match : matching){
            Node sub = null;
            Node obj =null;
            Node pred = null;


            if(match.getLeft().getSubject().matches(match.getRight().getSubject())) {

                sub = match.getLeft().getSubject();
            }
            else{
                if(var1.containsKey(match.getLeft().getSubject()) && var2.containsKey(match.getRight().getSubject())){
                    Set<String> var = var1.get(match.getLeft().getSubject());
                    var.retainAll(var2.get(match.getRight().getSubject()));
                    if(var.size()>0){
                        sub = new Node_Variable(var.iterator().next());
                    }
                }

            }
            if(sub==null) {
                String v = var_itr.next();
                sub= new Node_Variable(v);
                Set<String> var1_set = var1.getOrDefault(match.getLeft().getSubject(),new HashSet<String>());
                var1_set.add(v);
                var1.put(match.getLeft().getSubject(),var1_set);

                Set<String> var2_set = var2.getOrDefault(match.getRight().getSubject(),new HashSet<String>());
                var2_set.add(v);
                var2.put(match.getRight().getSubject(),var2_set);
            }

            if(match.getLeft().getObject().matches(match.getRight().getObject())) {

                obj = match.getLeft().getObject();
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
            }
            if(match.getLeft().getPredicate().matches(match.getRight().getPredicate())) {

                pred = match.getLeft().getPredicate();
            }else {
                pred = new Node_Variable(var_itr.next());
            }

            result.add(new Triple(sub,pred,obj));
        }

        return result;
    }

    /***
     * Tests if there is a connetion between exam[le to other nodes
     * @param pattern
     * @return
     */
    public static boolean testBasicPatten(BasicPattern pattern){
        UndirectedGraph<String,DefaultEdge> graph = new SimpleGraph<String,DefaultEdge>(DefaultEdge.class);
        for (Triple triple : pattern){
            graph.addVertex(triple.getSubject().toString());
            graph.addVertex(triple.getObject().toString());
            graph.addEdge(triple.getSubject().toString(),triple.getObject().toString());
        }

        ConnectivityInspector<String,DefaultEdge> CI = new ConnectivityInspector<String, DefaultEdge>(graph);
        return CI.isGraphConnected();
    }

    private static boolean testBoolArr(Boolean[] arr){
        return Arrays.stream(arr).reduce(true,(a,b)->Boolean.logicalAnd(a,b));
    }

    public static BasicPattern FindMergeHandler(List<Triple> l1, List<Triple> l2, float[][] weightMatchMatrix,
                                                Boolean[] mark1, Boolean[] mark2 ,
                                                ArrayList<Pair<Triple,Triple>> matching,
                                                PriorityQueue<Pair<Integer,Integer>> minimalQueue){
        if(minimalQueue==null) {
            minimalQueue = new PriorityQueue<Pair<Integer, Integer>>(new Comparator<Pair<Integer, Integer>>() {
                @Override
                public int compare(Pair<Integer, Integer> integerIntegerPair, Pair<Integer, Integer> t1) {
                    return Float.compare(weightMatchMatrix[integerIntegerPair.getLeft()][integerIntegerPair.getRight()],
                            weightMatchMatrix[t1.getLeft()][t1.getRight()]);
                }
            }
            );

            for (int i = 0; i < l1.size(); i++) {
                for (int j = 0; j < l2.size(); j++) {
                    minimalQueue.add(new Pair<>(i, j));

                }
            }
        }
        while(minimalQueue.size()>0 && !utils.testBoolArr(mark1) && !utils.testBoolArr(mark2))
        {

            Pair<Integer,Integer> arg_min  = minimalQueue.poll();
            float oldVal = weightMatchMatrix[arg_min.getLeft()][arg_min.getRight()];
            Boolean m1Old = mark1[arg_min.getLeft()];
            Boolean m2Old = mark2[arg_min.getRight()];
            weightMatchMatrix[arg_min.getLeft()][arg_min.getRight()] =1;
            if(!mark1[arg_min.getLeft()] || !mark2[arg_min.getLeft()]) {
                matching.add(new Pair<Triple,Triple>(l1.get(arg_min.getLeft()),l2.get(arg_min.getRight())));
                mark1[arg_min.getLeft()]=true;
                mark2[arg_min.getLeft()] = true;

                for(int i=0;i<l1.size();i++){
                    for(int j=0;j<l2.size();j++){
                        if(l1.get(i).subjectMatches(l2.get(j).getSubject())){
                            weightMatchMatrix[i][j] /=2;
                        }
                        if(l1.get(i).objectMatches(l2.get(j).getObject())){
                            weightMatchMatrix[i][j] /=2;
                        }
                    }
                }
            }

            BasicPattern p = FindMergeHandler(l1,l2,weightMatchMatrix,mark1,mark2,matching,minimalQueue);
            if(p!=null){
                return p;
            }

            if(!m1Old || ! m2Old){
                matching.remove(new Pair<Triple,Triple>(l1.get(arg_min.getLeft()),l2.get(arg_min.getRight())));
                mark1[arg_min.getLeft()]=m1Old;
                mark2[arg_min.getRight()]=m2Old;
            }
            for(int i=0;i<l1.size();i++){
                for(int j=0;j<l2.size();j++){
                    if(l1.get(i).subjectMatches(l2.get(j).getSubject())){
                        weightMatchMatrix[i][j] *=2;
                    }
                    if(l1.get(i).objectMatches(l2.get(j).getObject())){
                        weightMatchMatrix[i][j] *=2;
                    }
                }
            }

            minimalQueue.add(arg_min);
            weightMatchMatrix[arg_min.getLeft()][arg_min.getRight()] = oldVal;
        }

        if(testBoolArr(mark1) && testBoolArr(mark2)){
            BasicPattern p = CreateMergedPattern(matching);
            if(p!=null && testBasicPatten(p)){
                return p;
            }

        }
        return null;





    }


    public static BasicPattern FindBestMerge(BasicPattern p1, BasicPattern p2) {

        Logger logger = LoggerFactory.getLogger(App.class);
        List<Triple> l1 = p1.getList();
        List<Triple> l2 = p2.getList();


        HashMap<Node,Integer> P2 = new HashMap<Node, Integer>();

        HashMap<Node,Set<Triple>> node2Triple_p1 = new HashMap<Node, Set<Triple>>();
        HashMap<Node,Set<Triple>> node2Triple_p2 = new HashMap<Node, Set<Triple>>();

        for(Triple triple : l1){

            Set<Triple> lst_s = node2Triple_p1.getOrDefault(triple.getSubject(),new HashSet<>());
            Set<Triple> lst_o = node2Triple_p1.getOrDefault(triple.getObject(),new HashSet<>());
            lst_s.add(triple);
            lst_o.add(triple);
            node2Triple_p1.put(triple.getSubject(),lst_s);
            node2Triple_p1.put(triple.getObject(),lst_o);
        }



        HashMap<Node,Integer> edge2Triple_p2 = new HashMap<Node, Integer>();

        for (Triple triple : l2){
            Integer count = edge2Triple_p2.getOrDefault(triple.getPredicate(),0);
            edge2Triple_p2.put(triple.getPredicate(),count+1);

            Set<Triple> lst_s = node2Triple_p2.getOrDefault(triple.getSubject(),new HashSet<>());
            Set<Triple> lst_o = node2Triple_p2.getOrDefault(triple.getObject(),new HashSet<>());
            lst_s.add(triple);
            lst_o.add(triple);
            node2Triple_p2.put(triple.getSubject(),lst_s);
            node2Triple_p2.put(triple.getObject(),lst_o);
        }

        Integer predicates2Sum =  edge2Triple_p2.values().stream().mapToInt(Integer::intValue).sum();


        float[][] weightMatchMatrix =  new float[l1.size()][l2.size()];
        Boolean[] mark1 = new Boolean[l1.size()];
        Boolean[] mark2 = new Boolean[l2.size()];
        Arrays.fill(mark1,false);
        Arrays.fill(mark2,false);



        for(int i=0;i<l1.size();i++){
            for(int j=0;j<l2.size();j++){
                if(l1.get(i).getPredicate().matches(l2.get(j).getPredicate())){
                    weightMatchMatrix[i][j] = edge2Triple_p2.get(l1.get(i).getPredicate()).floatValue()/(predicates2Sum*5);
                }else{
                    weightMatchMatrix[i][j] = 1;
                }
                if(l1.get(i).getSubject().matches(l2.get(j).getSubject())){
                    weightMatchMatrix[i][j] = weightMatchMatrix[i][j]/2;
                }
                if(l1.get(i).getObject().matches(l2.get(j).getObject())){
                    weightMatchMatrix[i][j] = weightMatchMatrix[i][j]/2;
                }


            }
        }

        ArrayList<Pair<Triple,Triple>> matching = new ArrayList<>();




        BasicPattern p= FindMergeHandler(l1,l2,weightMatchMatrix,mark1,mark2,matching,null);

        return p;


    }
}
