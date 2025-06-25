package com.example.babyphotoapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReviewScreen() {
    val context = LocalContext.current
    var files by remember { mutableStateOf(listOf<File>()) }

    LaunchedEffect(Unit) {
        files = withContext(Dispatchers.IO) {
            val dir = File(
                context.filesDir,
                "photos/${SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date())}"
            )
            dir.listFiles()?.sortedDescending() ?: emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's Photos") },
                actions = {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh",
                        modifier = Modifier.clickable {
                            /* TODO: Refresh logic */
                        }
                    )
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(files) { file ->
                Image(
                    painter = rememberImagePainter(file),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { /* TODO: Mark active */ }
                )
            }
        }
    }
}
