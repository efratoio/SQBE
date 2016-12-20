package tau.cs.db.utils;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tau.cs.db.App;
import tau.cs.db.prov.ProvenanceGenerator;
import tau.cs.db.qbp.QbpExplanation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by efrat on 12/12/16.
 */
public class Experiment {

    static String testQueryDir = "./files/examples/%s/queries/";
    static String resultsPath = "./files/examples/%s/results/%d.json";
    static String explanationsPath = "./files/examples/%s/explanations/%se%d/";
    static String provFilePath = "./files/examples/%s/explanations/%se%d/prov.ttl";
    static String exampleFilePath = "./files/examples/%s/explanations/%se%d/node.txt";
    static String explanationDir = "./files/examples/%s/explanations";


    public static void CreateExperiment(String queryName,Model model,int k){
        MakeExamples(queryName,model,k);
        CreateSets(queryName,model,k);
    }

    public static void MakeExamples(String queryName, Model model, int k){
        Pair<List<Node>,String> kResults = QE.KRandomExamples(queryName,model,k);
        JS.WriteExamplesJson(queryName,kResults.getRight(),kResults.getLeft());
        JS.formatJsonToResultSet(queryName);

    }


    public static boolean CheckExample(String queryName, String testQuery, int exampleNum, Model model){
        Query query = QE.TestQueryFromFile(queryName,testQuery);
        QuerySolution binding = JS.JS2ResultSet(queryName,exampleNum).next();
        Query askQuery = ProvenanceGenerator.CreatAskQuery(query,binding);
        return ProvenanceGenerator.ExecuteAskQuery(askQuery,model,binding);

    }


    public static List<QbpExplanation> LoadSets(String queryName){

        List<QbpExplanation> result = new ArrayList<>();
        File folder = new File(String.format(explanationDir,queryName));
        File[] listOfDirs = folder.listFiles();

        for(File f : listOfDirs){
            Model exp = RDF.loadModel(f.getPath()+"/prov.ttl");
            int explanationNum=0;
            String pattern  = ".*e(\\d+)";
            Pattern r = Pattern.compile(pattern,Pattern.DOTALL);
            Matcher m = r.matcher(f.getName());
            if(m.find()) {
                explanationNum = new Integer(m.group(1));
            }
            QuerySolution sln = JS.JS2ResultSet(queryName,explanationNum).next();
            RDFNode rdfNode= sln.get(sln.varNames().next());

            result.add(new QbpExplanation(rdfNode.asNode(), exp));



        }

        return result;

    }

    public static void CreateExplanation(String queryName,String testQueryName,int exampleNum, Model model){
        Logger logger = LoggerFactory.getLogger(App.class);

        String resultPath = String.format(resultsPath,queryName,exampleNum);
        if(CheckExample(queryName,testQueryName,exampleNum,model)){
            logger.info(testQueryName,exampleNum);
            ResultSet res = JS.JS2ResultSet(queryName,exampleNum);
            Query query = QE.TestQueryFromFile(queryName,testQueryName);
            Query provQuery = ProvenanceGenerator.CreateProvenanceQuery(res.nextBinding(),query);
            Model provModel = ProvenanceGenerator.ExecuteProvenanceQuery(provQuery,model);

            File expDir = new File(String.format( explanationsPath,queryName,testQueryName,exampleNum));
            if(!expDir.exists()){
                try{
                    expDir.mkdir();
                }
                catch(SecurityException se){
                    return;
                }
            }
            File provFile = new File(String.format(provFilePath,queryName,testQueryName,exampleNum));
            if(!provFile.exists()){
                try{
                    provFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            try(FileWriter fileWriter = new FileWriter(provFile)) {
                provModel.write(fileWriter,"TTL");
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }

    public static void CreateSets(String queryName,Model model, int k){
        Logger logger = LoggerFactory.getLogger(App.class);
        File folder = new File(String.format(testQueryDir,queryName));
        File[] listOfFiles = folder.listFiles();
        for(File f : listOfFiles){
            //get all parameters
            String testQueryName="";
            String pattern  = "(.*?).sparql";
            Pattern r = Pattern.compile(pattern,Pattern.DOTALL);
            Matcher m = r.matcher(f.getName());
            if(m.find()) {
                testQueryName = m.group(1);
            }


            for(int i=1;i<=k;i++) {
                CreateExplanation(queryName,testQueryName,i,model);
            }
        }

    }
}