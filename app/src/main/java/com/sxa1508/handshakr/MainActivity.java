package com.sxa1508.handshakr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListPopupWindow;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Cache;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import org.json.JSONException;
import org.json.JSONObject;
import org.pgpainless.sop.SOPImpl;
import org.pgpainless.sop.*;
import org.pgpainless.sop.InlineSignImpl;
import sop.operation.InlineSign;

import sop.enums.InlineSignAs;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import sop.*;
import sop.DecryptionResult;
import sop.ReadyWithResult;
import sop.SOP;
import sop.exception.SOPGPException;

public class MainActivity extends AppCompatActivity {

    public final UUID MY_UUID = UUID.fromString("40bbb78d-4257-4aff-9607-70ba22b747d2");
    final static int SENDERKEY = 0;
    final static int RECEIVERKEY = 1;
    final static int OFFER = 2;
    final static int RESPONSE = 3;

    ListPopupWindow BTnearlist;
    public Map<BluetoothDevice, String> btList;
    BTNearbyReceiver btr;
    ArrayAdapter<String> BTnearadapter;
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;

    RequestQueue requestQueue;

    ExecutorService executor;
    ListeningExecutorService listeningExecutor;
    AcceptRunner acceptRunner;
    ConnectRunner connectRunner;
    SendRunner sendRunner;
    ReceiveRunner receiveRunner;

    EditText welcome;
    EditText dealTitle;
    EditText dealDesc;

    Button permButton;
    Button enableButton;
    Button sendButton;
    Switch modeSwitch;

    String loginToken;
    String loginJWT;
    String loginCookie;
    String userID;
    byte[] privateKey;
    byte[] publicKey;

    byte[] otherPublicKey;
    String otherUserID;

