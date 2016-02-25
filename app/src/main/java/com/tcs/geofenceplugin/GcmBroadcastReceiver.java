package com.tcs.geofenceplugin;

/**
 * Created by komal on 12/19/2015.
 */

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    String Tag="komal5";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Tag, "In broadcast receiver");

        SharedPreferences sd=context.getSharedPreferences("GCMID",Context.MODE_PRIVATE);
        String gcmid=sd.getString("gcmid", " ");
        Intent i = new Intent(context, BackgroundService.class);
        i.putExtra("gcmid", gcmid);
        Log.d(Tag, "In broadcast receiver2");


        ComponentName comp = new ComponentName(context.getPackageName(),
                GcmIntentService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        context.startService(i);
        setResultCode(Activity.RESULT_OK);
    }
}