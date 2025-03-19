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

//InsideALectureScreen
@Composable
fun InsideALectureScreen(

    day: String,
    name: String,
    moduleName: String,
    BackToLectureBuilder: () -> Unit

) {


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray) // Set background color to DarkGray
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween // Evenly distribute elements
    ) {
        // Title Box
        Column(modifier = Modifier.padding(top = 60.dp)){
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .border(6.dp, Color.Blue, RoundedCornerShape(12.dp)) // Rounded border
                    .background(Color.White, RoundedCornerShape(12.dp)) // Rounded background
                    .padding(top = 10.dp, bottom = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Lecture Name",
                    fontSize = 28.sp, // Increased text size
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        // Regular Text
        Text(
            text = "00:00",
            fontSize = 50.sp, // Larger text size
            color = Color.Black,
            modifier = Modifier.padding(30.dp)
        )

        Spacer(modifier = Modifier.weight(0.5f)) // Pushes content to distribute evenly

        // **Red Button**
        Column(modifier = Modifier.padding(bottom = 25.dp)){
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(125.dp)
                    .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                    .background(Color.Red, RoundedCornerShape(12.dp))
                    .clickable { /* Handle click */ },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Record",
                    color = Color.Black,
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ** Green Button**
        Column (modifier = Modifier.padding(bottom = 25.dp, top = 25.dp)){
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(75.dp)
                    .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                    .background(Color.Green, RoundedCornerShape(12.dp))
                    .clickable { /* Handle click */ },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Listen",
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // **Turquoise Button**
        Column(modifier = Modifier.padding(bottom = 25.dp, top = 15.dp)) {
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(60.dp)
                    .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                    .background(Color.Cyan, RoundedCornerShape(12.dp))
                    .clickable { /* Handle click */ }
                    .padding(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Email",
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f)) // Pushes purple button to the bottom

        // **Purple Button**
        Column (modifier = Modifier.padding(bottom = 30.dp)){
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(90.dp)
                    .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                    .background(Color(0xFF800080), RoundedCornerShape(12.dp))
                    .clickable { BackToLectureBuilder() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Weeks",
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

