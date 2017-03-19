package tau.cs.db.prov;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.*;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBase;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.sse.lang.SSE_Parser;
import tau.cs.db.utils.RDF;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by efrat on 21/11/16.
 */
public class ProvenanceGenerator {



    public static  Model getNodeEnviornment(RDFNode node, Model model){


        String qString = String.format("CONSTRUCT  WHERE {?a ?b ?c . }");
        Query pQuery = QueryFactory.create(qString);

        pQuery.setQueryConstructType();

        QuerySolutionMap initialBinding = new QuerySolutionMap();
        initialBinding.add("a", node);
        QueryExecution qexec = QueryExecutionFactory.create(pQuery, model,initialBinding);

        Model constructModel = qexec.execConstruct();

        qexec.close() ;
//        String qString2 = String.format("CONSTRUCT WHERE {?a ?b ?c . ?c ?d ?e .}");
//        Query pQuery2 = QueryFactory.create(qString);

//        initialBinding = new QuerySolutionMap();
//        initialBinding.add("c", node);
//
//        qexec = QueryExecutionFactory.create(pQuery, model, initialBinding);
//
//        constructModel.add(qexec.execConstruct());
//        qexec.close() ;
//
//        qString = String.format("CONSTRUCT  WHERE {?a ?b ?c . ?c ?d ?e .}");
//        pQuery = QueryFactory.create(qString);
//
//        initialBinding = new QuerySolutionMap();
//        initialBinding.add("a", node);
//        qexec = QueryExecutionFactory.create(pQuery, model,initialBinding);
//
//        constructModel.add(qexec.execConstruct());
//        qexec.close() ;
//
//        initialBinding = new QuerySolutionMap();
//        initialBinding.add("e", node);
//
//        qexec = QueryExecutionFactory.create(pQuery, model, initialBinding);
//
//        constructModel.add(qexec.execConstruct());
//        qexec.close() ;
//
//        qString = String.format("CONSTRUCT  WHERE {?a ?b ?c . ?c ?d ?e . ?e ?f ?g .}");
//        pQuery = QueryFactory.create(qString);
//
//        initialBinding = new QuerySolutionMap();
//        initialBinding.add("a", node);
//        qexec = QueryExecutionFactory.create(pQuery, model,initialBinding);
//
//        constructModel.add(qexec.execConstruct());
//        qexec.close() ;
//
//        initialBinding = new QuerySolutionMap();
//        initialBinding.add("g", node);
//
//        qexec = QueryExecutionFactory.create(pQuery, model, initialBinding);
//
//        constructModel.add(qexec.execConstruct());
//        qexec.close() ;

        return model;


    }

    /**
     *
     * @param binding the query result we want to get the provenance
     * @param query the query
     * @return a query that will return why provenance
     */
    public static Query CreateProvenanceQuery(Binding binding, Query query){


        Query pQuery = QueryFactory.create("SELECT * {<s> <p> <o>}");


        pQuery.setQueryPattern(query.getQueryPattern());
        pQuery.setQuerySelectType();
        pQuery.setResultVars();

        //variable bindings
        List<Var> vars = new ArrayList<Var>();
        Iterator<Var> varIt = binding.vars();
        while(varIt.hasNext()) {
            vars.add(varIt.next());

        }
        List<Binding> bindings = new ArrayList<Binding>();
        bindings.add(binding);
        pQuery.setValuesDataBlock(vars, bindings);



        return pQuery;
    }

    public static Triple GetBinded(Triple t, Binding bind){
        Node sub=null;
        Node predicate = null;
        Node obj = null;
        if(t.getSubject().isVariable()){
            Var v = Var.alloc(t.getSubject());
            sub = bind.get(v);
        }else{
            sub = t.getSubject();
        }
        if(t.getPredicate().isVariable()){
            Var v = Var.alloc(t.getPredicate());
            predicate = bind.get(v);
        }else{
            predicate = t.getPredicate();
        }
        if(t.getObject().isVariable()){
            Var v = Var.alloc(t.getObject());
            obj = bind.get(v);
        }else{
            obj = t.getObject();
        }
        if(sub == null || predicate== null || obj==null){
            return null;
        }
        return new Triple(sub,predicate,obj);
    }

