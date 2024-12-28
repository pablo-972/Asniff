package com.example.asniff;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Wifi extends AppCompatActivity {
    private TextView direccionIP;

    private ListView listaDispositivos;

    private ArrayAdapter<String> listaAdaptadaDispositivos;

    private Map<String, List<String>> dispositivosEncontrados = new HashMap<>();


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
        direccionIP = findViewById(R.id.direccionIP);
        listaDispositivos = findViewById(R.id.dispositivosWifi);
        listaAdaptadaDispositivos = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listaDispositivos.setAdapter(listaAdaptadaDispositivos);

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), getString(R.string.activa_servicio_wifi), Toast.LENGTH_LONG).show();
        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String myAddress = Formatter.formatIpAddress(wifiInfo.getIpAddress());
        direccionIP.setText(getString(R.string.conectado_a) + wifiInfo.getSSID() + getString(R.string.direccion_ip) + myAddress);


        wifiManager.startScan();
        List<ScanResult> results = wifiManager.getScanResults();
        for(ScanResult result : results){
            String info = "BBSID: " + result.BSSID + "\nSSID: " + result.SSID;
            List<String> data = new ArrayList<>();
            data.add(result.SSID);
            data.add(String.valueOf(result.getWifiStandard()));
            data.add(result.capabilities);



            if(!dispositivosEncontrados.containsKey(result.BSSID)){
                dispositivosEncontrados.put(result.BSSID, data);
                listaAdaptadaDispositivos.add(info);
                listaAdaptadaDispositivos.notifyDataSetChanged();
            }
        }

        Button registroWifiButton = findViewById(R.id.guardadosWifi);
        registroWifiButton.setOnClickListener(v -> {
            Intent intent = new Intent(Wifi.this, RegistrosWifi.class);
            startActivity(intent);
        });

        listaDispositivos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ap = listaAdaptadaDispositivos.getItem(position);
                int inicio = ap.indexOf("BSSID: ") + "BSSID: ".length();
                int fin = ap.indexOf("\n", inicio);
                String bssid = ap.substring(inicio,fin).trim();
                List<String> data = dispositivosEncontrados.get(bssid);


                DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://asniff-603d3-default-rtdb.europe-west1.firebasedatabase.app").getReference("wifi");

                Map<String, List<String>> deviceData = new HashMap<>();
                deviceData.put(bssid,data);

                databaseReference.push().setValue(deviceData)
                        .addOnSuccessListener(aVoid -> Toast.makeText(getApplicationContext(), "Dispositivo enviado a Firebase", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Error al enviar a Firebase", Toast.LENGTH_SHORT).show());
            }

        });


    }

}

