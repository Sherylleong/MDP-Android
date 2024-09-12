package com.example.tippy

import android.widget.Toast
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath

@Composable
fun CarDialog(onDismiss:()->Unit, onConfirm:()->Unit, viewModel: MainViewModel) {
    var textX by remember { mutableStateOf(viewModel.previewCar.coord.x.toString()) }
    var textY by remember { mutableStateOf(viewModel.previewCar.coord.y.toString()) }
    var carDirectionPreview: String by remember {mutableStateOf(viewModel.previewCar.direction)}
    val constraints = ConstraintSet {
        val dialogbox = createRefFor("dialogBoxCar")
        val objectPreview = createRefFor("objectPreviewCar")
        val northButton = createRefFor("northButtonCar")
        val southButton = createRefFor("southButtonCar")
        val eastButton = createRefFor("eastButtonCar")
        val westButton = createRefFor("westButtonCar")

        val objectDisplaySize = 100.dp
        constrain(objectPreview){
            centerHorizontallyTo(parent)
            centerVerticallyTo(parent)
            height = Dimension.value(objectDisplaySize)
            width = Dimension.value(objectDisplaySize)

        }
        constrain(northButton) {
            bottom.linkTo(objectPreview.top)
            centerHorizontallyTo(parent)
            width = Dimension.value(objectDisplaySize)
        }
        constrain(southButton) {
            top.linkTo(objectPreview.bottom)
            centerHorizontallyTo(parent)
            width = Dimension.value(objectDisplaySize)
        }
        constrain(eastButton) {
            start.linkTo(objectPreview.end, margin = 0.dp)
            top.linkTo(objectPreview.top)
            bottom.linkTo(objectPreview.bottom)
            width = Dimension.value(objectDisplaySize)
        }
        constrain(westButton) {
            end.linkTo(objectPreview.start)
            top.linkTo(objectPreview.top)
            bottom.linkTo(objectPreview.bottom)
            width = Dimension.value(objectDisplaySize)
        }
    }
    ConstraintLayout(constraintSet =  constraints, modifier = Modifier.fillMaxSize()) {
        Dialog(
            onDismissRequest = { onDismiss() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            ElevatedCard(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 15.dp
                ),
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .fillMaxHeight(0.4f)
            ) {

                Column {
                    ConstraintLayout(
                        constraintSet = constraints,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        // Object Preview
                        Box(
                            modifier = Modifier
                                .layoutId("objectPreviewCar")
                                .drawWithCache { // draw car
                                    val roundedPolygon = RoundedPolygon(
                                        numVertices = 3,
                                        radius = (size.minDimension * 0.5).toFloat(),
                                        centerX = size.width / 2,
                                        centerY = size.height / 2,
                                        rounding = CornerRounding(
                                            size.minDimension / 20f,
                                            smoothing = 0.1f
                                        )
                                    )
                                    val roundedPolygonPath = roundedPolygon
                                        .toPath()
                                        .asComposePath()
                                    onDrawBehind {
                                            val angle = when (carDirectionPreview) {
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
                        )

                        // North Button
                        Button(modifier = Modifier
                            .layoutId("northButtonCar"),
                            onClick = { carDirectionPreview = "north" }) {
                            Text("North")
                        }
                        // South Button
                        Button(modifier = Modifier
                            .fillMaxWidth()
                            .layoutId("southButtonCar"),
                            onClick = { carDirectionPreview = "south" }) {
                            Text("South")
                        }
                        // East Button
                        Button(modifier = Modifier
                            .fillMaxWidth()
                            .layoutId("eastButtonCar")
                            .rotate(90f)
                            .offset(y = 25.dp), // is there a better way
                            onClick = { carDirectionPreview = "east" }) {
                            Text("East")
                        }

                        // West Button
                        Button(modifier = Modifier
                            .layoutId("westButtonCar")
                            .rotate(-90f)
                            .offset(y = 25.dp), // is there a better way
                            onClick = { carDirectionPreview = "west" }) {
                            Text("West")
                        }
                    }
                    // input x and y
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        val focusManager = LocalFocusManager.current
                        TextField (
                            value = textX,
                            onValueChange = {
                                if (!(it.contains(" ") or it.contains(".") or it.contains(","))) {
                                    textX = it
                                }
                            },
                            modifier = Modifier.weight(1f)
                                .padding(horizontal = 10.dp),
                            textStyle = TextStyle(
                                fontSize = 25.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center // Center align the text
                            ),


                            label = { Text(text = "Car X Position:") },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                            )
                        )
                        TextField (
                            value = textY,
                            onValueChange = {
                                if (!(it.contains(" ") or it.contains(".") or it.contains(","))) {
                                    textY = it
                                }
                            },
                            modifier = Modifier.weight(1f)
                                .padding(horizontal = 10.dp),
                            textStyle = TextStyle(
                                fontSize = 25.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center // Center align the text
                            ),


                            label = { Text(text = "Car Y Position:") },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                            )
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        val context = LocalContext.current
                        Button( modifier = Modifier,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                            onClick = {
                                if (textX == "" || textY == "") {
                                    Toast
                                        .makeText(
                                            context,
                                            "Enter values for X and Y positions!",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                    return@Button
                                }
                                val x = textX.toInt()
                                val y = textY.toInt()
                                if (!(x in 2..19 && y in 2..19)) {
                                    Toast
                                        .makeText(
                                            context,
                                            "Invalid: Enter a range between 2 and 19!",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                                else {
                                    for (obstacle in viewModel.obstaclesList) {
                                        if (viewModel.checkCoordCarCollide(obstacle.coord, Coord(x,y))) {
                                            Toast
                                                .makeText(
                                                    context,
                                                    "Invalid: The space is occupied!",
                                                    Toast.LENGTH_SHORT
                                                )
                                                .show()
                                            return@Button
                                        }
                                    }
                                    viewModel.previewCar = GridCar(Coord(x, y), carDirectionPreview)
                                    onConfirm()
                                }

                                 }) {
                            Text("Confirm")
                        }
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            onClick = { onDismiss()}) {
                            Text("Cancel")
                        }
                    }
                }
            }

        }
    }

}





@Composable
fun TextInput(text: String){
    var currText = text
    TextField (
        value = currText,
        onValueChange = { currText = it  },
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center),
        textStyle = TextStyle(
            fontSize = 25.sp,
            color = Color.White,
            textAlign = TextAlign.Center // Center align the text
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
        ),

        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
    )
}


