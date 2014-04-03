package in.snowcraft.msm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Server extends Activity {

    public AsyncTask outgoing;
    public Object output;
    JSONObject outputJSON;
    String outputString;
    String username;
    String password;
    String server;
    int port;
    String key;

    ListView listView;
    List<Method> methods = new ArrayList<Method>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        server = getIntent().getStringExtra("server");
        port = Integer.parseInt(getIntent().getStringExtra("port"));
        call("jsonapi.methods", new String[0]);
        LocalBroadcastManager.getInstance(this).registerReceiver(ServerReceiver, new IntentFilter("output"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.server, menu);
        return true;
    }

    public void onDestroy(){
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(ServerReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void call(String method, String args[]){
        key = HashString.sha256((username + method + password).getBytes());
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
            URL url = new URL("http", server, port, urlArgs);
            new ServerConnection(this.getBaseContext(), url, jsonObject).execute();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void onConnectionSuccess(){
        setContentView(R.layout.activity_server);
        listView = (ListView) findViewById(R.id.methodList);
    }

    public BroadcastReceiver ServerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                boolean first = true;
                JSONArray jsonArray = new JSONArray(intent.getStringExtra("output"));
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String result = jsonObject.getString("result");
                if(result.equals("success") && first){
                    first = false;
                    onConnectionSuccess();
                    System.out.println("Setting view to server");
                    JSONArray apiMethods = jsonObject.getJSONArray("success");
                    for(int count = 0; count < apiMethods.length(); count++){
                        if(apiMethods.getString(count).contains(".") && !apiMethods.getString(count).startsWith("adminium"))
                            methods.add(new Method(apiMethods.getString(count)));
                    }
                    populateList();
                } else if(result.equals("success") && !first){
                    JSONArray apiMethods = jsonObject.getJSONArray("success");
                    for(int count = 0; count < apiMethods.length(); count++){
                        methods.add(new Method(apiMethods.getString(count)));
                    }
                    populateList();
                } else {
                    JSONObject subObject = jsonObject.getJSONObject("error");
                    int errorCode = subObject.getInt("code");
                    switch(errorCode){
                        case 8:
                            Toast.makeText(Server.this, "Username or password incorrect. Try again.", 2 * 1000).show();
                            Intent login = new Intent(Server.this, Login.class);
                            startActivity(login);
                            break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public void populateList(){
        ArrayAdapter<Method> adapter = new MethodListAdapter();
        listView.setAdapter(adapter);
    }

    private class MethodListAdapter extends ArrayAdapter<Method>{
        public MethodListAdapter(){
            super(Server.this, R.layout.methodview, methods);
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup){
            if(view == null) {
                view = getLayoutInflater().inflate(R.layout.methodview, viewGroup, false);
            }

            Method method = methods.get(position);

            TextView methodName = (TextView) view.findViewById(R.id.methodName);
            methodName.setText(method.getName());

            return view;
        }
    }
}



