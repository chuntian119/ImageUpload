package com.codephillip.app.imageupload;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import static android.content.Context.ALARM_SERVICE;

public class LocationReceiver extends BroadcastReceiver {
    private static final int REQUEST_CODE = 423;
    private final String TAG = LocationReceiver.class.getSimpleName();

    /**
     * LocationReceiver is triggered when the phone is booted.
     * Then starts the alarm mana
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: STARTED");
        //todo grab the values from the intent sent by the LocationService and pass them to this new intent
//        Intent mainIntent = new Intent(context, LocationChangedActivity.class);
//        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(mainIntent);
        startLocationServiceAlarm(context);
    }

    private void startLocationServiceAlarm(Context context) {
        Intent intent = new Intent(context, LocationService.class);
        PendingIntent pIntent = PendingIntent.getService(context, REQUEST_CODE,
                intent, 0);

        int alarmType = AlarmManager.ELAPSED_REALTIME;
        final int FIFTEEN_SEC_MILLIS = 15000;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        // setRepeating takes a start delay and period between alarms as arguments.
        // Fires after 5 mins, and repeats every 6 hrs.  This is very
        alarmManager.setRepeating(alarmType, SystemClock.elapsedRealtime() + FIFTEEN_SEC_MILLIS,
                FIFTEEN_SEC_MILLIS, pIntent);
        Log.d("RepeatingAlarmFragment", "Alarm set.");
    }
}
