package com.example.asniff;

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
        idRegistro.setText("ID del Dispositivo: " + idDispositivo);

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
                    //Se que es optimizable


                    String cleanedData = datosCrudos.replace("{", "").replace("}", "");
                    String[] parts = cleanedData.split("=");
                    String datosMac = parts[0];

                    HashMap<String, List<String>> dispositivo = (HashMap) dispositivoData;
                    infoRegistro.setText(dispositivo.get(datosMac).toString());



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
                            String mostrar = parseVulnerabilityData(nvdResponse);


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Actualizar la interfaz de usuario
                                    analisisRegistro.setText("Fabricante: " + macVendorResponse+"\nVulnerabilidades: " + mostrar);
                                }
                            });
                        }

                        public String getFirstWord(String input) {
                            String[] words = input.split("\\s+"); // Divide el string por espacios
                            return words[0]; // Devuelve siempre la primera palabra
                        }

                        public String parseVulnerabilityData(String nvdResponse) {
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

                                // Crear un nuevo String con la información extraída
                                StringBuilder updatedResponse = new StringBuilder();
                                updatedResponse.append("Last Modified: ").append(lastModified).append("\n")
                                        .append("Base Severity: ").append(baseSeverity).append("\n")
                                        .append("Exploitability Score: ").append(exploitabilityScore).append("\n")
                                        .append("Impact Score: ").append(impactScore).append("\n")
                                        .append("Version: ").append(version).append("\n")
                                        .append("Vector String: ").append(vectorString).append("\n")
                                        .append("Base Score: ").append(baseScore).append("\n")
                                        .append("Access Vector: ").append(accessVector).append("\n")
                                        .append("Access Complexity: ").append(accessComplexity).append("\n")
                                        .append("Authentication: ").append(authentication).append("\n")
                                        .append("Confidentiality Impact: ").append(confidentialityImpact).append("\n")
                                        .append("Integrity Impact: ").append(integrityImpact).append("\n")
                                        .append("Availability Impact: ").append(availabilityImpact);

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