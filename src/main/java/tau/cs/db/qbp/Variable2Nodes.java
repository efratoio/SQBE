package tau.cs.db.qbp;

import com.github.andrewoma.dexx.collection.Sets;
import org.apache.jena.graph.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by efrat on 25/01/17.
 */
public class Variable2Nodes extends HashMap<Node,Set<String>> {
    Map<String, Node> node2String;

    public Variable2Nodes() {
        super();
        this.node2String = new HashMap<>();
    }

    @Override
    public Set<String> put(Node node, Set<String> strings) {
        Set<String> res = super.put(node, new HashSet<String>(strings));
        for(String s: strings){
            this.node2String.put(s,node);
        }
        return res;
    }

    public Node getNode(String var){
        return this.node2String.get(var);

    }
}
