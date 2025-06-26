// File: app/src/main/java/com/example/babyphotoapp/ui/ReviewScreen.kt
@file:OptIn(ExperimentalFoundationApi::class)
package com.example.babyphotoapp.ui

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ReviewScreen(
    navController: NavController,
    vm: PhotoViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    var previewUri by remember { mutableStateOf<Uri?>(null) }
    val snackHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.snackFlow.collectLatest { msg ->
            snackHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's Photos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackHostState) }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.shots) { shot ->
                    Box(
                        Modifier
                            .padding(4.dp)
                            .size(100.dp)
                            .combinedClickable(
                                onClick = { vm.onShotClicked(shot.fileName) },
                                onLongClick = { previewUri = shot.uri }
                            )
                    ) {
                        AsyncImage(
                            model = shot.uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (shot.isActive) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Active",
                                tint = Color.Yellow,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(16.dp)
                            )
                        }
                    }
                }
            }

            previewUri?.let { uri ->
                Dialog(onDismissRequest = { previewUri = null }) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}
