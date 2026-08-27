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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.ItemAssignmentEntity
import com.example.data.entity.ItemPrerequisiteEntity
import com.example.data.entity.OversightItemEntity
import com.example.data.entity.UserEntity
import com.example.ui.SupervisorFilter
import com.example.ui.TaskLogisticsTag
import com.example.ui.WbsTreeNode
import com.example.ui.WbsViewMode
import com.example.ui.getLogisticsTagsForItem
import com.example.ui.theme.*

fun getUnitColor(unitName: String): Color {
    return when {
        unitName.contains("مکانیک") -> Color(0xFF1E3A5F) // Navy
        unitName.contains("برق") -> Color(0xFFD97706) // Amber
        unitName.contains("ابزار") || unitName.contains("اتوماسیون") -> Color(0xFF0284C7) // Cyan/Blue
        unitName.contains("نسوز") -> Color(0xFF991B1B) // Crimson/Red
        unitName.contains("سیالات") || unitName.contains("آب") || unitName.contains("WTP") -> Color(0xFF0D9488) // Teal
        unitName.contains("بازرسی") -> Color(0xFF7C3AED) // Purple
        else -> Color(0xFF475569) // Slate
    }
}

fun getOutlineLevelColor(level: Int): Color {
    return when (level) {
        1 -> Color(0xFF1E3A5F)
        2 -> Color(0xFF2563EB)
        3 -> Color(0xFF0D9488)
        4 -> Color(0xFFD97706)
        5 -> Color(0xFF7C3AED)
        else -> Color(0xFF64748B)
    }
}

fun getEmojiForTag(tag: TaskLogisticsTag): String {
    return when (tag.id) {
        "CRANE" -> "🏗️"
        "SCAFFOLDING" -> "🪜"
        "FLUSHING" -> "💧"
        "LOTO" -> "⚡"
        "HOT_WORK" -> "🔥"
        "NDT" -> "🔍"
        else -> "⚙️"
    }
}

