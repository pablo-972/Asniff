package com.example.asniff;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Bluetooth extends AppCompatActivity {

    private TextView dispositivoBluetooth;
    private ListView listaDispositivos;
    private ArrayAdapter<String> listaAdaptadaDispositivos;
    private BluetoothAdapter bluetoothAdapter;

    private Map<String, String> dispositivosEncontrados = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        //Inicializamos
        dispositivoBluetooth = findViewById(R.id.dispositivoBluetooth);
        listaDispositivos = findViewById(R.id.dispositivosBluetooth);
        listaAdaptadaDispositivos = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listaDispositivos.setAdapter(listaAdaptadaDispositivos);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        //Comprobamos que el adaptador funciona correctamente y este activado

        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_adaptador_bluetooth), Toast.LENGTH_LONG).show();
            return;
        }

        if(!bluetoothAdapter.isEnabled()){
            Toast.makeText(getApplicationContext(), getString(R.string.active_servicio_bluetooth), Toast.LENGTH_LONG).show();
        }

        dispositivoBluetooth.setText(getString(R.string.dispositivo_actual) + " " + bluetoothAdapter.getName());

        //Comenzamos a escanear
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        bluetoothAdapter.startDiscovery();


        listaDispositivos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String dispositivo = listaAdaptadaDispositivos.getItem(position);
                int indice = dispositivo.indexOf(getString(R.string.lista_bluetooth_direcion_mac)) + getString(R.string.lista_bluetooth_direcion_mac).length();
                String macDispositivo = dispositivo.substring(indice);
                String nombreDispositivo = dispositivosEncontrados.get(macDispositivo);

                DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://asniff-603d3-default-rtdb.europe-west1.firebasedatabase.app").getReference("bluetooth");

                Map<String, String> deviceData = new HashMap<>();
                deviceData.put(macDispositivo, nombreDispositivo);

                databaseReference.push().setValue(deviceData)
                        .addOnSuccessListener(aVoid -> Toast.makeText(getApplicationContext(),getString(R.string.dispositivo_enviado_firebase), Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), getString(R.string.error_envio_firebase), Toast.LENGTH_SHORT).show());
            }
        });

        Button registroBluetoothButton = findViewById(R.id.guardadosBluetooth);
        registroBluetoothButton.setOnClickListener(v -> {
            Intent intent = new Intent(Bluetooth.this, RegistrosBluetooth.class);
            startActivity(intent);
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

                    String info = getString(R.string.lista_bluetooth_nombre) + deviceName + "\n"+ getString(R.string.lista_bluetooth_direcion_mac) + macAddress;
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
