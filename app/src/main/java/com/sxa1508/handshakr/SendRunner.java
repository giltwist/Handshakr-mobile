package com.sxa1508.handshakr;

// This class adapted from the official Android Developer tutorial:
// https://developer.android.com/develop/connectivity/bluetooth/transfer-data

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class SendRunner implements Runnable{
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private byte[] mmBuffer; // mmBuffer store for the stream
    String TAG = "HandshakrTransfer";



    public SendRunner(BluetoothSocket socket) {
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

    /**
     * Attempts to send data to someone else
     *
     */
    public void run() {
        //System.out.println("Sending: \n" +new String(mmBuffer, StandardCharsets.UTF_8));
        //System.out.println("BUFFER SIZE: " + Integer.toString(mmBuffer.length));
        write(mmBuffer);
        //this.cancel();
    }


    /**
     * Do not call this directly, as it is a blocking call
     * Utilizing executor to call run()
     */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);

            // Share the sent message with the UI activity.
           /* Message writtenMsg = handler.obtainMessage(
                    MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
            writtenMsg.sendToTarget();*/
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);
        }
    }


    /**
     * Basic Setter
     * @param mmBuffer - The bytes to be sent.
     */
    public void setMmBuffer(byte[] mmBuffer) {
        this.mmBuffer = mmBuffer;
    }
}
