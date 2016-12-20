package tau.cs.db.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_Variable;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.util.FileManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.Iterator;

/**
 * Created by efrat on 22/11/16.
 */
public class RDF {

    public static BasicPattern model2Basicpattern(Model model) {
        BasicPattern result=new BasicPattern();;
        Iterator<Statement> stItr = model.listStatements();

        while (stItr.hasNext()) {
            Statement stmnt = stItr.next();
            result.add(stmnt.asTriple());


        }
        return result;
    }

    public static BasicPattern file2BasicpatternWithVars(String filePath) {

        BasicPattern result = new BasicPattern();
        try {
            for (String line : Files.readLines(new File(filePath),Charsets.UTF_8)) {
                if(line.startsWith("@"))
                    continue;
                String[] params = line.split("\\s");
                if(params.length<3)
                    continue;
                Node[] nodes = new Node[3];

                for(int i =0;i<nodes.length ;i++) {

                    if (params[i].trim().startsWith("?")) {
                        nodes[i]=new Node_Variable(params[i].trim().substring(1)) {
                        };
                    } else {

                        if(params[i].trim().startsWith("<")){
                            nodes[i] = NodeFactory.createURI(params[i].trim().substring(1,params[i].trim().length()-1));

                        }else {
                            if(params[i].compareTo("rdf:type")==0) {
                                nodes[i] = NodeFactory.createLiteral(params[i].trim());
                            }
                        }
                    }

                }
                result.add(new Triple(nodes[0],nodes[1],nodes[2]));

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    public static Model loadModel(String filePath)
    {
        Model model = ModelFactory.createDefaultModel();
        // Use the FileManager to find the input file
        InputStream in = FileManager.get().open(filePath);

        if (in == null)
            throw new IllegalArgumentException("File: "+filePath+" not found");

        // Read the RDF/XML file
        model.read(filePath);

        return model;
    }

    public static Model loadModel(String modelString, String format)
    {

        File file = new File("temp."+format);
        try {
            Files.write(modelString, file, Charsets.UTF_8);
            return loadModel("temp."+format);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Creates a binding from json file
     * @param filePath
     * @return
     */
    public static Binding LoadBinding(String filePath)
    {
        JSONParser parser = new JSONParser();
        Binding binding = null;
        try {

            Object obj = parser.parse(new FileReader(filePath));

            JSONObject jsonObject = (JSONObject) obj;

            Var var = Var.alloc((String) jsonObject.get("var"));
            Node node = NodeFactory.createURI((String)jsonObject.get("URI"));
            binding= BindingFactory.binding(var,node);
            return binding;



        }
        catch(FileNotFoundException ex){
            ex.printStackTrace();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
        catch(ParseException ex){
            ex.printStackTrace();
        }

        return null;

    }





}
