package tau.cs.db.utils;


import org.apache.jena.graph.Node;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.util.FileManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.List;

/**
 * Created by efrat on 12/12/16.
 */

/**
 * static class to handle everything concerning json
 *
 */
public class JS {

    static String examplesPath = "./files/examples/%s/examples.json";
    static String explanationsDir = "./files/examples/%s/results/%d.json";

    public static void WriteExamplesJson(String queryName,String varName,List<Node> results){
        JSONParser parser = new JSONParser();
        String jsonFile = String.format(examplesPath, queryName);

        JSONArray exapmles = new JSONArray();
        for(Node example : results){


            JSONObject jo= new JSONObject();
            jo.put("var",varName);
            if(example.isLiteral()) {

                jo.put("value",example.getLiteral().toString());
                jo.put("type","Literal");
            }
            else {
                jo.put("value",example.toString());
                jo.put("type","URI");
            }
            exapmles.add(jo);
        }

        try {
            File file = new File(jsonFile);
            if(!file.exists()){
                if (!file.getParentFile().exists())
                    file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            fileWriter.write(exapmles.toString());
            fileWriter.close();


        }catch(IOException ex){
            ex.printStackTrace();
        }

    }

    public static ResultSet JS2ResultSet(String examplePath){


        InputStream in = FileManager.get().open(examplePath);
        return ResultSetFactory.fromJSON(in);
    }

    public static ResultSet JS2ResultSet(String queryName,int exampleNum){

        String filePath = String.format(explanationsDir,queryName,exampleNum);
        InputStream in = FileManager.get().open(filePath);
        return ResultSetFactory.fromJSON(in);
    }
    public static void formatJsonToResultSet(String queryName){
        String jsonFile = String.format(examplesPath, queryName);
        JSONParser parser = new JSONParser();
        Object obj = null;
        try {

            FileReader filereader = new FileReader(jsonFile);
            obj = parser.parse(filereader);

            filereader.close();
        }catch(IOException ex){
            ex.printStackTrace();
            return;
        }

        catch(ParseException ex){
            ex.printStackTrace();
            return;
        }

        JSONArray jarray = (JSONArray) obj;
        int i=0;
        for(Object jv : jarray) {

            JSONObject jsonObject = (JSONObject) jv;
            i++;
            JSONObject writeObj = new JSONObject();

            JSONArray headArr = new JSONArray();
            headArr.add((String) jsonObject.get("var").toString());

            JSONObject varObj = new JSONObject();
            varObj.put("vars", headArr);
            writeObj.put("head", varObj);

            JSONObject resultObj = new JSONObject();
            JSONArray bindingArr = new JSONArray();
            JSONObject bindObj = new JSONObject();
            JSONObject singleObject = new JSONObject();
            if(jsonObject.get("type").toString().matches("Literal")) {
                singleObject.put("type", "literal");
            }else {
                singleObject.put("type", "uri");
            }
            singleObject.put("value", (String) jsonObject.get("value").toString());

            bindObj.put((String) jsonObject.get("var").toString(), singleObject);
            bindingArr.add(bindObj);
            resultObj.put("bindings", bindingArr);

            writeObj.put("results", resultObj);

            String targetFile = String.format(explanationsDir, queryName,i);
            FileWriter fileWriter = null;
            try {

                File file = new File(targetFile);
                if(!file.exists()){

                    file.createNewFile();
                }
                fileWriter = new FileWriter(targetFile);
                fileWriter.write(writeObj.toString());
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

}

