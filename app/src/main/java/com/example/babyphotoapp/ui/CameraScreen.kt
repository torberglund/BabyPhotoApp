package com.example.babyphotoapp.ui

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.babyphotoapp.R
import com.example.babyphotoapp.createMediaStoreEntry

@Composable
fun CameraScreen(
    navController: NavController,
    vm: PhotoViewModel = viewModel()
) {
    val context = LocalContext.current

    // 1) Request camera permission
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
        vm.loadToday()
    }

    if (!hasCameraPermission) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission is required")
        }
        return
    }

    // 2) Observe if a photo has been taken today
    val takenToday by vm.uiState
        .map { it.isTakenToday }
        .collectAsState(initial = false)

    // 3) Set up CameraX Preview + ImageCapture
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                ProcessCameraProvider.getInstance(ctx).also { future ->
                    future.addListener({
                        val cameraProvider = future.get()
                        val preview = Preview.Builder()
                            .build()
                            .also { it.setSurfaceProvider(surfaceProvider) }
                        imageCapture = ImageCapture.Builder().build()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture
                        )
                    }, ContextCompat.getMainExecutor(ctx))
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )

    // 4) Overlay: status lamp + shutter button
    Box(Modifier.fillMaxSize()) {
        // Status lamp
        Icon(
            painter = painterResource(
                if (takenToday) R.drawable.ic_lamp_green
                else R.drawable.ic_lamp_red
            ),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )

        // Shutter
        FloatingActionButton(
            onClick = {
                val capture = imageCapture ?: return@FloatingActionButton

                // 1) create an empty MediaStore entry
                val (uri, values) = createMediaStoreEntry(context)

                // 2) tell CameraX to write into it
                val outputOptions = ImageCapture.OutputFileOptions
                    .Builder(context.contentResolver, uri, values)
                    .build()

                capture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            // clear pending flag on Q+
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val update = ContentValues().apply {
                                    put(MediaStore.Images.Media.IS_PENDING, 0)
                                }
                                output.savedUri?.let {
                                    context.contentResolver.update(it, update, null, null)
                                }
                            }
                            vm.loadToday()
                            navController.navigate("review")
                        }
                        override fun onError(exc: ImageCaptureException) {
                            exc.printStackTrace()
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Icon(Icons.Filled.CameraAlt, contentDescription = "Capture")
        }
    }
}
