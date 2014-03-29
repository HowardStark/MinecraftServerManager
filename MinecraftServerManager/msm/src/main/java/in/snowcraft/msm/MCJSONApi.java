package in.snowcraft.msm;

import android.content.Intent;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by howard on 3/19/14.
 */
public class MCJSONApi {

    String username;
    String key;
    String method;
    String address;
    int port;
    String args[];
    String password;

    public MCJSONApi(String username, String password, String address, int port){
        this.password = password;
        System.out.println("Test 6");
        this.username = username;
        this.address = address;
        this.port = port;
    }

    public String call(String method, String args[]){
        this.key = HashString.sha256((username + method + password).getBytes());
        System.out.println("Test 6: Better Hashing. Username: " + username + ". Password: " + password + ". Method: " + method + ". Key: " + key + ".");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", method);
            jsonObject.put("key", key);
            jsonObject.put("username", username);
            jsonObject.put("arguments", new JSONArray(Arrays.asList(args)));
            jsonObject.put("tag", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(jsonObject);
        try {
            String urlArgs = Uri.parse("api/2/call").buildUpon().appendQueryParameter("json", jsonObject.toString()).build().toString();
            System.out.println(urlArgs);
            URL url = new URL("http", address, port, urlArgs);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setDoOutput(true);
            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.write(jsonObject.toString());
            out.flush();
            out.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String output;
            while((output = in.readLine()) != null){
                System.out.println(output);
                return output;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e){

        }
        return "";

    }

}
