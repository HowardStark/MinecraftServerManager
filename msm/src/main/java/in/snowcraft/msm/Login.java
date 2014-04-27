package in.snowcraft.msm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import in.snowcraft.msm.Server;

public class Login extends Activity {

    public String incoming;

    public static EditText serverAddress;
    public static EditText serverPort;
    public static EditText username;
    public static EditText password;
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginButton = (Button) findViewById(R.id.buttonLogin);
        serverAddress = (EditText) findViewById(R.id.textAddress);
        serverPort = (EditText) findViewById(R.id.textPort);
        username = (EditText) findViewById(R.id.textUsername);
        password = (EditText) findViewById(R.id.textPassword);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(serverAddress.getText().toString() != "" && serverPort.getText().toString() != "" && username.getText().toString() != "" && password.getText().toString() != ""){
                try {
                    Intent intent = new Intent(Login.this, Server.class);
                    intent.putExtra("username", username.getText().toString());
                    intent.putExtra("password", password.getText().toString());
                    intent.putExtra("server", serverAddress.getText().toString());
                    intent.putExtra("port", serverPort.getText().toString());
                    startActivity(intent);
                } catch (Exception e) {
                    /*if(clientSocket != null)
                        try {
                            clientSocket.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    e.printStackTrace();
                    */
                    return;
                }
            } else {
                Toast.makeText(Login.this, "Please fill in all the fields", 2 * 1000).show();
            }

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
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

}

