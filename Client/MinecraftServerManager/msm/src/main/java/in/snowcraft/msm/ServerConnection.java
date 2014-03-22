package in.snowcraft.msm;

import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ServerConnection extends AsyncTask<String, String, String> {

    private static String output;

    BufferedReader in;
    PrintWriter out;
    MCJSONApi json;
    boolean first = true;

    @Override
    protected String doInBackground(String... strings) {
        if(first){
            String address = strings[0];
            int port = Integer.parseInt(strings[1]);
            String username = strings[2];
            String password = strings[3];
            json = new MCJSONApi(username, password, address, port, first);
        } else {
            json.call(strings[0], Arrays.copyOfRange(strings, 1, strings.length));
        }
        return "";
    }

    @Override
    protected void onProgressUpdate(String... strings) {
    }

    @Override
    protected void onPostExecute(String result) {

    }

    public static String getOutput(){
        return output;
    }

}
