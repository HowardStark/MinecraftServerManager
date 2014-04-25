package in.snowcraft.msm;

import android.app.Activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.Uri;

import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.content.LocalBroadcastManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

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
    ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();
    ListView listView;
    List<Player> playerList = new ArrayList<Player>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        fragmentList.add(new LoginFragment());
        System.out.println(fragmentList.get(0));
        setFragment(fragmentList.get(0));
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
        System.out.println("Test 6: Better Hashing. Username: " + username + ". Password: " + password + ". Player: " + method + ". Key: " + key + ".");
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

    public void connectionSuccess() {
        setContentView(R.layout.playermanagement_fragment);
        //listView = (ListView) findViewById(R.id.methodList);
    }

    public void setFragment(Fragment fragment){
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
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
                    jsonObject.getString("source");
                    connectionSuccess();
                    System.out.println("Setting view to server");
                    JSONArray playersArray = jsonObject.getJSONArray("success");
                    for(int count = 0; count < playersArray.length(); count++) {
                        JSONObject playerObject = playersArray.getJSONObject(count);
                        playerList.add(parsePlayer(playerObject));
                    }
                    System.out.println(playerList.size());
                    populateList();
                } else if(result.equals("success") && !first){
                    JSONArray playersArray = jsonObject.getJSONArray("success");
                    for(int count = 0; count < playersArray.length(); count++){
                        JSONObject playerObject = playersArray.getJSONObject(count);
                        playerList.add(parsePlayer(playerObject));
                    }
                    populateList();
                } else {
                    JSONObject subObject = jsonObject.getJSONObject("error");
                    int errorCode = subObject.getInt("code");
                    switch(errorCode){
                        case 8:
                            Toast.makeText(Server.this, "Username or password incorrect. Try again.", 2 * 1000).show();
                            break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public Player parsePlayer(JSONObject jsonPlayer){
        Player player = new Player();
        try {
            player.setName(jsonPlayer.getString("name"));
            System.out.println(jsonPlayer.getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return player;
    }

    public void populateList(){
        ArrayAdapter<Player> adapter = new PlayerListAdapter();
        listView.setAdapter(adapter);
    }

    private class PlayerListAdapter extends ArrayAdapter<Player>{
        public PlayerListAdapter(){
            super(Server.this, R.layout.methodview, playerList);
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup){
            if(view == null) {
                view = getLayoutInflater().inflate(R.layout.methodview, viewGroup, false);
            }

            Player player = playerList.get(position);
            TextView methodName = (TextView) view.findViewById(R.id.methodName);
            System.out.println("Player name is " + player.getName());
            methodName.setText(player.getName());

            return view;
        }
    }

    public class LoginFragment extends Fragment {

        public EditText serverAddress;
        public EditText serverPort;
        public EditText username;
        public EditText password;
        public Button loginButton;

        public LoginFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_login, container, false);

            loginButton = (Button) rootView.findViewById(R.id.buttonLogin);
            serverAddress = (EditText) rootView.findViewById(R.id.textAddress);
            serverPort = (EditText) rootView.findViewById(R.id.textPort);
            username = (EditText) rootView.findViewById(R.id.textUsername);
            password = (EditText) rootView.findViewById(R.id.textPassword);
            System.out.println("Pre-click handler launch sequence initiated. I blame Gavin.");
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("MADE IT INTO ONCLICK. CONGRATS DIP****.");
                    if(serverAddress.getText().toString() != "" && serverPort.getText().toString() != "" && username.getText().toString() != "" && password.getText().toString() != ""){
                        try {
                            key = HashString.sha256((username.getText().toString() + "server.version" + password.getText().toString()).getBytes());
                            System.out.println("Test 6: Better Hashing. Username: " + username + ". Password: " + password + ". Player: " + "server.version" + ". Key: " + key + ".");
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("name", "server.version");
                                jsonObject.put("key", key);
                                jsonObject.put("username", username.getText().toString());
                                jsonObject.put("arguments", new JSONArray(Arrays.asList(new String[0])));
                                jsonObject.put("tag", "");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            System.out.println(jsonObject);
                            try {
                                String urlArgs = Uri.parse("api/2/call").buildUpon().appendQueryParameter("json", jsonObject.toString()).build().toString();
                                System.out.println(urlArgs);
                                URL url = new URL("http", serverAddress.getText().toString(), Integer.parseInt(serverPort.getText().toString()), urlArgs);
                                new ServerConnection(getBaseContext(), url, jsonObject).execute();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            System.out.println("Did not make it into here.");
                            return;
                        }
                    } else {
                        Toast.makeText(Server.this, "Please fill in all the fields", 2 * 1000).show();
                    }
                }
            });
            return rootView;
        }

    }

    //Player Management. List of players.
    public static class PlayerManagementFragment extends Fragment {

        public PlayerManagementFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.playermanagement_fragment, container, false);
            return rootView;
        }
    }

    //Player Fragment. Actual player object, with list of commands applicable on said person.
    //TODO: Needs custom fragment
    public static class PlayerFragment extends Fragment {
        public PlayerFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.tabfragment, container, false);
            return rootView;
        }
    }

    //Console Fragment. Used to pull data from the console.
    public static class ConsoleFragment extends Fragment {
        public ConsoleFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.console_fragment, container, false);
            return rootView;
        }
    }

    //Dynmap Fragment. Creates a WebView to load Dynmap.
    public static class DynmapFragment extends Fragment {
        public DynmapFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.dynmap_fragment, container, false);
            return rootView;
        }
    }

    //Chat Fragment. TBE.
    public static class ChatFragment extends Fragment {
        public ChatFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.tabfragment, container, false);
            return rootView;
        }
    }


}



