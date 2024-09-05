package com.example.tippy

import android.content.ClipData
import android.content.ClipDescription
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tippy.ui.theme.TippyTheme



@Composable
fun MainScreen(viewModel: MainViewModel){
        var previewCoord: Coord by remember { mutableStateOf(Coord(0, 0)) }
        GridScreen(viewModel)
        if (viewModel.isObstacleDialogShown) {
            ObstacleDialog(onDismiss = {viewModel.onDismissObstacleDialog() },
                onConfirm = {viewModel.onConfirmObstacleDialog()},
                viewModel = viewModel)
    }

}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Grid(
    viewModel: MainViewModel,
) {
    val obstaclesList = viewModel.obstaclesList
    val vehiclePos = viewModel.vehiclePos
    val rows = 20
    val columns = 20
    var draggedObstacleCoord by remember { mutableStateOf<Coord?>(null) }

    LazyVerticalGrid(columns = GridCells.Fixed(columns+2),
        contentPadding = PaddingValues(
            top = 15.dp,
            start = 11.2.dp,
            end = 11.2.dp,
        ),
    ) {
        items((rows + 2) * (columns + 2)) { index ->
            val row = index / (columns + 2)
            val col = index % (columns + 2)
            val isDark = (row + col) % 2 == 0
            val coord = Coord(col, row)
            val hasObstacle = obstaclesList.find { it.coord == coord }
            val number = hasObstacle?.number ?: ""
            val direction = hasObstacle?.direction
            val isSelectedVehicle = null
            val border = (row == 0) or (col==0) or (row == columns+1) or (col == columns+1)
            val backgroundColor = if (isDark) Color(0xFF4CAF50) else Color(0xFF8BC34A)
            val selectedColor = Color.Black
            if (border){
                if ((col==0) and (row >= 1) and (row <= columns)) { // bottom border labels
                    Box(modifier = Modifier
                        .aspectRatio(1f)
                        .wrapContentHeight()
                        //.border(width = 1.dp, color = Color.White)
                        .fillMaxSize()
                    ){
                        Text("$row",
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .wrapContentHeight()
                                //.border(width = 1.dp, color = Color.White)
                                .fillMaxSize()
                        )
                    }
                }
                else if ((row==columns+1) and (col >= 1) and (col <= columns)) { // leftmost border labels
                    Box(modifier = Modifier
                        .aspectRatio(1f)
                        .wrapContentHeight()
                        //.border(width = 1.dp, color = Color.White)
                        .fillMaxSize()
                    ){


                        Text("$col",
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .wrapContentHeight()
                                //.border(width = 1.dp, color = Color.White)
                                .fillMaxSize()


                        )
                    }
                }
                else { // other borders
                    Box(modifier = Modifier
                        .aspectRatio(1f)
                        .wrapContentHeight()
                        //.border(width = 1.dp, color = Color.White)
                        .fillMaxSize()
                    ){
                        Text("")

                    }
                }
            }
            else {
                val context = LocalContext.current
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(if (hasObstacle != null) selectedColor else backgroundColor)
                        .fillMaxSize()
                        .drawBehind {
                            val strokeWidth = 5.dp.toPx() * density
                            var start: Offset = Offset(0f, 0f)
                            var end: Offset = Offset(0f, 0f)
                            when (direction) {
                                "north" -> {
                                    start = Offset(0f, strokeWidth / 2)
                                    end = Offset(size.width, strokeWidth / 2)
                                }
                                "south" -> {
                                    start = Offset(0f, size.height - strokeWidth / 2)
                                    end = Offset(size.width, size.height - strokeWidth / 2)
                                }
                                "east" -> {
                                    start = Offset(size.width - strokeWidth / 2, 0f)
                                    end = Offset(size.width - strokeWidth / 2, size.height)
                                }
                                "west" -> {
                                    start = Offset(strokeWidth / 2,0f)
                                    end = Offset(strokeWidth / 2, size.height)
                                }
                            }
                            if (direction != null) {
                                drawLine(
                                    Color.Red,
                                    start,
                                    end,
                                    strokeWidth
                                )
                            }
                        }
                        .clickable {
                            if (hasObstacle != null) {
                                viewModel.previewObstacle = hasObstacle
                                viewModel.displayObstacleDialog()
                                //Toast.makeText(context, "Error: An obstacle is already placed there!", Toast.LENGTH_SHORT).show()
                                //obstaclesList.remove(coord)

                            } else {
                                //obstaclesList.add(Coord(col, row))
                            }

                            println("${obstaclesList}")
                        }

                        .dragAndDropSource { // if obstacle is there, can drag elsewhere
                            if (hasObstacle != null) {
                                detectDragGestures { change, dragAmount ->
                                    draggedObstacleCoord = hasObstacle.coord
                                    startTransfer(
                                        transferData = DragAndDropTransferData(
                                            clipData = ClipData.newPlainText(
                                                "obstacle",
                                                "existing"
                                            )
                                        )
                                    )
                                }
                            }

                        }


                        //.border(width = 0.5.dp, color = Color.White)

                        .dragAndDropTarget(
                            shouldStartDragAndDrop = { event ->
                                event
                                    .mimeTypes()
                                    .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                            },
                            target = remember {

                                object : DragAndDropTarget {

                                    override fun onDrop(event: DragAndDropEvent): Boolean {
                                        val label = event.toAndroidDragEvent().clipDescription.label
                                        val text =
                                            event.toAndroidDragEvent().clipData?.getItemAt(0)?.text

                                        if (label == "obstacle") {
                                            if (text == "new") {
                                                if (!obstaclesList.any { it.coord == coord }) { // object not already there
                                                    viewModel.previewObstacle =
                                                        GridObstacle(coord, "1", null)
                                                    viewModel.displayObstacleDialog()
                                                    // Toast.makeText(context, "Successfully placed obstacle!", Toast.LENGTH_SHORT).show()

                                                } else {
                                                    Toast
                                                        .makeText(
                                                            context,
                                                            "Invalid: Something is already placed there!",
                                                            Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                                }
                                            } else { // existing drag
                                                println(draggedObstacleCoord)
                                                if ((!obstaclesList.any { it.coord == coord }) or (coord == draggedObstacleCoord) ) {
                                                    val index = obstaclesList.indexOfFirst { it.coord == draggedObstacleCoord }
                                                    obstaclesList[index] = GridObstacle(coord, obstaclesList[index].number, obstaclesList[index].direction)
                                                } else {
                                                    Toast
                                                        .makeText(
                                                            context,
                                                            "Invalid: Something is already placed there!",
                                                            Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                                }

                                            }

                                        }
                                        return true
                                    }
                                }
                            }
                        )
                ) {
                    Text (
                        number,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                            .wrapContentHeight(),

                        style = TextStyle(
                            color = Color.White,
                            textAlign = TextAlign.Center // Center align the text
                        ),
                    )
                }
            }

        }
    }
}




@Composable
fun GridScreen(viewModel: MainViewModel) {
    Column (horizontalAlignment = Alignment.CenterHorizontally,){
        Grid(viewModel)
        ObstacleDraggable()
        GridLog(viewModel)
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ObstacleDraggable() {
    Box (
        modifier = Modifier
            .background(color = Color.Red)
            //.heightIn(max = 30.dp)
            .dragAndDropSource {
                detectTapGestures(
                    onPress = { offset ->

                        startTransfer(
                            transferData = DragAndDropTransferData(
                                clipData = ClipData.newPlainText(
                                    "obstacle",
                                    "new"
                                )
                            )
                        )
                    })
            }

    ){
        Text("Add Obstacle",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .wrapContentHeight()
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CarDraggable() {
    Box (
        modifier = Modifier
            .background(color = Color.Cyan)
            //.heightIn(max = 30.dp)
            .dragAndDropSource {
                detectTapGestures(
                    onPress = { offset ->

                        startTransfer(
                            transferData = DragAndDropTransferData(
                                clipData = ClipData.newPlainText(
                                    "obstacle",
                                    "north"
                                )
                            )
                        )
                    })
            }

    ){
        Text("Add Obstacle",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .wrapContentHeight()
        )
    }
}

@Composable
fun GridLog(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    LazyColumn {
        item {
            Text("selected coordinates:")
        }
        items(viewModel.obstaclesList) { obj ->
            Text(text = "(x=${obj.coord.x}, y=${obj.coord.y}, number=${obj.number}, dir=${obj.direction})")
        }
    }
}



