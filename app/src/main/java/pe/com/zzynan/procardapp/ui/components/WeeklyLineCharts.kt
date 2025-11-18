package pe.com.zzynan.procardapp.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import pe.com.zzynan.procardapp.R
import pe.com.zzynan.procardapp.ui.model.WeeklyCaloriesPointUiModel
import pe.com.zzynan.procardapp.ui.model.WeeklyStepsPoint
import pe.com.zzynan.procardapp.ui.model.WeeklyWeightPoint

private val chartHeight = 110.dp
private const val H_PADDING = 28f
private const val V_PADDING = 24f

@Composable
fun WeightLineChart(
    points: List<WeeklyWeightPoint>,
    onPointSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayPoints = if (points.isNotEmpty()) {
        points
    } else {
        val startDate = LocalDate.now().minusDays(6)
        (0 until 7).map { offset ->
            WeeklyWeightPoint(date = startDate.plusDays(offset.toLong()), weight = null)
        }
    }
    val emptyText = stringResource(id = R.string.chart_empty_state)
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
            CompactLineChart(
                values = displayPoints.map { it.weight },
                dates = displayPoints.map { it.date },
                onPointSelected = { index -> displayPoints.getOrNull(index)?.date?.let(onPointSelected) },
                valueFormatter = { value -> String.format("%.2f", value) },
                emptyText = emptyText
            )
        }
    }
}

@Composable
fun StepsLineChart(
    points: List<WeeklyStepsPoint>,
    modifier: Modifier = Modifier
) {
    val displayPoints = if (points.isNotEmpty()) {
        points
    } else {
        val startDate = LocalDate.now().minusDays(6)
        (0 until 7).map { offset ->
            WeeklyStepsPoint(date = startDate.plusDays(offset.toLong()), steps = 0)
        }
    }
    val emptyText = stringResource(id = R.string.chart_empty_state)
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
            CompactLineChart(
                values = displayPoints.map { it.steps.toFloat() },
                dates = displayPoints.map { it.date },
                onPointSelected = null,
                valueFormatter = { value -> value.toInt().toString() },
                emptyText = emptyText
            )
        }
    }
}

@Composable
fun WeeklyCaloriesChart(
    points: List<WeeklyCaloriesPointUiModel>,
    modifier: Modifier = Modifier
) {
    val displayPoints = if (points.isNotEmpty()) {
        points
    } else {
        val startDate = LocalDate.now().minusDays(6)
        (0 until 7).map { offset ->
            WeeklyCaloriesPointUiModel(
                dayLabel = dayAbbreviation(startDate.plusDays(offset.toLong())),
                date = startDate.plusDays(offset.toLong()),
                calories = 0f
            )
        }
    }
    val emptyText = stringResource(id = R.string.chart_empty_state)
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
                text = stringResource(id = R.string.weekly_calories_chart_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            CompactLineChart(
                values = displayPoints.map { it.calories },
                dates = displayPoints.map { it.date },
                onPointSelected = null,
                valueFormatter = { value -> value.toInt().toString() },
                emptyText = emptyText
            )
        }
    }
}

