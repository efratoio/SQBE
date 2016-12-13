package tau.cs.db.utils;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import tau.cs.db.prov.ProvenanceGenerator;

import java.util.List;

/**
 * Created by efrat on 12/12/16.
 */
public class Experiment {


    public static void MakeExamples(String queryName, Model model, int k){
            Pair<List<Node>,String> kResults = QE.KRandomExamples(queryName,model,k);
            JS.WriteExamplesJson(queryName,kResults.getRight(),kResults.getLeft());
            JS.formatJsonToResultSet("q8");

    }


    public static boolean CheackExample(String queryName, int exampleNum,Model model){
            Query query = QE.QueryFromFile(queryName);
            Query askQuery = ProvenanceGenerator.CreatAskQuery(query);
            QuerySolution binding = JS.JS2ResultSet(queryName,exampleNum).next();
            return ProvenanceGenerator.ExecuteAskQuery(askQuery,model,binding);

    }

    public static 
}
