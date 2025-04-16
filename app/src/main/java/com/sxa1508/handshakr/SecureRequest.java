package com.sxa1508.handshakr;

import androidx.annotation.Nullable;

import com.android.volley.Response;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SecureRequest extends BaseRequest {

    private final String token;
    private final String jwt;
    private final String cookie;
    public SecureRequest(int method, String token, String jwt, String cookie, String url, @Nullable JSONObject jsonRequest, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        this.token=token;
        this.jwt=jwt;
        this.cookie=cookie;
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String>  params = new HashMap<String, String>();
        params.put("Content-Type", "application/json");
        params.put("Origin", "app://com.handshakr");
        params.put("X-XSRF-TOKEN", this.token);
        params.put("Cookie", this.jwt+this.cookie);
        return params;
    }



}
