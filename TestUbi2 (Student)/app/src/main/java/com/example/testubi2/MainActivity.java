package com.example.testubi2;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.example.testubi2.ubidots.ApiClient;
import com.example.testubi2.ubidots.Value;
import com.example.testubi2.ubidots.Variable;
import com.google.gson.internal.LinkedTreeMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    private TextView temperatura;
    private TextView humedad;
    private ImageView tempImg;
    private Boolean active;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        temperatura = findViewById(R.id.temperatura);
        humedad = findViewById(R.id.humedad);
        tempImg = findViewById(R.id.tempImg);
        active = true;
        new ApiUbidots().execute();

    }



    @Override
    protected void onStop() {

        super.onStop();
        active = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
        new ApiUbidots().execute();
    }


    public class ApiUbidots extends AsyncTask<Integer, ProgressUpdate, Void> {
        private final String API_KEY_UBIDOTS = "BBUS-628c3827b7bd2729999bc33613af3378257";
        private final String TempVariable_ID = "67297f7b87a2979b04503b60";
        private final String HumVariable_ID = "67297f919bcbc9a2227b83c2";
        private final String LigthVariable_ID = "67297fbdb869fe9b669d1fba";
        private final String CounterVariable_ID = "6738cd8fbe6e5025553f0c31";
        private final String API_BASE_URL = "http://industrial.api.ubidots.com/api/v1.6/";


        @Override
        protected Void doInBackground(Integer... params) {
            while(active){
                try {
                    ApiClient apiClient = new ApiClient(API_KEY_UBIDOTS, API_BASE_URL); //API_KEY de Ubidots
                    Variable temperatura = apiClient.getVariable(TempVariable_ID); //Obtener referencia a la variable de temperatura con su ID
                    Variable humedad = apiClient.getVariable(HumVariable_ID); //Obtener referencia a la variable de humedad con su ID
                    Variable luz = apiClient.getVariable(LigthVariable_ID); //Obtener referencia a la variable de luz con su ID
                    Variable contador = apiClient.getVariable(CounterVariable_ID); //Obtener referencia a la variable de contador con su ID

                    String tempLastValue = temperatura.getLastValue();
                    String humLastValue = humedad.getLastValue();
                    String luzLastValue = luz.getLastValue();
                    String contLastValue = contador.getLastValue();

                    Log.wtf("UBIDOTS", tempLastValue);
                    Log.wtf("UBIDOTS", humLastValue);
                    Log.wtf("UBIDOTS", luzLastValue);
                    Log.wtf("UBIDOTS", contLastValue);

                    publishProgress(new ProgressUpdate(tempLastValue, humLastValue)); //actualizar UI con valores actuales
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(ProgressUpdate... values) { //método para actualizar UI
            super.onProgressUpdate(values);
            temperatura.setText(values[0].temperatura);
            humedad.setText(values[0].humedad);
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            new ApiUbidots().execute();
        }
    }

    class ProgressUpdate {
        public final String temperatura;
        public final String humedad;

        public ProgressUpdate(String temperatura, String humedad) {
            this.temperatura = temperatura;
            this.humedad = humedad;

            if(Double.parseDouble(temperatura) < 24 && temperatura!= null){
                tempImg.setColorFilter(Color.GREEN);
            }else{
                tempImg.setColorFilter(Color.RED);
            }
        }
    }
}