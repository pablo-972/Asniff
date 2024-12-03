package com.example.asniff;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;


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
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String myAddress = Formatter.formatIpAddress(wifiInfo.getIpAddress());

        listaAdaptadaDispositivos.add(myAddress);

        if (myAddress != null) {
            String subnet = myAddress.substring(0, myAddress.lastIndexOf("."));
            scanNetwork(subnet);

        } else {
            Toast.makeText(this, "No se pudo obtener la dirección IP local", Toast.LENGTH_SHORT).show();
        }
    }

    private void scanNetwork(String subnet) {
        //tiene que ir una hebra (creo)
    }

}