    public static OpBGP GetProv(Op alg, Binding bind, OpBGP result) throws Exception {
        if(alg instanceof  Op1) {

            Binding newBind = BindingFactory.binding(bind);
            if(alg instanceof OpFilter){
                for(Expr expr : ((OpFilter) alg).getExprs()){
                    if(expr instanceof E_Equals){
                        Var v=null;
                        Node n = null;
                        for(int i=1;i<=2; i++) {
                            if (((E_Equals) expr).getArg(i).isVariable()) {
                                v = ((E_Equals) expr).getArg(i).getExprVar().asVar();
                            }
                            if (((E_Equals) expr).getArg(i).isConstant()) {
                                n = ((E_Equals) expr).getArg(i).getConstant().asNode();
                            }
                        }
                        if(v!=null && n!=null) {
                            newBind = BindingFactory.binding(newBind,v,n);
                        }


                    }
                }
            }
            OpBGP res = GetProv(((Op1) alg).getSubOp(),newBind,result);
            return res;
        }
        if(alg instanceof OpUnion ) {
            OpBGP copyRes = (OpBGP)result.copy();
            OpBGP res1 = GetProv(((Op2)alg).getLeft(),bind,copyRes);
            if(res1!=null)
                return res1;
            OpBGP res2 = GetProv(((Op2)alg).getRight(),bind,result);
            return res2;
        }

        if(alg instanceof Op2 ) {

            GetProv(((Op2)alg).getLeft(),bind,result);
            OpBGP res2 = GetProv(((Op2)alg).getRight(),bind,result);
            return res2;
        }
        if(alg instanceof Op0){

            if(alg instanceof OpBGP) {
                for(Triple t: ((OpBGP) alg).getPattern()){
                    Triple newT = GetBinded(t,bind);
                    if(newT!=null){
                        result.getPattern().add(newT);
                    }else{
                        return null;
                    }
                }
                return result;
            }
            if(alg instanceof OpTable ){
                return result;
            }
            throw new Exception("Op algebra unknown");
        }
        assert false;
        return null;
    }


    public static Statement CreateStatement(Model model,Triple t){
        Resource res = model.createResource(t.getSubject().getURI());

        Property pr = model.createProperty(t.getPredicate().getURI());
        if(t.getObject().isLiteral()){
           Literal l= model.createTypedLiteral(t.getObject().getLiteral().getValue(),t.getObject().getLiteralDatatype().getURI());
            return model.createStatement(res,pr,l);
        }
        if(t.getObject().isURI()){

            return model.createStatement(res,pr,model.asRDFNode(t.getObject()));
        }

        assert false;
        return null;

    }
    public static Model ExecuteProvenanceQuery(Query provQuery, Model model) throws Exception {


        QueryExecution qExec = QueryExecutionFactory.create(provQuery, model);

        ResultSet rset = qExec.execSelect();

        ArrayList<ArrayList<Triple>> whyProv = new ArrayList<ArrayList<Triple>>();
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        pss.setCommandText(provQuery.toString());


        OpBGP res = new OpBGP();

        if(rset.hasNext()) {

            Binding sln = rset.nextBinding();

            Op op = Algebra.compile(provQuery);
            GetProv(op,sln,res);

        }

        Model provModel = ModelFactory.createDefaultModel();
        provModel.setNsPrefixes(model.getNsPrefixMap());
        for(Triple t: res.getPattern()){
            Statement stmt =  CreateStatement(provModel,t);
            provModel.add(stmt);
        }
        return provModel;

    }

    public static boolean ExecuteAskQuery(Query askQuery, Model model,QuerySolution binding) {


        QuerySolutionMap qsm = new QuerySolutionMap();
        qsm.add(binding.varNames().next(),binding.get(binding.varNames().next()));
        QueryExecution qExec = QueryExecutionFactory.create(askQuery, model,qsm);


        return qExec.execAsk();


    }
    public static Query CreatAskQuery(Query query,QuerySolution binding){


        Query askQuery = QueryFactory.create("ASK {<s> <p> <o>}");

        askQuery.setQueryPattern(query.getQueryPattern());
        askQuery.setQueryAskType();


        return askQuery;
    }
//    private static Query replaceWithVar(Query q){
//        for()
//    }
    public static Pair<Model,Node> CreateDiffModel(Query q1, Query q2, Model model) throws Exception {

        Op ominus = OpMinus.create((Op)Algebra.compile(q1),(Op)Algebra.compile(q2));

        QueryIterator qIter = Algebra.exec(ominus,model);
        Binding binding=null;
        Node node = null;
        for ( ; qIter.hasNext() ; )
        {
            binding = qIter.nextBinding() ;
            node  = binding.get(q1.getProjectVars().get(0));
        }
        qIter.close() ;
        if(binding == null)
            return null;

        Query q = CreateProvenanceQuery(binding,q1);

        return new Pair<Model,Node>(ExecuteProvenanceQuery(q,model),node);
    }

}
