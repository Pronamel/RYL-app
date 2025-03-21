package com.example.ryl_app

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlin.math.absoluteValue

@Composable
fun InsideAModuleScreen(

    path: String,
    duration: Int,


    BackToHome: () -> Unit,
    NavigateToDay: (duration: Int, week: Int, day: String, moduleName: String) -> Unit


) {

    println("passed in the name:: " + path + " and the duration:: " + duration)

    var currentWeek by remember { mutableStateOf(1) }
    val maxWeeks = duration

    Column(
        modifier = Modifier
            .background(Color.DarkGray)
            .fillMaxSize()
    ) {
        // Modules button
        Column(modifier = Modifier.padding(top = 60.dp)) {
            Button(
                onClick = { BackToHome() },
                modifier = Modifier
                    .size(170.dp, 60.dp)
                    .padding(start = 30.dp)
                    .border(4.dp, Color.Black, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = "Modules",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Days buttons
        Column(modifier = Modifier.padding(top = 70.dp, start = 8.dp)) {
            ColumnOfButtonsAnimated(
                weekNumber = currentWeek,  // Pass the currentWeek value here
                onClick1 = { NavigateToDay(duration, currentWeek, "Monday", path)},
                onClick2 = { NavigateToDay(duration, currentWeek, "Tuesday", path) },
                onClick3 = { NavigateToDay(duration, currentWeek, "Wednesday", path) },
                onClick4 = { NavigateToDay(duration, currentWeek, "Thursday", path) },
                onClick5 = { NavigateToDay(duration, currentWeek, "Friday", path) },
                onClick6 = { NavigateToDay(duration, currentWeek, "Sunday", path) },
                onClick7 = { NavigateToDay(duration, currentWeek, "Saturday", path) },
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Swipeable Week Selector with faster sensitivity
        SwipeableWeekSelector(
            currentWeek = currentWeek,
            maxWeeks = maxWeeks,
            onWeekChange = { week -> currentWeek = week }
        )
    }
}

@Composable
fun SwipeableWeekSelector(
    currentWeek: Int,
    maxWeeks: Int,
    onWeekChange: (Int) -> Unit
) {
    var dragOffset by remember { mutableStateOf(0f) }
    var tempWeek by remember { mutableStateOf(currentWeek) }

    val swipeThreshold = 50f

    val offsetX = animateIntOffsetAsState(
        targetValue = IntOffset(x = dragOffset.roundToInt(), y = 0),
        animationSpec = tween(durationMillis = 150, easing = LinearOutSlowInEasing)
    )

    fun handleSwipe() {
        if (dragOffset.absoluteValue >= swipeThreshold) {
            val weekChange = if (dragOffset < 0) +1 else -1
            val newWeek = (tempWeek + weekChange).coerceIn(1, maxWeeks)

            if (newWeek != tempWeek) {
                tempWeek = newWeek
                onWeekChange(newWeek)
            }
        }
        dragOffset = 0f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .height(100.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount -> dragOffset += dragAmount },
                    onDragEnd = { handleSwipe() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset { offsetX.value }
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.DarkGray, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "< Week $tempWeek >",
                fontSize = 34.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CustomButton(
    onClick: () -> Unit,
    text: String,
    width: Dp = 200.dp,
    height: Dp = 60.dp,
    paddingStart: Dp = 16.dp,
    backgroundColor: Color = Color.White.copy(alpha = 0.8f),
    textColor: Color = Color.Black,
    underlineColor: Color = Color.Red,
    underlineWidth: Dp = 10.dp
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(width)
            .height(height),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        contentPadding = PaddingValues(start = paddingStart)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Row {
                Text(
                    text = "> ",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = textColor
                    ),
                    fontSize = 20.sp
                )
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = textColor
                        ),
                        fontSize = 20.sp
                    )
                    Spacer(
                        modifier = Modifier
                            .height(2.dp)
                            .background(underlineColor)
                            .width(underlineWidth)

                    )
                }
            }
        }
    }
}

@Composable
fun ColumnOfButtonsAnimated(
    weekNumber: Int,
    onClick1: () -> Unit,
    onClick2: () -> Unit,
    onClick3: () -> Unit,
    onClick4: () -> Unit,
    onClick5: () -> Unit,
    onClick6: () -> Unit,
    onClick7: () -> Unit
) {
    // Define the animations for scale and alpha
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }

    // Animate the buttons when the week changes
    LaunchedEffect(weekNumber) {
        scale.snapTo(0.8f)
        alpha.snapTo(0f)

        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 200)
        )
    }

    // Apply the animation to the column of buttons
    Column(
        modifier = Modifier
            .padding(8.dp)
            .graphicsLayer(
                scaleX = scale.value,
                scaleY = scale.value,
                alpha = alpha.value
            ),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        CustomButton(onClick = onClick1, text = "Monday", width = 280.dp, underlineColor = Color.Blue, underlineWidth = 72.dp)
        CustomButton(onClick = onClick2, text = "Tuesday", width = 250.dp, underlineColor = Color.Red, underlineWidth = 79.dp)
        CustomButton(onClick = onClick3, text = "Wednesday", width = 220.dp, underlineColor = Color.Green, underlineWidth = 105.dp)
        CustomButton(onClick = onClick4, text = "Thursday", width = 200.dp, underlineColor = Color.Blue, underlineWidth = 88.dp)
        CustomButton(onClick = onClick5, text = "Friday", width = 220.dp, underlineColor = Color.Red, underlineWidth = 58 .dp)
        CustomButton(onClick = onClick6, text = "Saturday", width = 250.dp, underlineColor = Color.Blue, underlineWidth = 85.dp)
        CustomButton(onClick = onClick7, text = "Sunday", width = 280.dp, underlineColor = Color.Yellow, underlineWidth = 72.dp)
    }
}

fun onButtonClick(buttonNumber: Int) {
    println("Button $buttonNumber clicked!")
}


