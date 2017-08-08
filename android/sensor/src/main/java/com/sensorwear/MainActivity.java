package com.sensorwear;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.sensorwear.R;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class MainActivity extends WearableActivity
    implements
        GoogleApiClient.ConnectionCallbacks,
        MessageApi.MessageListener,
        SensorEventListener {

    private static final String TAG = "Sensor";

    private GoogleApiClient mGoogleApiClient = null;
    SensorManager mSensorManager = null;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        mGoogleApiClient = new GoogleApiClient  .Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();

        startMesurements();
        logAvailableSensors();
    }

    public void onButtonClicked(View target) {
        Log.i(TAG, "Alarm Button");
        //alert("che");
        sendMessageToHandheld("0");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    /**
     * sends a string message to the connected handheld using the google api client (if available)
     * @param message
     */
    public void sendMessageToHandheld(final String message) {

        if (mGoogleApiClient == null)
            return;

        // use the api client to send the heartbeat value to our handheld
        final PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
        nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {

            @Override
            public void onResult(NodeApi.GetConnectedNodesResult result) {
                final List<Node> nodes = result.getNodes();
                final String path = "/increase_phone_counter";

                for (Node node : nodes) {
                    Log.i(TAG, "SEND MESSAGE TO HANDHELD: " + message + " , node: " + node.getDisplayName());

                    byte[] data = message.getBytes(StandardCharsets.UTF_8);
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, data);
                }
            }
        });

    }

    @Override
    public void onConnected(final Bundle bundle) {
        Log.i(TAG, "Google Api connected");
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {

        final String path = messageEvent.getPath();
        switch (path) {
            default:
                Log.i(TAG, "Message " + path + " > " + new String(messageEvent.getData()));
                break;
        }
    }

    @Override
    public void onConnectionSuspended(final int i) {
        Log.i(TAG, "Connection suspended");
    }

    private void startMesurements() {
        mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
        Sensor mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor mSigMotion = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        TriggerListener mListener = new TriggerListener(this);
        mSensorManager.requestTriggerSensor(mListener, mSigMotion);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            String msg = "" + (int)sensorEvent.values[0];
            //Log.i(TAG, msg);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "onAccuracyChanged - accuracy: " + accuracy);
    }

    private void logAvailableSensors() {
        final List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        Log.i(TAG, "=== LIST AVAILABLE SENSORS ===");
        Log.i(TAG, String.format(Locale.getDefault(), "|%-35s|%-38s|%-6s|", "SensorName", "StringType", "Type"));
        for (Sensor sensor : sensors) {
            Log.i(TAG, String.format(Locale.getDefault(), "|%-35s|%-38s|%-6s|", sensor.getName(), sensor.getStringType(), sensor.getType()));
        }

        Log.i(TAG, "=== LIST AVAILABLE SENSORS ===");
    }

}