    //BEGIN ACTIVITY LAUNCHERS
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
                if (isGranted.containsValue(false)) {
                    Toast.makeText(getApplicationContext(), "At least one permission was denied", Toast.LENGTH_SHORT).show();
                    permButton.setVisibility(View.VISIBLE);
                    modeSwitch.setVisibility(View.INVISIBLE);
                    sendButton.setVisibility(View.INVISIBLE);

                } else {
                    Toast.makeText(getApplicationContext(), "All perms granted", Toast.LENGTH_SHORT).show();
                    permButton.setVisibility(View.INVISIBLE);
                    enableButton.setVisibility(bluetoothAdapter != null && bluetoothAdapter.isEnabled() ? View.GONE : View.VISIBLE);
                    modeSwitch.setVisibility(bluetoothAdapter != null && bluetoothAdapter.isEnabled() ? View.VISIBLE : View.INVISIBLE);

                }


            });

    private final ActivityResultLauncher<Intent> requestBTenable = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    //noop
                }
            });

    private final ActivityResultLauncher<Intent> startDiscoverable = registerForActivityResult(
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

        Bundle b = getIntent().getExtras();
        if (b != null) {
            this.loginToken = b.getString("token");
            this.loginJWT = b.getString("jwt");
            this.loginCookie = b.getString("cookie");
            this.userID = b.getString("user");
            generateKeys();
        }

        //BEGIN VOLLEY
        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        com.android.volley.Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);
        // Start the queue
        requestQueue.start();


        //Test creation of handshake
        //submitHandshake(loginToken, loginJWT, loginCookie, "user3", "TestHandshake"+UUID.randomUUID().toString().substring(0,5), "DEJGHESGJKJNVDKJNSDGFHJUIURJGNNDKLDL", findViewById(R.id.main), requestQueue);

        //validateCSRF(loginToken,loginJWT,loginCookie,findViewById(R.id.main),requestQueue);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //BEGIN INIT
        welcome = findViewById(R.id.userwelcome);
        dealTitle = findViewById(R.id.dealTitle);
        dealDesc = findViewById(R.id.dealDetail);
        permButton = findViewById(R.id.permButton);
        enableButton = findViewById(R.id.enableButton);
        sendButton = findViewById(R.id.send);
        modeSwitch = findViewById(R.id.mode);

        welcome.setText("Welcome " + userID);

        permButton.setVisibility(hasBTPerms() ? View.GONE : View.VISIBLE);
        enableButton.setVisibility(bluetoothAdapter != null && bluetoothAdapter.isEnabled() ? View.GONE : View.VISIBLE);
        modeSwitch.setVisibility(bluetoothAdapter != null && bluetoothAdapter.isEnabled() ? View.VISIBLE : View.INVISIBLE);

        BTStateChangeReceiver btSCR = new BTStateChangeReceiver(enableButton, sendButton, modeSwitch, bluetoothAdapter);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(btSCR, filter);

        //THREADING SETUP
        executor = Executors.newSingleThreadExecutor();
        listeningExecutor = MoreExecutors.listeningDecorator(executor);


        btList = new HashMap<>();

        BTnearadapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, btList.values().toArray(String[]::new));
        BTnearlist = new ListPopupWindow(this);
        BTnearlist.setAdapter(BTnearadapter);
        BTnearlist.setModal(true);
        BTnearlist.setOnItemClickListener((parent, view, position, id) -> {
            doPair(((BluetoothDevice) btList.keySet().toArray()[position]));
            BTnearlist.dismiss();
        });

        btr = new BTNearbyReceiver(this);
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
                    //Toast hasBTtoast = Toast.makeText(getApplicationContext(), "BT already enabled", Toast.LENGTH_SHORT);
                    enableButton.setVisibility(View.GONE);
                    modeSwitch.setVisibility(View.VISIBLE);

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
                    enableDiscoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60);
                    requestBTenable.launch(enableDiscoverable);

                    //BEGIN BLUETOOTH SETUP
                    acceptRunner = new AcceptRunner(this);
                    ListenableFuture<BluetoothSocket> futureSockReady = listeningExecutor.submit(acceptRunner);
                    Futures.addCallback(futureSockReady, new FutureCallback<>() {
                        @Override
                        public void onSuccess(BluetoothSocket s) {
                            ReceiveData(s);
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            //noop
                        }
                    }, executor);

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

        if (MainActivity.this.dealTitle.getText().isEmpty() || MainActivity.this.dealDesc.getText().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Fill out the deal form first", Toast.LENGTH_SHORT).show();
        } else {


            if (hasBTPerms()) {


                if (MainActivity.this.bluetoothAdapter == null) {
                    Toast.makeText(getApplicationContext(), "No BT functionality detected", Toast.LENGTH_SHORT).show();

                } else {
                    if (MainActivity.this.bluetoothAdapter.isEnabled()) {
                        MainActivity.this.bluetoothAdapter.startDiscovery();

                        MainActivity.this.BTnearlist.setAnchorView(view);
                        MainActivity.this.BTnearlist.show();

                    } else {
                        Toast.makeText(getApplicationContext(), "Need to enable BT", Toast.LENGTH_SHORT).show();

                    }
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
    public void doPair(BluetoothDevice b) {
        Toast.makeText(this, "Pairing with " + (b.getName() == null ? b.getAddress() : b.getName()), Toast.LENGTH_SHORT).show();
        bluetoothAdapter.cancelDiscovery();
        connectRunner = new ConnectRunner(MY_UUID, b);
        Future<BluetoothSocket> futureSockPair = executor.submit(connectRunner);
        //ListenableFuture<BluetoothSocket> futureSockPair = listeningExecutor.submit(connectRunner);
        try {
            //Toast.makeText(this, "Paired with " + (b.getName() == null ? b.getAddress() : b.getName()), Toast.LENGTH_SHORT).show();
            String key = new String(this.publicKey, StandardCharsets.UTF_8);
            //key=key.replace('\n','&');
            //key=key.replace('/','^');
            //key=key.substring(PGPHead.length(),key.length()-PGPFoot.length()-2);
            JSONObject payload = new JSONObject();
            payload.put("key", key);
            payload.put("user", MainActivity.this.userID);
            SendData(futureSockPair.get(), SENDERKEY, payload);
            ReceiveData(futureSockPair.get());
        } catch (ExecutionException | InterruptedException | JSONException e) {
            throw new RuntimeException(e);
        }

        /*
        Futures.addCallback(futureSockPair, new FutureCallback<>() {
            @Override
            public void onSuccess(BluetoothSocket s){
                Toast.makeText(getApplicationContext(),"Pairing successful", Toast.LENGTH_SHORT).show();
                testSendData(s);
            }

            @Override
            public void onFailure(Throwable t){
                Toast.makeText(getApplicationContext(),"Pairing Failed", Toast.LENGTH_SHORT).show();
            }
        },executor);
*/


    }


    @SuppressLint("MissingPermission")
    //Only call with permissions!
    public void SendData(BluetoothSocket s, int type, JSONObject payload) {
        BluetoothDevice b = s.getRemoteDevice();
        //Toast sendToast = Toast.makeText(this, "Sending data to " + (b.getName() == null ? b.getAddress() : b.getName()), Toast.LENGTH_SHORT);
        //sendToast.show();

        try {

            payload.put("SendType", type);
            //System.out.println("Payload \n"+payload.toString(4));
            //String payloadG = new Gson().toJson(payload);
            byte[] payloadAsBytes = payload.toString().getBytes(StandardCharsets.UTF_8);

            sendRunner = new SendRunner(s);
            sendRunner.setMmBuffer(payloadAsBytes);
            executor.execute(sendRunner);
            //s.close();

        } catch (Exception e) {

            Toast jsonToast = Toast.makeText(getApplicationContext(), "Error sending JSON payload " + e, Toast.LENGTH_SHORT);
            jsonToast.show();
            //throw new RuntimeException(e);
        }
    }

    @SuppressLint("MissingPermission")
    //Only call with permissions!
    public void ReceiveData(BluetoothSocket s) {

        receiveRunner = new ReceiveRunner(s);
        ListenableFuture<JSONObject> futureJSON = listeningExecutor.submit(receiveRunner);

        Futures.addCallback(futureJSON, new FutureCallback<>() {
            @Override
            public void onSuccess(JSONObject j) {


                try {
                    //System.out.println("Successful Receipt: \n" + j.toString(4));
                    switch (j.getInt("SendType")) {

                        case SENDERKEY:
                            System.out.println("Sender's Key");
                            System.out.println(j.getString("key"));
                            otherPublicKey = j.getString("key").getBytes(StandardCharsets.UTF_8);
                            otherUserID = j.getString("user");
                            //Snackbar.make(findViewById(R.id.main), j.getString("key"),Snackbar.LENGTH_LONG).setTextMaxLines(30).show();
                            try {
                                JSONObject p = new JSONObject();
                                p.put("user", MainActivity.this.userID);
                                p.put("key", new String(MainActivity.this.publicKey, StandardCharsets.UTF_8));
                                SendData(s, RECEIVERKEY, p);
                                ReceiveData(s);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        case RECEIVERKEY:
                            System.out.println("Receiver's Key");
                            System.out.println(j.getString("key"));
                            otherPublicKey = j.getString("key").getBytes(StandardCharsets.UTF_8);
                            otherUserID = j.getString("user");
                            JSONObject offer = new JSONObject();
                            offer.put("title", MainActivity.this.dealTitle.getText().toString());
                            offer.put("desc", MainActivity.this.dealDesc.getText().toString());
                            JSONObject encryptedOffer = encryptOffer(MainActivity.this.publicKey, MainActivity.this.privateKey, otherPublicKey, offer);
                            SendData(s, OFFER, encryptedOffer);
                            ReceiveData(s);
                            //Snackbar.make(findViewById(R.id.main), j.getString("key"),Snackbar.LENGTH_LONG).setTextMaxLines(30).show();
                            break;
                        case OFFER:
                            //Decrypt Offer
                            System.out.println("Got offer");
                            JSONObject theoffer = decryptOffer(otherPublicKey, publicKey, privateKey, j.getString("cipher"));
                            System.out.println(theoffer.toString(4));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle(otherUserID + " has offered this deal:");
                                    try {
                                        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //Sign Offer
                                                        try {
                                                            JSONObject a = new JSONObject();
                                                            a.put("response", "accept");
                                                            a.put("signed",signOffer(privateKey,j.getString("cipher").getBytes(StandardCharsets.UTF_8)));
                                                            SendData(s, RESPONSE, a);
                                                        } catch (JSONException e) {
                                                            throw new RuntimeException(e);
                                                        }
                                                    }
                                                })
                                                .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        try {
                                                            SendData(s, RESPONSE, new JSONObject().put("response", "reject"));
                                                        } catch (JSONException e) {
                                                            throw new RuntimeException(e);
                                                        }
                                                    }
                                                });
                                        JSONObject plain = new JSONObject(theoffer.getString("plain"));
                                        builder.setMessage("Title: " + plain.getString("title")+"\nDesc: " + plain.getString("desc"));
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                    builder.create();
                                    builder.show();
                                }
                            });
                            break;
                        case RESPONSE:
                            if (j.getString("response").equals("accept")) {
                                submitHandshake(loginToken,loginJWT,loginCookie,otherUserID,dealTitle.getText().toString(),j.getString("signed"),requestQueue);
                            } else {
                                //Toast
                            }

                            break;
                        default:
                            System.out.println("Unexpected BT Data");
                            break;


                    }
                } catch (JSONException e) {
                    //noop
                }


            }

            @Override
            public void onFailure(Throwable t) {
                //noop
            }
        }, executor);
    }


    public void generateKeys() {

        SOP sop = new SOPImpl();

        try {
            this.privateKey = sop.generateKey()
                    .userId(this.userID)
                    //.noArmor()
                    .generate()
                    .getBytes();

            this.publicKey = sop.extractCert()
                    //.noArmor()
                    .key(this.privateKey).getBytes();


            //String key = new String(this.publicKey,StandardCharsets.US_ASCII);
            //key=key.substring(key.length()-PGPFoot.length()-100);

            //System.out.println(key.length());
            //System.out.println(key);
        } catch (IOException e) {
            //no-op
        }
    }

    public JSONObject encryptOffer(byte[] senderPublicKey, byte[] senderPrivateKey, byte[] receiverPublicKey, JSONObject offer) {
        SOP sop = new SOPImpl();
        JSONObject result = new JSONObject();
        try {
            byte[] plaintext = offer.toString().getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = sop.encrypt()
                    .withCert(senderPublicKey)
                    .withCert(receiverPublicKey)
                    .signWith(senderPrivateKey)
                    .plaintext(plaintext)
                    .toByteArrayAndResult().getBytes();
            result.put("cipher", new String(ciphertext, StandardCharsets.UTF_8));
            //System.out.println(result.toString(4));
        } catch (IOException e) {
            //no-op
        } catch (SOPGPException.CannotDecrypt e) {
            //no-op
        } catch (JSONException ex) {
            //no-op
        }

        return result;
    }

    public JSONObject decryptOffer(byte[] senderPublicKey, byte[] receiverPublicKey, byte[] receiverPrivateKey, String encrypted) {
        SOP sop = new SOPImpl();
        JSONObject result = new JSONObject();
        byte[] ciphertext = null;
        ciphertext = encrypted.getBytes(StandardCharsets.UTF_8);

        ReadyWithResult<DecryptionResult> readyWithResult = null;
        try {
            readyWithResult = sop.decrypt()
                    .withKey(receiverPrivateKey)
                    .verifyWithCert(senderPublicKey)
                    .verifyWithCert(receiverPublicKey)
                    .ciphertext(ciphertext);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        ByteArrayAndResult<DecryptionResult> bytesAndResult = null;
        try {
            bytesAndResult = readyWithResult.toByteArrayAndResult();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        DecryptionResult dresult = bytesAndResult.getResult();
        byte[] resultText = bytesAndResult.getBytes();
        String decryptedMessage = new String(resultText);
        try {
            result.put("plain", decryptedMessage);
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        } catch (SOPGPException.CannotDecrypt e) {
            //no-op
        }
        return result;

    }

    public String signOffer(byte[] privateKey, byte[] cipher){
        SOP sop = new SOPImpl();
        byte[] cleartextSignedMessage= null;
        try {
            cleartextSignedMessage = sop.inlineSign()
                    .mode(InlineSignAs.clearsigned) // This MUST be set
                    .key(privateKey)
                    .data(cipher)
                    .getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new String(cleartextSignedMessage,StandardCharsets.UTF_8);

    }

    public void doToggle(View view) {


        if (modeSwitch.isChecked()) {
            sendButton.setVisibility(View.INVISIBLE);
            beDiscoverable(view);
        } else {
            sendButton.setVisibility(View.VISIBLE);
        }
    }

    public void validateCSRF(String token, String jwt, String cookie, View v, RequestQueue rq) {

        //CONFIRM LOGIN

        ValidateAuthRequest getRequest = new ValidateAuthRequest(token, jwt, cookie,
                response -> Snackbar.make(v, "Validation Volley Success", Snackbar.LENGTH_SHORT).setTextMaxLines(10).show(),
                error -> Snackbar.make(v, "Validation Volley Error", Snackbar.LENGTH_LONG).setTextMaxLines(10).show());
        // Add the request to the RequestQueue.
        rq.add(getRequest);


    }

    public void submitHandshake(String token, String jwt, String cookie, String receiver, String title, String encrypted, RequestQueue rq) {

        JSONObject handshakeInfo = new JSONObject();
        try {
            handshakeInfo.put("encryptedDetails", encrypted);
            handshakeInfo.put("handshakeName", title);
            handshakeInfo.put("receiverUsername", receiver);

            CreateHandshakeRequest getRequest = new CreateHandshakeRequest(token, jwt, cookie, handshakeInfo,
                    response -> Snackbar.make(findViewById(R.id.main), "Create Handshake Success", Snackbar.LENGTH_SHORT).setTextMaxLines(10).show(),
                    error -> Snackbar.make(findViewById(R.id.main), "Create Handshake Error", Snackbar.LENGTH_LONG).setTextMaxLines(10).show());
            // Add the request to the RequestQueue.
            rq.add(getRequest);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


    }

}