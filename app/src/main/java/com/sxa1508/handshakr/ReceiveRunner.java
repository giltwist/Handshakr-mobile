package com.sxa1508.handshakr;

// This class adapted from the official Android Developer tutorial:
// https://developer.android.com/develop/connectivity/bluetooth/transfer-data

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;


public class ReceiveRunner implements Callable<JSONObject>{
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private byte[] mmBuffer; // mmBuffer store for the stream
    String TAG = "HandshakrTransfer";


    public ReceiveRunner(BluetoothSocket socket) {
        mmSocket = socket;
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

    public JSONObject call() {
        mmBuffer = new byte[1024];
        int numBytes; // bytes returned from read()
        boolean done = false;
        JSONObject jsonObject = new JSONObject();
        String readMessage="";
        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                // Read from the InputStream.
                numBytes = mmInStream.read(mmBuffer);
                //System.out.println(numBytes);
                readMessage+=new String(mmBuffer, StandardCharsets.UTF_8);
                readMessage=readMessage.replace("\u0000", "");

                try {
                    jsonObject = new JSONObject(readMessage);
                    break;
                } catch (JSONException e) {
                }
                //jsonObject.put("test","check");

            } catch (IOException e) {
                done = true;
                try {
                    jsonObject.put("Receive Connection","Failed" + e);
                } catch (JSONException ex) {
                   // throw new RuntimeException(ex);
                }
                Log.d(TAG, "Input stream was disconnected", e);
                break;
            }
        }

        return jsonObject;
    }


}
