package com.example.asniff;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Bluetooth extends AppCompatActivity {

    private ListView listaDispositivos;
    private ArrayAdapter<String> listaAdaptadaDispositivos;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        listaDispositivos = findViewById(R.id.dispositivosBluetooth);
        listaAdaptadaDispositivos = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listaDispositivos.setAdapter(listaAdaptadaDispositivos);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "ERROR AL DETECTAR O ACTIVAR EL ADAPTADOR BLUETOOTH", Toast.LENGTH_LONG).show();
            return;
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        bluetoothAdapter.startDiscovery();
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
                    listaAdaptadaDispositivos.add(info);
                    listaAdaptadaDispositivos.notifyDataSetChanged();

                    // Aquí subimos los datos a Firebase
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://asniff-603d3-default-rtdb.europe-west1.firebasedatabase.app")
                            .getReference("dispositivos");

                    // Crear un mapa con los datos del dispositivo
                    Map<String, String> deviceData = new HashMap<>();
                    deviceData.put("name", deviceName);
                    deviceData.put("mac", macAddress);

                    // Subir los datos de forma sencilla
                    databaseReference.push().setValue(deviceData)
                            .addOnSuccessListener(aVoid -> Toast.makeText(getApplicationContext(), "Dispositivo enviado a Firebase", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Error al enviar a Firebase", Toast.LENGTH_SHORT).show());
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver); // No olvides cancelar el registro del receptor
    }
}
