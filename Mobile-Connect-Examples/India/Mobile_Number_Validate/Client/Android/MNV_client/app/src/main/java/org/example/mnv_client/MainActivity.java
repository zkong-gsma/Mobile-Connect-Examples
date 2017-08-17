package org.example.mnv_client;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener {
    private NetworkStateReceiver networkStateReceiver;
    private String auth_session;
    private MainActivity this_class;
    private Button button;

    private final String check_network_url = "http://localhost/mnv_plus_php/check_network.php";
    private final String start_mc_url = "http://localhost/mnv_plus_php/start_mc.php";
    private final String more_info_url = "http://localhost/mnv_plus_php/more_info.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this_class = this;
        setContentView(R.layout.activity_main);
        startNetworkBroadcastReceiver(this);

    }

    @Override
    protected void onPause() {
        unregisterNetworkBroadcastReceiver(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        registerNetworkBroadcastReceiver(this);
        super.onResume();
    }

    public void callback(String cb, String result) {
        switch(cb) {
            case "checkIPAddress":
                checkIPAddress(result);
                break;
            case "getExtraInfo":
                Toast.makeText(this, "Authentication Successful", Toast.LENGTH_LONG).show();
                Toast.makeText(this, result, Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //Request code from WebView
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

                //MNV successful do something with this
                if (getStatFromUrl(data.getStringExtra("result"))) {
                    new myHttp(this_class, "getExtraInfo").execute(more_info_url + "?session_id=" + auth_session);
                } else {
                    Toast.makeText(this, "Authentication unsuccessful", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public boolean getStatFromUrl(String query) {
        boolean state = false;
        String[] data = query.split("&");
        for(int i=0; i<data.length; i++) {
            String[] item = data[i].split("=");
            if (item[0].equalsIgnoreCase("state") && item[1].equalsIgnoreCase("1")) {
                state = true;
            }
        }
        return state;
    }

    public void makeToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    //only return true if its on mobile data.
    private boolean checkMobileData() {
        final ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifi.isConnectedOrConnecting ()) {
            //Toast.makeText(this, "Wifi", Toast.LENGTH_LONG).show();
            return false;
        } else if (mobile.isConnectedOrConnecting ()) {
            //Toast.makeText(this, "Mobile 3G ", Toast.LENGTH_LONG).show();
            return true;
        } else {
            //Toast.makeText(this, "No Network ", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void checkIPAddress(String result) {
        //avoid exception if check_network api is not available
        if (result == null) {
            result = "{}";
        }
        try {
            JSONObject res = new JSONObject(result);
            if (res.getBoolean("status")) {
                auth_session = res.getString("session_id");
                Toast.makeText(this, "Oh yes", Toast.LENGTH_LONG).show();
                setContentView(R.layout.mobile_connect_activity);

                button = (Button) findViewById(R.id.button);
                button.setOnClickListener(new OnClickListener() {
                    public void onClick(View arg0) {
                        TextView tv = (TextView) findViewById(R.id.editText);
                        if (tv.getText().length() > 0) {
                            Intent intent = new Intent(MainActivity.this, webview.class);
                            intent.putExtra("url", start_mc_url + "?session_id=" + auth_session + "&msisdn=" + tv.getText().toString()).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivityForResult(intent, 1);
                        } else {
                            this_class.makeToast("Please enter your MSISDN");
                        }
                    }
                });


            } else {
                Toast.makeText(this, "Oh no, 4G data not supported", Toast.LENGTH_LONG).show();
                setContentView(R.layout.activity_main);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void networkAvailable() {
        if (checkMobileData()) {
            new myHttp(this_class, "checkIPAddress").execute(check_network_url);
        } else {
            Toast.makeText(this, "We can't do MNV on WiFi", Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void networkUnavailable() {
        Toast.makeText(this, "Please Connect to the internet", Toast.LENGTH_LONG).show();
    }

    public void startNetworkBroadcastReceiver(Context currentContext) {
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener((NetworkStateReceiver.NetworkStateReceiverListener) currentContext);
        registerNetworkBroadcastReceiver(currentContext);
    }

    public void registerNetworkBroadcastReceiver(Context currentContext) {
        currentContext.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public void unregisterNetworkBroadcastReceiver(Context currentContext) {
        currentContext.unregisterReceiver(networkStateReceiver);
    }
}
