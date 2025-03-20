package com.example.ryl_app

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun createModuleScreen(
    ExitSelection: () -> Unit,
    ConfirmSelection: () -> Unit,
    onNavigateToInsideModule: (String, Int) -> Unit
) {
    var moduleName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color.White) } // Default color
    var selectedDuration by remember { mutableStateOf(1) }

    var showColorPicker by remember { mutableStateOf(false) }
    var showModuleDurationPicker by remember { mutableStateOf(false) } //  NEW state for Module Duration Bottom Sheet

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
            .background(color = Color.DarkGray)
    ) {
        Text(
            text = "Module Name",
            modifier = Modifier.padding(top = 100.dp, start = 28.dp),
            style = TextStyle(fontSize = 20.sp),
            fontSize = 35.sp
        )

        TextField(
            value = moduleName,
            onValueChange = { if (it.length <= 20) moduleName = it },
            label = { Text("Enter Here") },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(top = 8.dp, start = 25.dp)
                .background(Color.DarkGray, RoundedCornerShape(12.dp))
                .border(4.dp, Color.Black, RoundedCornerShape(12.dp))
                .padding(4.dp)
        )

        // **Text Color Selection Button**
        Button(
            onClick = { showColorPicker = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 0.dp, top = 15.dp, bottom = 15.dp)
                .height(150.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.colorwheel),
                    contentDescription = "image for color selection",
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.width(15.dp))
                Text(
                    text = "Text Color    ",
                    color = Color.White,
                    fontSize = 30.sp,
                    modifier = Modifier.padding(end = 80.dp)
                )
            }
        }

        // **Module Duration Button**
        Button(
            onClick = { showModuleDurationPicker = true }, // ✅ OPEN THE BOTTOM SHEET
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 0.dp, top = 15.dp, bottom = 15.dp)
                .height(120.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.calander),
                    contentDescription = "image for module duration",
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.width(15.dp))
                Text(
                    text = "Module Duration",
                    color = Color.White,
                    fontSize = 30.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.padding(top = 165.dp))

        // **Confirm & Exit Buttons**
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { onNavigateToInsideModule(moduleName, selectedDuration); confirmSelectionPress(moduleName,selectedColor, selectedDuration) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Confirm Selection",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                )
            }

            Spacer(modifier = Modifier.padding(10.dp))

            Button(
                onClick = { ExitSelection() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Exit Selection",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                )
            }
        }
    }

    // **Color Picker Bottom Sheet**
    if (showColorPicker) {
        ColorPickerBottomSheet(
            showSheet = true,
            onDismiss = { showColorPicker = false },
            onColorSelected = { color ->
                selectedColor = color
                // Handle the selected color (you can store it in a state variable if needed)
                showColorPicker = false
            }
        )
    }

    //  Module Duration Bottom Sheet
    if (showModuleDurationPicker) {
        ModuleDurationBottomSheet(
            showSheet = true,
            onDismiss = { showModuleDurationPicker = false }, //
            onDurationSelected = { duration ->
                selectedDuration = duration
                //  HANDLE DURATION SELECTION HERE (e.g., store in state)
                showModuleDurationPicker = false // CLOSE THE SHEET AFTER SELECTION
            }
        )
    }
}


// **Function to Handle Confirm Button Press**
fun confirmSelectionPress(moduleName: String, textColor: Color, duration: Int) {

    val check = modulemaker(moduleName, duration , textColor )

    if (check == false){
        println("enter unique module name")
    }
    else{
        println("module created")
    }


    println("Module Name: $moduleName")
    println("Selected Color: $textColor")
    println("Selected Duration: $duration")
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerBottomSheet(
    showSheet: Boolean,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF222222), // Dark Background
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            modifier = Modifier.fillMaxHeight(0.45f) // Increased to make "Pick a Color" section larger
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight() // Fill more space
                    .padding(10.dp) // Increased padding for better layout
                    .background(Color(0xFF333333), RoundedCornerShape(20.dp))
                    .animateContentSize()
            ) {
                // Draggable Handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.15f)
                        .height(6.dp)
                        .background(Color.Gray, CircleShape)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Larger "Pick a Color" title
                Text(
                    text = "Pick a Color",
                    fontSize = 28.sp, // Larger font size
                    color = Color.White,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .align(Alignment.CenterHorizontally)
                )

                val colors = listOf(
                    "Red" to Color.Red,
                    "Green" to Color.Green,
                    "Blue" to Color.Blue,
                    "Yellow" to Color.Yellow,
                    "Magenta" to Color.Magenta,
                    "Cyan" to Color.Cyan,
                    "Orange" to Color(0xFFFFA500),
                    "Purple" to Color(0xFF800080),
                    "Pink" to Color(0xFFFFC0CB),
                    "Teal" to Color(0xFF008080)
                )

                // Increase height for "Pick a Color" section
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(1f) // Takes up more space in the sheet
                ) {
                    items(colors) { (name, color) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(12.dp)
                                .clickable { onColorSelected(color) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp) // Keep color size the same
                                    .background(color, CircleShape)
                                    .border(3.dp, Color.White, CircleShape)
                                    .padding(5.dp)
                            )
                            Text(
                                text = name,
                                color = Color.White,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


//module duration bottom sheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleDurationBottomSheet(
    showSheet: Boolean,
    onDismiss: () -> Unit,
    onDurationSelected: (Int) -> Unit
) {
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF222222), // Dark Background
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            modifier = Modifier.fillMaxHeight(0.45f) // Similar height as Color Picker
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight() // Fill more space
                    .padding(10.dp) // Increased padding for better layout
                    .background(Color(0xFF333333), RoundedCornerShape(20.dp))
                    .animateContentSize()
            ) {
                // Draggable Handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.15f)
                        .height(6.dp)
                        .background(Color.Gray, CircleShape)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Title
                Text(
                    text = "Select Module Duration",
                    fontSize = 28.sp, // Larger font size
                    color = Color.White,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .align(Alignment.CenterHorizontally)
                )

                val durations = (1..12).toList() // Numbers from 1 to 12

                // Grid layout: 3 rows, 4 columns
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4), // Ensures 4 items per row
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(1f) // Takes up more space in the sheet
                ) {
                    items(durations) { duration ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(70.dp) // Square buttons
                                .background(Color.DarkGray, RoundedCornerShape(10.dp))
                                .border(4.dp, Color.White, RoundedCornerShape(10.dp)) // Thick border
                                .clickable { onDurationSelected(duration) }
                                .padding(8.dp) // Adds internal spacing
                        ) {
                            Text(
                                text = "$duration",
                                fontSize = 24.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
