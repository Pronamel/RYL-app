package com.example.ryl_app

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.compose.ui.platform.LocalLifecycleOwner

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun InsideADayScreen(
    duration: Int,
    week: Int,
    day: String,
    moduleName: String,
    BackToModule: () -> Unit,
    // Expecting week first, then lecture name, moduleName, and day
    ToLectureBuilder: (week: Int, name: String, moduleName: String, day: String) -> Unit,
    // For navigating directly to a lecture
    ToLecture: (name: String, week: Int, moduleName: String, day: String) -> Unit
) {
    // Store the list of lecture folder names as state so that UI updates when data changes.
    val lectures = remember { mutableStateOf(getLecturesInDay(moduleName, week.toString(), day).toList()) }
    // Process the raw folder names into pairs (text1, text2) for display.
    val processedNames = processFolderNames(lectures.value)

    // Refresh lecture list when the screen resumes.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Update the lecture state on resume.
                lectures.value = getLecturesInDay(moduleName, week.toString(), day).toList()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Variables to capture header text widths for dynamic underline sizing.
    var weekTextWidth by remember { mutableStateOf(0f) }
    var dayTextWidth by remember { mutableStateOf(0f) }
    val fontSize = 25.sp
    val density = LocalDensity.current.density

    // Variable to capture week value (used later if needed).
    var capturedWeek by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.DarkGray),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Section: Displays Week and Day with underlines.
        Column(
            modifier = Modifier
                .padding(top = 80.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(50.dp)
                    .border(4.dp, Color.Black, RoundedCornerShape(50.dp))
                    .background(color = Color.White, shape = RoundedCornerShape(50.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Display the week text and capture its width for the underline.
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Week $week",
                            color = Color.Black,
                            style = TextStyle(fontSize = fontSize, fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .wrapContentWidth()
                                .onGloballyPositioned { coordinates ->
                                    weekTextWidth = coordinates.size.width / density
                                }
                        )
                        // Underline for the week text.
                        Box(
                            modifier = Modifier
                                .height(3.dp)
                                .width(weekTextWidth.dp)
                                .background(Color.Blue)
                        )
                    }

                    Spacer(modifier = Modifier.width(35.dp))

                    // Display the day text and capture its width for the underline.
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = day,
                            color = Color.Black,
                            style = TextStyle(fontSize = fontSize, fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .wrapContentWidth()
                                .onGloballyPositioned { coordinates ->
                                    dayTextWidth = coordinates.size.width / density
                                }
                        )
                        // Underline for the day text.
                        Box(
                            modifier = Modifier
                                .height(3.dp)
                                .width(dayTextWidth.dp)
                                .background(Color.Blue)
                        )
                    }
                }
            }
        }

        Text(
            text = "Day",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 35.sp),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 30.dp)
        )

        capturedWeek = "$week"  // Store the week value as a string.

        // Main Content: LazyColumn displaying the list of lectures.
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 40.dp, bottom = 35.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // For each processed lecture name, display a custom rectangle button.
            items(processedNames) { (text1, text2) ->
                CustomRectangleButton(
                    text1 = text1,
                    text2 = text2,
                    onClick = {
                        Log.d("InsideADayScreen", "Clicked CustomRectangleButton with week: $week")
                        // Navigate to the lecture screen with parameters.
                        ToLecture(day, week, text1 + "__" + text2, "Z" + moduleName)
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
            // A button to add a new lecture.
            item {
                AddLectureButton(onClick = { ToLectureBuilder(week, "nothingyet", moduleName, day) })
            }
        }

        // Bottom Section: Back button to navigate back to the module screen.
        Column(
            modifier = Modifier
                .padding(bottom = 25.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { BackToModule() },
                modifier = Modifier
                    .size(170.dp, 60.dp)
                    .border(4.dp, Color.Black, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Back",
                    color = Color.Black,
                    style = TextStyle(fontSize = 23.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

// CustomRectangleButton creates a styled button displaying lecture information.
// It shows two text elements (top-left and bottom-right) on a colored background.
@Composable
fun CustomRectangleButton(
    width: Dp = 270.dp,
    height: Dp = 140.dp,
    borderWidth: Dp = 4.dp,
    innerEdgeWidth: Dp = 13.dp,
    text1: String,
    text2: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width, height)
            .border(borderWidth, Color.Black, RoundedCornerShape(4.dp))
            .background(Color(0xFFFF8EB2), RoundedCornerShape(4.dp))
            .padding(innerEdgeWidth)
            .background(Color(0xFFFFC1E3), RoundedCornerShape(4.dp))
    ) {
        // Transparent button overlay to capture click events.
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            elevation = null
        ) {}
        // Top-left text element.
        Text(
            text = text1,
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
            color = Color.Black,
            modifier = Modifier.align(Alignment.TopStart).padding(start = 5.dp)
        )
        // Bottom-right text element.
        Text(
            text = text2,
            style = TextStyle(fontSize = 25.sp, fontWeight = FontWeight.Normal),
            color = Color.Black,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 5.dp)
        )
    }
}

// AddLectureButton creates a button for adding a new lecture.
@Composable
fun AddLectureButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(315.dp)
            .height(60.dp)
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
    ) {
        Text("Add Lecture +", style = TextStyle(fontSize = 35.sp, fontWeight = FontWeight.Bold), color = Color.Black)
    }
}

// processFolderNames splits folder names on "__" to extract two parts for display.
// If the folder name doesn't contain "__", it returns the original name with an empty second part.
fun processFolderNames(folders: List<String>): List<Pair<String, String>> {
    return folders.map { folder ->
        val parts = folder.split("__")
        if (parts.size == 2) parts[0] to parts[1] else folder to ""
    }
}
