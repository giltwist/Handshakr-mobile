package com.sxa1508.handshakr;

// This class adapted from the official Android Developer tutorial:
// https://developer.android.com/develop/connectivity/bluetooth/connect-bluetooth-devices

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.Callable;

public class AcceptRunner implements Callable<BluetoothSocket> {
    MainActivity main;
    String TAG = "HandshakrAccept";
    private final BluetoothServerSocket mmServerSocket;

    @SuppressLint("MissingPermission")
    //Only call when you have the permission!
    public AcceptRunner(MainActivity mainActivity) {
        this.main=mainActivity;
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code.
            tmp = main.bluetoothAdapter.listenUsingRfcommWithServiceRecord("handshakr", main.MY_UUID);
        } catch (IOException e) {
           // Toast failToast = Toast.makeText(main, "Socket's listen() method failed", Toast.LENGTH_SHORT);
           // failToast.show();
            Log.e(TAG, "Socket's listen() method failed", e);
        }
        mmServerSocket = tmp;
    }

    /**
     * Attempts to accept a bluetooth pairing initiated by someone else.
     *
     * @return a completed bluetooth socket
     */
    public BluetoothSocket call() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                //Toast failToast = Toast.makeText(main, "Socket's accept() method failed", Toast.LENGTH_SHORT);
                //failToast.show();
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.

            break;
            }
        }
        return socket;
    }

}