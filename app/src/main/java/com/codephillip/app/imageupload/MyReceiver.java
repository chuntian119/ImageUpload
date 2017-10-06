package com.codephillip.app.imageupload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {
    private final String TAG = MyReceiver.class.getSimpleName();

    /**
     * Broadcast received, indicating
     * that the LocationService has finished getting a new location.
     * Then start the on LocationChangedActivity
     * */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: STARTED");
        //todo grab the values from the intent sent by the LocationService and pass them to this new intent
        Intent mainIntent = new Intent(context, LocationChangedActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mainIntent);
    }
}
