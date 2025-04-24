package com.sxa1508.handshakr;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.ArrayAdapter;

public class BTNearbyReceiver extends BroadcastReceiver {




    MainActivity main;
    public BTNearbyReceiver(MainActivity mainActivity) {
        this.main=mainActivity;
    }

    /**
     * Handler of detecting nearby bluetooth devices
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        BluetoothDevice fromIntent = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        assert fromIntent != null;
        String intentName=fromIntent.getName()==null?fromIntent.getAddress():fromIntent.getName();
        this.main.btList.putIfAbsent(fromIntent,intentName);
        this.main.BTnearadapter = new ArrayAdapter<>(this.main, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, this.main.btList.values().toArray(String[]::new));
        this.main.BTnearlist.setAdapter(this.main.BTnearadapter);
        this.main.BTnearlist.show();

    }
}