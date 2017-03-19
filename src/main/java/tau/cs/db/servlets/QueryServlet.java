package tau.cs.db.servlets;

import org.apache.jena.query.Query;
import org.json.simple.JSONObject;
import tau.cs.db.qbp.QBPHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by efrat on 19/02/17.
 */
@WebServlet(name = "QueryServlet", urlPatterns = {"/Query"})
public class QueryServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession context = request.getSession();
        QBPHandler qbpHandler = (QBPHandler) context.getAttribute("qbpHandler");
        response.setContentType("application/json");

        JSONObject jo = new JSONObject();
        Query q= qbpHandler.GetTop1Query();
        jo.put("query",q==null?"No Query Found":q.toString());
        response.getWriter().append(jo.toJSONString());

    }
}
