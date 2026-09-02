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
import com.example.data.entity.DigitalSignatureEntity
import com.example.data.entity.OversightItemEntity
import com.example.data.entity.SafetyPermitEntity
import com.example.data.entity.UserEntity
import com.example.data.service.DigitalSignatureService
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

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
    onRequestPermit: (SafetyPermitEntity) -> Unit = {},
    onApproveUnitHead: (Long) -> Unit = {},
    onRecordGasTest: (Long, String, String, String) -> Unit = { _, _, _, _ -> },
    onExtendPermit: (Long, Int) -> Unit = { _, _ -> },
    onSuspendPermit: (Long, String, String) -> Unit = { _, _, _ -> },
    onResumePermit: (Long) -> Unit = {},
    onUpdatePermitStatus: (Long, String) -> Unit,
    onUpdateLotoStatus: (Long, String, String) -> Unit,
    onDeletePermit: (Long) -> Unit,
    digitalSignatureService: DigitalSignatureService? = null,
    onSignDocumentWithToken: (stepOrder: Int, docType: String, docId: Long, enteredToken: String, genToken: String, expiry: Long, remarks: String) -> Unit = { _, _, _, _, _, _, _ -> }
) {
    var selectedFilter by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var showIssueDialog by remember { mutableStateOf(false) }
    var showWizardDialog by remember { mutableStateOf(false) }
    var showRequestDialog by remember { mutableStateOf(false) }
    var viewingPermitDetail by remember { mutableStateOf<SafetyPermitEntity?>(null) }
    var permitToSuspend by remember { mutableStateOf<SafetyPermitEntity?>(null) }
    var permitForGasTest by remember { mutableStateOf<SafetyPermitEntity?>(null) }
    var permitForExtension by remember { mutableStateOf<SafetyPermitEntity?>(null) }
    var permitForSigning by remember { mutableStateOf<Pair<SafetyPermitEntity, Int>?>(null) }
    var permitToConfirmAction by remember { mutableStateOf<Pair<SafetyPermitEntity, String>?>(null) }

    val filteredPermits = remember(permits, selectedFilter, searchQuery) {
        permits.filter { permit ->
            val matchesFilter = when (selectedFilter) {
                "all" -> true
                "pending" -> permit.status == "pending" || permit.status == "requested"
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
    val pendingCount = permits.count { it.status == "pending" || it.status == "requested" }
    val lotoCount = permits.count { it.requiresElectricalLoto }
    val suspendedCount = permits.count { it.status == "suspended" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. HSE Banner & Quick Action (Optimized compact height)
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0F766E).copy(alpha = 0.95f),
                                    Color(0xFF1E3A8A).copy(alpha = 0.95f)
                                )
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.HealthAndSafety,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "میز صدور و نظارت پرمیت‌های ایمنی (HSE Desk)",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "پایش مجوزهای کار گرم، فضای بسته، ارتفاع و کارت قرمز LOTO",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val isHseOrAdmin = currentUser?.role == "hse" || currentUser?.role == "admin" || currentUser?.unit?.contains("ایمنی") == true
                                if (isHseOrAdmin) {
                                    Button(
                                        onClick = { showWizardDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.AddModerator, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("صدور پرمیت HSE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = { showRequestDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("درخواست پرمیت", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // KPI Quick Stats Strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HseMiniStatCard("کل پرمیت‌ها", "$totalCount", Icons.Default.Description, Color(0xFF93C5FD))
                            HseMiniStatCard("معتبر", "$issuedCount", Icons.Default.CheckCircle, Color(0xFF86EFAC))
                            HseMiniStatCard("در انتظار", "$pendingCount", Icons.Default.HourglassEmpty, Color(0xFFFDE047))
                            HseMiniStatCard("متوقف HSE", "$suspendedCount", Icons.Default.Block, Color(0xFFFCA5A5))
                            HseMiniStatCard("LOTO برق", "$lotoCount", Icons.Default.Bolt, Color(0xFFFED7AA))
                        }
                    }
                }
            }
        }

        // 2. Compact Search & Filter Row
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                placeholder = { Text("جستجوی پرمیت بر اساس شماره، تجهیز، واحد یا موقعیت...", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true
            )
        }

        // 3. Category Filter Chips (Compact)
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val filters = listOf(
                    Triple("all", "همه ($totalCount)", Icons.Default.AllInclusive),
                    Triple("pending", "در انتظار ($pendingCount)", Icons.Default.PendingActions),
                    Triple("issued", "معتبر ($issuedCount)", Icons.Default.Verified),
                    Triple("suspended", "متوقف شده ($suspendedCount)", Icons.Default.Block),
                    Triple("loto", "LOTO برق ($lotoCount)", Icons.Default.Bolt),
                    Triple("hot_work", "کار گرم", Icons.Default.LocalFireDepartment),
                    Triple("confined_space", "فضای بسته", Icons.Default.MeetingRoom),
                    Triple("height", "ارتفاع", Icons.Default.Height)
                )

                items(filters) { (key, label, icon) ->
                    val isSelected = selectedFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = {
                            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        modifier = Modifier.height(30.dp),
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
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShieldMoon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "هیچ پرمیت ایمنی با این شرایط یافت نشد.",
                            fontSize = 12.sp,
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
                    onSuspend = { permitToSuspend = permit },
                    onResume = { onResumePermit(permit.id) },
                    onClose = { onUpdatePermitStatus(permit.id, "closed") },
                    onConfirmLoto = { taggedBy ->
                        onUpdateLotoStatus(permit.id, "isolated_and_tagged", taggedBy)
                    },
                    onApproveUnitHead = { onApproveUnitHead(permit.id) },
                    onRecordGasTest = { permitForGasTest = permit },
                    onExtend = { permitForExtension = permit },
                    onOpenSignatureDialog = { stepOrder ->
                        permitForSigning = permit to stepOrder
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dialogs
    if (showWizardDialog) {
        PermitIssuanceWizardDialog(
            wbsItems = wbsItems,
            currentUser = currentUser,
            onDismiss = { showWizardDialog = false },
            onConfirm = { newPermit ->
                onIssuePermit(newPermit)
                showWizardDialog = false
            }
        )
    }

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

    if (showRequestDialog) {
        RequestSafetyPermitDialog(
            wbsItems = wbsItems,
            currentUser = currentUser,
            onDismiss = { showRequestDialog = false },
            onConfirm = { requestedPermit ->
                onRequestPermit(requestedPermit)
                showRequestDialog = false
            }
        )
    }

    permitToSuspend?.let { permit ->
        SuspendSafetyPermitDialog(
            permit = permit,
            onDismiss = { permitToSuspend = null },
            onConfirm = { reason, details ->
                onSuspendPermit(permit.id, reason, details)
                permitToSuspend = null
            }
        )
    }

    permitForGasTest?.let { permit ->
        RecordGasTestDialog(
            permit = permit,
            currentUser = currentUser,
            onDismiss = { permitForGasTest = null },
            onConfirm = { o2, co, lel ->
                onRecordGasTest(permit.id, o2, co, lel)
                permitForGasTest = null
            }
        )
    }

    permitForExtension?.let { permit ->
        ExtendSafetyPermitDialog(
            permit = permit,
            onDismiss = { permitForExtension = null },
            onConfirm = { hours ->
                onExtendPermit(permit.id, hours)
                permitForExtension = null
            }
        )
    }

    viewingPermitDetail?.let { permit ->
        SafetyPermitDetailDialog(
            permit = permit,
            currentUser = currentUser,
            digitalSignatureService = digitalSignatureService,
            onOpenSignDialog = { stepOrder ->
                permitForSigning = permit to stepOrder
            },
            onDismiss = { viewingPermitDetail = null }
        )
    }

    permitForSigning?.let { (permit, stepOrder) ->
        if (digitalSignatureService != null) {
            DynamicTokenSignatureDialog(
                documentType = "safety_permit",
                documentId = permit.id,
                documentTitle = "پرمیت ایمنی ${permit.permitNumber} (${permit.equipmentName})",
                stepOrder = stepOrder,
                currentUser = currentUser,
                service = digitalSignatureService,
                onDismiss = { permitForSigning = null },
                onConfirmSign = { order, enteredToken, genToken, expiry, remarks ->
                    onSignDocumentWithToken(order, "safety_permit", permit.id, enteredToken, genToken, expiry, remarks)
                    permitForSigning = null
                }
            )
        }
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
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Text(title, color = Color.White.copy(alpha = 0.75f), fontSize = 9.sp)
    }
}

/**
 * تایمر زنده شمارش معکوس اعتبار زمانی پرمیت ایمنی (Live Countdown Timer)
 */
@Composable
fun PermitLiveTimer(
    expiryTimestamp: Long,
    modifier: Modifier = Modifier
) {
    if (expiryTimestamp <= 0L) return

    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(expiryTimestamp) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val diff = expiryTimestamp - currentTime
    val isExpired = diff <= 0
    val totalSeconds = if (isExpired) 0L else (diff / 1000L)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (isExpired) Color(0xFFEF4444).copy(alpha = 0.12f) else Color(0xFF059669).copy(alpha = 0.12f),
        border = BorderStroke(1.dp, if (isExpired) Color(0xFFEF4444).copy(alpha = 0.4f) else Color(0xFF059669).copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = if (isExpired) Icons.Default.TimerOff else Icons.Default.Timer,
                contentDescription = null,
                tint = if (isExpired) Color(0xFFEF4444) else Color(0xFF059669),
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = if (isExpired) "اعتبار منقضی شد" else String.format(Locale.US, "اعتبار زنده: %02d:%02d:%02d", hours, minutes, seconds),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isExpired) Color(0xFFEF4444) else Color(0xFF059669)
            )
        }
    }
}

/**
 * کارت متراکم و بهینه پرمیت ایمنی
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SafetyPermitCard(
    permit: SafetyPermitEntity,
    currentUser: UserEntity?,
    onViewDetail: () -> Unit,
    onApprove: () -> Unit,
    onSuspend: () -> Unit,
    onResume: () -> Unit,
    onClose: () -> Unit,
    onConfirmLoto: (String) -> Unit,
    onApproveUnitHead: () -> Unit = {},
    onRecordGasTest: () -> Unit = {},
    onExtend: () -> Unit = {},
    onOpenSignatureDialog: (stepOrder: Int) -> Unit = {}
) {
    val statusColor = when (permit.status) {
        "issued" -> Color(0xFF10B981) // سبز صادر شده
        "pending", "requested" -> Color(0xFFF59E0B) // زرد در انتظار
        "suspended" -> Color(0xFFEF4444) // قرمز متوقف
        "closed" -> Color(0xFF6B7280) // خاکستری بسته شده
        else -> Color.Gray
    }

    val statusText = when (permit.status) {
        "issued" -> "صادر شده و معتبر"
        "pending", "requested" -> "در انتظار تایید HSE"
        "suspended" -> "متوقف توسط ایمنی"
        "closed" -> "پایان فعالیت / بسته"
        else -> permit.status
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header Row: Permit No, Type, Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = statusColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = permit.permitNumber,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = permit.permitType,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Unit, Equipment & Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Engineering, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "واحد: ${permit.executiveUnit} • تجهیز: ${permit.equipmentName.ifBlank { "عمومی" }}",
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "اعتبار: ${permit.validHours}h | ${permit.issueDate}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (permit.expiryTimestamp > 0L && (permit.status == "issued" || permit.status == "extended")) {
                        Spacer(modifier = Modifier.width(6.dp))
                        PermitLiveTimer(expiryTimestamp = permit.expiryTimestamp)
                    }
                }
            }

            // Suspended Reason Banner (if applicable)
            if (permit.status == "suspended" && !permit.stopReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "علت توقف: ${permit.stopReason} ${permit.stopDetails?.let { "($it)" } ?: ""}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // High Risk & LOTO Tags Row (Compact)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (permit.unitHeadApproved) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "تایید رئیس واحد: ${permit.unitHeadApprovedBy.ifBlank { "انجام شد" }}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                } else if (permit.status == "requested" || permit.status == "pending") {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "در انتظار تایید رئیس واحد",
                            fontSize = 9.sp,
                            color = Color(0xFFD97706),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                if (permit.extensionCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF8B5CF6).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "تمدید (${permit.extensionCount} بار)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B5CF6),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                if (permit.requiresElectricalLoto) {
                    val isLotoDone = permit.electricalLotoStatus == "isolated_and_tagged"
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isLotoDone) Color(0xFFDC2626).copy(alpha = 0.1f) else Color(0xFFF59E0B).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, if (isLotoDone) Color(0xFFDC2626) else Color(0xFFF59E0B))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = if (isLotoDone) Color(0xFFDC2626) else Color(0xFFD97706),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = if (isLotoDone) "کارت قرمز برق نصب شد" else "نیازمند کارت قرمز LOTO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLotoDone) Color(0xFFDC2626) else Color(0xFFD97706)
                            )
                        }
                    }
                }

                if (permit.requiresGasTest) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = if (permit.gasTestResult.isNotBlank()) "تست گاز: ${permit.gasTestResult}" else "نیازمند تست گاز",
                            fontSize = 9.sp,
                            color = Color(0xFF0284C7),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                if (permit.requiresScaffoldingTag) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF059669).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "تگ سبز داربست",
                            fontSize = 9.sp,
                            color = Color(0xFF059669),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                if (permit.fireWatchRequired) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFEA580C).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "دیده‌بان آتش",
                            fontSize = 9.sp,
                            color = Color(0xFFEA580C),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons Row (Compact and functional)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onViewDetail,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("مشاهده جزئیات", fontSize = 10.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val isElectricalUser = currentUser?.unit?.contains("برق") == true || currentUser?.role == "admin"
                    if (permit.requiresElectricalLoto && permit.electricalLotoStatus != "isolated_and_tagged" && isElectricalUser) {
                        Button(
                            onClick = { onConfirmLoto(currentUser?.name ?: "واحد برق") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("نصب کارت قرمز", fontSize = 9.sp)
                        }
                    }

                    // Unit Head approval button
                    val isUnitHead = currentUser?.role == "unit_head" || currentUser?.role == "admin" || (currentUser?.unit != null && currentUser.unit.contains(permit.executiveUnit))
                    if (!permit.unitHeadApproved && (permit.status == "requested" || permit.status == "pending") && isUnitHead) {
                        Button(
                            onClick = onApproveUnitHead,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("تایید واحد", fontSize = 9.sp)
                        }
                    }

                    val isHseOrAdmin = currentUser?.role == "hse" || currentUser?.role == "admin" || currentUser?.unit?.contains("ایمنی") == true
                    if (isHseOrAdmin) {
                        if (permit.status == "pending" || permit.status == "requested") {
                            Button(
                                onClick = onApprove,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("تایید صدور", fontSize = 9.sp)
                            }
                        } else if (permit.status == "issued" || permit.status == "extended") {
                            if (permit.requiresGasTest) {
                                OutlinedButton(
                                    onClick = onRecordGasTest,
                                    contentPadding = PaddingValues(horizontal = 5.dp, vertical = 0.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Icon(Icons.Default.Air, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF0284C7))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("تست گاز", fontSize = 9.sp, color = Color(0xFF0284C7))
                                }
                            }
                            OutlinedButton(
                                onClick = onExtend,
                                contentPadding = PaddingValues(horizontal = 5.dp, vertical = 0.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Icon(Icons.Default.Update, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("تمدید", fontSize = 9.sp)
                            }
                            OutlinedButton(
                                onClick = onSuspend,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                contentPadding = PaddingValues(horizontal = 5.dp, vertical = 0.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("توقف", fontSize = 9.sp)
                            }
                            OutlinedButton(
                                onClick = onClose,
                                contentPadding = PaddingValues(horizontal = 5.dp, vertical = 0.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("بستن", fontSize = 9.sp)
                            }
                        } else if (permit.status == "suspended") {
                            Button(
                                onClick = onResume,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("رفع توقف", fontSize = 9.sp)
                            }
                        }
                    }

                    // Digital signature button
                    OutlinedButton(
                        onClick = { onOpenSignatureDialog(1) },
                        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("امضاء", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

/**
 * دیالوگ استاندارد توقف پرمیت ایمنی با انتخاب دلایل استاندارد و توضیحات
 */
@Composable
fun SuspendSafetyPermitDialog(
    permit: SafetyPermitEntity,
    onDismiss: () -> Unit,
    onConfirm: (reason: String, details: String) -> Unit
) {
    val standardReasons = listOf(
        "عدم نصب قفل و کارت قرمز LOTO توسط واحد برق",
        "عدم استفاده از تجهیزات حفاظت فردی (PPE)",
        "عدم استقرار دیده‌بان آتش یا کمبود کپسول اطفاء حریق در کار گرم",
        "غلظت گازهای خطرناک یا کمبود اکسیژن در محیط",
        "عدم وجود تگ سبز داربست یا مهاربندی غیراستاندارد",
        "حضور افراد متفرقه و عدم رعایت حریم ایمنی",
        "شرایط جوی نامساعد یا خطر باد و باران شدید",
        "سایر موارد نقض ایمنی (توضیحات ذیل)"
    )

    var selectedReason by remember { mutableStateOf(standardReasons.first()) }
    var details by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(28.dp))
        },
        title = {
            Text(
                text = "دستور توقف فوری کار توسط HSE (${permit.permitNumber})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "فعالیت: ${permit.equipmentName} (${permit.executiveUnit})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Text("انتخاب دلیل استاندارد توقف فعالیت:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    standardReasons.forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { selectedReason = reason }
                                .padding(vertical = 3.dp)
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(reason, fontSize = 11.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("توضیحات تکمیلی و شروط رفع توقف", fontSize = 10.sp) },
                    placeholder = { Text("مثال: الزامات رفع نقص تا شیفت بعد...", fontSize = 10.sp) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedReason, details) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Text("تایید توقف فوری فعالیت", fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", fontSize = 11.sp)
            }
        }
    )
}

