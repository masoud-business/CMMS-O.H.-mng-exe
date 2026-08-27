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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.OversightItemEntity
import com.example.data.entity.SafetyPermitEntity
import com.example.data.entity.UserEntity
import com.example.ui.theme.*

/**
 * تب مدیریت و پایش پرمیت‌های ایمنی و ایزولاسیون کارگاهی (HSE & LOTO Permits Desk)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SafetyPermitsTab(
    permits: List<SafetyPermitEntity>,
    wbsItems: List<OversightItemEntity>,
    currentUser: UserEntity?,
    onIssuePermit: (SafetyPermitEntity) -> Unit,
    onUpdatePermitStatus: (Long, String) -> Unit,
    onUpdateLotoStatus: (Long, String, String) -> Unit,
    onDeletePermit: (Long) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var showIssueDialog by remember { mutableStateOf(false) }
    var viewingPermitDetail by remember { mutableStateOf<SafetyPermitEntity?>(null) }

    val filteredPermits = remember(permits, selectedFilter, searchQuery) {
        permits.filter { permit ->
            val matchesFilter = when (selectedFilter) {
                "all" -> true
                "pending" -> permit.status == "pending"
                "issued" -> permit.status == "issued"
                "hot_work" -> permit.permitType.contains("گرم") || permit.permitType.contains("Hot", ignoreCase = true)
                "confined_space" -> permit.permitType.contains("بسته") || permit.permitType.contains("Confined", ignoreCase = true)
                "height" -> permit.permitType.contains("ارتفاع") || permit.permitType.contains("Height", ignoreCase = true)
                "loto" -> permit.requiresElectricalLoto || permit.permitType.contains("LOTO", ignoreCase = true) || permit.permitType.contains("ایزولاسیون")
                "suspended" -> permit.status == "suspended"
                else -> true
            }
            val matchesQuery = searchQuery.isBlank() ||
                    permit.permitNumber.contains(searchQuery, ignoreCase = true) ||
                    permit.permitType.contains(searchQuery, ignoreCase = true) ||
                    permit.equipmentName.contains(searchQuery, ignoreCase = true) ||
                    permit.executiveUnit.contains(searchQuery, ignoreCase = true) ||
                    permit.location.contains(searchQuery, ignoreCase = true)

            matchesFilter && matchesQuery
        }
    }

    val totalCount = permits.size
    val issuedCount = permits.count { it.status == "issued" }
    val pendingCount = permits.count { it.status == "pending" }
    val lotoCount = permits.count { it.requiresElectricalLoto }
    val suspendedCount = permits.count { it.status == "suspended" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // 1. HSE Banner & Quick Action
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0F766E).copy(alpha = 0.92f),
                                    Color(0xFF1E3A8A).copy(alpha = 0.95f)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.HealthAndSafety,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "میز صدور و نظارت پرمیت‌های ایمنی (HSE Desk)",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "پایش مجوزهای کار گرم، فضای بسته، ارتفاع و کارت قرمز LOTO برق",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Issue Permit Button (Available for HSE & Admin)
                            Button(
                                onClick = { showIssueDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.AddModerator, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("صدور پرمیت جدید", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // KPI Quick Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HseMiniStatCard("کل پرمیت‌ها", "$totalCount", Icons.Default.Description, Color(0xFF93C5FD))
                            HseMiniStatCard("صادر شده و معتبر", "$issuedCount", Icons.Default.CheckCircle, Color(0xFF86EFAC))
                            HseMiniStatCard("در انتظار تایید", "$pendingCount", Icons.Default.HourglassEmpty, Color(0xFFFDE047))
                            HseMiniStatCard("کارت قرمز LOTO", "$lotoCount", Icons.Default.Bolt, Color(0xFFFCA5A5))
                        }
                    }
                }
            }
        }

        // 2. Search & Filter Row
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                placeholder = { Text("جستجوی پرمیت بر اساس شماره، تجهیز، واحد یا موقعیت...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true
            )
        }

        // 3. Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val filters = listOf(
                    Triple("all", "همه پرمیت‌ها ($totalCount)", Icons.Default.AllInclusive),
                    Triple("pending", "در انتظار صدور ($pendingCount)", Icons.Default.PendingActions),
                    Triple("issued", "صادر شده ($issuedCount)", Icons.Default.Verified),
                    Triple("loto", "کارت قرمز و LOTO برق ($lotoCount)", Icons.Default.Bolt),
                    Triple("hot_work", "کار گرم و آتش", Icons.Default.LocalFireDepartment),
                    Triple("confined_space", "فضای بسته", Icons.Default.MeetingRoom),
                    Triple("height", "کار در ارتفاع", Icons.Default.Height),
                    Triple("suspended", "متوقف شده ($suspendedCount)", Icons.Default.Block)
                )

                items(filters) { (key, label, icon) ->
                    val isSelected = selectedFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = {
                            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(15.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndustrialEmerald.copy(alpha = 0.15f),
                            selectedLabelColor = IndustrialEmerald
                        )
                    )
                }
            }
        }

        // 4. List of Safety Permits
        if (filteredPermits.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShieldMoon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "هیچ پرمیت ایمنی با این شرایط یافت نشد.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredPermits, key = { it.id }) { permit ->
                SafetyPermitCard(
                    permit = permit,
                    currentUser = currentUser,
                    onViewDetail = { viewingPermitDetail = permit },
                    onApprove = { onUpdatePermitStatus(permit.id, "issued") },
                    onSuspend = { onUpdatePermitStatus(permit.id, "suspended") },
                    onClose = { onUpdatePermitStatus(permit.id, "closed") },
                    onConfirmLoto = { taggedBy ->
                        onUpdateLotoStatus(permit.id, "isolated_and_tagged", taggedBy)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialogs
    if (showIssueDialog) {
        IssueSafetyPermitDialog(
            wbsItems = wbsItems,
            currentUser = currentUser,
            onDismiss = { showIssueDialog = false },
            onConfirm = { newPermit ->
                onIssuePermit(newPermit)
                showIssueDialog = false
            }
        )
    }

    viewingPermitDetail?.let { permit ->
        SafetyPermitDetailDialog(
            permit = permit,
            onDismiss = { viewingPermitDetail = null }
        )
    }
}

@Composable
private fun HseMiniStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Text(title, color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp)
    }
}

/**
 * کارت نمایش پرمیت ایمنی در لیست
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SafetyPermitCard(
    permit: SafetyPermitEntity,
    currentUser: UserEntity?,
    onViewDetail: () -> Unit,
    onApprove: () -> Unit,
    onSuspend: () -> Unit,
    onClose: () -> Unit,
    onConfirmLoto: (String) -> Unit
) {
    val statusColor = when (permit.status) {
        "issued" -> Color(0xFF10B981) // سبز صادر شده
        "pending" -> Color(0xFFF59E0B) // زرد در انتظار
        "suspended" -> Color(0xFFEF4444) // قرمز متوقف
        "closed" -> Color(0xFF6B7280) // خاکستری بسته شده
        else -> Color.Gray
    }

    val statusText = when (permit.status) {
        "issued" -> "صادر شده و معتبر"
        "pending" -> "در انتظار صدور و تایید"
        "suspended" -> "متوقف به علت عدم رعایت ایمنی"
        "closed" -> "پایان فعالیت / خاتمه یافته"
        else -> permit.status
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f)),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Permit No, Type, Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = permit.permitNumber,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = permit.permitType,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = statusText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Task / Equipment & Location info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Engineering, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "واحد مجری: ${permit.executiveUnit} • تجهیز: ${permit.equipmentName.ifBlank { "کل مدار" }}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (permit.location.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "موقعیت: ${permit.location}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "تاریخ: ${permit.issueDate}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "اعتبار: ${permit.validHours} ساعت",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // High Risk & LOTO Tags Row
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Electrical LOTO Tag
                if (permit.requiresElectricalLoto) {
                    val isLotoDone = permit.electricalLotoStatus == "isolated_and_tagged"
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isLotoDone) Color(0xFFDC2626).copy(alpha = 0.12f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (isLotoDone) Color(0xFFDC2626) else Color(0xFFF59E0B))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = if (isLotoDone) Color(0xFFDC2626) else Color(0xFFD97706),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isLotoDone) "کارت قرمز برق نصب شد (${permit.electricalTaggedBy.ifBlank { "واحد برق" }})" else "نیازمند قطع برق و LOTO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLotoDone) Color(0xFFDC2626) else Color(0xFFD97706)
                            )
                        }
                    }
                }

                // Gas test
                if (permit.requiresGasTest) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.12f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Air, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (permit.gasTestResult.isNotBlank()) "تست گاز: ${permit.gasTestResult}" else "نیازمند تست گاز",
                                fontSize = 10.sp,
                                color = Color(0xFF0284C7)
                            )
                        }
                    }
                }

                // Scaffolding Tag
                if (permit.requiresScaffoldingTag) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF059669).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "تگ سبز داربست تایید شد",
                            fontSize = 10.sp,
                            color = Color(0xFF059669),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Fire watch
                if (permit.fireWatchRequired) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFEA580C).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "دیده‌بان آتش مستقر",
                            fontSize = 10.sp,
                            color = Color(0xFFEA580C),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onViewDetail,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مشاهده فرم استاندارد پرمیت", fontSize = 11.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Electrical LOTO direct confirmation for Electrical role
                    val isElectricalUser = currentUser?.unit?.contains("برق") == true || currentUser?.role == "admin"
                    if (permit.requiresElectricalLoto && permit.electricalLotoStatus != "isolated_and_tagged" && isElectricalUser) {
                        Button(
                            onClick = { onConfirmLoto(currentUser?.name ?: "واحد برق") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("نصب کارت قرمز برق", fontSize = 10.sp)
                        }
                    }

                    // HSE Approval / Action buttons
                    val isHseOrAdmin = currentUser?.role == "hse" || currentUser?.role == "admin" || currentUser?.unit?.contains("ایمنی") == true
                    if (isHseOrAdmin) {
                        if (permit.status == "pending") {
                            Button(
                                onClick = onApprove,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تایید صدور", fontSize = 10.sp)
                            }
                        } else if (permit.status == "issued") {
                            OutlinedButton(
                                onClick = onSuspend,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("توقف ایمنی", fontSize = 10.sp)
                            }
                            OutlinedButton(
                                onClick = onClose,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("بستن پرمیت", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * دیالوگ صدور پرمیت جدید کارگاهی با استانداردهای کامل ایمنی صنایع فولاد
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueSafetyPermitDialog(
    wbsItems: List<OversightItemEntity>,
    currentUser: UserEntity?,
    onDismiss: () -> Unit,
    onConfirm: (SafetyPermitEntity) -> Unit
) {
    var selectedItemId by remember { mutableStateOf(wbsItems.firstOrNull { it.active }?.id ?: 1L) }
    val selectedItem = remember(selectedItemId, wbsItems) { wbsItems.firstOrNull { it.id == selectedItemId } }

    var permitType by remember { mutableStateOf("کار گرم (Hot Work)") }
    var location by remember(selectedItem) { mutableStateOf(selectedItem?.executionLocation ?: "Core Area") }
    var equipmentName by remember(selectedItem) { mutableStateOf(selectedItem?.equipmentName ?: "") }
    var executiveUnit by remember(selectedItem) { mutableStateOf(selectedItem?.executiveUnit ?: "مکانیک") }
    var validHours by remember { mutableIntStateOf(8) }

    var requiresElectricalLoto by remember { mutableStateOf(false) }
    var requiresGasTest by remember { mutableStateOf(false) }
    var gasTestResult by remember { mutableStateOf("O2: 20.9% | CO: 0 ppm") }
    var requiresScaffoldingTag by remember { mutableStateOf(false) }
    var fireWatchRequired by remember { mutableStateOf(false) }
    var safetyPrecautions by remember { mutableStateOf("رعایت حریم ایمنی، استقرار کپسول آتش‌نشانی و استفاده از PPE الزامی است.") }
    var ppeRequirements by remember { mutableStateOf("کفش ایمنی، کلاه ایمنی، عینک حفاظتی و دستکش کار") }

    val permitTypesList = listOf(
        "کار گرم (Hot Work)",
        "فضای بسته (Confined Space)",
        "کار در ارتفاع (Height)",
        "ایزولاسیون مکانیکی و LOTO برق",
        "حفاری و خاکبرداری (Excavation)",
        "مجوز کار عمومی سرد (Cold Work)"
    )

    val randomNum = remember { (100..999).random() }
    val generatedPermitNo = "HSE-1404-$randomNum"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = Color(0xFF10B981))
                Spacer(modifier = Modifier.width(8.dp))
                Text("صدور پرمیت کارگاهی اورهال ($generatedPermitNo)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Task Selection
                item {
                    Text("انتخاب فعالیت مرتبط از WBS:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    var expandedTaskDropdown by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedTaskDropdown,
                        onExpandedChange = { expandedTaskDropdown = !expandedTaskDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedItem?.let { "${it.wbsCode} - ${it.title.take(35)}..." } ?: "انتخاب تسک",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTaskDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTaskDropdown,
                            onDismissRequest = { expandedTaskDropdown = false }
                        ) {
                            wbsItems.take(50).forEach { item ->
                                DropdownMenuItem(
                                    text = { Text("${item.wbsCode} • ${item.title}", fontSize = 11.sp) },
                                    onClick = {
                                        selectedItemId = item.id
                                        executiveUnit = item.executiveUnit
                                        equipmentName = item.equipmentName
                                        location = item.executionLocation
                                        expandedTaskDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 2. Permit Type Selection
                item {
                    Text("نوع پرمیت ایمنی:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(permitTypesList) { type ->
                            val isSelected = permitType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    permitType = type
                                    if (type.contains("بسته")) {
                                        requiresGasTest = true
                                        requiresElectricalLoto = true
                                    } else if (type.contains("گرم")) {
                                        fireWatchRequired = true
                                    } else if (type.contains("ارتفاع")) {
                                        requiresScaffoldingTag = true
                                    } else if (type.contains("LOTO") || type.contains("ایزولاسیون")) {
                                        requiresElectricalLoto = true
                                    }
                                },
                                label = { Text(type, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                // 3. Equipment & Location
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = equipmentName,
                            onValueChange = { equipmentName = it },
                            label = { Text("نام تجهیز", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("موقعیت دقیق", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                // 4. Executive Unit & Valid Hours
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = executiveUnit,
                            onValueChange = { executiveUnit = it },
                            label = { Text("واحد مجری", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = "$validHours ساعت",
                            onValueChange = { validHours = it.filter { c -> c.isDigit() }.toIntOrNull() ?: 8 },
                            label = { Text("مدت اعتبار", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                // 5. Checklists: Electrical LOTO, Gas Test, Scaffolding, Fire Watch
                item {
                    Text("چک‌لیست الزامات ایمنی و کنترلی:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            // LOTO
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = requiresElectricalLoto,
                                    onCheckedChange = { requiresElectricalLoto = it }
                                )
                                Text(
                                    text = "نیاز به قطع برق، نصب قفل و صدور کارت قرمز (Electrical LOTO)",
                                    fontSize = 11.sp,
                                    fontWeight = if (requiresElectricalLoto) FontWeight.Bold else FontWeight.Normal,
                                    color = if (requiresElectricalLoto) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Gas test
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = requiresGasTest,
                                    onCheckedChange = { requiresGasTest = it }
                                )
                                Text("نیاز به سنجش گاز محیطی (O2 / CO / H2S / LEL)", fontSize = 11.sp)
                            }

                            // Scaffolding Tag
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = requiresScaffoldingTag,
                                    onCheckedChange = { requiresScaffoldingTag = it }
                                )
                                Text("تایید تگ سبز داربست و مهاربندی ایمن", fontSize = 11.sp)
                            }

                            // Fire Watch
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = fireWatchRequired,
                                    onCheckedChange = { fireWatchRequired = it }
                                )
                                Text("استقرار دیده‌بان آتش و کپسول‌های CO2 و پودری", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // 6. Safety Precautions
                item {
                    OutlinedTextField(
                        value = safetyPrecautions,
                        onValueChange = { safetyPrecautions = it },
                        label = { Text("اقدامات کنترلی و احتیاطات HSE", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                // 7. PPE
                item {
                    OutlinedTextField(
                        value = ppeRequirements,
                        onValueChange = { ppeRequirements = it },
                        label = { Text("تجهیزات حفاظت فردی (PPE)", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val permit = SafetyPermitEntity(
                        itemId = selectedItemId,
                        oversightId = selectedItem?.oversightId ?: 1L,
                        permitNumber = generatedPermitNo,
                        permitType = permitType,
                        status = "issued",
                        executiveUnit = executiveUnit,
                        location = location,
                        equipmentName = equipmentName,
                        issueDate = "1404/10/14",
                        validHours = validHours,
                        issuedByUserId = currentUser?.id ?: 18L,
                        issuedByUserName = currentUser?.name ?: "سرپرست HSE",
                        requiresElectricalLoto = requiresElectricalLoto,
                        electricalLotoStatus = if (requiresElectricalLoto) "pending_isolation" else "not_required",
                        requiresGasTest = requiresGasTest,
                        gasTestResult = if (requiresGasTest) gasTestResult else "",
                        requiresScaffoldingTag = requiresScaffoldingTag,
                        fireWatchRequired = fireWatchRequired,
                        safetyPrecautions = safetyPrecautions,
                        ppeRequirements = ppeRequirements,
                        createdAt = "1404/10/14 10:00"
                    )
                    onConfirm(permit)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text("تایید و صدور نهایی پرمیت")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

/**
 * فرم نمایش جزئیات کامل استاندارد پرمیت ایمنی کارگاهی
 */
