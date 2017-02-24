package tau.cs.db.servlets;

import org.json.simple.JSONObject;
import tau.cs.db.qbp.QBPHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by efrat on 19/02/17.
 */
@WebServlet(name = "QueryServlet", urlPatterns = {"/Query"})
public class QueryServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletContext context = this.getServletContext();
        QBPHandler qbpHandler = (QBPHandler) context.getAttribute("qbpHandler");
        response.setContentType("application/json");

        JSONObject jo = new JSONObject();
        jo.put("query",qbpHandler.GetQuery());
        response.getWriter().append(jo.toJSONString());

    }
}