/**
 * دیالوگ درخواست پرمیت ایمنی از سوی واحدهای اجرایی
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestSafetyPermitDialog(
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
    var executiveUnit by remember(selectedItem) { mutableStateOf(currentUser?.unit ?: selectedItem?.executiveUnit ?: "مکانیک") }
    var validHours by remember { mutableIntStateOf(8) }
    var requiresElectricalLoto by remember { mutableStateOf(false) }
    var requiresGasTest by remember { mutableStateOf(false) }

    val randomNum = remember { (100..999).random() }
    val generatedPermitNo = "REQ-1404-$randomNum"

    val permitTypesList = listOf(
        "کار گرم (Hot Work)",
        "فضای بسته (Confined Space)",
        "کار در ارتفاع (Height)",
        "ایزولاسیون مکانیکی و LOTO برق",
        "حفاری و خاکبرداری (Excavation)",
        "مجوز کار عمومی سرد (Cold Work)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PostAdd, contentDescription = null, tint = Color(0xFF0284C7))
                Spacer(modifier = Modifier.width(6.dp))
                Text("درخواست صدور پرمیت ایمنی کارگاهی", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("انتخاب فعالیت مرتبط از WBS:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                var expandedTaskDropdown by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedTaskDropdown,
                    onExpandedChange = { expandedTaskDropdown = !expandedTaskDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedItem?.let { "${it.wbsCode} - ${it.title.take(30)}..." } ?: "انتخاب فعالیت",
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
                        wbsItems.take(40).forEach { item ->
                            DropdownMenuItem(
                                text = { Text("${item.wbsCode} • ${item.title}", fontSize = 11.sp) },
                                onClick = {
                                    selectedItemId = item.id
                                    equipmentName = item.equipmentName
                                    location = item.executionLocation
                                    expandedTaskDropdown = false
                                }
                            )
                        }
                    }
                }

                Text("نوع پرمیت درخواستی:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(permitTypesList) { type ->
                        FilterChip(
                            selected = permitType == type,
                            onClick = {
                                permitType = type
                                if (type.contains("بسته")) {
                                    requiresGasTest = true
                                    requiresElectricalLoto = true
                                } else if (type.contains("LOTO") || type.contains("ایزولاسیون")) {
                                    requiresElectricalLoto = true
                                }
                            },
                            label = { Text(type, fontSize = 9.sp) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = requiresElectricalLoto,
                        onCheckedChange = { requiresElectricalLoto = it }
                    )
                    Text("نیاز به ایزولاسیون و قطع برق (LOTO)", fontSize = 11.sp)
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
                        status = "requested",
                        executiveUnit = executiveUnit,
                        location = location,
                        equipmentName = equipmentName,
                        issueDate = "1404/10/14",
                        validHours = validHours,
                        requestedByUserId = currentUser?.id ?: 0L,
                        requestedByUserName = currentUser?.name ?: "",
                        requiresElectricalLoto = requiresElectricalLoto,
                        electricalLotoStatus = if (requiresElectricalLoto) "pending_isolation" else "not_required",
                        requiresGasTest = requiresGasTest,
                        gasTestResultO2 = "",
                        gasTestResultCO = "",
                        gasTestResultLEL = "",
                        expiryTimestamp = 0L,
                        safetyPrecautions = "رعایت کلیه الزامات ایمنی الزامی است.",
                        ppeRequirements = "کفش، کلاه، عینک حفاظتی و دستکش کار",
                        createdAt = "1404/10/14 10:00"
                    )
                    onConfirm(permit)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
            ) {
                Text("ارسال درخواست به HSE", fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", fontSize = 11.sp)
            }
        }
    )
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
                Text("صدور پرمیت کارگاهی اورهال ($generatedPermitNo)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Task Selection
                item {
                    Text("انتخاب فعالیت مرتبط از WBS:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
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
                    Spacer(modifier = Modifier.height(2.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
                                label = { Text(type, fontSize = 9.sp) },
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }

                // 3. Equipment & Location
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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

                // 5. Checklists
                item {
                    Text("چک‌لیست الزامات ایمنی و کنترلی:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = requiresElectricalLoto, onCheckedChange = { requiresElectricalLoto = it })
                                Text("قطع برق، قفل و صدور کارت قرمز (Electrical LOTO)", fontSize = 10.sp, fontWeight = if (requiresElectricalLoto) FontWeight.Bold else FontWeight.Normal, color = if (requiresElectricalLoto) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = requiresGasTest, onCheckedChange = { requiresGasTest = it })
                                Text("سنجش گاز محیطی (O2 / CO / H2S / LEL)", fontSize = 10.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = requiresScaffoldingTag, onCheckedChange = { requiresScaffoldingTag = it })
                                Text("تایید تگ سبز داربست و مهاربندی ایمن", fontSize = 10.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = fireWatchRequired, onCheckedChange = { fireWatchRequired = it })
                                Text("استقرار دیده‌بان آتش و کپسول اطفاء", fontSize = 10.sp)
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
                        expiryTimestamp = System.currentTimeMillis() + (validHours * 3600 * 1000L),
                        requiresGasTest = requiresGasTest,
                        gasTestResultO2 = if (requiresGasTest) "20.9%" else "",
                        gasTestResultCO = if (requiresGasTest) "0 ppm" else "",
                        gasTestResultLEL = if (requiresGasTest) "0%" else "",
                        gasTesterName = if (requiresGasTest) (currentUser?.name ?: "HSE") else "",
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
                Text("تایید و صدور نهایی پرمیت", fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", fontSize = 11.sp)
            }
        }
    )
}

/**
 * دیالوگ ثبت و مانیتورینگ تست گاز محیطی
 */
