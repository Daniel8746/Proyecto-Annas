package com.pmdm.annas.ui.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pmdm.annas.ui.navigation.AnnasNavHost
import com.pmdm.annas.ui.theme.AnnasTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnnasTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AnnasNavHost()
                }
            }
        }
    }
}