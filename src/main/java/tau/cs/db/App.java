package tau.cs.db;


import com.google.common.io.Files;
import org.apache.jena.ext.com.google.common.base.Charsets;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.engine.binding.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tau.cs.db.prov.ProvenanceGenerator;
import tau.cs.db.utils.RDF;

import java.io.File;
import java.io.IOException;

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

        Binding binding = RDF.LoadBinding("/home/efrat/Documents/SQBE/files/examples/q3a/example1.json");

        Model model = RDF.LoadModel("/home/efrat/Documents/SQBE/files/ontology/sp2b.n3");
     //   Model model = RDF.LoadModel("/home/efrat/Documents/SQBE/files/ontology/mod.ttl");
        String content = null;
        try {
            content = Files.toString(new File("/home/efrat/Documents/SQBE/files/queries/q3a.sparql"), Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Query query = QueryFactory.create(content);

        Query provQuery = ProvenanceGenerator.CreateProvenanceQuery(binding,query);

        logger.info(provQuery.toString());

        Model provModel = ProvenanceGenerator.ExecuteProvenanceQuery(provQuery,model);

    }
}