/**
 * WBS Hierarchy & Nested Task Management View
 * ساختار مدیریت سلسله‌مراتبی شکست کار (WBS) و پایش تسک‌های تو در تو در سامانه اورهال فولاد غدیر نی‌ریز
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTooltipIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tooltip: String,
    isSelected: Boolean = false,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    testTag: String = "",
    onClick: () -> Unit
) {
    val tooltipState = rememberTooltipState()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip(
                shape = RoundedCornerShape(8.dp),
                containerColor = IndustrialNavy,
                contentColor = Color.White
            ) {
                Text(
                    text = tooltip,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        },
        state = tooltipState
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(36.dp)
                .background(
                    if (isSelected) selectedContainerColor else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
                .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = tooltip,
                tint = if (isSelected) selectedContentColor else unselectedContentColor,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

@Composable
fun WbsHierarchyManagementView(
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
    onAddNewRootItem: () -> Unit,
    onAddSubtask: (parent: OversightItemEntity, title: String, durationHours: Double, manpower: Int, equipment: String, location: String) -> Unit,
    onDeleteHierarchy: (itemId: Long) -> Unit,
    onEditItem: (item: OversightItemEntity) -> Unit
) {
    var selectedParentForSubtask by remember { mutableStateOf<OversightItemEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<OversightItemEntity?>(null) }
    var selectedSupervisorId by remember { mutableStateOf<Long?>(null) }

    // Dropdown state controls
    var showStatusDropdown by remember { mutableStateOf(false) }
    var showSupervisorDropdown by remember { mutableStateOf(false) }
    var showUnitDropdown by remember { mutableStateOf(false) }

    val relevantSupervisors = remember(allUsers, selectedUnit, currentUser) {
        val targetUnit = selectedUnit ?: currentUser?.unit
        if (targetUnit != null && currentUser?.role == "unit_head") {
            allUsers.filter { it.unit == targetUnit || (it.role == "supervisor" && it.unit == targetUnit) }
        } else {
            allUsers.filter { it.role == "supervisor" || it.role == "unit_head" }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // 1. Search Bar & View Mode Selector Header with Long-Press Tooltips
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("جستجو در ساختار شکست WBS، تجهیز یا فعالیت...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "پاک کردن", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("wbs_search_input")
            )

            // View Mode Selector Segmented Buttons with Persian Tooltips on long-press
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(modifier = Modifier.padding(2.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    AppTooltipIconButton(
                        icon = Icons.Default.AccountTree,
                        tooltip = "نمای درختی سلسله‌مراتبی (WBS Tree)",
                        isSelected = currentViewMode == WbsViewMode.TREE,
                        testTag = "view_mode_tree_btn",
                        onClick = { onSetViewMode(WbsViewMode.TREE) }
                    )

                    AppTooltipIconButton(
                        icon = Icons.Default.FlashOn,
                        tooltip = "مرور سریع آکاردئونی و ثبت گزارش درجا",
                        isSelected = currentViewMode == WbsViewMode.QUICK_REVIEW,
                        testTag = "view_mode_quick_review_btn",
                        onClick = { onSetViewMode(WbsViewMode.QUICK_REVIEW) }
                    )

                    AppTooltipIconButton(
                        icon = Icons.Default.Engineering,
                        tooltip = "پایش لجستیک و ادوات (جرثقیل، داربست، فلاشینگ، LOTO)",
                        isSelected = currentViewMode == WbsViewMode.LOGISTICS_SCHEDULE,
                        testTag = "view_mode_logistics_btn",
                        onClick = { onSetViewMode(WbsViewMode.LOGISTICS_SCHEDULE) }
                    )

                    AppTooltipIconButton(
                        icon = Icons.Default.GridView,
                        tooltip = "ماتریس ناحیه‌ای و زون‌های کارخانه",
                        isSelected = currentViewMode == WbsViewMode.INDUSTRIAL_MATRIX,
                        testTag = "view_mode_matrix_btn",
                        onClick = { onSetViewMode(WbsViewMode.INDUSTRIAL_MATRIX) }
                    )

                    AppTooltipIconButton(
                        icon = Icons.Default.FormatListBulleted,
                        tooltip = "فهرست خطی با کدهای WBS",
                        isSelected = currentViewMode == WbsViewMode.LIST_OUTLINE,
                        testTag = "view_mode_list_btn",
                        onClick = { onSetViewMode(WbsViewMode.LIST_OUTLINE) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Compact Dropdown Filters Row (Clean, uncluttered, larger font)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Dropdown 1: Task Status / Schedule Filter
            Box(modifier = Modifier.weight(1.2f)) {
                val currentStatusTitle = when (supervisorFilter) {
                    SupervisorFilter.ALL -> "همه تسک‌ها (${items.size})"
                    SupervisorFilter.TODAY_TASKS -> "کارهای شیفت امروز"
                    SupervisorFilter.INCOMPLETE_DUE -> "ناتمام تا امروز"
                    SupervisorFilter.BLOCKED -> "دارای مانع (Blocked)"
                    SupervisorFilter.IN_PROGRESS -> "در حال اجرا"
                    SupervisorFilter.COMPLETED -> "تکمیل شده"
                }

                val currentStatusIcon = when (supervisorFilter) {
                    SupervisorFilter.ALL -> Icons.Default.ListAlt
                    SupervisorFilter.TODAY_TASKS -> Icons.Default.Today
                    SupervisorFilter.INCOMPLETE_DUE -> Icons.Default.PendingActions
                    SupervisorFilter.BLOCKED -> Icons.Default.Block
                    SupervisorFilter.IN_PROGRESS -> Icons.Default.PlayCircle
                    SupervisorFilter.COMPLETED -> Icons.Default.CheckCircle
                }

                Surface(
                    onClick = { showStatusDropdown = true },
                    shape = RoundedCornerShape(10.dp),
                    color = when (supervisorFilter) {
                        SupervisorFilter.BLOCKED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                        SupervisorFilter.TODAY_TASKS -> IndustrialSteelBlue.copy(alpha = 0.15f)
                        SupervisorFilter.IN_PROGRESS -> IndustrialAmber.copy(alpha = 0.15f)
                        SupervisorFilter.COMPLETED -> IndustrialEmerald.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth().height(38.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = currentStatusIcon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = when (supervisorFilter) {
                                    SupervisorFilter.BLOCKED -> MaterialTheme.colorScheme.error
                                    SupervisorFilter.TODAY_TASKS -> IndustrialSteelBlue
                                    SupervisorFilter.IN_PROGRESS -> IndustrialAmber
                                    SupervisorFilter.COMPLETED -> IndustrialEmerald
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = currentStatusTitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }

                DropdownMenu(
                    expanded = showStatusDropdown,
                    onDismissRequest = { showStatusDropdown = false }
                ) {
                    val statusOptions = listOf(
                        Triple(SupervisorFilter.ALL, "همه تسک‌ها (${items.size})", Icons.Default.ListAlt),
                        Triple(SupervisorFilter.TODAY_TASKS, "کارهای شیفت امروز", Icons.Default.Today),
                        Triple(SupervisorFilter.INCOMPLETE_DUE, "ناتمام تا امروز", Icons.Default.PendingActions),
                        Triple(SupervisorFilter.BLOCKED, "دارای مانع اجرایی", Icons.Default.Block),
                        Triple(SupervisorFilter.IN_PROGRESS, "در حال اجرا", Icons.Default.PlayCircle),
                        Triple(SupervisorFilter.COMPLETED, "تکمیل شده", Icons.Default.CheckCircle)
                    )

                    statusOptions.forEach { (filter, label, icon) ->
                        val isSelected = supervisorFilter == filter
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                            },
                            onClick = {
                                onSupervisorFilterSelect(filter)
                                showStatusDropdown = false
                            }
                        )
                    }
                }
            }

            // Dropdown 2: Supervisor Selector Filter
            if (relevantSupervisors.isNotEmpty()) {
                Box(modifier = Modifier.weight(1f)) {
                    val selectedSupName = remember(selectedSupervisorId, relevantSupervisors) {
                        relevantSupervisors.firstOrNull { it.id == selectedSupervisorId }?.name ?: "همه سرپرستان"
                    }

                    Surface(
                        onClick = { showSupervisorDropdown = true },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedSupervisorId != null) IndustrialSteelBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selectedSupervisorId != null) IndustrialSteelBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = selectedSupName,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedSupervisorId != null) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }

                    DropdownMenu(
                        expanded = showSupervisorDropdown,
                        onDismissRequest = { showSupervisorDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "همه سرپرستان",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedSupervisorId == null) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                if (selectedSupervisorId == null) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                            },
                            onClick = {
                                selectedSupervisorId = null
                                showSupervisorDropdown = false
                            }
                        )
                        Divider()
                        relevantSupervisors.forEach { sup ->
                            val isSelected = selectedSupervisorId == sup.id
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = sup.name,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = "واحد: ${sup.unit ?: "عمومی"}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) IndustrialSteelBlue else Color.LightGray,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = sup.name.take(1),
                                                fontSize = 10.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                },
                                trailingIcon = {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = IndustrialSteelBlue, modifier = Modifier.size(16.dp))
                                    }
                                },
                                onClick = {
                                    selectedSupervisorId = sup.id
                                    showSupervisorDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Tree Mode Actions: Expand All / Collapse All (With Tooltips)
            if (currentViewMode == WbsViewMode.TREE) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    AppTooltipIconButton(
                        icon = Icons.Default.UnfoldMore,
                        tooltip = "گسترش همه گره‌های درختی",
                        onClick = onExpandAllTreeNodes
                    )
                    AppTooltipIconButton(
                        icon = Icons.Default.UnfoldLess,
                        tooltip = "بستن همه زیرشاخه‌ها",
                        onClick = onCollapseAllTreeNodes
                    )
                }

                if (currentUser?.role == "admin" || currentUser?.role == "planner") {
                    AppTooltipIconButton(
                        icon = Icons.Default.Add,
                        tooltip = "افزودن فعالیت ریشه جدید به WBS",
                        selectedContainerColor = IndustrialNavy,
                        selectedContentColor = Color.White,
                        isSelected = true,
                        onClick = onAddNewRootItem
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 4. Main Body Content Based on Active View Mode
        val displayItems = remember(items, selectedSupervisorId, allAssignments) {
            if (selectedSupervisorId == null) {
                items
            } else {
                val assignedItemIds = allAssignments.filter { it.supervisorUserId == selectedSupervisorId }.map { it.itemId }.toSet()
                items.filter { assignedItemIds.contains(it.id) }
            }
        }

        when (currentViewMode) {
            WbsViewMode.TREE -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("wbs_tree_list"),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (wbsTree.isEmpty()) {
                        item { EmptyWbsState() }
                    } else {
                        items(wbsTree, key = { "tree_root_${it.item.id}" }) { rootNode ->
                            WbsTreeNodeRecursiveItem(
                                node = rootNode,
                                expandedNodeIds = expandedTreeNodes,
                                onToggleExpanded = onToggleTreeNodeExpanded,
                                onAddSubtaskClick = { selectedParentForSubtask = it },
                                onQuickUpdate = onItemClickForDailyUpdate,
                                onEditClick = onEditItem,
                                onDeleteClick = { itemToDelete = it },
                                currentUser = currentUser,
                                depth = 0
                            )
                        }
                    }
                }
            }

            WbsViewMode.QUICK_REVIEW -> {
                // High-Density Accordion Quick Review View
                WbsQuickReviewAccordionView(
                    items = displayItems,
                    allAssignments = allAssignments,
                    allUsers = allUsers,
                    currentUser = currentUser,
                    onItemClickForDailyUpdate = onItemClickForDailyUpdate,
                    onEditItem = onEditItem,
                    onDeleteHierarchy = { itemToDelete = it }
                )
            }

            WbsViewMode.LOGISTICS_SCHEDULE -> {
                // Heavy Machinery, Crane, Scaffolding and Logistics Schedule for Today & Tomorrow
                WbsLogisticsScheduleView(
                    items = displayItems,
                    allAssignments = allAssignments,
                    allUsers = allUsers,
                    onItemClickForDailyUpdate = onItemClickForDailyUpdate
                )
            }

            WbsViewMode.INDUSTRIAL_MATRIX -> {
                // Grouped by Executive Unit & General Area Matrix
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("wbs_matrix_list"),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val groupedByUnit = displayItems.groupBy { it.executiveUnit.ifBlank { "عمومی" } }
                    groupedByUnit.forEach { (unitName, unitTasks) ->
                        val isUnitExpanded = expandedUnits.contains(unitName)
                        val unitColor = getUnitColor(unitName)

                        item {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = unitColor.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, unitColor.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleUnitExpansion(unitName) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(unitColor, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = if (isUnitExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = unitColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "واحد اجرایی: $unitName",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = unitColor
                                        )
                                    }
                                    val completedInUnit = unitTasks.count { it.status == "completed" }
                                    Text(
                                        text = "$completedInUnit از ${unitTasks.size} تکمیل",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (isUnitExpanded) {
                            val groupedByArea = unitTasks.groupBy { it.generalArea.ifBlank { "محوطه عمومی" } }
                            groupedByArea.forEach { (areaName, areaTasks) ->
                                val isAreaExpanded = expandedAreas.contains(areaName)
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 8.dp)
                                            .clickable { onToggleAreaExpansion(areaName) }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (isAreaExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                                                    contentDescription = null,
                                                    tint = IndustrialSteelBlue,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "محل: $areaName",
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                            Text("${areaTasks.size} فعالیت", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }
                                }

                                if (isAreaExpanded) {
                                    items(areaTasks, key = { "matrix_item_${it.id}" }) { task ->
                                        Box(modifier = Modifier.padding(start = 12.dp)) {
                                            WbsHierarchicalTaskCard(
                                                item = task,
                                                allPrerequisites = allPrerequisites,
                                                allAssignments = allAssignments,
                                                allUsers = allUsers,
                                                currentUser = currentUser,
                                                onQuickUpdate = { onItemClickForDailyUpdate(task) },
                                                onAddSubtask = { selectedParentForSubtask = task },
                                                onDelete = { itemToDelete = task },
                                                onEdit = { onEditItem(task) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            WbsViewMode.LIST_OUTLINE -> {
                // High-density Linear Outline List with WBS Depth Indicators
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("wbs_outline_list"),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (displayItems.isEmpty()) {
                        item { EmptyWbsState() }
                    } else {
                        items(displayItems, key = { "outline_item_${it.id}" }) { item ->
                            val paddingStart = ((item.outlineLevel - 1) * 6).coerceIn(0, 20).dp
                            Box(modifier = Modifier.padding(start = paddingStart)) {
                                WbsHierarchicalTaskCard(
                                    item = item,
                                    allPrerequisites = allPrerequisites,
                                    allAssignments = allAssignments,
                                    allUsers = allUsers,
                                    currentUser = currentUser,
                                    onQuickUpdate = { onItemClickForDailyUpdate(item) },
                                    onAddSubtask = { selectedParentForSubtask = item },
                                    onDelete = { itemToDelete = item },
                                    onEdit = { onEditItem(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 5. Add Subtask Dialog
    if (selectedParentForSubtask != null) {
        AddSubtaskModalDialog(
            parent = selectedParentForSubtask!!,
            onDismiss = { selectedParentForSubtask = null },
            onConfirm = { title, dur, mp, eq, loc ->
                onAddSubtask(selectedParentForSubtask!!, title, dur, mp, eq, loc)
                selectedParentForSubtask = null
            }
        )
    }

    // 6. Delete Confirmation Dialog
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("حذف گره WBS و زیرشاخه‌ها") },
            text = {
                Text("آیا از حذف فعالیت «${itemToDelete?.wbsCode}: ${itemToDelete?.title}» و تمام زیرفعالیت‌های آن اطمینان دارید؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { onDeleteHierarchy(it.id) }
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف قطعی")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

/**
 * Recursive Tree Node Composable for WBS Nested View
 * High-density layout that maintains full-width card proportion at deep levels (levels 4, 5, 6).
 */
