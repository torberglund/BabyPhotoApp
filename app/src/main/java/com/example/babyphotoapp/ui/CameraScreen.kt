// File: app/src/main/java/com/example/babyphotoapp/ui/CameraScreen.kt
package com.example.babyphotoapp.ui

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
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
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

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

    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            // Once we have permission, load today's shots
            vm.loadToday()
        }
    }

    if (!hasCameraPermission) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission is required")
        }
        return
    }

    // 2) Observe if a photo has already been taken today
    val takenToday by vm.uiState
        .map { it.isTakenToday }
        .collectAsState(initial = false)

    // 3) Set up CameraX Preview + ImageCapture
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val provider = cameraProviderFuture.get()

                // 1) always unbind before (re-)binding
                provider.unbindAll()

                // 2) build both with 4:3 aspect
                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                imageCapture = ImageCapture.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                // 3) bind them both at once
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )

            }, ContextCompat.getMainExecutor(ctx))

            previewView
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

        // Shutter button
        FloatingActionButton(
            onClick = {
                val capture = imageCapture ?: return@FloatingActionButton

                // Prepare timestamped file + per-day folder
                val now = Date()
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now)
                val displayName = "IMG_$timestamp.jpg"
                val datePath = SimpleDateFormat("yyyy/MM/dd", Locale.US).format(now)

                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // Write into DCIM/BabyPhotoApp/yyyy/MM/dd/
                        put(
                            MediaStore.Images.Media.RELATIVE_PATH,
                            "DCIM/BabyPhotoApp/$datePath/"
                        )
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }

                val outputOptions = ImageCapture.OutputFileOptions
                    .Builder(
                        context.contentResolver,
                        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                        contentValues
                    )
                    .build()

                capture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                output.savedUri?.let { uri ->
                                    val update = ContentValues().apply {
                                        put(MediaStore.Images.Media.IS_PENDING, 0)
                                    }
                                    context.contentResolver.update(uri, update, null, null)
                                }
                            }
                            vm.loadToday()
                            navController.navigate("review")
                        }
                        override fun onError(exc: ImageCaptureException) {
                            exc.printStackTrace()
                            // Optionally show a Snackbar or Toast here
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
