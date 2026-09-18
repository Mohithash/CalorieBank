package com.mohithash.caloriebank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mohithash.caloriebank.ui.AppViewModel
import com.mohithash.caloriebank.ui.CalorieBankNav
import com.mohithash.caloriebank.ui.theme.CalorieBankTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as CalorieBankApp
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(app.repo, app.ai) as T
        }
        setContent {
            CalorieBankTheme {
                val vm: AppViewModel = viewModel(factory = factory)
                CalorieBankNav(vm)
            }
        }
    }
}
