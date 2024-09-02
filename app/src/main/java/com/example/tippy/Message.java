package com.example.tippy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class Message extends AppCompatActivity {
    private TextView messageDisplay;

    private BroadcastReceiver receiverIncomingMessages = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String incomingMessage = intent.getStringExtra("messageKey");
            if (incomingMessage != null) {
                updateChatLog(incomingMessage);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message);

        messageDisplay = findViewById(R.id.message_history);
        ImageButton sendButton = findViewById(R.id.send_button);

        LocalBroadcastManager.getInstance(this).registerReceiver(receiverIncomingMessages, new IntentFilter("inputMessage"));

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    public void sendMessage() {
        EditText input = findViewById(R.id.edit_message);
        String message = input.getText().toString();

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
        messageDisplay.setText(currentText + "\n[ME]" + message);
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
