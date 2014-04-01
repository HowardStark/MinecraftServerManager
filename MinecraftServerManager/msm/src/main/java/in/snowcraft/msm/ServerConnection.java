package in.snowcraft.msm;

import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

public class ServerConnection extends AsyncTask<String, String, String> {

    private static String output;

    BufferedReader in;
    PrintWriter out;
    JSONObject jsonObject;
    boolean first = true;
    Context context;
    URL url;

    public ServerConnection(Context context, URL url, JSONObject jsonObject){
        this.context = context;
        this.jsonObject = jsonObject;
        this.url = url;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onProgressUpdate(String... strings) {

    }

    @Override
    protected void onPostExecute(String result) {
        Intent send = new Intent("output");
        send.putExtra("output", result);
        LocalBroadcastManager.getInstance(context).sendBroadcast(send);
    }

}
