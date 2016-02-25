package com.tcs.geofenceplugin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by kaustav on 10/19/2015.
 */
public class NewInitialiser {
    private GoogleCloudMessaging gcm;
    String regid = "";
    String msg;
    String tokenid="";
    int noOfAttemptsAllowed = 1;   // Number of Retries allowed
    int noOfAttempts = 0;          // Number of tries done
    boolean stopFetching = false;     // Flag to denote if it has to be retried or not
    String regId = "";
    final String id = "851400602776";

    String Tag = "komal1";
    Context c;

    public NewInitialiser(Context c) {
        this.c = c;
        Log.d(Tag,"In newInitializer");
        SharedPreferences sd = c.getSharedPreferences(Tag, Context.MODE_PRIVATE);

        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset());
            tokenid= obj.getString("tokenID");
            Log.d(Tag,tokenid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (sd.getBoolean("status", false) == true) {
            msg = sd.getString("gcmid", "blabla");

            Intent i = new Intent(c, BackgroundService.class);
            i.putExtra("gcmid", msg);
            i.putExtra("tokenid",tokenid);
            c.startService(i);


        } else {
            Log.d(Tag,"GCMInitilization");
            Gcminitialisation();

        }

    }

    void Initialiser(String msg) {
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL("https://apphonics.tcs.com/geofence/FinalizeRegistration?tokenID="+tokenid+"&gcmID="+msg);
            //URL url = new URL("http://130.136.1.65:8080/Geofence1/FinalizeRegistration?tokenID="+tokenid+"&gcmID="+msg);
            Log.d(Tag,url.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();
            Log.d(Tag, "Response code:" + responseCode);
            Toast.makeText(c, responseCode, Toast.LENGTH_LONG).show();
            if (responseCode == 201) {

                Toast.makeText(c, "Regestered to Server Successfully", Toast.LENGTH_LONG).show();

                //response = new JSONArray(responseString);
            } else {
                Toast.makeText(c, "Server Not responding", Toast.LENGTH_LONG).show();


            }
            Log.d(Tag, "Response code:" + responseCode);


        } catch (Exception e) {
            e.printStackTrace();
            if (urlConnection != null)
                urlConnection.disconnect();
        }


    }

    private void Gcminitialisation() {
        Log.d(Tag,"In while");
        while (!stopFetching) {
            noOfAttempts++;

            try {
                // Leave some time here for the register to be
                // registered before going to the next line
                Thread.sleep(2000);   // Set this timing based on trial.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                // Get the registration ID
                getRegId();

            } catch (Exception e) {
                msg = e.getMessage();
            }


            if (!regid.isEmpty() || noOfAttempts >= noOfAttemptsAllowed) {
                // If registration ID obtained or No Of tries exceeded, stop fetching
                stopFetching = true;
            }
            if (!regid.isEmpty()) {
                // If registration ID Obtained, save to shared preferences
                Log.i(Tag, msg);
            }

        }
    }


    public void getRegId() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(c);
                    }
                    System.out.print("In new initializes asyn");
                    regid = gcm.register(id);
                    msg = regid;
                    Initialiser(msg);

                    SharedPreferences sd = c.getSharedPreferences(Tag, Context.MODE_PRIVATE);
                    SharedPreferences.Editor e = sd.edit();
                    e.putString("gcmid", msg);
                    e.putBoolean("status", true);
                    e.commit();
                    Intent i = new Intent(c, BackgroundService.class);
                    i.putExtra("gcmid", msg);
                    c.startService(i);
                    Log.d(Tag, msg)   ;

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();

                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {


            }
        }.execute(null, null, null);
    }


    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = c.getAssets().open("token.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

}
