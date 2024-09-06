//package com.example.tippy;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.ImageButton;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//public class Main extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.home);
//
//        ImageButton arenaButton = findViewById(R.id.arena_button);
//        ImageButton bluetoothButton = findViewById(R.id.bluetooth_button);
//        ImageButton messageButton = findViewById(R.id.message_button);
//
//        arenaButton.setOnClickListener(v -> openArenaPage());
//        bluetoothButton.setOnClickListener(v -> openBluetoothPage());
//        messageButton.setOnClickListener(v -> openMessagePage());
//    }
//
//    public void openArenaPage() {
//        Intent intent = new Intent(this, MainScreenKt.class);
//        startActivity(intent);
//    }
//
//
//    public void openBluetoothPage() {
//        Intent intent = new Intent(this, Bluetooth.class);
//        startActivity(intent);
//
//    }
//
//    public void openMessagePage() {
//        Intent intent = new Intent(this, Message.class);
//        startActivity(intent);
//    }
//}