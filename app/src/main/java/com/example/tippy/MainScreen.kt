package com.example.tippy

import android.content.ClipData
import android.content.ClipDescription
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import android.bluetooth.BluetoothAdapter
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size

fun startBluetoothStatusChecker(viewModel: MainViewModel) {
    val handler = Handler(Looper.getMainLooper())
    val checkInterval: Long = 2000 // 2 seconds

    val bluetoothCheckRunnable = object : Runnable {
        override fun run() {
            if (!BluetoothManager.BluetoothConnectionStatus) {
                viewModel.status = "Not connected"
            } else if (BluetoothManager.BluetoothConnectionStatus && viewModel.status == "Not connected") {
                viewModel.status = "Connected and ready"
            }
            handler.postDelayed(this, checkInterval)
        }
    }

    // Start checking Bluetooth status
    handler.post(bluetoothCheckRunnable)
}


fun sendMessage(message: String) {
    if (BluetoothManager.BluetoothConnectionStatus) {
        val bytes = message.toByteArray()
        BluetoothManager.write(bytes)
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel){
    startBluetoothStatusChecker(viewModel)
    if (!BluetoothManager.BluetoothConnectionStatus) {
        viewModel.status = "Not connected"
    }
    else if (BluetoothManager.BluetoothConnectionStatus && viewModel.status == "Not connected") {
        viewModel.status = "Connected and ready"
    }
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
                        Text(
                            "$row",
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
                        .background(if (hasObstacle != null) selectedColor else backgroundColor)
                        .fillMaxSize()
                        .zIndex(if (coord == car.coord) 2f else 1f)
                        .drawWithCache { // draw car
                            val width = size.width
                            val height = size.height
                            val path = Path().apply {
                                moveTo(width / 2, -height) // Top middle
                                lineTo(-width, 2 * height)    // Bottom left
                                lineTo(2 * width, 2 * height) // Bottom right
                                close()
                            }
                            onDrawBehind {
                                if (coord == car.coord) {
                                    val angle = when (car.direction) {
                                        "north" -> 0f
                                        "east" -> 90f
                                        "south" -> 180f
                                        "west" -> -90f
                                        else -> 0f // Default pointing up
                                    }
                                    rotate(degrees = angle) {
                                        drawPath(path, color = Color.Blue)
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
                                viewModel.displayObstacleDialog("existing")
                                //Toast.makeText(context, "Error: An obstacle is already placed there!", Toast.LENGTH_SHORT).show()
                                //obstaclesList.remove(coord)


                            } else if (viewModel.checkCoordCarCollide(coord, car.coord)) {
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
                                                    viewModel.displayObstacleDialog("new")

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
                                                    sendMessage("EDIT,${obstaclesList[index].number},${obstaclesList[index].number},${coord.x},${coord.y}},${obstaclesList[index].direction}")

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

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.speedometer),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

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
                            override fun onDrop(event: DragAndDropEvent): Boolean { // remove obstacle
                                val label = event.toAndroidDragEvent().clipDescription.label
                                val text = event.toAndroidDragEvent().clipData?.getItemAt(0)?.text
                                if ((label == "obstacle") and (text == "existing")) {
                                    val obstacleToRemove = viewModel.obstaclesList.find { it.coord == viewModel.draggedObstacleCoord }
                                    viewModel.obstaclesList.remove(obstacleToRemove)
                                    Toast
                                        .makeText(
                                            context,
                                            "Obstacle removed",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                    sendMessage("SUB,${obstacleToRemove!!.number}")
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
                ResetButton(viewModel)
            }
            Row {
                DPad(viewModel)
                StatusMessage(viewModel)
            }


            GridLog(viewModel)
        }
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

@Composable
fun ResetButton(viewModel: MainViewModel) {
    Button(onClick = {
    },
        modifier = Modifier
        //.heightIn(max = 30.dp)

    ){
        Text("Reset All",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .wrapContentHeight()
        )
    }
}


@Composable
fun DPad(viewModel: MainViewModel) {
    val x = viewModel.car.value.coord.x
    val y = viewModel.car.value.coord.y

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // North Button (Forward)
        Button(
            onClick = {
                if (viewModel.car.value.direction != "north") {
                    viewModel.car.value = viewModel.car.value.copy(direction = "north")
                    viewModel.previewCar = viewModel.car.value
                } else {
                    if (y < 19) viewModel.car.value = viewModel.car.value.copy(coord = Coord(x, y + 1))
                }
                sendMessage("FW10")
            },
            shape = TriangleShape(),
            modifier = Modifier.graphicsLayer(rotationZ = 0f)
        ) {}

        // West, Placeholder, and East Buttons
        Row {
            Button(
                onClick = {
                    if (viewModel.car.value.direction != "west") {
                        viewModel.car.value = viewModel.car.value.copy(direction = "west")
                        viewModel.previewCar = viewModel.car.value
                    } else {
                        if (x > 2) viewModel.car.value = viewModel.car.value.copy(coord = Coord(x - 1, y))
                    }
                    sendMessage("FL--")
                },
                shape = TriangleShape(),
                modifier = Modifier.graphicsLayer(rotationZ = -90f)
            ) {}

            Spacer(modifier = Modifier.size(50.dp)) // Central space or placeholder

            Button(
                onClick = {
                    if (viewModel.car.value.direction != "east") {
                        viewModel.car.value = viewModel.car.value.copy(direction = "east")
                        viewModel.previewCar = viewModel.car.value
                    } else {
                        if (x < 19) viewModel.car.value = viewModel.car.value.copy(coord = Coord(x + 1, y))
                    }
                    sendMessage("FR--")
                },
                shape = TriangleShape(),
                modifier = Modifier.graphicsLayer(rotationZ = 90f)
            ) {}
        }

        // South Button (Backward)
        Button(
            onClick = {
                if (viewModel.car.value.direction != "south") {
                    viewModel.car.value = viewModel.car.value.copy(direction = "south")
                    viewModel.previewCar = viewModel.car.value
                } else {
                    if (y > 2) viewModel.car.value = viewModel.car.value.copy(coord = Coord(x, y - 1))
                }
                sendMessage("BW10")
            },
            shape = TriangleShape(),
            modifier = Modifier.graphicsLayer(rotationZ = 180f)
        ) {}
    }
}



@Composable
fun CarButton(viewModel: MainViewModel) {
    Button(onClick = { viewModel.displayCarDialog() },
        modifier = Modifier
            //.heightIn(max = 30.dp)

    ){
        Text("Set Car",
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


class TriangleShape(direction: String="north") : Shape {
    val size = 100.dp
    val angle = when (direction) {
        "north" -> 0f
        "east" -> 90f
        "south" -> 180f
        "west" -> -90f
        else -> 0f // Default pointing up
    }
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val width = size.width
        val height = size.height



        val path = Path().apply {
            moveTo(width / 2f, 0f) // Top point
            lineTo(0f, height) // Bottom left point
            lineTo(width, height) // Bottom right point
            close() // Close path to form a triangle
        }

        return Outline.Generic(path)
    }
}
@Composable
fun StatusMessage(viewModel: MainViewModel){
    Box(){
        Column {
            Text("Current status: ")
            Text(viewModel.status)
        }

    }
}