package com.example.ryl_app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File


@Composable
fun HomeScreen(onNavigateToSecondScreen: () -> Unit, onNavigateToInsideModule: (String, Int) -> Unit ) {

    val (modulePaths, moduleColours, moduleDurations) = getModuleData()


    //debugging to see module info
    println("===== Module Data =====")
    for (i in modulePaths.indices) {
        println("Module ${i + 1}:")
        println("  Path: ${modulePaths[i]}")
        println("  Text Color: ${moduleColours[i]}")
        println("  Number: ${moduleDurations[i]}")
        println("-----------------------")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(

            painter = painterResource(id = R.drawable.homescreenpic),
            contentDescription = "microphone image",
            modifier = Modifier.padding(top = 70.dp)

        )

        Spacer(modifier = Modifier.height(60.dp))



        Box(
            modifier = Modifier.background(Color.DarkGray)
                .fillMaxHeight(0.95f)
                .fillMaxWidth(0.8f),
            contentAlignment = Alignment.Center
        ){
            LazyColumn {
                items(modulePaths.size) { index ->
                    CustomSizedButton(
                        text = getLastValueInPath(modulePaths[index]),
                        textColor = moduleColours[index]

                    )
                    { //passing the module path and the module duration to inside a module
                        val pathCurrent: String = modulePaths[index]
                        println("this is the module durations " +  modulePaths[index] )
                        onNavigateToInsideModule(getLastValueInPath(modulePaths[index]), moduleDurations[index] )
                    }
                }



            item(){
                    createModuleButton(text = "+", textColor = Color.Black)
                    { onNavigateToSecondScreen();  }
                }


            }

        }

    }
}


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
                val strokeWidth = 3.dp.toPx()  // stroke height
                val dashWidth = 20f            // dash width
                val dashGap = 10f              // dash spacing
                val dashPattern = PathEffect.dashPathEffect(floatArrayOf(dashWidth, dashGap), 0f)
                val cornerRadius = CornerRadius(12.dp.toPx())

                // White border underneath
                drawRoundRect(
                    color = Color.White,
                    style = Stroke(width = strokeWidth * 2), // Slightly thicker white border
                    cornerRadius = cornerRadius
                )

                // Dashed black border (wider dashes)
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



// This returns the last directory/file in the path
fun getLastValueInPath(path: String): String {
    val file = File(path)
    return file.name
}

