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
 * 1st iteration, easy solutions
 * All solutions except for stage 4 all commented out because I commented out completed tasks as I went along
 * so that I won't making calls to the same endpoint
 */
public class Challenge {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //I use OkHttp for Android as a singleton, makes network calls easy and beats AsyncTasks in Android
        //for now I'm just making an instance of it
        OkHttpClient client = new OkHttpClient();
        
        //construct JSON POST
        RequestBody body = RequestBody.create(JSON, 
                "{\"email\":\"srjames90@berkeley.edu\",\"github\":\"http://github.com/srjames90\"}");
        Request request = new Request.Builder()
                .url("http://challenge.code2040.org/api/register")
                .post(body)
                .build();
        
        //initialize JSON parser and a string for resulting token from response
        JsonParser parser = new JsonParser();
        String token = null;
        Response response;
        
        try {
            response = client.newCall(request).execute();
            token = ((JsonObject)parser.parse(response.body().string())).get("result").getAsString();
            System.out.println(token);
        } catch (IOException ex) {
            Logger.getLogger(Challenge.class.getName()).log(Level.SEVERE, null, ex);
        }
        RequestBody challengeRbody = RequestBody.create(JSON, "{\"token\":" + "\"" + token + "\"}");
        //Stage One:
        
        body = RequestBody.create(JSON, "{\"token\":" + "\"" + token + "\"}");
        request = new Request.Builder()
                .url("http://challenge.code2040.org/api/getstring")
                .post(body)
                .build();
        String reversed = "";
        try {
            response = client.newCall(request).execute();
            String str = ((JsonObject)parser.parse(response.body().string())).get("result").getAsString();
            reversed = new String(reverse(str));
        } catch (IOException ex) {
           Logger.getLogger(Challenge.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        body = RequestBody.create(JSON, "{\"token\":" + "\"" + token + "\", \"string\":\"" + reversed + "\"}");
        request = new Request.Builder()
                .url("http://challenge.code2040.org/api/validatestring")
                .post(body)
                .build();
        try {
            response = client.newCall(request).execute();
            System.out.println(response.body().string());
        } catch (IOException ex) {
            Logger.getLogger(Challenge.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Stage 2:
        
        request = new Request.Builder()
                .url("http://challenge.code2040.org/api/haystack")
                .post(challengeRbody)
                .build();
        try {
            response = client.newCall(request).execute();
            JsonObject result = ((JsonObject)parser.parse(response.body().string())).get("result").getAsJsonObject();
            System.out.println(result.toString());
            String needle = result.get("needle").getAsString();
            JsonArray haystack = result.get("haystack").getAsJsonArray();
            for(int x = 0; x < haystack.size(); x++) {
                if(haystack.get(x).getAsString().equals(needle)) {
                    request = new Request.Builder()
                    .url("http://challenge.code2040.org/api/validateneedle")
                    .post(RequestBody.create(JSON, "{\"token\":" + "\"" + token + "\", \"needle\":" + x + "}"))
                    .build();
                    response = client.newCall(request).execute();
                    System.out.println(response.body().string());
                    //return;
                }
            }
            //System.out.println("no match");
            
            
        } catch (IOException ex) {
            Logger.getLogger(Challenge.class.getName()).log(Level.SEVERE, null, ex);
        }

        
       //Stage 3:
        
        
        request = new Request.Builder()
                .url("http://challenge.code2040.org/api/prefix")
                .post(challengeRbody)
                .build();
        
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
            request = new Request.Builder()
                .url("http://challenge.code2040.org/api/validateprefix")
                .post(RequestBody.create(JSON, "{\"token\":" + "\"" + token + "\", \"array\":" + noPre + "}"))
                .build();
            response = client.newCall(request).execute();
                    System.out.println(response.body().string());           
            
        } catch (IOException ex) {
            Logger.getLogger(Challenge.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        //Stage 4: Creating JsonObject at end instead of using a string to represent Json
        
        request = new Request.Builder()
                .url("http://challenge.code2040.org/api/time")
                .post(challengeRbody)
                .build();
        
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
            request = new Request.Builder()
                .url("http://challenge.code2040.org/api/validatetime")
                .post(RequestBody.create(JSON, str.toString()))
                .build();
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
    
}
