package com.couplebase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext
import com.couplebase.navigation.RootComponent
import com.couplebase.navigation.RootContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val rootComponent = RootComponent(defaultComponentContext())

        setContent {
            RootContent(rootComponent)
        }
    }
}
