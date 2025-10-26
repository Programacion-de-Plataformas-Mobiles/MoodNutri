package com.example.moodnutri.mockups

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moodnutri.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(navController: NavController) {
    Scaffold(
        topBar = { SharedTopAppBar() },
        bottomBar = { SharedBottomNavigationBar(navController = navController, selectedTab = "Recipes") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = "Vegan Buddha Bowl",
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Vegan Buddha Bowl",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(text = "1. Cook the quinoa", fontSize = 20.sp)
                        Text(text = "2. Chop the vegetables", fontSize = 20.sp)
                        Text(text = "3. Combine everything in bowl", fontSize = 20.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* TODO: Start cooking action */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = "Start cooking", color = Color.White, fontSize = 20.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeScreenPreview() {
    RecipeScreen(navController = rememberNavController())
}
