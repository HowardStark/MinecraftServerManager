package in.snowcraft.msm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Server extends Activity {

    public AsyncTask outgoing;
    MCJSONApi json;
    boolean first = true;
    String output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        json = new MCJSONApi(getIntent().getStringExtra("username"), getIntent().getStringExtra("password"), getIntent().getStringExtra("server"), Integer.parseInt(getIntent().getStringExtra("port")));
        super.onCreate(savedInstanceState);
        outgoing = new ServerConnection(this, json).execute("server.version");
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

    public void loginFailure(){
        Intent intent = new Intent(Server.this, Login.class);
        startActivity(intent);
    }


    public BroadcastReceiver ServerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                JSONArray jsonArray = new JSONArray(intent.getStringExtra("output"));
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String result = jsonObject.getString("result");
                if(result.equals("success") && first){
                    setContentView(R.layout.activity_server);
                    System.out.println("Setting view to server");
                } else if(result.equals("success") && !first){

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

}



