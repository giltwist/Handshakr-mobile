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
import java.util.Iterator;
import java.util.Map;

public class AuthRequest extends JsonObjectRequest {


    public AuthRequest(int method, String url, @Nullable JSONObject jsonRequest, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {

        try {
            String responseText =
                    new String(
                            response.data,
                            HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            JSONObject responseBody=new JSONObject(responseText);
            JSONObject responseHeader=new JSONObject(response.headers);
            JSONObject fullResponse = responseBody;

            for (Iterator<String> it = responseHeader.keys(); it.hasNext(); ) {
                String k = it.next();
                fullResponse.put(k,responseHeader.get(k));
            }

            return Response.success(fullResponse
                    , HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException | JSONException e) {
            return Response.error(new ParseError(e));
        }
    }


    @Override
    public String getBodyContentType() {
        return "application/json; charset=utf-8";
    }

}
