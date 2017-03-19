package tau.cs.db.servlets;

import org.apache.jena.atlas.lib.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import tau.cs.db.qbp.QBPHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Servlet implementation class SandboxServlet
 */
@WebServlet(name = "SandboxServlet", urlPatterns = {"/SandboxServlet"})
public class SandboxServlet extends HttpServlet {
	private static final long serialVersionUID = 14564856456456465L;
    /**
     * Default constructor. 
     */
    public SandboxServlet() {
    	super();

    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		QBPHandler qbpHandler = (QBPHandler) request.getSession().getAttribute("qbpHandler");
		List<Pair<String,Integer>> results=qbpHandler.FindNodes("");

		JSONArray nodes = new JSONArray();
		results.stream().forEach(stringIntegerPair -> {
			JSONObject jo = new JSONObject();
			jo.put("text",stringIntegerPair.getLeft());
			jo.put("value",stringIntegerPair.getRight());
			nodes.add(jo);}
		);



		response.setContentType("application/json");

		response.getWriter().append(nodes.toJSONString());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
