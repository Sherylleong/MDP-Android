package com.example.tippy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Path
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var status by mutableStateOf("Not connected")
    val obstaclesList: MutableList<GridObstacle> = mutableStateListOf()
    var car: MutableState<GridCar> = mutableStateOf(GridCar(Coord(2,2), "N"))
    var previewCar: GridCar = GridCar(Coord(2,2), "N")
    var isObstacleDialogShown by mutableStateOf(false)
        private set
    var obstacleDialogMode by mutableStateOf("add")
        private set
    var isCarDialogShown by mutableStateOf(false)
        private set
    var previewObstacle: GridObstacle =  GridObstacle(Coord(0,0), "1", null)
    var draggedObstacleCoord by mutableStateOf<Coord?>(Coord(1,1))
    var draggedOverCoord by mutableStateOf<Coord?>(null)
    val messageViewModel = MessageViewModel()
    var isTimerRunning by mutableStateOf(false)
    lateinit var savedMapsManager: SavedMapsManager



    fun displayObstacleDialog(mode: String) {
        isObstacleDialogShown = true
        obstacleDialogMode = mode
    }

    fun onDismissObstacleDialog() {
        isObstacleDialogShown = false
    }
    fun displayCarDialog() {
        isCarDialogShown = true
    }

    fun onDismissCarDialog() {
        isCarDialogShown = false
    }

    fun onConfirmObstacleDialog() {
        obstaclesList.removeAll { it.coord == previewObstacle.coord }
        obstaclesList.add(previewObstacle)
        isObstacleDialogShown = false
    }
    fun onConfirmCarDialog() {
        car.value = previewCar
        isCarDialogShown = false
    }

    fun updateCarDirection(direction: String){
        car.value = car.value.copy(direction = direction)
    }

    fun checkCoordCarCollide(coord: Coord, carCoord: Coord) : Boolean {
        return (coord.x in carCoord.x-1..carCoord.x+1) && (coord.y in carCoord.y-1..carCoord.y+1)
    }

    fun isBluetoothConnected(): Boolean {
        return BluetoothManager.BluetoothConnectionStatus
    }

}