package tau.cs.db;

import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tau.cs.db.qbp.QbpExplanation;
import tau.cs.db.qbp.QbpLearner;
import tau.cs.db.qbp.QbpPattern;
import tau.cs.db.utils.Experiment;
import tau.cs.db.utils.NoExampleException;
import tau.cs.db.utils.QE;
import tau.cs.db.utils.RDF;

import java.io.File;
import java.io.FileNotFoundException;
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

    public static  void RunExperiment(Model model,String ontName,String queryName,int k,boolean chooseExamples,boolean prov) throws Exception {


        Logger logger = LoggerFactory.getLogger(App.class);

        if(chooseExamples) {

            try {
                Experiment.CreateExperiment(ontName, queryName, model, k);

            } catch (NoExampleException ex) {
                logger.info("Not examples");

                throw ex;
            }
        }
        if(prov) {
            Experiment.CreateSets(ontName,queryName,model,k);
        }
        List<QbpExplanation> res = Experiment.LoadSets(ontName,queryName);
//        TreeMap<QbpPattern,Set<QbpExplanation>> matching = QbpLearner.ComputeMatching(res);
        // List<Query> q = QbpLearner.LearnQuery(res,3);

        List<Integer> weights = new ArrayList<>();

        weights.add(5);


        // weights.add(90);
        for(int w : weights){
            new File(String.format("./files/%s/examples/%s/weights/%d/",ontName, queryName,w)).mkdirs();
            QbpLearner.W1=w;
            List<Query> q = QbpLearner.LearnQuery(res,5);
            int j=0;
            for(Query query: q){
                j++;
                File qf = new File(String.format("./files/%s/examples/%s/weights/%d/query%d.sparql", ontName,queryName,w,j));
                if(!qf.exists()){
                    try{
                        qf.createNewFile();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try(FileWriter fileWriter = new FileWriter(qf)) {
                    fileWriter.write(query.toString());
                    logger.info(String.format("weight %d", w));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    public static void main( String[] args ) throws Exception {

        Logger logger = LoggerFactory.getLogger(App.class);

        String ontName = "bsbm";


        Model model = RDF.loadModel(String.format("/home/efrat/Documents/SQBE/files/%s/ontology/dataset.ttl", ontName));
        File f = new File(String.format("/home/efrat/Documents/SQBE/files/%s/examples", ontName)); // current directory
        logger.info("loaded model");
        File[] files = f.listFiles();
        for (File file : files) {
            try {
                logger.info(file.getName());
                RunExperiment(model, ontName, file.getName(), 2,true,true);
            }catch(NoExampleException ex){
                File qf = new File(String.format("./files/%s/examples/%s/NoExamples", ontName,file.getName()));
                if(!qf.exists()){
                    try{
                        qf.createNewFile();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
            catch(Exception e){
                e.printStackTrace();
            }
        }


        logger.info("hi");


    }
}
