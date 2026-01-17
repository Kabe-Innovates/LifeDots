package com.example.lifedots.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlin.math.roundToInt

@Composable
fun ColorPickerDialog(
    initialColor: Int,
    title: String,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var hue by remember { mutableFloatStateOf(getHueFromColor(initialColor)) }
    var saturation by remember { mutableFloatStateOf(getSaturationFromColor(initialColor)) }
    var brightness by remember { mutableFloatStateOf(getBrightnessFromColor(initialColor)) }

    val currentColor = remember(hue, saturation, brightness) {
        android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Color preview
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(currentColor))
                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Hue slider
                Text(
                    text = "Hue",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                )
                HueSlider(
                    hue = hue,
                    onHueChange = { hue = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Saturation slider
                Text(
                    text = "Saturation",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                )
                Slider(
                    value = saturation,
                    onValueChange = { saturation = it },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(currentColor),
                        activeTrackColor = Color(currentColor)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Brightness slider
                Text(
                    text = "Brightness",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                )
                Slider(
                    value = brightness,
                    onValueChange = { brightness = it },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(currentColor),
                        activeTrackColor = Color(currentColor)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Preset colors
                Text(
                    text = "Presets",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                PresetColors(
                    onColorSelected = { color ->
                        hue = getHueFromColor(color)
                        saturation = getSaturationFromColor(color)
                        brightness = getBrightnessFromColor(color)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { onColorSelected(currentColor) }) {
                        Text("Select")
                    }
                }
            }
        }
    }
}

@Composable
fun HueSlider(
    hue: Float,
    onHueChange: (Float) -> Unit
) {
    val hueColors = remember {
        listOf(
            Color.Red,
            Color.Yellow,
            Color.Green,
            Color.Cyan,
            Color.Blue,
            Color.Magenta,
            Color.Red
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(vertical = 8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val newHue = (offset.x / size.width) * 360f
                        onHueChange(newHue.coerceIn(0f, 360f))
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val newHue = (change.position.x / size.width) * 360f
                        onHueChange(newHue.coerceIn(0f, 360f))
                    }
                }
        ) {
            drawRect(
                brush = Brush.horizontalGradient(hueColors)
            )

            // Draw indicator
            val indicatorX = (hue / 360f) * size.width
            drawCircle(
                color = Color.White,
                radius = 14f,
                center = Offset(indicatorX, size.height / 2)
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.3f),
                radius = 12f,
                center = Offset(indicatorX, size.height / 2)
            )
        }
    }
}

@Composable
fun PresetColors(
    onColorSelected: (Int) -> Unit
) {
    val presets = listOf(
        0xFFFFFFFF.toInt(), // White
        0xFF000000.toInt(), // Black
        0xFFE0E0E0.toInt(), // Light gray
        0xFF3A3A3A.toInt(), // Dark gray
        0xFF1A1A1A.toInt(), // Almost black
        0xFFF5F5F5.toInt(), // Off white
        0xFF4A90D9.toInt(), // Blue
        0xFF5BA0E9.toInt(), // Light blue
        0xFFE74C3C.toInt(), // Red
        0xFFE67E22.toInt(), // Orange
        0xFFF39C12.toInt(), // Yellow
        0xFF2ECC71.toInt(), // Green
        0xFF9B59B6.toInt(), // Purple
        0xFFE91E63.toInt(), // Pink
        0xFF00BCD4.toInt(), // Cyan
        0xFF795548.toInt()  // Brown
    )

    Column {
        for (row in 0 until 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0 until 8) {
                    val index = row * 8 + col
                    if (index < presets.size) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(presets[index]))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                                .clickable { onColorSelected(presets[index]) }
                        )
                    }
                }
            }
            if (row == 0) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ColorButton(
    color: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(color))
                    .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
            )
        }
    }
}

private fun getHueFromColor(color: Int): Float {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color, hsv)
    return hsv[0]
}

private fun getSaturationFromColor(color: Int): Float {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color, hsv)
    return hsv[1]
}

private fun getBrightnessFromColor(color: Int): Float {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color, hsv)
    return hsv[2]
}
