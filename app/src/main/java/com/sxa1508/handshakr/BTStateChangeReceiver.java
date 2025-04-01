package com.sxa1508.handshakr;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;

public class BTStateChangeReceiver extends BroadcastReceiver {

    final Button button;
    final BluetoothAdapter adapter;

    public BTStateChangeReceiver(Button button,BluetoothAdapter adapter){
        this.button = button;
        this.adapter = adapter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
           button.setVisibility(adapter.isEnabled()? View.GONE:View.VISIBLE);

        }
    }
}
