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
import java.util.Arrays;

public class ServerConnection extends AsyncTask<String, String, String> {

    private static String output;

    BufferedReader in;
    PrintWriter out;
    MCJSONApi json;
    boolean first = true;
    Activity activity;

    public ServerConnection(Activity activity, MCJSONApi json){
        this.activity = activity;
        this.json = json;
    }

    @Override
    protected String doInBackground(String... strings) {
        return json.call(strings[0], Arrays.copyOfRange(strings, 1, strings.length));
    }

    @Override
    protected void onProgressUpdate(String... strings) {

    }

    @Override
    protected void onPostExecute(String result) {
        Intent send = new Intent("output");
        send.putExtra("output", result);
        LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(send);
    }

}
