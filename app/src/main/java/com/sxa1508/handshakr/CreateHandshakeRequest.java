package com.sxa1508.handshakr;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class CreateHandshakeRequest extends SecureRequest {

    private final JSONObject handshake;

    public CreateHandshakeRequest(String token, String jwt, String cookie, @Nullable JSONObject jsonRequest, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
        super(Request.Method.POST, token, jwt, cookie, "https://handshakr.duckdns.org/api/users/create-handshake", jsonRequest, listener, errorListener);
        this.handshake = jsonRequest;
    }

    @Override
    public byte[] getBody() {
        byte[] body = new byte[0];
        try {
            body = handshake.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            //noop
        }
        return body;
    }


}
