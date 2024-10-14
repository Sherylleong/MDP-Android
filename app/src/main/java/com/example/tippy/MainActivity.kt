package com.example.tippy

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts


data class Coord(val x: Int, val y: Int)
data class GridObstacle(var coord: Coord, var number: String, var direction: String?)
data class GridCar(var coord: Coord, var direction: String)
class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()
    private val MY_PERMISSIONS_REQUEST_CODE = 1

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Bluetooth was enabled
            println("Bluetooth is enabled")
        } else {
            // Bluetooth was not enabled
            println("Bluetooth enabling failed or was cancelled")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        println("OnCreate")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.home);

        // Check and request Bluetooth and location permissions
        if (!hasBluetoothPermissions()) {
            requestBluetoothPermissions()
        }

        val arenaButton: ImageButton = findViewById(R.id.arena_button)
        val bluetoothButton: ImageButton = findViewById(R.id.bluetooth_button)
        val messageButton: ImageButton = findViewById(R.id.message_button)

        arenaButton.setOnClickListener { openArenaPage() }
        bluetoothButton.setOnClickListener { openBluetoothPage() }
        messageButton.setOnClickListener { openMessagePage() }
    }

    private fun hasBluetoothPermissions(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    // Function to request Bluetooth and location permissions
    private fun requestBluetoothPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            MY_PERMISSIONS_REQUEST_CODE
        )
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                println("Bluetooth and Location permissions granted")
            } else {
                println("Bluetooth and Location permissions denied")
            }
        }
    }

    private fun openArenaPage() {
        val intent = Intent(this, Arena::class.java)
        startActivity(intent)
    }

    private fun openBluetoothPage() {
        val intent = Intent(this, Bluetooth::class.java)
        startActivity(intent)
    }

    private fun openMessagePage() {
        val intent = Intent(this, Message::class.java)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        println("onStart")
    }

    override fun onResume() {
        super.onResume()
        println("onResume")
    }

    override fun onPause() {
        super.onPause()
        println("onPause")
    }

    override fun onStop() {
        super.onStop()
        println("onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("onDestroy")
    }

    override fun onRestart() {
        super.onRestart()
        println("onRestart")
    }


}