package in.snowcraft.msm;

import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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

    @Override
    protected String doInBackground(String... strings) {
        String address = strings[0];
        int port = Integer.parseInt(strings[1]);
        String username = strings[2];
        String password = strings[3];
        try{
            Socket socket = new Socket(address, port);
            out = new PrintWriter(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.write("login:" + username.trim() + ";" + password.trim());
            out.flush();
            String line;
            try{
                while((line = in.readLine()) != null){
                    output = line;
                    publishProgress(line);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception ex){
            ex.printStackTrace();
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
