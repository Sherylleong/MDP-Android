package com.example.tippy

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Button
import android.widget.ArrayAdapter

class BluetoothActivity : AppCompatActivity() {

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var listOfPairedDevices: ListView
    private lateinit var listOfAvailableDevices: ListView
    private lateinit var header: TextView
    private lateinit var bluetoothStatus: TextView
    private lateinit var scanForDevicesButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.bluetooth)

        bluetoothManager = BluetoothManager(this)

        listOfPairedDevices = findViewById(R.id.list_of_paired_devices)
        listOfAvailableDevices = findViewById(R.id.list_of_available_devices)
        header = findViewById(R.id.bluetooth_header)
        bluetoothStatus = findViewById(R.id.bluetooth_status)
        scanForDevicesButton = findViewById(R.id.scan_for_devices_button)

        if (bluetoothManager.isBluetoothSupported()) {
            if (bluetoothManager.isBluetoothEnabled()){
                bluetoothStatus.text = "Status: ON"
            }
        } else {
            bluetoothStatus.text = "Status: OFF"
        }

        // Setup the scan button click listener
        scanForDevicesButton.setOnClickListener {
            bluetoothManager.startDiscovery()
            updateListOfAvailableDevices()
        }

        updateListOfPairedDevices()


//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
    }

    private fun updateListOfAvailableDevices() {
        val availableDevices = bluetoothManager.getDiscoveredDevices()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, availableDevices.map { it.name })
        listOfAvailableDevices.adapter = adapter
        listOfAvailableDevices.setOnItemClickListener { _, _, position, _ ->
            val device = availableDevices[position]
            // Connect to the selected device
            bluetoothManager.connectToDevice(device, YOUR_UUID_HERE)
        }
    }

    private fun updateListOfPairedDevices() {
        val pairedDevices = bluetoothManager.getDiscoveredDevices()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, pairedDevices.map { it.name })
        listOfPairedDevices.adapter = adapter
        listOfPairedDevices.setOnItemClickListener { _, _, position, _ ->
            val device = pairedDevices[position]
            // Connect to the selected device
            bluetoothManager.connectToDevice(device, YOUR_UUID_HERE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.cleanUp()
    }
}