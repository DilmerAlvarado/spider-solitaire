package com.mun0n.spider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mun0n.spider.ui.SpiderTheme
import com.mun0n.spider.ui.TableScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpiderTheme {
                TableScreen()
            }
        }
    }
}
