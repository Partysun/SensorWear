package com.sensorwear;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class WearCommunicationModule extends ReactContextBaseJavaModule
  implements
        GoogleApiClient.ConnectionCallbacks,
        MessageApi.MessageListener,
        LifecycleEventListener {

  private static final String TAG = "Sensor";
  private final GoogleApiClient googleApiClient;

  public WearCommunicationModule(ReactApplicationContext reactContext) {
    super(reactContext);
    reactContext.addLifecycleEventListener(this);
    googleApiClient = new GoogleApiClient.Builder(getReactApplicationContext()).addApi(Wearable.API)
      .addConnectionCallbacks(this)
       .build();
  }

  @Override
  public void onHostResume() {
      Log.i(TAG, "connecting...");
      googleApiClient.connect();
  }

  @Override
  public void onConnected(@Nullable Bundle bundle) {
      Log.i(TAG, "wear connected");
      Wearable.MessageApi.addListener(googleApiClient, this);
  }

  @Override
  public void onConnectionSuspended(int i) {
      Log.i(TAG, "wear disconnected");
      Wearable.MessageApi.removeListener(googleApiClient, this);
  }

  public void sendMessage(final String path, final String message) {
      final List<Node> nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes();
      if (nodes.size() > 0) {
          for (Node node : nodes) {
              byte[] data = message.getBytes(StandardCharsets.UTF_8);
              Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), path, data);
              Log.i(TAG, "Message sended to " + node.getDisplayName());
          }
      } else {
          Toast.makeText(getReactApplicationContext(), "No connected nodes found", Toast.LENGTH_SHORT).show();
      }
  }

  /** Toggle Alarm system on every node that is connected to this device. */
  @ReactMethod
  public void toggleWatchAlarm() {
      sendMessage("/toggleAlarm", "");
  }

  @ReactMethod
  public void resetWatch() {
      sendMessage("/reset", "");
  }

  @ReactMethod
  public void stopStepCounter() {
      sendMessage("/stopStepCounter", "");
  }

  @Override
  public void onMessageReceived(MessageEvent messageEvent) {
      final String path = messageEvent.getPath();
      switch (path) {
          case "/toggleAlarm":
              WritableMap map = Arguments.createMap();
              map.putString("isAlarm", new String(messageEvent.getData()));
              sendEvent(getReactApplicationContext(), "toggleAlarm", map);
              break;
          case "/sigMotion":
              sendEvent(getReactApplicationContext(), "sigMotion", null);
              break;
          case "/steps":
              WritableMap mapSteps = Arguments.createMap();
              mapSteps.putString("steps", new String(messageEvent.getData()));
              sendEvent(getReactApplicationContext(), "steps", mapSteps);
              break;
          default:
              Log.i(TAG, "Message " + path + " > " + new String(messageEvent.getData()));
              break;
      }
  }

  private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
  }

  @Override
  public void onHostPause() {

  }

  @Override
  public void onHostDestroy() {
    Wearable.MessageApi.removeListener(googleApiClient, this);
    googleApiClient.disconnect();
  }

  @Override
  public String getName() {
    return "AndroidWearCommunication";
  }
}