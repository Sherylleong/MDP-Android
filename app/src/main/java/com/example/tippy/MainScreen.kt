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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TabRow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.Tab
import androidx.compose.runtime.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui .unit.dp
val panColor = Color(0xFFC603FC)
val fearColor = Color(0xff9400f7)
val disgustColor = Color(0xff008f1a)
val joyColor = Color(0xffff9914)
val confirmColor = Color(0xff2E8B57)
val joystickBackgroundColor = Color(0xffb49fbf)
val anxietyColor = Color(0xfffc4103)
val sadColor = Color(0xff1c77ed)
fun mixWhite(originalColor: Color): Color {
    return Color(
        red = (originalColor.red + 1f) / 2,
        green = (originalColor.green + 1f) / 2,
        blue = (originalColor.blue + 1f) / 2,
        alpha = originalColor.alpha
    )
}
val shadow = 6.dp
val shadowShape = RoundedCornerShape(20.dp)
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


fun sendObjectsList(obstaclesList: List<GridObstacle>) {
    if (BluetoothManager.BluetoothConnectionStatus) {
        val jsonObstaclesArray = JSONArray().apply {
            obstaclesList.forEach { item ->
                val jsonObject = JSONObject().apply {
                    put("x", item.coord.x)
                    put("y", item.coord.y)
                    put("d", item.direction)
                    put("id", item.number)
                }
                put(jsonObject) // Add JSON object to the array
            }
        }

        val mainJsonObject = JSONObject().apply {
            put("cat", "obstacles")
            put("value", JSONObject().apply {
                put("obstacles", jsonObstaclesArray)
            })
        }
        val bytes = mainJsonObject.toString().toByteArray()
        BluetoothManager.write(bytes)
    }
}

fun sendMessage(cat: String, value: String) {
    if (BluetoothManager.BluetoothConnectionStatus) {
        val jsonObject = JSONObject().apply {
            put("cat", cat)
            put("value", value)
        }
        val bytes = jsonObject.toString().toByteArray()
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
    val car by viewModel.car
    val rows = 20
    val columns = 20
    val angerImageBitmap: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.anger_transparent)  //  image resource
    LazyVerticalGrid(columns = GridCells.Fixed(columns+2),
        contentPadding = PaddingValues(
            //top = 15.dp,
            start = 11.2.dp,
            end = 11.2.dp,
        ),
        modifier = Modifier
    ) {

        items((rows + 2) * (columns + 2)) { index ->
            val row =  remember { 21 - (index / (columns + 2))}
            val col = remember { index % (columns + 2)}
            val isDark = remember { (row + col) % 2 == 0}
            val coord = remember { Coord(col, row)}
            val hasObstacle = obstaclesList.find { it.coord == coord }
            val number = hasObstacle?.number ?: ""
            val direction = hasObstacle?.direction
            //  Color(0xFFcafe8f) else Color(0xff88c971)
            //  Color(0xFF4CAF50) else Color(0xFF8BC34A)
            var draggedOver by remember {mutableStateOf(false)}
            val border = remember {(row == 0) or (col==0) or (row == columns+1) or (col == columns+1)}
            val backgroundColor = if (draggedOver) panColor else if (isDark) Color(0xFFcafe8f) else Color(0xff88c971)
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
                        .background(if (draggedOverRow) panColor else Color.Transparent)
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
                        .background(if (draggedOverCol) panColor else Color.Transparent)
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
                                        "N" -> 0f
                                        "E" -> 90f
                                        "S" -> 180f
                                        "W" -> -90f
                                        else -> 0f // Default pointing up
                                    }
                                    rotate(degrees = angle) {
                                        val scaleFactor =
                                            0.35f  // This reduces the size to 50% of the original
                                        drawPath(path, color = Color.Red)

                                        scale(scaleFactor) {
                                            drawImage(
                                                image = angerImageBitmap,
                                                topLeft = Offset(
                                                    x = -4f * size.width,
                                                    y = -1.5f * size.height
                                                ),
                                            )
                                        }


                                    }

                                }

                            }

                        }
                        .drawBehind {
                            val strokeWidth = 5.dp.toPx() * density
                            var start: Offset = Offset(0f, 0f)
                            var end: Offset = Offset(0f, 0f)
                            when (direction) {
                                "N" -> {
                                    start = Offset(0f, strokeWidth / 2)
                                    end = Offset(size.width, strokeWidth / 2)
                                }

                                "S" -> {
                                    start = Offset(0f, size.height - strokeWidth / 2)
                                    end = Offset(size.width, size.height - strokeWidth / 2)
                                }

                                "E" -> {
                                    start = Offset(size.width - strokeWidth / 2, 0f)
                                    end = Offset(size.width - strokeWidth / 2, size.height)
                                }

                                "W" -> {
                                    start = Offset(strokeWidth / 2, 0f)
                                    end = Offset(strokeWidth / 2, size.height)
                                }
                            }
                            if (direction != null) {
                                drawLine(
                                    joyColor,
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
                                                    // sendMessage("EDIT,${obstaclesList[index].number},${obstaclesList[index].number},${coord.x},${coord.y}},${obstaclesList[index].direction}")

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
            painter = painterResource(R.drawable.inside_out_wallpaper_2),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Dark Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)) // Adjust alpha for darkness
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
                                    val obstacleToRemove =
                                        viewModel.obstaclesList.find { it.coord == viewModel.draggedObstacleCoord }
                                    viewModel.obstaclesList.remove(obstacleToRemove)
                                    Toast
                                        .makeText(
                                            context,
                                            "Obstacle removed",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                    //sendMessage("SUB,${obstacleToRemove!!.number}")
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
            Console(viewModel)
        }
    }
}

