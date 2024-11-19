package com.example.testubi2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList

class DevicesActivity : AppCompatActivity() {
    lateinit var deviceAdapter: DeviceAdapter
    lateinit var rvDevice: RecyclerView
    lateinit var deviceManager: RecyclerView.LayoutManager
    lateinit var logout_btn : Button
    lateinit var deviceCard : CardView
    lateinit var adminButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devices)

        val devices = ArrayList<DeviceClass>()

        devices.add(DeviceClass("Temp", "thermometer"))
        devices.add(DeviceClass("Humedad", "humiditysensor"))
        devices.add(DeviceClass("Luz", "idea"))
        devices.add(DeviceClass("Personas", "user"))

        rvDevice = findViewById(R.id.rv_device)
        deviceAdapter = DeviceAdapter(devices)
        deviceManager = GridLayoutManager(this,2)
        rvDevice.adapter = deviceAdapter
        rvDevice.layoutManager = deviceManager


        logout_btn = findViewById(R.id.logout_btn)

        adminButton = findViewById(R.id.adminButton)
        adminButton.visibility = View.GONE
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            FirebaseFirestore.getInstance().collection("admins")
                .whereEqualTo("email", user.email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        // User is an admin
                        adminButton.visibility = View.VISIBLE
                    }
                }
        }

        adminButton.setOnClickListener{
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        }

        logout_btn.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}