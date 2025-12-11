package com.tagmypet.ui.components.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

@Composable
fun PawIcon(
    modifier: Modifier = Modifier,
    color: Color,
    filled: Boolean = false
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val stroke = if (filled) null else Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)

        // Função auxiliar para desenhar elipses (toes/pads)
        fun drawPad(center: Offset, size: Size, rotation: Float = 0f) {
            rotate(rotation, pivot = center) {
                drawOval(
                    color = color,
                    topLeft = Offset(center.x - size.width / 2, center.y - size.height / 2),
                    size = size,
                    style = stroke ?: androidx.compose.ui.graphics.drawscope.Fill
                )
            }
        }

        val w = size.width
        val h = size.height

        // Coordenadas baseadas no SVG viewBox 0 0 24 24
        // Main pad (cx=12 cy=17 rx=4 ry=3.5)
        drawPad(
            center = Offset(w * (12f/24f), h * (17f/24f)),
            size = Size(w * (8f/24f), h * (7f/24f))
        )

        // Top left toe (cx=6.5 cy=10 rx=2 ry=2.5)
        drawPad(
            center = Offset(w * (6.5f/24f), h * (10f/24f)),
            size = Size(w * (4f/24f), h * (5f/24f))
        )

        // Top right toe (cx=17.5 cy=10 rx=2 ry=2.5)
        drawPad(
            center = Offset(w * (17.5f/24f), h * (10f/24f)),
            size = Size(w * (4f/24f), h * (5f/24f))
        )

        // Bottom left toe (cx=8 cy=14 rx=1.5 ry=2)
        drawPad(
            center = Offset(w * (8f/24f), h * (14f/24f)),
            size = Size(w * (3f/24f), h * (4f/24f))
        )

        // Bottom right toe (cx=16 cy=14 rx=1.5 ry=2)
        drawPad(
            center = Offset(w * (16f/24f), h * (14f/24f)),
            size = Size(w * (3f/24f), h * (4f/24f))
        )
    }
}