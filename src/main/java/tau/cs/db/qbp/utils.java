package tau.cs.db.qbp;

import org.apache.commons.math3.analysis.function.Exp;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.XSDBaseNumericType;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Variable;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.PathBlock;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.*;
import org.apache.jena.sparql.expr.nodevalue.NodeValueInteger;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_OneOrMore1;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.vocabulary.XSD;

import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.TopologicalOrderIterator;


import java.math.BigInteger;
import java.util.*;

/**
 * Created by efrat on 10/12/16.
 */
public class utils {

    public static void fillClassMap(Map<Node,Node> classMap, Model model){
        for (Node n : classMap.keySet()  ) {
            Query classQuery = QueryFactory.create("SELECT ?class WHERE {?a a ?class}");

            classQuery.setQuerySelectType();

            QuerySolutionMap initialBinding = new QuerySolutionMap();
            initialBinding.add("a", model.getRDFNode(n));
            QueryExecution qexec = QueryExecutionFactory.create(classQuery, model,initialBinding);

            ResultSet rs = qexec.execSelect();
            if(rs.hasNext()){
                Binding bind = rs.nextBinding();
                classMap.put(n,bind.get(Var.alloc("class")));
            }


        }

    }

    public static int varsCount(Patternable pattern){
        int varsNum = 0;

        for(TriplePath triple : pattern.getPattern()){
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

    /***
     * The list must be all literals, and all same datatype
     * @param ln
     * @return
     */
    public static ExprList getRangeExpr(Var v, List<Node> ln) {
        RDFDatatype rdt = ln.get(0).getLiteralDatatype();
        ExprList elst = new ExprList();

        if (rdt.getJavaClass().equals(java.math.BigInteger.class)) {
            Integer min = (Integer) ln.get(0).getLiteral().getValue();
            Integer max = (Integer) ln.get(0).getLiteral().getValue();

            for (Node n : ln) {
                Integer num = (Integer) n.getLiteral().getValue();
                if (num < min)
                    min = num;
                if (max < num)
                    max = num;

            }
            elst.add(new E_LessThanOrEqual(new ExprVar(v),
                    new NodeValueInteger(new BigInteger(max.toString()))));
            elst.add(new E_GreaterThanOrEqual(new ExprVar(v),
                    new NodeValueInteger(new BigInteger(min.toString()))));
        }
//        if(rdt instanceof XSDBaseNumericType){

        return elst;
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

    public static boolean isBgpIsomorphic(List<TriplePath> p1, List<TriplePath> p2){
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
        l1.addAll(p1);


        for(TriplePath t1: p1){

            Node s = t1.getSubject();
            Node p = t1.getPredicate();
            Node o = t1.getObject();
            if(s.isVariable()) {
                variablesSet1.add((Node_Variable) s);
            }
            if(t1.isTriple() && p.isVariable()) {
                variablesSet1.add((Node_Variable) p);
            }

            if(o.isVariable()) {
                variablesSet1.add((Node_Variable) o);
            }
        }
        for(TriplePath t2: p2){
            Node s = t2.getSubject();
            Node p = t2.getPredicate();
            Node o = t2.getObject();
            if(s.isVariable()) {
                variableSet2.add((Node_Variable) s);
            }
            if(t2.isTriple() && p.isVariable()) {
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
        for(List<Node_Variable> perm : utils.permute(lst1,0,null)){
            SortedSet<TriplePath> l2 = new TreeSet<TriplePath>(cmp);
            for(TriplePath t : p2){
                Node[] nodes=new Node[3];
                Path path=null;
                if(t.getSubject().isVariable()){
                    nodes[0]=perm.get(lst2.indexOf(t.getSubject()));
                }else{
                    nodes[0] = t.getSubject();
                }
                if(t.isTriple() && t.getPredicate().isVariable()){
                    path = PathFactory.pathLink(perm.get(lst2.indexOf(t.getPredicate())));
                }else{
                    if(t.isTriple()) {
                        path = PathFactory.pathLink(t.getPredicate());
                    }
                    else{
                        path = t.getPath();
                    }
                }
                if(t.getObject().isVariable()){
                    nodes[2]=perm.get(lst2.indexOf(t.getObject()));
                }else{
                    nodes[2] = t.getObject();
                }
                l2.add(new TriplePath(nodes[0],path,nodes[2]));
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





    public static boolean TestConnectivityWithPath(Set<TriplePath> value) {
        DirectedGraph<String,DefaultEdge> graph = makeDirectedGraph(value, true);
        if(!testConnectivityAndNoCycles(graph)){
            return false;
        }

        TopologicalOrderIterator<String,DefaultEdge> itr = new TopologicalOrderIterator<String, DefaultEdge>(graph);
        while( itr.hasNext()){
            String node = itr.next();
            if(graph.inDegreeOf(node)>1 || graph.outDegreeOf(node)>1){
                return false;
            }
        }
		return true;
	}

    public static DirectedGraph<Node,DefaultEdge> makeDirectedGraphNodes(Set<TriplePath> pattern, boolean b){
        DirectedGraph<Node,DefaultEdge> graph = new DefaultDirectedGraph<Node, DefaultEdge>(DefaultEdge.class);
        for (TriplePath triple : pattern){
            graph.addVertex(triple.getSubject());
            graph.addVertex(triple.getObject());
            graph.addEdge(triple.getSubject(),triple.getObject());
        }
        return graph;
    }

	static Pair<Node,Node> GetSourceSink(Set<TriplePath> value){
        DirectedGraph<Node,DefaultEdge> graph = makeDirectedGraphNodes(value,true);

        TopologicalOrderIterator<Node,DefaultEdge> itr = new TopologicalOrderIterator<Node, DefaultEdge>(graph);
        Node source = itr.next();
        Node sink=null;
        while( itr.hasNext()){
            sink = itr.next();

        }

        return new Pair<Node,Node>(source,sink);
    }

	/***
     * Tests if there is a connetion between example to other nodes
     * @param pattern
     * @return
     */
    public static boolean testBasicPatten(Patternable pattern){
        boolean flag = false;
        UndirectedGraph<String,DefaultEdge> graph = new Pseudograph<String, DefaultEdge>(DefaultEdge.class);
        for (TriplePath triple : pattern.getPattern()){
            if((triple.getSubject().isVariable() && triple.getSubject().getName().contains("example"))
                    || (triple.getObject().isVariable() && triple.getObject().getName().contains("example"))){
                flag = true;
            }
            graph.addVertex(triple.getSubject().toString());
            graph.addVertex(triple.getObject().toString());
            graph.addEdge(triple.getSubject().toString(),triple.getObject().toString());
        }

        ConnectivityInspector<String,DefaultEdge> CI = new ConnectivityInspector<String, DefaultEdge>(graph);
        return CI.isGraphConnected() && flag;
    }

    public static class RelationshipEdge<V> extends DefaultEdge {
        private V v1;
        private V v2;
        private Statement label;

        public RelationshipEdge(V v1, V v2, Statement label) {
            this.v1 = v1;
            this.v2 = v2;
            this.label = label;
        }

        public V getV1() {
            return v1;
        }

        public V getV2() {
            return v2;
        }

        public Statement GetNode(){
            return this.label;
        }
        public String toString() {
            return this.label.toString();
        }
    }
    public static Model ConnectAllComponents(Node example, List<Node> explanations ,Model model){
        Model res = ModelFactory.createDefaultModel();

        UndirectedGraph<String,RelationshipEdge> graph =
                new Pseudograph<String, RelationshipEdge>(new ClassBasedEdgeFactory<String, RelationshipEdge>(RelationshipEdge.class));

        for (StmtIterator itr = model.listStatements(); itr.hasNext();){
            Statement stmt = itr.next();
            graph.addVertex(stmt.getSubject().toString());
            graph.addVertex(stmt.getObject().toString());
            graph.addEdge(stmt.getSubject().toString(),stmt.getObject().toString(),
                    new RelationshipEdge<String>(stmt.getSubject().toString(),stmt.getObject().toString(),stmt));
        }


        KShortestPaths<String,RelationshipEdge> kshort = new KShortestPaths<String, RelationshipEdge>(graph,
                example.toString(),1);

        for(Node e : explanations){
            List<GraphPath<String,RelationshipEdge>>path = kshort.getPaths(e.isLiteral()?e.getLiteral().toString():e.toString());
            for(RelationshipEdge edge : path.get(0).getEdgeList()){
                res.add(edge.GetNode());
            }
        }
        return res;

    }
    public static DirectedGraph<String,DefaultEdge> makeDirectedGraph(Set<TriplePath> pattern, boolean b){
        DirectedGraph<String,DefaultEdge> graph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        for (TriplePath triple : pattern){

            graph.addVertex(triple.getSubject().toString());
            graph.addVertex(triple.getObject().toString());

            graph.addEdge(triple.getSubject().toString(),triple.getObject().toString());
        }
        return graph;
    }
    public static boolean testConnectivityAndNoCycles( DirectedGraph<String,DefaultEdge> graph){

        ConnectivityInspector<String,DefaultEdge> CI = new ConnectivityInspector<String, DefaultEdge>(graph);
        if(!CI.isGraphConnected())
            return false;

        CycleDetector<String,DefaultEdge> cd = new CycleDetector<>(graph);
        return !cd.detectCycles();
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

/***
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
            Filterable p = createMergedPattern(matching);
            if(p!=null && testBasicPatten(p)){
                return p;
            }

        }
        return null;





    }
***/
    static private Op flush(BasicPattern bp, Op op)
    {
        if ( bp == null || bp.isEmpty() )
            return op ;

        OpBGP opBGP = new OpBGP(bp) ;
        op = OpSequence.create(op, opBGP) ;
        return op ;
    }

    public static Triple varAllocTriple(Triple t){
        return new Triple(
                t.getSubject().isVariable()?Var.alloc(t.getSubject().getName()):t.getSubject(),
                t.getPredicate(),
                t.getObject().isVariable()?Var.alloc(t.getObject().getName()):t.getObject());

    }
    /** Convert any paths of exactly one predicate to a triple pattern */
    public static Op queryBuilder(Filterable filt, Model model)
    {
        BasicPattern bp = null ;
        Op op = null ;

        for ( TriplePath tp : filt.getPattern() )
        {
            if ( tp.isTriple() )
            {
                if ( bp == null )
                    bp = new BasicPattern() ;
                bp.add(varAllocTriple(tp.asTriple())) ;
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
        ExprList elst = filt.getExpList(model);
        op = OpFilter.filter(elst,op);

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
