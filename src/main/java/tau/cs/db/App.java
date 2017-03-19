package tau.cs.db;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;


import org.apache.log4j.*;
import org.apache.log4j.spi.LoggerFactory;
import tau.cs.db.prov.ProvenanceGenerator;
import tau.cs.db.qbp.QBPHandler;
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
    public static  void RunExperiment2(Model model,String ontName,String queryName,int k,boolean chooseExamples,boolean prov) throws Exception {


        if(chooseExamples) {

            try {
                Experiment.CreateExperiment(ontName, queryName, model, k);

            } catch (NoExampleException ex) {


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

        Logger logger = Logger.getLogger("QueryBuilder");

        // weights.add(90);
        for(int w : weights){
            new File(String.format("./files/%s/examples/%s/resultQueries/",ontName, queryName)).mkdirs();
            QbpLearner.W1=w;
            FileAppender appender = new FileAppender();
            appender.setName(String.format(queryName));
            appender.setLayout(new PatternLayout("[%c{1}] %m%n"));
            appender.setFile(String.format("./files/%s/examples/%s/build.log", ontName,queryName));
            appender.setAppend(true);
            appender.setThreshold(Level.INFO);
            appender.activateOptions();
            logger.addAppender(appender);
            List<Query> queryList = QbpLearner.LearnQuery(res,4,model);
            for(int i=0; i<queryList.size();i++){
                logger.info(String.format("Query %d : %s",i,queryList.get(i).toString()));
            }

            while(queryList.size()>1) {
                Pair<Model, Node> pair = ProvenanceGenerator.CreateDiffModel(queryList.get(0), queryList.get(1), model);
                if (pair != null) {
                    logger.info(String.format("Diff between query 0 and 1 %s", pair.getLeft().toString()));
                    if (Experiment.CheckDiffExample(ontName, queryName, pair.getLeft().asRDFNode(pair.getRight()), pair.getLeft())) {
                        logger.info(String.format("Query %s win", queryList.get(0).toString()));
                        queryList.remove(1);
                    } else {
                        logger.info(String.format("Query %s removed", queryList.get(0).toString()));
                        queryList.remove(0);
                    }
                } else {
                    pair = ProvenanceGenerator.CreateDiffModel(queryList.get(1), queryList.get(0), model);
                    if(pair!=null) {
                        if (Experiment.CheckDiffExample(ontName, queryName, pair.getLeft().asRDFNode(pair.getRight()), pair.getLeft())) {
                            logger.info(String.format("Query %s win", queryList.get(1).toString()));
                            queryList.remove(0);
                        } else {
                            logger.info(String.format("Query %s removed", queryList.get(1).toString()));
                            queryList.remove(1);
                        }
                    }
                    else{
                        logger.info(String.format("No diff between ermoving arbitrary"));
                        Query q = queryList.remove(0);
                    }
                }
            }


            logger.info(String.format("Winning Query %s ", queryList.get(0).toString()));

            logger.removeAllAppenders();
            int j=0;
            for(Query query: queryList){
                j++;


                File qf = new File(String.format("./files/%s/examples/%s/resultQueries/query%d.sparql", ontName,queryName,j));
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
    public static  void RunExperiment(Model model,String ontName,String queryName,int k,boolean chooseExamples,boolean prov) throws Exception {


        if(chooseExamples) {

            try {
                Experiment.CreateExperiment(ontName, queryName, model, k);

            } catch (NoExampleException ex) {


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

        Logger logger = Logger.getLogger("QueryBuilder");

        // weights.add(90);
        for(int w : weights){
            new File(String.format("./files/%s/examples/%s/resultQueries/",ontName, queryName)).mkdirs();
            QbpLearner.W1=w;
            FileAppender appender = new FileAppender();
            appender.setName(String.format(queryName));
            appender.setLayout(new PatternLayout("[%c{1}] %m%n"));
            appender.setFile(String.format("./files/%s/examples/%s/build.log", ontName,queryName));
            appender.setAppend(true);
            appender.setThreshold(Level.INFO);
            appender.activateOptions();
            logger.addAppender(appender);
            List<Query> q = QbpLearner.LearnQuery(res,4,model);
            logger.removeAllAppenders();
            int j=0;
            for(Query query: q){
                j++;


                File qf = new File(String.format("./files/%s/examples/%s/resultQueries/query%d.sparql", ontName,queryName,j));
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


//        System.setProperty("log4j.configurationFile",);
        PropertyConfigurator.configure("configuration.xml");
        Logger logger = Logger.getRootLogger();

        String ontName = "ont";


        Model model = RDF.loadModel(String.format("/home/efrat/Documents/SQBE/files/%s/ontology/dataset.ttl", ontName));
        File f = new File(String.format("/home/efrat/Documents/SQBE/files/%s/examples", ontName)); // current directory
        logger.info("loaded model");
        File[] files = f.listFiles();
        for (File file : files) {
            try {
                logger.info(file.getName());
                RunExperiment2(model, ontName, file.getName(), 4,false,false);
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
