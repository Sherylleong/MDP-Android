package com.example.tippy;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.annotation.SuppressLint;


import androidx.activity.EdgeToEdge;
import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class Bluetooth extends AppCompatActivity {

    private static final String TAG = "Bluetooth";
    private String connStatus;
    BluetoothAdapter myBluetoothAdapter;
    public ArrayList<BluetoothDevice> myNewBTDevices;
    public ArrayList<BluetoothDevice> myPairedBTDevices;
    public DeviceListAdapter myNewDeviceListAdapter;
    public DeviceListAdapter myPairedDeviceListAdapter;
    TextView connectionStatusTextView;
    ListView listOfAvailableDevices;
    ListView listOfPairedDevices;
    Button connectButton;
    Button scanButton;
    ProgressDialog myDialog;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    BluetoothManager myBluetoothConnection;
    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static BluetoothDevice myDevice;

    boolean retryConnection = false;
    Handler reconnectionHandler = new Handler();

    private Handler connectionHandler = new Handler();
    private Runnable connectionStatusChecker = new Runnable() {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            if (myDevice != null && myDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                connectionStatusTextView.setText("Connected to " + myDevice.getName());
            } else {
                connectionStatusTextView.setText("Disconnected");
            }

            // Repeat this runnable code block again after 2 seconds
            connectionHandler.postDelayed(this, 2000);
        }
    };


    Runnable reconnectionRunnable = new Runnable() {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            try {
                if (!BluetoothManager.BluetoothConnectionStatus) {
//                    myBluetoothConnection = new BluetoothManager(Bluetooth.this);
                    startBTConnection(myDevice, myUUID);
                    // Check the connection status after the attempt
                    if (!BluetoothManager.BluetoothConnectionStatus) {
                        // If still not connected, show a Toast and reschedule
                        Toast.makeText(Bluetooth.this, "Failed to reconnect, retry in 5s", Toast.LENGTH_SHORT).show();
                        reconnectionHandler.postDelayed(reconnectionRunnable, 5000); // Retry after 5 seconds
                    }
                    else {
                        Toast.makeText(Bluetooth.this, "Reconnection Success", Toast.LENGTH_SHORT).show();
                    }

                }
                reconnectionHandler.removeCallbacks(reconnectionRunnable);
                retryConnection = false;
            } catch (Exception e) {
                Toast.makeText(Bluetooth.this, "Failed to reconnect, retry in 5s", Toast.LENGTH_SHORT).show();
                reconnectionHandler.postDelayed(reconnectionRunnable, 5000); // Retry after 5 seconds
            }
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.bluetooth);
        Log.d(TAG,"CREATED");

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        connectionHandler.post(connectionStatusChecker);

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        SwitchCompat bluetoothSwitch = findViewById(R.id.bluetoothSwitch);
        if (myBluetoothAdapter.isEnabled()) {
            bluetoothSwitch.setChecked(true);
            bluetoothSwitch.setText("ON");
        }
//        TextView connectStatus = findViewById(R.id.connectionStatus);
//        if(myBluetoothAdapter.isEnabled()){
//            Log.d(TAG, "NO");
//            connectStatus.setText("ON");
//        }

        listOfAvailableDevices = findViewById(R.id.list_of_available_devices);
        listOfPairedDevices = findViewById(R.id.list_of_paired_devices);
        myNewBTDevices = new ArrayList<>();
        myPairedBTDevices = new ArrayList<>();
        connectButton = findViewById(R.id.connectButton);
        scanButton = findViewById(R.id.scan_for_devices_button);

        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiverHandleOnOff, BTIntent);
        IntentFilter discoverIntent = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(receiverHandleScan, discoverIntent);
        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiverHandleNewDevices, discoverDevicesIntent);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(receiverHandlePairing, filter);

        IntentFilter filter2 = new IntentFilter("ConnectionStatus");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiverHandleConnections, filter2);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Scanning for devices..");
                scanForDevices(view);
            }
        });

        listOfAvailableDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                myBluetoothAdapter.cancelDiscovery();
