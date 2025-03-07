package com.sxa1508.handshakr;

// This class adapted from the official Android Developer tutorial:
// https://developer.android.com/develop/connectivity/bluetooth/connect-bluetooth-devices

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Callable;

@SuppressLint("MissingPermission")
//Only call with permission!
public class ConnectRunner implements Callable<BluetoothSocket> {

    private final BluetoothDevice mmDevice;
    private final UUID uuid;
    String TAG = "HandshakrConnect";


    public ConnectRunner(UUID uuid, BluetoothDevice device) {

        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        this.mmDevice = device;
        this.uuid = uuid;
    }

    public BluetoothSocket call() {

        BluetoothSocket mmSocket = null;

        // Cancel discovery because it otherwise slows down the connection.
        //Toast.makeText(main, "Running Connection", Toast.LENGTH_SHORT).show();
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
        } catch (IOException e) {
            Log.e(TAG, "Socket's connect() method failed", e);

        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        //Toast.makeText(main, "Socket Active", Toast.LENGTH_SHORT).show();
        return mmSocket;
    }
}

