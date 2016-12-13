package tau.cs.db.utils;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;

import java.util.Collection;
import java.util.function.Function;

/**
 * Created by efrat on 28/11/16.
 */
public abstract class BBLearner {
    protected static Function<Query, QueryExecution> queryToQueryExecution;

    public BBLearner(Model model){
        queryToQueryExecution =  (query) ->
                QueryExecutionFactory.create(query,model);

    }

    public abstract Collection<Query> learnQueries();
}