@Composable
fun Console(viewModel: MainViewModel) {
    // Load background image from resources
    val backgroundImage = painterResource(id = R.drawable.inside_out_ctrl_panel)

    // State to manage which tab is currently selected
    var selectedTab by remember { mutableStateOf(0) } // 0 for the main console, 1 for additional functions

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // TabRow to hold the tabs
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Main Console") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Communications") }
            )
        }

        // Background image for the console
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Image(
                painter = backgroundImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Main Console content
            if (selectedTab == 0) {
                Column(
                    verticalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ObstacleDraggable() // Keep this in the row
                        CarButton(viewModel)
                        ResetButton(viewModel)
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Joystick(viewModel)
                        StatusMessage(viewModel)
                        Column(
                            verticalArrangement = Arrangement.SpaceAround,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top=16.dp, bottom = 16.dp)
                        ) {

                            sendObstaclesButton(viewModel)
                            startFastestPathButton(viewModel)
                            startImageRecButton(viewModel)
                        }
                    }
                }
            }

            // Additional Functions content
            if (selectedTab == 1) {
                CommunicationsTab(viewModel.messageViewModel, viewModel::isBluetoothConnected)
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ObstacleDraggable() {
    Box(
        modifier = Modifier
            .shadow(10.dp)
            .background(fearColor, RoundedCornerShape(5.dp))

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(5.dp)
                    .shadow(10.dp)
                    .background(color = Color.Black)
                    .size(30.dp, 30.dp)
                    .dragAndDropSource(drawDragDecoration = { drawRect(Color(0x80000000)) }) {
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
                            }
                        )
                    }


            )
            {
                Text("0_0'", color = fearColor, textAlign = TextAlign.Center,modifier = Modifier.fillMaxSize().align(Alignment.Center), )
            }

            Text(
                "Add Object (Drag the box!)",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(end = 5.dp),
                color = Color.White

            )
        }
    }
}


