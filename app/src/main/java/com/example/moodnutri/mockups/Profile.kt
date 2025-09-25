package com.example.moodnutri.mockups

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodnutri.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    Scaffold(
        bottomBar = { SharedBottomNavigationBar(selectedTab = "Profile") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Profile / Stats",
                fontSize = 28.sp, // Increased
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background), // Placeholder
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(72.dp) // Increased
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Sara",
                    fontSize = 26.sp, // Increased
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Emotional state",
                fontSize = 22.sp, // Increased
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF4CAF50),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp), // Increased
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEmotions,
                        contentDescription = "Happy",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp) // Increased
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Happy",
                        color = Color.White,
                        fontSize = 20.sp // Increased
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Nutrition",
                fontSize = 22.sp, // Increased
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = 0.75f,
                        modifier = Modifier.size(90.dp), // Increased
                        strokeWidth = 10.dp, // Increased
                        color = Color(0xFF4CAF50),
                        trackColor = Color.LightGray
                    )
                    Text(text = "2200", fontSize = 20.sp, fontWeight = FontWeight.Bold) // Increased
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "2200 kcal", fontSize = 20.sp) // Increased
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Progress",
                fontSize = 22.sp, // Increased
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = 0.6f,
                modifier = Modifier.fillMaxWidth().height(12.dp), // Increased
                color = Color(0xFF4CAF50),
                trackColor = Color.LightGray
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}
