package com.example.asniff;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bluetooth extends AppCompatActivity {

    private ListView listaDispositivos;
    private ArrayAdapter<String> listaAdaptadaDispositivos;
    private BluetoothAdapter bluetoothAdapter;

    private Map<String, String> dispositivosEncontrados = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        //Inicializamos
        listaDispositivos = findViewById(R.id.dispositivosBluetooth);
        listaAdaptadaDispositivos = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listaDispositivos.setAdapter(listaAdaptadaDispositivos);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        //Comprobamos que el adaptador funciona correctamente y este activado
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "ERROR AL DETECTAR EL ADAPTADOR BLUETOOTH", Toast.LENGTH_LONG).show();
            return;
        }
        if(!bluetoothAdapter.isEnabled()){
            Toast.makeText(getApplicationContext(), "ACTIVE EL SERVICIO BLUETOOTH", Toast.LENGTH_LONG).show();
        }

        //Comenzamos a escanear
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        bluetoothAdapter.startDiscovery();


        listaDispositivos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String dispositivo = listaAdaptadaDispositivos.getItem(position);
                int indice = dispositivo.indexOf("Dirección MAC: ") + "Dirección MAC: ".length();
                String macDispositivo = dispositivo.substring(indice);

                DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://asniff-603d3-default-rtdb.europe-west1.firebasedatabase.app").getReference("bluetooth");

                Map<String, String> deviceData = new HashMap<>();
                deviceData.put(macDispositivo, macDispositivo);

                databaseReference.push().setValue(deviceData)
                        .addOnSuccessListener(aVoid -> Toast.makeText(getApplicationContext(), "Dispositivo enviado a Firebase", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Error al enviar a Firebase", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String macAddress = device.getAddress();

                if (deviceName != null) {
                    String info = "Nombre: " + deviceName + "\nDirección MAC: " + macAddress;
                    if(!dispositivosEncontrados.containsKey(macAddress)){
                        dispositivosEncontrados.put(macAddress, deviceName);
                        listaAdaptadaDispositivos.add(info);
                        listaAdaptadaDispositivos.notifyDataSetChanged();
                    }

                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
