package tau.cs.db.qbp;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import tau.cs.db.prov.ProvenanceGenerator;
import tau.cs.db.utils.RDF;


import java.io.InputStream;
import java.util.*;

/**
 * Created by efrat on 04/01/17.
 */
public class QBPHandler {
    Model model;
    List<Node> allNodes;
    List<Statement> allStatements;

    Map<String, Integer> node2Num;
    Map<Statement, Integer> statement2Num;
    List<QbpExplanation> explanations;

//    public static Model loadModel(String filePath)
//    {
//        Model model = ModelFactory.createDefaultModel();
//        // Use the FileManager to find the input file
//        InputStream in = FileManager.get().open(filePath);
//
//        if (in == null)
//            throw new IllegalArgumentException("File: "+filePath+" not found");
//
//        // Read the RDF/XML file
//        model.read(filePath);
//
//        return model;
//    }
    public QBPHandler(String path) {
    	
        this.model = RDF.loadModel(path);
        this.allNodes = this.loadAllNodes();
        this.allStatements = new ArrayList<>();
        this.handleStatements();
        this.node2Num = new HashMap<>();
        for(int i=0; i< this.allNodes.size();i++){
            this.node2Num.put(this.allNodes.get(i).isLiteral()?
                    this.allNodes.get(i).getLiteral().toString():this.allNodes.get(i).getLocalName() ,i);
        }
        this.explanations = new ArrayList<>();
    }


    public void AddExplanation(int nodeNum, List<Integer> chosenNodes,
                               List<Integer> statments){
//        List<Node> e
//
//
//
// xps = new ArrayList<>();
//        chosenNodes.forEach((n)->exps.add(this.allNodes.get(n)));

        Model prov = ModelFactory.createDefaultModel();//utils.ConnectAllComponents(this.allNodes.get(nodeNum),exps,this.model);
        for(Integer i : statments){
            prov.add(this.allStatements.get(i));
        }
        if(prov!=null)
            this.explanations.add(new QbpExplanation(this.allNodes.get(nodeNum),prov));


    }

    public String GetQuery() throws InvalidPropertiesFormatException {

        List<Query> q = QbpLearner.LearnQuery(this.explanations,5);
        this.explanations.clear();
        return q.get(0).toString();

    }
    public JSONArray GetProvenanceGraph(int nodeNum){
        Model model = ProvenanceGenerator.getNodeEnviornment( this.model.getRDFNode(this.allNodes.get(nodeNum)),this.model);
        Set<Node> allNodes = new HashSet<>();
        for (final ResIterator it = model.listSubjects(); it.hasNext();) {
            allNodes.add(it.next().asNode());
        }
        for (final NodeIterator it = model.listObjects(); it.hasNext();) {
            allNodes.add(it.next().asNode());

        }

        JSONArray jo = new JSONArray();
        for(Node node : allNodes){
            JSONObject temp = new JSONObject();
            temp.put("label",node.isLiteral() || node.isVariable()?node.toString():node.getLocalName());
            temp.put("id",node.isLiteral()?this.node2Num.get(node.getLiteral().toString()   ):this.node2Num.get(node.getLocalName()));
            jo.add(temp);
        }

        JSONArray ja = new JSONArray();
        for(StmtIterator itr = model.listStatements(); itr.hasNext();){
            Statement stm = itr.nextStatement();
            JSONObject lk = new JSONObject();
            lk.put("id",this.allStatements.indexOf(stm));
            lk.put("source",this.node2Num.get(stm.getSubject().getLocalName()));
                                                                    lk.put("label",stm.getPredicate().getLocalName());
            if(stm.getObject().isLiteral()) {
                lk.put("target", this.node2Num.get(stm.getObject().toString()));
            }else{
                lk.put("target",this.node2Num.get(stm.getObject().asResource().getLocalName()));
            }
            if(lk.get("target") != null)
                ja.add(lk);
        }

        JSONObject prop = new JSONObject();
        prop.put("name", this.allNodes.get(nodeNum).toString());
        prop.put("num",nodeNum);

        JSONArray result = new JSONArray();
        result.add(jo);
        result.add(ja);
        result.add(prop);
        return result;

    }
    public List<Pair<String,Integer>> FindNodes(String prefix){

        List<Pair<String,Integer>> foundNodes = new ArrayList<>();
        for(int i=0; i<this.allNodes.size();i++){
            if(this.allNodes.get(i).toString().contains(prefix)){
                foundNodes.add(new Pair<String,Integer>(this.allNodes.get(i).isLiteral() || this.allNodes.get(i).isBlank()?this.allNodes.get(i).toString():
                        this.allNodes.get(i).getLocalName(),i));
            }
        }


        return foundNodes;
    }
    private void handleStatements() {
        Set<Node> allNodes = new HashSet<>();
        for (final StmtIterator it = this.model.listStatements(); it.hasNext();) {
            Statement stmt = it.next();
            this.allStatements.add(stmt);

        }

    }
    private List<Node> loadAllNodes() {
        Set<Node> allNodes = new HashSet<>();
        for (final ResIterator it = this.model.listSubjects(); it.hasNext();) {
            allNodes.add(it.next().asNode());
        }
        for (final NodeIterator it = this.model.listObjects(); it.hasNext();) {
            allNodes.add(it.next().asNode());

        }

        List<Node> nodes = new ArrayList<>();
        nodes.addAll(allNodes);
        return nodes;
    }
}
