package com.tcs.geofenceplugin;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class BackgroundService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    GoogleApiClient mGoogleApiClient;
    ArrayList<Geofence> mGeofenceList;
    boolean mGeofencesAdded;
    PendingIntent mGeofencePendingIntent=null;
    SharedPreferences mSharedPreferences;
    String gcmid="";
    String tokenid="";
    String Tag="komal2";
    public BackgroundService() {
    }



    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Tag,"In start");
        //intent=get
        gcmid=intent.getStringExtra("gcmid");
        tokenid=intent.getStringExtra("tokenid");
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            Log.d(Tag,"Google play services there");

        }else
        { Log.d(Tag, "unable to connect to google play services.");

        }
        mGeofenceList = new ArrayList<Geofence>();
        mGeofencePendingIntent = null;

        MyAsyncTask task = new MyAsyncTask();
        task.execute("https://apphonics.tcs.com/geofence/TriggerListToJSON?tokenID=" + tokenid);

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d(Tag,"In bind");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }


    private void logSecurityException(SecurityException securityException) {
        Log.e(Tag, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }


    public void populateGeofenceList(ArrayList<Geolocation> geo) {

        ArrayList<Geolocation> arrayList = new ArrayList<Geolocation>();
        arrayList = geo;


        for (Geolocation g : arrayList) {
            Random r = new Random();
            Log.d(Tag,"Populate geofence list");
            mGeofenceList.add(new Geofence.Builder()
                    .setRequestId(String.valueOf(g.getTriggerID()))
                    .setCircularRegion(
                            g.getLatitude(),
                            g.getLongitude(),
                            g.getRadius()
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
        }
    }


    private PendingIntent getGeofencePendingIntent() {
        Log.d(Tag, "In Geofence Pending Intent");

        Intent mintent = new Intent(this, GeofenceTransitionsIntentService.class);
        mintent.putExtra("gcmid",gcmid);
        mintent.putExtra("tokenid",tokenid);
        Log.d(Tag, "Geofence Transition intent service class Intent");
        mGeofencePendingIntent=PendingIntent.getService(this, 0, mintent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(Tag, "In onConnected");

        SharedPreferences sd = getApplicationContext().getSharedPreferences("userrun", Context.MODE_PRIVATE);
        int runtime = sd.getInt("run", 0);
        Intent intent = new Intent(BackgroundService.this, GeofenceTransitionsIntentService.class);
        Log.d(Tag, "Geofence Transition intent service class Intent");
        mGeofencePendingIntent=PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (runtime == 0) {
            SharedPreferences sd1 = getApplicationContext().getSharedPreferences("userrun", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sd1.edit();
            Log.d(Tag,"In shared preferences edit");
            edit.putInt("run", 1);
            edit.commit();

            try {

                LocationServices.GeofencingApi.addGeofences(
                        mGoogleApiClient,
                        getGeofencingRequest(),
                        getGeofencePendingIntent()
                ).setResultCallback(this);

                Log.d("thrown", "ok1");

                // Result processed in onResult().
            } catch (SecurityException securityException) {
                // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                logSecurityException(securityException);
            }
        } else {

            try {
                LocationServices.GeofencingApi.removeGeofences(
                        mGoogleApiClient,
                        // This is the same pending intent that was used in addGeofences().
                        getGeofencePendingIntent()
                ).setResultCallback(this); // Result processed in onResult().
            } catch (SecurityException securityException) {
                // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                logSecurityException(securityException);
            }


            try {
                LocationServices.GeofencingApi.addGeofences(
                        mGoogleApiClient,
                        getGeofencingRequest(),
                        getGeofencePendingIntent()
                ).setResultCallback(this);
                boolean val=getGeofencePendingIntent()==null;
                Log.d(Tag, "ok2");

                // Result processed in onResult().
            } catch (SecurityException securityException) {
                securityException.printStackTrace();
                logSecurityException(securityException);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(Tag, "Connection suspended");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(Tag, "Connection failed");

    }

    @Override
    public void onResult(Status status) {
        Boolean STATUS=status.isSuccess();
        if (status.isSuccess()) {
            Toast.makeText(
                    getApplicationContext(),
                    "Geofence Added",
                    Toast.LENGTH_LONG
            ).show();
            Log.d(Tag, "Toast added");
        } else {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    status.getStatusCode());
            Log.d(Tag, errorMessage);
        }
    }
    private class MyAsyncTask extends AsyncTask<String,Void,JSONObject> {
        ProgressDialog p;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(Tag,"In pre");

        }

        @Override
        protected JSONObject doInBackground(String... params) {

            Log.d(Tag, "In background");

            URL url;
            HttpURLConnection urlConnection = null;
            JSONArray response = new JSONArray();

            JSONObject obj = new JSONObject();
            try {
                url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                urlConnection.connect();
                int responseCode = urlConnection.getResponseCode();
                Log.d(Tag,responseCode+"");

                if (responseCode == 200) {

                    InputStream i = urlConnection.getInputStream();
                    String responseString = readStream(urlConnection.getInputStream());

                    Log.d(Tag, responseString);

                    obj = new JSONObject(responseString);
                    //response = new JSONArray(responseString);
                } else {
                    Log.d(Tag, "Response code:" + responseCode);
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (urlConnection != null)
                    urlConnection.disconnect();
            }

            return obj;


        }

        @Override
        protected void onPostExecute(JSONObject obj) {
            super.onPostExecute(obj);

            ArrayList<Geolocation> geoar = new ArrayList<Geolocation>();

            try {
                JSONArray arr = obj.getJSONArray("triggerData");
                Log.d(Tag,arr.toString());
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject O = arr.getJSONObject(i);
                    Geolocation g = new Geolocation(O.getInt("triggerID"), O.getInt("userID"), O.getString("tokenID"), O.getString("place"), O.getDouble("latitude"), O.getDouble("longitude"), O.getInt("radius"), O.getString("notificationText"), O.getString("startDate"), O.getString("endDate"), O.getString("status"),O.getInt("expires"));
                    geoar.add(g);
                }

                populateGeofenceList(geoar);

                if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
                    try {
                        mGoogleApiClient.connect();
                        Log.d(Tag,"mGoogleApiClient is now connected");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }else{
                    Log.d(Tag,"mGoogleApiClient is not connected");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
        private String readStream(InputStream in) throws UnsupportedEncodingException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
            StringBuilder sb = new StringBuilder();
            try {

                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return sb.toString();
        }
    }
    }
