/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Code2040;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;



/**
 *
 * @author Steven
 * 1st iteration of solutions
 */
public class Challenge {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static String token;
    public static JsonParser parser = new JsonParser();
    public static OkHttpClient client = new OkHttpClient();
    public static Response response;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //I use OkHttp for Android as a singleton, makes network calls easy and beats AsyncTasks in Android
        //for now I'm just making an instance of it
        
        //construct intitial JSON POST
        JsonObject json = new JsonObject();
        json.addProperty("email", "srjames90@berkeley.edu");
        json.addProperty("github", "http://github.com/srjames90");
        RequestBody body = RequestBody.create(JSON, json.toString());
        Request request = buildRequest("http://challenge.code2040.org/api/register", body);
        
        //initialize JSON parser and a string for resulting token from response
        
        try {
            response = client.newCall(request).execute();
            token = ((JsonObject)parser.parse(response.body().string())).get("result").getAsString();
        } catch (IOException ex) {
            Logger.getLogger(Challenge.class.getName()).log(Level.SEVERE, null, ex);
        }        

        //Stage One:
        
        request = buildRequest("http://challenge.code2040.org/api/getstring");
        String reversed = "";
        try {
            response = client.newCall(request).execute();
            String str = ((JsonObject)parser.parse(response.body().string())).get("result").getAsString();
            reversed = new String(reverse(str));
        } catch (IOException ex) {
           Logger.getLogger(Challenge.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        json.addProperty("token", token);
        json.addProperty("string", reversed);
        
        body = RequestBody.create(JSON, json.toString());
        request = buildRequest("http://challenge.code2040.org/api/validatestring", body);
        try {
            response = client.newCall(request).execute();
            System.out.println(response.body().string());
        } catch (IOException ex) {
            Logger.getLogger(Challenge.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Stage 2:
        
        request = buildRequest("http://challenge.code2040.org/api/haystack");
        try {
            response = client.newCall(request).execute();
            JsonObject result = ((JsonObject)parser.parse(response.body().string())).get("result").getAsJsonObject();
            String needle = result.get("needle").getAsString();
            JsonArray haystack = result.get("haystack").getAsJsonArray();
            for(int x = 0; x < haystack.size(); x++) {
                if(haystack.get(x).getAsString().equals(needle)) {
                    request = buildRequest("http://challenge.code2040.org/api/validateneedle", 
                            RequestBody.create(JSON, "{\"token\":" + "\"" + token + "\", \"needle\":" + x + "}"));
                    response = client.newCall(request).execute();
                    System.out.println(response.body().string());
                }
            } 
        } catch (IOException ex) {
            Logger.getLogger(Challenge.class.getName()).log(Level.SEVERE, null, ex);
        }

        
       //Stage 3:
        
        request = buildRequest("http://challenge.code2040.org/api/prefix");        
        try {
            response = client.newCall(request).execute();
            JsonObject result = ((JsonObject)parser.parse(response.body().string())).get("result").getAsJsonObject();
            System.out.println(result.toString());
            String prefix = result.get("prefix").getAsString();
            JsonArray strings = result.get("array").getAsJsonArray();
            JsonArray noPre = new JsonArray();
            for(JsonElement e: strings) {
                if(!e.getAsString().startsWith(prefix)) {
                    noPre.add(e);
                }
            }
            request = buildRequest("http://challenge.code2040.org/api/validateprefix",
                     RequestBody.create(JSON, "{\"token\":" + "\"" + token + "\", \"array\":" + noPre + "}"));  
            response = client.newCall(request).execute();
            System.out.println(response.body().string());             
        } catch (IOException ex) {
            Logger.getLogger(Challenge.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        //Stage 4: Creating JsonObject at end instead of using a string to represent Json
        
        request = buildRequest("http://challenge.code2040.org/api/time");
        try {
            response = client.newCall(request).execute();
            JsonObject result = ((JsonObject)parser.parse(response.body().string())).get("result").getAsJsonObject();
            System.out.println(result.toString());
            String dateStamp = result.get("datestamp").getAsString();
            int  interval = result.get("interval").getAsInt();
            DateTime dateTime = new DateTime(dateStamp);
            dateTime = dateTime.plusSeconds(interval);
            JsonObject str = new JsonObject();
            str.addProperty("token", token);
            str.addProperty("datestamp", dateTime.toString());
            request = buildRequest("http://challenge.code2040.org/api/validatetime", RequestBody.create(JSON, str.toString()));
            response = client.newCall(request).execute();
            System.out.println(response.body().string());           
            
        } catch (IOException ex) {
            Logger.getLogger(Challenge.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

        
    public static char[] reverse(String str) {
        int length = str.length();
        char[] reversed = new char[length]; 
        for(int x = 0; x <= length / 2; x++) {
            reversed[x] = str.charAt(length - x - 1);
            reversed[length - x - 1] = str.charAt(x);
        }
        return reversed;
    }
    
    public static Request buildRequest(String url, RequestBody body) {
        return new Request.Builder().url(url).post(body).build();
    }
    
    public static Request buildRequest(String url) {
        JsonObject json = new JsonObject();
        json.addProperty("token", token);
        return new Request.Builder()
                .url(url).post(RequestBody.create(JSON, json.toString())).build();
    }
    
}
