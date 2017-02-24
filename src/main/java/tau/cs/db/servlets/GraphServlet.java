package tau.cs.db.servlets;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import tau.cs.db.qbp.QBPHandler;
import org.apache.jena.atlas.lib.Pair;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by efrat on 29/01/17.
 */
@WebServlet(name = "GraphServlet", urlPatterns = {"/Provenance"})
public class GraphServlet extends HttpServlet {
    private static final long serialVersionUID = 102831973239L;
    public GraphServlet() {
        super();

    }

    @Override
    public void init() throws ServletException {

    }
        // .

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletContext context = this.getServletContext();
        QBPHandler qbpHandler = (QBPHandler) context.getAttribute("qbpHandler");

        if(!request.getParameterMap().containsKey("nodeList"))
            return;

        List<String> nodesList = Arrays.asList(request.getParameter("nodeList").split("\\s*,\\s*"));


        JSONArray results = new JSONArray();

        for(String nodeNum: nodesList){
            if(nodeNum==""){
                return;
            }
            results.add(qbpHandler.GetProvenanceGraph(new Integer(nodeNum)));
        }


        response.setContentType("application/json");

        response.getWriter().append(results.toJSONString());
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuffer jb = new StringBuffer();
        String line = null;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) { /*report an error*/ }
        JSONParser parser = new JSONParser();


        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) parser.parse(jb.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        response.setContentType("application/json");
        ServletContext context = this.getServletContext();
        QBPHandler qbpHandler = (QBPHandler) context.getAttribute("qbpHandler");
        List<Integer> exps = new ArrayList<>();
        ((JSONArray)jsonObject.get("explanation")).forEach((e)->exps.add(Integer.parseInt(e.toString())));


        qbpHandler.AddExplanation(Integer.parseInt(jsonObject.get("example").toString()),null,exps);


    }

}