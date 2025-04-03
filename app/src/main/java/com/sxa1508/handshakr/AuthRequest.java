package com.sxa1508.handshakr;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AuthRequest extends StringRequest {


    public AuthRequest(int method, String url, Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            // Since minSdkVersion = 8, we can't call
            // new String(response.data, Charset.defaultCharset())
            // So suppress the warning instead.
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

/*
    @Override
    public byte[] getBody() throws AuthFailureError {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", "user1");
            jsonBody.put("password", "password");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        final String requestBody = jsonBody.toString();
        return requestBody == null ? null : requestBody.getBytes(StandardCharsets.UTF_8);
    }
    @Override
    public String getBodyContentType() {
        return "application/json; charset=utf-8";
    }*/

}
