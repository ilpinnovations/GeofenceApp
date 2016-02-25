/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tcs.geofenceplugin;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {

    protected static final String TAG = "komal3";
    String gcmid="";
    String tokenid="";
    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
        Log.d(TAG,"In super");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG,"In create");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Handling intent");
        gcmid=intent.getStringExtra("gcmid");
        tokenid=intent.getStringExtra("tokenid");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.d(TAG, errorMessage);
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            Log.d(TAG, "recieved");
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL("https://apphonics.tcs.com/geofence/Analytics?tokenid="+tokenid);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                int responseCode = urlConnection.getResponseCode();
                if(responseCode==200){
                    Log.d(TAG,"Analytics data entered");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

             getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

        } else {
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }

    }

    private void getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        ArrayList triggeringGeofencesIdsList = new ArrayList();
        Handler mHandler = new Handler();
            for (Geofence geofence : triggeringGeofences) {

                String geofenceTransitionString = getTransitionString(geofenceTransition);
                geofenceTransitionString+=geofence.getRequestId();
                URL url;
                HttpURLConnection urlConnection = null;
                JSONArray response = new JSONArray();
                String notificationtext="";
                String place="";
                String startdate="";
                String enddate="";
                JSONObject obj=new JSONObject();
                try {
                    int restid=Integer.parseInt(geofence.getRequestId());
                    url = new URL("https://apphonics.tcs.com/geofence/SelectTrigger?triggerid="+restid);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");

                    urlConnection.connect();
                    int responseCode = urlConnection.getResponseCode();
                    Log.d("Komal3",responseCode+"");

                    if(responseCode == 200){



                        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"utf-8"));
                        StringBuilder sb = new StringBuilder();
                        try {

                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                sb.append(line + "\n");
                            }

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


                        obj= new JSONObject(sb.toString());
                        JSONArray arr=obj.getJSONArray("notificationdata");

                        for(int i=0; i<arr.length(); i++) {
                            JSONObject O=arr.getJSONObject(i);
                               notificationtext=O.getString("notification_text");
                            place=O.getString("place");
                            startdate=O.getString("startdate");
                            enddate=O.getString("enddate");
                        }
                        //response = new JSONArray(responseString);
                    }else{
                        Log.d(TAG, "Response code:"+ responseCode);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if(urlConnection != null)
                        urlConnection.disconnect();
                }





                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat format2=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date datestart,dateend;
                try {
                    datestart = format.parse(startdate);
                    dateend=format2.parse(enddate);
                    if(datestart.compareTo(new Date())<=0 && dateend.compareTo(new Date())>=0) {
                        sendNotification(geofenceTransitionString, notificationtext, place);
                    }

                    mHandler.postDelayed(new Runnable() {
                        public void run() {

                        }
                    }, 5000);


                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }

        }

    private void sendNotification(String notificationDetails,String contenttext,String place) {
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent =
        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationDetails = notificationDetails.substring(0, notificationDetails.length()-1);



         Notification n  = new Notification.Builder(this)
                .setContentTitle("Entered :"+place)
                .setContentText(contenttext)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL).build();


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, n);



    }


    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }

}
