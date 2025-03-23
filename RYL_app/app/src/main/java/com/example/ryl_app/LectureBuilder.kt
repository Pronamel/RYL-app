package com.example.ryl_app

import android.graphics.Color.alpha
import android.text.style.BackgroundColorSpan
import android.widget.Space
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

// LectureBuilderScreen provides the UI for building a lecture by entering its name and selecting a time range.
// It displays a text field for the lecture name, a button to open a time selection dialog, a preview rectangle showing the lecture details,
// and bottom buttons to exit or confirm the lecture creation.
@Composable
fun LectureBuilderScreen(
    day: String,
    week: Int,
    name: String,
    moduleName: String,
    BackToDay: () -> Unit,
    ToLecture: (day: String, week: Int, name: String, moduleName: String) -> Unit,
) {
    // Mutable state for lecture name input and lecture time range.
    var text by remember { mutableStateOf("") }
    var lectureTime by remember { mutableStateOf("00:00 - 00:00") }
    // State flag to show/hide the time selection dialog.
    var showDialog by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    // Root container covers the full screen with a dark gray background.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
    ) {
        // Main content area with gesture detection to hide keyboard when tapping outside.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                }
        ) {
            // Lecture Name input area.
            Column(modifier = Modifier.padding(start = 11.dp, top = 15.dp)) {
                Text(
                    text = "Lecture Name",
                    modifier = Modifier.padding(bottom = 7.dp, start = 23.dp),
                    style = TextStyle(fontSize = 35.sp),
                    fontSize = 35.sp
                )
                TextField(
                    value = text,
                    onValueChange = { if (it.length <= 15) text = it },
                    label = { Text("Enter Here") },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(65.dp)
                        .padding(start = 15.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.DarkGray, RoundedCornerShape(12.dp))
                        .border(4.dp, Color.Black, RoundedCornerShape(12.dp))
                )
            }

            // Button to open the time selection dialog.
            Button(
                onClick = { showDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp, bottom = 15.dp)
                    .height(150.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Image(
                        painter = painterResource(R.drawable.clock),
                        contentDescription = "Color selection",
                        modifier = Modifier.size(130.dp)
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(
                        text = "Lecture Time",
                        color = Color.White,
                        fontSize = 30.sp
                    )
                    Spacer(modifier = Modifier.width(45.dp))
                }
            }

            // Display the time selection dialog if triggered.
            if (showDialog) {
                TimeSelectionDialog(
                    onDismiss = { showDialog = false },
                    onConfirm = { start, end ->
                        lectureTime = "$start - $end"
                    }
                )
            }

            Spacer(modifier = Modifier.padding(top = 30.dp))
            // Preview area: a custom rectangle that shows the lecture name and time.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 50.dp),
                contentAlignment = Alignment.Center
            ) {
                CustomRectangle(
                    text1 = text,
                    text2 = lectureTime
                )
            }

            // Spacer to push the bottom container to the bottom.
            Spacer(modifier = Modifier.weight(1f))
        }

        // Bottom container with Exit and Confirm buttons.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(horizontal = 16.dp, vertical = 40.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = -10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val context = LocalContext.current

                // Exit button to return to the previous screen.
                Button(
                    onClick = { BackToDay() },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "Exit",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Confirm button: checks for time conflicts and creates the lecture folder.
                Button(
                    onClick = {
                        val isConflict = checkLectureTimeConflict(moduleName, week.toString(), day, lectureTime)
                        if (isConflict) {
                            Toast.makeText(context, "Time conflict with another lecture!", Toast.LENGTH_SHORT).show()
                        } else {
                            findModuleWeekDayLectureAndCreateFolder(moduleName, week.toString(), day, text, lectureTime)
                            ToLecture(day, week, text + "__" + lectureTime, "L" + moduleName)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(start = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Green.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "Confirm",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// CustomRectangle displays a styled box with two pieces of text (top-left and bottom-right) on a layered background.
@Composable
fun CustomRectangle(
    width: Dp = 320.dp,
    height: Dp = 180.dp,
    borderWidth: Dp = 4.dp,
    innerEdgeWidth: Dp = 13.dp,
    text1: String,
    text2: String
) {
    Box(
        modifier = Modifier
            .size(width, height)
            .border(borderWidth, Color.Black, RoundedCornerShape(4.dp))
            .background(Color(0xFFFF8EB2), RoundedCornerShape(4.dp))
            .padding(innerEdgeWidth)
            .background(Color(0xFFFFC1E3), RoundedCornerShape(4.dp))
    ) {
        // Display the first text at the top-left.
        Text(
            text = text1,
            style = TextStyle(fontSize = 25.sp, fontWeight = FontWeight.Bold),
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 5.dp)
        )
        // Display the second text at the bottom-right.
        Text(
            text = text2,
            style = TextStyle(fontSize = 25.sp, fontWeight = FontWeight.Normal),
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 5.dp)
        )
    }
}

// TimeSelectionDialog presents a dialog for selecting start and end times for the lecture.
@Composable
fun TimeSelectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("17:00") }

    // Generate time intervals (e.g., every 30 minutes).
    val timeIntervals = (0..47).map { index ->
        String.format("%02d:%02d", index / 2, if (index % 2 == 0) 0 else 30)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.DarkGray.copy(alpha = 0.9f),
        title = {
            Text(
                text = "Select Lecture Time",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimeSelector(
                    label = "Start Time",
                    time = startTime,
                    onTimeChange = { newTime ->
                        if (timeIntervals.indexOf(newTime) < timeIntervals.indexOf(endTime)) {
                            startTime = newTime
                        }
                    },
                    timeIntervals = timeIntervals
                )
                Spacer(modifier = Modifier.height(16.dp))
                TimeSelector(
                    label = "End Time",
                    time = endTime,
                    onTimeChange = { newTime ->
                        if (timeIntervals.indexOf(newTime) > timeIntervals.indexOf(startTime)) {
                            endTime = newTime
                        }
                    },
                    timeIntervals = timeIntervals
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(startTime, endTime); onDismiss() }) {
                Text("Confirm Selection")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// TimeSelector provides UI controls to increment or decrement a time value from a list of time intervals.
@Composable
fun TimeSelector(label: String, time: String, onTimeChange: (String) -> Unit, timeIntervals: List<String>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        IconButton(onClick = {
            val currentIndex = timeIntervals.indexOf(time)
            if (currentIndex > 0) onTimeChange(timeIntervals[currentIndex - 1])
        }) {
            Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Increase Time", tint = Color.White)
        }
        Text(text = time, fontSize = 22.sp, color = Color.White, modifier = Modifier.padding(horizontal = 16.dp))
        IconButton(onClick = {
            val currentIndex = timeIntervals.indexOf(time)
            if (currentIndex < timeIntervals.size - 1) onTimeChange(timeIntervals[currentIndex + 1])
        }) {
            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Decrease Time", tint = Color.White)
        }
    }
}
