package tau.cs.db.servlets;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import tau.cs.db.qbp.QBPHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by efrat on 07/03/17.
 */
@WebServlet(name = "DiffServlet", urlPatterns = {"/Diff"})
public class DiffServlet extends HttpServlet {
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
        HttpSession context = request.getSession();
        QBPHandler qbpHandler = (QBPHandler) context.getAttribute("qbpHandler");
        if(((String)jsonObject.get("request")).equals("render")){

            qbpHandler.InitQuestionIterator();
        }
        if(((String)jsonObject.get("request")).equals("answer")){
            qbpHandler.AnswerQuestion(Boolean.parseBoolean((String) jsonObject.get("answer")));
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession context = request.getSession();
        QBPHandler qbpHandler = (QBPHandler) context.getAttribute("qbpHandler");
        if(request.getParameterMap().containsKey("question")){
            response.setContentType("application/json");

            response.getWriter().append(qbpHandler.GetQuestion().toJSONString());

        }
        if(request.getParameterMap().containsKey("next")){
            response.setContentType("application/json");
            JSONObject ob = new JSONObject();
            ob.put("has",qbpHandler.HasMoreQuestions());
            response.getWriter().append(ob.toJSONString());

        }

    }
}