@Composable
fun ResetButton(viewModel: MainViewModel) {
    Button(onClick = {
        viewModel.obstaclesList.clear();
        viewModel.car.value = GridCar(Coord(2,2), "N")
    },
        colors = ButtonDefaults.buttonColors(
            containerColor = anxietyColor,
            ),
        modifier = Modifier.shadow(shadow, shadowShape)
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
fun Joystick(viewModel: MainViewModel) {
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }
    val size = 180.dp

    Box(
        modifier = Modifier
            .size(size)
            .background(joystickBackgroundColor, shape = CircleShape) // Background of the joystick
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // Reset the joystick position when the drag ends
                        offset = Offset(0f, 0f)
                    },
                    onDragCancel = {
                        // Reset the joystick position if the drag is canceled
                        offset = Offset(0f, 0f)
                    }
                ) { change, dragAmount ->
                    // Update the offset based on drag
                    offset = Offset(
                        x = (offset.x + dragAmount.x).coerceIn(
                            -90f,
                            90f
                        ), // Limit within the circle
                        y = (offset.y + dragAmount.y).coerceIn(-90f, 90f)
                    )
                    change.consume() // Consume the drag event
                }
            }
            .graphicsLayer {
                // Translate the joystick position based on the offset
                translationX = offset.x
                translationY = offset.y
            }
    ) {
        // Joystick knob
        Box(
            modifier = Modifier
                .shadow(8.dp, shape = CircleShape)
                .size(60.dp) // Size of the knob
                .background(Color.Red, shape = CircleShape)
                .align(Alignment.Center)

        )
    }

    // Handle joystick position changes
    LaunchedEffect(offset) {
        val threshold = 50f // Define a threshold for movement detection
        val x = viewModel.car.value.coord.x
        val y = viewModel.car.value.coord.y

        when {
            offset.y < -threshold -> {
                // Move forward
                val dir = viewModel.car.value.direction
                when (dir) {
                    "N" -> if (y < 19) viewModel.car.value = viewModel.car.value.copy(coord = Coord(x, y + 1))
                    "S" -> if (y > 2) viewModel.car.value = viewModel.car.value.copy(coord = Coord(x, y - 1))
                    "E" -> if (x < 19) viewModel.car.value = viewModel.car.value.copy(coord = Coord(x + 1, y))
                    "W" -> if (x > 2) viewModel.car.value = viewModel.car.value.copy(coord = Coord(x - 1, y))
                }
                sendMessage("instruction","FW05")

            }
            offset.y > threshold -> {
                // Move backward
                val dir = viewModel.car.value.direction
                when (dir) {
                    "N" -> if (y > 2) viewModel.car.value = viewModel.car.value.copy(coord = Coord(x, y - 1))
                    "S" -> if (y < 19) viewModel.car.value = viewModel.car.value.copy(coord = Coord(x, y + 1))
                    "E" -> if (x > 2) viewModel.car.value = viewModel.car.value.copy(coord = Coord(x - 1, y))
                    "W" -> if (x < 19) viewModel.car.value = viewModel.car.value.copy(coord = Coord(x + 1, y))
                }
                sendMessage("instruction", "BW05")
            }
            offset.x < -threshold -> {
                // Rotate left
                viewModel.car.value = viewModel.car.value.copy(direction = when (viewModel.car.value.direction) {
                    "N" -> "W"
                    "W" -> "S"
                    "S" -> "E"
                    "E" -> "N"
                    else -> viewModel.car.value.direction
                })
                sendMessage("instruction", "FL05") // Adjust to your left turn instruction
            }
            offset.x > threshold -> {
                // Rotate right
                viewModel.car.value = viewModel.car.value.copy(direction = when (viewModel.car.value.direction) {
                    "N" -> "E"
                    "E" -> "S"
                    "S" -> "W"
                    "W" -> "N"
                    else -> viewModel.car.value.direction
                })
                sendMessage("instruction", "FR05") // Adjust to your right turn instruction
            }
        }
    }
}

@Composable
fun DPad(viewModel: MainViewModel) {
    val x = viewModel.car.value.coord.x
    val y = viewModel.car.value.coord.y

    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier) {
        // North Button (Forward)
        Button(
            onClick = {
                val dir = viewModel.car.value.direction
                when (dir) {
                    "N" -> if (y < 19) viewModel.car.value = viewModel.car.value.copy(coord = Coord(x, y + 1))
                    "S" -> if (y > 2) viewModel.car.value = viewModel.car.value.copy(coord = Coord(x, y - 1))
                    "E" -> if (x < 19) viewModel.car.value = viewModel.car.value.copy(coord = Coord(x + 1, y))
                    "W" -> if (x > 2) viewModel.car.value = viewModel.car.value.copy(coord = Coord(x - 1, y))
                }
                sendMessage("instruction","FW05")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xff1C1C1C),
                contentColor = Color.White
            ),
            shape = TriangleShape(),
            modifier = Modifier
                .graphicsLayer(rotationZ = 0f)
                .size(100.dp, 60.dp)
        ) {}

        // West, Placeholder, and East Buttons
        Row (horizontalArrangement =Arrangement.SpaceBetween,
            modifier = Modifier
            .padding(bottom = 30.dp, top = 30.dp)
        ){
            Button(
                onClick = {
                    val dir = viewModel.car.value.direction
                    when (dir) {
                        "N" -> viewModel.car.value = viewModel.car.value.copy(direction = "W")
                        "S" -> viewModel.car.value = viewModel.car.value.copy(direction = "E")
                        "E" -> viewModel.car.value = viewModel.car.value.copy(direction = "N")
                        "W" -> viewModel.car.value = viewModel.car.value.copy(direction = "S")
                    }
                    viewModel.previewCar = viewModel.car.value
                    sendMessage("instruction", "FL05")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xff1C1C1C),
                    contentColor = Color.White
                ),
                shape = TriangleShape(),
                modifier = Modifier
                    .graphicsLayer(rotationZ = -90f)
                    .size(100.dp, 60.dp)
                    .offset(y = -(15.dp))
            ) {}

            Spacer(modifier = Modifier.size(50.dp)) // Central space or placeholder

            Button(
                onClick = {
                    val dir = viewModel.car.value.direction
                    when (dir) {
                        "N" -> viewModel.car.value = viewModel.car.value.copy(direction = "E")
                        "S" -> viewModel.car.value = viewModel.car.value.copy(direction = "W")
                        "E" -> viewModel.car.value = viewModel.car.value.copy(direction = "S")
                        "W" -> viewModel.car.value = viewModel.car.value.copy(direction = "N")
                    }
                    sendMessage("instruction", "FR05")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xff1C1C1C),
                    contentColor = Color.White
                ),
                shape = TriangleShape(),
                modifier = Modifier
                    .graphicsLayer(rotationZ = 90f)
                    .size(100.dp, 60.dp)
                    .offset(y = -(15.dp))
            ) {}
        }

        // South Button (Backward)
        Button(
            onClick = {
                val dir = viewModel.car.value.direction
                when (dir) {
                    "N" -> if (y > 2) viewModel.car.value = viewModel.car.value.copy(coord = Coord(x, y - 1))
                    "S" -> if (y < 19) viewModel.car.value = viewModel.car.value.copy(coord = Coord(x, y + 1))
                    "E" -> if (x > 2) viewModel.car.value = viewModel.car.value.copy(coord = Coord(x - 1, y))
                    "W" -> if (x < 19) viewModel.car.value = viewModel.car.value.copy(coord = Coord(x + 1, y))
                }
                sendMessage("instruction", "BW05")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xff1C1C1C),
                contentColor = Color.White
            ),
            shape = TriangleShape(),
            modifier = Modifier
                .graphicsLayer(rotationZ = 180f)
                .size(100.dp, 60.dp)
        ) {}
    }
}



