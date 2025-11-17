package pe.com.zzynan.procardapp.ui.components

import android.graphics.Paint
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import pe.com.zzynan.procardapp.R
import pe.com.zzynan.procardapp.ui.model.WeeklyStepsPoint
import pe.com.zzynan.procardapp.ui.model.WeeklyWeightPoint
import androidx.compose.ui.graphics.nativeCanvas


private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM")

@Composable
fun WeightLineChart(
    points: List<WeeklyWeightPoint>,
    onPointSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.weight_chart_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            LineChart(
                points = points.map { it.weight ?: 0f },
                labels = points.map { it.date.format(dateFormatter) },
                onPointSelected = { index ->
                    points.getOrNull(index)?.date?.let(onPointSelected)
                },
                valueLabel = { value -> String.format("%.2f", value) }
            )
        }
    }
}

@Composable
fun StepsLineChart(
    points: List<WeeklyStepsPoint>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.steps_chart_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            LineChart(
                points = points.map { it.steps.toFloat() },
                labels = points.map { it.date.format(dateFormatter) },
                onPointSelected = {},
                valueLabel = { value -> value.roundToInt().toString() }
            )
        }
    }
}

@Composable
private fun LineChart(
    points: List<Float>,
    labels: List<String>,
    onPointSelected: (Int) -> Unit,
    valueLabel: (Float) -> String,
    modifier: Modifier = Modifier
) {
    val maxValue = (points.maxOrNull() ?: 0f).coerceAtLeast(1f)
    val minValue = points.minOrNull() ?: 0f
    val range = (maxValue - minValue).takeIf { it > 0f } ?: 1f

    // Saca los colores del MaterialTheme ANTES del Canvas
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val textPaint = Paint().apply {
        color = onSurfaceColor.toArgb()
        textSize = 28f
        isAntiAlias = true
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .pointerInput(points, labels) {
                detectTapGestures { offset ->
                    if (points.isNotEmpty()) {
                        val segment =
                            if (points.size > 1) size.width / (points.size - 1) else size.width
                        val index = (offset.x / segment)
                            .roundToInt()
                            .coerceIn(points.indices)
                        onPointSelected(index)
                    }
                }
            }
    ) {
        if (points.isEmpty()) return@Canvas

        val chartHeight = size.height
        val chartWidth = size.width
        val xStep = if (points.size > 1) chartWidth / (points.size - 1) else chartWidth

        val path = Path()
        val positions = points.mapIndexed { index, value ->
            val x = xStep * index
            val normalized = (value - minValue) / range
            val y = chartHeight - (normalized * chartHeight)
            Offset(x, y)
        }

        path.moveTo(positions.first().x, positions.first().y)
        positions.drop(1).forEach { point ->
            path.lineTo(point.x, point.y)
        }

        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )

        positions.forEachIndexed { index, point ->
            drawCircle(
                color = secondaryColor,
                radius = 10f,
                center = point
            )

            drawContext.canvas.nativeCanvas.drawText(
                valueLabel(points[index]),
                point.x,
                point.y - 12f,
                textPaint
            )

            val label = labels.getOrNull(index)
            if (label != null) {
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    point.x,
                    chartHeight,
                    textPaint
                )
            }
        }
    }
}


private fun Color.toArgb(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(),
    (red * 255).toInt(),
    (green * 255).toInt(),
    (blue * 255).toInt()
)
