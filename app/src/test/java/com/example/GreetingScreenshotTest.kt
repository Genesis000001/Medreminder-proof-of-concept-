package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.data.model.Medication
import com.example.data.model.MedicationForm
import com.example.ui.ScheduledDoseItem
import com.example.ui.screens.DoseCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun dose_card_screenshot() {
        val testMed = Medication(
            id = 1,
            name = "Lisinopril",
            dosage = "10 mg",
            form = MedicationForm.PILL,
            instructions = "Take in the morning with water"
        )
        val doseItem = ScheduledDoseItem(
            medication = testMed,
            timeString = "08:00 AM",
            scheduledTimeMillis = System.currentTimeMillis(),
            log = null,
            isOverdue = false
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                Box(modifier = Modifier.padding(16.dp)) {
                    DoseCard(
                        doseItem = doseItem,
                        isLargeText = false,
                        onMarkTaken = {},
                        onMarkSkipped = {},
                        onSnooze = {}
                    )
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