@Composable
fun SafetyPermitDetailDialog(
    permit: SafetyPermitEntity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF10B981))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("فرم استاندارد پرمیت ایمنی", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = permit.permitNumber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Table
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("مشخصات پرمیت کارگاهی:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("نوع مجوز: ${permit.permitType}", fontSize = 11.sp)
                            Text("واحد مجری: ${permit.executiveUnit}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("تجهیز: ${permit.equipmentName}", fontSize = 11.sp)
                            Text("موقعیت: ${permit.location}", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("تاریخ صدور: ${permit.issueDate}", fontSize = 11.sp)
                            Text("مدت اعتبار: ${permit.validHours} ساعت", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("صادرکننده: ${permit.issuedByUserName}", fontSize = 11.sp)
                    }
                }

                // Electrical LOTO Section
                if (permit.requiresElectricalLoto) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFDC2626).copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("وضعیت ایزولاسیون و کارت قرمز برق (Electrical LOTO):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFDC2626))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            val isIsolated = permit.electricalLotoStatus == "isolated_and_tagged"
                            Text(
                                text = if (isIsolated) "تغذیه الکتریکی به طور کامل قطع، فیدر مربوطه قفل و کارت قرمز با تایید ${permit.electricalTaggedBy} نصب گردید." else "هشدار: تا زمان قطع قطعی برق و نصب کارت قرمز توسط واحد برق، شروع کار ممنوع است.",
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Gas test & Precautions
                if (permit.requiresGasTest && permit.gasTestResult.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("نتایج پایش و تست گاز محیطی:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF0284C7))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(permit.gasTestResult, fontSize = 11.sp)
                        }
                    }
                }

                // Precautions & PPE
                Text("اقدامات کنترلی و الزامات HSE:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(permit.safetyPrecautions.ifBlank { "رعایت کلیه دستورالعمل‌های عمومی ایمنی کارگاهی الزامی است." }, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(4.dp))
                Text("تجهیزات حفاظت فردی مورد نیاز (PPE):", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(permit.ppeRequirements, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("بستن")
            }
        }
    )
}
