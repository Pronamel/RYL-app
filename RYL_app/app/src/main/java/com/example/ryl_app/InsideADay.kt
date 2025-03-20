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

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun InsideADayScreen(
    duration: Int,
    week: Int,
    day: String,
    moduleName: String,
    BackToModule: () -> Unit,
    ToLectureBuilder: (week: Int, duration: Int, moduleName: String, day: String) -> Unit,
    ToLecture: (day: String, week: Int, name: String, moduleName: String) -> Unit

) {




    var weekTextWidth by remember { mutableStateOf(0f) }
    var dayTextWidth by remember { mutableStateOf(0f) }
    val fontSize = 25.sp
    val density = LocalDensity.current.density

    var capturedWeek by remember { mutableStateOf("") }

    val folderNames by remember { mutableStateOf(getLecturesInDay(moduleName, week.toString(), day).toList()) }
    val processedNames = processFolderNames(folderNames)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.DarkGray),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                        Box(
                            modifier = Modifier
                                .height(3.dp)
                                .width(weekTextWidth.dp)
                                .background(Color.Blue)
                        )
                    }

                    Spacer(modifier = Modifier.width(35.dp))

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


        capturedWeek = "$week"

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 70.dp, bottom = 35.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(processedNames) { (text1, text2) ->
                CustomRectangleButton(
                    text1 = text1,
                    text2 = text2,
                    onClick = {
                        Log.d("InsideADayScreen", "Clicked CustomRectangleButton with week: $week")
                        ToLecture(day, week, text1 + text2, moduleName)
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                AddLectureButton(onClick = { ToLectureBuilder(week, duration, moduleName, day) })
            }
        }


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
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            elevation = null
        ) {}

        Text(
            text = text1,
            style = TextStyle(fontSize = 25.sp, fontWeight = FontWeight.Bold),
            color = Color.Black,
            modifier = Modifier.align(Alignment.TopStart).padding(start = 5.dp)
        )

        Text(
            text = text2,
            style = TextStyle(fontSize = 25.sp, fontWeight = FontWeight.Normal),
            color = Color.Black,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 5.dp)
        )
    }
}



@Composable
fun AddLectureButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.width(315.dp).height(60.dp).padding(horizontal = 20.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
    ) {
        Text("Add Lecture +", style = TextStyle(fontSize = 35.sp, fontWeight = FontWeight.Bold), color = Color.Black)
    }
}

fun processFolderNames(folders: List<String>): List<Pair<String, String>> {
    return folders.map { folder ->
        val parts = folder.split("__")
        if (parts.size == 2) parts[0] to parts[1] else folder to ""
    }
}


