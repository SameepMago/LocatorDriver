package com.ymca.locatordriver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;


public class Splash extends Activity {
    private GoogleApiClient googleApiClient;

    final static int REQUEST_LOCATION = 199;
    final Context context = this;
    protected void onCreate(Bundle SavedInstanceState){
        super.onCreate(SavedInstanceState);
        if(noLocation()) {
            noLocation();
        }
        else {
            CountDownTimer countDownTimer=new CountDownTimer(3000,1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Log.i("prog","timer progress");
                }

                @Override
                public void onFinish() {
                    Intent i=new Intent(Splash.this,Bus.class);
                    startActivity(i);
                    finish();
                }
            }.start();

        }
        setContentView(R.layout.splash);
    }
    public boolean noLocation() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //  buildAlertMessageNoGps();

            enableLoc();
            return true;
        }
        return false;

    }


    private void enableLoc() {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                        }
                    }).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(
                                        (Activity) context, REQUEST_LOCATION);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                    }
                }
            });

        }
    }


        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            switch (requestCode) {
                case REQUEST_LOCATION:
                    switch (resultCode) {
                        case Activity.RESULT_CANCELED: {
                            // The user was asked to change settings, but chose not to
                            finish();
                            break;
                        }
                        default: {

                            CountDownTimer countDownTimer=new CountDownTimer(3000,1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    Log.i("prog","timer progress");
                                }

                                @Override
                                public void onFinish() {
                                    Intent i=new Intent(Splash.this,Bus.class);
                                    startActivity(i);
                                    finish();
                                }
                            }.start();
                        }
                    }
                    break;
            }

        }

    }
