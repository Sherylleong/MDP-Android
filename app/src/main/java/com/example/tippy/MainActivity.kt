package com.example.tippy

import android.content.ClipData
import android.content.ClipDescription
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.tippy.ui.theme.TippyTheme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt

data class Coord(val x: Int, val y: Int)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        println("OnCreate")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TippyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GridScreen(
                    )
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
        println("onStart")
    }

    override fun onResume() {
        super.onResume()
        println("onResume")
    }

    override fun onPause() {
        super.onPause()
        println("onPause")
    }

    override fun onStop() {
        super.onStop()
        println("onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("onDestroy")
    }

    override fun onRestart() {
        super.onRestart()
        println("onRestart")
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Grid(coordsList: MutableList<Coord>,
         modifier: Modifier = Modifier.background(Color(0xFF4CAF50))
) {
    val rows = 20
    val columns = 20
    LazyVerticalGrid(columns = GridCells.Fixed(columns+2),
        contentPadding = PaddingValues(
            start = 11.2.dp,
            end = 11.2.dp,
        ),
    ) {
        items((rows + 2) * (columns + 2)) { index ->
            val row = index / (columns + 2)
            val col = index % (columns + 2)
            val isDark = (row + col) % 2 == 0
            val coord = Coord(col, row)
            val isSelected = coordsList.contains(coord)
            val border = (row == 0) or (col==0) or (row == columns+1) or (col == columns+1)
            val backgroundColor = if (isDark) Color(0xFF4CAF50) else Color(0xFF8BC34A)
            val selectedColor = Color(0xFFFF3D00)
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
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(if (isSelected) selectedColor else backgroundColor)
                        .fillMaxSize()
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
                                        if (!coordsList.contains(coord)) {
                                            coordsList.add(Coord(col, row))
                                        }
                                        return true
                                    }
                                }
                            }
                        )
                        .clickable {
                            if (isSelected) {
                                coordsList.remove(coord)
                            } else {
                                coordsList.add(Coord(col, row))
                            }

                            println("${coordsList}")
                        }
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TippyTheme {
        GridScreen()
    }
}
@Composable
fun GridScreen() {
    val coordsList: MutableList<Coord> = remember { mutableStateListOf() }
    Column (horizontalAlignment = Alignment.CenterHorizontally,){
        Grid(coordsList)
        ObstacleDraggable()
        GridLog(coordsList)
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
fun GridLog(coordsList: List<Coord>, modifier: Modifier = Modifier) {
    LazyColumn {
        item {
            Text("selected coordinates:")
        }
        items(coordsList) { coord ->
            Text(text = "(x=${coord.x}, y=${coord.y})")
        }
    }
}



