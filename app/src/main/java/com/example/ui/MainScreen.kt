package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AdherenceScreen
import com.example.ui.screens.CaregiverScreen
import com.example.ui.screens.MedicationsScreen
import com.example.ui.screens.OnboardingDialog
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TodayScreen

enum class AppTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    TODAY("Today", Icons.Filled.CalendarToday, Icons.Outlined.CalendarToday, "tab_today"),
    MEDICATIONS("Meds", Icons.Filled.Medication, Icons.Outlined.Medication, "tab_medications"),
    ADHERENCE("Adherence", Icons.Filled.Insights, Icons.Outlined.Insights, "tab_adherence"),
    CAREGIVER("Caregiver", Icons.Filled.People, Icons.Outlined.People, "tab_caregiver"),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "tab_settings")
}

@Composable
fun MainScreen(
    viewModel: MedicationViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf(AppTab.TODAY) }

    // Check if onboarding dialog should be shown (if there are no meds yet and not dismissed)
    var showOnboarding by remember(uiState.medications) {
        mutableStateOf(uiState.medications.isEmpty())
    }

    val isLargeText = uiState.settings.isLargeText

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                AppTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    val badgeCount = when (tab) {
                        AppTab.TODAY -> uiState.todayDoses.count { it.log == null }
                        AppTab.MEDICATIONS -> uiState.refillAlerts.size
                        AppTab.CAREGIVER -> uiState.caregivers.sumOf { it.missedAlertCount }
                        else -> 0
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        modifier = Modifier.testTag(tab.tag),
                        icon = {
                            if (badgeCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            Text("$badgeCount")
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            }
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = if (isLargeText) 14.sp else 12.sp
                            )
                        }
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
            Crossfade(
                targetState = currentTab,
                label = "tab_crossfade"
            ) { tab ->
                when (tab) {
                    AppTab.TODAY -> TodayScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        onNavigateToMedications = { currentTab = AppTab.MEDICATIONS }
                    )
                    AppTab.MEDICATIONS -> MedicationsScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                    AppTab.ADHERENCE -> AdherenceScreen(
                        uiState = uiState
                    )
                    AppTab.CAREGIVER -> CaregiverScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                    AppTab.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
            }
        }
    }

    // First-run onboarding flow (< 2 min guided setup)
    if (showOnboarding) {
        OnboardingDialog(
            onComplete = { med, caregiverName ->
                if (med != null) {
                    viewModel.saveMedication(med)
                }
                if (!caregiverName.isNullOrBlank()) {
                    viewModel.generateCaregiverInvite(caregiverName)
                }
                showOnboarding = false
            },
            onDismiss = {
                showOnboarding = false
            }
        )
    }
}
