package tau.cs.db;

import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tau.cs.db.qbp.QbpExplanation;
import tau.cs.db.qbp.QbpLearner;
import tau.cs.db.qbp.QbpPattern;
import tau.cs.db.utils.Experiment;
import tau.cs.db.utils.RDF;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


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

    public static  void RunExperiment(Model model,String queryName,int k){

        Experiment.CreateExperiment(queryName,model,k);
//       Experiment.CreateSets("q8",model);
        List<QbpExplanation> res = Experiment.LoadSets(queryName);
//        TreeMap<QbpPattern,Set<QbpExplanation>> matching = QbpLearner.ComputeMatching(res);
        Query q = QbpLearner.unifyBest(res);
        File provFile = new File(String.format("./files/examples/%s/query.txt", queryName));
        if(!provFile.exists()){
            try{
                provFile.createNewFile();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try(FileWriter fileWriter = new FileWriter(provFile)) {
            fileWriter.write(q.toString());


        } catch (IOException e) {
            e.printStackTrace();
        }
//

//
//        try(FileWriter fileWriter = new FileWriter(provFile)) {
//            for(Map.Entry<QbpPattern,Set<QbpExplanation>> e: matching.entrySet()){
//                fileWriter.write(e.getKey().toString());
//                fileWriter.write(String.format("\nIR: %f\n",e.getKey().GetIR()));
//                fileWriter.write("\n\n---------------------------\n");
//                for(QbpExplanation exp : e.getValue()){
//                    fileWriter.write(exp.toString());
//                    fileWriter.write("\n\n\n");
//                }
//                fileWriter.write("\n\n\n#################################################\n\n\n");
//            }
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//        File resultsFile = new File(String.format("./files/examples/%s/resulst.txt", queryName));
//        if(!resultsFile.exists()){
//            try{
//                resultsFile.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//
//        try(FileWriter fileWriter = new FileWriter(resultsFile)) {
//
//            for(QbpPattern patt : matching.keySet()){
//                fileWriter.write(patt.toString());
//                fileWriter.write(String.format("\n\nIR %f\n\n", patt.GetIR()));
//
//                fileWriter.write("\n\n\n#################################################\n\n\n");
//            }
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

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
//

        RunExperiment(model,"q8",8);

//
//        Experiment.CreateExperiment("q6",model,20);
//        List<QbpExplanation> res = Experiment.LoadSets("q6");
//        TreeMap<QbpPattern,Set<QbpExplanation>>  matching = QbpLearner.ComputeMatching(res);
//
//        File provFile = new File("./files/examples/q6/run.txt");
//        if(!provFile.exists()){
//            try{
//                provFile.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//
//        try(FileWriter fileWriter = new FileWriter(provFile)) {
//            for(Map.Entry<QbpPattern,Set<QbpExplanation>> e: matching.entrySet()){
//                fileWriter.write(e.getKey().toString());
//                fileWriter.write(String.format("\nIR: %f\n",e.getKey().GetIR()));
//                fileWriter.write("\n\n---------------------------\n");
//                for(QbpExplanation exp : e.getValue()){
//                    fileWriter.write(exp.toString());
//                    fileWriter.write("\n\n\n");
//                }
//                fileWriter.write("\n\n\n#################################################\n\n\n");
//            }
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        logger.info("hi");

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
