package com.sxa1508.handshakr;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.Manifest;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public final UUID MY_UUID = UUID.fromString("40bbb78d-4257-4aff-9607-70ba22b747d2");

    ListPopupWindow BTnearlist;
    public Map<BluetoothDevice, String> btList;
    BTNearbyReceiver btr;
    ArrayAdapter<String> BTnearadapter;
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;

    AcceptThread acceptThread;
    ConnectThread connectThread;

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
                    Intent data = result.getData();
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

    public void beDiscoverable(View view) {
        if (hasBTPerms()) {

            if (bluetoothAdapter == null) {
                Toast hasntBTtoast = Toast.makeText(getApplicationContext(), "No BT functionality detected", Toast.LENGTH_SHORT);
                hasntBTtoast.show();
            } else {
                if (bluetoothAdapter.isEnabled()) {
                    if (bluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        Toast hasBTtoast = Toast.makeText(getApplicationContext(), "BT already discoverable", Toast.LENGTH_SHORT);

                        hasBTtoast.show();
                    } else {
                        Intent enableDiscoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        requestBTenable.launch(enableDiscoverable);
                        acceptThread = new AcceptThread(this);
                    }
                } else {
                    Toast hasntBTtoast = Toast.makeText(getApplicationContext(), "Need to enable BT", Toast.LENGTH_SHORT);
                    hasntBTtoast.show();
                }
            }

        }

    }

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
        }
        else{
            return false;
        }

    }

    public void doPair(MainActivity main, BluetoothDevice b) {
        Toast pairedToast = Toast.makeText(getApplicationContext(), "Pairing with " + (b.getName()==null?b.getAddress():b.getName()), Toast.LENGTH_SHORT);
        pairedToast.show();
        connectThread = new ConnectThread(main,b);
        connectThread.run();

    }

    public void testSendData(BluetoothSocket s) {
        //TODO
        BluetoothDevice b = s.getRemoteDevice();
        Toast sendToast = Toast.makeText(getApplicationContext(), "Sending test data to " + (b.getName()==null?b.getAddress():b.getName()), Toast.LENGTH_SHORT);
        sendToast.show();

        //connectThread.cancel();
    }
}