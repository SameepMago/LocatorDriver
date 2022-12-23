package com.ymca.locatordriver;


import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;


public class LocationMonitoringService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = LocationMonitoringService.class.getSimpleName();
    GoogleApiClient mLocationClient;
    LocationRequest mLocationRequest = new LocationRequest();

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;


    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private DatabaseReference mDatabase;
    protected Location mCurrentLocation;

    protected String mLastUpdateTime;
    private float maxspeed;
    private static final String LOG_TAG = "ForegroundService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE);
        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        int priority = LocationRequest.PRIORITY_HIGH_ACCURACY; //by default
        //PRIORITY_BALANCED_POWER_ACCURACY, PRIORITY_LOW_POWER, PRIORITY_NO_POWER are the other priority modes


        mLocationRequest.setPriority(priority);
        mLocationClient.connect();
        Log.i(LOG_TAG, "Received Start Foreground Intent ");
        Intent notificationIntent = new Intent(this, Display.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Locator" + "\n" + sharedPreferences.getString("Busnum", ""))
                .setTicker("Locator")
                .setContentText("Location service running.....")
                .setSmallIcon(R.drawable.main)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notification);


        //Make it stick to the notification panel so it is less prone to get cancelled by the Operating System.
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
     * LOCATION CALLBACKS
     */
    @Override
    public void onConnected(Bundle dataBundle) {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);
        }
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
        mLastUpdateTime  = DateFormat.getTimeInstance().format(new Date());
        if(mCurrentLocation!=null) {
            if (mCurrentLocation.getSpeed() > maxspeed) {
                maxspeed = mCurrentLocation.getSpeed();

            }
            mDatabase = FirebaseDatabase.getInstance().getReference();
            SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE);
            mDatabase.child("Busnum").child(sharedPreferences.getString("Busnum","")).child("latitude").setValue(mCurrentLocation.getLatitude());
            mDatabase.child("Busnum").child(sharedPreferences.getString("Busnum","")).child("longitude").setValue(mCurrentLocation.getLongitude());
            mDatabase.child("Busnum").child(sharedPreferences.getString("Busnum","")).child("maxspeed").setValue(maxspeed);
        }


        Log.d(TAG, "Connected to Google API");
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended");
    }

    //to get the location change
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed");


        if (location != null) {
            Log.d(TAG, "== location != null");
            mCurrentLocation = location;
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

            if(mCurrentLocation.getSpeed()>maxspeed){
                maxspeed=mCurrentLocation.getSpeed();
            }
            mDatabase = FirebaseDatabase.getInstance().getReference();
            SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE);
            mDatabase.child("Busnum").child(sharedPreferences.getString("Busnum","")).child("latitude").setValue(mCurrentLocation.getLatitude());
            mDatabase.child("Busnum").child(sharedPreferences.getString("Busnum","")).child("longitude").setValue(mCurrentLocation.getLongitude());
            mDatabase.child("Busnum").child(sharedPreferences.getString("Busnum","")).child("maxspeed").setValue(maxspeed);
        }


    }




    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Failed to connect to Google API");

    }




    public void onDestroy() {
        Log.d("BusApp","Stopped\n\n");
        Intent intent = new Intent("com.android.techtrainner");
        intent.putExtra("yourvalue", "torestore");
        sendBroadcast(intent);
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopForeground(true);
        PendingIntent service = PendingIntent.getService(
                getApplicationContext(),
                1001,
                new Intent(getApplicationContext(), LocationMonitoringService.class),
                PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, service);
        Log.e("ClearFromRecentService", "END");
        super.onTaskRemoved(rootIntent);
    }


}