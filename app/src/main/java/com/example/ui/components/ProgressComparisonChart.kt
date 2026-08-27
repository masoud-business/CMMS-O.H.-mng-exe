package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AreaProgressComparison
import com.example.ui.TimelineProgressPoint
import com.example.ui.UnitProgressComparison
import com.example.ui.theme.*
import kotlin.math.roundToInt

enum class ChartViewType(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    UNIT_BARS("مقایسه واحدها (میله‌ای)", Icons.Default.BarChart),
    S_CURVE("منحنی S-Curve پیشرفت", Icons.Default.ShowChart),
    VARIANCE_SPI("ماتریس انحراف و SPI", Icons.Default.Assessment),
    RADIAL_GAUGES("گیج سرعت‌سنج و راندمان", Icons.Default.Speed),
    WORKFORCE_DISTRIBUTION("توزیع منابع و نفر-ساعت", Icons.Default.Groups)
}

enum class SortMode(val title: String) {
    DEFAULT("پیش‌فرض"),
    MAX_DELAY("بیشترین تأخیر"),
    MAX_PROGRESS("بیشترین پیشرفت"),
    TASK_COUNT("تعداد تسک")
}

@Composable
fun ProgressComparisonDashboardCard(
    unitComparisons: List<UnitProgressComparison>,
    timelinePoints: List<TimelineProgressPoint>,
    overallPlannedProgress: Float,
    overallActualProgress: Float,
    modifier: Modifier = Modifier,
    onUnitClick: ((UnitProgressComparison) -> Unit)? = null
) {
    var selectedView by remember { mutableStateOf(ChartViewType.UNIT_BARS) }
    var selectedUnit by remember { mutableStateOf<UnitProgressComparison?>(null) }
    var sortMode by remember { mutableStateOf(SortMode.DEFAULT) }

    val sortedUnits = remember(unitComparisons, sortMode) {
        when (sortMode) {
            SortMode.DEFAULT -> unitComparisons
            SortMode.MAX_DELAY -> unitComparisons.sortedBy { it.variance } // Lowest variance = highest delay
            SortMode.MAX_PROGRESS -> unitComparisons.sortedByDescending { it.actualProgress }
            SortMode.TASK_COUNT -> unitComparisons.sortedByDescending { it.totalTasks }
        }
    }

    val overallVariance = overallActualProgress - overallPlannedProgress
    val overallSpi = if (overallPlannedProgress > 0f) overallActualProgress / overallPlannedProgress else 1.0f

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        shadowElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .animateContentSize()
        ) {
            // 1. Header with Title and Overall EVM Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = IndustrialSteelBlue.copy(alpha = 0.12f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = IndustrialSteelBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "تحلیل پیشرفت: برنامه‌ای در برابر واقعی",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Planned vs. Actual Progress Dashboard across Units",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Global Variance Badge
                VarianceBadge(
                    variance = overallVariance,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. High-Level Summary Metric Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricMiniCard(
                    title = "پیشرفت برنامه‌ای (PV)",
                    value = "${String.format("%.1f", overallPlannedProgress)}%",
                    color = IndustrialAmber,
                    icon = Icons.Outlined.EventNote,
                    modifier = Modifier.weight(1f)
                )
                MetricMiniCard(
                    title = "پیشرفت واقعی (EV)",
                    value = "${String.format("%.1f", overallActualProgress)}%",
                    color = IndustrialEmerald,
                    icon = Icons.Outlined.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
                MetricMiniCard(
                    title = "شاخص زمانبندی (SPI)",
                    value = String.format("%.2f", overallSpi),
                    color = if (overallSpi >= 1.0f) IndustrialEmerald else if (overallSpi >= 0.85f) IndustrialAmber else MaterialTheme.colorScheme.error,
                    icon = Icons.Outlined.Speed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. View Type Selector Tabs (Bar Chart / S-Curve / Variance Matrix)
            SingleChoiceSegmentedRow(
                selectedView = selectedView,
                onViewSelected = { selectedView = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Interactive Chart Views
            when (selectedView) {
                ChartViewType.UNIT_BARS -> {
                    // Sorting and Legend Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Chart Legend
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ChartLegendItem(label = "پیشرفت واقعی (Actual)", color = IndustrialEmerald)
                            Spacer(modifier = Modifier.width(14.dp))
                            ChartLegendItem(label = "برنامه‌ای مصوب (Planned)", color = IndustrialAmber)
                        }

                        // Sorting Filter Menu
                        SortFilterMenu(
                            currentSort = sortMode,
                            onSortSelected = { sortMode = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Unit Comparison Bar Chart
                    UnitBarComparisonChart(
                        units = sortedUnits,
                        selectedUnit = selectedUnit,
                        onSelectUnit = { unit ->
                            selectedUnit = if (selectedUnit?.unitName == unit.unitName) null else unit
                            onUnitClick?.invoke(unit)
                        }
                    )
                }

                ChartViewType.S_CURVE -> {
                    // S-Curve Cumulative Progress Chart
                    SCurveProgressChart(
                        timelinePoints = timelinePoints,
                        currentDayNumber = 5
                    )
                }

                ChartViewType.VARIANCE_SPI -> {
                    // Variance and SPI Performance Table / Matrix
                    VariancePerformanceMatrix(
                        units = sortedUnits,
                        selectedUnit = selectedUnit,
                        onSelectUnit = { unit ->
                            selectedUnit = if (selectedUnit?.unitName == unit.unitName) null else unit
                        }
                    )
                }

                ChartViewType.RADIAL_GAUGES -> {
                    // Creative Radial Speedometer and Concentric Progress Gauges
                    RadialGaugesAndSpeedometerChart(
                        units = sortedUnits,
                        overallSpi = overallSpi,
                        overallActualProgress = overallActualProgress,
                        overallPlannedProgress = overallPlannedProgress
                    )
                }

                ChartViewType.WORKFORCE_DISTRIBUTION -> {
                    // Resource & Shift Man-Hours Allocation Chart
                    WorkforceAndShiftAnalyticsChart(
                        units = sortedUnits
                    )
                }
            }

            // 5. Selected Unit Drill-down Inspector Card
            AnimatedVisibility(visible = selectedUnit != null) {
                selectedUnit?.let { unit ->
                    Spacer(modifier = Modifier.height(16.dp))
                    UnitDrilldownCard(
                        unit = unit,
                        onClose = { selectedUnit = null }
                    )
                }
            }
        }
    }
}

// ====================================================================
// A. DUAL-BAR CHART COMPONENT (NATIVE COMPOSE RECHARTS-LIKE BAR CHART)
// ====================================================================

@Composable
fun UnitBarComparisonChart(
    units: List<UnitProgressComparison>,
    selectedUnit: UnitProgressComparison?,
    onSelectUnit: (UnitProgressComparison) -> Unit,
    modifier: Modifier = Modifier
) {
    if (units.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("داده‌ای برای نمایش نمودار موجود نیست", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        units.forEach { unit ->
            val isSelected = selectedUnit?.unitName == unit.unitName

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) IndustrialNavy.copy(alpha = 0.05f) else Color.Transparent,
                border = if (isSelected) BorderStroke(1.5.dp, IndustrialSteelBlue) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelectUnit(unit) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Unit Header: Name, SPI, Variance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "واحد ${unit.unitName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "${unit.totalTasks} فعالیت",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SPI: ${String.format("%.2f", unit.spi)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (unit.spi >= 1.0f) IndustrialEmerald else if (unit.spi >= 0.85f) IndustrialAmber else MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            VarianceBadge(variance = unit.variance, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Comparison Bars
                    // 1. Actual Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "واقعی",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = IndustrialEmerald,
                            modifier = Modifier.width(42.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        ) {
                            val animatedActualProgress by animateFloatAsState(
                                targetValue = (unit.actualProgress / 100f).coerceIn(0f, 1f),
                                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                                label = "actualProgress"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animatedActualProgress)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                IndustrialEmerald.copy(alpha = 0.8f),
                                                IndustrialEmerald
                                            )
                                        )
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${String.format("%.1f", unit.actualProgress)}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndustrialEmerald,
                            modifier = Modifier.width(42.dp),
                            textAlign = TextAlign.End
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 2. Planned Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "برنامه",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = IndustrialAmberDark,
                            modifier = Modifier.width(42.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        ) {
                            val animatedPlannedProgress by animateFloatAsState(
                                targetValue = (unit.plannedProgress / 100f).coerceIn(0f, 1f),
                                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                                label = "plannedProgress"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animatedPlannedProgress)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                IndustrialAmber.copy(alpha = 0.7f),
                                                IndustrialAmber
                                            )
                                        )
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${String.format("%.1f", unit.plannedProgress)}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IndustrialAmberDark,
                            modifier = Modifier.width(42.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

// ====================================================================
// B. S-CURVE CUMULATIVE PROGRESS CHART (CANVAS DRAWN S-CURVE)
// ====================================================================

@Composable
fun SCurveProgressChart(
    timelinePoints: List<TimelineProgressPoint>,
    currentDayNumber: Int,
    modifier: Modifier = Modifier
) {
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Legend & Explanatory Label
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ChartLegendItem(label = "منحنی واقعی (Actual EV)", color = IndustrialEmerald)
                Spacer(modifier = Modifier.width(12.dp))
                ChartLegendItem(label = "منحنی مصوب (Baseline PV)", color = IndustrialAmber)
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = IndustrialSteelBlue.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "روز ۵ از ۱۵ اورهال",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = IndustrialSteelBlue,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // Canvas S-Curve Chart
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 36.dp, end = 20.dp, top = 24.dp, bottom = 32.dp)
                        .pointerInput(timelinePoints) {
                            detectTapGestures { offset ->
                                val spacing = size.width / (timelinePoints.size - 1).coerceAtLeast(1)
                                val index = ((offset.x + spacing / 2) / spacing).toInt().coerceIn(0, timelinePoints.size - 1)
                                selectedPointIndex = if (selectedPointIndex == index) null else index
                            }
                        }
                ) {
                    drawSCurveChart(
                        points = timelinePoints,
                        selectedPointIndex = selectedPointIndex
                    )
                }

                // Y-Axis Labels (0%, 25%, 50%, 75%, 100%)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 6.dp, top = 16.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("100%", "75%", "50%", "25%", "0%").forEach { label ->
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Interactive Point Tooltip Card
        val activePoint = selectedPointIndex?.let { timelinePoints.getOrNull(it) } ?: timelinePoints.firstOrNull { it.dayNumber == currentDayNumber }
        activePoint?.let { pt ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, IndustrialSteelBlue.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = pt.dayLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        pt.milestoneTitle?.let { milestone ->
                            Text(
                                text = "🚩 $milestone",
                                fontSize = 11.sp,
                                color = IndustrialAmberDark,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("برنامه", fontSize = 10.sp, color = IndustrialAmberDark)
                            Text("${String.format("%.1f", pt.plannedProgress)}%", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = IndustrialAmberDark)
                        }
                        if (pt.actualProgress != null) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("واقعی", fontSize = 10.sp, color = IndustrialEmerald)
                                Text("${String.format("%.1f", pt.actualProgress)}%", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = IndustrialEmerald)
                            }
                            val diff = pt.actualProgress - pt.plannedProgress
                            Column(horizontalAlignment = Alignment.End) {
                                Text("انحراف", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = if (diff >= 0) "+${String.format("%.1f", diff)}%" else "${String.format("%.1f", diff)}%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (diff >= 0) IndustrialEmerald else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawSCurveChart(
    points: List<TimelineProgressPoint>,
    selectedPointIndex: Int?
) {
    if (points.size < 2) return

    val w = size.width
    val h = size.height
    val numPoints = points.size
    val stepX = w / (numPoints - 1)

    // Draw horizontal grid lines
    val gridCount = 4
    for (i in 0..gridCount) {
        val y = h * (i / gridCount.toFloat())
        drawLine(
            color = Color.LightGray.copy(alpha = 0.35f),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
        )
    }

    // 1. Draw Planned S-Curve (Amber)
    val plannedPath = Path()
    val plannedAreaPath = Path()

    points.forEachIndexed { index, pt ->
        val x = index * stepX
        val y = h * (1f - (pt.plannedProgress / 100f).coerceIn(0f, 1f))
        if (index == 0) {
            plannedPath.moveTo(x, y)
            plannedAreaPath.moveTo(x, h)
            plannedAreaPath.lineTo(x, y)
        } else {
            val prevX = (index - 1) * stepX
            val prevY = h * (1f - (points[index - 1].plannedProgress / 100f).coerceIn(0f, 1f))
            val cx1 = (prevX + x) / 2
            plannedPath.cubicTo(cx1, prevY, cx1, y, x, y)
            plannedAreaPath.cubicTo(cx1, prevY, cx1, y, x, y)
        }
    }
    plannedAreaPath.lineTo(w, h)
    plannedAreaPath.close()

    // Fill area under planned curve
    drawPath(
        path = plannedAreaPath,
        brush = Brush.verticalGradient(
            colors = listOf(IndustrialAmber.copy(alpha = 0.15f), Color.Transparent)
        )
    )

    // Stroke planned line
    drawPath(
        path = plannedPath,
        color = IndustrialAmber,
        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
    )

    // 2. Draw Actual Progress Curve (Emerald)
    val actualPoints = points.filter { it.actualProgress != null }
    if (actualPoints.size >= 2) {
        val actualPath = Path()
        val actualAreaPath = Path()

        actualPoints.forEachIndexed { index, pt ->
            val x = index * stepX
            val y = h * (1f - (pt.actualProgress!! / 100f).coerceIn(0f, 1f))
            if (index == 0) {
                actualPath.moveTo(x, y)
                actualAreaPath.moveTo(x, h)
                actualAreaPath.lineTo(x, y)
            } else {
                val prevX = (index - 1) * stepX
                val prevY = h * (1f - (actualPoints[index - 1].actualProgress!! / 100f).coerceIn(0f, 1f))
                val cx1 = (prevX + x) / 2
                actualPath.cubicTo(cx1, prevY, cx1, y, x, y)
                actualAreaPath.cubicTo(cx1, prevY, cx1, y, x, y)
            }
        }
        val lastActualX = (actualPoints.size - 1) * stepX
        actualAreaPath.lineTo(lastActualX, h)
        actualAreaPath.close()

        drawPath(
            path = actualAreaPath,
            brush = Brush.verticalGradient(
                colors = listOf(IndustrialEmerald.copy(alpha = 0.3f), Color.Transparent)
            )
        )

        drawPath(
            path = actualPath,
            color = IndustrialEmerald,
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // 3. Draw Points & Milestones
    points.forEachIndexed { index, pt ->
        val x = index * stepX
        val plannedY = h * (1f - (pt.plannedProgress / 100f).coerceIn(0f, 1f))

        // Planned point
        drawCircle(
            color = IndustrialAmber,
            radius = 3.5.dp.toPx(),
            center = Offset(x, plannedY)
        )

        // Actual point
        if (pt.actualProgress != null) {
            val actualY = h * (1f - (pt.actualProgress / 100f).coerceIn(0f, 1f))
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = Offset(x, actualY)
            )
            drawCircle(
                color = IndustrialEmerald,
                radius = 3.5.dp.toPx(),
                center = Offset(x, actualY)
            )
        }

        // Milestone Flag
        if (pt.isMilestone) {
            drawCircle(
                color = IndustrialCrimson,
                radius = 5.5.dp.toPx(),
                center = Offset(x, plannedY),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // Highlight selected point
        if (selectedPointIndex == index) {
            drawLine(
                color = IndustrialSteelBlue,
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
            )
        }
    }
}

// ====================================================================
// C. VARIANCE AND PERFORMANCE MATRIX (SPI & EVM ANALYSIS)
// ====================================================================

@Composable
fun VariancePerformanceMatrix(
    units: List<UnitProgressComparison>,
    selectedUnit: UnitProgressComparison?,
    onSelectUnit: (UnitProgressComparison) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Table Header
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("واحد اجرایی", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.3f))
                Text("واقعی / برنامه", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.3f), textAlign = TextAlign.Center)
                Text("انحراف (%)", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("شاخص SPI", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
            }
        }

        // Table Rows
        units.forEach { unit ->
            val isSelected = selectedUnit?.unitName == unit.unitName

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) IndustrialSteelBlue.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (isSelected) IndustrialSteelBlue else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectUnit(unit) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.3f)) {
                        Text(
                            text = unit.unitName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${unit.completedTasks}/${unit.totalTasks} تسک",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1.3f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${String.format("%.1f", unit.actualProgress)}% / ${String.format("%.1f", unit.plannedProgress)}%",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        VarianceBadge(variance = unit.variance, fontSize = 10.sp)
                    }

                    Column(
                        modifier = Modifier.weight(0.9f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = String.format("%.2f", unit.spi),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (unit.spi >= 1.0f) IndustrialEmerald else if (unit.spi >= 0.85f) IndustrialAmber else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = if (unit.spi >= 1.0f) "جلوتر" else if (unit.spi >= 0.85f) "تأخیر جزئی" else "بحرانی",
                            fontSize = 9.sp,
                            color = if (unit.spi >= 1.0f) IndustrialEmerald else if (unit.spi >= 0.85f) IndustrialAmber else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

// ====================================================================
// D. UNIT SUB-AREA DRILLDOWN INSPECTOR CARD
// ====================================================================

@Composable
fun UnitDrilldownCard(
    unit: UnitProgressComparison,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, IndustrialSteelBlue.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = IndustrialSteelBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "جزئیات و تفکیک بخش‌های واحد ${unit.unitName}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "بستن", modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Task State Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusMiniBox(label = "تکمیل", count = unit.completedTasks, color = IndustrialEmerald, modifier = Modifier.weight(1f))
                StatusMiniBox(label = "در حال اجرا", count = unit.inProgressTasks, color = IndustrialSteelBlue, modifier = Modifier.weight(1f))
                StatusMiniBox(label = "دارای مانع", count = unit.blockedTasks, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                StatusMiniBox(label = "در انتظار", count = unit.pendingTasks, color = Color.Gray, modifier = Modifier.weight(1f))
            }

            // Sub-Area Progress Breakdown
            if (unit.areas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "پیشرفت به تفکیک زون و تجهیزات (Area Breakdown)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                unit.areas.forEach { area ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = area.areaName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${String.format("%.1f", area.actualProgress)}% (برنامه: ${String.format("%.1f", area.plannedProgress)}%)",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { (area.actualProgress / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (area.actualProgress >= area.plannedProgress) IndustrialEmerald else IndustrialAmber,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ====================================================================
// HELPER SUB-COMPONENTS
// ====================================================================

@Composable
fun MetricMiniCard(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.09f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = color
            )
            Text(
                text = title,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun VarianceBadge(
    variance: Float,
    fontSize: androidx.compose.ui.unit.TextUnit = 11.sp,
    modifier: Modifier = Modifier
) {
    val isPositive = variance >= 0f
    val isNeutral = variance == 0f
    val bgColor = if (isNeutral) Color.LightGray.copy(alpha = 0.2f) else if (isPositive) StatusCompletedBg else StatusBlockedBg
    val textColor = if (isNeutral) Color.DarkGray else if (isPositive) StatusCompleted else StatusBlocked

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isPositive) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = if (isPositive) "+${String.format("%.1f", variance)}%" else "${String.format("%.1f", variance)}%",
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ChartLegendItem(
    label: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SingleChoiceSegmentedRow(
    selectedView: ChartViewType,
    onViewSelected: (ChartViewType) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ChartViewType.values().forEach { viewType ->
                val isSelected = selectedView == viewType
                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onViewSelected(viewType) }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = viewType.icon,
                            contentDescription = null,
                            tint = if (isSelected) IndustrialSteelBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = viewType.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) IndustrialSteelBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SortFilterMenu(
    currentSort: SortMode,
    onSortSelected: (SortMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = currentSort.title,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SortMode.values().forEach { mode ->
                DropdownMenuItem(
                    text = { Text(mode.title, fontSize = 12.sp) },
                    onClick = {
                        onSortSelected(mode)
                        expanded = false
                    },
                    leadingIcon = {
                        if (currentSort == mode) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = IndustrialSteelBlue, modifier = Modifier.size(16.dp))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun StatusMiniBox(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count.toString(), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
            Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ====================================================================
// D. RADIAL GAUGES & SPEEDOMETER CHART COMPONENT
// ====================================================================

@Composable
fun RadialGaugesAndSpeedometerChart(
    units: List<UnitProgressComparison>,
    overallSpi: Float,
    overallActualProgress: Float,
    overallPlannedProgress: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Semi-Circle SPI Speedometer
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "سرعت‌سنج پیشرفت و راندمان زمانی (SPI Speedometer)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "ارزیابی نرخ شتاب اجرایی پروژه بر اساس ارزش کسب‌شده",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Canvas Semi-Circle Gauge
                Box(
                    modifier = Modifier
                        .size(width = 220.dp, height = 120.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 18.dp.toPx()
                        val arcSize = Size(size.width - strokeWidth, (size.height * 2) - strokeWidth)
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                        // 1. Background Track Arc (180 degrees from 180 to 360)
                        // Red zone (0 to 0.85 SPI) -> 0 to 60 deg
                        drawArc(
                            color = Color(0xFFEF5350).copy(alpha = 0.3f),
                            startAngle = 180f,
                            sweepAngle = 60f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        // Yellow zone (0.85 to 1.0 SPI) -> 60 to 120 deg
                        drawArc(
                            color = Color(0xFFFFA726).copy(alpha = 0.3f),
                            startAngle = 240f,
                            sweepAngle = 60f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth)
                        )
                        // Green zone (1.0 to 1.3+ SPI) -> 120 to 180 deg
                        drawArc(
                            color = Color(0xFF2E7D32).copy(alpha = 0.3f),
                            startAngle = 300f,
                            sweepAngle = 60f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // 2. Active Indicator Arc
                        val normalizedSpi = (overallSpi / 1.5f).coerceIn(0f, 1f)
                        val sweepAngle = normalizedSpi * 180f
                        val activeColor = when {
                            overallSpi >= 1.0f -> Color(0xFF2E7D32)
                            overallSpi >= 0.85f -> Color(0xFFFFA726)
                            else -> Color(0xFFEF5350)
                        }

                        drawArc(
                            color = activeColor,
                            startAngle = 180f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = String.format("%.2f", overallSpi),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = when {
                                overallSpi >= 1.0f -> IndustrialEmerald
                                overallSpi >= 0.85f -> IndustrialAmberDark
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                        Text(
                            text = when {
                                overallSpi >= 1.0f -> "وضعیت مطلوب و جلوتر از برنامه"
                                overallSpi >= 0.85f -> "هشدار: نیازمند تسریع در شیفت‌ها"
                                else -> "تأخیر بحرانی: نیازمند جبران فوری"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. Concentric Multi-Ring Discipline Progress
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "گیج‌های حلقوی پیشرفت واحدهای اجرایی",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                units.take(5).forEach { unit ->
                    val color = when (unit.unitName) {
                        "مکانیک" -> Color(0xFF1E88E5)
                        "برق" -> Color(0xFFF57C00)
                        "ابزاردقیق", "ابزاردقیق و اتوماسیون" -> Color(0xFF7B1FA2)
                        "نسوز" -> Color(0xFFD32F2F)
                        "انرژی و سیالات" -> Color(0xFF00897B)
                        else -> Color(0xFF546E7A)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(color, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "واحد ${unit.unitName}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Circular Mini Indicator
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { unit.actualProgress / 100f },
                                    modifier = Modifier.size(28.dp),
                                    color = color,
                                    trackColor = color.copy(alpha = 0.2f),
                                    strokeWidth = 3.dp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${String.format("%.1f", unit.actualProgress)}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = color,
                                modifier = Modifier.width(48.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }
    }
}

// ====================================================================
// E. WORKFORCE & SHIFT ANALYTICS CHART COMPONENT
// ====================================================================

@Composable
fun WorkforceAndShiftAnalyticsChart(
    units: List<UnitProgressComparison>,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "توزیع نیروی انسانی و شیفت‌های کاری",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "تفکیک نفرات حاضر در شیفت روز و شب به تفکیک واحدها",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = IndustrialSteelBlue.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "مجموع: ${units.sumOf { it.totalTasks * 3 }} نفر-شیفت",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndustrialSteelBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Shift Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartLegendItem(label = "شیفت روز (Day Shift)", color = Color(0xFF1E88E5))
                Spacer(modifier = Modifier.width(16.dp))
                ChartLegendItem(label = "شیفت شب (Night Shift)", color = Color(0xFF5E35B1))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stacked Unit Allocation Bars
            units.forEach { unit ->
                val dayManpower = (unit.totalTasks * 2.2).roundToInt()
                val nightManpower = (unit.totalTasks * 1.3).roundToInt()
                val totalUnit = dayManpower + nightManpower

                Column(modifier = Modifier.padding(vertical = 5.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "واحد ${unit.unitName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$totalUnit نفر (روز: $dayManpower / شب: $nightManpower)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Stacked Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        val dayWeight = if (totalUnit > 0) dayManpower.toFloat() / totalUnit else 0.5f
                        val nightWeight = if (totalUnit > 0) nightManpower.toFloat() / totalUnit else 0.5f

                        Box(
                            modifier = Modifier
                                .weight(dayWeight)
                                .fillMaxHeight()
                                .background(Color(0xFF1E88E5))
                        )
                        Box(
                            modifier = Modifier
                                .weight(nightWeight)
                                .fillMaxHeight()
                                .background(Color(0xFF5E35B1))
                        )
                    }
                }
            }
        }
    }
}
