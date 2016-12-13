package tau.cs.db.qbp;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Variable;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;

import java.util.*;

/**
 * Created by efrat on 10/12/16.
 */
public class utils {


    public static SortedSet<Pair<Pair<Integer,Integer>,Float>> kMin(Float[][] arr,Integer k){
        SortedSet<Pair<Pair<Integer,Integer>,Float>> res = new TreeSet<>();
        for(int i=0;i<arr.length;i++){
            for(int j=0;j<arr[0].length;j++){
                if(res.last().getRight()>arr[i][j]){
                    res.add(new Pair<>(new Pair<Integer, Integer>(i,j),arr[i][j]));
                }
                while(res.size()>k){
                    res.remove(res.last());
                }
            }

        }
        return res;

    }


    public static Pair<Integer,Integer> argmin(Float[][] arr){
        Float m_v = new Float(Float.MAX_VALUE);
        Integer m_i = -1,m_j = -1;
        for(int i=0;i<arr.length;i++){
            for(int j=0;j<arr[0].length;j++){
                if(arr[i][j]<m_v){
                    m_v = arr[i][j];
                    m_i=i;
                    m_j=j;
                }
            }
        }
        return new Pair<Integer,Integer>(m_i,m_j);
    }

    public static BasicPattern FindBestMerge(BasicPattern p1, BasicPattern p2) {

        List<Triple> l1 = p1.getList();
        List<Triple> l2 = p2.getList();


        HashMap<Node,Integer> P2 = new HashMap<Node, Integer>();

        HashMap<Node,Set<Triple>> node2Triple_p1 = new HashMap<Node, Set<Triple>>();
        HashMap<Node,Set<Triple>> node2Triple_p2 = new HashMap<Node, Set<Triple>>();

        for(Triple triple : l1){

            Set<Triple> lst_s = node2Triple_p1.getOrDefault(triple.getSubject(),new HashSet<>());
            Set<Triple> lst_o = node2Triple_p1.getOrDefault(triple.getObject(),new HashSet<>());
            lst_s.add(triple);
            lst_o.add(triple);
            node2Triple_p1.put(triple.getSubject(),lst_s);
            node2Triple_p1.put(triple.getObject(),lst_o);
        }



        HashMap<Node,Integer> edge2Triple_p2 = new HashMap<Node, Integer>();

        for (Triple triple : l2){
            Integer count = edge2Triple_p2.getOrDefault(triple.getPredicate(),0);
            edge2Triple_p2.put(triple.getPredicate(),count+1);

            Set<Triple> lst_s = node2Triple_p2.getOrDefault(triple.getSubject(),new HashSet<>());
            Set<Triple> lst_o = node2Triple_p2.getOrDefault(triple.getObject(),new HashSet<>());
            lst_s.add(triple);
            lst_o.add(triple);
            node2Triple_p2.put(triple.getSubject(),lst_s);
            node2Triple_p2.put(triple.getObject(),lst_o);
        }

        Integer predicates2Sum =  edge2Triple_p2.values().stream().mapToInt(Integer::intValue).sum();
//        SortedSet<Node> predicateFreq =new TreeSet<>(new Comparator<Node>() {
//            @Override
//            public int compare(Node node, Node t1) {
//                return Integer.compare(edge2Triple_p2.get(node),edge2Triple_p2.get(t1));
//            }
//        });

        Float[][] weightMatchMatrix =  new Float[l1.size()][l2.size()];
        Boolean[] mark1 = new Boolean[l1.size()];
        Boolean[] mark2 = new Boolean[l2.size()];
        Arrays.fill(mark1,false);
        Arrays.fill(mark2,false);
//        predicateFreq.addAll(edge2Triple_p2.keySet());



        for(int i=0;i<l1.size();i++){
            for(int j=0;j<l2.size();j++){
                if(l1.get(i).getPredicate().matches(l2.get(j).getPredicate())){
                    weightMatchMatrix[i][j] = edge2Triple_p2.get(l1.get(i).getPredicate()).floatValue()/predicates2Sum;
                }else{
                    weightMatchMatrix[i][j] = new Float(1);
                }
                if(l1.get(i).getSubject().matches(l2.get(j).getSubject())){
                    weightMatchMatrix[i][j] = weightMatchMatrix[i][j]/2;
                }
                if(l1.get(i).getObject().matches(l2.get(j).getObject())){
                    weightMatchMatrix[i][j] = weightMatchMatrix[i][j]/2;
                }


            }
        }

        ArrayList<Pair<Triple,Triple>> matching = new ArrayList<>();
        Pair<Integer,Integer> arg_min  = utils.argmin(weightMatchMatrix);


        while(!Arrays.stream(mark1).reduce(true,(a,b)->Boolean.logicalAnd(a,b)) ||
                !Arrays.stream(mark2).reduce(true,(a,b)->Boolean.logicalAnd(a,b))){


            for(Triple t1: node2Triple_p1.get(l1.get(arg_min.getLeft()).getSubject())){
                for(Triple t2: node2Triple_p2.get(l2.get(arg_min.getRight()).getSubject())){
                    if((t1.getSubject().matches(t2.getSubject()) || t1.getObject().matches(t2.getObject())) &&
                            t1.getPredicate().matches(t2.getPredicate())){
                        weightMatchMatrix[l1.indexOf(t1)][l2.indexOf(t2)]/= 3;
                    }

                }
            }
            for(Triple t1: node2Triple_p1.get(l1.get(arg_min.getLeft()).getObject())){
                for(Triple t2: node2Triple_p2.get(l2.get(arg_min.getRight()).getObject())){
                    if((t1.getSubject().matches(t2.getSubject()) || t1.getObject().matches(t2.getObject())) &&
                            t1.getPredicate().matches(t2.getPredicate())){
                        weightMatchMatrix[l1.indexOf(t1)][l2.indexOf(t2)]/= 3;
                    }

                }
            }

            for (int i=0; i<l1.size();i++){
                weightMatchMatrix[arg_min.getLeft()][i]=Float.min(new Float(1),weightMatchMatrix[arg_min.getLeft()][i]*2);
            }

            for (int i=0; i<l2.size();i++) {
                weightMatchMatrix[i][arg_min.getRight()] = Float.min(new Float(1), weightMatchMatrix[i][arg_min.getRight()] * 2);
            }
            weightMatchMatrix[arg_min.getLeft()][arg_min.getRight()] = new Float(1);

            mark1[arg_min.getLeft()] = true;
            mark2[arg_min.getRight()] = true;
            matching.add(new Pair(l1.get(arg_min.getLeft()),l2.get(arg_min.getRight())));
            arg_min = argmin(weightMatchMatrix);

        }

        BasicPattern result = new BasicPattern();

        Iterator<String> var_itr = (new Alphabet('a','z')).iterator();

        for(Pair<Triple,Triple> match : matching){
            Node sub =(match.getLeft().getSubject().matches(match.getRight().getSubject()))?match.getLeft().getSubject():
                    new Node_Variable(var_itr.next());
            Node obj =(match.getLeft().getObject().matches(match.getRight().getObject()))?match.getLeft().getObject():
                    new Node_Variable(var_itr.next());
            Node pred = match.getLeft().getPredicate();

            result.add(new Triple(sub,pred,obj));
        }

        return result;


    }
}
