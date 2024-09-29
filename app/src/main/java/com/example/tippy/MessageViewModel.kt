package com.example.tippy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MessageViewModel : ViewModel() {
    var messageHistory by mutableStateOf("")
        private set

    private val receiverIncomingMessages = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("MessageViewModel", "RECEIVING")
            val incomingMessage = intent.getStringExtra("receivedMessage")
            if (incomingMessage != null) {
                Log.d("MessageViewModel", "This works")
                updateChatLogRobot(incomingMessage) // Update message history in ViewModel
            } else {
                Log.e("MessageViewModel", "Received message is null")
            }
        }
    }

    private var isReceiverRegistered = false // Flag to track registration

    fun registerReceiver(context: Context) {
        if (!isReceiverRegistered) { // Check if receiver is already registered
            val filter = IntentFilter("incomingMessage")
            LocalBroadcastManager.getInstance(context).registerReceiver(receiverIncomingMessages, filter)
            isReceiverRegistered = true // Set the flag to true
        }
    }

    fun unregisterReceiver(context: Context) {
        if (isReceiverRegistered) { // Check if receiver is registered before unregistering
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiverIncomingMessages)
            isReceiverRegistered = false // Reset the flag
        }
    }

    private fun updateChatLog(message: String) {
        messageHistory += "\n[ME] $message"
    }

    fun updateChatLogRobot(message: String) {
        messageHistory += "\n[Robot] $message"
    }

    fun sendMessage(message: String, isBluetoothConnected: () -> Boolean) {
        if (isBluetoothConnected()) {
            val bytes = message.toByteArray()
            BluetoothManager.write(bytes)
            updateChatLog(message)
        } else {
            // Handle Bluetooth not connected
            Log.d("MessageViewModel", "Please connect to Bluetooth!")
        }
    }
}

@Composable
fun CommunicationsTab(viewModel: MessageViewModel, isBluetoothConnected: () -> Boolean) {
    var message by remember { mutableStateOf("") }

    // Remember a context to use in the BroadcastReceiver
    val context = LocalContext.current

    // This will register the receiver when the composable enters the composition
    LaunchedEffect(Unit){
        viewModel.registerReceiver(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display Message History Title
        Text(
            text = "Message History:",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Show the message history
        val listState = rememberLazyListState()

        // Split messageHistory into a list before the LazyColumn
        val messages = viewModel.messageHistory.split("\n")

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
                .border(1.dp, Color.Gray),
            state = listState
        ) {
            items(messages) { message ->
                Text(
                    text = message,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

// Use LaunchedEffect to scroll to the bottom whenever messageHistory changes
        LaunchedEffect(viewModel.messageHistory) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }


        Spacer(modifier = Modifier.height(8.dp))

        // Input field for new messages
        BasicTextField(
            value = message,
            onValueChange = { message = it },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray)
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Send button
        Button(onClick = {
            viewModel.sendMessage(message, isBluetoothConnected)
            message = "" // Clear input field after sending
        }) {
            Text("Send")
        }
    }
}
