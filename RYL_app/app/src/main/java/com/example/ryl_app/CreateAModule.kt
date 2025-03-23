package com.example.ryl_app

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun createModuleScreen(
    ExitSelection: () -> Unit,                // Callback for exit action
    ConfirmSelection: () -> Unit,             // (Not used directly in this snippet)
    onNavigateToInsideModule: (String, Int) -> Unit  // Navigate to inside module with module name and duration
) {
    // State variables for module name, selected color, and duration.
    var moduleName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color.White) } // Default white requires user change.
    var selectedDuration by remember { mutableStateOf(0) }          // Default 0 forces user to pick a valid duration.

    // Flags to display the bottom sheets for color and duration selection.
    var showColorPicker by remember { mutableStateOf(false) }
    var showModuleDurationPicker by remember { mutableStateOf(false) }

    // Get local controllers for keyboard, focus, and haptic feedback.
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    // Confirm button is enabled only when all inputs are valid.
    val isConfirmEnabled = moduleName.trim().isNotEmpty() && selectedColor != Color.White && selectedDuration > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Dismiss keyboard and clear focus when tapping outside.
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
            .background(color = Color.DarkGray)
    ) {
        // Label for the module name.
        Text(
            text = "Module Name",
            modifier = Modifier.padding(top = 100.dp, start = 28.dp),
            style = TextStyle(fontSize = 20.sp),
            fontSize = 35.sp
        )

        // TextField for module name input, limited to 20 characters.
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

        Spacer(modifier = Modifier.padding(top = 40.dp))

        // Button to open the color picker bottom sheet.
        Button(
            onClick = { showColorPicker = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp, top = 15.dp, bottom = 15.dp)
                .height(150.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.colorwheel),
                    contentDescription = "image for color selection",
                    modifier = Modifier.size(113.dp)
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

        // Button to open the module duration picker bottom sheet.
        Button(
            onClick = { showModuleDurationPicker = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp, top = 30.dp, bottom = 15.dp)
                .height(150.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.calander),
                    contentDescription = "image for module duration",
                    modifier = Modifier.size(115.dp)
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

        Spacer(modifier = Modifier.padding(top = 160.dp))

        // Box container for the Exit and Confirm buttons.
        Box(
            modifier = Modifier
                .background(color = Color.Black.copy(alpha = 0.8f))
                .padding(top = 15.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Exit button.
                Button(
                    onClick = { ExitSelection() },
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Exit",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.padding(10.dp))

                // Confirm button; triggers navigation, confirmation logic, and haptic feedback.
                Button(
                    onClick = {
                        onNavigateToInsideModule(moduleName, selectedDuration)
                        confirmSelectionPress(moduleName, selectedColor, selectedDuration)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    enabled = isConfirmEnabled,
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .height(50.dp)
                        .padding(start = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green.copy(alpha = 0.5f))
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

    // Show the Color Picker Bottom Sheet if the flag is set.
    if (showColorPicker) {
        ColorPickerBottomSheet(
            showSheet = true,
            onDismiss = { showColorPicker = false },
            onColorSelected = { color ->
                selectedColor = color
                showColorPicker = false
            }
        )
    }

    // Show the Module Duration Bottom Sheet if the flag is set.
    if (showModuleDurationPicker) {
        ModuleDurationBottomSheet(
            showSheet = true,
            onDismiss = { showModuleDurationPicker = false },
            onDurationSelected = { duration ->
                selectedDuration = duration
                showModuleDurationPicker = false
            }
        )
    }
}

// Function to handle Confirm button press; uses a module maker function to validate and create the module.
fun confirmSelectionPress(moduleName: String, textColor: Color, duration: Int) {
    val check = modulemaker(moduleName, duration, textColor)
    if (!check) {
        println("enter unique module name")
    } else {
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
        // Modal bottom sheet for picking a text color.
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF222222),
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            modifier = Modifier.fillMaxHeight(0.45f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(10.dp)
                    .background(Color(0xFF333333), RoundedCornerShape(20.dp))
                    .animateContentSize()
            ) {
                // Draggable handle indicator.
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.15f)
                        .height(6.dp)
                        .background(Color.Gray, CircleShape)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Title text.
                Text(
                    text = "Pick a Color",
                    fontSize = 28.sp,
                    color = Color.White,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .align(Alignment.CenterHorizontally)
                )

                // List of colors (first 9 are color-blind friendly, followed by 10 additional colors).
                val colors = listOf(
                    "Orange" to Color(0xFFE69F00),
                    "Sky Blue" to Color(0xFF56B4E9),
                    "Bluish Green" to Color(0xFF009E73),
                    "Yellow" to Color(0xFFF0E442),
                    "Blue" to Color(0xFF0072B2),
                    "Vermillion" to Color(0xFFD55E00),
                    "Light Pink" to Color(0xFFCC79A7),
                    "Gray" to Color(0xFF999999),
                    "Teal" to Color(0xFF008080),
                    // Additional colors
                    "Red" to Color.Red,
                    "Green" to Color.Green,
                    "Blue" to Color.Blue,
                    "Yellow" to Color.Yellow,
                    "Magenta" to Color.Magenta,
                    "Cyan" to Color.Cyan,
                    "Orange" to Color(0xFFFFA500),
                    "Purple" to Color(0xFF800080),
                    "Pink" to Color(0xFFFFC0CB),
                    "Brown" to Color(0xFF8B4513),
                    "Lime" to Color(0xFF32CD32)
                )

                // Grid layout displaying each color option.
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(1f)
                ) {
                    items(colors) { (name, color) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(12.dp)
                                .clickable { onColorSelected(color) }
                        ) {
                            // Color circle with border.
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(color, CircleShape)
                                    .border(3.dp, Color.White, CircleShape)
                                    .padding(5.dp)
                            )
                            // Color name label.
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleDurationBottomSheet(
    showSheet: Boolean,
    onDismiss: () -> Unit,
    onDurationSelected: (Int) -> Unit
) {
    if (showSheet) {
        // Modal bottom sheet for selecting the module duration.
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF222222),
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            modifier = Modifier.fillMaxHeight(0.45f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(10.dp)
                    .background(Color(0xFF333333), RoundedCornerShape(20.dp))
                    .animateContentSize()
            ) {
                // Draggable handle indicator.
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.15f)
                        .height(6.dp)
                        .background(Color.Gray, CircleShape)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Title for the duration selection.
                Text(
                    text = "Select Module Duration",
                    fontSize = 28.sp,
                    color = Color.White,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .align(Alignment.CenterHorizontally)
                )

                // Duration options from 1 to 12.
                val durations = (1..12).toList()

                // Grid layout for duration options.
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(1f)
                ) {
                    items(durations) { duration ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(70.dp)
                                .background(Color.DarkGray, RoundedCornerShape(10.dp))
                                .border(4.dp, Color.White, RoundedCornerShape(10.dp))
                                .clickable { onDurationSelected(duration) }
                                .padding(8.dp)
                        ) {
                            // Display the duration number.
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
