package com.sensorwear;

import android.content.Context;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.util.Log;

/**
 * Created by reva on 08/08/2017.
 */

public class TriggerListener extends TriggerEventListener {
    private Context mContext;

    TriggerListener(Context context) {
        mContext = context;
    }
    @Override
    public void onTrigger(TriggerEvent event) {
        if (event.values[0] == 1) {
            Log.i("Sensor", "Motion");
        }
    }
}
