// app/src/main/java/com/example/babyphotoapp/ui/SettingsScreen.kt
package com.example.babyphotoapp.ui
import androidx.compose.ui.text.input.PasswordVisualTransformation

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun SettingsScreen(
    navController: NavController,
    vm: SettingsViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    val syncStatus by vm.syncStatus.collectAsState()

    // show a Snackbar on sync success/failure
    val scaffoldState = rememberScaffoldState()
    LaunchedEffect(syncStatus) {
        when (syncStatus) {
            SyncStatus.Success -> scaffoldState.snackbarHostState.showSnackbar("Sync succeeded!")
            is SyncStatus.Error -> scaffoldState.snackbarHostState.showSnackbar("Sync failed: ${(syncStatus as SyncStatus.Error).msg}")
            else -> {}
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton({ navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier
            .padding(padding)
            .padding(16.dp)
            .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.host,
                onValueChange = vm::onHostChange,
                label = { Text("NAS Host") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.port,
                onValueChange = vm::onPortChange,
                label = { Text("Port") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.user,
                onValueChange = vm::onUserChange,
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.pass,
                onValueChange = vm::onPassChange,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.remotePath,
                onValueChange = vm::onRemotePathChange,
                label = { Text("Remote Path") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { vm.syncNow() },
                enabled = syncStatus != SyncStatus.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (syncStatus == SyncStatus.Loading) "Syncing…" else "Sync Now")
            }
        }
    }
}
