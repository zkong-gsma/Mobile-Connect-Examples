package org.example.force_4g;

import android.content.Context;
import android.content.Intent;
import android.media.audiofx.BassBoost;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static ConnectivityManager connectivityManager;
    private static TelephonyManager tel;
    private static volatile Network cellularNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        initalizeCellularNetwork();
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        Log.i("==MCCMNC==", tel.getSimOperator());
        getMccMnc();
        if (cellularNetwork == null) {
            Log.i("==on resume==", "we are bad!!!");
        } else {
            Log.i("==on resume==", "we are good");
        }
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

    }

    @Override
    public void onClick(View view) {

        Intent intent = new Intent(MainActivity.this, Webview.class);
        switch(view.getId()) {
            case R.id.mobile_data:
                if (cellularNetwork == null) {
                    intent.putExtra("network", "normal");
                    Toast.makeText(this, "Cellular Network is not connected. Please check! Using Default.", Toast.LENGTH_LONG).show();
                } else {
                    intent.putExtra("network", "mobile_data");
                }
                break;

            case R.id.normal:
                intent.putExtra("network", "normal");
                break;
        }
        intent.putExtra("url", "https://whatismyipaddress.com/").setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, 1);

    }

    public static Network getCellularNetwork() {
        return cellularNetwork;
    }

    public static void initalizeCellularNetwork() {
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        connectivityManager.requestNetwork(
                networkRequest,
                new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        cellularNetwork = network;
                        Log.i("==!!2==", network.toString()+ "|New network connected.");
                    }

                    @Override
                    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                        Log.i("==!!3==", network.toString() + "|" + networkCapabilities.getLinkDownstreamBandwidthKbps());
                    }

                    @Override
                    public void onLosing (Network network, int maxMsToLive) {
                        cellularNetwork = null;
                        Log.i("==!!3==", network.toString() + "|About to loose!!! unset.(" + maxMsToLive + ")");
                    }

                    @Override
                    public void onLost (Network network) {
                        cellularNetwork = null;
                        Log.i("==!!3==", network.toString() + "|Lost!!! unset.");
                    }
                }
        );
    }

    public static void initalizeWiFiNetwork() {
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();
        connectivityManager.requestNetwork(
                networkRequest,
                new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {


                        Log.i("==!!1==", network.toString());
                    }
                }
        );
    }


    public void getMccMnc() {
        final TextView text = (TextView) findViewById(R.id.textView);
        text.setText("Your SIM MCC/MNC:" + tel.getSimOperator());
        //return tel.getSimOperator();
    }

}

