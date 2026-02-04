package com.sanika.yearprogresswallpaper

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.graphics.*
import java.util.*

class YearProgressWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return YearProgressEngine()
    }

    inner class YearProgressEngine : Engine() {

        private val paint = Paint()

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            drawWallpaper()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                drawWallpaper()
            }
        }

        private fun drawWallpaper() {
            val holder = surfaceHolder
            val canvas = holder.lockCanvas() ?: return

            try {
                val width = canvas.width.toFloat()
                val height = canvas.height.toFloat()

                // ===== BACKGROUND =====
                val bgPaint = Paint()
                bgPaint.shader = LinearGradient(
                    0f, 0f, 0f, height,
                    Color.parseColor("#0E0820"),
                    Color.parseColor("#1E1038"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, width, height, bgPaint)

                val calendar = Calendar.getInstance()
                val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
                val year = calendar.get(Calendar.YEAR)

                val isLeap = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
                val totalDays = if (isLeap) 366 else 365
                val percent = dayOfYear * 100f / totalDays

                paint.isAntiAlias = true
                paint.textAlign = Paint.Align.CENTER

                // ===== MONTH START DAYS =====
                val monthStartDays = HashSet<Int>()
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                for (m in 0..11) {
                    cal.set(Calendar.MONTH, m)
                    monthStartDays.add(cal.get(Calendar.DAY_OF_YEAR))
                }

                // ===== CENTER BLOCK POSITION (SHIFTED DOWN) =====
                val blockCenterY = height * 0.60f   // was 0.50f

                // ===== YEAR TEXT (SMALLER) =====
                paint.color = Color.WHITE
                paint.typeface = Typeface.DEFAULT
                paint.textSize = height * 0.038f   // was 0.055f
                canvas.drawText("$year", width / 2, blockCenterY - height * 0.30f, paint)

                paint.textSize = height * 0.024f
                paint.color = Color.parseColor("#D6CFFF")
                canvas.drawText("$dayOfYear Day started", width / 2, blockCenterY - height * 0.24f, paint)

                // ===== DOT GRID =====
                val cols = 22
                val rows = 17

                val gridWidth = width * 0.78f
                val gridHeight = height * 0.42f

                val cellSize = minOf(gridWidth / cols, gridHeight / rows)
                val spacingFactor = 1.12f
                val spacedCell = cellSize * spacingFactor
                val dotRadius = cellSize * 0.38f

                val startX = width / 2 - (cols * spacedCell) / 2
                val startY = blockCenterY - (rows * spacedCell) / 2

                var index = 1
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        if (index > totalDays) break

                        val cx = startX + c * spacedCell + spacedCell / 2
                        val cy = startY + r * spacedCell + spacedCell / 2

                        val isMonthStart = monthStartDays.contains(index)

                        val isToday = (index == dayOfYear)

                        if (isToday) {
                            // Glow halo
                            val glowPaint = Paint(paint)
                            glowPaint.color = Color.WHITE
                            glowPaint.maskFilter = BlurMaskFilter(dotRadius * 2.5f, BlurMaskFilter.Blur.NORMAL)
                            canvas.drawCircle(cx, cy, dotRadius * 1.8f, glowPaint)
                        }

// Normal dot color
                        paint.color = when {
                            isMonthStart -> Color.parseColor("#FF9F1C")   // 🟠 month start
                            index <= dayOfYear -> Color.WHITE
                            else -> Color.parseColor("#4A3B75")
                        }

                        canvas.drawCircle(cx, cy, dotRadius, paint)
                        index++

                    }
                }

                // ===== PROGRESS BAR =====
                val gridBottom = startY + rows * spacedCell
                val barY = gridBottom + height * 0.035f

                val barWidth = width * 0.65f
                val barHeight = height * 0.004f
                val barLeft = (width - barWidth) / 2

                paint.color = Color.parseColor("#4A3B75")
                canvas.drawRoundRect(barLeft, barY, barLeft + barWidth, barY + barHeight, 50f, 50f, paint)

                paint.color = Color.WHITE
                canvas.drawRoundRect(
                    barLeft,
                    barY,
                    barLeft + (barWidth * percent / 100f),
                    barY + barHeight,
                    50f,
                    50f,
                    paint
                )

                // ===== PERCENT TEXT =====
                paint.textSize = height * 0.020f
                paint.color = Color.parseColor("#EAE6FF")
                paint.typeface = Typeface.DEFAULT
                canvas.drawText(
                    String.format("%.1f%%", percent),
                    width / 2,
                    barY - height * 0.012f,
                    paint
                )

                // ===== SIGNATURE =====
                paint.textSize = height * 0.015f
                paint.color = Color.parseColor("#B8A9D9")
                paint.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                canvas.drawText(
                    "developed by Sanika",
                    width / 2,
                    height * 0.95f,
                    paint
                )

            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }










    }
}
