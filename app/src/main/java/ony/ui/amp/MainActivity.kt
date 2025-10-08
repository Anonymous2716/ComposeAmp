package ony.ui.amp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ony.ui.amp.ui.theme.ComposeAmpTheme
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeAmpTheme {
                TextColumns()
            }
        }
    }
}

@Composable
fun TextColumns(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    var batteryInfo by remember { mutableStateOf("") }

    LaunchedEffect(key1 = Unit) {
        while(true) {
            batteryInfo = updateBatteryInfo(context)
            delay(1000)
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        Text(
            text = batteryInfo,
            color = Color(0xFFFAFFE1)
        )
    }
}

private fun updateBatteryInfo(context: Context): String {

    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    val batteryIntent : Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { intentFilter ->
        context.registerReceiver(null, intentFilter)
    }

    //Ampere
    val microAmp = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)

    //Voltage
    val batteryVoltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)?.div(1000.0) ?: -1.0

    //Temperature
    val batteryTemperature = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)?.div(10f) ?: -1f

    //Percentage
    val batteryPercentage = batteryIntent?.let { intent ->
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        level * 100 / scale.coerceAtLeast(1)
    } ?: -1

    //Watt
    val watt = (batteryVoltage * microAmp)/1000000.0

    //Health
    val batteryHealth = batteryIntent?.let { intent ->
        when (val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_COLD -> "COLD"
            BatteryManager.BATTERY_HEALTH_DEAD -> "DEAD"
            BatteryManager.BATTERY_HEALTH_GOOD -> "GOOD"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "OVERHEAT"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "OVER_VOLTAGE"
            BatteryManager.BATTERY_HEALTH_UNKNOWN -> "UNKNOWN"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "UNSPECIFIED_FAILURE"
            else -> "INVALID: $health"
        }
    }

    //Status
    val batteryStatus = batteryIntent?.let { intent ->
        when (val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "CHARGING"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "DISCHARGING"
            BatteryManager.BATTERY_STATUS_FULL -> "FULL"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "NOT CHARGING"
            BatteryManager.BATTERY_STATUS_UNKNOWN -> "UNKNOWN"
            else -> "INVALID: $status"
        }
    }

    //Plugged
    val batteryPlugged = batteryIntent?.let { intent ->
        when (val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "WIRELESS"
            BatteryManager.BATTERY_PLUGGED_DOCK -> "DOCK"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            0 -> "UNPLUGGED"
            else -> "INVALID: $plugged"
        }
    }

    //Present
    val batteryPresent = batteryIntent?.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false)

    //Low
    val batteryLow = batteryIntent?.getBooleanExtra(BatteryManager.EXTRA_BATTERY_LOW, true)

    //Cycle
    val batteryChargeCycle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        batteryIntent?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1)
    } else {
        "Available Android 14+"
    }

    //Technology
    val batteryTechnology = batteryIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)

    return """
        Current: $microAmp
        Watt: %.1f
        Voltage: %.1f
        Percentage: $batteryPercentage
        Temperature: %.1f
        Health: $batteryHealth
        Status: $batteryStatus
        Plugged: $batteryPlugged
        Low: $batteryLow
        Cycle: $batteryChargeCycle
        Present: $batteryPresent
        Technology: $batteryTechnology
    """.trimIndent().format(watt, batteryVoltage, batteryTemperature)
}
