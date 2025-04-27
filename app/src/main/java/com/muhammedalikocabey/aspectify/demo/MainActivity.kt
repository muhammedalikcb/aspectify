package com.muhammedalikocabey.aspectify.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muhammedalikocabey.aspectify.demo.ui.theme.AspectifyTheme
import com.muhammedalikocabey.aspectify.session.UserSession

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        UserSession.init(applicationContext)

        setContent {
            AspectifyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        when (uiState) {
            is UiState.Loading -> {
                CircularProgressIndicator()
            }
            is UiState.Success -> {
                Text(text = (uiState as UiState.Success).data)
            }
            is UiState.Error -> {
                Text(text = (uiState as UiState.Error).message)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { viewModel.loginAndFetch() }) {
            Text("Login Ol")
        }
    }
}
