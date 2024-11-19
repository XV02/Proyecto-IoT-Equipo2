package com.example.testubi2;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.testubi2.ubidots.ApiClient;
import com.example.testubi2.ubidots.Value;
import com.example.testubi2.ubidots.Variable;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

public class MainActivity extends Activity {
    private TextView mainValue;
    private ImageView mainImage;
    private TextView mainVarTitle;
    private LineChartView lineChart;
    private Boolean active;
    private String variableToDisplay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainValue = findViewById(R.id.mainValue);
        mainImage = findViewById(R.id.mainImg);
        mainVarTitle = findViewById(R.id.mainVarTitle);
        lineChart = findViewById(R.id.lineChart);
        active = true;

        variableToDisplay = getIntent().getStringExtra("deviceName");

        if (variableToDisplay == null || variableToDisplay.isEmpty()) {
            variableToDisplay = "Temp"; // Default to "temperatura" if no variable is passed
        }

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
        private final String API_KEY_UBIDOTS = "BBUS-af2a914720bfaccf3fbb003d6xxxxxxx";
        private final String API_BASE_URL = "http://industrial.api.ubidots.com/api/v1.6/";
        private final Map<String, String> variableIds = new HashMap<>();
        private final Map<String, String> images = new HashMap<>();
        private final Map<String, String> titles = new HashMap<>();

        public ApiUbidots(){
            final String TempVariable_ID = "6733b3e9735fd8057xxxxxx";
            final String HumVariable_ID = "67297f919bcbc9a222xxxxxx";
            final String LuzVariable_ID = "67297fbdb869fe9b66xxxxxx";
            final String ContVariable_ID = "6738cd8fbe6e502555xxxxxx";
            variableIds.put("Temp", TempVariable_ID);
            variableIds.put("Humedad", HumVariable_ID);
            variableIds.put("Luz", LuzVariable_ID);
            variableIds.put("Personas", ContVariable_ID);

            images.put("Temp", "thermometer");
            images.put("Humedad", "humiditysensor");
            images.put("Luz", "idea");
            images.put("Personas", "user");

            titles.put("Temp", "Temperatura en grados Celsius");
            titles.put("Humedad", "Humedad de la habitación");
            titles.put("Luz", "Porcentaje de luz");
            titles.put("Personas", "Número de personas");
        }


        @Override
        protected Void doInBackground(Integer... params) {
            mainVarTitle.setText(titles.get(variableToDisplay));
            mainImage.setImageResource(getResources().getIdentifier(images.get(variableToDisplay), "drawable", getPackageName()));
            while(active){
                try {
                    ApiClient apiClient = new ApiClient(API_KEY_UBIDOTS, API_BASE_URL); //API_KEY de Ubidots
                    String variableId = variableIds.get(variableToDisplay); //Obtener ID de la variable a mostrar

                    if (variableId != null) {
                        Variable variable = apiClient.getVariable(variableId);
                        String variableValue = variable.getLastValue();

                        Map<String, String> customParameters = new HashMap<>();
                        String startTimestamp = String.valueOf(System.currentTimeMillis() - 5 * 60 * 1000);
                        String endTimestamp = String.valueOf(System.currentTimeMillis());
                        customParameters.put("start", startTimestamp);
                        customParameters.put("end", endTimestamp);

                        Value[] values = variable.getValues(customParameters);
                        Log.wtf("UBIDOTS", "Values: " + values.length);

                        Log.wtf("UBIDOTS", variableToDisplay + ": " + variableValue);
                        publishProgress(new ProgressUpdate(variableToDisplay, variableValue, values));
                    } else {
                        Log.e("UBIDOTS", "Invalid variable selected: " + variableToDisplay);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(ProgressUpdate... values) { // Method to update the UI
            ProgressUpdate update = values[0];
            mainValue.setText(update.value);

            if (update.values != null) {
                // Convert values to points for the Y-axis and timestamps for the X-axis
                List<PointValue> points = new ArrayList<>();
                List<AxisValue> axisValues = new ArrayList<>();

                for (int i = 0; i < update.values.length; i++) {
                    double yValue = update.values[i].getValue();
                    long timestamp = update.values[i].getTimestamp();

                    // Add Y-axis points
                    points.add(new PointValue(i, (float) yValue));

                    // Format the timestamp for display on the X-axis (e.g., "HH:mm")
                    String formattedTimestamp = new SimpleDateFormat("HH:mm").format(new Date(timestamp));
                    axisValues.add(new AxisValue(i).setLabel(formattedTimestamp));
                }

                // Create the line for the chart
                Line line = new Line(points).setColor(Color.BLUE).setCubic(false);
                List<Line> lines = new ArrayList<>();
                lines.add(line);

                // Set up the LineChartData
                LineChartData data = new LineChartData();
                data.setLines(lines);

                // Set up the X-axis with the formatted timestamps
                Axis xAxis = new Axis();
                xAxis.setValues(axisValues); // Assign the X-axis labels
                xAxis.setName("Time"); // Optional: X-axis title
                xAxis.setTextColor(Color.BLACK); // Customize axis label color
                xAxis.setTextSize(12); // Customize font size
                data.setAxisXBottom(xAxis); // Set this axis as the bottom axis

                // Set up the Y-axis (optional)
                Axis yAxis = new Axis();
                yAxis.setName("Value"); // Optional: Y-axis title
                yAxis.setTextColor(Color.BLACK);
                yAxis.setTextSize(12);
                data.setAxisYLeft(yAxis); // Set this axis as the left axis

                // Assign the data to the chart
                lineChart.setLineChartData(data);
            }
        }


        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            new ApiUbidots().execute();
        }
    }

    private List<PointValue> convertValuesToPoints(Value[] values) {
        List<PointValue> points = Collections.emptyList();
        for (int i = 0; i < values.length; i++) {
            points.add(new PointValue(i, (float) (values[i].getValue())));
        }
        return points;
    }

    class ProgressUpdate {
        public final String variableToDisplay;
        public final String value;
        public final Value[] values;

        public ProgressUpdate(String variableToDisplay, String value, Value[] values) {
            this.variableToDisplay = variableToDisplay;
            this.value = value;
            this.values = values;

            // Get the limit from the Firestore table "limites"
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            final double[] limit = new double[1]; // Array to hold the value

            db.collection("limites").document("limites").get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Map data = documentSnapshot.getData();
                        if (data != null) {
                            switch (variableToDisplay) {
                                case "Temp":
                                    limit[0] = Double.parseDouble(data.get("temperatura").toString());
                                    break;
                                case "Humedad":
                                    limit[0] = Double.parseDouble(data.get("humedad").toString());
                                    break;
                                case "Luz":
                                    limit[0] = Double.parseDouble(data.get("luz").toString());
                                    break;
                                case "Personas":
                                    limit[0] = Double.parseDouble(data.get("personas").toString());
                                    break;
                            }
                        }

                        // Ensure this logic runs after the limit is retrieved
                        if (value != null && Double.parseDouble(value) > limit[0]) {
                            mainImage.setColorFilter(Color.GREEN);
                        } else {
                            mainImage.setColorFilter(Color.RED);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle the failure here, if needed
                        Log.e("FirestoreError", "Failed to fetch limits", e);
                    });
        }
    }
}