@Composable
fun CarButton(viewModel: MainViewModel) {
    Button(onClick = { viewModel.displayCarDialog() },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xff2E8B57)),
        modifier = Modifier.shadow(shadow, shadowShape)
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


class TriangleShape(direction: String="N") : Shape {
    val size = 100.dp
    val angle = when (direction) {
        "N" -> 0f
        "E" -> 90f
        "S" -> 180f
        "W" -> -90f
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
fun StatusMessage(viewModel: MainViewModel) {
    Box (modifier = Modifier.background(color = Color.Black, RoundedCornerShape(5.dp))
        .size(width = 200.dp, height = 150.dp)){
        Column {
            Text(
                color = confirmColor,
                fontFamily = FontFamily.Monospace,
                text = "Current status: ",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            )
            Text(
                color = confirmColor,
                text = viewModel.status,
                fontFamily = FontFamily.Monospace,
                style = TextStyle(
                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}


@Composable
fun sendObstaclesButton(viewModel: MainViewModel){
    Button(onClick = { // send obstacles button
        sendObjectsList(viewModel.obstaclesList)
    },
        colors = ButtonDefaults.buttonColors(containerColor = sadColor),
        modifier = Modifier.shadow(shadow, shadowShape)
        //.heightIn(max = 30.dp)
    ){
        Text("Send Obstacles",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .wrapContentHeight()
        )
    }
}

@Composable
fun startFastestPathButton(viewModel: MainViewModel){
    Button(onClick = { // start fastest path
        sendMessage("control", "start")
    },
        colors = ButtonDefaults.buttonColors(containerColor = joyColor),
        modifier = Modifier.shadow(shadow, shadowShape)
        //.heightIn(max = 30.dp)

    ){
        Text("Start Fastest Path",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .wrapContentHeight()
        )
    }
}

@Composable
fun startImageRecButton(viewModel: MainViewModel){
    Button(onClick = { // start image rec
        sendMessage("imagerec", "start")
    },
        colors = ButtonDefaults.buttonColors(containerColor = joyColor),
        modifier = Modifier.shadow(shadow, shadowShape)
        //.heightIn(max = 30.dp)

    ){
        Text("Start Image Rec",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .wrapContentHeight()
        )
    }
}

//@Composable
//fun StartButtons(viewModel: MainViewModel){
//    Column {
//        Button(onClick = { // send obstacles button
//            sendObjectsList(viewModel.obstaclesList)
//        },
//            colors = ButtonDefaults.buttonColors(containerColor = Color(0xffed7979)),
//            modifier = Modifier
//            //.heightIn(max = 30.dp)
//        ){
//            Text("Send Obstacles",
//                textAlign = TextAlign.Center,
//                modifier = Modifier
//                    .wrapContentHeight()
//            )
//        }
//
//        Button(onClick = { // start fastest path
//            sendMessage("control", "start")
//        },
//            colors = ButtonDefaults.buttonColors(containerColor = Color(0xffed7979)),
//            modifier = Modifier
//            //.heightIn(max = 30.dp)
//
//        ){
//            Text("Start Fastest Path",
//                textAlign = TextAlign.Center,
//                modifier = Modifier
//                    .wrapContentHeight()
//            )
//        }
//
//        Button(onClick = { // start image rec
//            sendMessage("imagerec", "start")
//        },
//            colors = ButtonDefaults.buttonColors(containerColor = Color(0xffed7979)),
//            modifier = Modifier
//            //.heightIn(max = 30.dp)
//
//        ){
//            Text("Start Image Rec",
//                textAlign = TextAlign.Center,
//                modifier = Modifier
//                    .wrapContentHeight()
//            )
//        }
//    }
//
//
//}