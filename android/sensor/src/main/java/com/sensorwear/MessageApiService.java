package com.sensorwear;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

/**
 *
 */
public class MessageApiService extends WearableListenerService {

    private static final String TAG = MessageApiService.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        final ConnectionResult connectionResult = mGoogleApiClient
                .blockingConnect(10, TimeUnit.SECONDS);
        if (!connectionResult.isSuccess()) {
            Log.i(TAG, "couldn't connect");
            return;
        }

        final String path = messageEvent.getPath();
        switch (path) {
            case "/hello":
                Toast.makeText(this, new String(messageEvent.getData()), Toast.LENGTH_SHORT).show();
                break;
            default:
                Log.i(TAG, "message for path " + path + " not handled");
                break;
        }
        mGoogleApiClient.disconnect();
    }
}
