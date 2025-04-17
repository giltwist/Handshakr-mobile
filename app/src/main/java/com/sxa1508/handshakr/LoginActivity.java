package com.sxa1508.handshakr;

import static android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void doLogin(View view) {

        EditText user = findViewById(R.id.user);
        EditText password = findViewById(R.id.password);
        Button login = findViewById(R.id.loginButton);
        ImageView wait = findViewById(R.id.wait);


        if (user.getText().isEmpty() || password.getText().isEmpty()) {
            Toast.makeText(view.getContext(), "Fill out username and password", Toast.LENGTH_LONG).show();
        } else {
        //BEGIN NET CHECK

        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
        android.net.Network currentNetwork = connectivityManager.getActiveNetwork();

        if (currentNetwork != null) {
            NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(currentNetwork);
            LinkProperties linkProperties = connectivityManager.getLinkProperties(currentNetwork);

            assert caps != null;
            if (caps.hasCapability(NET_CAPABILITY_VALIDATED)) {

                login.setEnabled(false);
                login.setAlpha(.5f);
                login.setClickable(false);
                wait.setVisibility(View.VISIBLE);
                //BEGIN VOLLEY
                // Instantiate the cache
                Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
                // Set up the network to use HttpURLConnection as the HTTP client.
                com.android.volley.Network network = new BasicNetwork(new HurlStack());
                // Instantiate the RequestQueue with the cache and network.
                RequestQueue requestQueue = new RequestQueue(cache, network);
                // Start the queue
                requestQueue.start();




                    //BEGIN LOGIN
                    JSONObject loginInfo = new JSONObject();
                    try {
                        loginInfo.put("username", user.getText());
                        loginInfo.put("password", password.getText());

                        Toast.makeText(view.getContext(), "Attempting login as: " + loginInfo.getString("username"), Toast.LENGTH_SHORT).show();

                        AuthRequest authRequest = new AuthRequest(Request.Method.POST, loginInfo,
                                response -> {
                                    try {
                                        if (response.getJSONObject("body").getString("httpStatus").equals("200")) {
                                            //parse response
                                            String xsrfToken = response.getJSONObject("header").getString("X-CSRF-TOKEN");
                                            String jwt = response.getJSONObject("body").getJSONObject("data").getString("jwtCookie");
                                            String xsrfCookie = response.getJSONObject("body").getJSONObject("data").getString("csrfCookie");
                                            jwt = jwt.substring(0, jwt.indexOf(";") + 1);
                                            Toast.makeText(view.getContext(), "Login Successful", Toast.LENGTH_SHORT).show();

                                            Intent myIntent = new Intent(this, MainActivity.class);
                                            Bundle b = new Bundle();
                                            b.putString("user", String.valueOf(user.getText()));
                                            b.putString("token", xsrfToken);
                                            b.putString("jwt",jwt);
                                            b.putString("cookie",xsrfCookie);
                                            myIntent.putExtras(b);
                                            startActivity(myIntent);


                                        } else {
                                            Toast.makeText(view.getContext(), "Login Failed" +response.toString(4), Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                },
                                error -> {
                                    Toast.makeText(view.getContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                                    wait.setVisibility(View.INVISIBLE);
                                    login.setEnabled(true);
                                    login.setAlpha(1.0f);
                                    login.setClickable(true);

                                });
                        requestQueue.add(authRequest);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }


                } else{
                    Toast.makeText(view.getContext(), "No internet access", Toast.LENGTH_LONG).show();
                }


            } else {
                Toast.makeText(view.getContext(), "No internet connection", Toast.LENGTH_LONG).show();
            }
        }

    }
}