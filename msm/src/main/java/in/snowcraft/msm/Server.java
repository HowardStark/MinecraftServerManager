package in.snowcraft.msm;

import android.app.Activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.Uri;

import android.os.Bundle;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Server extends Activity {

    String username;
    String password;
    String server;
    int port;
    String key;
    ArrayAdapter<Player> playerAdapter;
    HashMap<String, ExtendedFragment> fragmentList = new HashMap<String, ExtendedFragment>();
    ListView playerListView;
    List<Player> playerList = new ArrayList<Player>();
    //Navigations/Action Bar
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    ListView drawerList;
    ArrayAdapter<String> drawerAdapter;
    ArrayList<String> titles = new ArrayList<String>();
    ArrayList<String> possibleMethods = new ArrayList<String>();
    ArrayList<String> possibleGroups = new ArrayList<String>();
    HashMap<String, Class<? extends ExtendedFragment>> hashMap = new HashMap<String, Class<? extends ExtendedFragment>>();
    boolean first = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hashMap.put("players", PlayerManagementFragment.class);
        hashMap.put("server", ConsoleFragment.class);
        hashMap.put("dynmap", DynmapFragment.class);
        hashMap.put("streams", ChatFragment.class);
        setContentView(R.layout.activity_server);

        //Nav Drawer
        drawerList = (ListView) findViewById(R.id.drawer_list);
        drawerAdapter = new ArrayAdapter<String>(this, R.layout.drawer_item, titles);
        drawerList.setAdapter(drawerAdapter);
        drawerList.setOnItemClickListener(new DrawerItemClickListener());
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view){
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View view){
                invalidateOptionsMenu();
                System.out.println("HOORAY IT WORKS :D");
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        //This code gaurantees that these two are the first in the fragmentList.
        fragmentList.put("Login", new LoginFragment());
        fragmentList.put("Loading", new LoadingFragment());
        System.out.println(fragmentList.get(0));
        setFragment("Login");
        LocalBroadcastManager.getInstance(this).registerReceiver(ServerReceiver, new IntentFilter("output"));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
        return super.onPrepareOptionsMenu(menu);
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
        int lockMode = drawerLayout.getDrawerLockMode(Gravity.LEFT);

        if(lockMode == DrawerLayout.LOCK_MODE_UNLOCKED && drawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            for(ExtendedFragment fragment : fragmentList.values()){
                if(fragment.getPlace() == position){
                    setFragment(fragment.getTitle());
                }
            }
        }
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
        System.out.println("Inside Connection Success");
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        setFragment("Loading");
        call("jsonapi.methods", new String[0]);
        //playerListView = (ListView) findViewById(R.id.methodList);
    }

    //Sets the current fragment.
    public void setFragment(String name){
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentList.get(name)).commit();
        System.out.println("Setting fragment to " + fragmentList.get(name));
        drawerList.setItemChecked(fragmentList.get(name).getPlace() - 2, true);
        drawerLayout.closeDrawer(drawerList);
    }

    //TODO: Broadcast Receiver
    public BroadcastReceiver ServerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                JSONArray jsonArray = new JSONArray(intent.getStringExtra("output"));
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String result = jsonObject.getString("result");
                String source = jsonObject.getString("source");
                if(result.equals("success")){
                    if(first){
                        System.out.println("Calling connectionSuccess");
                        connectionSuccess();
                        first = false;
                    }
                    if(source.equals("jsonapi.methods")){
                        JSONArray methods = jsonObject.getJSONArray("success");
                        System.out.println(methods);
                        for(int i = 0; i < methods.length(); i++){
                            if(methods.get(i).toString().contains(".")){
                                possibleMethods.add(methods.get(i).toString());
                                String[] group = methods.get(i).toString().split("\\.");
                                if(!possibleGroups.contains(group[0])){
                                    possibleGroups.add(group[0]);
                                }
                            }
                        }
                        for (String possibleGroup : possibleGroups) {
                            try {
                                System.out.println(possibleGroup);
                                System.out.println("class is " + hashMap.get(possibleGroup));
                                String title = hashMap.get(possibleGroup).getConstructor().newInstance().getTitle();
                                fragmentList.put(title, hashMap.get(possibleGroup).getConstructor().newInstance()); //Null pointer exception
                                fragmentList.get(title).setPlace(titles.size());
                                titles.add(title);
                            } catch (InstantiationException e) {
                                e.printStackTrace();
                                break;
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                                break;
                            } catch (NullPointerException e) {
                                //Who cares. Expected.
                                System.out.println("NULL POINTER. JEN. U MADE ME DO DIS. I BLAYME U FOR MAH PRABLEHMS. GRRRR. #TRUMPETR");
                                //TODO Needs to be added back before production e.printStackTrace();
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                        if(possibleGroups.contains("players")){
                            call("players.online", new String[0]);
                        }
                        drawerAdapter.notifyDataSetChanged();
                    } else if(source.startsWith("players")){
                        if(source.equalsIgnoreCase("players.online")){
                            JSONArray success = jsonObject.getJSONArray("success");
                            for(int count = 0; count < success.length(); count++){
                                if(!playerList.contains(parsePlayer(success.getJSONObject(count))))
                                    playerList.add(parsePlayer(success.getJSONObject(count)));
                            }
                            setFragment("Player");
                        }
                    }
                /*if(result.equals("success") && first){
                    first = false;
                    jsonObject.getString("source");
                    connectionSuccess();
                    System.out.println("Setting fragment to something else");
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
                }*/
                } else {
                    JSONObject subObject = jsonObject.getJSONObject("error");
                    int errorCode = subObject.getInt("code");
                    switch(errorCode){
                        case 8:
                            setFragment("Login");
                            Toast.makeText(Server.this, "Username or password incorrect. Try again.", 2 * 1000).show();
                            break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //TODO: Login Fragment
    public static class LoginFragment extends ExtendedFragment {

        public EditText serverAddress;
        public EditText serverPort;
        public EditText username;
        public EditText password;
        public Button loginButton;

        View rootView;
        Server parentActivity;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            parentActivity = (Server) activity;
        }

        public LoginFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_login, container, false);

            loginButton = (Button) rootView.findViewById(R.id.buttonLogin);
            serverAddress = (EditText) rootView.findViewById(R.id.textAddress);
            serverPort = (EditText) rootView.findViewById(R.id.textPort);
            username = (EditText) rootView.findViewById(R.id.textUsername);
            password = (EditText) rootView.findViewById(R.id.textPassword);


            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(serverAddress.getText().toString() != "" && serverPort.getText().toString() != "" && username.getText().toString() != "" && password.getText().toString() != ""){
                        try {
                            parentActivity.key = HashString.sha256((username.getText().toString() + "server.version" + password.getText().toString()).getBytes());
                            System.out.println("Test 6: Better Hashing. Username: " + username + ". Password: " + password + ". Player: " + "server.version" + ". Key: " + parentActivity.key + ".");
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("name", "server.version");
                                jsonObject.put("key", parentActivity.key);
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
                                new ServerConnection(parentActivity.getBaseContext(), url, jsonObject).execute();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            System.out.println("Did not make it into here.");
                            return;
                        }
                        parentActivity.setUsername(username.getText().toString());
                        parentActivity.setPassword(password.getText().toString());
                        parentActivity.setPort(Integer.parseInt(serverPort.getText().toString()));
                        parentActivity.setServer(serverAddress.getText().toString());
                    } else {
                        Toast.makeText(parentActivity.getApplicationContext(), "Please fill in all the fields", 2 * 1000).show();
                    }
                }
            });
            parentActivity.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            return rootView;
        }
    }

    //TODO: Loading Fragment
    public static class LoadingFragment extends ExtendedFragment {

        private int place;

        View rootView;
        Server parentActivity;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            parentActivity = (Server) activity;
        }

        public LoadingFragment(){

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_loading, container, false);
            return rootView;
        }
    }

    //TODO: Player management code
    public Player parsePlayer(JSONObject jsonPlayer) {
        Player player = null;
        try {
            player = new Player(jsonPlayer.getString("name"), jsonPlayer.getInt("health"), jsonPlayer.getInt("foodLevel"));
            System.out.println(jsonPlayer.getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return player;
    }

    public void populatePlayerList(View rootView) {
        playerAdapter = new PlayerListAdapter();
        playerListView = (ListView) rootView.findViewById(R.id.playerList);
        playerListView.setAdapter(playerAdapter);
        playerAdapter.notifyDataSetChanged();
    }

    public class PlayerListAdapter extends ArrayAdapter<Player>{
        public PlayerListAdapter(){
            super(Server.this, R.layout.methodview, playerList);
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup){
            if(view == null) {
                view = getLayoutInflater().inflate(R.layout.object_playermanagement, viewGroup, false);
            }

            Player player = playerList.get(position);
            TextView username = (TextView) view.findViewById(R.id.username);
            System.out.println("Player name is " + player.getName());
            username.setText(player.getName());
            TextView health = (TextView) view.findViewById(R.id.health);
            System.out.println(player.getName() + " has " + player.getHealth() + " health.");
            health.setText(player.getHealth().toString());
            TextView food = (TextView) view.findViewById(R.id.food);
            System.out.println(player.getName() + " has " + player.getFood() + " food.");
            food.setText(player.getFood().toString());

            return view;
        }
    }

    //Player Management. List of players.
    public static class PlayerManagementFragment extends ExtendedFragment {

        View rootView;
        Server parentActivity;

        public static String title = "Player";

        @Override
        public String getTitle() {
            return title;
        }


        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            parentActivity = (Server) activity;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            System.out.println("Created view for fragment: PlayerManagementFragment");
            rootView = inflater.inflate(R.layout.playermanagement_fragment, container, false);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            parentActivity.populatePlayerList(rootView);
        }

    }

    //TODO: Player Fragment. Actual player object, with list of commands applicable on said person.
    public static class PlayerFragment extends Fragment {

        View rootView;
        Server parentActivity;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            parentActivity = (Server) activity;
        }

        public PlayerFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.tabfragment, container, false);
            return rootView;
        }
    }

    //TODO: Console Fragment
    //Console Fragment. Used to pull data from the console.
    public static class ConsoleFragment extends ExtendedFragment {

        View rootView;
        Server parentActivity;

        public static String title = "Console";

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            parentActivity = (Server) activity;
        }

        public ConsoleFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.console_fragment, container, false);
            return rootView;
        }
    }

    //TODO: Dynmap
    //Dynmap Fragment. Creates a WebView to load Dynmap.
    public static class DynmapFragment extends ExtendedFragment {

        private int place;

        View rootView;
        Server parentActivity;

        public static String title = "Dynmap";

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            parentActivity = (Server) activity;
        }

        WebView webView = new WebView(getActivity().getApplicationContext());

        public DynmapFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstance){
            /*call("dynmap.host", new String[0]);
            call("dynmap.port", new String[0]);*/
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.dynmap_fragment, container, false);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl("http://reddit.com/");
            return rootView;
        }
    }

    //TODO: Chat
    //Chat Fragment. TBE.
    public static class ChatFragment extends ExtendedFragment {

        View rootView;
        Server parentActivity;

        public static String title = "Chat";

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            parentActivity = (Server) activity;
        }

        public ChatFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.tabfragment, container, false);
            return rootView;
        }
    }

    //TODO: GETTERS AND SETTERS
    public void setUsername(String username){
        this.username = username;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public void setPort(int port){
        this.port = port;
    }

    public void setServer(String server){
        this.server = server;
    }

}



