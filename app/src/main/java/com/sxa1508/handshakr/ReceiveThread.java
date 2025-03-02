package com.sxa1508.handshakr;

// This class adapted from the official Android Developer tutorial:
// https://developer.android.com/develop/connectivity/bluetooth/transfer-data

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class ReceiveThread extends Thread implements MessageConstants {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private byte[] mmBuffer; // mmBuffer store for the stream
    String TAG = "HandshakrTransfer";

    private MsgHandler handler; // handler that gets info from Bluetooth service

    public ReceiveThread(BluetoothSocket socket, MsgHandler h) {
        mmSocket = socket;
        handler = h;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        mmBuffer = new byte[1024];
        int numBytes; // bytes returned from read()
        boolean done=false;


        // Keep listening to the InputStream until an exception occurs.
        while (!done) {
            try {
                // Read from the InputStream.
                numBytes = mmInStream.read(mmBuffer);
                // Send the obtained bytes to the UI activity.
                Message readMsg = handler.obtainMessage(
                        MessageConstants.JSON_READ, numBytes, -1,
                        mmBuffer);
                readMsg.sendToTarget();
                done=true;
            } catch (IOException e) {
                //Toast.makeText(handler.activity, "pong", Toast.LENGTH_LONG).show();
                done=true;
                this.cancel();
                //Log.d(TAG, "Input stream was disconnected", e);
                break;
            }
        }
    }


    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}
