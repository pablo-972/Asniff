package com.example.asniff;

import android.content.Intent;
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

public class RegistrosBluetooth extends AppCompatActivity {

    private ListView listaDispositivosBluetooth;
    private ArrayAdapter<String> listaAdaptadaDispositivosBluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registros_bluetooth);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Inicializamos
        listaDispositivosBluetooth = findViewById(R.id.guardadosBluetooth);
        listaAdaptadaDispositivosBluetooth = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listaDispositivosBluetooth.setAdapter(listaAdaptadaDispositivosBluetooth);
        cargaDispositivosBluetooth();



        //Goto AnalisisBluetooth
        listaDispositivosBluetooth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDevice = listaAdaptadaDispositivosBluetooth.getItem(position);
                int inicio = selectedDevice.indexOf("ID: ") + "ID: ".length();
                int fin = selectedDevice.indexOf("\n", inicio);
                String idDispositivo = selectedDevice.substring(inicio, fin).trim();

                Intent intent = new Intent(RegistrosBluetooth.this, Analisis.class);
                intent.putExtra("idDispositivo", idDispositivo);
                intent.putExtra("tipoDispositivo", "bluetooth");
                startActivityForResult(intent, 1);

            }
        });
    }

    private void cargaDispositivosBluetooth(){
        DatabaseReference databaseReferenceBluetooth = FirebaseDatabase.getInstance("https://asniff-603d3-default-rtdb.europe-west1.firebasedatabase.app").getReference("bluetooth");

        databaseReferenceBluetooth.get().addOnCompleteListener(task -> {
            DataSnapshot dataSnapshot = task.getResult();

            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                Map<String, String> dispositivo = (Map<String, String>) snapshot.getValue();
                listaAdaptadaDispositivosBluetooth.add("ID: " + snapshot.getKey() + "\n INFO: " + dispositivo.toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            cargaDispositivosBluetooth();
        }
    }
}