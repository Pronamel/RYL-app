package com.example.ryl_app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

// HomeScreen is the main composable that displays a list of modules and a button to create a new module.
@Composable
fun HomeScreen(
    onNavigateToSecondScreen: () -> Unit,
    onNavigateToInsideModule: (String, Int) -> Unit
) {
    // Retrieve module data (paths, colors, durations) from storage.
    val (modulePaths, moduleColours, moduleDurations) = getModuleData()

    // Debug: Print out module data to the console.
    println("===== Module Data =====")
    for (i in modulePaths.indices) {
        println("Module ${i + 1}:")
        println("  Path: ${modulePaths[i]}")
        println("  Text Color: ${moduleColours[i]}")
        println("  Number: ${moduleDurations[i]}")
        println("-----------------------")
    }

    // Wrap the main content in a Box to layer components.
    Box(modifier = Modifier.fillMaxSize()) {
        // Main content column centered on screen.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display the home screen image.
            Image(
                painter = painterResource(id = R.drawable.homescreenpic),
                contentDescription = "microphone image",
                modifier = Modifier
                    .padding(top = 90.dp)
                    .scale(1.2f)  // Scale the image 20% larger (1.2 times)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Container for the module list and create button.
            Box(
                modifier = Modifier
                    .background(Color.DarkGray)
                    .fillMaxHeight(0.95f)
                    .fillMaxWidth(0.8f),
                contentAlignment = Alignment.Center
            ) {
                // LazyColumn to display modules and the create module button.
                LazyColumn {
                    // List modules dynamically from stored module data.
                    items(modulePaths.size) { index ->
                        CustomSizedButton(
                            text = getLastValueInPath(modulePaths[index]),
                            textColor = moduleColours[index]
                        ) {
                            val pathCurrent: String = modulePaths[index]
                            println("this is the module durations $pathCurrent")
                            // Navigate into the selected module.
                            onNavigateToInsideModule(
                                getLastValueInPath(modulePaths[index]),
                                moduleDurations[index]
                            )
                        }
                    }
                    // Additional item: Button to create a new module.
                    item {
                        createModuleButton(text = "+", textColor = Color.Black) {
                            onNavigateToSecondScreen()
                        }
                    }
                }
            }
        }

        // Top-right info icon with padding.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 45.dp, end = 15.dp, bottom = 20.dp)
        ) {
            InfoIcon()  // Displays the info icon with a popup when clicked.
        }
    }
}

// InfoIcon displays a clickable circle with an "i" that opens an AlertDialog.
@Composable
fun InfoIcon() {
    var showDialog by remember { mutableStateOf(false) }

    // Circular icon with "i".
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(Color.LightGray, shape = CircleShape)
            .clickable { showDialog = true },
        contentAlignment = Alignment.Center
    ) {
        Text(text = "i", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }

    // AlertDialog popup for displaying informational text.
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text("Important") },
            text = {
                Text(
                    "Please ensure you comply with local laws and obtain all necessary " +
                            "permissions before recording any lectures or conversations."
                )
            }
        )
    }
}

// CustomSizedButton is a reusable button with fixed dimensions and border styling.
@Composable
fun CustomSizedButton(
    text: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(300.dp)
            .height(120.dp)
            .padding(13.dp)
            .border(3.dp, Color.Black, shape = RoundedCornerShape(12.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = text, fontSize = 18.sp, color = textColor)
    }
}

// createModuleButton is styled with a dashed border and used to trigger the creation of a new module.
@Composable
fun createModuleButton(
    text: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(298.dp)
            .height(120.dp)
            .padding(15.dp)
            .drawBehind {
                val strokeWidth = 3.dp.toPx()  // Define stroke width in pixels.
                val dashWidth = 20f            // Width of each dash.
                val dashGap = 10f              // Gap between dashes.
                val dashPattern = PathEffect.dashPathEffect(floatArrayOf(dashWidth, dashGap), 0f)
                val cornerRadius = CornerRadius(12.dp.toPx())

                // Draw a white solid rounded rectangle as background.
                drawRoundRect(
                    color = Color.White,
                    style = Stroke(width = strokeWidth * 2),
                    cornerRadius = cornerRadius
                )

                // Draw a dashed black border on top.
                drawRoundRect(
                    color = Color.Black,
                    style = Stroke(width = strokeWidth * 2, pathEffect = dashPattern),
                    cornerRadius = cornerRadius
                )
            },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = text, fontSize = 30.sp, color = textColor)
    }
}

// getLastValueInPath extracts and returns the final folder name from a file path.
fun getLastValueInPath(path: String): String {
    val file = File(path)
    return file.name
}
