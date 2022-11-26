package com.encoding.es_movil;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.encoding.es_movil.Models.DniModels;
import com.encoding.es_movil.Models.GroupModels;
import com.encoding.es_movil.Models.SalonModels;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    //CONTROLES
    Button btnScanner;
    Spinner spinCedula;
    Spinner spinGrupo;
    Spinner spinSalon;
    ImageView imgValidar;

    //VARIABLES GLOBALES
    String BaseURl = "http://192.168.0.7:8000/es_api/vistas/";
    String id_profesor;
    String id_grupo;
    String id_salon;

    //LISTA PARA TRAER LAS CEDULAS DEL PROFESOR
    List<DniModels> dniList = new ArrayList<>();
    List<String> dniList2 = new ArrayList<>();

    //LISTA PARA TRAER LOS GRUPOS DEL PROFESOR
    List<GroupModels> groupList = new ArrayList<>();
    List<String> groupList2 = new ArrayList<>();

    //LISTA PARA TRAER LOS GRUPOS DEL PROFESOR
    List<SalonModels> salonList = new ArrayList<>();
    List<String> salonList2 = new ArrayList<>();

    //LISTA PARA GUARDAR LA INFORMACION DEL ESTUDIANTE LEIDO
    List<String> estudianteList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Inicializamos controles
        InitializerControls();
        //Entramos al scanner
        ScannerDNI();
        //Traemos la informacion del profesor
        GetDNI();
        //Traemos el ID el profesor
        GetId();
        //Traemos el ID del grupo
        GetIdGroup();
        //Traemos el ID del salon
        GetIdSalon();
    }

    private void InitializerControls() {
        btnScanner = (Button) findViewById(R.id.btnEscanear);
        spinCedula = (Spinner) findViewById(R.id.spinCedula);
        spinGrupo = (Spinner) findViewById(R.id.spinGrupo);
        spinSalon = (Spinner) findViewById(R.id.spinSalon);
        imgValidar = (ImageView) findViewById(R.id.imgValidar);
    }

    private void ScannerDNI() {
        btnScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                estudianteList.clear();
                IntentIntegrator intentIntegrator = new IntentIntegrator(MainActivity.this);
                intentIntegrator.setOrientationLocked(false);
                intentIntegrator.setBarcodeImageEnabled(true);
                intentIntegrator.setBeepEnabled(true);
                intentIntegrator.setCameraId(0);
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                intentIntegrator.initiateScan();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() != null) {
                //GET DNI information from API
                String cedula = intentResult.getContents();
                GetEstudiante(cedula);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //Traemos la Cedula de los profesores
    private void GetDNI() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(BaseURl + "profesor.php", new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject jsonObject = response.getJSONObject(i);

                        dniList.add(new DniModels(jsonObject.getString("id"), jsonObject.getString("cedula")));
                        dniList2.add(jsonObject.getString("cedula"));
                    }

                    spinCedula.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_spinner, dniList2));
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "ERROR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "VOLLEY ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        jsonArrayRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                Toast.makeText(MainActivity.this, "VOLLEY RETRY ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonArrayRequest);
    }

    //Guardamos el ID del profesor seleccionado
    private void GetId() {
        spinCedula.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                groupList2.clear();
                salonList2.clear();
                salonList.clear();
                groupList.clear();
                id_profesor = dniList.get(i).getId();
                GetGroup(dniList.get(i).getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    //Segun el profesor traeremos los grupos
    private void GetGroup(String id_profesor) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(BaseURl + "grupo.php?id=" + id_profesor, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject jsonObject = response.getJSONObject(i);

                        groupList.add(new GroupModels(jsonObject.getString("id"), jsonObject.getString("grupo")));
                        groupList2.add(jsonObject.getString("grupo"));
                    }

                    spinGrupo.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_spinner, groupList2));
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "ERROR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "VOLLEY ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        jsonArrayRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                Toast.makeText(MainActivity.this, "VOLLEY RETRY ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonArrayRequest);
    }

    //Guardamos el ID del Grupo seleccionado
    private void GetIdGroup() {
        spinGrupo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                salonList.clear();
                salonList2.clear();
                GetSalon(groupList.get(i).getId());
                id_grupo = groupList.get(i).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    //Segun el profesor traeremos los grupos
    private void GetSalon(String id_grupo) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(BaseURl + "salones.php?id=" + id_profesor + "&id_grupo=" + id_grupo, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject jsonObject = response.getJSONObject(i);

                        salonList.add(new SalonModels(jsonObject.getString("id_horario"), jsonObject.getString("salon")));
                        salonList2.add(jsonObject.getString("salon"));
                    }
                    spinSalon.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_spinner, salonList2));
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "ERROR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "VOLLEY ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        jsonArrayRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                Toast.makeText(MainActivity.this, "VOLLEY RETRY ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonArrayRequest);
    }

    //Segun el salon seleccionado traemos el ID
    private void GetIdSalon() {
        spinSalon.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                id_salon = salonList.get(i).getId();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    //Validamos que el estudiante pertenezca a los campos seleccionados anteriormente
    private void GetEstudiante(String cedula) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(BaseURl + "estudiante.php?cedula=" + cedula + "&id_grupo=" + id_grupo, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject jsonObject = response.getJSONObject(i);

                        estudianteList.add(jsonObject.getString("id"));

                        imgValidar.setColorFilter(Color.parseColor("#22bb33"));
                        //Agregamos a los estudiantes al sistema de asistencia
                        AddStudent();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "El estudiante no pertenece a este grupo", Toast.LENGTH_LONG).show();
                imgValidar.setColorFilter(Color.parseColor("#bb2124"));
            }
        });

        jsonArrayRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                Toast.makeText(MainActivity.this, "VOLLEY RETRY ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonArrayRequest);
    }

    private void AddStudent() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, BaseURl + "asistencia.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(MainActivity.this, "Estudiante ingresado", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "ERROR ADD STUDENT" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("id_profesor", id_profesor);
                parametros.put("id_estudiante", estudianteList.get(0));
                parametros.put("id_horario", id_salon);
                parametros.put("id_grupo", id_grupo);
                parametros.put("asistencia", "asistio.png");
                parametros.put("porcentaje", "0.5");

                return parametros;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}