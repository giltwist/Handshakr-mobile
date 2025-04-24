package com.sxa1508.handshakr;

import androidx.annotation.Nullable;

import com.android.volley.Response;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AuthRequest extends BaseRequest {


    public AuthRequest(int method, @Nullable JSONObject jsonRequest, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
        super(method, "https://handshakr.duckdns.org/api/auth/login", jsonRequest, listener, errorListener);

    }

    /**
     * Ensures that the backend recognizes this HTTP request as from the mobile app
     * @return the HTTP headers of this request as a Map
     */
    @Override
    public Map<String, String> getHeaders() {
        Map<String, String>  params = new HashMap<String, String>();
        params.put("Content-Type", "application/json");
        params.put("Origin", "app://com.handshakr");
        return params;
    }

}
