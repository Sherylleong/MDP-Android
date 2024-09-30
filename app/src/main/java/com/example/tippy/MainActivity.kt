package com.example.tippy

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels


data class Coord(val x: Int, val y: Int)
data class GridObstacle(var coord: Coord, var number: String, var direction: String?)
data class GridCar(var coord: Coord, var direction: String)
class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        println("OnCreate")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.home);

        val arenaButton: ImageButton = findViewById(R.id.arena_button)
        val bluetoothButton: ImageButton = findViewById(R.id.bluetooth_button)
        val messageButton: ImageButton = findViewById(R.id.message_button)

        arenaButton.setOnClickListener { openArenaPage() }
        bluetoothButton.setOnClickListener { openBluetoothPage() }
        messageButton.setOnClickListener { openMessagePage() }

//        setContent {
//            TippyTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    MainScreen(viewModel = viewModel)
//                }
//            }
//        }
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