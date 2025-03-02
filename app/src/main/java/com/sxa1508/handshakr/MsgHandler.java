package com.sxa1508.handshakr;

import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

//This class adapted from the official Android Developers tutorial for bluetooth chat
// https://github.com/android/connectivity-samples/blob/master/BluetoothChat/Application/src/main/java/com/example/android/bluetoothchat/BluetoothChatFragment.java
public class MsgHandler extends Handler implements MessageConstants {

    MainActivity activity;

    public MsgHandler setActivity(MainActivity main){
        this.activity=main;
        return this;
    }

    @Override
    public void handleMessage(Message msg) {
        Toast.makeText(activity, "Handling it", Toast.LENGTH_SHORT).show();
        switch (msg.what) {
            case MessageConstants.MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                activity.mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MessageConstants.JSON_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1, StandardCharsets.UTF_8);

                StringBuilder sb = new StringBuilder();
                try {
                    JSONObject jsonObject = new JSONObject(readMessage);
                    jsonObject.keys().forEachRemaining(keyStr ->
                    {
                        try {
                            sb.append(keyStr + ": " + jsonObject.get(keyStr) + "\n");
                        } catch (JSONException e) {
                            sb.append("ERROR IN JSON DECODING LOOP");
                        }
                    });
                } catch (JSONException e){
                    sb.append("ERROR DECODING JSON");
                }

                Toast.makeText(activity, sb.toString(), Toast.LENGTH_LONG).show();
                //activity.mConversationArrayAdapter.add(readMessage);
                break;

            case MessageConstants.MESSAGE_TOAST:
                if (null != activity) {
                    Toast.makeText(activity, msg.arg1,Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}