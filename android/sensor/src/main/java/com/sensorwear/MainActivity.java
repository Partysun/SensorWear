package com.sensorwear;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

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
    private static final Integer RECORD_DURATION = 1;

    private GoogleApiClient mGoogleApiClient = null;
    private SensorManager mSensorManager = null;
    private Sensor mStepCountSensor = null;
    private Sensor mSigMotion;
    private TriggerListener mListener;
    private boolean isAlarm = false;
    private boolean isSigMotion = false;

    public void onButtonClicked(View target) {
        if(!isSigMotion) {
            startStepCounter();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient  .Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();

        startMesurements();
        //logAvailableSensors();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mGoogleApiClient != null && !(mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting())) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onDestroy() {
        if(mGoogleApiClient != null )
            mGoogleApiClient.unregisterConnectionCallbacks(this);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        if (mSigMotion != null) mSensorManager.cancelTriggerSensor(mListener, mSigMotion);
        if (mGoogleApiClient != null) {
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }
        super.onStop();
    }

    /**
     * Sends a string message to the connected handheld using the google api client (if available)
     * @param message
     */
    public void sendMessageToHandheld(final String path, final String message) {

        if (mGoogleApiClient == null)
            return;

        // use the api client to send the heartbeat value to our handheld
        final PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
        nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {

            @Override
            public void onResult(NodeApi.GetConnectedNodesResult result) {
                final List<Node> nodes = result.getNodes();

                for (Node node : nodes) {
                    Log.i(TAG, "SEND MESSAGE TO HANDHELD: " + path + " > " + message + " , node: " + node.getDisplayName());
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
            case "/toggleAlarm":
                isAlarm = !isAlarm;
                if (!isAlarm) {
                    stopStepCounter();
                }
                Toast.makeText(this, "Alarm " + isAlarm, Toast.LENGTH_SHORT).show();
                sendMessageToHandheld("/toggleAlarm", isAlarm + "");
                break;
            case "/stopStepCounter":
                stopStepCounter();
                break;
            case "/reset":
                isSigMotion = false;
                isAlarm = false;
                finish();
                startActivity(getIntent());
                break;
            default:
                Log.i(TAG, "Message " + path + " > " + new String(messageEvent.getData()));
                break;
        }
    }

    @Override
    public void onConnectionSuspended(final int i) {
        Log.e(TAG, "onConnectionSuspended: " + (i == CAUSE_NETWORK_LOST ? "NETWORK LOST" : "SERVICE_DISCONNECTED"));
    }

    private void startStepCounter() {
        Log.i(TAG, "Start Step Counter Sensor");
        isSigMotion = true;
        sendMessageToHandheld("/sigMotion", "");
        mStepCountSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener(this, mStepCountSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void stopStepCounter() {
        Log.i(TAG, "Stop Step Counter Sensor");
        isSigMotion = false;
        mSensorManager.unregisterListener(this, mStepCountSensor);
    }

    private void startMesurements() {
        mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
        mSigMotion = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        mListener = new TriggerListener(this) {
            @Override
            public void onTrigger(TriggerEvent event) {
                if (event.values[0] == 1 && !isSigMotion) {
                    startStepCounter();
                }
            }
        };
        mSensorManager.requestTriggerSensor(mListener, mSigMotion);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            String msg = (int)sensorEvent.values[0] + "";
            Log.d(TAG, msg);
            sendMessageToHandheld("/steps", msg);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged - accuracy: " + accuracy);
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
