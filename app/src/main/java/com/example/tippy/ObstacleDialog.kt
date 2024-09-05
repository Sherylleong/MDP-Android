package com.example.tippy

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
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

@Composable
fun ObstacleDialog(onDismiss:()->Unit, onConfirm:()->Unit, viewModel: MainViewModel) {
    var text by remember { mutableStateOf(viewModel.previewObstacle.number) }
    var obstacleDirectionPreview: String? by remember {mutableStateOf(viewModel.previewObstacle.direction)}
    val constraints = ConstraintSet {
        val dialogbox = createRefFor("dialogBox")
        val objectPreview = createRefFor("objectPreview")
        val northButton = createRefFor("northButton")
        val southButton = createRefFor("southButton")
        val eastButton = createRefFor("eastButton")
        val westButton = createRefFor("westButton")

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
                                .background(Color.Black)
                                //.border(width = 15.dp, color = Color.Red)
                                .layoutId("objectPreview")
                                .drawBehind {
                                    val strokeWidth = 20.dp.toPx() * density
                                    var start: Offset = Offset(0f, 0f)
                                    var end: Offset = Offset(0f, 0f)
                                    when (obstacleDirectionPreview) {
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
                                    if (obstacleDirectionPreview != null) {
                                        drawLine(
                                            Color.Red,
                                            start,
                                            end,
                                            strokeWidth
                                        )
                                    }
                                }
                        ) {
                            TextField (
                                value = text,
                                onValueChange = { text = it  },
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

                        // North Button
                        Button(modifier = Modifier
                            .layoutId("northButton"),
                            onClick = { obstacleDirectionPreview = "north" }) {
                            Text("North")
                        }
                        // South Button
                        Button(modifier = Modifier
                            .fillMaxWidth()
                            .layoutId("southButton"),
                            onClick = { obstacleDirectionPreview = "south" }) {
                            Text("South")
                        }
                        // East Button
                        Button(modifier = Modifier
                            .fillMaxWidth()
                            .layoutId("eastButton")
                            .rotate(90f)
                            .offset(y = 25.dp), // is there a better way
                            onClick = { obstacleDirectionPreview = "east" }) {
                            Text("East")
                        }

                        // West Button
                        Button(modifier = Modifier
                            .layoutId("westButton")
                            .rotate(-90f)
                            .offset(y = 25.dp), // is there a better way
                            onClick = { obstacleDirectionPreview = "west" }) {
                            Text("West")
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Button( modifier = Modifier,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                            onClick = {
                                viewModel.previewObstacle = GridObstacle(viewModel.previewObstacle.coord, text, obstacleDirectionPreview)
                                onConfirm() }) {
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