@Composable
fun RecordGasTestDialog(
    permit: SafetyPermitEntity,
    currentUser: UserEntity?,
    onDismiss: () -> Unit,
    onConfirm: (o2: String, co: String, lel: String) -> Unit
) {
    var o2 by remember { mutableStateOf(permit.gasTestResultO2.ifBlank { "20.9%" }) }
    var co by remember { mutableStateOf(permit.gasTestResultCO.ifBlank { "0 ppm" }) }
    var lel by remember { mutableStateOf(permit.gasTestResultLEL.ifBlank { "0%" }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Air, contentDescription = null, tint = Color(0xFF0284C7))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("ثبت رسمی تست گاز محیطی", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("پرمیت: ${permit.permitNumber} • ${permit.equipmentName}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0284C7).copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ثبت توسط: ${currentUser?.name ?: "کارشناس HSE"}\nحدود مجاز استاندارد: O2 بین ۱۹.۵ تا ۲۳.۵ درصد | CO کمتر از ۲۵ ppm | LEL زیر ۱۰ درصد",
                        fontSize = 9.sp,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.padding(8.dp),
                        lineHeight = 13.sp
                    )
                }

                OutlinedTextField(
                    value = o2,
                    onValueChange = { o2 = it },
                    label = { Text("میزان اکسیژن (O2 %)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = co,
                    onValueChange = { co = it },
                    label = { Text("منوکسید کربن (CO ppm)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lel,
                    onValueChange = { lel = it },
                    label = { Text("گازهای قابل اشتعال (LEL %)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(o2, co, lel) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
            ) {
                Text("ثبت در سامانه HSE", fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف", fontSize = 11.sp) }
        }
    )
}

/**
 * دیالوگ تمدید ساعت اعتبار پرمیت ایمنی
 */
@Composable
fun ExtendSafetyPermitDialog(
    permit: SafetyPermitEntity,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var hours by remember { mutableIntStateOf(8) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Update, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("تمدید اعتبار پرمیت ایمنی", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("${permit.permitNumber} • ${permit.equipmentName}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("مدت تمدید اعتبار جدید را انتخاب نمایید:", fontSize = 11.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(4, 8, 12, 24).forEach { h ->
                        FilterChip(
                            selected = hours == h,
                            onClick = { hours = h },
                            label = { Text("$h ساعت", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("اعتبار فعلی: ${permit.validHours} ساعت", fontSize = 10.sp)
                        Text("دفعات تمدید قبلی: ${permit.extensionCount} بار", fontSize = 10.sp)
                        Text("مجموع اعتبار پس از تمدید: ${permit.validHours + hours} ساعت", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(hours) }) {
                Text("تایید تمدید", fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف", fontSize = 11.sp) }
        }
    )
}

/**
 * ویزارد چندمرحله‌ای صدور پرمیت ایمنی (Permit Issuance Wizard)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermitIssuanceWizardDialog(
    wbsItems: List<OversightItemEntity>,
    currentUser: UserEntity?,
    onDismiss: () -> Unit,
    onConfirm: (SafetyPermitEntity) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    var selectedItemId by remember { mutableStateOf(wbsItems.firstOrNull { it.active }?.id ?: 1L) }
    val selectedItem = remember(selectedItemId, wbsItems) { wbsItems.firstOrNull { it.id == selectedItemId } }

    var permitType by remember { mutableStateOf("کار گرم (Hot Work)") }
    var location by remember(selectedItem) { mutableStateOf(selectedItem?.executionLocation ?: "ناحیه کوره احیا") }
    var equipmentName by remember(selectedItem) { mutableStateOf(selectedItem?.equipmentName ?: "ریفورمر و لوله‌های گاز") }
    var executiveUnit by remember(selectedItem) { mutableStateOf(selectedItem?.executiveUnit ?: "مکانیک") }
    var validHours by remember { mutableIntStateOf(8) }

    var requiresElectricalLoto by remember { mutableStateOf(false) }
    var lotoLockNumber by remember { mutableStateOf("LCK-204") }
    var requiresGasTest by remember { mutableStateOf(false) }
    var gasO2 by remember { mutableStateOf("20.9%") }
    var gasCO by remember { mutableStateOf("0 ppm") }
    var gasLEL by remember { mutableStateOf("0%") }
    var requiresScaffoldingTag by remember { mutableStateOf(false) }
    var fireWatchRequired by remember { mutableStateOf(false) }
    var safetyPrecautions by remember { mutableStateOf("رعایت حریم ایمنی، استقرار کپسول آتش‌نشانی، تهویه مداوم و ایزولاسیون خطوط الزامی است.") }
    var ppeRequirements by remember { mutableStateOf("کفش ایمنی، کلاه ایمنی، شیلد جوشکاری، دستکش چرمی و هارنس ایمنی") }

    val randomNum = remember { (100..999).random() }
    val generatedPermitNo = remember { "HSE-1404-$randomNum" }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("ویزارد جامع صدور پرمیت ایمنی اورهال", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("شماره رسمی: $generatedPermitNo • گام $step از ۳", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Stepper Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        1 to "مشخصات تسک",
                        2 to "ایزولاسیون و گاز",
                        3 to "چک‌لیست و صدور"
                    ).forEach { (sNum, sLabel) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (step >= sNum) Color(0xFF10B981) else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("$sNum", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = sLabel,
                                fontSize = 9.sp,
                                fontWeight = if (step == sNum) FontWeight.Bold else FontWeight.Normal,
                                color = if (step == sNum) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                when (step) {
                    1 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("انتخاب فعالیت WBS:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            var expandedTask by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expandedTask,
                                onExpandedChange = { expandedTask = !expandedTask }
                            ) {
                                OutlinedTextField(
                                    value = selectedItem?.let { "${it.wbsCode} - ${it.title.take(35)}" } ?: "انتخاب تسک",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTask) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedTask,
                                    onDismissRequest = { expandedTask = false }
                                ) {
                                    wbsItems.take(50).forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text("${item.wbsCode} • ${item.title}", fontSize = 10.sp) },
                                            onClick = {
                                                selectedItemId = item.id
                                                executiveUnit = item.executiveUnit
                                                equipmentName = item.equipmentName
                                                location = item.executionLocation
                                                expandedTask = false
                                            }
                                        )
                                    }
                                }
                            }

                            Text("نوع پرمیت:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            val pTypes = listOf("کار گرم (Hot Work)", "فضای بسته (Confined Space)", "کار در ارتفاع (Height)", "ایزولاسیون مکانیکی و LOTO", "حفاری و خاکبرداری", "مجوز کار سرد عمومی")
                            var expandedType by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = expandedType, onExpandedChange = { expandedType = !expandedType }) {
                                OutlinedTextField(
                                    value = permitType,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                                ExposedDropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                                    pTypes.forEach { t ->
                                        DropdownMenuItem(text = { Text(t, fontSize = 11.sp) }, onClick = { permitType = t; expandedType = false })
                                    }
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = executiveUnit,
                                    onValueChange = { executiveUnit = it },
                                    label = { Text("واحد مجری", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = equipmentName,
                                    onValueChange = { equipmentName = it },
                                    label = { Text("نام تجهیز", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            OutlinedTextField(
                                value = location,
                                onValueChange = { location = it },
                                label = { Text("موقعیت کارگاهی", fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Text("مدت اعتبار اولیه:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(4, 8, 12, 24).forEach { h ->
                                    FilterChip(
                                        selected = validHours == h,
                                        onClick = { validHours = h },
                                        label = { Text("$h ساعت", fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                    2 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFDC2626).copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("ایزولاسیون برقی (Electrical LOTO)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                        }
                                        Switch(checked = requiresElectricalLoto, onCheckedChange = { requiresElectricalLoto = it })
                                    }
                                    if (requiresElectricalLoto) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = lotoLockNumber,
                                            onValueChange = { lotoLockNumber = it },
                                            label = { Text("شماره قفل و تگ LOTO", fontSize = 10.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0284C7).copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Air, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("سنجش و تست گاز محیطی", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                        }
                                        Switch(checked = requiresGasTest, onCheckedChange = { requiresGasTest = it })
                                    }
                                    if (requiresGasTest) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            OutlinedTextField(value = gasO2, onValueChange = { gasO2 = it }, label = { Text("O2 %", fontSize = 9.sp) }, modifier = Modifier.weight(1f), singleLine = true)
                                            OutlinedTextField(value = gasCO, onValueChange = { gasCO = it }, label = { Text("CO ppm", fontSize = 9.sp) }, modifier = Modifier.weight(1f), singleLine = true)
                                            OutlinedTextField(value = gasLEL, onValueChange = { gasLEL = it }, label = { Text("LEL %", fontSize = 9.sp) }, modifier = Modifier.weight(1f), singleLine = true)
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("نیاز به تگ سبز داربست بندی", fontSize = 11.sp)
                                Switch(checked = requiresScaffoldingTag, onCheckedChange = { requiresScaffoldingTag = it })
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("استقرار دیده‌بان آتش (Fire Watch)", fontSize = 11.sp)
                                Switch(checked = fireWatchRequired, onCheckedChange = { fireWatchRequired = it })
                            }
                        }
                    }
                    3 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = safetyPrecautions,
                                onValueChange = { safetyPrecautions = it },
                                label = { Text("دستورالعمل‌ها و تدابیر احتیاطی HSE", fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                            )

                            OutlinedTextField(
                                value = ppeRequirements,
                                onValueChange = { ppeRequirements = it },
                                label = { Text("تجهیزات حفاظت فردی الزامی (PPE)", fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 2
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("خلاصه نهایی پرمیت قبل از صدور:", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    Text("شماره: $generatedPermitNo • نوع: $permitType", fontSize = 9.sp)
                                    Text("تجهیز: $equipmentName • موقعیت: $location", fontSize = 9.sp)
                                    Text("اعتبار: $validHours ساعت • تاییدکننده: ${currentUser?.name ?: "سرپرست HSE"}", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (step < 3) {
                Button(
                    onClick = { step++ },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("گام بعدی", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.NavigateNext, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            } else {
                Button(
                    onClick = {
                        val expiry = System.currentTimeMillis() + (validHours * 3600 * 1000L)
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
                            expiryTimestamp = expiry,
                            issuedByUserId = currentUser?.id ?: 18L,
                            issuedByUserName = currentUser?.name ?: "سرپرست HSE",
                            requiresElectricalLoto = requiresElectricalLoto,
                            electricalLotoStatus = if (requiresElectricalLoto) "pending_isolation" else "not_required",
                            lotoLockNumbersJson = if (requiresElectricalLoto) "[\"$lotoLockNumber\"]" else "",
                            requiresGasTest = requiresGasTest,
                            gasTestResultO2 = if (requiresGasTest) gasO2 else "",
                            gasTestResultCO = if (requiresGasTest) gasCO else "",
                            gasTestResultLEL = if (requiresGasTest) gasLEL else "",
                            gasTesterName = if (requiresGasTest) (currentUser?.name ?: "HSE") else "",
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
                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("صدور رسمی پرمیت HSE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (step > 1) {
                OutlinedButton(onClick = { step-- }) {
                    Icon(Icons.Default.NavigateBefore, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("گام قبلی", fontSize = 11.sp)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("انصراف", fontSize = 11.sp)
                }
            }
        }
    )
}

/**
 * فرم نمایش جزئیات کامل استاندارد پرمیت ایمنی کارگاهی با ماژول امضای دیجیتال
 */
@Composable
fun SafetyPermitDetailDialog(
    permit: SafetyPermitEntity,
    currentUser: UserEntity? = null,
    digitalSignatureService: DigitalSignatureService? = null,
    onOpenSignDialog: ((stepOrder: Int) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val signatures by produceState<List<DigitalSignatureEntity>>(initialValue = emptyList(), permit.id) {
        digitalSignatureService?.getSignaturesFlow("safety_permit", permit.id)?.collect {
            value = it
        }
    }

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
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("فرم رسمی پرمیت ایمنی کارگاه", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = permit.permitNumber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Specs Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("مشخصات پرمیت کارگاهی:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("نوع مجوز: ${permit.permitType}", fontSize = 10.sp)
                            Text("واحد مجری: ${permit.executiveUnit}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("تجهیز: ${permit.equipmentName}", fontSize = 10.sp)
                            Text("موقعیت: ${permit.location}", fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("تاریخ صدور: ${permit.issueDate}", fontSize = 10.sp)
                            Text("مدت اعتبار: ${permit.validHours} ساعت", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        if (permit.expiryTimestamp > 0L) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("وضعیت اعتبار زمانی: ", fontSize = 10.sp)
                                PermitLiveTimer(expiryTimestamp = permit.expiryTimestamp)
                            }
                        }
                        if (permit.issuedByUserName.isNotBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("صادرکننده: ${permit.issuedByUserName}", fontSize = 10.sp)
                        }
                        if (permit.unitHeadApproved) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("تایید فنی واحد: ${permit.unitHeadApprovedBy}", fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }
                        if (permit.extensionCount > 0) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("تمدید اعتبار: ${permit.extensionCount} بار توسط ${permit.extendedByUserName}", fontSize = 10.sp, color = Color(0xFF8B5CF6))
                        }
                        if (!permit.stopReason.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("علت توقف: ${permit.stopReason} (${permit.stopDetails.orEmpty()})", fontSize = 10.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Digital Signature Hierarchy Module Card
                if (digitalSignatureService != null && onOpenSignDialog != null) {
                    DigitalSignatureHierarchyCard(
                        documentType = "safety_permit",
                        documentId = permit.id,
                        documentTitle = "پرمیت ایمنی ${permit.permitNumber} (${permit.equipmentName})",
                        signatures = signatures,
                        currentUser = currentUser,
                        onOpenSignDialog = onOpenSignDialog
                    )
                }

                if (permit.requiresElectricalLoto) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFDC2626).copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("وضعیت کارت قرمز برق (Electrical LOTO):", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFFDC2626))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val isIsolated = permit.electricalLotoStatus == "isolated_and_tagged"
                            Text(
                                text = if (isIsolated) "تغذیه الکتریکی قطع، فیدر قفل و کارت قرمز با تایید ${permit.electricalTaggedBy} نصب گردید." else "هشدار: تا زمان قطع برق و تایید واحد برق، هرگونه کار ممنوع است.",
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                if (permit.requiresGasTest && permit.gasTestResult.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("نتایج پایش و تست گاز محیطی:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF0284C7))
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(permit.gasTestResult, fontSize = 10.sp)
                            if (permit.gasTesterName.isNotBlank()) {
                                Text("تست‌کننده: ${permit.gasTesterName}", fontSize = 9.sp, color = Color(0xFF0284C7))
                            }
                        }
                    }
                }

                Text("اقدامات کنترلی و الزامات HSE:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(permit.safetyPrecautions.ifBlank { "رعایت دستورالعمل‌های عمومی ایمنی کارگاهی الزامی است." }, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(2.dp))
                Text("تجهیزات حفاظت فردی مورد نیاز (PPE):", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(permit.ppeRequirements, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("بستن", fontSize = 11.sp)
            }
        }
    )
}

