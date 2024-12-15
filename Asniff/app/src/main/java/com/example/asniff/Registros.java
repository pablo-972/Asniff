package com.example.asniff;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class Registros extends AppCompatActivity {

    private ListView listaDispositivosBluetooth;
    private ArrayAdapter<String> listaAdaptadaDispositivosBluetooth;

    private ListView listaDispositivosWifi;
    private ArrayAdapter<String> listaAdaptadaDispositivosWifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registros);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Inicializamos
        listaDispositivosBluetooth = findViewById(R.id.registrosBluetooth);
        listaAdaptadaDispositivosBluetooth = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listaDispositivosBluetooth.setAdapter(listaAdaptadaDispositivosBluetooth);

        DatabaseReference databaseReferenceBluetooth = FirebaseDatabase.getInstance("https://asniff-603d3-default-rtdb.europe-west1.firebasedatabase.app").getReference("bluetooth");

        databaseReferenceBluetooth.get().addOnCompleteListener(task -> {
            DataSnapshot dataSnapshot = task.getResult();

            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                Map<String, String> dispositivo = (Map<String, String>) snapshot.getValue();
                listaAdaptadaDispositivosBluetooth.add("ID: " + snapshot.getKey() + "\n INFO: " + dispositivo.toString());
            }
        });

        //BorrarBluetooth
        listaDispositivosBluetooth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDevice = listaAdaptadaDispositivosBluetooth.getItem(position);
                int inicio = selectedDevice.indexOf("ID: ") + "ID: ".length();
                int fin = selectedDevice.indexOf("\n", inicio);
                String idDispositivo = selectedDevice.substring(inicio, fin).trim();

                databaseReferenceBluetooth.child(idDispositivo).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            listaAdaptadaDispositivosBluetooth.remove(selectedDevice);
                            Toast.makeText(Registros.this, "Dispositivo eliminado", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(Registros.this, "Error al eliminar el dispositivo", Toast.LENGTH_SHORT).show());
            }
        });

            //Inicializamos
        listaDispositivosWifi = findViewById(R.id.registrosWifi);
        listaAdaptadaDispositivosWifi = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listaDispositivosWifi.setAdapter(listaAdaptadaDispositivosWifi);

        DatabaseReference databaseReferenceWifi = FirebaseDatabase.getInstance("https://asniff-603d3-default-rtdb.europe-west1.firebasedatabase.app").getReference("wifi");

        databaseReferenceWifi.get().addOnCompleteListener(task -> {
            DataSnapshot dataSnapshot = task.getResult();

            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                Map<String, String> dispositivoWifi = (Map<String, String>) snapshot.getValue();
                listaAdaptadaDispositivosWifi.add("ID: " + snapshot.getKey() + "\n INFO: " + dispositivoWifi.toString());
            }
        });


        //BorrarWifi
        listaDispositivosWifi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDevice = listaAdaptadaDispositivosWifi.getItem(position);
                int inicio = selectedDevice.indexOf("ID: ") + "ID: ".length();
                int fin = selectedDevice.indexOf("\n", inicio);
                String idDispositivo = selectedDevice.substring(inicio, fin).trim();

                databaseReferenceWifi.child(idDispositivo).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            listaAdaptadaDispositivosWifi.remove(selectedDevice);
                            Toast.makeText(Registros.this, "Dispositivo eliminado", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(Registros.this, "Error al eliminar el dispositivo", Toast.LENGTH_SHORT).show());
            }
        });


    }
}