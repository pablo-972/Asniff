package com.example.asniff;

import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_CLASSIC;
import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_DUAL;
import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_LE;
import static android.net.wifi.ScanResult.WIFI_STANDARD_11AC;
import static android.net.wifi.ScanResult.WIFI_STANDARD_11AD;
import static android.net.wifi.ScanResult.WIFI_STANDARD_11AX;
import static android.net.wifi.ScanResult.WIFI_STANDARD_11BE;
import static android.net.wifi.ScanResult.WIFI_STANDARD_11N;
import static android.net.wifi.ScanResult.WIFI_STANDARD_LEGACY;
import static android.net.wifi.ScanResult.WIFI_STANDARD_UNKNOWN;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Analisis extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //Inicializamos
        setContentView(R.layout.activity_analisis);
        TextView idRegistro = findViewById(R.id.idRegistro);
        TextView infoRegistro = findViewById(R.id.infoRegistro);
        TextView analisisRegistro = findViewById(R.id.analisisRegistro);

        String idDispositivo = getIntent().getStringExtra("idDispositivo");
        String tipoDispositivo = getIntent().getStringExtra("tipoDispositivo");
        idRegistro.setText("ID: " + idDispositivo);

        DatabaseReference databaseReference;
        if(tipoDispositivo.equals("wifi")){
            databaseReference = FirebaseDatabase.getInstance("https://asniff-603d3-default-rtdb.europe-west1.firebasedatabase.app").getReference("wifi");
        }else if(tipoDispositivo.equals("bluetooth")){
            databaseReference = FirebaseDatabase.getInstance("https://asniff-603d3-default-rtdb.europe-west1.firebasedatabase.app").getReference("bluetooth");
        } else {
            databaseReference = null;
        }


        databaseReference.child(idDispositivo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    Object dispositivoData = dataSnapshot.getValue();
                    String datosCrudos = dispositivoData.toString();


                    String cleanedData = datosCrudos.replace("{", "").replace("}", "");
                    String[] parts = cleanedData.split("=");
                    String datosMac = parts[0];

                    HashMap<String, List<String>> dispositivo = (HashMap) dispositivoData;

                    if (tipoDispositivo.equals("wifi")) {

                        String ssid = dispositivo.get(datosMac).get(0);
                        int estandarWifi = Integer.parseInt(dispositivo.get(datosMac).get(1));
                        String estandarWifiDescripcion = getString(R.string.desconocido);
                        switch(estandarWifi) {
                            case WIFI_STANDARD_LEGACY:
                                estandarWifiDescripcion = "legacy";
                            case WIFI_STANDARD_11N:
                                estandarWifiDescripcion = "11n";
                            case WIFI_STANDARD_11AC:
                                estandarWifiDescripcion = "11ac";
                            case WIFI_STANDARD_11AX:
                                estandarWifiDescripcion = "11ax";
                            case WIFI_STANDARD_11AD:
                                estandarWifiDescripcion = "11ad";
                            case WIFI_STANDARD_11BE:
                                estandarWifiDescripcion = "11be";
                        }

                        String capabilities = dispositivo.get(datosMac).get(2);

                        StringBuilder wifiInfo = new StringBuilder();
                        wifiInfo.append("SSID: ").append(ssid).append("\n");
                        wifiInfo.append(getString(R.string.estandar_wifi)).append(" " + estandarWifiDescripcion).append("\n");
                        wifiInfo.append(getString(R.string.capacidades_wifi)).append(" " + capabilities).append("\n");

                        infoRegistro.setText(wifiInfo.toString());

                    } else if (tipoDispositivo.equals("bluetooth")) {

                        String nombreBluetooth = dispositivo.get(datosMac).get(0);
                        int tipoBluetooth = Integer.parseInt(dispositivo.get(datosMac).get(1));
                        String tipoBluetoothDescripcion = getString(R.string.desconocido);
                        if (tipoBluetooth == DEVICE_TYPE_LE) {
                            tipoBluetoothDescripcion = "Bluetooth Low Energy (BLE)";
                        } else if (tipoBluetooth == DEVICE_TYPE_CLASSIC) {
                            tipoBluetoothDescripcion = getString(R.string.b_clasico);
                        } else if (tipoBluetooth == DEVICE_TYPE_DUAL) {
                            tipoBluetoothDescripcion = getString(R.string.b_dual);
                        }
                        String uuids = dispositivo.get(datosMac).get(2);

                        StringBuilder bluetoothInfo = new StringBuilder();
                        bluetoothInfo.append(getString(R.string.nombre)).append(" " + nombreBluetooth).append("\n");
                        bluetoothInfo.append(getString(R.string.tipo_bluetooth)).append(" " + tipoBluetoothDescripcion).append("\n");
                        bluetoothInfo.append("UUIDs: ").append(uuids != null && !uuids.isEmpty() ? uuids : "-").append("\n");

                        infoRegistro.setText(bluetoothInfo.toString());

                    }




                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            //Llamada para saber fabricante
                            String macVendorResponse = getMacVendorInfo(datosMac);

                            String primeraPalabra = getFirstWord(macVendorResponse);

                            if(primeraPalabra.equals("Beijing")) primeraPalabra= "Xiaomi";

                            //Llamada a USA
                            String nvdResponse = getCVEInfoFromNVD(primeraPalabra);
                            String mostrar = parseVulnerabilityData(Analisis.this,nvdResponse);


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    analisisRegistro.setText(getString(R.string.fabricante) + macVendorResponse+"\n"+ getString(R.string.vulnerabilidades) + "\n" + mostrar);
                                }
                            });
                        }

                        public String getFirstWord(String input) {
                            String[] words = input.split("\\s+"); // Divide el string por espacios
                            return words[0]; // Devuelve siempre la primera palabra
                        }

                        public String parseVulnerabilityData(Context context, String nvdResponse) {
                            try {
                                // Convertir la respuesta JSON en un objeto JSONObject
                                JSONObject responseJson = new JSONObject(nvdResponse);

                                // Verificar que la respuesta tenga el campo "vulnerabilities"
                                if (!responseJson.has("vulnerabilities")) {
                                    throw new Exception("Campo 'vulnerabilities' no encontrado en la respuesta.");
                                }

                                // Obtener el array de vulnerabilidades
                                JSONArray vulnerabilities = responseJson.getJSONArray("vulnerabilities");

                                // Verificar que haya al menos una vulnerabilidad
                                if (vulnerabilities.length() == 0) {
                                    throw new Exception("No se encontraron vulnerabilidades en la respuesta.");
                                }

                                // Tomar solo la primera vulnerabilidad si hay varias
                                JSONObject vulnerability = vulnerabilities.getJSONObject(0);

                                // Verificar que la vulnerabilidad contenga el campo "cve"
                                if (!vulnerability.has("cve")) {
                                    throw new Exception("Campo 'cve' no encontrado en la vulnerabilidad.");
                                }

                                JSONObject cve = vulnerability.getJSONObject("cve");

                                // Verificar que existan métricas
                                if (!cve.has("metrics")) {
                                    throw new Exception("Campo 'metrics' no encontrado en la vulnerabilidad.");
                                }

                                JSONObject metrics = cve.getJSONObject("metrics");

                                // Verificar que existan datos CVSS V2
                                if (!metrics.has("cvssMetricV2")) {
                                    throw new Exception("Campo 'cvssMetricV2' no encontrado en las métricas.");
                                }

                                JSONArray cvssMetricV2Array = metrics.getJSONArray("cvssMetricV2");
                                JSONObject cvssMetricV2 = cvssMetricV2Array.getJSONObject(0);

                                // Obtener los datos relevantes
                                String lastModified = cve.optString("lastModified", "No disponible");
                                String baseSeverity = cvssMetricV2.optString("baseSeverity", "No disponible");
                                double exploitabilityScore = cvssMetricV2.optDouble("exploitabilityScore", -1);
                                double impactScore = cvssMetricV2.optDouble("impactScore", -1);

                                JSONObject cvssData = cvssMetricV2.getJSONObject("cvssData");
                                String version = responseJson.optString("version", "No disponible");
                                String vectorString = cvssData.optString("vectorString", "No disponible");
                                double baseScore = cvssData.optDouble("baseScore", -1);
                                String accessVector = cvssData.optString("accessVector", "No disponible");
                                String accessComplexity = cvssData.optString("accessComplexity", "No disponible");
                                String authentication = cvssData.optString("authentication", "No disponible");
                                String confidentialityImpact = cvssData.optString("confidentialityImpact", "No disponible");
                                String integrityImpact = cvssData.optString("integrityImpact", "No disponible");
                                String availabilityImpact = cvssData.optString("availabilityImpact", "No disponible");
                                //R.string.fabricante
                                // Crear un nuevo String con la información extraída
                                StringBuilder updatedResponse = new StringBuilder();
                                updatedResponse.append(context.getString(R.string.ultima_modificacion)).append(lastModified).append("\n")
                                        .append(context.getString(R.string.severidad)).append(baseSeverity).append("\n")
                                        .append(context.getString(R.string.explotable)).append(exploitabilityScore).append("\n")
                                        .append(context.getString(R.string.impacto)).append(impactScore).append("\n")
                                        .append(context.getString(R.string.version)).append(version).append("\n")
                                        .append(context.getString(R.string.cadena_vectores)).append(vectorString).append("\n")
                                        .append(context.getString(R.string.puntuacion_base)).append(baseScore).append("\n")
                                        .append(context.getString(R.string.vector_acceso)).append(accessVector).append("\n")
                                        .append(context.getString(R.string.complejidad_acceso)).append(accessComplexity).append("\n")
                                        .append(context.getString(R.string.autenticacion)).append(authentication).append("\n")
                                        .append(context.getString(R.string.impacto_confidencialidad)).append(confidentialityImpact).append("\n")
                                        .append(context.getString(R.string.impacto_integridad)).append(integrityImpact).append("\n")
                                        .append(context.getString(R.string.impacto_disponibilidad)).append(availabilityImpact);

                                // Devolver el nuevo String con la información procesada
                                return updatedResponse.toString();

                            } catch (Exception e) {
                                // Mostrar el error con más detalle
                                e.printStackTrace();
                                return e.getMessage();
                            }
                        }


                        private String getMacVendorInfo(String macAddress) {
                            String response = "";
                            try {
                                String urlString = "https://api.macvendors.com/" + macAddress;
                                URL url = new URL(urlString);

                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("GET");

                                // Leer la respuesta de la API
                                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                String inputLine;
                                StringBuilder responseBuilder = new StringBuilder();

                                while ((inputLine = in.readLine()) != null) {
                                    responseBuilder.append(inputLine);
                                }
                                in.close();

                                response = responseBuilder.toString();

                            } catch (Exception e) {
                                e.printStackTrace();
                                return "Error al obtener datos";
                            }
                            return response;
                        }

                        private String getCVEInfoFromNVD(String vendor) {
                            String response = "";
                            try {
                                // Usar la respuesta de macvendors como parte del query de NVD
                                String urlString = "https://services.nvd.nist.gov/rest/json/cves/2.0?keywordSearch=" + vendor;
                                URL url = new URL(urlString);

                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("GET");

                                // Leer la respuesta de la API
                                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                String inputLine;
                                StringBuilder responseBuilder = new StringBuilder();

                                while ((inputLine = in.readLine()) != null) {
                                    responseBuilder.append(inputLine);
                                }
                                in.close();

                                response = responseBuilder.toString();

                            } catch (Exception e) {
                                e.printStackTrace();
                                return "Error al obtener datos de NVD";
                            }
                            return response;
                        }
                    });


                } else {
                    Toast.makeText(Analisis.this, "Dispositivo no encontrado", Toast.LENGTH_SHORT).show();
                }
            }



            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Analisis.this, "Error al obtener datos", Toast.LENGTH_SHORT).show();
            }
        });

        Button botonEliminar = findViewById(R.id.borrarRegistro);
        botonEliminar.setOnClickListener(v -> {
            databaseReference.child(idDispositivo).removeValue().addOnSuccessListener(aVoid -> { setResult(RESULT_OK);
                Toast.makeText(Analisis.this, "Dispositivo eliminado", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> Toast.makeText(Analisis.this, "Error al eliminar el dispositivo", Toast.LENGTH_SHORT).show());

        });

    }
}