@Composable
private fun CompactLineChart(
    values: List<Float?>,
    dates: List<LocalDate>,
    onPointSelected: ((Int) -> Unit)?,
    valueFormatter: (Float) -> String,
    emptyText: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val onSurfaceColor = colors.onSurface
    val primaryColor = colors.primary
    val secondaryColor = colors.secondary

    val textPaint = Paint().apply {
        color = onSurfaceColor.toArgb()
        textSize = 26f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    val safeDates = dates.ifEmpty {
        val today = LocalDate.now()
        (0 until values.size.coerceAtLeast(7)).map { offset ->
            today.minusDays((values.size.coerceAtLeast(7) - 1 - offset).toLong())
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
            .pointerInput(values, dates, onPointSelected) {
                if (onPointSelected == null || values.isEmpty()) return@pointerInput
                detectTapGestures { offset ->
                    val effectiveWidth = size.width - (H_PADDING * 2)
                    val step = if (values.size > 1) effectiveWidth / (values.size - 1) else effectiveWidth
                    val clampedX = (offset.x - H_PADDING).coerceIn(0f, effectiveWidth)
                    val index = if (step == 0f) 0 else (clampedX / step).toInt().coerceIn(values.indices)
                    onPointSelected(index)
                }
            }
    ) {
        if (values.isEmpty()) {
            drawXAxisLabels(textPaint, safeDates, values.size)
            drawEmptyMessage(textPaint, emptyText)
            return@Canvas
        }

        val availableValues = values.filterNotNull()
        if (availableValues.isEmpty()) {
            drawXAxisLabels(textPaint, safeDates, values.size)
            drawEmptyMessage(textPaint, emptyText)
            return@Canvas
        }

        val chartWidth = size.width - (H_PADDING * 2)
        val chartHeightPx = size.height - (V_PADDING * 2)

        val minValue = availableValues.minOrNull() ?: 0f
        val maxValue = availableValues.maxOrNull() ?: 0f
        val baseRange = (maxValue - minValue).takeIf { it > 0f } ?: maxValue.coerceAtLeast(1f) * 0.25f
        val yMin = minValue - baseRange * 0.2f
        val yMax = maxValue + baseRange * 0.2f
        val range = (yMax - yMin).coerceAtLeast(0.1f)

        val offsets = values.mapIndexed { index, value ->
            val x = H_PADDING + if (values.size > 1) chartWidth / (values.size - 1) * index else chartWidth / 2
            value?.let {
                val normalized = (it - yMin) / range
                val y = size.height - V_PADDING - (normalized * chartHeightPx)
                index to Offset(x, y)
            }
        }

        val paths = mutableListOf<Path>()
        var currentPath: Path? = null
        offsets.forEach { pair ->
            if (pair != null) {
                val (_, point) = pair
                if (currentPath == null) {
                    currentPath = Path().apply { moveTo(point.x, point.y) }
                } else {
                    currentPath?.lineTo(point.x, point.y)
                }
            } else {
                currentPath?.let { paths += it }
                currentPath = null
            }
        }
        currentPath?.let { paths += it }

        paths.forEach { path ->
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )
        }

        offsets.forEachIndexed { index, pair ->
            val x = H_PADDING + if (values.size > 1) chartWidth / (values.size - 1) * index else chartWidth / 2
            drawContext.canvas.nativeCanvas.drawText(
                dayAbbreviation(safeDates.getOrNull(index)),
                x,
                size.height - (V_PADDING / 2),
                textPaint
            )

            pair?.let { (_, point) ->
                drawCircle(
                    color = secondaryColor,
                    radius = 9f,
                    center = point
                )
                drawContext.canvas.nativeCanvas.drawText(
                    valueFormatter(values[index] ?: 0f),
                    point.x,
                    point.y - 14f,
                    textPaint
                )
            }
        }
    }
}

private fun dayAbbreviation(date: LocalDate?): String = when (date?.dayOfWeek) {
    DayOfWeek.MONDAY -> "Lun"
    DayOfWeek.TUESDAY -> "Mar"
    DayOfWeek.WEDNESDAY -> "Mie"
    DayOfWeek.THURSDAY -> "Jue"
    DayOfWeek.FRIDAY -> "Vie"
    DayOfWeek.SATURDAY -> "Sab"
    DayOfWeek.SUNDAY -> "Dom"
    else -> ""
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawXAxisLabels(
    textPaint: Paint,
    dates: List<LocalDate>,
    valueCount: Int
) {
    val count = valueCount.coerceAtLeast(dates.size).coerceAtLeast(1)
    val chartWidth = size.width - (H_PADDING * 2)
    repeat(count) { index ->
        val x = H_PADDING + if (count > 1) chartWidth / (count - 1) * index else chartWidth / 2
        drawContext.canvas.nativeCanvas.drawText(
            dayAbbreviation(dates.getOrNull(index)),
            x,
            size.height - (V_PADDING / 2),
            textPaint
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEmptyMessage(
    textPaint: Paint,
    emptyText: String
) {
    drawContext.canvas.nativeCanvas.drawText(
        emptyText,
        size.width / 2,
        size.height / 2,
        textPaint
    )
}

private fun Color.toArgb(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(),
    (red * 255).toInt(),
    (green * 255).toInt(),
    (blue * 255).toInt()
)
