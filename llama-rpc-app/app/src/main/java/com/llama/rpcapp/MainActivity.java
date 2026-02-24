package com.llama.rpcapp;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView tvIpAddress;
    private EditText etPort, etThreads;
    private Button btnStart, btnStop;
    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvIpAddress = findViewById(R.id.tvIpAddress);
        etPort = findViewById(R.id.etPort);
        etThreads = findViewById(R.id.etThreads);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);

        String ip = getWifiIpAddress();
        tvIpAddress.setText(String.format("IP Address: %s", ip));

        btnStart.setOnClickListener(v -> {
            startRpcService();
        });

        btnStop.setOnClickListener(v -> {
            // Since rpc server is blocking and doesn't have an interrupt, 
            // we restart the process for a clean state if needed.
            System.exit(0);
        });

        if (getIntent().getBooleanExtra("autoStart", false)) {
            startRpcService();
        }
    }

    private void startRpcService() {
        int port = Integer.parseInt(etPort.getText().toString());
        int threads = Integer.parseInt(etThreads.getText().toString());

        Intent serviceIntent = new Intent(this, RpcServerService.class);
        serviceIntent.putExtra("host", "0.0.0.0");
        serviceIntent.putExtra("port", port);
        serviceIntent.putExtra("threads", threads);

        ContextCompat.startForegroundService(this, serviceIntent);
        
        btnStart.setEnabled(false);
        btnStart.setText("SERVER RUNNING");
        btnStop.setVisibility(View.VISIBLE);
        isRunning = true;
    }

    private String getWifiIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        return Formatter.formatIpAddress(ipInt);
    }
}
