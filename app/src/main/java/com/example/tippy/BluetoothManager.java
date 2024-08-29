package com.example.tippy;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothManager {

    private static final String TAG = "Bluetooth";
    public static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static BluetoothDevice myDevice;
    private final BluetoothAdapter myBluetoothAdapter;
    private UUID deviceUUID;
    private ConnectThread myConnectThread;
    private static ConnectedThread myConnectedThread;
    public static boolean BluetoothConnectionStatus = false;

    Context myContext;
    ProgressDialog myProgressDialog;
    Intent connectionStatus;

    public BluetoothManager(Context context) {
        this.myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.myContext = context;
    }

    private class ConnectThread extends Thread{
        // A BluetoothSocket object that represents the outgoing Bluetooth connection.
        private BluetoothSocket mySocket;

        public ConnectThread(BluetoothDevice device, UUID u){
            myDevice = device;
            deviceUUID = u;
        }

        @SuppressLint("MissingPermission")
        public void run(){
            BluetoothSocket tmp = null;
            try {
                tmp = myDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mySocket= tmp;
            myBluetoothAdapter.cancelDiscovery();

            try {
                mySocket.connect();
                connected(mySocket,myDevice);
            } catch (IOException e) {
                try {
                    mySocket.close();
                } catch (IOException e1) {
                    e.printStackTrace();
                }

                try {
                    Bluetooth myBluetoothActivity = (Bluetooth) myContext;
                    myBluetoothActivity.runOnUiThread(() -> Toast.makeText(myContext,
                            "Failed to connect to the Device.", Toast.LENGTH_SHORT).show());
                } catch (Exception z) {
                    z.printStackTrace();
                }

            }
            try {
                myProgressDialog.dismiss();
            } catch (NullPointerException e){
                e.printStackTrace();
            }
        }

        public void cancel(){
            try{
                mySocket.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public void startClientThread(BluetoothDevice device, UUID uuid) {
        try {
            myDevice = device;
            myProgressDialog = ProgressDialog.show(myContext, "Connecting Bluetooth", "Please Wait...", true);
        } catch (Exception e) {
            Log.d(TAG, "Failed to connect!");
            e.printStackTrace();
        }

        myConnectThread = new ConnectThread(device, uuid);
        myConnectThread.start();
    }

    private class ConnectedThread extends Thread{
        private final BluetoothSocket mSocket;
        private final InputStream inStream;
        private final OutputStream outStream;
        private boolean stopThread = false;

        @SuppressLint("MissingPermission")
        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            connectionStatus = new Intent("ConnectionStatus");
            connectionStatus.putExtra("Status", "connected");
            connectionStatus.putExtra("Device", myDevice);
            LocalBroadcastManager.getInstance(myContext).sendBroadcast(connectionStatus);
            BluetoothConnectionStatus = true;

//            TextView status = MainActivity.getBluetoothStatus();
//            status.setText(R.string.bt_connected);
//            status.setTextColor(Color.GREEN);
//
//            TextView device = MainActivity.getConnectedDevice();
//            device.setText(myDevice.getName());

            this.mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mSocket.getInputStream();
                tmpOut = mSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inStream = tmpIn;
            outStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];
            int bytes;
            StringBuilder messageBuffer = new StringBuilder();

            while (true){
                try {
                    bytes = inStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: "+ incomingMessage);

                    Intent incomingMessageIntent = new Intent("incomingMessage");
                    incomingMessageIntent.putExtra("receivedMessage", incomingMessage);
                    LocalBroadcastManager.getInstance(myContext).sendBroadcast(incomingMessageIntent);

                } catch (IOException e) {
                    Log.e(TAG, "Error reading input stream. "+e.getMessage());

                    connectionStatus = new Intent("ConnectionStatus");
                    connectionStatus.putExtra("Status", "disconnected");
//                    TextView status = MainActivity.getBluetoothStatus();
//                    status.setText(R.string.bt_disconnect);
//                    status.setTextColor(Color.RED);
                    connectionStatus.putExtra("Device", myDevice);
                    LocalBroadcastManager.getInstance(myContext).sendBroadcast(connectionStatus);
                    BluetoothConnectionStatus = false;

                    break;
                }
            }
        }

        public void write(byte[] bytes){
            String text = new String(bytes, Charset.defaultCharset());
            try {
                outStream.write(bytes);
                Log.d(TAG, "Message out: "+text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel(){
            Log.d(TAG, "cancel: Closing Client Socket");
            try{
                this.stopThread = true;
                mSocket.close();
            } catch(IOException e){
                Log.e(TAG, "cancel: Failed to close ConnectThread mSocket " + e.getMessage());
            }
        }
    }

    private void connected(BluetoothSocket socket, BluetoothDevice device) {
        myDevice = device;
        myConnectedThread = new ConnectedThread(socket);
        myConnectedThread.start();
    }

    public static void write(byte[] out){
        Log.d(TAG, "write is called" );
        myConnectedThread.write(out);
    }
}
