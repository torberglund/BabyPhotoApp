package com.example.babyphotoapp.ui

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.babyphotoapp.createMediaStoreEntry
import java.util.*

@Composable
fun CameraScreen(navController: NavController) {
    val context = LocalContext.current

    // camera permission
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    if (!hasPermission) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Camera permission is required")
        }
        return
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                ProcessCameraProvider.getInstance(ctx).also { future ->
                    future.addListener({
                        val provider = future.get()
                        val preview = Preview.Builder().build()
                            .also { it.setSurfaceProvider(surfaceProvider) }
                        imageCapture = ImageCapture.Builder().build()
                        provider.bindToLifecycle(
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

    Box(Modifier.fillMaxSize()) {
        FloatingActionButton(
            onClick = {
                // 1) create MediaStore entry + values
                val (uri, values) = createMediaStoreEntry(context)
                // 2) request CameraX to write into that Uri
                imageCapture?.takePicture(
                    ImageCapture.OutputFileOptions
                        .Builder(context.contentResolver, uri, values)
                        .build(),
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
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