//                listOfPairedDevices.setAdapter(myPairedDeviceListAdapter);

                String deviceName = myNewBTDevices.get(i).getName();
                String deviceAddress = myNewBTDevices.get(i).getAddress();
                Log.d(TAG, "onItemClick: A device is selected.");
                Log.d(TAG, "onItemClick: DEVICE NAME: " + deviceName);
                Log.d(TAG, "onItemClick: DEVICE ADDRESS: " + deviceAddress);

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    Log.d(TAG, "onItemClick: Initiating pairing with " + deviceName);
                    myNewBTDevices.get(i).createBond();

                    myBluetoothConnection = new BluetoothManager(Bluetooth.this);
                    myDevice = myNewBTDevices.get(i);
                }
            }
        });

        listOfPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                myBluetoothAdapter.cancelDiscovery();
                listOfAvailableDevices.setAdapter(myNewDeviceListAdapter);

                String deviceName = myPairedBTDevices.get(i).getName();
                String deviceAddress = myPairedBTDevices.get(i).getAddress();
                Log.d(TAG, "onItemClick: A device is selected.");
                Log.d(TAG, "onItemClick: DEVICE NAME: " + deviceName);
                Log.d(TAG, "onItemClick: DEVICE ADDRESS: " + deviceAddress);
                BluetoothManager.myDevice = myPairedBTDevices.get(i);
                Toast.makeText(Bluetooth.this, deviceName, Toast.LENGTH_SHORT).show();
                myBluetoothConnection = new BluetoothManager(Bluetooth.this);
                myDevice = myPairedBTDevices.get(i);
            }
        });

        // on and off bluetooth switch
        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                try {
                    Log.d(TAG, "onChecked: Switch button toggled. Enabling/Disabling Bluetooth");
                    if (isChecked) {
                        compoundButton.setText("ON");
                    } else {
                        compoundButton.setText("OFF");
                    }

                    if (myBluetoothAdapter == null) {
                        Log.d(TAG, "enableDisableBT: Device does not support Bluetooth capabilities!");
                        Toast.makeText(Bluetooth.this, "Device Does Not Support Bluetooth capabilities!", Toast.LENGTH_LONG)
                                .show();
                        compoundButton.setChecked(false);
                    } else {
                        if (!myBluetoothAdapter.isEnabled()) {
                            Log.d(TAG, "enableDisableBT: enabling Bluetooth");
                            Log.d(TAG, "enableDisableBT: Making device discoverable for 600 seconds.");

                            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 600);
                            startActivity(discoverableIntent);

                            compoundButton.setChecked(true);

                            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                            registerReceiver(receiverHandleOnOff, BTIntent);

                            IntentFilter discoverIntent = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                            registerReceiver(receiverHandleScan, discoverIntent);
                        }
                        if (myBluetoothAdapter.isEnabled()) {
                            Log.d(TAG, "enableDisableBT: disabling Bluetooth");
                            myBluetoothAdapter.disable();

                            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                            registerReceiver(receiverHandleOnOff, BTIntent);
                        }
                    }
                } catch (Exception e){
                    Toast.makeText(Bluetooth.this, "Please turn on Bluetooth first!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // connect button
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myDevice == null) {
                    Toast.makeText(Bluetooth.this, "Please select a device before connecting.", Toast.LENGTH_LONG)
                            .show();
                } else {
                    startConnection();
                }
            }
        });

        connectionStatusTextView = (TextView) findViewById(R.id.connectionStatus);
        connStatus = "Disconnected";
        connectionStatusTextView.setText(connStatus);

        myDialog = new ProgressDialog(Bluetooth.this);
        myDialog.setMessage("Waiting for other device to reconnect...");
        myDialog.setCancelable(false);
        myDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        connectionHandler.removeCallbacks(connectionStatusChecker); // Stop the checker when the activity is paused
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connectionHandler.removeCallbacks(connectionStatusChecker); // Stop the checker when the activity is destroyed
    }


    // Search Button
    @SuppressLint("MissingPermission")
    public void scanForDevices(View view) {
        Log.d(TAG, "scanForDevices(): Scanning for unpaired devices.");
        myNewBTDevices.clear();
        try {
            if (myBluetoothAdapter != null) {
//            Log.d(TAG, "myBluetoothAdapter != null");
                if (!myBluetoothAdapter.isEnabled()) {
                    Toast.makeText(Bluetooth.this, "Please turn on Bluetooth first!", Toast.LENGTH_SHORT).show();
                }
                if (myBluetoothAdapter.isDiscovering()) {
                    myBluetoothAdapter.cancelDiscovery();
                    Log.d(TAG, "toggleButton: Cancelling Discovery.");
                    checkBTPermissions();

                    myBluetoothAdapter.startDiscovery();
                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(receiverHandleNewDevices, discoverDevicesIntent);
                }
                else if (!myBluetoothAdapter.isDiscovering()) {
                    checkBTPermissions();

                    myBluetoothAdapter.startDiscovery();
                    Log.d(TAG, "startDiscovery() is running");
                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(receiverHandleNewDevices, discoverDevicesIntent);
                }

                myPairedBTDevices.clear();
                Set<BluetoothDevice> pairedDevices = myBluetoothAdapter.getBondedDevices();
                Log.d(TAG, "toggleButton: Number of paired devices found: " + pairedDevices.size());

                for (BluetoothDevice d : pairedDevices) {
                    Log.d(TAG, "Paired Devices: " + d.getName() + " : " + d.getAddress());
                    myPairedBTDevices.add(d);
                    myPairedDeviceListAdapter = new DeviceListAdapter(this, R.layout.device_adapter_view,
                            myPairedBTDevices);
                    listOfPairedDevices.setAdapter(myPairedDeviceListAdapter);
                }

//            Log.d(TAG, "toggleButton: Number of paired devices found now is: " + pairedDevices.size());
            }
        } catch (Exception e){
            Toast.makeText(Bluetooth.this, "Please turn on Bluetooth first!", Toast.LENGTH_SHORT).show();
        }

    }

    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
        } else {
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    private final BroadcastReceiver receiverHandleOnOff = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "receiverHandleOnOff: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "receiverHandleOnOff: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "receiverHandleOnOff: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "receiverHandleOnOff: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver receiverHandleScan = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                final int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "receiverHandleScan: Discoverability Enabled.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "receiverHandleScan: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "receiverHandleScan: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "receiverHandleScan: Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "receiverHandleScan: Connected.");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver receiverHandleNewDevices = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if(action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState()!=BluetoothDevice.BOND_BONDED) {
                    myNewBTDevices.add(device);
                    Log.d(TAG, "onReceive: " + device.getName() + " : " + device.getAddress());
                    myNewDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, myNewBTDevices);
                    listOfAvailableDevices.setAdapter(myNewDeviceListAdapter);
                }

            }
        }
    };

    private final BroadcastReceiver receiverHandlePairing = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice myNewDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int bondState = myNewDevice.getBondState();

                if (bondState == BluetoothDevice.BOND_BONDED){
                    myPairedBTDevices.clear();

                    Set<BluetoothDevice> pairedDevices = myBluetoothAdapter.getBondedDevices();
                    Log.d(TAG, "toggleButton: Number of paired devices found: "+ pairedDevices.size());

                    for(BluetoothDevice d : pairedDevices){
                        Log.d(TAG, "Paired Devices: " + d.getName() + " : " + d.getAddress());
                        myPairedBTDevices.add(d);
                        myPairedDeviceListAdapter = new DeviceListAdapter(Bluetooth.this, R.layout.device_adapter_view, myPairedBTDevices);
                        listOfPairedDevices.setAdapter(myPairedDeviceListAdapter);
                    }

                    myDevice = myNewDevice;
                    connectionStatusTextView.setText("Connected to: " + myDevice.getName());
                    Toast.makeText(Bluetooth.this, "Successfully paired with " + myDevice, Toast.LENGTH_SHORT).show();

                    Log.d(TAG, "BOND_BONDED to: " + myDevice + myUUID);
//                    myBluetoothConnection = new BluetoothManager(Bluetooth.this);
//                    startBTConnection(myDevice, myUUID);
                }
                if(bondState== BluetoothDevice.BOND_BONDING){
                    Log.d(TAG, "BOND_BONDING.");
                }
                if(bondState == BluetoothDevice.BOND_NONE){
                    Log.d(TAG, "BOND_NONE.");
                }
            }
        }
    };

    private final BroadcastReceiver receiverHandleConnections = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice myBTDevice = intent.getParcelableExtra("Device");
            Log.d(TAG, "bluetooth screen now");
            String status = intent.getStringExtra("Status");
            sharedPreferences = getApplicationContext().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
            TextView connStatusTextView = findViewById(R.id.connectionStatus);

            Log.d(TAG, status);

            if(status.equals("connected")){
                try {
                    myDialog.dismiss();
                } catch(NullPointerException e){
                    e.printStackTrace();
                }

                Log.d(TAG, "receiverHandleConnections: Device now connected to "+myBTDevice.getName());
                Toast.makeText(Bluetooth.this, "Device now connected to "+myBTDevice.getName(), Toast.LENGTH_SHORT).show();
                editor.putString("connStatus", "Connected to " + myBTDevice.getName());
                connStatusTextView.setText("Connected to " + myBTDevice.getName());

            }
            else if(status.equals("disconnected") && !retryConnection){
                Log.d(TAG, "receiverHandleConnections: Disconnected from "+myBTDevice.getName());
                Toast.makeText(Bluetooth.this, "Disconnected from "+myBTDevice.getName(), Toast.LENGTH_SHORT).show();
                myBluetoothConnection = new BluetoothManager(Bluetooth.this);


                sharedPreferences = getApplicationContext().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
                editor = sharedPreferences.edit();
                editor.putString("connStatus", "Disconnected");

                connStatusTextView.setText("Disconnected");

                editor.commit();

                try {
                    myDialog.show();
                }catch (Exception e){
                    Log.d(TAG, "Bluetooth: receiverHandleConnections Dialog show failure");
                }
                retryConnection = true;
                reconnectionHandler.postDelayed(reconnectionRunnable, 5000);

            }
            if (status.equals("disconnected"))
                connectionStatusTextView.setText("Disconnected");
            editor.commit();
        }
    };


    public void startConnection(){
        startBTConnection(myDevice,myUUID);
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection");
        myBluetoothConnection.startClientThread(device, uuid);
    }

//    @Override
//    protected void onDestroy() {
//        Log.d(TAG, "onDestroy: called");
//        super.onDestroy();
//        try {
//            unregisterReceiver(receiverHandleOnOff);
//            unregisterReceiver(receiverHandleScan);
//            unregisterReceiver(receiverHandleNewDevices);
//            unregisterReceiver(receiverHandlePairing);
//            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverHandleConnections);
//        } catch(IllegalArgumentException e){
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        Log.d(TAG, "onPause: called");
//        super.onPause();
//        try {
//            unregisterReceiver(receiverHandleOnOff);
//            unregisterReceiver(receiverHandleScan);
//            unregisterReceiver(receiverHandleNewDevices);
//            unregisterReceiver(receiverHandlePairing);
//            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverHandleConnections);
//        } catch(IllegalArgumentException e){
//            e.printStackTrace();
//        }
//    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("myDevice", myDevice);
        data.putExtra("myUUID",myUUID);
        setResult(RESULT_OK, data);
        super.finish();
    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//
//        // Save your list of paired devices to the Bundle
//        outState.putParcelableArrayList("paired_devices", myPairedBTDevices);
//    }
}