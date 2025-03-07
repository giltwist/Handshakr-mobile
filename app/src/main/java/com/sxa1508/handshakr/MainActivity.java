package com.sxa1508.handshakr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    public final UUID MY_UUID = UUID.fromString("40bbb78d-4257-4aff-9607-70ba22b747d2");

    ListPopupWindow BTnearlist;
    public Map<BluetoothDevice, String> btList;
    BTNearbyReceiver btr;
    ArrayAdapter<String> BTnearadapter;
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;


    ExecutorService executor;
    AcceptRunner acceptRunner;
    ConnectRunner connectRunner;
    SendRunner sendRunner;
    ReceiveRunner receiveRunner;


    //BEGIN ACTIVITY LAUNCHERS
    private ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
                if (isGranted.containsValue(false)) {
                    Toast hasntBTtoast = Toast.makeText(getApplicationContext(), "At least one permission was denied", Toast.LENGTH_SHORT);
                    hasntBTtoast.show();
                } else {
                    Toast hasBTtoast = Toast.makeText(getApplicationContext(), "All perms granted", Toast.LENGTH_SHORT);
                    hasBTtoast.show();
                }
            });

    private ActivityResultLauncher<Intent> requestBTenable = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                }
            });

    private ActivityResultLauncher<Intent> startDiscoverable = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //BEGIN INIT

        executor = Executors.newSingleThreadExecutor();
        btList = new HashMap<>();
        btr = new BTNearbyReceiver(this);
        BTnearadapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, btList.values().toArray(String[]::new));
        BTnearlist = new ListPopupWindow(this);
        BTnearlist.setAdapter(BTnearadapter);
        BTnearlist.setModal(true);
        BTnearlist.setOnItemClickListener((parent, view, position, id) -> {

            //Toast choseToast = Toast.makeText(getApplicationContext(), "You chose " + ((BluetoothDevice) btList.keySet().toArray()[position]).getName(), Toast.LENGTH_SHORT);
            //choseToast.show();

            doPair(this, ((BluetoothDevice) btList.keySet().toArray()[position]));

            BTnearlist.dismiss();
        });

        registerReceiver(btr, new IntentFilter(BluetoothDevice.ACTION_FOUND));


    }

    public void getBTperms(View view) {

        if (hasBTPerms()) {

            Toast hasBTtoast = Toast.makeText(getApplicationContext(), "Already have BT perms", Toast.LENGTH_SHORT);
            hasBTtoast.show();

        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN});
        }
    }

    private boolean hasBTPerms() {
        boolean result;
        result = (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED);
        if (result) {
            bluetoothManager = getSystemService(BluetoothManager.class);
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
        return (result);
    }

    public void enableBT(View view) {
        if (hasBTPerms()) {

            if (bluetoothAdapter == null) {
                Toast hasntBTtoast = Toast.makeText(getApplicationContext(), "No BT functionality detected", Toast.LENGTH_SHORT);
                hasntBTtoast.show();
            } else {
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    requestBTenable.launch(enableBtIntent);
                } else {
                    Toast hasBTtoast = Toast.makeText(getApplicationContext(), "BT already enabled", Toast.LENGTH_SHORT);
                    hasBTtoast.show();
                }
            }
        } else {
            Toast hasntBTtoast = Toast.makeText(getApplicationContext(), "Need BT perms", Toast.LENGTH_SHORT);
            hasntBTtoast.show();
        }
    }

    @SuppressLint("MissingPermission")
    //Has a permission check but linter doesn't see it
    public void beDiscoverable(View view) {
        if (hasBTPerms()) {

            if (bluetoothAdapter == null) {
                Toast hasntBTtoast = Toast.makeText(getApplicationContext(), "No BT functionality detected", Toast.LENGTH_SHORT);
                hasntBTtoast.show();
            } else {
                if (bluetoothAdapter.isEnabled()) {
                    if (bluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        bluetoothAdapter.cancelDiscovery();
                    }
                    Intent enableDiscoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    requestBTenable.launch(enableDiscoverable);
                    acceptRunner = new AcceptRunner(this);
                    Future<?> futureSock = executor.submit(acceptRunner);
                    try {
                        testReceiveData((BluetoothSocket) futureSock.get());
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }


                } else {
                    Toast hasntBTtoast = Toast.makeText(getApplicationContext(), "Need to enable BT", Toast.LENGTH_SHORT);
                    hasntBTtoast.show();
                }
            }

        }

    }

    @SuppressLint("MissingPermission")
    //Has a permission check but linter doesn't see it
    public void doDiscover(View view) {

        if (hasBTPerms()) {


            if (bluetoothAdapter == null) {
                Toast hasntBTtoast = Toast.makeText(getApplicationContext(), "No BT functionality detected", Toast.LENGTH_SHORT);
                hasntBTtoast.show();
            } else {
                if (bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.startDiscovery();

                    BTnearlist.setAnchorView(view);
                    BTnearlist.show();

                } else {
                    Toast hasntBTtoast = Toast.makeText(getApplicationContext(), "Need to enable BT", Toast.LENGTH_SHORT);
                    hasntBTtoast.show();
                }
            }

        }

    }

    public boolean isPaired(MainActivity main, BluetoothDevice b) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            return main.bluetoothAdapter.getBondedDevices().contains(b);
        } else {
            return false;
        }

    }

    @SuppressLint("MissingPermission")
    //Only call with permissions!
    public void doPair(MainActivity main, BluetoothDevice b) {
        Toast pairedToast = Toast.makeText(getApplicationContext(), "Pairing with " + (b.getName() == null ? b.getAddress() : b.getName()), Toast.LENGTH_SHORT);
        pairedToast.show();
        connectRunner = new ConnectRunner(main, b);
        Future<?> futureSock = executor.submit(connectRunner);
        try {
            testSendData((BluetoothSocket) futureSock.get());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


    @SuppressLint("MissingPermission")
    //Only call with permissions!
    public void testSendData(BluetoothSocket s) {
        //this = mainactivity
        BluetoothDevice b = s.getRemoteDevice();
        Toast sendToast = Toast.makeText(getApplicationContext(), "Sending test data to " + (b.getName() == null ? b.getAddress() : b.getName()), Toast.LENGTH_SHORT);
        sendToast.show();

        JSONObject testData = new JSONObject();
        try {
            testData.put("user", "docsock");
            testData.put("title", "lawnmowing");
            testData.put("detail", "If you mow my lawn on Saturday then I will pay you $50.");
            testData.put("signature", UUID.randomUUID().toString());
            byte[] testDataAsBytes = testData.toString().getBytes(StandardCharsets.UTF_8);
            sendRunner = new SendRunner(s);
            sendRunner.setMmBuffer(testDataAsBytes);
            executor.execute(sendRunner);
            s.close();

        } catch (Exception e) {

            Toast jsonToast = Toast.makeText(getApplicationContext(), "Error sending JSON payload " + e, Toast.LENGTH_SHORT);
            jsonToast.show();
            //throw new RuntimeException(e);
        }
    }

    @SuppressLint("MissingPermission")
    //Only call with permissions!
    public void testReceiveData(BluetoothSocket s) {
        //this = mainactivity

        BluetoothDevice b = s.getRemoteDevice();
        //Toast receiveToast = Toast.makeText(getApplicationContext(), "Awaiting data from" + (b.getName() == null ? b.getAddress() : b.getName()), Toast.LENGTH_SHORT);
        //receiveToast.show();

        receiveRunner = new ReceiveRunner(s);
        Future<?> futureJSON = executor.submit(receiveRunner);

        StringBuilder sb = new StringBuilder();

        try {
            JSONObject jsonObject = (JSONObject) futureJSON.get();
            jsonObject.keys().forEachRemaining(keyStr ->
            {
                try {
                    sb.append(keyStr).append(": ").append(jsonObject.get(keyStr)).append("\n");
                } catch (JSONException e) {
                    sb.append("ERROR IN JSON DECODING LOOP");
                }
            });
        } catch (ExecutionException | InterruptedException e) {
            //throw new RuntimeException(e);
        }

        Snackbar snackbar = Snackbar.make(findViewById(R.id.main), sb.toString(), Snackbar.LENGTH_LONG);
        snackbar.setTextMaxLines(10);
        snackbar.show();



    }


}