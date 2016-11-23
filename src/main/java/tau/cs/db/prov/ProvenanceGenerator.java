package tau.cs.db.prov;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
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

    public static Model ExecuteProvenanceQuery(Query provQuery, Model model){


        QueryExecution qExec = QueryExecutionFactory.create(provQuery, model);

        ResultSet rset = qExec.execSelect();

        ArrayList<ArrayList<Triple>> whyProv = new ArrayList<ArrayList<Triple>>();
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        pss.setCommandText(provQuery.toString());



        while(rset.hasNext()) {

            QuerySolution sln = rset.next();

            Iterator<String> itr = sln.varNames();
            for(;itr.hasNext();)
            {
                String varName = itr.next();
                RDFNode node = sln.get(varName);
                if(node.isResource()){
                    pss.setIri(varName,sln.getResource(varName).toString());}
                if(node.isLiteral()){
                    pss.setLiteral(varName,sln.getLiteral(varName));
                }
            }


        }



        String stringModel = pss.toString();


        //get all parameters
        String pattern  = "\\{(.*?)FILTER";
        Pattern r = Pattern.compile(pattern,Pattern.DOTALL);
        Matcher m = r.matcher(stringModel);
        if(m.find()) {
            stringModel = m.group(1);
        }
        StringBuilder stringModelBuilder = new StringBuilder(stringModel);

        stringModelBuilder.insert(stringModelBuilder.lastIndexOf("\n"),".");

        stringModel = stringModelBuilder.toString();

        Model provModel = ModelFactory.createDefaultModel();

 //       provModel.read(new ByteArrayInputStream(stringModel.getBytes()), "TURTLE");
        //provModel.read(is,"TURTLE");

        return RDF.LoadModel(stringModel,"ttl");

    }

}
