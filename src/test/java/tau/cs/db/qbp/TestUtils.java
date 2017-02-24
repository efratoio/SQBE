package tau.cs.db.qbp;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.sparql.core.BasicPattern;
import org.junit.Before;
import tau.cs.db.utils.RDF;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by efrat on 09/01/17.
 */
public class TestUtils {


    private static String patternTest = "./files/tests/pattern/";
    private static String isoTest = "./files/tests/isomorphism/pat%d.ttl";
    private static String provTest = "./files/tests/provenance/prov%d.ttl";

    public static List<BasicPattern> setProvList() throws Exception {
        List<BasicPattern> provenanceList = null;

        provenanceList = new ArrayList<>();
        for (int i = 1; i < 8; i++) {
            provenanceList.add(RDF.model2Basicpattern(RDF.loadModel(String.format(provTest, i))));
        }
        return provenanceList;
    }

    public static Map<Integer, BasicPattern> setIsoTestist() throws Exception {
        Map<Integer, BasicPattern> isoTestList = null;
        isoTestList = new HashMap<Integer, BasicPattern>();
        for (int i = 1; i < 5; i++) {
            isoTestList.put(i, RDF.file2BasicpatternWithVars(String.format(isoTest, i)));
        }


        return isoTestList;
    }

    public static Map<Pair<Integer, Integer>, Boolean> setIsoTestAnswers() {
        Map<Pair<Integer, Integer>, Boolean> isoTestAnswers = null;
        isoTestAnswers = new HashMap<Pair<Integer, Integer>, Boolean>();
        isoTestAnswers.put(new Pair<Integer, Integer>(1, 2), true);
        isoTestAnswers.put(new Pair<Integer, Integer>(1, 3), false);
        isoTestAnswers.put(new Pair<Integer, Integer>(3, 4), false);
        isoTestAnswers.put(new Pair<Integer, Integer>(2, 4), false);
        return isoTestAnswers;

    }

    public static List<Pair<BasicPattern, Pair<Integer, Integer>>> setPatternList() {
        List<Pair<BasicPattern, Pair<Integer, Integer>>> patternList = null;
        File folder = new File(patternTest);
        File[] listOfFiles = folder.listFiles();
        patternList = new ArrayList<>();
        String pattern = ".*(\\d+)_(\\d+).ttl";
        Pattern r = Pattern.compile(pattern, Pattern.DOTALL);
        for (File f : listOfFiles) {

            Matcher m = r.matcher(f.getName());
            if (m.find()) {
                Integer p1_i = new Integer(m.group(1));
                Integer p2_i = new Integer(m.group(2));
                BasicPattern p = RDF.file2BasicpatternWithVars(f.getPath());

                patternList.add(new Pair<BasicPattern, Pair<Integer, Integer>>
                        (p, new Pair<Integer, Integer>(p1_i, p2_i)));
            }
        }

        return patternList;
    }

}
