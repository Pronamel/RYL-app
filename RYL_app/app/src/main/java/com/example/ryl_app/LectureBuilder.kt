package com.example.ryl_app

import android.graphics.Color.alpha
import android.text.style.BackgroundColorSpan
import android.widget.Space
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




@Composable
fun LectureBuilderScreen(
    day: String,
    week: String,
    name: String,
    moduleName: String,

    BackToDay: () -> Unit,
    ToLecture: (day: String, name: String, moduleName: String) -> Unit,


) {
    var text by remember { mutableStateOf("") }

    // Get the keyboard controller
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    // Get the focus manager
    val focusManager = LocalFocusManager.current

    println("======================================================  == = = ==  ==  = == =")
    println("Here Are The Stats:: " + day + "  " + week + "  " + moduleName + "  " )
    println("======================================================  == = = ==  ==  = == =")


    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    // Clear the focus and hide the keyboard when tapping outside the TextField
                    focusManager.clearFocus() // Clears focus from the TextField
                    keyboardController?.hide() // Hides the keyboard
                })
            }
            .background(color = Color.DarkGray)
            .padding(horizontal = 16.dp) // Add padding around the column for general alignment
    ) {
        Text(
            text = "Module Name",
            modifier = Modifier
                .padding(top = 100.dp, start = 22.dp),
            style = TextStyle(fontSize = 20.sp),
            fontSize = 35.sp
        )

        // TextField
        TextField(
            value = text,
            onValueChange = { if (it.length <= 20) text = it },
            label = { Text("Enter Here") },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(top = 8.dp)
                .background(
                    Color.DarkGray,
                    RoundedCornerShape(12.dp)
                ) // Set background to DarkGray with rounded corners
                .border(
                    4.dp,
                    Color.Black,
                    RoundedCornerShape(12.dp)
                ) // Black border with rounded corners
                .padding(4.dp) // Optional padding inside the text field to ensure text does not touch the edges
        )

        // Text Color Selection Row
        Row(
            modifier = Modifier.padding(top = 70.dp, start = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.colorwheel),
                contentDescription = "Image for color selection",
                modifier = Modifier.fillMaxWidth(0.25f)
            )
            Box(
                modifier = Modifier
                    .padding(15.dp)
                    .background(Color.White, shape = RoundedCornerShape(22.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .size(200.dp, 50.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Text Color",
                    color = Color.Black,
                    fontSize = 34.sp
                )
            }
        }

        // CustomRectangle Box (Centered)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 100.dp) // Added space between the other components and the rectangle box
                .wrapContentHeight(), // Ensures it takes up only necessary vertical space
            contentAlignment = Alignment.Center
        ) {
            CustomRectangle(
                text1 = "Lecture Name",
                text2 = "00:00"
            )
        }

        // Buttons column for confirming selection and going back
        Column(
            modifier = Modifier
                .padding(top = 100.dp, start = 16.dp, end = 16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Apply Changes Button
            Button(
                onClick = { ToLecture(day, text , moduleName); findModuleWeekDayLectureAndCreateFolder(moduleName, week, day, text, "10:00") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(vertical = 8.dp), // Added vertical padding between buttons
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Green.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = "Confirm Selection",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                )
            }

            // Exit Selection Button
            Button(
                onClick = { BackToDay() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(vertical = 8.dp), // Added vertical padding between buttons
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = "Exit Selection",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                )
            }
        }
    }
}

@Composable
fun CustomRectangle(
    width: Dp = 320.dp,
    height: Dp = 180.dp,
    borderWidth: Dp = 4.dp,
    innerEdgeWidth: Dp = 13.dp, // Thicker inner edge
    text1: String, // First text for the top-left
    text2: String // Second text for the bottom-right
) {
    Box(
        modifier = Modifier
            .size(width, height)
            .border(borderWidth, Color.Black, RoundedCornerShape(4.dp)) // Black outer border
            .background(Color(0xFFFF8EB2), RoundedCornerShape(4.dp)) // Slightly darker pink inner edge
            .padding(innerEdgeWidth) // Creates spacing for the main pink
            .background(Color(0xFFFFC1E3), RoundedCornerShape(4.dp)) // Main pink fill
    ) {
        // Position the texts on top-left and bottom-right
        Text(
            text = text1,
            style = TextStyle(fontSize = 25.sp, fontWeight = FontWeight.Bold),
            color = Color.Black, // Text color for text1
            modifier = Modifier
                .align(Alignment.TopStart) // Align top-left within the Box
                .padding(start = 5.dp)
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
