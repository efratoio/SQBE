package tau.cs.db.qbp;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Variable;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.PathBlock;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.*;

/**
 * Created by efrat on 10/12/16.
 */
public class utils {


    public static int varsCount(ElementPathBlock pattern){
        int varsNum = 0;

        for(TriplePath triple : pattern.getPattern().getList()){
            if(triple.getSubject().isVariable()){
                varsNum++;
            }
            if(triple.getPredicate()!=null && triple.getPredicate().isVariable()){
                varsNum++;
            }
            if(triple.getObject().isVariable()){
                varsNum++;
            }
        }
        return varsNum;

    }


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

    public static boolean isBgpIsomorphic(ElementPathBlock p1, ElementPathBlock p2){
        if(p1 == null || p2 == null){
            return false;
        }
        if(p1.equals(p2)){
            return true;
        }
        Set<Node_Variable> variablesSet1 = new HashSet<Node_Variable>();
        Set<Node_Variable> variableSet2 = new HashSet<Node_Variable>();

        Comparator<TriplePath> cmp = new Comparator<TriplePath>() {
            @Override
            public int compare(TriplePath node, TriplePath other) {
                if (node.getSubject().toString().compareTo(other.getSubject().toString()) == 0) {
                    if (node.isTriple() && other.isTriple()) {
                        if (node.getPredicate().toString().compareTo(other.getPredicate().toString()) == 0) {
                            return node.getObject().toString().compareTo(other.getObject().toString());
                        } else {
                            return node.getPredicate().toString().compareTo(other.getPredicate().toString());
                        }
                    } else {
                        if (!node.isTriple() && !node.isTriple()) {
                            if (node.getPath().toString().compareTo(other.getPath().toString()) == 0) {
                                return node.getObject().toString().compareTo(other.getObject().toString());
                            }
                        } else {
                            return -1;
                        }
                    }
                }

                return node.getSubject().toString().compareTo(other.getSubject().toString());
            }

        };
        SortedSet<TriplePath> l1 = new TreeSet<TriplePath>(cmp);
        l1.addAll(p1.getPattern().getList());


        for(TriplePath t1: p1.getPattern()){
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
        for(TriplePath t2: p2.getPattern()){
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
//        l2.addAll(p2.getList());

//        Map<String,Node> nodeList = new HashMap<String,Node>();
//
//        Set<Pair<Node,Node>> nodeMatch = new HashSet<Pair<Node,Node>>();
//        if(variablesSet1.size()!=variableSet2.size()){
//            return false;
//        }
        if(variablesSet1.size()==0) {
            return true;
        }

        List<Node_Variable> lst1 = new ArrayList<Node_Variable>(variablesSet1);
        List<Node_Variable> lst2 = new ArrayList<Node_Variable>(variableSet2);
//
        for(List<Node_Variable> perm : utils.<Node_Variable>permute(lst1,0,null)){
            SortedSet<TriplePath> l2 = new TreeSet<TriplePath>(cmp);
            for(TriplePath t : p2.getPattern()){
                Node[] nodes=new Node[3];
                if(t.getSubject().isVariable()){
                    nodes[0]=perm.get(lst2.indexOf(t.getSubject()));
                }else{
                    nodes[0] = t.getSubject();
                }
                if(t.getPredicate().isVariable()){
                    nodes[1]=perm.get(lst2.indexOf(t.getPredicate()));
                }else{
                    nodes[1] = t.getPredicate();
                }
                if(t.getObject().isVariable()){
                    nodes[2]=perm.get(lst2.indexOf(t.getObject()));
                }else{
                    nodes[2] = t.getObject();
                }
                l2.add(new TriplePath(nodes[0],PathFactory.pathLink(nodes[1]),nodes[2]));
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


    public  static ElementPathBlock createMergedPattern(List<Pair<TriplePath,TriplePath>> matching){

        ElementPathBlock result = new ElementPathBlock();

        Iterator<String> var_itr = (new Alphabet('a','z')).iterator();

        Map<Node,Set<String>> var1 = new HashMap<Node,Set<String>>();
        Map<Node,Set<String>> var2 = new HashMap<Node,Set<String>>();

        Map<Pair<TriplePath,Boolean>,Set<TriplePath>> clustered = new HashMap<>();


        for(Pair<TriplePath,TriplePath> pair : matching){
            Set<TriplePath> s1= clustered.getOrDefault(new Pair<TriplePath,Boolean>(pair.getLeft(),false),new HashSet<TriplePath>());
            s1.add(pair.getRight());
            clustered.put(new Pair<TriplePath,Boolean>(pair.getLeft(),false),s1);
            Set<TriplePath> s2= clustered.getOrDefault(new Pair<TriplePath,Boolean>(pair.getRight(),true),new HashSet<TriplePath>());
            s2.add(pair.getLeft());
            clustered.put(new Pair<TriplePath,Boolean>(pair.getRight(),true),s2);
        }


        for(Map.Entry<Pair<TriplePath,Boolean>,Set<TriplePath>> entry : clustered.entrySet()){
            if(entry.getValue().size()>1){
                if(testConnectivity(entry.getValue())){
                    Path path = PathFactory.pathOneOrMore1(PathFactory
                            .pathAlt(PathFactory.pathLink(entry.getKey().getLeft().getPredicate()),
                                    PathFactory.pathInverse(
                                            PathFactory.pathLink(entry.getKey().getLeft().getPredicate()))));

                    result.addTriplePath(new TriplePath(entry.getKey().getLeft().getSubject(),
                            path,entry.getKey().getLeft().getObject()));

//                    Pair<Node,Node>  source_sink = t



                }
                if(entry.getKey().getRight()) {
                    for (TriplePath t :
                            entry.getValue()) {
                        matching.remove(new Pair<TriplePath, TriplePath>(t, entry.getKey().getLeft()));
                    }
                }else {
                    for (TriplePath t :
                            entry.getValue()) {
                        matching.remove(new Pair<TriplePath, TriplePath>(entry.getKey().getLeft(),t));
                    }
                }

            }
        }



        for(Pair<TriplePath,TriplePath> match : matching){
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

            result.addTriple(new Triple(sub,pred,obj));
        }

        return result;
    }



    private static boolean testConnectivity(Set<TriplePath> value) {
		// TODO Auto-generated method stub
		return false;
	}


	/***
     * Tests if there is a connetion between exam[le to other nodes
     * @param pattern
     * @return
     */
    public static boolean testBasicPatten(ElementPathBlock pattern){
        UndirectedGraph<String,DefaultEdge> graph = new SimpleGraph<String,DefaultEdge>(DefaultEdge.class);
        for (TriplePath triple : pattern.getPattern()){
            graph.addVertex(triple.getSubject().toString());
            graph.addVertex(triple.getObject().toString());
            graph.addEdge(triple.getSubject().toString(),triple.getObject().toString());
        }

        ConnectivityInspector<String,DefaultEdge> CI = new ConnectivityInspector<String, DefaultEdge>(graph);
        return CI.isGraphConnected();
    }

    public static DirectedGraph<String,DefaultEdge> makeGraph(Set<TriplePath> pattern){
        DirectedGraph<String,DefaultEdge> graph = new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        for (TriplePath triple : pattern){
            graph.addVertex(triple.getSubject().toString());
            graph.addVertex(triple.getObject().toString());
            graph.addEdge(triple.getSubject().toString(),triple.getObject().toString());
        }
        return graph;
    }
    public static boolean testConnectivityAndNoCycles(Set<TriplePath> pattern){

        DirectedGraph<String,DefaultEdge> graph = makeGraph(pattern);
        ConnectivityInspector<String,DefaultEdge> CI = new ConnectivityInspector<String, DefaultEdge>(graph);
        if(!CI.isGraphConnected())
            return false;

        CycleDetector<String,DefaultEdge> cd = new CycleDetector<>(graph);
        return !cd.detectCycles();
    }

    /**
     * For graphs with no cycles
     * @param pattern
     * @return
     */
    public static Pair<Node,Node> getSourceSink(Set<TriplePath> pattern){
        DirectedGraph<String,DefaultEdge> graph = makeGraph(pattern);
        TopologicalOrderIterator<String,DefaultEdge> it = new TopologicalOrderIterator<String, DefaultEdge>(graph);
        return null;
    }

    public static boolean getIsPath(Triple source ,Set<Triple> pattern){
        DirectedGraph<String,DefaultEdge> graph = new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        for (Triple triple : pattern){
            graph.addVertex(triple.getSubject().toString());
            graph.addVertex(triple.getObject().toString());
            graph.addEdge(triple.getSubject().toString(),triple.getObject().toString());
        }


        ConnectivityInspector<String,DefaultEdge> CI = new ConnectivityInspector<String, DefaultEdge>(graph);
        if(!CI.isGraphConnected())
            return false;

        boolean s =false;
        boolean o = false;
        for (Triple triple : pattern){
            if(graph.inDegreeOf(triple.getSubject().toString())==0 &&
                    graph.outDegreeOf(triple.getSubject().toString())==1){
                if(source.getSubject().toString().compareTo(triple.getSubject().toString())==0){
                    s=true;
                }

            }
            if(graph.outDegreeOf(triple.getObject().toString())==0 &&
                    graph.inDegreeOf(triple.getObject().toString())==1){
                if(source.getObject().toString().compareTo(triple.getObject().toString())==0){
                    o=true;
                }

            }
        }
        return true;
    }

    public static boolean testBoolArr(Boolean[] arr){
        return Arrays.stream(arr).reduce(true,(a,b)->Boolean.logicalAnd(a,b));
    }

    public static int countFalse(Boolean[] arr){
        int res = 0;
        for (Boolean b : arr){
            if(!b)
                res++;
        }
        return res;
    }


    public static ElementPathBlock FindMergeHandler(List<TriplePath> l1, List<TriplePath> l2, float[][] weightMatchMatrix,
                                                Boolean[] mark1, Boolean[] mark2 ,
                                                ArrayList<Pair<TriplePath,TriplePath>> matching,
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
            if(!mark1[arg_min.getLeft()] || !mark2[arg_min.getRight()]) {
                matching.add(new Pair<TriplePath,TriplePath>(l1.get(arg_min.getLeft()),l2.get(arg_min.getRight())));
                mark1[arg_min.getLeft()]=true;
                mark2[arg_min.getRight()] = true;

                for(int i=0;i<l1.size();i++){
                    for(int j=0;j<l2.size();j++){
                        if(l1.get(i).getSubject().matches(l2.get(j).getSubject())){
                            weightMatchMatrix[i][j] /=2;
                        }
                        if(l1.get(i).getObject().matches(l2.get(j).getObject())){
                            weightMatchMatrix[i][j] /=2;
                        }
                    }
                }
            }

            ElementPathBlock p = FindMergeHandler(l1,l2,weightMatchMatrix,mark1,mark2,matching,minimalQueue);
            if(p!=null){
                return p;
            }

            if(!m1Old || ! m2Old){
                matching.remove(new Pair<TriplePath,TriplePath>(l1.get(arg_min.getLeft()),l2.get(arg_min.getRight())));
                mark1[arg_min.getLeft()]=m1Old;
                mark2[arg_min.getRight()]=m2Old;
            }
            for(int i=0;i<l1.size();i++){
                for(int j=0;j<l2.size();j++){
                    if(l1.get(i).getSubject().matches(l2.get(j).getSubject())){
                        weightMatchMatrix[i][j] *=2;
                    }
                    if(l1.get(i).getObject().matches(l2.get(j).getObject())){
                        weightMatchMatrix[i][j] *=2;
                    }
                }
            }

//            minimalQueue.add(arg_min);
            weightMatchMatrix[arg_min.getLeft()][arg_min.getRight()] = oldVal;
        }

        if(testBoolArr(mark1) && testBoolArr(mark2)){
            ElementPathBlock p = createMergedPattern(matching);
            if(p!=null && testBasicPatten(p)){
                return p;
            }

        }
        return null;





    }

    static private Op flush(BasicPattern bp, Op op)
    {
        if ( bp == null || bp.isEmpty() )
            return op ;

        OpBGP opBGP = new OpBGP(bp) ;
        op = OpSequence.create(op, opBGP) ;
        return op ;
    }

    /** Convert any paths of exactly one predicate to a triple pattern */
    public static Op pathToTriples(PathBlock pattern)
    {
        BasicPattern bp = null ;
        Op op = null ;

        for ( TriplePath tp : pattern )
        {
            if ( tp.isTriple() )
            {
                if ( bp == null )
                    bp = new BasicPattern() ;
                bp.add(tp.asTriple()) ;
                continue ;
            }
            // Path form.
            op = flush(bp, op) ;
            bp = null ;

            OpPath opPath2 = new OpPath(tp) ;
            op = OpSequence.create(op, opPath2) ;
            continue ;
        }

        // End.  Finish off any outstanding BGP.
        op = flush(bp, op) ;
        return op ;
    }
/*
    public static ElementPathBlock FindBestMerge(ElementPathBlock p1, ElementPathBlock p2) {

        Logger logger = LoggerFactory.getLogger(App.class);
        List<TriplePath> l1 = p1.getPattern().getList();
        List<TriplePath> l2 = p2.getPattern().getList();


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

        for (TriplePath triple : l2){
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




        ElementPathBlock p= FindMergeHandler(l1,l2,weightMatchMatrix,mark1,mark2,matching,null);

        return p;


    }*/
}
