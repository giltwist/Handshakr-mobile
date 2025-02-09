package com.sxa1508.handshakr;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BTNearbyReceiver extends BroadcastReceiver {




    MainActivity main;
    public BTNearbyReceiver(MainActivity mainActivity) {
        this.main=mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        BluetoothDevice fromIntent = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        String intentName=fromIntent.getName()==null?fromIntent.getAddress():fromIntent.getName();
        this.main.btList.putIfAbsent(fromIntent,intentName);
        this.main.BTnearadapter = new ArrayAdapter<String>(this.main, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, this.main.btList.values().toArray(String[]::new));
        this.main.BTnearlist.setAdapter(this.main.BTnearadapter);
        this.main.BTnearlist.show();


        //Toast hasBTtoast = Toast.makeText(main, "FoundBTnear", Toast.LENGTH_SHORT);
        //hasBTtoast.show();
    }
}