package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import com.example.data.entity.*
import com.example.data.importer.ParsedImportPreview
import com.example.ui.components.*
import com.example.ui.theme.*

// ====================================================================
// 1. APP HEADER WITH RBAC BADGE & CONSOLIDATED USER MENU
// ====================================================================

@Composable
fun AppHeader(
    currentUser: UserEntity?,
    allUsers: List<UserEntity>,
    oversights: List<OversightEntity>,
    selectedOversightId: Long?,
    onSwitchUser: (UserEntity) -> Unit,
    onSelectOversight: (Long) -> Unit,
    onLogout: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onResetSeedData: () -> Unit = {}
) {
    var showUserMenu by remember { mutableStateOf(false) }
    var showSwitchUserSubMenu by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    // Distinct list of personnel for switching (sorted by organizational rank)
    val distinctPersonnel = remember(allUsers) {
        allUsers.distinctBy { it.id }.sortedWith(
            compareBy<UserEntity> {
                when (it.role) {
                    "admin" -> 1
                    "planner" -> 2
                    "unit_head" -> 3
                    "supervisor" -> 4
                    "hse" -> 5
                    else -> 6
                }
            }.thenBy { it.id }
        )
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand Badge & Plant Name
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(IndustrialNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PrecisionManufacturing,
                            contentDescription = "Ghadir Steel Logo",
                            tint = IndustrialAmber,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "فولاد غدیر نی‌ریز",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "اورهال سالیانه احیا ۱۴۰۴",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Unified User Profile & Options Dropdown Trigger
                Box {
                    Surface(
                        onClick = { showUserMenu = true },
                        shape = RoundedCornerShape(20.dp),
                        color = when (currentUser?.role) {
                            "admin" -> IndustrialNavy.copy(alpha = 0.12f)
                            "planner" -> IndustrialPurple.copy(alpha = 0.12f)
                            "unit_head" -> IndustrialEmerald.copy(alpha = 0.12f)
                            "hse" -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                            else -> IndustrialAmber.copy(alpha = 0.15f)
                        },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (currentUser?.role) {
                                            "admin" -> IndustrialNavy
                                            "planner" -> IndustrialPurple
                                            "unit_head" -> IndustrialEmerald
                                            "hse" -> MaterialTheme.colorScheme.error
                                            else -> IndustrialAmber
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = currentUser?.name?.take(18) ?: "کاربر",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = when (currentUser?.role) {
                                        "admin" -> "مدیر ارشد"
                                        "planner" -> "برنامه‌ریز"
                                        "unit_head" -> "رئیس واحد (${currentUser.unit})"
                                        "supervisor" -> "ناظر (${currentUser.unit})"
                                        "hse" -> "سرپرست HSE"
                                        else -> currentUser?.role ?: ""
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "User Menu",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Comprehensive User Dropdown Menu
                    DropdownMenu(
                        expanded = showUserMenu,
                        onDismissRequest = {
                            showUserMenu = false
                            showSwitchUserSubMenu = false
                        },
                        modifier = Modifier.widthIn(min = 280.dp, max = 340.dp)
                    ) {
                        // User Profile Info Card
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = currentUser?.name ?: "کاربر",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "نقش: ${getPersianRole(currentUser?.role ?: "")} | واحد: ${currentUser?.unit ?: "عمومی"}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "نام کاربری: ${currentUser?.username ?: "-"}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }

                        Divider()

                        // 1. Switch User Section (Available for admin or testing)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (showSwitchUserSubMenu) "بستن لیست کاربران ▴" else "سوییچ به حساب سایر پرسنل ▾",
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.SupervisorAccount,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                showSwitchUserSubMenu = !showSwitchUserSubMenu
                            }
                        )

                        if (showSwitchUserSubMenu) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    distinctPersonnel.forEach { user ->
                                        val isCurrent = user.id == currentUser?.id
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = user.name,
                                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                            fontSize = 12.sp,
                                                            color = if (isCurrent) IndustrialEmerald else MaterialTheme.colorScheme.onSurface
                                                        )
                                                        if (isCurrent) {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(
                                                                text = "(فعلی)",
                                                                fontSize = 10.sp,
                                                                color = IndustrialEmerald
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = "${getPersianRole(user.role)} • ${user.unit ?: "عمومی"}",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = if (isCurrent) Icons.Default.CheckCircle else Icons.Default.AccountCircle,
                                                    contentDescription = null,
                                                    tint = if (isCurrent) IndustrialEmerald else Color.Gray,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            onClick = {
                                                onSwitchUser(user)
                                                showUserMenu = false
                                                showSwitchUserSubMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Divider()

                        // 2. Settings Item
                        DropdownMenuItem(
                            text = { Text("تنظیمات برنامه و ظاهر (Theme/Font)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "تنظیمات",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = {
                                showUserMenu = false
                                onOpenSettings()
                            }
                        )

                        // 3. Reset & Re-Seed Data Item
                        DropdownMenuItem(
                            text = { Text("بازنشانی و پر کردن داده‌های WBS اورهال") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "بازنشانی داده‌ها",
                                    tint = IndustrialAmber
                                )
                            },
                            onClick = {
                                showUserMenu = false
                                showResetConfirm = true
                            }
                        )

                        Divider()

                        // 4. Logout Item
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "خروج از حساب کاربری (Logout)",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = "خروج",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showUserMenu = false
                                showLogoutConfirm = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("بازنشانی اطلاعات نمونه اورهال", fontWeight = FontWeight.Bold) },
            text = { Text("آیا مایلید تمام داده‌های ساختار WBS، پیش‌نیازها، درصد پیشرفت‌های فرضی، لاگ‌های روزانه و پرمیت‌های ایمنی مجدداً در دیتابیس بارگذاری شوند؟") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirm = false
                        onResetSeedData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialAmber)
                ) {
                    Text("بازنشانی و بارگذاری مجدد")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("خروج از حساب کاربری", fontWeight = FontWeight.Bold) },
            text = { Text("آیا مطمئن هستید که می‌خواهید از حساب کاربری خارج شوید؟ برای ورود بعدی نیاز به نام کاربری و کلمه عبور خواهید داشت.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("خروج قطعی")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

// ====================================================================
// 2. DASHBOARD TAB WITH ROLE-SPECIFIC ANALYTICS & KPIS
// ====================================================================

@Composable
fun DashboardTab(
    kpis: DashboardKpis,
    unitAnalytics: UnitAnalytics,
    areaAnalyticsList: List<AreaAnalytics>,
    unitComparisons: List<UnitProgressComparison>,
    timelinePoints: List<TimelineProgressPoint>,
    overallPlannedProgress: Float,
    overallActualProgress: Float,
    selectedAreaName: String,
    selectedUnitName: String,
    currentUser: UserEntity?,
    oversight: OversightEntity?,
    allUsers: List<UserEntity>,
    rawItems: List<OversightItemEntity> = emptyList(),
    onSelectArea: (String) -> Unit,
    onSelectUnit: (String) -> Unit,
    onNavigateToWbs: () -> Unit,
    onNavigateToEodSync: () -> Unit,
    onNavigateToProcurement: () -> Unit,
    onItemClickForDailyUpdate: (OversightItemEntity) -> Unit = {}
) {
    // Current role perspective: Admins can preview perspectives, but non-admins are strictly locked to their assigned role
    val userRole = currentUser?.role ?: "admin"
    var selectedRolePerspective by remember(userRole) {
        mutableStateOf(userRole)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Perspective Switcher Bar: Only accessible to Admin / Project Manager to prevent unauthorized dashboard access
        if (userRole == "admin") {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "نمای مدیریتی (تغییر پرسپکتیو):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val rolesList = listOf(
                        Triple("admin", "مدیر ارشد پروژه", Icons.Default.Dashboard),
                        Triple("supervisor", "ناظر اجرایی", Icons.Default.Engineering),
                        Triple("planner", "کنترل پروژه", Icons.Default.Timeline),
                        Triple("unit_head", "رئیس واحد", Icons.Default.Analytics)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(rolesList) { (roleKey, roleLabel, roleIcon) ->
                            val isSelected = selectedRolePerspective == roleKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedRolePerspective = roleKey },
                                label = { Text(roleLabel, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = roleIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.height(30.dp)
                            )
                        }
                    }
                }
            }
        }

        // Active Dashboard strictly matching the user's role (or selected perspective for Admin)
        val activeRole = if (userRole == "admin") selectedRolePerspective else userRole

        Box(modifier = Modifier.weight(1f)) {
            when (activeRole) {
                "supervisor" -> {
                    ShopFloorSupervisorDashboard(
                        currentUser = currentUser ?: UserEntity(name = "مهندس بقری", role = "supervisor", unit = "مکانیک", email = ""),
                        kpis = kpis,
                        unitAnalytics = unitAnalytics,
                        rawItems = rawItems,
                        oversight = oversight,
                        onNavigateToWbs = onNavigateToWbs,
                        onNavigateToFastUpdate = onNavigateToEodSync,
                        onNavigateToProcurement = onNavigateToProcurement,
                        onItemClickForUpdate = onItemClickForDailyUpdate
                    )
                }
                "planner" -> {
                    PlannerControlDashboard(
                        kpis = kpis,
                        unitAnalytics = unitAnalytics,
                        areaAnalyticsList = areaAnalyticsList,
                        unitComparisons = unitComparisons,
                        timelinePoints = timelinePoints,
                        overallPlannedProgress = overallPlannedProgress,
                        overallActualProgress = overallActualProgress,
                        oversight = oversight,
                        onNavigateToWbs = onNavigateToWbs,
                        onNavigateToEodSync = onNavigateToEodSync,
                        onNavigateToSessions = {}
                    )
                }
                "unit_head" -> {
                    UnitHeadExecutiveDashboard(
                        currentUser = currentUser ?: UserEntity(name = "مهندس اله بخش", role = "unit_head", unit = "مکانیک", email = ""),
                        kpis = kpis,
                        unitAnalytics = unitAnalytics,
                        areaAnalyticsList = areaAnalyticsList,
                        allUsers = allUsers,
                        rawItems = rawItems,
                        oversight = oversight,
                        onNavigateToWbs = onNavigateToWbs,
                        onNavigateToEodSync = onNavigateToEodSync,
                        onNavigateToProcurement = onNavigateToProcurement,
                        onItemClickForUpdate = onItemClickForDailyUpdate
                    )
                }
                else -> {
                    // Default: Project Manager / Admin Dashboard
                    ProjectManagerExecutiveDashboard(
                        kpis = kpis,
                        unitAnalytics = unitAnalytics,
                        areaAnalyticsList = areaAnalyticsList,
                        unitComparisons = unitComparisons,
                        timelinePoints = timelinePoints,
                        overallPlannedProgress = overallPlannedProgress,
                        overallActualProgress = overallActualProgress,
                        selectedAreaName = selectedAreaName,
                        selectedUnitName = selectedUnitName,
                        currentUser = currentUser,
                        oversight = oversight,
                        allUsers = allUsers,
                        onSelectArea = onSelectArea,
                        onSelectUnit = onSelectUnit
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectManagerExecutiveDashboard(
    kpis: DashboardKpis,
    unitAnalytics: UnitAnalytics,
    areaAnalyticsList: List<AreaAnalytics>,
    unitComparisons: List<UnitProgressComparison>,
    timelinePoints: List<TimelineProgressPoint>,
    overallPlannedProgress: Float,
    overallActualProgress: Float,
    selectedAreaName: String,
    selectedUnitName: String,
    currentUser: UserEntity?,
    oversight: OversightEntity?,
    allUsers: List<UserEntity>,
    onSelectArea: (String) -> Unit,
    onSelectUnit: (String) -> Unit
) {
    var activeAreaTab by remember(selectedAreaName) { mutableStateOf(selectedAreaName) }
    var activeUnitTab by remember(selectedUnitName) { mutableStateOf(selectedUnitName) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = IndustrialNavy,
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
                                color = IndustrialAmber.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "وضعیت: در حال اجرای اورهال سالیانه (نمای مدیر پروژه)",
                                    color = IndustrialAmber,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = oversight?.title ?: "برنامه جامع اورهال سالیانه مجتمع فولاد غدیر نی‌ریز",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Progress Gauge
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { kpis.overallProgressPercent / 100f },
                                modifier = Modifier.size(62.dp),
                                color = IndustrialAmber,
                                trackColor = Color.White.copy(alpha = 0.2f),
                                strokeWidth = 6.dp
                            )
                            Text(
                                text = "${kpis.overallProgressPercent}%",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
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
                            Text("نفرات فعال ثبت‌شده", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text("${kpis.totalManpowerToday} نفر", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Column {
                            Text("کارکرد صرف‌شده", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text("${kpis.totalActualHours.toInt()} ساعت", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Column {
                            Text("کل فعالیت‌های WBS", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text("${kpis.totalItems} تسک", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        // 1. DASHBOARD AND CHARTS FOR MAIN AREAS (Core Area, MHU, WTP)
        item {
            AreaExecutionDashboardCard(
                areaList = areaAnalyticsList,
                selectedAreaKey = activeAreaTab,
                onSelectArea = {
                    activeAreaTab = it
                    onSelectArea(it)
                }
            )
        }

        // 2. DASHBOARD AND CHARTS FOR MAIN REPAIR UNITS (مکانیک، برق، ابزاردقیق و اتوماسیون، نسوز)
        item {
            UnitExecutionDashboardCard(
                unitAnalytics = unitAnalytics,
                allUsers = allUsers,
                selectedUnitName = activeUnitTab,
                onSelectUnit = {
                    activeUnitTab = it
                    onSelectUnit(it)
                }
            )
        }

        // 3. OVERHAUL ORGANIZATIONAL PERSONNEL ROSTER
        item {
            PersonnelDirectoryCard(allUsers = allUsers)
        }

        // 4. COMPARATIVE PROGRESS CHART ACROSS ALL UNITS & S-CURVE
        item {
            ProgressComparisonDashboardCard(
                unitComparisons = unitComparisons,
                timelinePoints = timelinePoints,
                overallPlannedProgress = overallPlannedProgress,
                overallActualProgress = overallActualProgress,
                onUnitClick = { unitComp ->
                    activeUnitTab = unitComp.unitName
                    onSelectUnit(unitComp.unitName)
                }
            )
        }

        // 5. STATUS BREAKDOWN CARDS
        item {
            Text(
                text = "تفکیک وضعیت کل فعالیت‌های اورهال",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusKpiCard(
                    title = "تکمیل شده",
                    count = kpis.completedCount,
                    color = IndustrialEmerald,
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
                StatusKpiCard(
                    title = "در حال اجرا",
                    count = kpis.inProgressCount,
                    color = IndustrialSteelBlue,
                    icon = Icons.Default.Engineering,
                    modifier = Modifier.weight(1f)
                )
                StatusKpiCard(
                    title = "دارای مانع",
                    count = kpis.blockedCount,
                    color = MaterialTheme.colorScheme.error,
                    icon = Icons.Default.ReportProblem,
                    modifier = Modifier.weight(1f)
                )
                StatusKpiCard(
                    title = "در انتظار",
                    count = kpis.pendingCount,
                    color = Color.Gray,
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 6. CRITICAL BOTTLENECKS SECTION
        if (kpis.criticalBottlenecks.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "موانع و گلوگاه‌های بحرانی اورهال (${kpis.criticalBottlenecks.size})",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        kpis.criticalBottlenecks.forEach { task ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(task.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            text = "ناحیه: ${task.generalArea} • واحد: ${task.executiveUnit} • تجهیز: ${task.equipmentName} • علت: ${task.issues.ifBlank { "توقف اجرایی یا کمبود متریال" }}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.error
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

// ====================================================================
// 2.1 DASHBOARD & CHARTS FOR MAIN AREAS (Core Area, MHU, WTP)
// ====================================================================

@Composable
fun AreaExecutionDashboardCard(
    areaList: List<AreaAnalytics>,
    selectedAreaKey: String,
    onSelectArea: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeKey by remember(selectedAreaKey) { mutableStateOf(selectedAreaKey) }
    val currentArea = areaList.firstOrNull { it.areaKey.equals(activeKey, ignoreCase = true) }
        ?: areaList.firstOrNull { it.areaKey.contains(activeKey, ignoreCase = true) }
        ?: areaList.firstOrNull()
        ?: AreaAnalytics(areaKey = activeKey, areaTitle = activeKey)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        shadowElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = IndustrialNavy.copy(alpha = 0.12f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocationCity,
                                contentDescription = null,
                                tint = IndustrialNavy,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "داشبورد و نمودار پیشرفت نواحی اصلی کارخانه",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "کوره و احیا • انتقال مواد (MHU) • تصفیه آب (WTP)",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Variance Badge
                val isPositive = currentArea.variance >= 0f
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPositive) StatusCompletedBg else StatusBlockedBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPositive) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (isPositive) StatusCompleted else StatusBlocked,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isPositive) "+${String.format("%.1f", currentArea.variance)}%" else "${String.format("%.1f", currentArea.variance)}%",
                            color = if (isPositive) StatusCompleted else StatusBlocked,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Area Selection Chips
            val areaTabs = listOf(
                Pair("Core Area", "کوره و احیا (Core Area)"),
                Pair("MHU", "انتقال مواد (MHU)"),
                Pair("WTP", "تصفیه آب (WTP)")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                areaTabs.forEach { (key, title) ->
                    val isSelected = activeKey.equals(key, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) IndustrialNavy else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                activeKey = key
                                onSelectArea(key)
                            }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Area Execution Card details
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header of current Area
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = currentArea.areaTitle.ifBlank { currentArea.areaKey },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "تعداد ${currentArea.totalTasks} فعالیت WBS در این ناحیه تعریف شده است",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${String.format("%.1f", currentArea.actualProgress)}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = IndustrialNavy
                            )
                            Text(
                                text = "برنامه: ${String.format("%.1f", currentArea.plannedProgress)}%",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Comparative Bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("پیشرفت واقعی (Actual)", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = IndustrialNavy)
                            Text("${String.format("%.1f", currentArea.actualProgress)}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = IndustrialNavy)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        LinearProgressIndicator(
                            progress = { (currentArea.actualProgress / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (currentArea.actualProgress >= currentArea.plannedProgress) IndustrialEmerald else IndustrialAmber,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("برنامه زمان‌بندی (Planned)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${String.format("%.1f", currentArea.plannedProgress)}%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        LinearProgressIndicator(
                            progress = { (currentArea.plannedProgress / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = IndustrialSteelBlue,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Status & Resource Mini Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatusMiniBox(label = "تکمیل", count = currentArea.completedTasks, color = IndustrialEmerald, modifier = Modifier.weight(1f))
                        StatusMiniBox(label = "در حال اجرا", count = currentArea.inProgressTasks, color = IndustrialSteelBlue, modifier = Modifier.weight(1f))
                        StatusMiniBox(label = "مانع‌دار", count = currentArea.blockedTasks, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                        StatusMiniBox(label = "نفر فعال", count = currentArea.totalManpower, color = IndustrialPurple, modifier = Modifier.weight(1f))
                    }

                    // Progress breakdown across repair units in this area
                    if (currentArea.unitBreakdown.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "پیشرفت به تفکیک واحدهای تعمیراتی در این ناحیه:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        currentArea.unitBreakdown.forEach { (unit, prog) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = unit,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.width(110.dp)
                                )
                                LinearProgressIndicator(
                                    progress = { (prog / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = when {
                                        unit.contains("مکانیک") -> IndustrialNavy
                                        unit.contains("برق") -> IndustrialAmber
                                        unit.contains("ابزار") || unit.contains("اتوماسیون") -> IndustrialSteelBlue
                                        unit.contains("نسوز") -> IndustrialBurgundy
                                        else -> IndustrialEmerald
                                    },
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$prog%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ====================================================================
// 2.2 DASHBOARD & CHARTS FOR MAIN REPAIR UNITS
// ====================================================================

@Composable
fun UnitExecutionDashboardCard(
    unitAnalytics: UnitAnalytics,
    allUsers: List<UserEntity>,
    selectedUnitName: String,
    onSelectUnit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val unitList = listOf(
        "مکانیک",
        "برق",
        "ابزاردقیق و اتوماسیون",
        "نسوز",
        "انرژی و سیالات",
        "بازرسی فنی"
    )

    val unitHeadMap = mapOf(
        "مکانیک" to Pair("مهندس اله بخش", "رئیس واحد مکانیک"),
        "برق" to Pair("مهندس یادگار", "رئیس واحد برق"),
        "ابزاردقیق و اتوماسیون" to Pair("مهندس ریحانی", "رئیس واحد ابزاردقیق و اتوماسیون"),
        "نسوز" to Pair("مهندس یاراحمدی", "سرپرست و ناظر نسوز"),
        "انرژی و سیالات" to Pair("مهندس ضیغمی", "کارشناس و ناظر سیالات (WTP)"),
        "بازرسی فنی" to Pair("مهندس خاکی", "کارشناس بازرسی فنی")
    )

    val unitSupervisorsMap = mapOf(
        "مکانیک" to listOf("مهندس بقری", "مهندس فرخ", "مهندس شجاعی فرد", "مهندس مبارکی"),
        "برق" to listOf("مهندس البرزی"),
        "ابزاردقیق و اتوماسیون" to listOf("مهندس دمیری"),
        "نسوز" to listOf("تیم تخصصی نسوزکاری و آجرچینی"),
        "انرژی و سیالات" to listOf("تیم پایش آب صنعتی و کلاریفایر"),
        "بازرسی فنی" to listOf("تیم بازرسی ضخامت‌سنجی و NDT")
    )

    val currentHead = unitHeadMap[selectedUnitName] ?: Pair("مسئول واحد", "رئیس واحد")
    val currentSupervisors = unitSupervisorsMap[selectedUnitName] ?: emptyList()

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        shadowElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = IndustrialEmerald.copy(alpha = 0.12f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Engineering,
                                contentDescription = null,
                                tint = IndustrialEmerald,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "داشبورد و نمودار واحدهای تعمیراتی اصلی",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "مکانیک • برق • ابزاردقیق و اتوماسیون • نسوز",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // SPI Performance badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (unitAnalytics.spi >= 1.0f) StatusCompletedBg else if (unitAnalytics.spi >= 0.85f) IndustrialAmber.copy(alpha = 0.15f) else StatusBlockedBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SPI: ${String.format("%.2f", unitAnalytics.spi)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (unitAnalytics.spi >= 1.0f) StatusCompleted else if (unitAnalytics.spi >= 0.85f) IndustrialAmberDark else StatusBlocked
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Unit Selector Scrollable Row / Grid
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(unitList) { unitName ->
                    val isSelected = selectedUnitName == unitName
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) IndustrialEmerald else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.clickable { onSelectUnit(unitName) }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = unitName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Unit Detail Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Unit Head & Team Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = IndustrialEmerald.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = currentHead.second,
                                        color = IndustrialEmerald,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentHead.first,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            if (currentSupervisors.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "کارشناسان و ناظرین: ${currentSupervisors.joinToString("، ")}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${unitAnalytics.progressPercentage}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = IndustrialEmerald
                            )
                            Text(
                                text = "برنامه: ${String.format("%.1f", unitAnalytics.plannedProgress)}%",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Comparison Bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("پیشرفت واقعی واحد $selectedUnitName", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = IndustrialEmerald)
                            Text("${unitAnalytics.progressPercentage}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = IndustrialEmerald)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        LinearProgressIndicator(
                            progress = { (unitAnalytics.progressPercentage / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (unitAnalytics.progressPercentage >= unitAnalytics.plannedProgress) IndustrialEmerald else IndustrialAmber,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("برنامه زمان‌بندی (Planned)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${String.format("%.1f", unitAnalytics.plannedProgress)}%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        LinearProgressIndicator(
                            progress = { (unitAnalytics.plannedProgress / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = IndustrialSteelBlue,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Status & Resource Mini Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatusMiniBox(label = "کل فعالیت‌ها", count = unitAnalytics.totalTasks, color = IndustrialNavy, modifier = Modifier.weight(1f))
                        StatusMiniBox(label = "تکمیل", count = unitAnalytics.completedTasks, color = IndustrialEmerald, modifier = Modifier.weight(1f))
                        StatusMiniBox(label = "در حال اجرا", count = unitAnalytics.inProgressTasks, color = IndustrialSteelBlue, modifier = Modifier.weight(1f))
                        StatusMiniBox(label = "نفرات فعال", count = unitAnalytics.totalManpower, color = IndustrialAmberDark, modifier = Modifier.weight(1f))
                    }

                    // Progress breakdown across areas (Core Area, MHU, WTP)
                    if (unitAnalytics.areaBreakdown.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "پیشرفت واحد $selectedUnitName در نواحی کارخانه:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        unitAnalytics.areaBreakdown.forEach { (area, prog) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = area,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.width(110.dp)
                                )
                                LinearProgressIndicator(
                                    progress = { (prog / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (prog >= 50) IndustrialEmerald else IndustrialAmber,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$prog%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ====================================================================
// 2.3 OVERHAUL ORGANIZATIONAL PERSONNEL ROSTER
// ====================================================================

@Composable
fun PersonnelDirectoryCard(
    allUsers: List<UserEntity>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        shadowElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = IndustrialPurple.copy(alpha = 0.12f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = IndustrialPurple,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ساختار سازمانی و پرسنل اورهال غدیر نی‌ریز",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "مدیریت • برنامه‌ریزی • مکانیک • برق • ابزاردقیق و اتوماسیون • نسوز • WTP • بازرسی",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(10.dp))

                    val orgSections = listOf(
                        Triple("مدیریت پروژه اورهال", "مهندس اعمالی (مدیر پروژه اورهال فولاد غدیر نی‌ریز)", IndustrialNavy),
                        Triple(
                            "واحد برنامه‌ریزی تعمیرات",
                            "مهندس تاج بخش (رئیس برنامه‌ریزی تعمیرات)\nکارشناسان: مهندس پورخندانی، مهندس اله بخشی، مهندس شبستان",
                            IndustrialPurple
                        ),
                        Triple(
                            "واحد تعمیرات مکانیک",
                            "مهندس اله بخش (رئیس واحد مکانیک)\nکارشناسان و ناظرین: مهندسان بقری، فرخ، شجاعی فرد، مبارکی",
                            IndustrialEmerald
                        ),
                        Triple(
                            "واحد تعمیرات برق",
                            "مهندس یادگار (رئیس واحد برق)\nکارشناس: مهندس البرزی",
                            IndustrialAmberDark
                        ),
                        Triple(
                            "واحد ابزاردقیق و اتوماسیون",
                            "مهندس ریحانی (رئیس واحد ابزاردقیق و اتوماسیون)\nکارشناس: مهندس دمیری (واحد یکپارچه اتوماسیون و ابزاردقیق)",
                            IndustrialSteelBlue
                        ),
                        Triple(
                            "واحد نسوز",
                            "مهندس یاراحمدی (کارشناس و سرپرست نسوز)",
                            IndustrialBurgundy
                        ),
                        Triple(
                            "واحد انرژی و سیالات (WTP)",
                            "مهندس ضیغمی (کارشناس و ناظر سیالات)",
                            IndustrialSteelBlue
                        ),
                        Triple(
                            "واحد بازرسی فنی",
                            "مهندس خاکی (کارشناس بازرسی فنی و NDT)",
                            Color(0xFF5D4037)
                        )
                    )

                    orgSections.forEach { (title, description, color) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = color.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = color
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusKpiCard(
    title: String,
    count: Int,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = count.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

// ====================================================================
// 3. WBS EXECUTION TAB WITH HIERARCHICAL MINDMAP & NESTED TASK TRACKING
// ====================================================================

@Composable
fun WbsExecutionTab(
    items: List<OversightItemEntity>,
    wbsTree: List<WbsTreeNode>,
    allPrerequisites: List<ItemPrerequisiteEntity>,
    allAssignments: List<ItemAssignmentEntity>,
    allUsers: List<UserEntity>,
    currentUser: UserEntity?,
    searchQuery: String,
    selectedUnit: String?,
    selectedStatus: String?,
    supervisorFilter: SupervisorFilter,
    showOnlyMine: Boolean,
    expandedUnits: Set<String>,
    expandedAreas: Set<String>,
    expandedTreeNodes: Set<Long>,
    currentViewMode: WbsViewMode,
    onSearchChange: (String) -> Unit,
    onUnitSelect: (String?) -> Unit,
    onStatusSelect: (String?) -> Unit,
    onSupervisorFilterSelect: (SupervisorFilter) -> Unit,
    onToggleOnlyMine: (Boolean) -> Unit,
    onToggleUnitExpansion: (String) -> Unit,
    onToggleAreaExpansion: (String) -> Unit,
    onToggleTreeNodeExpanded: (Long) -> Unit,
    onExpandAllTreeNodes: () -> Unit,
    onCollapseAllTreeNodes: () -> Unit,
    onSetViewMode: (WbsViewMode) -> Unit,
    onItemClickForDailyUpdate: (OversightItemEntity) -> Unit,
    onAddNewItem: () -> Unit,
    onAddSubtask: (parent: OversightItemEntity, title: String, durationHours: Double, manpower: Int, equipment: String, location: String) -> Unit,
    onDeleteHierarchy: (itemId: Long) -> Unit,
    onEditItem: (item: OversightItemEntity) -> Unit
) {
    com.example.ui.components.WbsHierarchyManagementView(
        items = items,
        wbsTree = wbsTree,
        allPrerequisites = allPrerequisites,
        allAssignments = allAssignments,
        allUsers = allUsers,
        currentUser = currentUser,
        searchQuery = searchQuery,
        selectedUnit = selectedUnit,
        selectedStatus = selectedStatus,
        supervisorFilter = supervisorFilter,
        showOnlyMine = showOnlyMine,
        expandedUnits = expandedUnits,
        expandedAreas = expandedAreas,
        expandedTreeNodes = expandedTreeNodes,
        currentViewMode = currentViewMode,
        onSearchChange = onSearchChange,
        onUnitSelect = onUnitSelect,
        onStatusSelect = onStatusSelect,
        onSupervisorFilterSelect = onSupervisorFilterSelect,
        onToggleOnlyMine = onToggleOnlyMine,
        onToggleUnitExpansion = onToggleUnitExpansion,
        onToggleAreaExpansion = onToggleAreaExpansion,
        onToggleTreeNodeExpanded = onToggleTreeNodeExpanded,
        onExpandAllTreeNodes = onExpandAllTreeNodes,
        onCollapseAllTreeNodes = onCollapseAllTreeNodes,
        onSetViewMode = onSetViewMode,
        onItemClickForDailyUpdate = onItemClickForDailyUpdate,
        onAddNewRootItem = onAddNewItem,
        onAddSubtask = onAddSubtask,
        onDeleteHierarchy = onDeleteHierarchy,
        onEditItem = onEditItem
    )
}

@Composable
fun WbsTaskCard(
    item: OversightItemEntity,
    allPrerequisites: List<ItemPrerequisiteEntity>,
    allAssignments: List<ItemAssignmentEntity>,
    allUsers: List<UserEntity>,
    currentUser: UserEntity?,
    onQuickUpdate: () -> Unit
) {
    val prereqCount = allPrerequisites.count { it.itemId == item.id }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(
            1.dp,
            when (item.status) {
                "blocked" -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                "in_progress" -> IndustrialSteelBlue.copy(alpha = 0.5f)
                "completed" -> IndustrialEmerald.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outlineVariant
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onQuickUpdate() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // WBS & Equipment & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "WBS: ${item.wbsCode}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (item.equipmentName.isNotBlank() && item.equipmentName != item.title) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تجهیز: ${item.equipmentName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (item.status) {
                        "completed" -> IndustrialEmerald.copy(alpha = 0.15f)
                        "in_progress" -> IndustrialSteelBlue.copy(alpha = 0.15f)
                        "blocked" -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        else -> Color.Gray.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = when (item.status) {
                            "completed" -> "تکمیل شده"
                            "in_progress" -> "در حال اجرا"
                            "blocked" -> "متوقف / دارای مانع"
                            else -> "در انتظار شروع"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (item.status) {
                            "completed" -> IndustrialEmerald
                            "in_progress" -> IndustrialSteelBlue
                            "blocked" -> MaterialTheme.colorScheme.error
                            else -> Color.Gray
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Task Name
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { item.progressPercentage / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when (item.status) {
                        "completed" -> IndustrialEmerald
                        "blocked" -> MaterialTheme.colorScheme.error
                        else -> IndustrialAmber
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${item.progressPercentage}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Badges: Manpower, Hours, Prerequisites, Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Manpower Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${item.manpowerCount} نفر", fontSize = 11.sp)
                        }
                    }

                    // Duration / Hours Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${item.actualHours.toInt()} / ${item.durationHours.toInt()} ساعت", fontSize = 11.sp)
                        }
                    }

                    if (prereqCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = IndustrialAmber.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = IndustrialAmberDark, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("$prereqCount پیش‌نیاز", fontSize = 10.sp, color = IndustrialAmberDark)
                            }
                        }
                    }
                }

                // Quick Action Button
                FilledTonalButton(
                    onClick = onQuickUpdate,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (currentUser?.role == "supervisor") "ثبت کارکرد" else "جزئیات / تغییر",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ====================================================================
// 4. SUPERVISOR DAILY PROGRESS & TIME RECORDING DIALOG
// ====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorDailyUpdateDialog(
    item: OversightItemEntity,
    prerequisites: List<OversightItemEntity>,
    currentUser: UserEntity?,
    onDismiss: () -> Unit,
    onConfirm: (
        newProgress: Int,
        manpowerCount: Int,
        hoursSpent: Double,
        status: String,
        remarks: String,
        issues: String
    ) -> Unit
) {
    var progress by remember { mutableStateOf(item.progressPercentage) }
    var manpower by remember { mutableStateOf(if (item.manpowerCount > 0) item.manpowerCount else 4) }
    var hoursSpentText by remember { mutableStateOf("2") }
    var selectedStatus by remember { mutableStateOf(item.status) }
    var remarks by remember { mutableStateOf("") }
    var issues by remember { mutableStateOf(item.issues) }

    val incompletePrereqs = prerequisites.filter { it.status != "completed" }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ثبت کارکرد روزانه و پیشرفت",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "کد WBS: ${item.wbsCode} • واحد: ${item.executiveUnit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "بستن")
                        }
                    }
                }

                // Task Name Banner
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item.title,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Prerequisite Warnings
                if (incompletePrereqs.isNotEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "هشدار: پیش‌نیازهای زیر تکمیل نشده‌اند:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                                incompletePrereqs.forEach { p ->
                                    Text(
                                        text = "• [${p.wbsCode}] ${p.title} (${getPersianStatusLabel(p.status)} - ${p.progressPercentage}%)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Progress Percentage Slider
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("درصد پیشرفت کار (Progress)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("$progress%", fontWeight = FontWeight.Bold, color = IndustrialSteelBlue, fontSize = 14.sp)
                        }
                        Slider(
                            value = progress.toFloat(),
                            onValueChange = { progress = it.toInt() },
                            valueRange = 0f..100f,
                            steps = 19
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(0, 25, 50, 75, 100).forEach { p ->
                                OutlinedButton(
                                    onClick = {
                                        progress = p
                                        if (p == 100) selectedStatus = "completed"
                                        else if (p > 0 && selectedStatus == "pending") selectedStatus = "in_progress"
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("$p%", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                // Manpower and Hours Count
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Manpower Count
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("تعداد نفرات فعال", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    IconButton(
                                        onClick = { if (manpower > 1) manpower-- },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null)
                                    }
                                    Text("$manpower نفر", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    IconButton(
                                        onClick = { manpower++ },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                    }
                                }
                            }
                        }

                        // Hours Spent Today
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("ساعات کارکرد امروز", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = hoursSpentText,
                                    onValueChange = { hoursSpentText = it },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Status Selector
                item {
                    Text("وضعیت اجرایی فعالیت", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple("pending", "در انتظار", Color.Gray),
                            Triple("in_progress", "در حال اجرا", IndustrialSteelBlue),
                            Triple("completed", "تکمیل شده", IndustrialEmerald),
                            Triple("blocked", "متوقف / مانع", MaterialTheme.colorScheme.error)
                        ).forEach { (st, label, clr) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedStatus == st) clr.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, if (selectedStatus == st) clr else Color.Transparent),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedStatus = st
                                        if (st == "completed") progress = 100
                                        if (st == "pending") progress = 0
                                    }
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedStatus == st) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedStatus == st) clr else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Obstacles / Issues (if blocked or general)
                item {
                    OutlinedTextField(
                        value = issues,
                        onValueChange = { issues = it },
                        label = { Text("شرح موانع یا دلایل توقف (در صورت وجود)") },
                        placeholder = { Text("مانند: در انتظار تحویل قطعه یدکی، عدم حضور پیمانکار نسوز...") },
                        maxLines = 2,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Supervisor Daily Remarks
                item {
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("توضیحات و گزارش کار ناظر") },
                        placeholder = { Text("اقدامات انجام‌شده در شیفت جاری...") },
                        maxLines = 2,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Action Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("انصراف")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val hrs = hoursSpentText.toDoubleOrNull() ?: 1.0
                                onConfirm(progress, manpower, hrs, selectedStatus, remarks, issues)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IndustrialSteelBlue)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ثبت و ذخیره گزارش", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ====================================================================
// 5. MSP SYNCHRONIZATION & EOD REVIEW TAB (FOR PLANNER / ADMIN)
// ====================================================================

@Composable
fun MspSyncTab(
    dailyLogs: List<DailyWorkLogEntity>,
    exportedMspCsv: String?,
    preview: ParsedImportPreview?,
    currentUser: UserEntity?,
    onGenerateEodExport: () -> Unit,
    onImportUpdatedSchedule: (String) -> Unit,
    onLoadSampleGhadirData: () -> Unit,
    onCommitImport: () -> Unit,
    onCancelPreview: () -> Unit,
    onClearExport: () -> Unit,
    onCorrectDailyLog: ((logId: Long, newProgress: Int, newManpower: Int, newHours: Double, newRemarks: String, newIssues: String) -> Unit)? = null
) {
    var showImportDialog by remember { mutableStateOf(false) }
    var selectedLogForCorrection by remember { mutableStateOf<DailyWorkLogEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Planner Workflow Guide Banner
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = IndustrialNavy,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountTree, contentDescription = null, tint = IndustrialAmber, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "چرخه همگام‌سازی پایان روز با MS Project",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "۱. بررسی درصدهای ثبت‌شده توسط سرپرستان اجرایی\n۲. استخراج خروجی پایان روز به فرمت MSP\n۳. به‌روزرسانی فایل mpp/اکسل و انتشار مجدد به اپلیکیشن کل نفرات",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Action Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onGenerateEodExport,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialEmerald),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("استخراج برای MSP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = { showImportDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("بارگذاری از MSP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Export Result Card (if generated)
        if (exportedMspCsv != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, IndustrialEmerald),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("خروجی CSV آماده ورود به MS Project:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            IconButton(onClick = onClearExport, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "بستن")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E1E1E),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = exportedMspCsv.take(500) + "\n... [ادامه داده‌ها موجود است]",
                                color = Color(0xFF81C784),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Import Preview Card (if active)
        if (preview != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = IndustrialPurple.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, IndustrialPurple),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "پیش‌نمایش واردات: ${preview.activeTasksCount} فعالیت شناسایی شد",
                            fontWeight = FontWeight.Bold,
                            color = IndustrialPurple
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "واحدهای شناسایی‌شده: ${preview.executiveUnitsFound.joinToString("، ")} • عمق WBS: ${preview.maxOutlineDepth} سطح",
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onCancelPreview) {
                                Text("انصراف")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = onCommitImport,
                                colors = ButtonDefaults.buttonColors(containerColor = IndustrialPurple)
                            ) {
                                Text("تأیید و انتشار سراسری در اپ")
                            }
                        }
                    }
                }
            }
        }

        // Daily Progress Log Register from Supervisors
        item {
            Text(
                text = "گزارشات کارکرد ثبت‌شده امروز توسط سرپرستان (${dailyLogs.size} رکورد)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (dailyLogs.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "هنوز گزارشی برای امروز ثبت نشده است. ناظرین و سرپرستان در بخش WBS کارکرد روزانه را ثبت می‌کنند.",
                        modifier = Modifier.padding(20.dp),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(dailyLogs, key = { it.id }) { log ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "واحد ${log.unitName} • ناظر: ${log.recordedByUserName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "پیشرفت: ${log.progressPercentage}% • نفرات: ${log.manpowerCount} نفر • ساعت کار: ${log.hoursSpent}h",
                                fontSize = 12.sp,
                                color = IndustrialSteelBlue
                            )
                            if (log.remarks.isNotBlank()) {
                                Text("توضیحات: ${log.remarks}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (log.syncedToMsp) IndustrialEmerald.copy(alpha = 0.15f) else IndustrialAmber.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (log.syncedToMsp) "${log.date} • همگام‌شده" else "${log.date} • در انتظار آپلود",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    color = if (log.syncedToMsp) IndustrialEmerald else IndustrialAmberDark,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            val isCreator = log.recordedByUserId == currentUser?.id
                            val isUnitHead = (currentUser?.role == "supervisor" || currentUser?.role == "unit_head") && (currentUser.unit == log.unitName)
                            val isAdminOrPlanner = currentUser?.role == "admin" || currentUser?.role == "planner"
                            val canEdit = (isCreator || isUnitHead || isAdminOrPlanner) && (!log.syncedToMsp || isAdminOrPlanner)

                            if (canEdit && onCorrectDailyLog != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedButton(
                                    onClick = { selectedLogForCorrection = log },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("اصلاح اطلاعات", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedLogForCorrection?.let { logToCorrect ->
        EditDailyWorkLogDialog(
            log = logToCorrect,
            onDismiss = { selectedLogForCorrection = null },
            onConfirm = { progress, manpower, hours, remarks, issues ->
                onCorrectDailyLog?.invoke(logToCorrect.id, progress, manpower, hours, remarks, issues)
                selectedLogForCorrection = null
            }
        )
    }

    if (showImportDialog) {
        var csvInput by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showImportDialog = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("واردات فایل به‌روزشده از MS Project", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("می‌توانید داده‌های نمونه فولاد غدیر نی‌ریز را بارگذاری کنید یا متن خروجی CSV را جای‌گذاری نمایید:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            onLoadSampleGhadirData()
                            showImportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialSteelBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("بارگذاری خودکار داده‌های واقعی کارخانه احیا")
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = csvInput,
                        onValueChange = { csvInput = it },
                        placeholder = { Text("یا چسباندن متن CSV فایل MS Project...") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showImportDialog = false }) {
                            Text("انصراف")
                        }
                        if (csvInput.isNotBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                onImportUpdatedSchedule(csvInput)
                                showImportDialog = false
                            }) {
                                Text("پردازش و پیش‌نمایش")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ====================================================================
// 6. SESSIONS, DECISIONS & NOTES TAB
// ====================================================================

@Composable
fun SessionsDecisionsTab(
    sessions: List<PlanningSessionEntity>,
    decisions: List<SessionDecisionEntity>,
    notes: List<SessionNoteEntity>,
    wbsItems: List<OversightItemEntity>,
    currentUser: UserEntity?,
    onAddSession: () -> Unit,
    onAddDecision: (Long) -> Unit,
    onAddNote: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Top Header & Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "جلسات هماهنگی و مصوبات اورهال",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ثبت صورت‌جلسات، تصمیمات اجرایی و پیگیری تکالیف واحدها",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (currentUser?.role == "admin" || currentUser?.role == "planner") {
                    Button(
                        onClick = onAddSession,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialSteelBlue)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ثبت جلسه جدید", fontSize = 11.sp)
                    }
                }
            }
        }

        // 2. High-Density KPI Metric Strip
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("کل جلسات", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${sessions.size}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = IndustrialSteelBlue)
                    }
                    Divider(modifier = Modifier.height(24.dp).width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("مصوبات ثبت‌شده", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${decisions.size}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = IndustrialEmerald)
                    }
                    Divider(modifier = Modifier.height(24.dp).width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("نکات و یادداشت‌ها", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${notes.size}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = IndustrialNavy)
                    }
                }
            }
        }

        if (sessions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("هیچ جلسه هماهنگی ثبت نشده است.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        items(sessions, key = { it.id }) { session ->
            val sessionDecs = decisions.filter { it.sessionId == session.id }
            val sessionNts = notes.filter { it.sessionId == session.id }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = CircleShape,
                                color = IndustrialSteelBlue.copy(alpha = 0.12f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Groups, contentDescription = null, tint = IndustrialSteelBlue, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(session.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${session.sessionDate} • ${session.location}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Summary Section
                    if (session.minutesSummary.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = session.minutesSummary,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    // Decisions List
                    if (sessionDecs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("مصوبات اجرایی جلسه:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = IndustrialEmerald)
                        Spacer(modifier = Modifier.height(4.dp))
                        sessionDecs.forEach { dec ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = IndustrialEmerald.copy(alpha = 0.08f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IndustrialEmerald, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${dec.decisionText}  [مسئول: ${dec.assignedUnit}]",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Notes List
                    if (sessionNts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("نکات و تذکرات مطرح‌شده:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = IndustrialNavy)
                        Spacer(modifier = Modifier.height(3.dp))
                        sessionNts.forEach { nt ->
                            Text(
                                text = "• ${nt.noteText} (${nt.authorName})",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (currentUser?.role == "admin" || currentUser?.role == "planner") {
                            OutlinedButton(
                                onClick = { onAddDecision(session.id) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ثبت مصوبه", fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        OutlinedButton(
                            onClick = { onAddNote(session.id) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("یادداشت", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

// ====================================================================
// 7. PROCUREMENT & SPARE PARTS TAB
// ====================================================================

@Composable
fun ProcurementTab(
    procurements: List<ProcurementRequestEntity>,
    wbsItems: List<OversightItemEntity>,
    currentUser: UserEntity?,
    onAddRequest: () -> Unit,
    onApproveRequest: (Long) -> Unit,
    onRejectRequest: (Long, String) -> Unit,
    onUpdateStatus: (Long, String) -> Unit
) {
    var selectedStatusFilter by remember { mutableStateOf("all") }

    val filteredProcurements = remember(procurements, selectedStatusFilter) {
        when (selectedStatusFilter) {
            "requested" -> procurements.filter { it.status == "requested" }
            "approved" -> procurements.filter { it.status == "approved" || it.status == "ordered" }
            "received" -> procurements.filter { it.status == "received" }
            "rejected" -> procurements.filter { it.status == "rejected" }
            else -> procurements
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Top Header & Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "مدیریت تأمین قطعات یدکی و اقلام بحرانی",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "پایش چرخه درخواست، سفارش‌گذاری و تحویل قطعات به کارگاه",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onAddRequest,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialSteelBlue)
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("درخواست تأمین", fontSize = 11.sp)
                }
            }
        }

        // 2. High-Density KPI Metric Strip
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("کل اقلام", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${procurements.size}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IndustrialSteelBlue)
                    }
                    Divider(modifier = Modifier.height(24.dp).width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("در انتظار تأیید", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${procurements.count { it.status == "requested" }}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IndustrialAmberDark)
                    }
                    Divider(modifier = Modifier.height(24.dp).width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("تأیید و خرید", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${procurements.count { it.status == "approved" || it.status == "ordered" }}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E88E5))
                    }
                    Divider(modifier = Modifier.height(24.dp).width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("تحویل سایت", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${procurements.count { it.status == "received" }}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IndustrialEmerald)
                    }
                }
            }
        }

        // 3. Status Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    Pair("all", "همه (${procurements.size})"),
                    Pair("requested", "در انتظار بررسی (${procurements.count { it.status == "requested" }})"),
                    Pair("approved", "سفارش و تأمین (${procurements.count { it.status == "approved" || it.status == "ordered" }})"),
                    Pair("received", "تحویل سایت (${procurements.count { it.status == "received" }})")
                ).forEach { (key, label) ->
                    val isSelected = selectedStatusFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatusFilter = key },
                        label = { Text(label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.height(28.dp)
                    )
                }
            }
        }

        if (filteredProcurements.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("هیچ درخواستی با این فیلتر یافت نشد.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        items(filteredProcurements, key = { it.id }) { req ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Row 1: Title & Status Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = CircleShape,
                                color = IndustrialSteelBlue.copy(alpha = 0.12f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Build,
                                        contentDescription = null,
                                        tint = IndustrialSteelBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(req.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (req.status) {
                                "approved", "ordered", "received" -> IndustrialEmerald.copy(alpha = 0.15f)
                                "rejected" -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                else -> IndustrialAmber.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = when (req.status) {
                                    "requested" -> "در انتظار تأیید"
                                    "approved" -> "تأیید شده"
                                    "ordered" -> "سفارش‌گذاری شده"
                                    "received" -> "تحویل انبار / سایت"
                                    "rejected" -> "رد شده"
                                    else -> req.status
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (req.status) {
                                    "approved", "ordered", "received" -> IndustrialEmerald
                                    "rejected" -> MaterialTheme.colorScheme.error
                                    else -> IndustrialAmberDark
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2: Structured Details Grid
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("نوع کالا / قطعه:", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(req.itemType, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                            Column {
                                Text("تعداد / مقدار:", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${req.quantity}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                            Column {
                                Text("برآورد هزینه:", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(req.estimatedCost, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                            Column {
                                Text("درخواست‌کننده:", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(req.requestedByUserName, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    // Row 3: 4-Stage Mini Tracker
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val stages = listOf("درخواست", "تأیید برنامه", "خرید/تأمین", "تحویل کارگاه")
                        val currentStageIdx = when (req.status) {
                            "requested" -> 0
                            "approved" -> 1
                            "ordered" -> 2
                            "received" -> 3
                            else -> 0
                        }

                        stages.forEachIndexed { idx, stageLabel ->
                            val isDone = idx <= currentStageIdx && req.status != "rejected"
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(
                                            if (isDone) IndustrialEmerald else MaterialTheme.colorScheme.outlineVariant,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isDone) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stageLabel,
                                    fontSize = 9.sp,
                                    color = if (isDone) IndustrialEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            if (idx < stages.size - 1) {
                                Divider(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp),
                                    color = if (idx < currentStageIdx && req.status != "rejected") IndustrialEmerald else MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }

                    // Row 4: Planner / Admin Action Buttons
                    if ((currentUser?.role == "admin" || currentUser?.role == "planner") && req.status == "requested") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { onRejectRequest(req.id, "عدم اولویت در این شیفت") },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(30.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text("رد درخواست", fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onApproveRequest(req.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = IndustrialEmerald),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(30.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text("تأیید خرید", fontSize = 10.sp)
                            }
                        }
                    } else if (req.status == "approved" && (currentUser?.role == "admin" || currentUser?.role == "planner")) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { onUpdateStatus(req.id, "received") },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = IndustrialEmerald),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("اعلام تحویل به سایت", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ====================================================================
// 8. AUDIT AND LOGS TAB
// ====================================================================

@Composable
fun AuditAndReportsTab(
    auditLogs: List<AuditLogEntity>,
    currentUser: UserEntity?,
    kpis: DashboardKpis
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "سوابق ممیزی و تاریخچه تغییرات سیستم (Audit Trail)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(auditLogs, key = { it.id }) { log ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${log.performedByUserName} (${getPersianRole(log.performedByUserRole)})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = log.remarks.ifBlank { "عملیات: ${log.action} بر روی ${log.entityType}" },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = log.timestamp.takeLast(11),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// ====================================================================
// 9. DIALOGS (ADD WBS ITEM, SESSIONS, DECISIONS, PROCUREMENTS)
// ====================================================================

@Composable
fun AddWbsItemDialog(
    existingItems: List<OversightItemEntity>,
    supervisors: List<UserEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        wbsCode: String,
        parentItemId: Long?,
        outlineLevel: Int,
        executiveUnit: String,
        generalArea: String,
        executionLocation: String,
        equipmentName: String,
        durationHours: Double,
        plannedStart: String,
        plannedEnd: String,
        prerequisiteIds: List<Long>,
        supervisorIds: List<Long>
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var wbsCode by remember { mutableStateOf("") }
    var executiveUnit by remember { mutableStateOf("مکانیک") }
    var generalArea by remember { mutableStateOf("سایت احیا") }
    var executionLocation by remember { mutableStateOf("محوطه عمومی") }
    var equipmentName by remember { mutableStateOf("تجهیزات فرآیندی") }
    var durationText by remember { mutableStateOf("8") }
    var plannedStart by remember { mutableStateOf("1404/10/12") }
    var plannedEnd by remember { mutableStateOf("1404/10/14") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("تعریف فعالیت جدید در WBS", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان فعالیت") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = wbsCode,
                        onValueChange = { wbsCode = it },
                        label = { Text("کد WBS") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = executiveUnit,
                        onValueChange = { executiveUnit = it },
                        label = { Text("واحد اجرایی (مکانیک، برق، ابزار دقیق...)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = equipmentName,
                        onValueChange = { equipmentName = it },
                        label = { Text("نام تجهیز") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it },
                        label = { Text("مدت زمان پیش‌بینی‌شده (ساعت)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("انصراف")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    val dur = durationText.toDoubleOrNull() ?: 8.0
                                    onConfirm(
                                        title,
                                        wbsCode.ifBlank { "100" },
                                        null,
                                        2,
                                        executiveUnit,
                                        generalArea,
                                        executionLocation,
                                        equipmentName,
                                        dur,
                                        plannedStart,
                                        plannedEnd,
                                        emptyList(),
                                        emptyList()
                                    )
                                }
                            }
                        ) {
                            Text("ثبت فعالیت")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditWbsItemDialog(
    item: OversightItemEntity,
    existingItems: List<OversightItemEntity>,
    supervisors: List<UserEntity>,
    allPrerequisites: List<ItemPrerequisiteEntity>,
    allAssignments: List<ItemAssignmentEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        updatedItem: OversightItemEntity,
        prerequisiteIds: List<Long>,
        supervisorIds: List<Long>
    ) -> Unit
) {
    var title by remember { mutableStateOf(item.title) }
    var wbsCode by remember { mutableStateOf(item.wbsCode) }
    var executiveUnit by remember { mutableStateOf(item.executiveUnit) }
    var generalArea by remember { mutableStateOf(item.generalArea) }
    var executionLocation by remember { mutableStateOf(item.executionLocation) }
    var equipmentName by remember { mutableStateOf(item.equipmentName) }
    var durationText by remember { mutableStateOf(item.durationHours.toString()) }
    var manpowerText by remember { mutableStateOf(item.manpowerCount.toString()) }
    var plannedStart by remember { mutableStateOf(item.plannedStartDate) }
    var plannedEnd by remember { mutableStateOf(item.plannedEndDate) }
    var status by remember { mutableStateOf(item.status) }
    var progress by remember { mutableStateOf(item.progressPercentage) }

    val initialPrereqs = remember { allPrerequisites.filter { it.itemId == item.id }.map { it.prerequisiteItemId }.toSet() }
    var selectedPrereqIds by remember { mutableStateOf(initialPrereqs) }

    val initialSupervisors = remember { allAssignments.filter { it.itemId == item.id }.map { it.supervisorUserId }.toSet() }
    var selectedSupervisorIds by remember { mutableStateOf(initialSupervisors) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = IndustrialSteelBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ویرایش اطلاعات فعالیت WBS", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان فعالیت *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = wbsCode,
                            onValueChange = { wbsCode = it },
                            label = { Text("کد WBS") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = executiveUnit,
                            onValueChange = { executiveUnit = it },
                            label = { Text("واحد اجرایی") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = equipmentName,
                            onValueChange = { equipmentName = it },
                            label = { Text("نام تجهیز") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = executionLocation,
                            onValueChange = { executionLocation = it },
                            label = { Text("محل اجرا") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = durationText,
                            onValueChange = { durationText = it },
                            label = { Text("مدت پیش‌بینی (ساعت)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = manpowerText,
                            onValueChange = { manpowerText = it },
                            label = { Text("نفرات") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = plannedStart,
                            onValueChange = { plannedStart = it },
                            label = { Text("تاریخ شروع") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = plannedEnd,
                            onValueChange = { plannedEnd = it },
                            label = { Text("تاریخ پایان") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("انصراف") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    val dur = durationText.toDoubleOrNull() ?: item.durationHours
                                    val mp = manpowerText.toIntOrNull() ?: item.manpowerCount
                                    val updated = item.copy(
                                        title = title,
                                        wbsCode = wbsCode,
                                        executiveUnit = executiveUnit,
                                        generalArea = generalArea,
                                        executionLocation = executionLocation,
                                        equipmentName = equipmentName,
                                        durationHours = dur,
                                        manpowerCount = mp,
                                        plannedStartDate = plannedStart,
                                        plannedEndDate = plannedEnd,
                                        status = status,
                                        progressPercentage = progress
                                    )
                                    onConfirm(updated, selectedPrereqIds.toList(), selectedSupervisorIds.toList())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndustrialSteelBlue)
                        ) {
                            Text("ذخیره تغییرات")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddPlanningSessionDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, date: String, location: String, minutes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("1404/10/12") }
    var location by remember { mutableStateOf("اتاق جلسات کنترل اورهال") }
    var minutes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("ثبت صورتجلسه هماهنگی جدید", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان جلسه") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("تاریخ جلسه") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("مکان / سالن") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = minutes, onValueChange = { minutes = it }, label = { Text("خلاصه مذاکرات") }, maxLines = 3, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("انصراف") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { if (title.isNotBlank()) onConfirm(title, date, location, minutes) }) {
                        Text("ثبت جلسه")
                    }
                }
            }
        }
    }
}

@Composable
fun AddDecisionDialog(
    wbsItems: List<OversightItemEntity>,
    onDismiss: () -> Unit,
    onConfirm: (decisionText: String, assignedUnit: String, itemId: Long?) -> Unit
) {
    var decisionText by remember { mutableStateOf("") }
    var assignedUnit by remember { mutableStateOf("مکانیک") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("ثبت مصوبه و تصمیم رسمی جلسه", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(value = decisionText, onValueChange = { decisionText = it }, label = { Text("متن مصوبه / اقدام مورد توافق") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = assignedUnit, onValueChange = { assignedUnit = it }, label = { Text("واحد مسئول پیگیری") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("انصراف") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { if (decisionText.isNotBlank()) onConfirm(decisionText, assignedUnit, null) }) {
                        Text("ثبت مصوبه")
                    }
                }
            }
        }
    }
}

@Composable
fun AddProcurementRequestDialog(
    wbsItems: List<OversightItemEntity>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, itemType: String, quantity: String, estimatedCost: String, itemId: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var itemType by remember { mutableStateOf("قطعه یدکی") }
    var quantity by remember { mutableStateOf("1 عدد") }
    var estimatedCost by remember { mutableStateOf("50,000,000 ریال") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("ثبت درخواست تأمین قطعه / پیمانکار", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("شرح قطعه یا خدمت درخواستی") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = itemType, onValueChange = { itemType = it }, label = { Text("نوع (قطعه یدکی، پیمانکار، ابزار)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("تعداد / مقدار") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = estimatedCost, onValueChange = { estimatedCost = it }, label = { Text("برآورد هزینه") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("انصراف") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (title.isNotBlank()) {
                            val targetItemId = wbsItems.firstOrNull()?.id ?: 1L
                            onConfirm(title, itemType, quantity, estimatedCost, targetItemId)
                        }
                    }) {
                        Text("ارسال درخواست")
                    }
                }
            }
        }
    }
}

@Composable
fun EditDailyWorkLogDialog(
    log: DailyWorkLogEntity,
    onDismiss: () -> Unit,
    onConfirm: (progress: Int, manpower: Int, hours: Double, remarks: String, issues: String) -> Unit
) {
    var progressSlider by remember { mutableStateOf(log.progressPercentage.toFloat()) }
    var manpowerText by remember { mutableStateOf(log.manpowerCount.toString()) }
    var hoursText by remember { mutableStateOf(log.hoursSpent.toString()) }
    var remarksText by remember { mutableStateOf(log.remarks) }
    var issuesText by remember { mutableStateOf(log.issues) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = IndustrialAmber.copy(alpha = 0.2f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.EditNote, contentDescription = null, tint = IndustrialAmberDark, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("اصلاح گزارش کارکرد روزانه", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("واحد ${log.unitName} • ناظر ثبت‌کننده: ${log.recordedByUserName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = IndustrialAmber.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, IndustrialAmber.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "توجه: تمامی اصلاحات، مقادیر قبلی و مشخصات کاربری که ویرایش را انجام می‌دهد در جدول ممیزی (Audit Trail) سیستم ثبت و ماندگار خواهد شد.",
                        fontSize = 10.sp,
                        color = IndustrialAmberDark,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // Progress Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("درصد پیشرفت اصلاح‌شده:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${progressSlider.toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = IndustrialSteelBlue)
                    }
                    Slider(
                        value = progressSlider,
                        onValueChange = { progressSlider = it },
                        valueRange = 0f..100f,
                        steps = 99
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = manpowerText,
                        onValueChange = { manpowerText = it.filter { c -> c.isDigit() } },
                        label = { Text("نفرات حاضر") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = hoursText,
                        onValueChange = { hoursText = it },
                        label = { Text("ساعت کارکرد") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = remarksText,
                    onValueChange = { remarksText = it },
                    label = { Text("شرح اقدامات / توضیحات اصلاحی") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = issuesText,
                    onValueChange = { issuesText = it },
                    label = { Text("موانع یا هماهنگی‌های لازم") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("انصراف") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val mp = manpowerText.toIntOrNull() ?: log.manpowerCount
                            val hrs = hoursText.toDoubleOrNull() ?: log.hoursSpent
                            onConfirm(progressSlider.toInt(), mp, hrs, remarksText, issuesText)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialSteelBlue)
                    ) {
                        Text("ثبت اصلاحیه و لاگ ممیزی")
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectReportViewerDialog(
    onDismiss: () -> Unit
) {
    val reportText = remember { com.example.util.ProjectReportGenerator.generateFullProjectReport() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = IndustrialSteelBlue, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("گزارش فنی و مستندات پروژه ساخت اپلیکیشن", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("اورهال مجتمع فولاد غدیر نی‌ریز • ریویژن ۵.۲", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E1E1E),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Text(
                            text = reportText,
                            color = Color(0xFFE0E0E0),
                            fontSize = 11.sp,
                            lineHeight = 18.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                        Text("بستن گزارش")
                    }
                }
            }
        }
    }
}

private fun getPersianRole(role: String): String = when (role) {
    "admin" -> "مدیر ارشد اورهال"
    "planner" -> "برنامه‌ریز اجرایی"
    "supervisor" -> "ناظر و سرپرست اجرایی"
    "unit_head" -> "مدیر / رئیس واحد"
    else -> role
}

private fun getPersianStatusLabel(status: String): String = when (status) {
    "pending" -> "در انتظار شروع"
    "in_progress" -> "در حال اجرا"
    "completed" -> "تکمیل شده"
    "blocked" -> "متوقف / دارای مانع"
    else -> status
}
