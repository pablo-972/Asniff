package com.example.asniff;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;




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

        listaDispositivos = findViewById(R.id.dispositivosWifi);
        listaAdaptadaDispositivos = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listaDispositivos.setAdapter(listaAdaptadaDispositivos);


        String ipAddress = getLocalIpAddress();
        if (ipAddress != null) {
            String subnet = ipAddress.substring(0, ipAddress.lastIndexOf("."));
            scanNetwork(subnet);
        } else {
            Toast.makeText(this, "No se pudo obtener la dirección IP local", Toast.LENGTH_SHORT).show();
        }
    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void scanNetwork(String subnet) {
        for (int i = 1; i < 255; i++) {
            String host = subnet + "." + i;
            new Thread(() -> {
                try {
                    InetAddress inetAddress = InetAddress.getByName(host);
                    if (inetAddress.isReachable(500)) { // Tiempo de espera en milisegundos
                        runOnUiThread(() -> {
                        String resultado = "Nombre de dispositivo : " + inetAddress.getHostName() + "\nDirección IP:" + inetAddress.getHostAddress();
                        listaAdaptadaDispositivos.add(resultado);
                        listaAdaptadaDispositivos.notifyDataSetChanged();
                        });


                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

}