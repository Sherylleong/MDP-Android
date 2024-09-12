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
    var car: MutableState<GridCar> = mutableStateOf(GridCar(Coord(2,2), "north"))
    var previewCar: GridCar = GridCar(Coord(2,2), "north")
    var isObstacleDialogShown by mutableStateOf(false)
        private set
    var isCarDialogShown by mutableStateOf(false)
        private set
    var previewObstacle: GridObstacle =  GridObstacle(Coord(0,0), "1", null)
    var draggedObstacleCoord by mutableStateOf<Coord?>(Coord(1,1))
    var draggedOverCoord by mutableStateOf<Coord?>(null)


    fun displayObstacleDialog() {
        isObstacleDialogShown = true
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

    fun checkCoordCarCollide(coord: Coord, carCoord: Coord) : Boolean {
        return (coord.x in carCoord.x-1..carCoord.x+1) && (coord.y in carCoord.y-1..carCoord.y+1)
    }

}