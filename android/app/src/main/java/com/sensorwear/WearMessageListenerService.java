package com.sensorwear;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import com.sensorwear.MainActivity;

/**
 *
 */
public class WearMessageListenerService extends WearableListenerService {
    private static final String START_ACTIVITY = "/increase_phone_counter";
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i("Sensor", "received message: " + messageEvent.getPath());
        if( messageEvent.getPath().equalsIgnoreCase( START_ACTIVITY ) ) {
            Intent intent = new Intent( this, MainActivity.class );
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity( intent );
        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}

