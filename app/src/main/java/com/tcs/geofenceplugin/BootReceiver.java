package com.tcs.geofenceplugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by kaustav on 10/15/2015.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override

    public void onReceive(Context context, Intent intent) {
        SharedPreferences sd=context.getSharedPreferences("GCMID",Context.MODE_PRIVATE);

        String gcmid=sd.getString("gcmid", "");
        Intent i = new Intent(context, BackgroundService.class);
        i.putExtra("gcmid", gcmid);
        context.startService(i);


    }
}
