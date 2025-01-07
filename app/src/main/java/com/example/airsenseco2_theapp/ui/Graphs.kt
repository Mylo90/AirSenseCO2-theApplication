package com.example.airsenseco2_theapp.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Graphs(data: List<Pair<Int, Float>>, modifier: Modifier = Modifier) {
    val minYValue = 0f
    val maxYValue = 50000f
    val displayMaxYValue = 50f  // 50 motsvarar 50k på Y-axeln
    val timeWindow = 10
    val padding = 40.dp

    fun linearScale(value: Float): Float {
        // Skala om värden från 10000–50000 till 0–50
        return ((value - minYValue) / (maxYValue - minYValue)) * displayMaxYValue
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        if (data.isNotEmpty()) {
            val latestData = data.takeLast(timeWindow)

            val maxX = latestData.maxOf { it.first }
            val minX = latestData.minOf { it.first }

            val scaleX = (size.width - padding.toPx() * 2) / (timeWindow.toFloat())
            val scaleY = (size.height - padding.toPx() * 2) / displayMaxYValue

            val textPaint = Paint().asFrameworkPaint().apply {
                color = Color.Black.toArgb()
                textSize = 14.sp.toPx()
            }

            // Rita axlar
            drawLine(
                color = Color.Gray,
                start = Offset(padding.toPx(), size.height - padding.toPx()),
                end = Offset(size.width - padding.toPx(), size.height - padding.toPx()),
                strokeWidth = 3f
            )
            drawLine(
                color = Color.Gray,
                start = Offset(padding.toPx(), padding.toPx()),
                end = Offset(padding.toPx(), size.height - padding.toPx()),
                strokeWidth = 3f
            )

            // Rita Y-axel etiketter (5k till 50k)
            for (i in 0..10) {
                val labelValue = (i * 5000) + 0  // Börjar på 10k, går upp till 50k
                val y = size.height - padding.toPx() - linearScale(labelValue.toFloat()) * scaleY

                drawLine(
                    color = Color.LightGray,
                    start = Offset(padding.toPx(), y),
                    end = Offset(size.width - padding.toPx(), y),
                    strokeWidth = 1f
                )
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        "${labelValue / 1000}k",
                        8f,
                        y + 5f,
                        textPaint
                    )
                }
            }

            // Rita graflinje
            latestData.zipWithNext { a, b ->
                drawLine(
                    start = Offset(
                        padding.toPx() + (a.first - minX) * scaleX,
                        size.height - padding.toPx() - linearScale(a.second) * scaleY
                    ),
                    end = Offset(
                        padding.toPx() + (b.first - minX) * scaleX,
                        size.height - padding.toPx() - linearScale(b.second) * scaleY
                    ),
                    color = Color.Blue,
                    strokeWidth = 4f
                )
            }
        }
    }
}
