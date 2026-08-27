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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.ItemAssignmentEntity
import com.example.data.entity.ItemPrerequisiteEntity
import com.example.data.entity.OversightItemEntity
import com.example.data.entity.UserEntity
import com.example.ui.SupervisorFilter
import com.example.ui.WbsTreeNode
import com.example.ui.WbsViewMode
import com.example.ui.theme.*

/**
 * WBS Hierarchy & Nested Task Management View
 * ساختار مدیریت سلسله‌مراتبی شکست کار (WBS) و پایش تسک‌های تو در تو در سامانه اورهال فولاد غدیر نی‌ریز
 */
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // 1. Search Bar & View Mode Selector Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("جستجو در WBS، تجهیز یا فعالیت...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("wbs_search_input")
            )

            // View Mode Selector Segmented Buttons
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(modifier = Modifier.padding(3.dp)) {
                    IconButton(
                        onClick = { onSetViewMode(WbsViewMode.TREE) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (currentViewMode == WbsViewMode.TREE) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .testTag("view_mode_tree_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountTree,
                            contentDescription = "درخت تودرتو",
                            tint = if (currentViewMode == WbsViewMode.TREE) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { onSetViewMode(WbsViewMode.INDUSTRIAL_MATRIX) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (currentViewMode == WbsViewMode.INDUSTRIAL_MATRIX) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .testTag("view_mode_matrix_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "ماتریس ۵ سطحی",
                            tint = if (currentViewMode == WbsViewMode.INDUSTRIAL_MATRIX) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { onSetViewMode(WbsViewMode.LIST_OUTLINE) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (currentViewMode == WbsViewMode.LIST_OUTLINE) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .testTag("view_mode_list_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatListBulleted,
                            contentDescription = "فهرست خطی",
                            tint = if (currentViewMode == WbsViewMode.LIST_OUTLINE) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Filter Chips (Supervisor Filters & Units)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = supervisorFilter == SupervisorFilter.ALL,
                    onClick = { onSupervisorFilterSelect(SupervisorFilter.ALL) },
                    label = { Text("همه تسک‌ها (${items.size})", fontSize = 11.sp) }
                )
            }
            item {
                FilterChip(
                    selected = supervisorFilter == SupervisorFilter.TODAY_TASKS,
                    onClick = { onSupervisorFilterSelect(SupervisorFilter.TODAY_TASKS) },
                    label = { Text("امروز", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
            }
            item {
                FilterChip(
                    selected = supervisorFilter == SupervisorFilter.INCOMPLETE_DUE,
                    onClick = { onSupervisorFilterSelect(SupervisorFilter.INCOMPLETE_DUE) },
                    label = { Text("ناتمام تا امروز", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.PendingActions, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
            }
            item {
                FilterChip(
                    selected = supervisorFilter == SupervisorFilter.BLOCKED,
                    onClick = { onSupervisorFilterSelect(SupervisorFilter.BLOCKED) },
                    label = { Text("دارای مانع", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }
            item {
                FilterChip(
                    selected = supervisorFilter == SupervisorFilter.IN_PROGRESS,
                    onClick = { onSupervisorFilterSelect(SupervisorFilter.IN_PROGRESS) },
                    label = { Text("در حال اجرا", fontSize = 11.sp) }
                )
            }
            item {
                FilterChip(
                    selected = supervisorFilter == SupervisorFilter.COMPLETED,
                    onClick = { onSupervisorFilterSelect(SupervisorFilter.COMPLETED) },
                    label = { Text("تکمیل شده", fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Hierarchy Management Action Bar (Expand / Collapse / Add Root Task)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.clickable { onExpandAllTreeNodes() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.UnfoldMore, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("گسترش همه", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clickable { onCollapseAllTreeNodes() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.UnfoldLess, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("بستن همه", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (currentUser?.role == "admin" || currentUser?.role == "planner") {
                Button(
                    onClick = onAddNewRootItem,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("add_root_wbs_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تعریف فعالیت WBS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Body Content Depending on View Mode
        when (currentViewMode) {
            WbsViewMode.TREE -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("wbs_nested_tree_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (wbsTree.isEmpty()) {
                        item {
                            EmptyWbsState()
                        }
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

            WbsViewMode.INDUSTRIAL_MATRIX -> {
                // Grouped 5-level Industrial Matrix (Unit -> Area -> Location/Equipment -> Tasks)
                val groupedByUnit = remember(items) {
                    items.groupBy { it.executiveUnit.ifBlank { "عمومی" } }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("wbs_matrix_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (items.isEmpty()) {
                        item { EmptyWbsState() }
                    }

                    groupedByUnit.forEach { (unitName, unitTasks) ->
                        val isUnitExpanded = expandedUnits.contains(unitName)
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = when (unitName) {
                                    "مکانیک" -> IndustrialNavy.copy(alpha = 0.08f)
                                    "برق" -> IndustrialAmber.copy(alpha = 0.08f)
                                    "ابزار دقیق" -> IndustrialEmerald.copy(alpha = 0.08f)
                                    "نسوز" -> IndustrialCrimson.copy(alpha = 0.08f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleUnitExpansion(unitName) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isUnitExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "واحد اجرایی: $unitName",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    }
                                    val completedInUnit = unitTasks.count { it.status == "completed" }
                                    Text(
                                        text = "$completedInUnit از ${unitTasks.size} تکمیل",
                                        fontSize = 12.sp,
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
                                            .padding(start = 12.dp)
                                            .clickable { onToggleAreaExpansion(areaName) }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (isAreaExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                                                    contentDescription = null,
                                                    tint = IndustrialSteelBlue,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "محل کلی: $areaName",
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp
                                                )
                                            }
                                            Text("${areaTasks.size} فعالیت", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                }

                                if (isAreaExpanded) {
                                    items(areaTasks, key = { "matrix_item_${it.id}" }) { task ->
                                        Box(modifier = Modifier.padding(start = 24.dp)) {
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
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (items.isEmpty()) {
                        item { EmptyWbsState() }
                    } else {
                        items(items, key = { "outline_item_${it.id}" }) { item ->
                            val paddingStart = ((item.outlineLevel - 1) * 16).coerceAtLeast(0).dp
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
            onConfirm = { title, hours, manpower, equip, loc ->
                onAddSubtask(selectedParentForSubtask!!, title, hours, manpower, equip, loc)
                selectedParentForSubtask = null
            }
        )
    }

    // 6. Delete Confirmation Dialog
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حذف فعالیت و زیرفعالیت‌ها", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("آیا از حذف فعالیت «${itemToDelete!!.wbsCode}: ${itemToDelete!!.title}» و تمام زیرفعالیت‌های وابسته به آن از دیتابیس Room اطمینان دارید؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteHierarchy(itemToDelete!!.id)
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
 * Recursive Tree Node Composable for Nested Task Hierarchy
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
    val paddingStart = (depth * 14).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = paddingStart)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (depth == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            tonalElevation = (4 - depth).coerceAtLeast(1).dp,
            border = BorderStroke(
                1.dp,
                when {
                    node.item.status == "blocked" -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    node.item.status == "completed" || node.aggregateProgress >= 100 -> IndustrialEmerald.copy(alpha = 0.5f)
                    node.item.status == "in_progress" -> IndustrialSteelBlue.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.outlineVariant
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onQuickUpdate(node.item) }
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
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
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Expand/Collapse",
                                    tint = IndustrialSteelBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(7.dp)
                                    .background(Color.Gray.copy(alpha = 0.4f), shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        // WBS Code Pill
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (node.item.outlineLevel) {
                                1 -> IndustrialNavy.copy(alpha = 0.15f)
                                2 -> IndustrialSteelBlue.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.secondaryContainer
                            }
                        ) {
                            Text(
                                text = "WBS: ${node.item.wbsCode}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (node.item.outlineLevel == 1) IndustrialNavy else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Outline Level Badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                        ) {
                            Text(
                                text = "L${node.item.outlineLevel}",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (node.item.equipmentName.isNotBlank() && node.item.equipmentName != node.item.title) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = node.item.equipmentName,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Progress Status Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (node.item.status) {
                            "completed" -> IndustrialEmerald.copy(alpha = 0.15f)
                            "in_progress" -> IndustrialSteelBlue.copy(alpha = 0.15f)
                            "blocked" -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            else -> Color.Gray.copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            text = when (node.item.status) {
                                "completed" -> "تکمیل شده"
                                "in_progress" -> "در حال اجرا"
                                "blocked" -> "دارای مانع"
                                else -> "در انتظار"
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
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

                Spacer(modifier = Modifier.height(6.dp))

                // Title
                Text(
                    text = node.item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (hasChildren || depth == 0) FontWeight.Bold else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar (With Aggregate Rollup indicator if parent)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayProgress = if (hasChildren) node.aggregateProgress else node.item.progressPercentage
                    LinearProgressIndicator(
                        progress = { displayProgress / 100f },
                        modifier = Modifier
                            .weight(1f)
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when {
                            displayProgress >= 100 -> IndustrialEmerald
                            node.item.status == "blocked" -> MaterialTheme.colorScheme.error
                            else -> IndustrialAmber
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$displayProgress%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Footer Row: Subtask counter badge, Hours, and Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hasChildren) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = IndustrialPurple.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AccountTree, contentDescription = null, tint = IndustrialPurple, modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${node.totalDirectChildren} زیرفعالیت (${node.totalDescendants} کل)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IndustrialPurple
                                    )
                                }
                            }
                        }

                        // Hours Pill
                        val actHours = if (hasChildren) node.aggregateActualHours else node.item.actualHours
                        val planHours = if (hasChildren) node.aggregatePlannedHours else node.item.durationHours
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "${actHours.toInt()} / ${planHours.toInt()} س",
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }

                        // Manpower
                        if (node.item.manpowerCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "${node.item.manpowerCount} نفر",
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Actions Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentUser?.role == "admin" || currentUser?.role == "planner") {
                            // Add Subtask button
                            FilledTonalButton(
                                onClick = { onAddSubtaskClick(node.item) },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("زیرفعالیت", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            // Edit Button
                            IconButton(
                                onClick = { onEditClick(node.item) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "ویرایش", modifier = Modifier.size(14.dp))
                            }

                            // Delete Button
                            IconButton(
                                onClick = { onDeleteClick(node.item) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                            }
                        }

                        // Quick Update button
                        Button(
                            onClick = { onQuickUpdate(node.item) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IndustrialSteelBlue),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text(if (currentUser?.role == "supervisor") "ثبت کارکرد" else "جزئیات", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Render Children if Expanded
        if (hasChildren && isExpanded) {
            Spacer(modifier = Modifier.height(6.dp))
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
                Spacer(modifier = Modifier.height(4.dp))
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
            // Top Row
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

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "سطح ${item.outlineLevel}",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (item.equipmentName.isNotBlank() && item.equipmentName != item.title) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.equipmentName,
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
                            "blocked" -> "دارای مانع"
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

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

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

            // Action & Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.manpowerCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "${item.manpowerCount} نفر",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "${item.actualHours.toInt()} / ${item.durationHours.toInt()} ساعت",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp
                        )
                    }

                    if (prereqCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = IndustrialAmber.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "$prereqCount پیش‌نیاز",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                color = IndustrialAmberDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentUser?.role == "admin" || currentUser?.role == "planner") {
                        FilledTonalButton(
                            onClick = onAddSubtask,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("زیرفعالیت", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "ویرایش", modifier = Modifier.size(15.dp))
                        }

                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(15.dp))
                        }
                    }

                    Button(
                        onClick = onQuickUpdate,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialSteelBlue),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(if (currentUser?.role == "supervisor") "ثبت کارکرد" else "جزئیات", fontSize = 10.sp)
                    }
                }
            }
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
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = IndustrialSteelBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تعریف زیرفعالیت WBS جدید", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                // Parent Context Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("گره والد (Parent Node):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("${parent.wbsCode}: ${parent.title}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("واحد: ${parent.executiveUnit} • محوطه: ${parent.generalArea}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان زیرفعالیت اجرایی *") },
                    placeholder = { Text("مانند: تعویض رینگ آب‌بندی شفت...") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Activity Presets
                Text("عناوین پیشنهادی سریع:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val presets = listOf("دشارژ و تمیزکاری", "بازرسی چشمی و NDT", "سرویس و روانکاری", "مونتاژ و تست نهایی", "تنظیم و کالیبراسیون")
                    items(presets) { p ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { title = p }
                        ) {
                            Text(p, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), fontSize = 10.sp)
                        }
                    }
                }

                // Duration and Manpower Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it },
                        label = { Text("مدت زمان (ساعت)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = manpowerText,
                        onValueChange = { manpowerText = it },
                        label = { Text("تعداد نفرات") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Equipment & Location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = equipment,
                        onValueChange = { equipment = it },
                        label = { Text("تجهیز") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("محل / زون") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val hrs = durationText.toDoubleOrNull() ?: 4.0
                                val mp = manpowerText.toIntOrNull() ?: 2
                                onConfirm(title, hrs, mp, equipment, location)
                            }
                        },
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialSteelBlue)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ثبت زیرفعالیت", fontWeight = FontWeight.Bold)
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
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AccountTree, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text("هیچ فعالیتی در ساختار شکست کار یافت نشد.", color = Color.Gray)
        }
    }
}
