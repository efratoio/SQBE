package tau.cs.db;

import tau.cs.db.qbp.utils;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.util.NodeIsomorphismMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tau.cs.db.utils.BBLearner;
import tau.cs.db.utils.Experiment;
import tau.cs.db.utils.RDF;
import tau.cs.db.utils.sparqlbyeLearner;

import java.util.Collection;
import java.util.Iterator;


/**
 * Hello world!
 *
 */
public class App 
{
    /**
     * Loads the model from filePath
     * @param filePath must end with correct suffix
     * @return the model loaded
     */


    public static void main( String[] args )
    {

        Logger logger = LoggerFactory.getLogger(App.class);


//
//        Binding binding = RDF.loadBindin

// g("/home/efrat/Documents/SQBE/files/examples/q3a/example1.JSON");
//
//
//
        Model model = RDF.loadModel("/home/efrat/Documents/SQBE/files/ontology/sp2b.n3");

        boolean b = Experiment.CheackExample("q8",1,model);
        logger.info(String.format("%b",b));
//        Experiment.MakeExamples("q8",model,8);

//
//
//        Model model = RDF.loadModel("/home/efrat/Documents/SQBE/files/examples/q3a/mod.ttl");
//        Model pattern = RDF.loadModel("/home/efrat/Documents/SQBE/files/examples/q3a/pat.ttl");
//
//
//
//        BasicPattern p1 = new BasicPattern();
//        Iterator<Statement> stItr = model.listStatements();
//
//        while(stItr.hasNext()){
//            Statement stmnt = stItr.next();
//            p1.add(stmnt.asTriple());
//
//        }
//
//        BasicPattern p2 = new BasicPattern();
//        Iterator<Statement> stItr2 = pattern.listStatements();
//
//        while(stItr2.hasNext()){
//            Statement stmnt = stItr2.next();
//            p2.add(stmnt.asTriple());
//
//        }
//
//        BasicPattern m = tau.cs.db.qbp.utils.FindBestMerge(p1,p2);
//
//      //  logger.info(lables.toString());
//
//
//
////        String content = null;
////        try {
////            content = Files.toString(new File("/home/efrat/Documents/SQBE/files/queries/q3a.sparql"), Charsets.UTF_8);
////        } catch (IOException e) {
////            e.printStackTrace();
////            return;
////        }
////
////        Query query = QueryFactory.create(content);
////
////
////
////        Query provQuery = ProvenanceGenerator.CreateProvenanceQuery(binding,query);
////
////        logger.info(provQuery.toString());
////
////        Model provModel = ProvenanceGenerator.ExecuteProvenanceQuery(provQuery,mode
//        BBLearner learner = new sparqlbyeLearner(model,"/home/efrat/Documents/SQBE/files/examples/q3a/results.JSON");
//        Collection<Query> queries = learner.learnQueries();
//
//        for(Query q : queries){
//            logger.info(q.toString());
//        }


    }
}
