package com.example.tippy

import android.content.ClipData
import android.content.ClipDescription
import android.view.MotionEvent
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.example.tippy.ui.theme.TippyTheme
import kotlinx.coroutines.flow.callbackFlow


@Composable
fun MainScreen(viewModel: MainViewModel){
    var previewCoord: Coord by remember { mutableStateOf(Coord(0, 0)) }
    GridScreen(viewModel)
    if (viewModel.isObstacleDialogShown) {
        ObstacleDialog(onDismiss = {viewModel.onDismissObstacleDialog() },
            onConfirm = {viewModel.onConfirmObstacleDialog()},
            viewModel = viewModel)
}
    if (viewModel.isCarDialogShown) {
        CarDialog(onDismiss = {viewModel.onDismissCarDialog() },
            onConfirm = {viewModel.onConfirmCarDialog()},
            viewModel = viewModel)
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun Grid(
    viewModel: MainViewModel,
) {
    val obstaclesList = viewModel.obstaclesList
    val car by remember { viewModel.car }
    val rows = 20
    val columns = 20
    //var draggedObstacleCoord by remember { mutableStateOf<Coord?>(null) }
    var draggedObstacleCoord = viewModel.draggedObstacleCoord
    var carPosXAbs by remember { mutableStateOf(1) }
    var carPosYAbs by remember { mutableStateOf(1) }
    LazyVerticalGrid(columns = GridCells.Fixed(columns+2),
        contentPadding = PaddingValues(
            top = 15.dp,
            start = 11.2.dp,
            end = 11.2.dp,
        ),
        modifier = Modifier
    ) {

        items((rows + 2) * (columns + 2)) { index ->
            val row =  21 - (index / (columns + 2))
            val col = index % (columns + 2)
            val isDark = (row + col) % 2 == 0
            val coord = Coord(col, row)
            val hasObstacle = obstaclesList.find { it.coord == coord }
            val number = hasObstacle?.number ?: ""
            val direction = hasObstacle?.direction
            val isSelectedVehicle = null
            val panColor = Color.Red
            var draggedOver by remember {mutableStateOf(false)}
            val border = (row == 0) or (col==0) or (row == columns+1) or (col == columns+1)
            val backgroundColor = if (draggedOver) panColor else if (isDark) Color(0xFF4CAF50) else Color(0xFF8BC34A)
            val selectedColor = Color.Black



            if (border){
                if ((col==0) and (row >= 1) and (row <= columns)) { // leftmost column labels
                    var draggedOverRow = false
                   if (coord.y == viewModel.draggedOverCoord?.y){
                       draggedOverRow = true
                   }
                    Box(modifier = Modifier
                        .aspectRatio(1f)
                        .wrapContentHeight()
                        //.border(width = 1.dp, color = Color.White)
                        .fillMaxSize()
                        .background(if (draggedOverRow) Color.Red else Color.Transparent)
                    ){
                        Text("$row",
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp,
                            color = Color.White,
                            modifier = Modifier
                                .aspectRatio(1f)
                                //.border(width = 1.dp, color = Color.White)
                                .fillMaxSize()
                                .wrapContentHeight(align = Alignment.CenterVertically),
                        )
                    }
                }
                else if ((row==0) and (col >= 1) and (col <= columns)) { // bottom borders labels
                    var draggedOverCol = false
                    if (coord.x == viewModel.draggedOverCoord?.x){
                        draggedOverCol = true
                    }
                    Box(modifier = Modifier
                        .aspectRatio(1f)
                        .wrapContentHeight()
                        //.border(width = 1.dp, color = Color.White)
                        .fillMaxSize()
                        .background(if (draggedOverCol) Color.Red else Color.Transparent)
                    ){


                        Text("$col",
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp,
                            color = Color.White,
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
                        .background(if (hasObstacle != null) selectedColor  else backgroundColor)
                        .fillMaxSize()
                        .zIndex(if (coord == car.coord) 2f else 1f)
                        .drawWithCache { // draw car
                            val roundedPolygon = RoundedPolygon(
                                numVertices = 3,
                                radius = (size.minDimension * 1.5).toFloat(),
                                centerX = size.width / 2,
                                centerY = size.height / 2,
                                rounding = CornerRounding(
                                    size.minDimension / 10f,
                                    smoothing = 0.1f
                                )
                            )
                            val roundedPolygonPath = roundedPolygon
                                .toPath()
                                .asComposePath()
                            onDrawBehind {
                                if (coord == viewModel.car.value.coord) {
                                    val angle = when (viewModel.car.value.direction) {
                                        "north" -> -90f
                                        "east" -> 0f
                                        "south" -> 90f
                                        "west" -> 180f
                                        else -> -90f // Default pointing up
                                    }
                                    rotate(degrees = angle) {
                                        drawPath(roundedPolygonPath, color = Color.Blue)
                                    }

                                }

                            }
                        }
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
                                    start = Offset(strokeWidth / 2, 0f)
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

                            } else if (viewModel.checkCoordCarCollide(coord, car.coord)) {
                                println("aasdskajda")
                                viewModel.displayCarDialog()
                            }

                            println("${obstaclesList}")
                        }

                        .dragAndDropSource { // if obstacle is there, can drag elsewhere
                            if (hasObstacle != null) {
                                detectDragGestures { change, dragAmount ->
                                    viewModel.draggedObstacleCoord = hasObstacle.coord
                                    print(viewModel.draggedObstacleCoord)
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
                                        draggedOver = false
                                        viewModel.draggedOverCoord = null
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
                                                println(11)
                                                println(draggedObstacleCoord)
                                                if ((!obstaclesList.any { it.coord == coord }) or (coord == viewModel.draggedObstacleCoord)) {
                                                    val index =
                                                        obstaclesList.indexOfFirst { it.coord == viewModel.draggedObstacleCoord }
                                                    if (index != -1) {
                                                        obstaclesList[index] = GridObstacle(
                                                            coord,
                                                            obstaclesList[index].number,
                                                            obstaclesList[index].direction
                                                        )
                                                    }

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

                                    override fun onEntered(event: DragAndDropEvent) {
                                        super.onEntered(event)
                                        draggedOver = true
                                        viewModel.draggedOverCoord = coord
                                    }

                                    override fun onExited(event: DragAndDropEvent) {
                                        super.onExited(event)
                                        draggedOver = false
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




@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    Column (horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
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
                            val text = event.toAndroidDragEvent().clipData?.getItemAt(0)?.text
                            if ((label == "obstacle") and (text == "existing")) {
                                viewModel.obstaclesList.removeAll { it.coord == viewModel.draggedObstacleCoord }
                                Toast
                                    .makeText(
                                        context,
                                        "Obstacle removed",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }
                            return true
                        }

                        override fun onEntered(event: DragAndDropEvent) {
                            super.onEntered(event)
                            viewModel.draggedOverCoord = null
                        }
                    }
                })
    ){
        Grid(viewModel)
        Row (horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically) {
            ObstacleDraggable()
            CarButton(viewModel)
        }

        GridLog(viewModel)
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ObstacleDraggable() {
    Box (
        modifier = Modifier
            .background(color = Color.Red)
            //.size(20.dp, 20.dp)
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

    ){Text("Add Object")}
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
        Text("Drag to Add Obstacle",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .wrapContentHeight()
        )
    }
}

@Composable
fun CarButton(viewModel: MainViewModel) {
    Button(onClick = { viewModel.displayCarDialog() },
        modifier = Modifier
            //.heightIn(max = 30.dp)

    ){
        Text("Set Car Coordinates",
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



