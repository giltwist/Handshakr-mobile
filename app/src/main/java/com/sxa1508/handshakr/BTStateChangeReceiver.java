package com.sxa1508.handshakr;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

public class BTStateChangeReceiver extends BroadcastReceiver {

    final Button enableButton;
    final Switch modeSwitch;
    final Button sendButton;
    final BluetoothAdapter adapter;

    public BTStateChangeReceiver(Button enableButton, Button sendButton,Switch modeSwitch, BluetoothAdapter adapter){
        this.enableButton = enableButton;
        this.sendButton = sendButton;
        this.modeSwitch = modeSwitch;
        this.adapter = adapter;
    }

    /**
     * Basic handler of updated bluetooth state
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     * TODO: Interrupt bluetooth transfers safely if BT is disabled mid-transfer
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        try {

            //System.out.println("BTSCR:" + intent.getAction());

            Bundle bundle = intent.getExtras();
            switch (intent.getAction()){

                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    enableButton.setVisibility(adapter!=null&&adapter.isEnabled() ? View.INVISIBLE : View.VISIBLE);
                    modeSwitch.setVisibility(adapter!=null&&adapter.isEnabled() ? View.VISIBLE : View.INVISIBLE);
                    sendButton.setVisibility(adapter!=null&&adapter.isEnabled() ? View.VISIBLE : View.INVISIBLE);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    sendButton.setVisibility(View.VISIBLE);
                    modeSwitch.setChecked(false);
                    break;
                case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:

                    if (intent.getExtras().getInt("android.bluetooth.adapter.extra.SCAN_MODE")==BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
                        enableButton.setVisibility(adapter!=null&&adapter.isEnabled() ? View.INVISIBLE : View.VISIBLE);
                        sendButton.setVisibility(View.INVISIBLE);
                    }else{
                        sendButton.setVisibility(View.VISIBLE);
                        modeSwitch.setChecked(false);
                    }
                    break;
                }


        }catch(Exception e){
            //noop
        }
    }
}
