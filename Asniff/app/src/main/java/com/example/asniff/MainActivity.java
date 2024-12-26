package com.example.asniff;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    DatabaseReference databaseReference;

    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        // Actualizar la configuración de recursos
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        String systemLanguage = Locale.getDefault().getLanguage();
        if ("es".equals(systemLanguage)) {
            // Cambiar el idioma de la aplicación a español
            setLocale("es");
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Pedimos los servicios necesarios
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, 1);
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            // Si la localización está desactivada, muestra un Toast
            Toast.makeText(getApplicationContext(), getString(R.string.activa_localizacion), Toast.LENGTH_LONG).show();
        }



        Button botonBluetooth = findViewById(R.id.bluetooth);
        botonBluetooth.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Bluetooth.class);
            startActivity(intent);
        });

        Button wifiButton = findViewById(R.id.wifi);
        wifiButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Wifi.class);
            startActivity(intent);
        });



    }
}