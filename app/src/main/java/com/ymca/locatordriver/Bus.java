package com.ymca.locatordriver;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.List;


public class Bus extends AppCompatActivity {
    String busnum="null";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.bus);
        if(isRegistered()){
            Intent i=new Intent(getApplicationContext(),Display.class);
            startActivity(i);
            finish();
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        List<String> list = new ArrayList<String>();
        list.add("Select Bus Number");
        list.add("7620: Sector 9,Sector 10 to College");
        list.add("4297: Baldev Nagar to College");
        list.add("1185: Defence Colony to College");
        list.add("1195: Kharga Canteen,Babyal,Boh to College");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                (Bus.this, R.layout.spinner_item, list);

        dataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                switch (position){
                    case 0:
                        busnum="0";
                        break;
                    case 1:
                        busnum="7620";
                        break;
                    case 2:
                        busnum="4297";
                        break;
                    case 3:
                        busnum="1185";
                        break;
                    case 4:
                        busnum ="1195";
                        break;
                }
                if(busnum=="null"||busnum=="0"){
                    Toast.makeText(Bus.this, "Select valid Bus Number", Toast.LENGTH_SHORT).show();
                }
                else {
                    SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("Busnum", busnum);
                    editor.putBoolean(Constants.REGISTERED, true);
                    editor.apply();
                    Intent i=new Intent(Bus.this,Display.class);
                    startActivity(i);
                    finish();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }
    private boolean isRegistered() {
        //Getting shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE);

        //Getting the value from shared preferences
        //The second parameter is the default value
        //if there is no value in sharedpreference then it will return false
        //that means the device is not registered
        return sharedPreferences.getBoolean(Constants.REGISTERED, false);
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {


                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);



            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

            }
            return false;
        } else {
            return true;
        }
    }


    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        // PERMISSION GRANTED
                    } else {
                        finish();
                    }
                }
            }
    );

}