@Composable
fun WbsTreeNodeRecursiveItem(
    node: WbsTreeNode,
    expandedNodeIds: Set<Long>,
    onToggleExpanded: (Long) -> Unit,
    onAddSubtaskClick: (OversightItemEntity) -> Unit,
    onQuickUpdate: (OversightItemEntity) -> Unit,
    onEditClick: (OversightItemEntity) -> Unit,
    onDeleteClick: (OversightItemEntity) -> Unit,
    currentUser: UserEntity?,
    depth: Int
) {
    val isExpanded = expandedNodeIds.contains(node.item.id) || (depth == 0 && expandedNodeIds.isEmpty())
    val hasChildren = node.children.isNotEmpty()
    // Compact indent capped at 18dp so card width remains optimal and comfortable on mobile
    val paddingStart = (depth * 5).coerceAtMost(18).dp
    val unitColor = getUnitColor(node.item.executiveUnit)
    val levelColor = getOutlineLevelColor(node.item.outlineLevel)
    val logisticsTags = getLogisticsTagsForItem(node.item)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = paddingStart)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (depth == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            tonalElevation = (3 - depth).coerceAtLeast(1).dp,
            border = BorderStroke(
                1.dp,
                when {
                    node.item.status == "blocked" -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    node.item.status == "completed" || node.aggregateProgress >= 100 -> IndustrialEmerald.copy(alpha = 0.5f)
                    node.item.status == "in_progress" -> IndustrialSteelBlue.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onQuickUpdate(node.item) }
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Left Colored Indicator Strip (Unit & Level indicator)
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(unitColor)
                )

                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp).weight(1f)) {
                    // Header: Expand Icon, WBS Badge, Level Tag, Progress Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (hasChildren) {
                                IconButton(
                                    onClick = { onToggleExpanded(node.item.id) },
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Expand/Collapse",
                                        tint = unitColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(3.dp))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(5.dp)
                                        .background(unitColor.copy(alpha = 0.3f), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                            }

                            // WBS Code Pill
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = levelColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = node.item.wbsCode,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = levelColor,
                                    fontSize = 10.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Outline Level Badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                            ) {
                                Text(
                                    text = "L${node.item.outlineLevel}",
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Unit Pill
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = unitColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = node.item.executiveUnit,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = unitColor
                                )
                            }

                            if (node.item.equipmentName.isNotBlank() && node.item.equipmentName != node.item.title) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = node.item.equipmentName,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Progress Status Badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = when (node.item.status) {
                                "completed" -> IndustrialEmerald.copy(alpha = 0.15f)
                                "in_progress" -> IndustrialSteelBlue.copy(alpha = 0.15f)
                                "blocked" -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                else -> Color.Gray.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = when (node.item.status) {
                                    "completed" -> "تکمیل"
                                    "in_progress" -> "در حال اجرا"
                                    "blocked" -> "مانع‌دار"
                                    else -> "در انتظار"
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (node.item.status) {
                                    "completed" -> IndustrialEmerald
                                    "in_progress" -> IndustrialSteelBlue
                                    "blocked" -> MaterialTheme.colorScheme.error
                                    else -> Color.Gray
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Title
                    Text(
                        text = node.item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (hasChildren || depth == 0) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Logistics Tags (Crane, Scaffolding, Flushing, LOTO)
                    if (logisticsTags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            logisticsTags.take(3).forEach { tag ->
                                val tagColor = Color(tag.colorHex)
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = tagColor.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(getEmojiForTag(tag), fontSize = 9.sp)
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(tag.shortLabel, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = tagColor)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayProgress = if (hasChildren) node.aggregateProgress else node.item.progressPercentage
                        LinearProgressIndicator(
                            progress = { displayProgress / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = when {
                                displayProgress >= 100 -> IndustrialEmerald
                                node.item.status == "blocked" -> MaterialTheme.colorScheme.error
                                else -> IndustrialAmber
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$displayProgress%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Bottom info and quick actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (hasChildren) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = IndustrialNavy.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "${node.totalDescendants} زیرفعالیت",
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        fontSize = 8.sp,
                                        color = IndustrialNavy
                                    )
                                }
                            }

                            val actHours = if (hasChildren) node.aggregateActualHours else node.item.actualHours
                            val planHours = if (hasChildren) node.aggregatePlannedHours else node.item.durationHours
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "${actHours.toInt()}/${planHours.toInt()} س",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    fontSize = 9.sp
                                )
                            }

                            if (node.item.manpowerCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = "${node.item.manpowerCount} نفر",
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }

                        // Action buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (currentUser?.role == "admin" || currentUser?.role == "planner") {
                                FilledTonalButton(
                                    onClick = { onAddSubtaskClick(node.item) },
                                    contentPadding = PaddingValues(horizontal = 5.dp, vertical = 1.dp),
                                    shape = RoundedCornerShape(5.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("زیرفعالیت", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }

                                IconButton(
                                    onClick = { onEditClick(node.item) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "ویرایش", modifier = Modifier.size(12.dp))
                                }

                                IconButton(
                                    onClick = { onDeleteClick(node.item) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(12.dp))
                                }
                            }

                            Button(
                                onClick = { onQuickUpdate(node.item) },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp),
                                shape = RoundedCornerShape(5.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IndustrialSteelBlue),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text(if (currentUser?.role == "supervisor") "ثبت کارکرد" else "جزئیات", fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }

        // Render Children if Expanded
        if (hasChildren && isExpanded) {
            Spacer(modifier = Modifier.height(4.dp))
            node.children.forEach { childNode ->
                WbsTreeNodeRecursiveItem(
                    node = childNode,
                    expandedNodeIds = expandedNodeIds,
                    onToggleExpanded = onToggleExpanded,
                    onAddSubtaskClick = onAddSubtaskClick,
                    onQuickUpdate = onQuickUpdate,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick,
                    currentUser = currentUser,
                    depth = depth + 1
                )
                Spacer(modifier = Modifier.height(3.dp))
            }
        }
    }
}

/**
 * Standard WBS Hierarchical Task Card for Matrix and Outline List Views
 */
@Composable
fun WbsHierarchicalTaskCard(
    item: OversightItemEntity,
    allPrerequisites: List<ItemPrerequisiteEntity>,
    allAssignments: List<ItemAssignmentEntity>,
    allUsers: List<UserEntity>,
    currentUser: UserEntity?,
    onQuickUpdate: () -> Unit,
    onAddSubtask: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val prereqCount = allPrerequisites.count { it.itemId == item.id }
    val unitColor = getUnitColor(item.executiveUnit)
    val levelColor = getOutlineLevelColor(item.outlineLevel)
    val logisticsTags = getLogisticsTagsForItem(item)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(
            1.dp,
            when (item.status) {
                "blocked" -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                "in_progress" -> IndustrialSteelBlue.copy(alpha = 0.5f)
                "completed" -> IndustrialEmerald.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onQuickUpdate() }
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(unitColor)
            )

            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp).weight(1f)) {
                // Top Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = levelColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "WBS: ${item.wbsCode}",
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = levelColor,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = unitColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = item.executiveUnit,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = unitColor
                            )
                        }

                        if (item.equipmentName.isNotBlank() && item.equipmentName != item.title) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.equipmentName,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
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
                                "blocked" -> "دارای مانع"
                                else -> "در انتظار"
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp,
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

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )

                if (logisticsTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        logisticsTags.take(3).forEach { tag ->
                            val tagColor = Color(tag.colorHex)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = tagColor.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(getEmojiForTag(tag), fontSize = 9.sp)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(tag.shortLabel, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = tagColor)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { item.progressPercentage / 100f },
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = when (item.status) {
                            "completed" -> IndustrialEmerald
                            "blocked" -> MaterialTheme.colorScheme.error
                            else -> IndustrialAmber
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${item.progressPercentage}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action & Info Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (item.manpowerCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "${item.manpowerCount} نفر",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    fontSize = 9.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "${item.actualHours.toInt()}/${item.durationHours.toInt()} س",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                fontSize = 9.sp
                            )
                        }

                        if (prereqCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = IndustrialAmber.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$prereqCount پیش‌نیاز",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    fontSize = 9.sp,
                                    color = IndustrialAmberDark,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentUser?.role == "admin" || currentUser?.role == "planner") {
                            FilledTonalButton(
                                onClick = onAddSubtask,
                                contentPadding = PaddingValues(horizontal = 5.dp, vertical = 1.dp),
                                shape = RoundedCornerShape(5.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("زیرفعالیت", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }

                            IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "ویرایش", modifier = Modifier.size(12.dp))
                            }

                            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(12.dp))
                            }
                        }

                        Button(
                            onClick = onQuickUpdate,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp),
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IndustrialSteelBlue),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text(if (currentUser?.role == "supervisor") "ثبت کارکرد" else "جزئیات", fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * High-Density Accordion Quick Review View
 * کلیک روی هر تسک کادر را باز کرده و کنترل‌های پیشرفت سریع، مانع‌زدایی و جزئیات کامل لجستیک را نمایش می‌دهد.
 */
@Composable
fun WbsQuickReviewAccordionView(
    items: List<OversightItemEntity>,
    allAssignments: List<ItemAssignmentEntity>,
    allUsers: List<UserEntity>,
    currentUser: UserEntity?,
    onItemClickForDailyUpdate: (OversightItemEntity) -> Unit,
    onEditItem: (OversightItemEntity) -> Unit,
    onDeleteHierarchy: (OversightItemEntity) -> Unit
) {
    var expandedTaskId by remember { mutableStateOf<Long?>(null) }
    val userMap = remember(allUsers) { allUsers.associateBy { it.id } }
    val assignmentGroup = remember(allAssignments) { allAssignments.groupBy { it.itemId } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("wbs_quick_review_list"),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (items.isEmpty()) {
            item { EmptyWbsState() }
        } else {
            items(items, key = { "quick_review_${it.id}" }) { task ->
                val isExpanded = expandedTaskId == task.id
                val unitColor = getUnitColor(task.executiveUnit)
                val logisticsTags = getLogisticsTagsForItem(task)
                val assignedSupervisors = assignmentGroup[task.id]?.mapNotNull { userMap[it.supervisorUserId] } ?: emptyList()

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = if (isExpanded) 3.dp else 1.dp,
                    border = BorderStroke(
                        1.dp,
                        if (isExpanded) unitColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedTaskId = if (isExpanded) null else task.id }
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                                .background(unitColor)
                        )

                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp).weight(1f)) {
                            // Summary Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = unitColor.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = task.wbsCode,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = unitColor,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Text(
                                        text = task.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = if (isExpanded) 3 else 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${task.progressPercentage}%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (task.progressPercentage >= 100) IndustrialEmerald else IndustrialAmber
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Progress Mini Bar
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { task.progressPercentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (task.progressPercentage >= 100) IndustrialEmerald else if (task.status == "blocked") MaterialTheme.colorScheme.error else IndustrialAmber,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            // Expanded Detail Panel
                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Context Row: Area, Location, Equipment, Supervisor
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("واحد: ${task.executiveUnit} • محوطه: ${task.generalArea}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("تجهیز: ${task.equipmentName.ifBlank { "عمومی" }} • زون: ${task.executionLocation.ifBlank { "کارگاه" }}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                        if (assignedSupervisors.isNotEmpty()) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = IndustrialSteelBlue.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = "ناظر: ${assignedSupervisors.joinToString { it.name }}",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = IndustrialSteelBlue,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Logistics Badges
                                    if (logisticsTags.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("نیازمندی‌های خاص:", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            logisticsTags.forEach { tag ->
                                                val tagColor = Color(tag.colorHex)
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = tagColor.copy(alpha = 0.15f)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(getEmojiForTag(tag), fontSize = 9.sp)
                                                        Spacer(modifier = Modifier.width(2.dp))
                                                        Text(tag.shortLabel, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = tagColor)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Action Buttons: Quick Update, Obstacle, Details
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                            ) {
                                                Text("${task.actualHours.toInt()}/${task.durationHours.toInt()} ساعت", fontSize = 9.sp, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                            ) {
                                                Text("${task.manpowerCount} نفر", fontSize = 9.sp, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            if (currentUser?.role == "admin" || currentUser?.role == "planner") {
                                                IconButton(onClick = { onEditItem(task) }, modifier = Modifier.size(26.dp)) {
                                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                                }
                                            }

                                            Button(
                                                onClick = { onItemClickForDailyUpdate(task) },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(6.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = IndustrialSteelBlue),
                                                modifier = Modifier.height(26.dp)
                                            ) {
                                                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("ثبت کارکرد و مانع", fontSize = 10.sp, fontWeight = FontWeight.Bold)
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

/**
 * Logistics, Crane, Heavy Equipment & Scaffolding Schedule View
 * تفکیک کارهای نیازمند جرثقیل، داربست، فلاشینگ، تست و پروانه‌های کار گرم برای هماهنگی لجستیک روزانه و فردا
 */
@Composable
fun WbsLogisticsScheduleView(
    items: List<OversightItemEntity>,
    allAssignments: List<ItemAssignmentEntity>,
    allUsers: List<UserEntity>,
    onItemClickForDailyUpdate: (OversightItemEntity) -> Unit
) {
    var selectedLogisticsFilter by remember { mutableStateOf("all") } // "all", "crane", "scaffolding", "flushing", "loto"

    val userMap = remember(allUsers) { allUsers.associateBy { it.id } }
    val assignmentGroup = remember(allAssignments) { allAssignments.groupBy { it.itemId } }

    // Tagged tasks with special logistics requirements
    val logisticsTasks = remember(items, selectedLogisticsFilter) {
        items.mapNotNull { item ->
            val tags = getLogisticsTagsForItem(item)
            if (tags.isNotEmpty()) {
                val matchesFilter = when (selectedLogisticsFilter) {
                    "crane" -> tags.any { it.id == "CRANE" }
                    "scaffolding" -> tags.any { it.id == "SCAFFOLDING" }
                    "flushing" -> tags.any { it.id == "FLUSHING" }
                    "loto" -> tags.any { it.id == "LOTO" }
                    else -> true
                }
                if (matchesFilter) Pair(item, tags) else null
            } else null
        }
    }

    val craneCount = items.count { getLogisticsTagsForItem(it).any { t -> t.id == "CRANE" } }
    val scaffoldingCount = items.count { getLogisticsTagsForItem(it).any { t -> t.id == "SCAFFOLDING" } }
    val flushingCount = items.count { getLogisticsTagsForItem(it).any { t -> t.id == "FLUSHING" } }
    val lotoCount = items.count { getLogisticsTagsForItem(it).any { t -> t.id == "LOTO" } }

    Column(modifier = Modifier.fillMaxSize()) {
        // Logistics Summary KPIs Header
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = IndustrialNavy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Engineering, contentDescription = null, tint = IndustrialAmber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مرکز هماهنگی لجستیک و ادوات سنگین", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = IndustrialAmber.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "${logisticsTasks.size} فعالیت لجستیکی فعال",
                            color = IndustrialAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // KPI Mini Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LogisticsKpiBadge(title = "جرثقیل", count = craneCount, emoji = "🏗️", color = IndustrialAmber, modifier = Modifier.weight(1f))
                    LogisticsKpiBadge(title = "داربست", count = scaffoldingCount, emoji = "🪜", color = IndustrialEmerald, modifier = Modifier.weight(1f))
                    LogisticsKpiBadge(title = "فلاشینگ", count = flushingCount, emoji = "💧", color = IndustrialSteelBlue, modifier = Modifier.weight(1f))
                    LogisticsKpiBadge(title = "LOTO", count = lotoCount, emoji = "⚡", color = IndustrialCrimson, modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Filter chips: All, Crane, Scaffolding, Flushing, LOTO
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val filterOptions = listOf(
                Pair("all", "همه ادوات"),
                Pair("crane", "🏗️ جرثقیل سنگین"),
                Pair("scaffolding", "🪜 داربست‌بندی"),
                Pair("flushing", "💧 شستشو و فلاشینگ"),
                Pair("loto", "⚡ قطع و قفل برق (LOTO)")
            )
            items(filterOptions) { (key, label) ->
                val isSelected = selectedLogisticsFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedLogisticsFilter = key },
                    label = { Text(label, fontSize = 10.sp) },
                    modifier = Modifier.height(30.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Logistics Task List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("wbs_logistics_list"),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (logisticsTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("هیچ نیازمندی لجستیکی برای این فیلتر ثبت نشده است.", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            } else {
                items(logisticsTasks, key = { "logistics_${it.first.id}" }) { (task, tags) ->
                    val assignedSupervisors = assignmentGroup[task.id]?.mapNotNull { userMap[it.supervisorUserId] } ?: emptyList()
                    val unitColor = getUnitColor(task.executiveUnit)

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClickForDailyUpdate(task) }
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(unitColor)
                            )

                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp).weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = unitColor.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = task.wbsCode,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = unitColor,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "واحد ${task.executiveUnit}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = unitColor
                                        )
                                    }

                                    // Tags
                                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        tags.forEach { t ->
                                            val tagColor = Color(t.colorHex)
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = tagColor.copy(alpha = 0.15f)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(getEmojiForTag(t), fontSize = 9.sp)
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(t.shortLabel, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = tagColor)
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = task.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(3.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "محل: ${task.generalArea} • زون: ${task.executionLocation.ifBlank { "سایت" }}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (assignedSupervisors.isNotEmpty()) {
                                        Text(
                                            text = "ناظر: ${assignedSupervisors.first().name}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = IndustrialSteelBlue
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

@Composable
fun LogisticsKpiBadge(
    title: String,
    count: Int,
    emoji: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 10.sp)
                Spacer(modifier = Modifier.width(2.dp))
                Text(title, fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
            }
            Text("$count تسک", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

/**
 * Add Subtask Modal Dialog with Auto Child WBS Numbering
 */
@Composable
fun AddSubtaskModalDialog(
    parent: OversightItemEntity,
    onDismiss: () -> Unit,
    onConfirm: (title: String, durationHours: Double, manpower: Int, equipment: String, location: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("4") }
    var manpowerText by remember { mutableStateOf("2") }
    var equipment by remember { mutableStateOf(parent.equipmentName) }
    var location by remember { mutableStateOf(parent.executionLocation) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = IndustrialSteelBlue)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تعریف زیرفعالیت WBS جدید", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Parent Context Box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("گره والد (Parent Node):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("${parent.wbsCode}: ${parent.title}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("واحد: ${parent.executiveUnit} • محوطه: ${parent.generalArea}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان زیرفعالیت اجرایی *", fontSize = 11.sp) },
                    placeholder = { Text("مانند: تعویض رینگ آب‌بندی شفت...", fontSize = 10.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Activity Presets
                Text("عناوین پیشنهادی سریع:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val presets = listOf("دشارژ و تمیزکاری", "بازرسی چشمی و NDT", "سرویس و روانکاری", "مونتاژ و تست نهایی", "تنظیم و کالیبراسیون")
                    items(presets) { p ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { title = p }
                        ) {
                            Text(p, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), fontSize = 9.sp)
                        }
                    }
                }

                // Duration and Manpower Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it },
                        label = { Text("مدت زمان (ساعت)", fontSize = 10.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = manpowerText,
                        onValueChange = { manpowerText = it },
                        label = { Text("تعداد نفرات", fontSize = 10.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Equipment & Location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = equipment,
                        onValueChange = { equipment = it },
                        label = { Text("تجهیز", fontSize = 10.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("محل / زون", fontSize = 10.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("انصراف")
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val hrs = durationText.toDoubleOrNull() ?: 4.0
                                val mp = manpowerText.toIntOrNull() ?: 2
                                onConfirm(title, hrs, mp, equipment, location)
                            }
                        },
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialSteelBlue)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ثبت زیرفعالیت", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyWbsState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(30.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AccountTree, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("هیچ فعالیتی در ساختار شکست کار با این فیلترها یافت نشد.", color = Color.Gray, fontSize = 11.sp)
        }
    }
}
