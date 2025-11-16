package com.example.moodnutri.mockups

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

@Composable
fun ImageSelectionButton(selectedImage: Bitmap?, buttonText: String, onClick: () -> Unit) {
    if (selectedImage != null) {
        Image(
            bitmap = selectedImage.asImageBitmap(),
            contentDescription = "Selected image",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentScale = ContentScale.Crop
        )
    } else {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.PhotoCamera, contentDescription = "Camera", modifier = Modifier.size(48.dp))
                Text(buttonText)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientListView(ingredients: List<String>, onAdd: (String) -> Unit, onRemove: (String) -> Unit) {
    var newIngredient by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        LazyRow {
            items(ingredients) { ingredient ->
                InputChip(
                    selected = false,
                    onClick = { onRemove(ingredient) },
                    label = { Text(ingredient) },
                    trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove") },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newIngredient,
                onValueChange = { newIngredient = it },
                label = { Text("Add ingredient") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                onAdd(newIngredient)
                newIngredient = ""
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    }
}
