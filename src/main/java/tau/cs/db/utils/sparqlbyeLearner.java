package tau.cs.db.utils;

import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.util.FileManager;
import uk.ac.ox.cs.sparqlbye.core.AOTree;
import uk.ac.ox.cs.sparqlbye.core.LearnerDirector;
import uk.ac.ox.cs.sparqlbye.core.UtilsJena;
import uk.ac.ox.cs.sparqlbye.core.UtilsLearner;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by efrat on 28/11/16.
 */
public class sparqlbyeLearner extends BBLearner {
    private LearnerDirector learnerDirector;

    public sparqlbyeLearner(Model model, Collection<QuerySolution> positiveExamples) {
        super(model);
        this.learnerDirector = new LearnerDirector(positiveExamples,null,null,this.queryToQueryExecution);


    }


    public sparqlbyeLearner(Model model, String filePath) {
        super(model);
        InputStream in = FileManager.get().open(filePath);
        ResultSet results = ResultSetFactory.fromJSON(in);
        Collection<QuerySolution> pos = new ArrayList<QuerySolution>();
        while(results.hasNext()){
            pos.add(results.next());

        }
        Collection<QuerySolution> nSols = new ArrayList<QuerySolution>();
        List<String> badUris = new ArrayList<String>();
        this.learnerDirector = new LearnerDirector(pos,nSols,badUris,this.queryToQueryExecution);


    }
    public UtilsLearner.URevengResponse learn(){
        return learnerDirector.learn();
    }

    public Collection<Query> getQueries(UtilsLearner.URevengResponse response){
        AOTree learnedTree = response.getOptLearnedTree().get();
        Op learnedOp = UtilsJena.convertAOFTreeToOp(learnedTree);
        Query learnedQuery = OpAsQuery.asQuery(learnedOp);

        Collection<Query> res = new ArrayDeque<Query>();
        res.add(learnedQuery);
        return res;






    }

    @Override
    public Collection<Query> learnQueries() {
        return getQueries(learn());
    }
}
