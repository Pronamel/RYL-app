package com.example.ryl_app

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

// InsideAModuleScreen displays the module's content including a top bar, a list of days and a swipeable week selector.
@Composable
fun InsideAModuleScreen(
    path: String,
    duration: Int,
    BackToHome: () -> Unit,
    NavigateToDay: (duration: Int, week: Int, day: String, moduleName: String) -> Unit
) {
    // Log the received module path and duration.
    println("passed in the name:: $path and the duration:: $duration")
    // Current week is stored as state; initial value is 1.
    var currentWeek by remember { mutableStateOf(1) }
    val maxWeeks = duration

    // State to control the display of the delete confirmation dialog.
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .background(Color.DarkGray)
            .fillMaxSize()
    ) {
        // Top bar containing Back and Delete buttons.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp)
        ) {
            // Back button aligned to the left.
            Button(
                onClick = { BackToHome() },
                modifier = Modifier
                    .size(170.dp, 60.dp)
                    .align(Alignment.CenterStart)
                    .padding(start = 30.dp)
                    .border(4.dp, Color.Black, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = "Back",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp),
                    fontWeight = FontWeight.Bold
                )
            }

            // Delete button aligned to the right.
            Button(
                onClick = {
                    showDeleteDialog = true // Set flag to show delete confirmation dialog.
                },
                modifier = Modifier
                    .size(170.dp, 60.dp)
                    .align(Alignment.CenterEnd)
                    .padding(end = 30.dp)
                    .border(4.dp, Color.Black, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800) // Use orange color.
                )
            ) {
                Text(
                    text = "Delete",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp),
                    fontWeight = FontWeight.Bold
                )
            }
        }


        Text(
            text = "Module",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 35.sp),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 30.dp)
        )


        // Days buttons section; centered horizontally.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated column of buttons for each day of the week.
            ColumnOfButtonsAnimated(
                weekNumber = currentWeek,
                onClick1 = { NavigateToDay(duration, currentWeek, "Monday", path) },
                onClick2 = { NavigateToDay(duration, currentWeek, "Tuesday", path) },
                onClick3 = { NavigateToDay(duration, currentWeek, "Wednesday", path) },
                onClick4 = { NavigateToDay(duration, currentWeek, "Thursday", path) },
                onClick5 = { NavigateToDay(duration, currentWeek, "Friday", path) },
                onClick6 = { NavigateToDay(duration, currentWeek, "Saturday", path) },
                onClick7 = { NavigateToDay(duration, currentWeek, "Sunday", path) },
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Swipeable Week Selector allows the user to change the current week by swiping horizontally.
        SwipeableWeekSelector(
            currentWeek = currentWeek,
            maxWeeks = maxWeeks,
            onWeekChange = { week -> currentWeek = week }
        )

        // Show the delete confirmation dialog if triggered.
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            deleteModuleByName(path)
                            println("DELETE CONFIRMED for module: $path")
                            BackToHome() // Navigate back after deletion.
                        }
                    ) {
                        Text("Yes", color = Color.Red, fontSize = 18.sp)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text("No", color = Color.Gray, fontSize = 18.sp)
                    }
                },
                title = { Text("Delete Module", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete this module?\nThis action cannot be undone.") },
                containerColor = Color.White,
                titleContentColor = Color.Black,
                textContentColor = Color.DarkGray
            )
        }
    }
}

// SwipeableWeekSelector creates a horizontal swipeable box to change the current week.
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
            .padding(start = 16.dp, end = 16.dp, top = 27.dp)
            .background(Color.Gray.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            .height(100.dp)
            // Apply swipe gesture only if there's more than one week.
            .then(
                if (maxWeeks > 1)
                    Modifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount -> dragOffset += dragAmount },
                            onDragEnd = { handleSwipe() }
                        )
                    } else Modifier
            ),
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
                text = "<             Week $tempWeek             >",
                fontSize = 30.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


// CustomButton is a reusable button with customizable size, colors, and border.
@Composable
fun CustomButton(
    onClick: () -> Unit,
    text: String,
    width: Dp = 200.dp,
    height: Dp = 60.dp,
    backgroundColor: Color = Color.White.copy(alpha = 0.9f),
    textColor: Color = Color.Black,
    borderColor: Color = Color.Red
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(width)
            .height(height)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(50)
            ),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = textColor,
                    fontWeight = FontWeight.Bold
                ),
                fontSize = 20.sp
            )
        }
    }
}

// ColumnOfButtonsAnimated displays a column of day buttons with animation effects for scale and transparency.
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
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }

    // Animate the scale and alpha when the week number changes.
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
        CustomButton(onClick = onClick1, text = "Monday", width = 250.dp, borderColor = Color.Blue)
        CustomButton(onClick = onClick2, text = "Tuesday", width = 250.dp, borderColor = Color.Red)
        CustomButton(onClick = onClick3, text = "Wednesday", width = 250.dp, borderColor = Color.Green)
        CustomButton(onClick = onClick4, text = "Thursday", width = 250.dp, borderColor = Color.Blue)
        CustomButton(onClick = onClick5, text = "Friday", width = 250.dp, borderColor = Color.Red)
        CustomButton(onClick = onClick6, text = "Saturday", width = 250.dp, borderColor = Color.Blue)
        CustomButton(onClick = onClick7, text = "Sunday", width = 250.dp, borderColor = Color.Green)
    }
}
