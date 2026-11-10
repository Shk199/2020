package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AlignerTab
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SliceVoiceScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceContainerHigh
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.MainViewModel

/**
 * Main Activity for SliceVoice
 * Minimalist, high-performance audio & text forced aligner tool.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    SliceVoiceApp()
                }
            }
        }
    }
}

@Composable
fun SliceVoiceApp(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Instant user feedback / snackbar messages
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissToast()
        }
    }

    // Return to STUDIO tab when back is pressed while in HISTORY or SETTINGS
    BackHandler(enabled = uiState.selectedTab != AlignerTab.STUDIO) {
        viewModel.selectTab(AlignerTab.STUDIO)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                val isFailure = data.visuals.message.contains("خطأ") ||
                    data.visuals.message.contains("تعذر") ||
                    data.visuals.message.contains("فشل") ||
                    data.visuals.message.contains("أولاً")
                val isWarning = data.visuals.message.contains("تنبيه") ||
                    data.visuals.message.contains("تحذير")
                val isSuccess = data.visuals.message.contains("بنجاح") ||
                    data.visuals.message.contains("✓")

                val contentColor = when {
                    isFailure -> ErrorRed
                    isWarning -> WarningAmber
                    isSuccess -> NeonEmerald
                    else -> TextPrimary
                }

                androidx.compose.material3.Snackbar(
                    snackbarData = data,
                    containerColor = DarkSurfaceContainerHigh,
                    contentColor = contentColor
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = TextPrimary
            ) {
                val navItems = listOf(
                    Triple(AlignerTab.STUDIO, "المُطابق", Icons.Default.Tune),
                    Triple(AlignerTab.HISTORY, "السجل", Icons.Default.History),
                    Triple(AlignerTab.SETTINGS, "الإعدادات", Icons.Default.Settings)
                )

                navItems.forEach { (tab, label, icon) ->
                    val isSelected = uiState.selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(tab) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricCyan,
                            selectedTextColor = ElectricCyan,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = DarkSurfaceContainerHigh
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.selectedTab) {
                AlignerTab.STUDIO -> SliceVoiceScreen(viewModel = viewModel)
                AlignerTab.HISTORY -> HistoryScreen(viewModel = viewModel)
                AlignerTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                else -> SliceVoiceScreen(viewModel = viewModel)
            }
        }
    }
}
