package com.example.ryl_app

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun InsideADayScreen(
    duration: Int,
    week: Int,
    day: String,
    moduleName: String,

    BackToModule: () -> Unit,
    ToLectureBuilder: (week: Int , duration: Int , moduleName: String , day: String) -> Unit,
    ToLecture: (day: String, name: String, moduleName: String) -> Unit,


) {
    // State to hold the width of the text
    var weekTextWidth by remember { mutableStateOf(0f) }
    var dayTextWidth by remember { mutableStateOf(0f) }

    val fontSize = 25.sp // Fixed font size for both texts
    val density = LocalDensity.current.density // Get the current density (scale factor)

    val name = "defualt"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.DarkGray),
        horizontalAlignment = Alignment.CenterHorizontally // Centers everything inside the Column
    ) {
        // Top Column with Week and Day Texts
        Column(
            modifier = Modifier
                .padding(top = 80.dp) // 35.dp padding from the top
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f) // 85% of screen width
                    .height(50.dp) // Slightly reduced height
                    .border(4.dp, Color.Black, RoundedCornerShape(50.dp)) // Black border
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(50.dp) // Rounded edges
                    ),
                contentAlignment = Alignment.Center // Centers the content inside the Box
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Week Text + Underline
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Week " + week.toString(),
                            color = Color.Black,
                            style = TextStyle(fontSize = fontSize, fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .wrapContentWidth()
                                .onGloballyPositioned { coordinates ->
                                    // Get the width of the text in pixels and convert to Dp
                                    weekTextWidth = coordinates.size.width / density
                                }
                        )

                        // Underline for Week (set width based on text width)
                        Box(
                            modifier = Modifier
                                .height(3.dp) // Underline height
                                .width(weekTextWidth.dp) // Use the width of the text for the underline
                                .background(Color.Blue)
                        )
                    }

                    Spacer(modifier = Modifier.width(35.dp)) // Reduced space between the texts

                    // Day Text + Underline
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = day,
                            color = Color.Black,
                            style = TextStyle(fontSize = fontSize, fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .wrapContentWidth()
                                .onGloballyPositioned { coordinates ->
                                    // Get the width of the text in pixels and convert to Dp
                                    dayTextWidth = coordinates.size.width / density
                                }
                        )

                        // Underline for Day (set width based on text width)
                        Box(
                            modifier = Modifier
                                .height(3.dp) // Underline height
                                .width(dayTextWidth.dp) // Use the width of the Day text for the underline
                                .background(Color.Blue)
                        )
                    }
                }
            }
        }

        // Middle Column with LazyColumn (takes up most of the screen space)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Takes up the largest portion of the screen
                .padding(top = 70.dp, bottom = 35.dp), // Space between LazyColumn and other elements
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Create 10 buttons dynamically
            items(10) { index ->
                CustomRectangleButton(
                    text1 = "Hello $index",  // Display dynamic text for each button
                    text2 = "World $index",  // Display dynamic text for each button
                    onClick = {ToLecture(day , name , moduleName)}
                )


                Spacer(modifier = Modifier.height(20.dp)) // Add spacing between buttons

            }

            item {
                AddLectureButton(onClick = {ToLectureBuilder(week, duration, moduleName, day) })
            }

        }

        // Bottom Column with Centered Button (with 25.dp padding)
        Column(
            modifier = Modifier
                .padding(bottom = 25.dp) // 25.dp padding from the bottom
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { BackToModule() },
                modifier = Modifier
                    .size(170.dp, 60.dp)
                    .border(4.dp, Color.Black, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red.copy(alpha = 0.5f)
                )
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



@Composable
fun CustomRectangleButton(
    width: Dp = 270.dp,
    height: Dp = 140.dp,
    borderWidth: Dp = 4.dp,
    innerEdgeWidth: Dp = 13.dp, // Thicker inner edge
    text1: String, // First text for the button (top-left)
    text2: String, // Second text for the button (bottom-right)
    onClick: () -> Unit // On click function for the button
) {
    Box(
        modifier = Modifier
            .size(width, height)
            .border(borderWidth, Color.Black, RoundedCornerShape(4.dp)) // Black outer border
            .background(Color(0xFFFF8EB2), RoundedCornerShape(4.dp)) // Slightly darker pink inner edge
            .padding(innerEdgeWidth) // Creates spacing for the main pink
            .background(Color(0xFFFFC1E3), RoundedCornerShape(4.dp)) // Main pink fill
    ) {
        // Transparent Button with the same size as the pink box
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize() // Make the button fill the box
                .background(Color.Transparent), // Transparent button background
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent, // Make the button container transparent
                contentColor = Color.Transparent // Make the button text color transparent
            ),
            elevation = null // Remove the button's elevation (shadow)
        ) {
            // Empty Button, used only for interaction (no content)
        }

        // Position the texts on top-left and bottom-right
        Text(
            text = text1,
            style = TextStyle(fontSize = 25.sp, fontWeight = FontWeight.Bold),
            color = Color.Black, // Text color for text1
            modifier = Modifier
                .align(Alignment.TopStart) // Align top-left within the Box
                .padding(start =5.dp)
        )

        Text(
            text = text2,
            style = TextStyle(fontSize = 25.sp, fontWeight = FontWeight.Normal),
            color = Color.Black, // Text color for text2
            modifier = Modifier
                .align(Alignment.BottomEnd) // Align bottom-right within the Box
                .padding(end = 5.dp)
        )
    }
}

@Composable
fun AddLectureButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(315.dp) // Makes the button fill the width of the parent
            .height(60.dp) // Set a fixed height for the button
            .padding(horizontal = 20.dp), // Add some horizontal padding
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White, // White background color
            contentColor = Color.Black // Black text color
        ),
    ) {
        Text(
            text = "Add Lecture +",
            style = TextStyle(
                fontSize = 35.sp, // Font size of 35
                fontWeight = FontWeight.Bold
            ),
            color = Color.Black // Text color
        )
    }
}





