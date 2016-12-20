package tau.cs.db.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.engine.binding.Binding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by efrat on 12/12/16.
 */
public class QE {

    static String queryPath = "./files/queries/%s.sparql";
    static String testQueryPath = "./files/examples/%s/queries/%s.sparql";

    public static Query TestQueryFromFile(String queryName,String testQuery){
        String content = null;
        String filePath = String.format(testQueryPath,queryName,testQuery);
        try {
            content = Files.toString(new File(filePath), Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Query query = QueryFactory.create(content);
        return query;
    }

    public static Query QueryFromFile(String queryName){
        String content = null;
        String filePath = String.format(queryPath,queryName);
        try {
            content = Files.toString(new File(filePath), Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Query query = QueryFactory.create(content);
        return query;
    }

    public static Pair<List<Node>,String> KRandomExamples(String queryName, Model model, int k){

        Query query = QueryFromFile(queryName);
        QueryExecution qExec = QueryExecutionFactory.create(query, model);

        ResultSet rset = qExec.execSelect();
        List<Node> result = new ArrayList<>();
        while(rset.hasNext()){
            Binding sln = rset.nextBinding();
            if(Math.random()>0.5){
                result.add(sln.get(query.getProjectVars().get(0)));
            }
            if(result.size()==k){
                break;
            }
        }

        return new Pair<List<Node>,String>(result,query.getResultVars().get(0));

    }
}
