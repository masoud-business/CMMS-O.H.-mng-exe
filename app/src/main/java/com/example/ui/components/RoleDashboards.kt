package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AppTab
import com.example.data.entity.*
import com.example.ui.*
import com.example.ui.theme.*

/**
 * Conditional Navigation Tab Configuration tailored per User Role
 */
data class RoleTabConfig(
    val tab: AppTab,
    val title: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

fun getNavigationTabsForRole(
    user: UserEntity?,
    kpis: DashboardKpis,
    pendingSyncCount: Int = 0,
    blockedTaskCount: Int = 0
): List<RoleTabConfig> {
    return when (user?.role?.lowercase()) {
        "supervisor" -> listOf(
            RoleTabConfig(AppTab.DASHBOARD, "میز کارگاه", Icons.Default.Engineering, badgeCount = blockedTaskCount),
            RoleTabConfig(AppTab.WBS, "وظایف شیفت", Icons.Default.Checklist),
            RoleTabConfig(AppTab.MSP_SYNC, "ثبت کارکرد", Icons.Default.AssignmentTurnedIn),
            RoleTabConfig(AppTab.PROCUREMENT, "قطعات کارگاه", Icons.Default.Build),
            RoleTabConfig(AppTab.SESSIONS, "مصوبات شیفت", Icons.Default.Notes)
        )
        "planner" -> listOf(
            RoleTabConfig(AppTab.DASHBOARD, "کنترل پروژه", Icons.Default.Timeline),
            RoleTabConfig(AppTab.WBS, "مدیریت WBS", Icons.Default.AccountTree),
            RoleTabConfig(AppTab.MSP_SYNC, "تسویه MSP", Icons.Default.SyncAlt, badgeCount = pendingSyncCount),
            RoleTabConfig(AppTab.SESSIONS, "صورتجلسات", Icons.Default.Groups),
            RoleTabConfig(AppTab.PROCUREMENT, "پایش تامین", Icons.Default.ShoppingCart)
        )
        "unit_head" -> listOf(
            RoleTabConfig(AppTab.DASHBOARD, "داشبورد واحد", Icons.Default.Analytics),
            RoleTabConfig(AppTab.WBS, "کارهای واحد", Icons.Default.AccountTree),
            RoleTabConfig(AppTab.MSP_SYNC, "تاییدیه شیفت", Icons.Default.FactCheck),
            RoleTabConfig(AppTab.SESSIONS, "جلسات واحدها", Icons.Default.Groups),
            RoleTabConfig(AppTab.PROCUREMENT, "درخواست قطعه", Icons.Default.ShoppingCart)
        )
        else -> listOf(
            // Admin / Project Manager (مدیر ارشد و مدیر پروژه)
            RoleTabConfig(AppTab.DASHBOARD, "داشبورد کلان", Icons.Default.Dashboard),
            RoleTabConfig(AppTab.WBS, "ساختار WBS", Icons.Default.AccountTree),
            RoleTabConfig(AppTab.MSP_SYNC, "کنترل MSP", Icons.Default.SyncAlt),
            RoleTabConfig(AppTab.SESSIONS, "جلسات و تصمیمات", Icons.Default.Groups),
            RoleTabConfig(AppTab.PROCUREMENT, "تدارکات و خرید", Icons.Default.ShoppingCart),
            RoleTabConfig(AppTab.AUDIT, "ممیزی سیستم", Icons.Default.Assessment)
        )
    }
}

// ====================================================================
// 1. SHOP FLOOR SUPERVISOR DASHBOARD (داشبورد سرپرست کارگاه و شیفت)
// ====================================================================

@Composable
fun ShopFloorSupervisorDashboard(
    currentUser: UserEntity,
    kpis: DashboardKpis,
    unitAnalytics: UnitAnalytics,
    rawItems: List<OversightItemEntity>,
    oversight: OversightEntity?,
    onNavigateToWbs: () -> Unit,
    onNavigateToFastUpdate: () -> Unit,
    onNavigateToProcurement: () -> Unit,
    onItemClickForUpdate: (OversightItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter tasks specific to this supervisor or their unit
    val userUnit = currentUser.unit ?: "مکانیک"
    val unitTasks = remember(rawItems, userUnit, currentUser.name) {
        rawItems.filter { item ->
            item.executiveUnit.contains(userUnit) || item.title.contains(userUnit)
        }
    }

    val inProgressTasks = unitTasks.filter { it.progressPercentage in 1..99 }
    val blockedTasks = unitTasks.filter { it.issues.isNotBlank() }
    val completedTasks = unitTasks.filter { it.progressPercentage >= 100 }
    val notStartedTasks = unitTasks.filter { it.progressPercentage == 0 }

    var showQuickObstacleDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Supervisor Identity & Shift Header
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = IndustrialAmberDark,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Engineering,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Black.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "میز سرپرستی کارگاه و شیفت اجرایی",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = currentUser.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "ناظر و سرپرست واحد $userUnit • مجتمع فولاد غدیر نی‌ریز",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        // Fast Shift Progress Status
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier.clickable { onNavigateToFastUpdate() }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${unitAnalytics.progressPercentage}%",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "پیشرفت واحد",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Shift stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("کارهای امروز واحد", color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
                            Text("${unitTasks.size} تسک", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("در حال اجرای شیفت", color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
                            Text("${inProgressTasks.size} فعال", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("نفرات تحت نظارت", color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
                            Text("${unitAnalytics.totalManpower} نفر", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("موانع کارگاهی", color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
                            Text(
                                text = "${blockedTasks.size} مورد",
                                color = if (blockedTasks.isNotEmpty()) Color(0xFFFFD1D1) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Quick Action Fast Logs Banner
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = IndustrialEmerald.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, IndustrialEmerald.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToFastUpdate() }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditCalendar,
                            contentDescription = null,
                            tint = IndustrialEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "ثبت درصد و کارکرد شیفت",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = IndustrialEmerald
                            )
                            Text(
                                text = "ثبت سریع درصد و ساعت",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showQuickObstacleDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReportProblem,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "اعلام فوری مانع و توقف",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "اطلاع‌رسانی به برنامه‌ریزی",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 3. Safety & Work Permits Status Card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = IndustrialEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مجوزهای ایمنی و پروانه کار شیفت (Work Permits)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = IndustrialEmerald.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "مجوز فعال",
                                color = IndustrialEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PermitBadge(title = "کار گرم (Hot Work)", status = "تایید HSE", isOk = true, modifier = Modifier.weight(1f))
                        PermitBadge(title = "کار در ارتفاع", status = "داربست آماده", isOk = true, modifier = Modifier.weight(1f))
                        PermitBadge(title = "ایزولاسیون برقی/مکانیکی", status = "قفل و نشانه LOTO", isOk = true, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // 4. Today's Assigned Work Packages & Active Items
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "بسته‌های کاری و فعالیت‌های کارگاه واحد $userUnit",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigateToWbs() }) {
                    Text("مشاهده همه در WBS", fontSize = 11.sp)
                }
            }
        }

        if (unitTasks.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "هیچ فعالیت تعریف‌شده‌ای برای واحد $userUnit یافت نشد.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(unitTasks.take(6)) { task ->
                SupervisorTaskItemCard(
                    task = task,
                    onUpdateClick = { onItemClickForUpdate(task) }
                )
            }
        }

        // 5. Field Spare Parts & Materials Status
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = IndustrialSteelBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "وضعیت قطعات یدکی و ابزار کارگاه",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        TextButton(onClick = { onNavigateToProcurement() }) {
                            Text("درخواست قطعه", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val shopParts = listOf(
                        Triple("بیرینگ‌های پینیون شفت کمپرسور گاز پروسس", "تحویل کارگاه شده", true),
                        Triple("سیل‌رینگ‌ها و گسکت‌های کوره و شارژ هاپر", "آماده در انبار موقت", true),
                        Triple("تیغه‌ها و پاروهای ضدسایش کلاریفایر WTP", "در حال تخلیه", false)
                    )

                    shopParts.forEach { (part, status, isReady) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isReady) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = if (isReady) IndustrialEmerald else IndustrialAmberDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = part, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isReady) IndustrialEmerald.copy(alpha = 0.12f) else IndustrialAmber.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = status,
                                    fontSize = 10.sp,
                                    color = if (isReady) IndustrialEmerald else IndustrialAmberDark,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Quick Obstacle Dialog
    if (showQuickObstacleDialog) {
        var obstacleTitle by remember { mutableStateOf("") }
        var obstacleLocation by remember { mutableStateOf("Core Area - کوره") }
        var obstacleReason by remember { mutableStateOf("توقف به علت تاخیر جرثقیل یا ابزار مخصوص") }

        AlertDialog(
            onDismissRequest = { showQuickObstacleDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ReportProblem, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ثبت و اعلام مانع کارگاهی فوری", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "اعلان این مانع مستقیماً در داشبورد مدیر پروژه و واحد برنامه‌ریزی درج می‌شود.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = obstacleTitle,
                        onValueChange = { obstacleTitle = it },
                        label = { Text("شرح مانع یا توقف") },
                        placeholder = { Text("مثال: توقف دمونتاژ هاپر به دلیل عدم حضور جرثقیل ۵۰ تن") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = obstacleLocation,
                        onValueChange = { obstacleLocation = it },
                        label = { Text("موقعیت و ناحیه") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = obstacleReason,
                        onValueChange = { obstacleReason = it },
                        label = { Text("نوع و علت مانع") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showQuickObstacleDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("ارسال فوری گزارش توقف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickObstacleDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
fun SupervisorTaskItemCard(
    task: OversightItemEntity,
    onUpdateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onUpdateClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "ناحیه: ${task.generalArea} • تجهیز: ${task.equipmentName} • محل: ${task.executionLocation}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        task.progressPercentage >= 100 -> StatusCompletedBg
                        task.issues.isNotBlank() -> StatusBlockedBg
                        task.progressPercentage > 0 -> StatusInProgressBg
                        else -> Color.LightGray.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = "${task.progressPercentage}%",
                        color = when {
                            task.progressPercentage >= 100 -> StatusCompleted
                            task.issues.isNotBlank() -> StatusBlocked
                            task.progressPercentage > 0 -> StatusInProgress
                            else -> Color.DarkGray
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { (task.progressPercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    task.progressPercentage >= 100 -> IndustrialEmerald
                    task.issues.isNotBlank() -> MaterialTheme.colorScheme.error
                    else -> IndustrialSteelBlue
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "نفرات: ${task.manpowerCount} نفر • ساعات: ${task.actualHours}/${task.durationHours} ساعت",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ثبت درصد و کارکرد",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (task.issues.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "مانع: ${task.issues}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PermitBadge(
    title: String,
    status: String,
    isOk: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isOk) IndustrialEmerald.copy(alpha = 0.08f) else IndustrialAmber.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, if (isOk) IndustrialEmerald.copy(alpha = 0.3f) else IndustrialAmber.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = status,
                fontSize = 9.sp,
                color = if (isOk) IndustrialEmerald else IndustrialAmberDark,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ====================================================================
// 2. PLANNER PROJECT CONTROL DASHBOARD (داشبورد کنترل پروژه و برنامه‌ریزی)
// ====================================================================

@Composable
fun PlannerControlDashboard(
    kpis: DashboardKpis,
    unitAnalytics: UnitAnalytics,
    areaAnalyticsList: List<AreaAnalytics>,
    unitComparisons: List<UnitProgressComparison>,
    timelinePoints: List<TimelineProgressPoint>,
    overallPlannedProgress: Float,
    overallActualProgress: Float,
    oversight: OversightEntity?,
    onNavigateToWbs: () -> Unit,
    onNavigateToEodSync: () -> Unit,
    onNavigateToSessions: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Planner Header
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = IndustrialPurple,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "میز برنامه‌ریزی و کنترل پروژه (MSP Integration)",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "مرکز پایش انحرافات زمانی و تسویه پایان روز",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "SPI: ${String.format("%.2f", if (overallPlannedProgress > 0) overallActualProgress / overallPlannedProgress else 1.0f)}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "شاخص زمان",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Color.White.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("پیشرفت واقعی", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            Text("${String.format("%.1f", overallActualProgress)}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Column {
                            Text("برنامه مصوب MSP", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            Text("${String.format("%.1f", overallPlannedProgress)}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Column {
                            val variance = overallActualProgress - overallPlannedProgress
                            Text("انحراف از خط مبنا", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            Text(
                                text = if (variance >= 0) "+${String.format("%.1f", variance)}%" else "${String.format("%.1f", variance)}%",
                                color = if (variance >= 0) IndustrialEmerald else Color(0xFFFFD1D1),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Column {
                            Text("تسک‌های باز پایان روز", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            Text("${kpis.inProgressCount + kpis.blockedCount}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }

        // Quick EOD Sync Station
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = IndustrialPurple.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, IndustrialPurple.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToEodSync() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SyncAlt,
                            contentDescription = null,
                            tint = IndustrialPurple,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "تسویه پایان روز (EOD) و تبادل با MSP",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "جمع‌بندی کارکرد روزانه، انطباق با تقویم شیفت و دانلود فایل خروجی MSP",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Comparative S-Curve Card
        item {
            ProgressComparisonDashboardCard(
                unitComparisons = unitComparisons,
                timelinePoints = timelinePoints,
                overallPlannedProgress = overallPlannedProgress,
                overallActualProgress = overallActualProgress,
                onUnitClick = {}
            )
        }

        // Areas Breakdown
        item {
            AreaExecutionDashboardCard(
                areaList = areaAnalyticsList,
                selectedAreaKey = "Core Area",
                onSelectArea = {}
            )
        }
    }
}

// ====================================================================
// 3. UNIT HEAD EXECUTIVE DASHBOARD (داشبورد تخصصی مدیر/رئیس واحد)
// ====================================================================

@Composable
fun UnitHeadExecutiveDashboard(
    currentUser: UserEntity,
    kpis: DashboardKpis,
    unitAnalytics: UnitAnalytics,
    areaAnalyticsList: List<AreaAnalytics>,
    allUsers: List<UserEntity>,
    oversight: OversightEntity?,
    onNavigateToWbs: () -> Unit,
    onNavigateToEodSync: () -> Unit,
    onNavigateToProcurement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unitName = currentUser.unit ?: "مکانیک"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Unit Head Banner
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = IndustrialEmerald,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "داشبورد تخصصی و مدیریتی واحد $unitName",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "مدیر واحد: ${currentUser.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Progress Badge
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { unitAnalytics.progressPercentage / 100f },
                                modifier = Modifier.size(58.dp),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.25f),
                                strokeWidth = 5.dp
                            )
                            Text(
                                text = "${unitAnalytics.progressPercentage}%",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Color.White.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("کل تسک‌های واحد", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            Text("${unitAnalytics.totalTasks} فعالیت", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("تکمیل شده", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            Text("${unitAnalytics.completedTasks} تسک", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("نفرات فعال واحد", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            Text("${unitAnalytics.totalManpower} نفر", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("شاخص عملکرد SPI", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            Text("${String.format("%.2f", unitAnalytics.spi)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Unit Execution Detail Card
        item {
            UnitExecutionDashboardCard(
                unitAnalytics = unitAnalytics,
                allUsers = allUsers,
                selectedUnitName = unitName,
                onSelectUnit = {}
            )
        }

        // Area Distribution for this unit
        item {
            AreaExecutionDashboardCard(
                areaList = areaAnalyticsList,
                selectedAreaKey = "Core Area",
                onSelectArea = {}
            )
        }

        // Action shortcuts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onNavigateToWbs() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialEmerald)
                ) {
                    Icon(imageVector = Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("بسته‌های کاری واحد", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { onNavigateToProcurement() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("قطعات بحرانی", fontSize = 12.sp)
                }
            }
        }
    }
}
