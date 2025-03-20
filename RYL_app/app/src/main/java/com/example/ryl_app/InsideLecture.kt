package com.example.ryl_app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import android.media.MediaRecorder
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

@Composable
fun InsideALectureScreen(
    day: String,
    week: Int,
    name: String,
    moduleName: String,
    BackToLectureBuilder: () -> Unit,
    BackToInsideADay: () -> Unit,
) {

    println("InsideALectureScreen Week received: $week")  // Log the week value

    // Process the passed-in moduleName to get a Boolean and an updated module name.
    val (isL, updatedModuleName) = processModuleName(moduleName)
    // Now use updatedModuleName in place of the original moduleName in your UI.
    println("isL: $isL, updatedModuleName: $updatedModuleName")

    // The rest of your code using updatedModuleName...
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var recorder: MediaRecorder? by remember { mutableStateOf(null) }
    val outputFile = File(context.filesDir, "lecture_recording.mp3")

    fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(outputFile.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            prepare()
            start()
        }
        isRecording = true
    }

    fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        isRecording = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(top = 60.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .border(6.dp, Color.Blue, RoundedCornerShape(12.dp))
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(top = 10.dp, bottom = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                // Use updatedModuleName instead of moduleName.
                Text(
                    text = "$name $day $week $updatedModuleName",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        Text(
            text = if (isRecording) "Recording..." else "00:00",
            fontSize = 50.sp,
            color = Color.Black,
            modifier = Modifier.padding(30.dp)
        )

        Spacer(modifier = Modifier.weight(0.5f))

        Column(modifier = Modifier.padding(bottom = 25.dp)) {
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(125.dp)
                    .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                    .background(if (isRecording) Color.Gray else Color.Red, RoundedCornerShape(12.dp))
                    .clickable {
                        if (isRecording) stopRecording() else startRecording()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isRecording) "Stop" else "Record",
                    color = Color.Black,
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(modifier = Modifier.padding(bottom = 30.dp)) {
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(90.dp)
                    .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                    .background(Color(0xFF800080), RoundedCornerShape(12.dp))
                    .clickable { if(isL == true){
                        BackToLectureBuilder()
                    }else{
                        BackToInsideADay()
                    }

                               },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Days",
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


fun processModuleName(moduleName: String): Pair<Boolean, String> {
    if (moduleName.isEmpty()) return Pair(false, moduleName)
    // Get the first character in uppercase.
    val firstChar = moduleName[0].uppercaseChar()
    return when (firstChar) {
        'L' -> Pair(true, moduleName.substring(1))  // Return true and drop the first char.
        'Z' -> Pair(false, moduleName.substring(1)) // Return false and drop the first char.
        else -> Pair(false, moduleName)             // If neither, leave the module name as is.
    }
}

