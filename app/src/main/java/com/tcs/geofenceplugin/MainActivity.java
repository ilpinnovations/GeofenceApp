package com.tcs.geofenceplugin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.gcm.GoogleCloudMessaging;


import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private LocationManager locationManager;
    private String provider;
    double lat, lng;

    private GoogleCloudMessaging gcm;
    String regid = "";
    String msg;
    String TAG = "Komal9";
    int noOfAttemptsAllowed = 25;   // Number of Retries allowed
    int noOfAttempts = 0;          // Number of tries done
    boolean stopFetching = false;     // Flag to denote if it has to be retried or not
    String regId = "";
    final String id = "851400602776";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NewInitialiser n = new NewInitialiser(this);

    }


    public void getRegId() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                Log.d("komal","getregid");
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(id);
                    Log.d(TAG, regid);
                    msg = "Device registered, registration ID=" + regid;

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                Log.d(TAG, msg);

                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        }.execute(null, null, null);
    }


}


