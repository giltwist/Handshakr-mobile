package com.sxa1508.handshakr;

// This class adapted from the official Android Developer tutorial:
// https://developer.android.com/develop/connectivity/bluetooth/connect-bluetooth-devices

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

private class AcceptThread extends Thread {
    MainActivity main;
    String TAG = "HandshakrAccept";
    private final BluetoothServerSocket mmServerSocket;

    public AcceptThread(MainActivity mainActivity) {
        this.main=mainActivity;
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code.
            tmp = main.bluetoothAdapter.listenUsingRfcommWithServiceRecord("handshakr", main.MY_UUID);
        } catch (IOException e) {
            Toast failToast = Toast.makeText(main, "Socket's listen() method failed", Toast.LENGTH_SHORT);
            failToast.show();
            Log.e(TAG, "Socket's listen() method failed", e);
        }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                Toast failToast = Toast.makeText(main, "Socket's accept() method failed", Toast.LENGTH_SHORT);
                failToast.show();
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                //manageMyConnectedSocket(socket);
                mmServerSocket.close();
                break;
            }
        }
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}