package com.example.tippy

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.UUID

class BluetoothManager(private val context: Context) {
    // context: Context is a reference to the activity that is passed to the BluetoothManager

    // returns a 'BluetoothAdapter' object that represents the device bluetooth radio
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val connectedDevices = mutableListOf<BluetoothDevice>()
    private val discoveredDevices = mutableListOf<BluetoothDevice>()

    // Check if Bluetooth is supported
    fun isBluetoothSupported(): Boolean {
        return bluetoothAdapter != null
    }

    // discover Bluetooth devices
    fun startDiscovery() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(receiver, filter)
        bluetoothAdapter?.startDiscovery()
    }

    // handles discovered devices
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action: String? = intent?.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? = intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let { discoveredDevices.add(it) }
            }
        }
    }

//    // Pair with a Bluetooth device
//    fun pairDevice(device: BluetoothDevice) {
//        device.createBond()
//    }

    // manage Bluetooth connections
    fun connectToDevice(device: BluetoothDevice, uuid: UUID): BluetoothSocket? {
        return try {
            val socket: BluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            socket.connect()
            connectedDevices.add(device)
            socket
        } catch (e: Exception) {
            Log.e("BluetoothManager", "Failed to connect: ${e.message}")
            null
        }
    }

    fun getConnectedDevices(): List<BluetoothDevice> {
        return connectedDevices
    }

    fun getDiscoveredDevices(): List<BluetoothDevice> {
        return discoveredDevices
    }

    fun autoReconnectDevice(device: BluetoothDevice, uuid: UUID): BluetoothSocket? {
        val handler = Handler(Looper.getMainLooper())
        var socket: BluetoothSocket? = null
        handler.post {
            socket = connectToDevice(device, uuid)
        }
        return socket
    }

    // Clean up resources
    fun cleanUp() {
        context.unregisterReceiver(receiver)
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

}
