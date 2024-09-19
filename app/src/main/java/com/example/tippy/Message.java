package com.example.tippy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class Message extends AppCompatActivity {
    TextView messageDisplay;
    private final String TAG  = "Message";

    BroadcastReceiver receiverIncomingMessages = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "RECEIVING");
            String incomingMessage = intent.getStringExtra("receivedMessage");
            updateChatLogRobot(incomingMessage);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message);
        Log.d(TAG, "Message Page");

        messageDisplay = findViewById(R.id.message_history);
        ImageButton sendButton = findViewById(R.id.send_button);

        LocalBroadcastManager.getInstance(this).registerReceiver(receiverIncomingMessages, new IntentFilter("incomingMessage"));

        sendButton.setOnClickListener(v -> sendMessage());
    }

    public void sendMessage() {
        EditText input = findViewById(R.id.edit_message);
        String message = input.getText().toString();
        Log.d(TAG, message);

        if(isBluetoothConnected()){
            byte[] bytes = message.getBytes();
            BluetoothManager.write(bytes);
            updateChatLog(message);
            input.setText("");
        }
        else {
            Toast.makeText(this, "Please connect to Bluetooth!", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateChatLog(String message) {
        String currentText = messageDisplay.getText().toString();
        messageDisplay.setText(currentText + "\n[ME] " + message);
    }

    public void updateChatLogRobot(String message) {
        String currentText = messageDisplay.getText().toString();
        messageDisplay.setText(currentText + "\n[Robot] " + message);
    }

    public boolean isBluetoothConnected() {
        return BluetoothManager.BluetoothConnectionStatus;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverIncomingMessages);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

}
