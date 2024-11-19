package com.example.testubi2;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.testubi2.ubidots.ApiClient;
import com.example.testubi2.ubidots.Value;
import com.example.testubi2.ubidots.Variable;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

public class AdminActivity extends Activity {
    private TextView tempConf;
    private TextView humConf;
    private TextView luzConf;
    private TextView contConf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        tempConf = findViewById(R.id.tempConf);
        humConf = findViewById(R.id.humConf);
        luzConf = findViewById(R.id.luzConf);
        contConf = findViewById(R.id.contConf);
        Button confButton = findViewById(R.id.confButton);


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("limites").document("limites").get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, Object> data = documentSnapshot.getData();
                    if (data != null) {
                        tempConf.setText(data.get("temperatura").toString());
                        humConf.setText(data.get("humedad").toString());
                        luzConf.setText(data.get("luz").toString());
                        contConf.setText(data.get("personas").toString());
                    }
                });

        confButton
                .setOnClickListener(view -> {
                    // Set the new values on the Firestore table "limites"
                    // There is only one record that contains the four measures
                    // Check that every field is filled and is a number

                    if (tempConf.getText().toString().isEmpty() ||
                            humConf.getText().toString().isEmpty() ||
                            luzConf.getText().toString().isEmpty() ||
                            contConf.getText().toString().isEmpty()) {
                        return;
                    }
                    if (!tempConf.getText().toString().matches("-?\\d+(\\.\\d+)?")) {
                        return;
                    }
                    if (!humConf.getText().toString().matches("-?\\d+(\\.\\d+)?")) {
                        return;
                    }
                    if (!luzConf.getText().toString().matches("-?\\d+(\\.\\d+)?")) {
                        return;
                    }
                    if (!contConf.getText().toString().matches("-?\\d+(\\.\\d+)?")) {
                        return;
                    }
                    Map<String, Object> data = new HashMap<>();
                    data.put("temperatura", tempConf.getText().toString());
                    data.put("humedad", humConf.getText().toString());
                    data.put("luz", luzConf.getText().toString());
                    data.put("personas", contConf.getText().toString());
                    db.collection("limites").document("limites").set(data);
                });
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}