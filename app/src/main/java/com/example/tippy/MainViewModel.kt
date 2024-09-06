package com.example.tippy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val obstaclesList: MutableList<GridObstacle> = mutableStateListOf()
    var vehiclePos: MutableState<GridObstacle?> = mutableStateOf(null)
    var isObstacleDialogShown by mutableStateOf(false)
        private set
    var previewObstacle: GridObstacle =  GridObstacle(Coord(0,0), "1", null)
    var draggedObstacleCoord by mutableStateOf<Coord?>(Coord(1,1))


    fun displayObstacleDialog() {
        isObstacleDialogShown = true
    }

    fun onDismissObstacleDialog() {
        isObstacleDialogShown = false
    }

    fun onConfirmObstacleDialog() {
        obstaclesList.removeAll { it.coord == previewObstacle.coord }
        obstaclesList.add(previewObstacle)
        isObstacleDialogShown = false
    }
}