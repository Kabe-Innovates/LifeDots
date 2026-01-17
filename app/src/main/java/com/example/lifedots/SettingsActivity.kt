package com.example.lifedots

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedots.preferences.DotShape
import com.example.lifedots.preferences.DotSize
import com.example.lifedots.preferences.GridDensity
import com.example.lifedots.preferences.LifeDotsPreferences
import com.example.lifedots.preferences.ThemeOption
import com.example.lifedots.ui.components.ColorButton
import com.example.lifedots.ui.components.ColorPickerDialog
import com.example.lifedots.ui.theme.LifeDotsTheme
import kotlin.math.roundToInt

class SettingsActivity : ComponentActivity() {

    private lateinit var preferences: LifeDotsPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        preferences = LifeDotsPreferences.getInstance(this)

        setContent {
            LifeDotsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SettingsScreen(
                        preferences = preferences,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    preferences: LifeDotsPreferences,
    modifier: Modifier = Modifier
) {
    val settings by preferences.settingsFlow.collectAsState()

    var showBgColorPicker by remember { mutableStateOf(false) }
    var showFilledColorPicker by remember { mutableStateOf(false) }
    var showEmptyColorPicker by remember { mutableStateOf(false) }
    var showTodayColorPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Theme Section
        SettingsSection(title = stringResource(R.string.theme_section)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionButton(
                    label = stringResource(R.string.theme_light),
                    backgroundColor = Color(0xFFF5F5F5),
                    dotColor = Color(0xFF2C2C2C),
                    isSelected = settings.theme == ThemeOption.LIGHT,
                    onClick = { preferences.setTheme(ThemeOption.LIGHT) },
                    modifier = Modifier.weight(1f)
                )
                ThemeOptionButton(
                    label = stringResource(R.string.theme_dark),
                    backgroundColor = Color(0xFF1A1A1A),
                    dotColor = Color(0xFFE0E0E0),
                    isSelected = settings.theme == ThemeOption.DARK,
                    onClick = { preferences.setTheme(ThemeOption.DARK) },
                    modifier = Modifier.weight(1f)
                )
                ThemeOptionButton(
                    label = stringResource(R.string.theme_amoled),
                    backgroundColor = Color(0xFF000000),
                    dotColor = Color(0xFFFFFFFF),
                    isSelected = settings.theme == ThemeOption.AMOLED,
                    onClick = { preferences.setTheme(ThemeOption.AMOLED) },
                    modifier = Modifier.weight(1f)
                )
                ThemeOptionButton(
                    label = "Custom",
                    backgroundColor = Color(settings.customColors.backgroundColor),
                    dotColor = Color(settings.customColors.filledDotColor),
                    isSelected = settings.theme == ThemeOption.CUSTOM,
                    onClick = { preferences.setTheme(ThemeOption.CUSTOM) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Custom Colors Section (visible when Custom theme selected)
        AnimatedVisibility(
            visible = settings.theme == ThemeOption.CUSTOM,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Custom Colors") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ColorButton(
                            color = settings.customColors.backgroundColor,
                            label = "Background",
                            onClick = { showBgColorPicker = true }
                        )
                        ColorButton(
                            color = settings.customColors.filledDotColor,
                            label = "Filled Dots",
                            onClick = { showFilledColorPicker = true }
                        )
                        ColorButton(
                            color = settings.customColors.emptyDotColor,
                            label = "Empty Dots",
                            onClick = { showEmptyColorPicker = true }
                        )
                        ColorButton(
                            color = settings.customColors.todayDotColor,
                            label = "Today's Dot",
                            onClick = { showTodayColorPicker = true }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dot Shape Section
        SettingsSection(title = "Dot Shape") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DotShapeOption(
                    shape = DotShape.CIRCLE,
                    label = "Circle",
                    isSelected = settings.dotShape == DotShape.CIRCLE,
                    onClick = { preferences.setDotShape(DotShape.CIRCLE) },
                    modifier = Modifier.weight(1f)
                )
                DotShapeOption(
                    shape = DotShape.SQUARE,
                    label = "Square",
                    isSelected = settings.dotShape == DotShape.SQUARE,
                    onClick = { preferences.setDotShape(DotShape.SQUARE) },
                    modifier = Modifier.weight(1f)
                )
                DotShapeOption(
                    shape = DotShape.ROUNDED_SQUARE,
                    label = "Rounded",
                    isSelected = settings.dotShape == DotShape.ROUNDED_SQUARE,
                    onClick = { preferences.setDotShape(DotShape.ROUNDED_SQUARE) },
                    modifier = Modifier.weight(1f)
                )
                DotShapeOption(
                    shape = DotShape.DIAMOND,
                    label = "Diamond",
                    isSelected = settings.dotShape == DotShape.DIAMOND,
                    onClick = { preferences.setDotShape(DotShape.DIAMOND) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dot Size Section
        SettingsSection(title = stringResource(R.string.dot_size_section)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DotSizeOption(
                    label = "Tiny",
                    dotSize = 6.dp,
                    isSelected = settings.dotSize == DotSize.TINY,
                    onClick = { preferences.setDotSize(DotSize.TINY) },
                    modifier = Modifier.weight(1f)
                )
                DotSizeOption(
                    label = stringResource(R.string.dot_size_small),
                    dotSize = 8.dp,
                    isSelected = settings.dotSize == DotSize.SMALL,
                    onClick = { preferences.setDotSize(DotSize.SMALL) },
                    modifier = Modifier.weight(1f)
                )
                DotSizeOption(
                    label = stringResource(R.string.dot_size_medium),
                    dotSize = 12.dp,
                    isSelected = settings.dotSize == DotSize.MEDIUM,
                    onClick = { preferences.setDotSize(DotSize.MEDIUM) },
                    modifier = Modifier.weight(1f)
                )
                DotSizeOption(
                    label = stringResource(R.string.dot_size_large),
                    dotSize = 16.dp,
                    isSelected = settings.dotSize == DotSize.LARGE,
                    onClick = { preferences.setDotSize(DotSize.LARGE) },
                    modifier = Modifier.weight(1f)
                )
                DotSizeOption(
                    label = "Huge",
                    dotSize = 20.dp,
                    isSelected = settings.dotSize == DotSize.HUGE,
                    onClick = { preferences.setDotSize(DotSize.HUGE) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Grid Density Section
        SettingsSection(title = stringResource(R.string.grid_density_section)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GridDensityOption(
                    label = stringResource(R.string.grid_compact),
                    columns = 6,
                    isSelected = settings.gridDensity == GridDensity.COMPACT,
                    onClick = { preferences.setGridDensity(GridDensity.COMPACT) },
                    modifier = Modifier.weight(1f)
                )
                GridDensityOption(
                    label = "Normal",
                    columns = 5,
                    isSelected = settings.gridDensity == GridDensity.NORMAL,
                    onClick = { preferences.setGridDensity(GridDensity.NORMAL) },
                    modifier = Modifier.weight(1f)
                )
                GridDensityOption(
                    label = stringResource(R.string.grid_relaxed),
                    columns = 4,
                    isSelected = settings.gridDensity == GridDensity.RELAXED,
                    onClick = { preferences.setGridDensity(GridDensity.RELAXED) },
                    modifier = Modifier.weight(1f)
                )
                GridDensityOption(
                    label = "Spacious",
                    columns = 3,
                    isSelected = settings.gridDensity == GridDensity.SPACIOUS,
                    onClick = { preferences.setGridDensity(GridDensity.SPACIOUS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Transparency Section
        SettingsSection(title = "Transparency") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Filled dots alpha
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filled Dots",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${(settings.filledDotAlpha * 100).roundToInt()}%",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Slider(
                        value = settings.filledDotAlpha,
                        onValueChange = { preferences.setFilledDotAlpha(it) },
                        valueRange = 0.1f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Empty dots alpha
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Empty Dots",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${(settings.emptyDotAlpha * 100).roundToInt()}%",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Slider(
                        value = settings.emptyDotAlpha,
                        onValueChange = { preferences.setEmptyDotAlpha(it) },
                        valueRange = 0.1f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Highlight Today Toggle
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.highlight_today),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.highlight_today_desc),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = settings.highlightToday,
                    onCheckedChange = { preferences.setHighlightToday(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Color Picker Dialogs
    if (showBgColorPicker) {
        ColorPickerDialog(
            initialColor = settings.customColors.backgroundColor,
            title = "Background Color",
            onColorSelected = {
                preferences.setCustomBackgroundColor(it)
                showBgColorPicker = false
            },
            onDismiss = { showBgColorPicker = false }
        )
    }

    if (showFilledColorPicker) {
        ColorPickerDialog(
            initialColor = settings.customColors.filledDotColor,
            title = "Filled Dots Color",
            onColorSelected = {
                preferences.setCustomFilledDotColor(it)
                showFilledColorPicker = false
            },
            onDismiss = { showFilledColorPicker = false }
        )
    }

    if (showEmptyColorPicker) {
        ColorPickerDialog(
            initialColor = settings.customColors.emptyDotColor,
            title = "Empty Dots Color",
            onColorSelected = {
                preferences.setCustomEmptyDotColor(it)
                showEmptyColorPicker = false
            },
            onDismiss = { showEmptyColorPicker = false }
        )
    }

    if (showTodayColorPicker) {
        ColorPickerDialog(
            initialColor = settings.customColors.todayDotColor,
            title = "Today's Dot Color",
            onColorSelected = {
                preferences.setCustomTodayDotColor(it)
                showTodayColorPicker = false
            },
            onDismiss = { showTodayColorPicker = false }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
fun ThemeOptionButton(
    label: String,
    backgroundColor: Color,
    dotColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DotShapeOption(
    shape: DotShape,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val dotColor = MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(20.dp)) {
                    when (shape) {
                        DotShape.CIRCLE -> {
                            drawCircle(color = dotColor)
                        }
                        DotShape.SQUARE -> {
                            drawRect(color = dotColor)
                        }
                        DotShape.ROUNDED_SQUARE -> {
                            drawRoundRect(
                                color = dotColor,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                            )
                        }
                        DotShape.DIAMOND -> {
                            val path = Path().apply {
                                moveTo(size.width / 2, 0f)
                                lineTo(size.width, size.height / 2)
                                lineTo(size.width / 2, size.height)
                                lineTo(0f, size.height / 2)
                                close()
                            }
                            drawPath(path, dotColor)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DotSizeOption(
    label: String,
    dotSize: Dp,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.height(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun GridDensityOption(
    label: String,
    columns: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(3) {
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(columns) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
