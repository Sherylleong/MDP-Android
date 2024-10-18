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
import org.json.JSONObject
val idToCharMap: Map<String, String> = mapOf(
    "20" to "A",
    "21" to "B",
    "22" to "C",
    "23" to "D",
    "24" to "E",
    "25" to "F",
    "26" to "G",
    "27" to "H",
    "28" to "S",
    "29" to "T",
    "30" to "U",
    "31" to "V",
    "32" to "W",
    "33" to "X",
    "34" to "Y",
    "35" to "Z",
    "36" to "Up",
    "37" to "Down",
    "38" to "Right",
    "39" to "Left",
    "40" to "Circle",
    "41" to "Bullseye",
    "11" to "1",
    "12" to "2",
    "13" to "3",
    "14" to "4",
    "15" to "5",
    "16" to "6",
    "17" to "7",
    "18" to "8",
    "19" to "9",
    "99" to "?"
)
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
                try {
                    val jsonObject = JSONObject(incomingMessage)
                    val cat = jsonObject.getString("cat")
                    when (cat) {
                        "location" -> {
                            val valueObject = jsonObject.getJSONObject("value")
                            val x = valueObject.getInt("x")
                            val y = valueObject.getInt("y")
                            val direction = valueObject.getString("d")
                            viewModel.car.value = GridCar(Coord(x,y), direction)
                        }
                        "image-rec" -> {
                            val valueObject = jsonObject.getJSONObject("value")
                            val obstacle_id = valueObject.getString("obstacle_id")
                            val image_id = valueObject.getString("image_id")
                            val toReplaceNumberObstacle = viewModel.obstaclesList.find { it.number == obstacle_id }
                            lateinit var toAddObstacle: GridObstacle
                            if (toReplaceNumberObstacle != null) {
                                if (idToCharMap.containsKey(image_id)){
                                    toAddObstacle = GridObstacle(toReplaceNumberObstacle.coord, idToCharMap[image_id]!! ,null)
                                }
                                else {
                                    toAddObstacle = GridObstacle(toReplaceNumberObstacle.coord, "?", null)
                                }
                                viewModel.obstaclesList.remove(toReplaceNumberObstacle)
                                viewModel.obstaclesList.add(toAddObstacle)

                            }
                        }
                        "status" -> {
                            val value = jsonObject.getString("value")
                            if (value == "finished") {
                                viewModel.isTimerRunning = false // Stop the timer when status is finished
                            }
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

