package com.example.asniff;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Bluetooth extends AppCompatActivity {

    private ListView listaDispositivos;
    private ArrayAdapter<String> listaAdaptadaDispositivos;
    private BluetoothAdapter bluetoothAdapter;
    private Toast toast;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bluetooth);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        //Inicializamos
        listaDispositivos = findViewById(R.id.dispositivosBluetooth);
        listaAdaptadaDispositivos = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listaDispositivos.setAdapter(listaAdaptadaDispositivos);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(bluetoothAdapter == null){
            toast = Toast.makeText(getApplicationContext(), "ERROR AL DETECTAR EL ADAPTADOR BLUETOOTH", Toast.LENGTH_LONG);
            toast.show();
        }

        if(!bluetoothAdapter.isEnabled()){
            toast = Toast.makeText(getApplicationContext(), "ACTIVE EL SERVICIO BLUETOOTH", Toast.LENGTH_LONG);
            toast.show();
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);


        bluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String macAddress = device.getAddress();

                if(deviceName != null){
                    String info = "Nombre: " + deviceName + "\nDirección MAC: " + macAddress;
                    listaAdaptadaDispositivos.add(info);
                    listaAdaptadaDispositivos.notifyDataSetChanged();
                }
            }
        }
    };

}