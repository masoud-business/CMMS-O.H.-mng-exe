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
        "hse" -> listOf(
            RoleTabConfig(AppTab.SAFETY_PERMITS, "میز پرمیت و HSE", Icons.Default.HealthAndSafety),
            RoleTabConfig(AppTab.WBS, "پایش کارهای امروز", Icons.Default.AccountTree),
            RoleTabConfig(AppTab.DASHBOARD, "شاخص‌های ایمنی", Icons.Default.Shield),
            RoleTabConfig(AppTab.SESSIONS, "جلسات هماهنگی", Icons.Default.Groups),
            RoleTabConfig(AppTab.AUDIT, "لاگ‌های ممیزی", Icons.Default.Assessment)
        )
        "supervisor" -> listOf(
            RoleTabConfig(AppTab.DASHBOARD, "میز کارگاه", Icons.Default.Engineering, badgeCount = blockedTaskCount),
            RoleTabConfig(AppTab.WBS, "وظایف شیفت", Icons.Default.Checklist),
            RoleTabConfig(AppTab.SAFETY_PERMITS, "پرمیت‌های ایمنی", Icons.Default.HealthAndSafety),
            RoleTabConfig(AppTab.MSP_SYNC, "ثبت کارکرد", Icons.Default.AssignmentTurnedIn),
            RoleTabConfig(AppTab.PROCUREMENT, "قطعات کارگاه", Icons.Default.Build),
            RoleTabConfig(AppTab.SESSIONS, "مصوبات شیفت", Icons.Default.Notes)
        )
        "planner" -> listOf(
            RoleTabConfig(AppTab.DASHBOARD, "کنترل پروژه", Icons.Default.Timeline),
            RoleTabConfig(AppTab.WBS, "مدیریت WBS", Icons.Default.AccountTree),
            RoleTabConfig(AppTab.SAFETY_PERMITS, "مجوزهای HSE", Icons.Default.HealthAndSafety),
            RoleTabConfig(AppTab.MSP_SYNC, "تسویه MSP", Icons.Default.SyncAlt, badgeCount = pendingSyncCount),
            RoleTabConfig(AppTab.SESSIONS, "صورتجلسات", Icons.Default.Groups),
            RoleTabConfig(AppTab.PROCUREMENT, "پایش تامین", Icons.Default.ShoppingCart)
        )
        "unit_head" -> listOf(
            RoleTabConfig(AppTab.DASHBOARD, "داشبورد واحد", Icons.Default.Analytics),
            RoleTabConfig(AppTab.WBS, "کارهای واحد", Icons.Default.AccountTree),
            RoleTabConfig(AppTab.SAFETY_PERMITS, "پرمیت‌ها و LOTO", Icons.Default.HealthAndSafety),
            RoleTabConfig(AppTab.MSP_SYNC, "تاییدیه شیفت", Icons.Default.FactCheck),
            RoleTabConfig(AppTab.SESSIONS, "جلسات واحدها", Icons.Default.Groups),
            RoleTabConfig(AppTab.PROCUREMENT, "درخواست قطعه", Icons.Default.ShoppingCart)
        )
        else -> listOf(
            // Admin / Project Manager (مدیر ارشد و مدیر پروژه)
            RoleTabConfig(AppTab.DASHBOARD, "داشبورد کلان", Icons.Default.Dashboard),
            RoleTabConfig(AppTab.WBS, "ساختار WBS", Icons.Default.AccountTree),
            RoleTabConfig(AppTab.SAFETY_PERMITS, "پرمیت‌های ایمنی", Icons.Default.HealthAndSafety),
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
    // Filter tasks specific to this supervisor
    val userUnit = currentUser.unit ?: "مکانیک"
    val unitTasks = remember(rawItems, userUnit, currentUser.id, currentUser.name) {
        val baseUnitTasks = rawItems.filter { item ->
            item.executiveUnit.contains(userUnit) || item.title.contains(userUnit)
        }
        when (currentUser.id) {
            7L -> baseUnitTasks.filter { it.generalArea.equals("MHU", true) || it.title.contains("MHU") || it.title.contains("نوار") || it.equipmentName.contains("نوار") }
            8L -> baseUnitTasks.filter { it.equipmentName.contains("کمپرسور") || it.title.contains("کمپرسور") || it.equipmentName.contains("بلور") || it.title.contains("بلوور") }
            9L -> baseUnitTasks.filter { it.generalArea.equals("Core Area", true) && (it.equipmentName.contains("کوره") || it.title.contains("کوره") || it.equipmentName.contains("راکتور") || it.title.contains("لگ") || it.title.contains("وینچ") || it.title.contains("اسکوئر")) }
            10L -> baseUnitTasks.filter { item ->
                !(item.generalArea.equals("MHU", true) || item.title.contains("نوار") || item.equipmentName.contains("کمپرسور") || (item.generalArea.equals("Core Area", true) && (item.equipmentName.contains("کوره") || item.title.contains("کوره"))))
            }
            else -> baseUnitTasks
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
    rawItems: List<OversightItemEntity>,
    oversight: OversightEntity?,
    electricalLotoPermits: List<SafetyPermitEntity> = emptyList(),
    onNavigateToWbs: () -> Unit,
    onNavigateToEodSync: () -> Unit,
    onNavigateToProcurement: () -> Unit,
    onItemClickForUpdate: (OversightItemEntity) -> Unit = {},
    onUpdateLotoStatus: (Long, String, String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val unitName = currentUser.unit ?: "مکانیک"

    // 1. Find all supervisors belonging to this unit
    val unitSupervisors = remember(allUsers, unitName) {
        allUsers.filter { user ->
            user.role.equals("supervisor", ignoreCase = true) &&
            (user.unit?.contains(unitName) == true || unitName.contains(user.unit ?: ""))
        }
    }

    var selectedSupervisorId by remember { mutableStateOf<Long?>(null) } // null = All Supervisors in this Unit
    var selectedStatusFilter by remember { mutableStateOf("all") } // "all", "in_progress", "blocked", "completed"

    // 2. Unit-level items (Isolated strictly to this unit)
    val unitTasks = remember(rawItems, unitName) {
        rawItems.filter { it.executiveUnit.contains(unitName) || it.title.contains(unitName) }
    }

    // 3. Filter tasks by selected Supervisor
    val supervisorFilteredTasks = remember(unitTasks, selectedSupervisorId) {
        if (selectedSupervisorId == null) {
            unitTasks
        } else {
            when (selectedSupervisorId) {
                7L -> unitTasks.filter { it.generalArea.equals("MHU", true) || it.title.contains("MHU") || it.title.contains("نوار") || it.equipmentName.contains("نوار") }
                8L -> unitTasks.filter { it.equipmentName.contains("کمپرسور") || it.title.contains("کمپرسور") || it.equipmentName.contains("بلور") || it.title.contains("بلوور") }
                9L -> unitTasks.filter { it.generalArea.equals("Core Area", true) && (it.equipmentName.contains("کوره") || it.title.contains("کوره") || it.equipmentName.contains("راکتور") || it.title.contains("لگ") || it.title.contains("وینچ") || it.title.contains("اسکوئر")) }
                10L -> unitTasks.filter { item ->
                    !(item.generalArea.equals("MHU", true) || item.title.contains("نوار") || item.equipmentName.contains("کمپرسور") || (item.generalArea.equals("Core Area", true) && (item.equipmentName.contains("کوره") || item.title.contains("کوره"))))
                }
                12L -> unitTasks.filter { it.executiveUnit.contains("برق") }
                14L -> unitTasks.filter { it.executiveUnit.contains("ابزاردقیق") }
                15L -> unitTasks.filter { it.executiveUnit.contains("نسوز") }
                16L -> unitTasks.filter { it.executiveUnit.contains("انرژی") || it.executiveUnit.contains("سیالات") }
                17L -> unitTasks.filter { it.executiveUnit.contains("بازرسی") }
                else -> unitTasks
            }
        }
    }

    val displayTasks = remember(supervisorFilteredTasks, selectedStatusFilter) {
        when (selectedStatusFilter) {
            "in_progress" -> supervisorFilteredTasks.filter { it.progressPercentage in 1..99 }
            "blocked" -> supervisorFilteredTasks.filter { it.issues.isNotBlank() }
            "completed" -> supervisorFilteredTasks.filter { it.progressPercentage >= 100 }
            else -> supervisorFilteredTasks
        }
    }

    val activeSupervisor = unitSupervisors.firstOrNull { it.id == selectedSupervisorId }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Unit Head Executive Banner
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = IndustrialEmerald,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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
                                    text = "داشبورد تخصصی مدیریت واحد $unitName",
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

                        // Progress Gauge
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { unitAnalytics.progressPercentage / 100f },
                                modifier = Modifier.size(56.dp),
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
                            Text("${unitTasks.size} فعالیت", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("تکمیل شده", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            Text("${unitTasks.count { it.progressPercentage >= 100 }} تسک", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("دارای مانع", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            Text("${unitTasks.count { it.issues.isNotBlank() }} تسک", color = Color(0xFFFFD2D2), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("تعداد ناظران شیفت", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            Text("${unitSupervisors.size} ناظر اجرایی", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // 2. Supervisor Filter Bar (فیلتر بر اساس ناظران اجرایی واحد)
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = null,
                                tint = IndustrialEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "فیلتر وظایف بر اساس ناظران اجرایی واحد $unitName",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        if (selectedSupervisorId != null) {
                            TextButton(
                                onClick = { selectedSupervisorId = null },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("نمایش همه ناظران", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // "All" option
                        item {
                            val isAllSelected = selectedSupervisorId == null
                            FilterChip(
                                selected = isAllSelected,
                                onClick = { selectedSupervisorId = null },
                                label = { Text("همه ناظران (${unitTasks.size})", fontSize = 11.sp, fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Groups,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndustrialEmerald.copy(alpha = 0.15f),
                                    selectedLabelColor = IndustrialEmerald
                                )
                            )
                        }

                        items(unitSupervisors, key = { it.id }) { sup ->
                            val isSelected = selectedSupervisorId == sup.id
                            val supDomain = when (sup.id) {
                                7L -> "نوارنقاله و MHU"
                                8L -> "کمپرسورها و پکیج گاز"
                                9L -> "کوره و راکتور احیا"
                                10L -> "تجهیزات عمومی"
                                12L -> "پست‌های برق و MCC"
                                14L -> "اتوماسیون و ابزاردقیق"
                                15L -> "نسوزکاری و آجرچینی"
                                else -> sup.unit ?: "کارگاه"
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedSupervisorId = if (isSelected) null else sup.id },
                                label = {
                                    Column {
                                        Text(sup.name, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                        Text(supDomain, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isSelected) IndustrialEmerald else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndustrialEmerald.copy(alpha = 0.15f),
                                    selectedLabelColor = IndustrialEmerald
                                )
                            )
                        }
                    }
                }
            }
        }

        // 3. Selected Supervisor Spotlight Card (when a supervisor is chosen)
        if (activeSupervisor != null) {
            val supTasks = supervisorFilteredTasks
            val supProgress = if (supTasks.isNotEmpty()) supTasks.map { it.progressPercentage }.average().toInt() else 0
            val supManpower = supTasks.sumOf { it.manpowerCount }
            val supBlocked = supTasks.count { it.issues.isNotBlank() }

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = IndustrialSteelBlue.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, IndustrialSteelBlue.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = IndustrialSteelBlue.copy(alpha = 0.2f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Engineering, contentDescription = null, tint = IndustrialSteelBlue)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "کارهای شیفت امروز: ${activeSupervisor.name}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "تعداد تسک: ${supTasks.size} • نفرات: $supManpower نفر • موانع: $supBlocked",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = IndustrialEmerald.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "میانگین: $supProgress%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndustrialEmerald,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3.5. Special Electrical LOTO & Red Card Desk (سامانه پایش کارت‌های قرمز و ایزولاسیون برای واحد برق)
        if (unitName.contains("برق") || currentUser.unit?.contains("برق") == true || electricalLotoPermits.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFDC2626).copy(alpha = 0.08f),
                    border = BorderStroke(1.5.dp, Color(0xFFDC2626).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFDC2626).copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "سامانه کارت قرمز و ایزولاسیون الکتریکی (LOTO Desk)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFDC2626)
                                    )
                                    Text(
                                        text = "پایش فعالیت‌های نیازمند قطع برق در تمام واحدهای مجتمع",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            val pendingLotoCount = electricalLotoPermits.count { it.electricalLotoStatus != "isolated_and_tagged" }
                            if (pendingLotoCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFDC2626)
                                ) {
                                    Text(
                                        text = "$pendingLotoCount نیاز به قطع برق",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        if (electricalLotoPermits.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                electricalLotoPermits.forEach { lotoPermit ->
                                    val isIsolated = lotoPermit.electricalLotoStatus == "isolated_and_tagged"
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, if (isIsolated) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFFDC2626).copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "پرمیت ${lotoPermit.permitNumber}: ${lotoPermit.equipmentName}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                                Text(
                                                    text = "واحد درخواست‌کننده: ${lotoPermit.executiveUnit} • موقعیت: ${lotoPermit.location}",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            if (!isIsolated) {
                                                Button(
                                                    onClick = { onUpdateLotoStatus(lotoPermit.id, "isolated_and_tagged", currentUser.name) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("نصب کارت قرمز و قفل", fontSize = 10.sp)
                                                }
                                            } else {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                    ) {
                                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(13.dp))
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = "ایزوله شد (${lotoPermit.electricalTaggedBy})",
                                                            fontSize = 10.sp,
                                                            color = Color(0xFF10B981),
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Status Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    Pair("all", "همه (${supervisorFilteredTasks.size})"),
                    Pair("in_progress", "در حال اجرا (${supervisorFilteredTasks.count { it.progressPercentage in 1..99 }})"),
                    Pair("blocked", "دارای مانع (${supervisorFilteredTasks.count { it.issues.isNotBlank() }})"),
                    Pair("completed", "تکمیل شده (${supervisorFilteredTasks.count { it.progressPercentage >= 100 }})")
                ).forEach { (key, label) ->
                    val isSelected = selectedStatusFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatusFilter = key },
                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.height(30.dp)
                    )
                }
            }
        }

        // 5. Task Cards List
        if (displayTasks.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "هیچ فعالیتی با فیلتر انتخاب‌شده برای این ناظر یا واحد یافت نشد.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(displayTasks, key = { it.id }) { task ->
                SupervisorTaskItemCard(
                    task = task,
                    onUpdateClick = { onItemClickForUpdate(task) }
                )
            }
        }

        // 6. Action Shortcuts
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
                    Icon(imageVector = Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاهده کل WBS واحد", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = { onNavigateToProcurement() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("درخواست و قطعات", fontSize = 11.sp)
                }
            }
        }
    }
}
