package com.example.asniff;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class RegistrosWifi extends AppCompatActivity {



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
        listaDispositivosWifi = findViewById(R.id.guardadosWifi);
        listaAdaptadaDispositivosWifi = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listaDispositivosWifi.setAdapter(listaAdaptadaDispositivosWifi);
        cargarDispositivosWifi();


        //AnalizarWifi
        listaDispositivosWifi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDevice = listaAdaptadaDispositivosWifi.getItem(position);
                int inicio = selectedDevice.indexOf("ID: ") + "ID: ".length();
                int fin = selectedDevice.indexOf("\n", inicio);
                String idDispositivo = selectedDevice.substring(inicio, fin).trim();

                Intent intent = new Intent(RegistrosWifi.this, Analisis.class);
                intent.putExtra("idDispositivo", idDispositivo);
                intent.putExtra("tipoDispositivo", "wifi");
                startActivityForResult(intent, 1);

            }
        });

    }

    private void cargarDispositivosWifi(){
        DatabaseReference databaseReferenceWifi = FirebaseDatabase.getInstance("https://asniff-603d3-default-rtdb.europe-west1.firebasedatabase.app").getReference("wifi");

        databaseReferenceWifi.get().addOnCompleteListener(task -> {
            DataSnapshot dataSnapshot = task.getResult();

            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                Map<String, String> dispositivoWifi = (Map<String, String>) snapshot.getValue();
                listaAdaptadaDispositivosWifi.add("ID: " + snapshot.getKey() + "\n INFO: " + dispositivoWifi.toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            cargarDispositivosWifi();
        }
    }
}