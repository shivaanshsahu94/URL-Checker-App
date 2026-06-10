package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun StickHistoryIcon(color: Color, modifier: Modifier = Modifier) {
    val arrowPath = remember { Path() }
    Canvas(modifier = modifier.size(24.dp)) {
        val r = size.width / 2f - 3f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Draw Clock outline
        drawCircle(
            color = color,
            radius = r,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        // Clock Center Dot
        drawCircle(
            color = color,
            radius = 2.dp.toPx(),
            center = center
        )

        // Hands
        drawLine(
            color = color,
            start = center,
            end = Offset(center.x, center.y - r * 0.5f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = color,
            start = center,
            end = Offset(center.x + r * 0.4f, center.y),
            strokeWidth = 2.dp.toPx()
        )

        // Little arrow tip on top to feel like recurring history loop context
        arrowPath.reset()
        arrowPath.moveTo(center.x - 3.dp.toPx(), center.y - r - 2.dp.toPx())
        arrowPath.lineTo(center.x + 2.dp.toPx(), center.y - r)
        arrowPath.lineTo(center.x - 3.dp.toPx(), center.y - r + 3.dp.toPx())
        drawPath(
            path = arrowPath,
            color = color,
            style = Stroke(width = 1.5f.dp.toPx())
        )
    }
}

@Composable
fun StickSettingsIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = size.width / 2f - 4f
        val innerRadius = size.width / 2f - 9f

        // Draw center hole
        drawCircle(
            color = color,
            radius = innerRadius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw outer bounding circle
        drawCircle(
            color = color,
            radius = outerRadius,
            center = center,
            style = Stroke(width = 1.5f.dp.toPx())
        )

        // Draw gear pins (spokes)
        val numTeeth = 8
        for (i in 0 until numTeeth) {
            val angle = i * (2 * Math.PI / numTeeth)
            val startX = (center.x + innerRadius * Math.cos(angle)).toFloat()
            val startY = (center.y + innerRadius * Math.sin(angle)).toFloat()
            val endX = (center.x + (outerRadius + 3.dp.toPx()) * Math.cos(angle)).toFloat()
            val endY = (center.y + (outerRadius + 3.dp.toPx()) * Math.sin(angle)).toFloat()

            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

@Composable
fun StickProfileIcon(color: Color, modifier: Modifier = Modifier) {
    val shoulderPath = remember { Path() }
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height

        // Head circle (upper centered)
        drawCircle(
            color = color,
            radius = 5.dp.toPx(),
            center = Offset(w / 2f, h * 0.35f),
            style = Stroke(width = 2.dp.toPx())
        )

        // Shoulders (arch starting from lower left to lower right)
        shoulderPath.reset()
        shoulderPath.moveTo(w * 0.15f, h * 0.85f)
        shoulderPath.quadraticTo(
            w / 2f, h * 0.55f, // Control point triggers natural shoulder drop
            w * 0.85f, h * 0.85f
        )
        drawPath(
            path = shoulderPath,
            color = color,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun StickDustbinIcon(color: Color, modifier: Modifier = Modifier) {
    val bodyPath = remember { Path() }
    val handlePath = remember { Path() }
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height

        // Lid horizontal line
        drawLine(
            color = color,
            start = Offset(w * 0.2f, h * 0.25f),
            end = Offset(w * 0.8f, h * 0.25f),
            strokeWidth = 2.dp.toPx()
        )

        // Trashcan bracket
        bodyPath.reset()
        bodyPath.moveTo(w * 0.3f, h * 0.25f)
        bodyPath.lineTo(w * 0.32f, h * 0.85f)
        bodyPath.lineTo(w * 0.68f, h * 0.85f)
        bodyPath.lineTo(w * 0.7f, h * 0.25f)
        drawPath(
            path = bodyPath,
            color = color,
            style = Stroke(width = 2.dp.toPx())
        )

        // Lid handle
        handlePath.reset()
        handlePath.moveTo(w * 0.42f, h * 0.25f)
        handlePath.lineTo(w * 0.42f, h * 0.15f)
        handlePath.lineTo(w * 0.58f, h * 0.15f)
        handlePath.lineTo(w * 0.58f, h * 0.25f)
        drawPath(
            path = handlePath,
            color = color,
            style = Stroke(width = 2.dp.toPx())
        )

        // Vertical lines indicating depth lines inside trashcan
        drawLine(
            color = color,
            start = Offset(w * 0.42f, h * 0.35f),
            end = Offset(w * 0.42f, h * 0.75f),
            strokeWidth = 1.5f.dp.toPx()
        )
        drawLine(
            color = color,
            start = Offset(w * 0.58f, h * 0.35f),
            end = Offset(w * 0.58f, h * 0.75f),
            strokeWidth = 1.5f.dp.toPx()
        )
    }
}

@Composable
fun StickCloseIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val margin = 6.dp.toPx()

        // X lines
        drawLine(
            color = color,
            start = Offset(margin, margin),
            end = Offset(w - margin, h - margin),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = color,
            start = Offset(w - margin, margin),
            end = Offset(margin, h - margin),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
fun StickArrowBackIcon(color: Color, modifier: Modifier = Modifier) {
    val headPath = remember { Path() }
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height

        // Stem
        drawLine(
            color = color,
            start = Offset(w * 0.2f, h / 2f),
            end = Offset(w * 0.8f, h / 2f),
            strokeWidth = 2.dp.toPx()
        )

        // Arrow head leaves
        headPath.reset()
        headPath.moveTo(w * 0.45f, h * 0.25f)
        headPath.lineTo(w * 0.2f, h / 2f)
        headPath.lineTo(w * 0.45f, h * 0.75f)
        drawPath(
            path = headPath,
            color = color,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun StickShareIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height

        // Coordinates of nodes
        val source = Offset(w * 0.25f, h / 2f)
        val topTarget = Offset(w * 0.75f, h * 0.25f)
        val bottomTarget = Offset(w * 0.75f, h * 0.75f)

        // Connection lines
        drawLine(color = color, start = source, end = topTarget, strokeWidth = 2.dp.toPx())
        drawLine(color = color, start = source, end = bottomTarget, strokeWidth = 2.dp.toPx())

        // Node circles
        drawCircle(color = color, radius = 3.dp.toPx(), center = source)
        drawCircle(color = color, radius = 3.dp.toPx(), center = topTarget)
        drawCircle(color = color, radius = 3.dp.toPx(), center = bottomTarget)
    }
}

@Composable
fun StickCopyIcon(color: Color, modifier: Modifier = Modifier) {
    val rearPath = remember { Path() }
    val frontPath = remember { Path() }
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height

        // Rear card
        rearPath.reset()
        rearPath.moveTo(w * 0.15f, h * 0.4f)
        rearPath.lineTo(w * 0.15f, h * 0.15f)
        rearPath.lineTo(w * 0.6f, h * 0.15f)
        rearPath.lineTo(w * 0.6f, h * 0.4f)
        drawPath(path = rearPath, color = color, style = Stroke(width = 1.8f.dp.toPx()))

        // Front card
        frontPath.reset()
        frontPath.moveTo(w * 0.35f, h * 0.35f)
        frontPath.lineTo(w * 0.35f, h * 0.85f)
        frontPath.lineTo(w * 0.85f, h * 0.85f)
        frontPath.lineTo(w * 0.85f, h * 0.35f)
        frontPath.close()
        drawPath(path = frontPath, color = color, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
fun StickSearchWwwIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(36.dp)) {
        val w = size.width
        val h = size.height

        // Circle of magnifying glass
        drawCircle(
            color = color,
            radius = 10.dp.toPx(),
            center = Offset(w * 0.45f, h * 0.4f),
            style = Stroke(width = 2.5f.dp.toPx())
        )

        // Handle of magnifying glass
        drawLine(
            color = color,
            start = Offset(w * 0.6f, h * 0.55f),
            end = Offset(w * 0.82f, h * 0.78f),
            strokeWidth = 3.dp.toPx()
        )
    }
}

@Composable
fun StickSecurityIcon(color: Color, modifier: Modifier = Modifier) {
    val shieldPath = remember { Path() }
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height

        // Draw a minimalist M3 high-security shield path
        shieldPath.reset()
        shieldPath.moveTo(w * 0.2f, h * 0.2f)
        shieldPath.lineTo(w * 0.8f, h * 0.2f)
        shieldPath.quadraticTo(
            w * 0.8f, h * 0.55f,
            w / 2f, h * 0.85f
        )
        shieldPath.quadraticTo(
            w * 0.2f, h * 0.55f,
            w * 0.2f, h * 0.2f
        )
        shieldPath.close()
        drawPath(path = shieldPath, color = color, style = Stroke(width = 2.dp.toPx()))

        // Internal lock/tick indicator
        drawLine(
            color = color,
            start = Offset(w * 0.4f, h * 0.48f),
            end = Offset(w * 0.48f, h * 0.55f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = color,
            start = Offset(w * 0.48f, h * 0.55f),
            end = Offset(w * 0.65f, h * 0.38f),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
fun StickAboutIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height

        // Inner circle information
        drawCircle(
            color = color,
            radius = size.width / 2f - 3.dp.toPx(),
            center = Offset(w / 2f, h / 2f),
            style = Stroke(width = 2.dp.toPx())
        )

        // Stem of letter 'i'
        drawLine(
            color = color,
            start = Offset(w / 2f, h * 0.45f),
            end = Offset(w / 2f, h * 0.75f),
            strokeWidth = 2.2f.dp.toPx()
        )

        // Dot of letter 'i'
        drawCircle(
            color = color,
            radius = 1.8f.dp.toPx(),
            center = Offset(w / 2f, h * 0.32f)
        )
    }
}

@Composable
fun StickHelpIcon(color: Color, modifier: Modifier = Modifier) {
    val qPath = remember { Path() }
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height

        // Question mark path
        qPath.reset()
        qPath.moveTo(w * 0.32f, h * 0.32f)
        qPath.quadraticTo(w * 0.32f, h * 0.18f, w * 0.5f, h * 0.18f)
        qPath.quadraticTo(w * 0.68f, h * 0.18f, w * 0.68f, h * 0.34f)
        qPath.quadraticTo(w * 0.68f, h * 0.45f, w * 0.5f, h * 0.52f)
        qPath.lineTo(w * 0.5f, h * 0.62f)
        drawPath(path = qPath, color = color, style = Stroke(width = 2.dp.toPx()))

        // Dot
        drawCircle(
            color = color,
            radius = 1.8f.dp.toPx(),
            center = Offset(w / 2f, h * 0.78f)
        )
    }
}

@Composable
fun StickCheckerIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height

        // Minimalist magnifying glass highlighting tick mark safety
        drawCircle(
            color = color,
            radius = 6.dp.toPx(),
            center = Offset(w * 0.42f, h * 0.42f),
            style = Stroke(width = 2.dp.toPx())
        )
        drawLine(
            color = color,
            start = Offset(w * 0.55f, h * 0.55f),
            end = Offset(w * 0.8f, h * 0.8f),
            strokeWidth = 2.5f.dp.toPx()
        )

        // Mini checked tick inside lens area
        drawLine(
            color = color,
            start = Offset(w * 0.35f, h * 0.42f),
            end = Offset(w * 0.41f, h * 0.48f),
            strokeWidth = 1.3f.dp.toPx()
        )
        drawLine(
            color = color,
            start = Offset(w * 0.41f, h * 0.48f),
            end = Offset(w * 0.5f, h * 0.36f),
            strokeWidth = 1.3f.dp.toPx()
        )
    }
}

@Composable
fun StickThemeIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)

        // Draw a lightbulb shape utilizing minimal lines
        drawCircle(
            color = color,
            radius = 5.dp.toPx(),
            center = Offset(center.x, center.y - 2.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )

        // Filament line
        drawLine(
            color = color,
            start = Offset(center.x - 2.dp.toPx(), center.y + 4.dp.toPx()),
            end = Offset(center.x + 2.dp.toPx(), center.y + 4.dp.toPx()),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = color,
            start = Offset(center.x - 1.5f.dp.toPx(), center.y + 6.dp.toPx()),
            end = Offset(center.x + 1.5f.dp.toPx(), center.y + 6.dp.toPx()),
            strokeWidth = 1.5f.dp.toPx()
        )

        // Rays
        val rayLength = 3.dp.toPx()
        val numRays = 5
        for (i in 0 until numRays) {
            val angle = -Math.PI + (i * Math.PI / (numRays - 1))
            val sX = (center.x + 7.5f.dp.toPx() * Math.cos(angle)).toFloat()
            val sY = (center.y - 2.dp.toPx() + 7.5f.dp.toPx() * Math.sin(angle)).toFloat()
            val eX = (center.x + (7.5f.dp.toPx() + rayLength) * Math.cos(angle)).toFloat()
            val eY = (center.y - 2.dp.toPx() + (7.5f.dp.toPx() + rayLength) * Math.sin(angle)).toFloat()

            drawLine(color = color, start = Offset(sX, sY), end = Offset(eX, eY), strokeWidth = 1.5f.dp.toPx())
        }
    }
}

@Composable
fun StickBrowserIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(36.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.width / 2f - 2.dp.toPx()
        val innerCircleRadius = radius * 0.43f
        val whiteRingRadius = radius * 0.53f

        // Google Chrome traditional values
        val redColor = Color(0xFFEA4335)
        val greenColor = Color(0xFF34A853)
        val yellowColor = Color(0xFFFBBC05)
        val blueColor = Color(0xFF4285F4)
        val whiteColor = Color.White

        // Outer Pinwheel segments
        drawArc(
            color = redColor,
            startAngle = -30f,
            sweepAngle = 120f,
            useCenter = true
        )
        drawArc(
            color = greenColor,
            startAngle = 90f,
            sweepAngle = 120f,
            useCenter = true
        )
        drawArc(
            color = yellowColor,
            startAngle = 210f,
            sweepAngle = 120f,
            useCenter = true
        )

        // White separating ring
        drawCircle(
            color = whiteColor,
            radius = whiteRingRadius,
            center = center
        )

        // Blue center circle
        drawCircle(
            color = blueColor,
            radius = innerCircleRadius,
            center = center
        )
    }
}
