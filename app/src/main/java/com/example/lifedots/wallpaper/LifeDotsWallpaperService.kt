package com.example.lifedots.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.example.lifedots.preferences.DotShape
import com.example.lifedots.preferences.DotSize
import com.example.lifedots.preferences.GridDensity
import com.example.lifedots.preferences.LifeDotsPreferences
import com.example.lifedots.preferences.ThemeOption
import com.example.lifedots.preferences.WallpaperSettings
import java.util.Calendar

class LifeDotsWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return LifeDotsEngine()
    }

    inner class LifeDotsEngine : Engine() {

        private val preferences by lazy { LifeDotsPreferences.getInstance(applicationContext) }
        private val handler = Handler(Looper.getMainLooper())
        private var visible = false
        private var lastDrawnDay = -1

        private val filledPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val todayPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        private val diamondPath = Path()
        private val rectF = RectF()

        private val settingsChangeListener: () -> Unit = {
            handler.post { draw() }
        }

        private val midnightChecker = object : Runnable {
            override fun run() {
                val currentDay = getCurrentDayOfYear()
                if (currentDay != lastDrawnDay) {
                    draw()
                }
                scheduleNextMidnightCheck()
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            LifeDotsPreferences.addWallpaperChangeListener(settingsChangeListener)
        }

        override fun onDestroy() {
            super.onDestroy()
            LifeDotsPreferences.removeWallpaperChangeListener(settingsChangeListener)
            handler.removeCallbacksAndMessages(null)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                draw()
                scheduleNextMidnightCheck()
            } else {
                handler.removeCallbacks(midnightChecker)
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            draw()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacksAndMessages(null)
        }

        private fun scheduleNextMidnightCheck() {
            handler.removeCallbacks(midnightChecker)
            val now = Calendar.getInstance()
            val midnight = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 1)
                set(Calendar.MILLISECOND, 0)
            }
            val delay = midnight.timeInMillis - now.timeInMillis
            handler.postDelayed(midnightChecker, delay)
        }

        private fun getCurrentDayOfYear(): Int {
            return Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        }

        private fun getTotalDaysInYear(): Int {
            val calendar = Calendar.getInstance()
            return calendar.getActualMaximum(Calendar.DAY_OF_YEAR)
        }

        private fun draw() {
            if (!visible) return

            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    drawDots(canvas)
                    lastDrawnDay = getCurrentDayOfYear()
                }
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (e: IllegalArgumentException) {
                        // Surface was destroyed
                    }
                }
            }
        }

        private fun drawDots(canvas: Canvas) {
            val settings = preferences.settings
            val colors = getThemeColors(settings)

            canvas.drawColor(colors.background)

            setupPaints(colors, settings)

            val dayOfYear = getCurrentDayOfYear()
            val totalDays = getTotalDaysInYear()

            val gridConfig = calculateGridConfig(canvas.width, canvas.height, settings, totalDays)

            var dotIndex = 0
            for (row in 0 until gridConfig.rows) {
                for (col in 0 until gridConfig.cols) {
                    if (dotIndex >= totalDays) break

                    val cx = gridConfig.startX + col * gridConfig.cellSize + gridConfig.cellSize / 2
                    val cy = gridConfig.startY + row * gridConfig.cellSize + gridConfig.cellSize / 2

                    val paint = when {
                        dotIndex + 1 == dayOfYear && settings.highlightToday -> todayPaint
                        dotIndex + 1 <= dayOfYear -> filledPaint
                        else -> emptyPaint
                    }

                    drawDot(canvas, cx, cy, gridConfig.dotRadius, paint, settings.dotShape)
                    dotIndex++
                }
                if (dotIndex >= totalDays) break
            }
        }

        private fun drawDot(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint, shape: DotShape) {
            when (shape) {
                DotShape.CIRCLE -> {
                    canvas.drawCircle(cx, cy, radius, paint)
                }
                DotShape.SQUARE -> {
                    rectF.set(cx - radius, cy - radius, cx + radius, cy + radius)
                    canvas.drawRect(rectF, paint)
                }
                DotShape.ROUNDED_SQUARE -> {
                    rectF.set(cx - radius, cy - radius, cx + radius, cy + radius)
                    val cornerRadius = radius * 0.3f
                    canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
                }
                DotShape.DIAMOND -> {
                    diamondPath.reset()
                    diamondPath.moveTo(cx, cy - radius)
                    diamondPath.lineTo(cx + radius, cy)
                    diamondPath.lineTo(cx, cy + radius)
                    diamondPath.lineTo(cx - radius, cy)
                    diamondPath.close()
                    canvas.drawPath(diamondPath, paint)
                }
            }
        }

        private fun setupPaints(colors: ThemeColors, settings: WallpaperSettings) {
            filledPaint.color = colors.filledDot
            filledPaint.style = Paint.Style.FILL
            filledPaint.alpha = (settings.filledDotAlpha * 255).toInt()

            emptyPaint.color = colors.emptyDot
            emptyPaint.style = Paint.Style.FILL
            emptyPaint.alpha = (settings.emptyDotAlpha * 255).toInt()

            todayPaint.color = colors.todayDot
            todayPaint.style = Paint.Style.FILL
            todayPaint.alpha = 255
        }

        private fun calculateGridConfig(
            width: Int,
            height: Int,
            settings: WallpaperSettings,
            totalDots: Int
        ): GridConfig {
            val cols = when (settings.gridDensity) {
                GridDensity.COMPACT -> 21
                GridDensity.NORMAL -> 19
                GridDensity.RELAXED -> 15
                GridDensity.SPACIOUS -> 12
            }

            val rows = (totalDots + cols - 1) / cols

            val dotSizeMultiplier = when (settings.dotSize) {
                DotSize.TINY -> 0.4f
                DotSize.SMALL -> 0.55f
                DotSize.MEDIUM -> 0.7f
                DotSize.LARGE -> 0.85f
                DotSize.HUGE -> 0.95f
            }

            val paddingPercent = when (settings.gridDensity) {
                GridDensity.COMPACT -> 0.06f
                GridDensity.NORMAL -> 0.08f
                GridDensity.RELAXED -> 0.10f
                GridDensity.SPACIOUS -> 0.12f
            }

            val horizontalPadding = width * paddingPercent
            val verticalPadding = height * paddingPercent

            val availableWidth = width - (2 * horizontalPadding)
            val availableHeight = height - (2 * verticalPadding)

            val cellSizeByWidth = availableWidth / cols
            val cellSizeByHeight = availableHeight / rows
            val cellSize = minOf(cellSizeByWidth, cellSizeByHeight)

            val gridWidth = cols * cellSize
            val gridHeight = rows * cellSize

            val startX = (width - gridWidth) / 2
            val startY = (height - gridHeight) / 2

            val dotRadius = (cellSize / 2) * dotSizeMultiplier

            return GridConfig(
                cols = cols,
                rows = rows,
                cellSize = cellSize,
                dotRadius = dotRadius,
                startX = startX,
                startY = startY
            )
        }

        private fun getThemeColors(settings: WallpaperSettings): ThemeColors {
            return when (settings.theme) {
                ThemeOption.LIGHT -> ThemeColors(
                    background = Color.parseColor("#F5F5F5"),
                    filledDot = Color.parseColor("#2C2C2C"),
                    emptyDot = Color.parseColor("#D0D0D0"),
                    todayDot = Color.parseColor("#4A90D9")
                )
                ThemeOption.DARK -> ThemeColors(
                    background = Color.parseColor("#1A1A1A"),
                    filledDot = Color.parseColor("#E0E0E0"),
                    emptyDot = Color.parseColor("#3A3A3A"),
                    todayDot = Color.parseColor("#5BA0E9")
                )
                ThemeOption.AMOLED -> ThemeColors(
                    background = Color.parseColor("#000000"),
                    filledDot = Color.parseColor("#FFFFFF"),
                    emptyDot = Color.parseColor("#2A2A2A"),
                    todayDot = Color.parseColor("#6AB0F9")
                )
                ThemeOption.CUSTOM -> ThemeColors(
                    background = settings.customColors.backgroundColor,
                    filledDot = settings.customColors.filledDotColor,
                    emptyDot = settings.customColors.emptyDotColor,
                    todayDot = settings.customColors.todayDotColor
                )
            }
        }
    }

    private data class ThemeColors(
        val background: Int,
        val filledDot: Int,
        val emptyDot: Int,
        val todayDot: Int
    )

    private data class GridConfig(
        val cols: Int,
        val rows: Int,
        val cellSize: Float,
        val dotRadius: Float,
        val startX: Float,
        val startY: Float
    )
}
