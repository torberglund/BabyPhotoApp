package com.example.babyphotoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.babyphotoapp.ui.NavGraph
import com.example.babyphotoapp.ui.theme.BabyPhotoAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BabyPhotoAppTheme {
                NavGraph()
            }
        }
    }
}
