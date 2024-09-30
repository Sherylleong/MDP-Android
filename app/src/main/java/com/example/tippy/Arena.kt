package com.example.tippy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.tippy.MessageViewModel

class Arena : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val messageViewModel: MessageViewModel by viewModels() // Add this line to get MessageViewModel
    val TAG = "Arena"

    var receiverIncomingMessages: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "RECEIVING")
            val incomingMessage = intent.getStringExtra("receivedMessage")
//            if (incomingMessage != null) {
//                messageViewModel.updateChatLogRobot(incomingMessage)
//            }
            // handle bluetooth incoming messages
            if (incomingMessage != null){
                Log.d(TAG, "MEZSAAGESGE")
                val strs = incomingMessage.split(",").toTypedArray()
                try {
                    when (strs[0]) {
                        "TARGET" -> { // change number on obstacle
                            val oriNumber = strs[1]
                            val newNumber = strs[2]
                            var direction: String? = null
                            if (strs.size == 4) { // there is a 4th str for direction
                                direction = strs[3].lowercase()
                            }
                            println(oriNumber+ newNumber)
                            val toReplaceNumberObstacle = viewModel.obstaclesList.find { it.number == oriNumber }
                            if (toReplaceNumberObstacle != null) {
                                val toAddObstacle = GridObstacle(toReplaceNumberObstacle.coord, newNumber ,direction)
                                viewModel.obstaclesList.remove(toReplaceNumberObstacle)
                                viewModel.obstaclesList.add(toAddObstacle)
                            }
                        }
                        "ROBOT" -> { //change car details ROBOT, <x>, <y>, <direction>
                            val x = strs[1].toInt()
                            val y = strs[2].toInt()
                            val direction = strs[3]
                            val toUpdateCar = GridCar(Coord(x,y), direction)
                            viewModel.car.value = toUpdateCar
                            println(toUpdateCar)
                            }
                        "STATUS" -> {
                            viewModel.status = strs[1]
                        }
                    }
                }
                finally{}

            }

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val savedMapsManager = SavedMapsManager(context = this)
        viewModel.savedMapsManager = savedMapsManager
        setContent {
            MainScreen(viewModel = viewModel)
        }

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiverIncomingMessages, IntentFilter("incomingMessage"))
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverIncomingMessages)
        super.onDestroy()
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverIncomingMessages)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }
}

