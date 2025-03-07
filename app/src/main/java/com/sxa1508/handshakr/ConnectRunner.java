package com.sxa1508.handshakr;

// This class adapted from the official Android Developer tutorial:
// https://developer.android.com/develop/connectivity/bluetooth/connect-bluetooth-devices

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.Callable;

@SuppressLint("MissingPermission")
//Only call with permission!
public class ConnectRunner implements Callable<BluetoothSocket> {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    String TAG = "HandshakrConnect";
    MainActivity main;


    public ConnectRunner(MainActivity mainActivity, BluetoothDevice device) {
        this.main=mainActivity;
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.

        BluetoothSocket tmp = null;
        mmDevice = device;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = mmDevice.createRfcommSocketToServiceRecord(main.MY_UUID);
        } catch (IOException e) {
            //Toast failToast = Toast.makeText(main, "Socket's create() method failed", Toast.LENGTH_SHORT);
            //failToast.show();
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    public BluetoothSocket call() {
        // Cancel discovery because it otherwise slows down the connection.
        //Toast.makeText(main, "Running Connection", Toast.LENGTH_SHORT).show();
        main.bluetoothAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return null;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        //Toast.makeText(main, "Socket Active", Toast.LENGTH_SHORT).show();
        return mmSocket;
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}