package com.example.asniff;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Wifi extends AppCompatActivity {

    private ListView listaDispositivos;

    private ArrayAdapter<String> listaAdaptadaDispositivos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Inicializamos
        listaDispositivos = findViewById(R.id.dispositivosWifi);
        listaAdaptadaDispositivos = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listaDispositivos.setAdapter(listaAdaptadaDispositivos);

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), "ACTIVE EL SERVICIO WIFI", Toast.LENGTH_LONG).show();
        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String myAddress = Formatter.formatIpAddress(wifiInfo.getIpAddress());
        listaAdaptadaDispositivos.add(myAddress);

        wifiManager.startScan();
        List<ScanResult> results = wifiManager.getScanResults();
        for(ScanResult result : results){
            //listaAdaptadaDispositivos.add(result.toString());
            String info = "BBSID: " + result.BSSID + "\nSSID: " + result.SSID;
            listaAdaptadaDispositivos.add(info);
            listaAdaptadaDispositivos.notifyDataSetChanged();
        }

        listaDispositivos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
            }

        });


    }

}

