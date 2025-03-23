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
    ExitSelection: () -> Unit,
    ConfirmSelection: () -> Unit,
    onNavigateToInsideModule: (String, Int) -> Unit
) {
    var moduleName by remember { mutableStateOf("") }
    // Default color is White; user must pick another color.
    var selectedColor by remember { mutableStateOf(Color.White) }
    // Set default duration to 0 so that the user must pick a valid duration (> 0)
    var selectedDuration by remember { mutableStateOf(0) }

    var showColorPicker by remember { mutableStateOf(false) }
    var showModuleDurationPicker by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    // Enable Confirm only when moduleName is not blank, a color is selected (not white),
    // and a module duration greater than 0 has been chosen.
    val isConfirmEnabled = moduleName.trim().isNotEmpty() && selectedColor != Color.White && selectedDuration > 0

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

        Spacer(modifier = Modifier.padding(top = 40.dp))

        // **Text Color Selection Button**
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

        // **Module Duration Button**
        Button(
            onClick = { showModuleDurationPicker = true }, // OPEN THE BOTTOM SHEET
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

        // **Confirm & Exit Buttons**
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

                // Confirm Button: enabled only if all fields have valid input.
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

    // **Color Picker Bottom Sheet**
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

    // **Module Duration Bottom Sheet**
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

// **Function to Handle Confirm Button Press**
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
                // Draggable Handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.15f)
                        .height(6.dp)
                        .background(Color.Gray, CircleShape)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // "Pick a Color" Title
                Text(
                    text = "Pick a Color",
                    fontSize = 28.sp,
                    color = Color.White,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .align(Alignment.CenterHorizontally)
                )

                // First nine are color blind friendly colors, then an additional ten
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
                    // Additional 10 colors
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
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
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
                // Draggable Handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.15f)
                        .height(6.dp)
                        .background(Color.Gray, CircleShape)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Title for Duration Selection
                Text(
                    text = "Select Module Duration",
                    fontSize = 28.sp,
                    color = Color.White,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .align(Alignment.CenterHorizontally)
                )

                val durations = (1..12).toList()

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
