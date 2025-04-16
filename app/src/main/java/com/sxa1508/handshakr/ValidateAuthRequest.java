package com.sxa1508.handshakr;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONObject;

public class ValidateAuthRequest extends SecureRequest {


    public ValidateAuthRequest(String token, String jwt, String cookie, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
        super(Request.Method.GET, token, jwt, cookie, "https://handshakr.duckdns.org/api/users/me", null, listener, errorListener);
    }


}
