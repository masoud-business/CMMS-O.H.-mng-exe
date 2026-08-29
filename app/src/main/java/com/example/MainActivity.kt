package com.example

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.OversightItemEntity
import com.example.ui.*
import com.example.ui.components.*
import com.example.ui.theme.MyApplicationTheme

enum class AppTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD("داشبورد", Icons.Default.Dashboard),
    WBS("ساختار WBS", Icons.Default.AccountTree),
    SAFETY_PERMITS("پرمیت‌های ایمنی و HSE", Icons.Default.HealthAndSafety),
    MSP_SYNC("همگام‌سازی MSP", Icons.Default.SyncAlt),
    SESSIONS("جلسات و تصمیمات", Icons.Default.Groups),
    PROCUREMENT("تأمین و خرید", Icons.Default.ShoppingCart),
    AUDIT("سوابق ممیزی", Icons.Default.Assessment)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configure Fullscreen Immersive Mode: Hide status bar to prevent overlap with top app elements
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.statusBars())

        setContent {
            val currentThemeMode by viewModel.appThemeMode.collectAsStateWithLifecycle()
            val currentFontScale by viewModel.appFontScale.collectAsStateWithLifecycle()

            MyApplicationTheme(
                themeMode = currentThemeMode,
                fontScale = currentFontScale
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
                    val allUsers by viewModel.users.collectAsStateWithLifecycle()

                    if (!isLoggedIn) {
                        LoginScreen(
                            users = allUsers,
                            onLogin = { u, p -> viewModel.login(u, p) }
                        )
                    } else {
                        OverhaulCoordinationApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverhaulCoordinationApp(viewModel: MainViewModel) {
    var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }

    // States from ViewModel
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.users.collectAsStateWithLifecycle()
    val oversights by viewModel.oversights.collectAsStateWithLifecycle()
    val selectedOversightId by viewModel.selectedOversightId.collectAsStateWithLifecycle()
    val selectedOversight = oversights.firstOrNull { it.id == selectedOversightId }

    val rawItems by viewModel.rawOversightItems.collectAsStateWithLifecycle()
    val filteredItems by viewModel.filteredItems.collectAsStateWithLifecycle()
    val wbsTree by viewModel.wbsTree.collectAsStateWithLifecycle()
    val expandedTreeNodes by viewModel.expandedTreeNodes.collectAsStateWithLifecycle()
    val wbsViewMode by viewModel.wbsViewMode.collectAsStateWithLifecycle()
    val allPrerequisites by viewModel.allPrerequisites.collectAsStateWithLifecycle()
    val allAssignments by viewModel.allAssignments.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedUnitFilter by viewModel.selectedUnitFilter.collectAsStateWithLifecycle()
    val selectedStatusFilter by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val supervisorFilter by viewModel.supervisorFilterMode.collectAsStateWithLifecycle()
    val showOnlyMyTasks by viewModel.showOnlyMyTasks.collectAsStateWithLifecycle()
    val expandedUnits by viewModel.expandedUnits.collectAsStateWithLifecycle()
    val expandedAreas by viewModel.expandedAreas.collectAsStateWithLifecycle()

    val dashboardKpis by viewModel.dashboardKpis.collectAsStateWithLifecycle()
    val unitAnalytics by viewModel.unitAnalytics.collectAsStateWithLifecycle()
    val areaAnalyticsList by viewModel.areaAnalyticsList.collectAsStateWithLifecycle()
    val selectedUnitForDashboard by viewModel.selectedUnitForDashboard.collectAsStateWithLifecycle()
    val selectedAreaForDashboard by viewModel.selectedAreaForDashboard.collectAsStateWithLifecycle()
    val unitComparisons by viewModel.unitProgressComparisons.collectAsStateWithLifecycle()
    val timelinePoints by viewModel.projectTimelinePoints.collectAsStateWithLifecycle()
    val overallPlannedProgress by viewModel.overallPlannedProgress.collectAsStateWithLifecycle()
    val dailyLogs by viewModel.dailyLogs.collectAsStateWithLifecycle()
    val exportedMspCsv by viewModel.exportedMspCsv.collectAsStateWithLifecycle()
    val importPreview by viewModel.importPreview.collectAsStateWithLifecycle()

    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val sessionDecisions by viewModel.sessionDecisions.collectAsStateWithLifecycle()
    val sessionNotes by viewModel.sessionNotes.collectAsStateWithLifecycle()
    val procurements by viewModel.procurements.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val safetyPermits by viewModel.safetyPermits.collectAsStateWithLifecycle()
    val electricalLotoPermits by viewModel.electricalLotoPermits.collectAsStateWithLifecycle()
    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Dialog States
    var selectedItemForDailyUpdate by remember { mutableStateOf<OversightItemEntity?>(null) }
    var editingWbsItem by remember { mutableStateOf<OversightItemEntity?>(null) }
    var showAddWbsItemDialog by remember { mutableStateOf(false) }
    var showAddSessionDialog by remember { mutableStateOf(false) }
    var activeSessionIdForDecision by remember { mutableStateOf<Long?>(null) }
    var activeSessionIdForNote by remember { mutableStateOf<Long?>(null) }
    var showAddProcurementDialog by remember { mutableStateOf(false) }
    var showUserSettingsDialog by remember { mutableStateOf(false) }
    var showProjectReportDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    val roleTabs = remember(currentUser, dashboardKpis) {
        getNavigationTabsForRole(
            user = currentUser,
            kpis = dashboardKpis,
            pendingSyncCount = dashboardKpis.inProgressCount,
            blockedTaskCount = dashboardKpis.blockedCount
        )
    }

    // Ensure valid tab when switching roles
    LaunchedEffect(roleTabs) {
        if (roleTabs.none { it.tab == currentTab }) {
            currentTab = roleTabs.firstOrNull()?.tab ?: AppTab.DASHBOARD
        }
    }

    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg.text,
                duration = if (msg.isError) SnackbarDuration.Long else SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val isError = uiMessage?.isError == true
                Snackbar(
                    containerColor = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column {
                        Text(text = data.visuals.message, fontWeight = FontWeight.Bold)
                        if (uiMessage?.details?.isNotEmpty() == true) {
                            Spacer(modifier = Modifier.height(4.dp))
                            uiMessage?.details?.forEach { detail ->
                                Text(text = "• $detail", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        topBar = {
            if (!isLandscape) {
                AppHeader(
                    currentUser = currentUser,
                    allUsers = allUsers,
                    oversights = oversights,
                    selectedOversightId = selectedOversightId,
                    onSwitchUser = { viewModel.switchUserDirectly(it) },
                    onSelectOversight = { viewModel.selectOversight(it) },
                    onLogout = { viewModel.logout() },
                    onOpenSettings = { showUserSettingsDialog = true },
                    onResetSeedData = { viewModel.resetAndSeedDatabase() }
                )
            }
        },
        bottomBar = {
            if (!isLandscape) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    roleTabs.forEach { tabConfig ->
                        val isSelected = currentTab == tabConfig.tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tabConfig.tab },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (tabConfig.badgeCount > 0 && !isSelected) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.error
                                            ) {
                                                Text("${tabConfig.badgeCount}")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(imageVector = tabConfig.icon, contentDescription = tabConfig.title)
                                }
                            },
                            label = {
                                Text(
                                    text = tabConfig.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.DASHBOARD -> DashboardTab(
                    kpis = dashboardKpis,
                    unitAnalytics = unitAnalytics,
                    areaAnalyticsList = areaAnalyticsList,
                    unitComparisons = unitComparisons,
                    timelinePoints = timelinePoints,
                    overallPlannedProgress = overallPlannedProgress,
                    overallActualProgress = dashboardKpis.overallProgressPercent.toFloat(),
                    selectedAreaName = selectedAreaForDashboard,
                    selectedUnitName = selectedUnitForDashboard,
                    currentUser = currentUser,
                    oversight = selectedOversight,
                    allUsers = allUsers,
                    rawItems = rawItems,
                    onSelectArea = { viewModel.selectedAreaForDashboard.value = it },
                    onSelectUnit = { viewModel.selectedUnitForDashboard.value = it },
                    onNavigateToWbs = { currentTab = AppTab.WBS },
                    onNavigateToEodSync = { currentTab = AppTab.MSP_SYNC },
                    onNavigateToProcurement = { currentTab = AppTab.PROCUREMENT },
                    onItemClickForDailyUpdate = { selectedItemForDailyUpdate = it }
                )

                AppTab.WBS -> WbsExecutionTab(
                    items = filteredItems,
                    wbsTree = wbsTree,
                    allPrerequisites = allPrerequisites,
                    allAssignments = allAssignments,
                    allUsers = allUsers,
                    currentUser = currentUser,
                    searchQuery = searchQuery,
                    selectedUnit = selectedUnitFilter,
                    selectedStatus = selectedStatusFilter,
                    supervisorFilter = supervisorFilter,
                    showOnlyMine = showOnlyMyTasks,
                    expandedUnits = expandedUnits,
                    expandedAreas = expandedAreas,
                    expandedTreeNodes = expandedTreeNodes,
                    currentViewMode = wbsViewMode,
                    onSearchChange = { viewModel.searchQuery.value = it },
                    onUnitSelect = { viewModel.selectedUnitFilter.value = it },
                    onStatusSelect = { viewModel.selectedStatusFilter.value = it },
                    onSupervisorFilterSelect = { viewModel.supervisorFilterMode.value = it },
                    onToggleOnlyMine = { viewModel.showOnlyMyTasks.value = it },
                    onToggleUnitExpansion = { viewModel.toggleUnitExpansion(it) },
                    onToggleAreaExpansion = { viewModel.toggleAreaExpansion(it) },
                    onToggleTreeNodeExpanded = { viewModel.toggleTreeNodeExpanded(it) },
                    onExpandAllTreeNodes = { viewModel.expandAllTreeNodes() },
                    onCollapseAllTreeNodes = { viewModel.collapseAllTreeNodes() },
                    onSetViewMode = { viewModel.setWbsViewMode(it) },
                    onItemClickForDailyUpdate = { selectedItemForDailyUpdate = it },
                    onAddNewItem = { showAddWbsItemDialog = true },
                    onAddSubtask = { parent, title, duration, manpower, equip, loc ->
                        viewModel.addSubtask(
                            parentItem = parent,
                            title = title,
                            durationHours = duration,
                            manpowerCount = manpower,
                            equipmentName = equip,
                            executionLocation = loc
                        )
                    },
                    onDeleteHierarchy = { itemId ->
                        viewModel.deleteWbsHierarchy(itemId)
                    },
                    onEditItem = { item ->
                        editingWbsItem = item
                    }
                )

                AppTab.SAFETY_PERMITS -> SafetyPermitsTab(
                    permits = safetyPermits,
                    wbsItems = rawItems,
                    currentUser = currentUser,
                    onIssuePermit = { viewModel.issueSafetyPermit(it) },
                    onUpdatePermitStatus = { id, status -> viewModel.updateSafetyPermitStatus(id, status) },
                    onUpdateLotoStatus = { id, status, taggedBy -> viewModel.updateElectricalLotoStatus(id, status, taggedBy) },
                    onDeletePermit = { viewModel.deleteSafetyPermit(it) }
                )

                AppTab.MSP_SYNC -> MspSyncTab(
                    dailyLogs = dailyLogs,
                    exportedMspCsv = exportedMspCsv,
                    preview = importPreview,
                    currentUser = currentUser,
                    onGenerateEodExport = { viewModel.generateEodMspExport() },
                    onImportUpdatedSchedule = { viewModel.parseCustomCsvText(it) },
                    onLoadSampleGhadirData = { viewModel.loadGhadirNeyrizSampleCsvForPreview() },
                    onCommitImport = { viewModel.commitMsProjectImport() },
                    onCancelPreview = { viewModel.cancelImportPreview() },
                    onClearExport = { viewModel.clearExportedMspCsv() },
                    onCorrectDailyLog = { logId, progress, manpower, hours, remarks, issues ->
                        viewModel.correctDailyWorkLog(logId, progress, manpower, hours, remarks, issues)
                    }
                )

                AppTab.SESSIONS -> SessionsDecisionsTab(
                    sessions = sessions,
                    decisions = sessionDecisions,
                    notes = sessionNotes,
                    wbsItems = rawItems,
                    currentUser = currentUser,
                    onAddSession = { showAddSessionDialog = true },
                    onAddDecision = { sessionId -> activeSessionIdForDecision = sessionId },
                    onAddNote = { sessionId -> activeSessionIdForNote = sessionId }
                )

                AppTab.PROCUREMENT -> ProcurementTab(
                    procurements = procurements,
                    wbsItems = rawItems,
                    currentUser = currentUser,
                    onAddRequest = { showAddProcurementDialog = true },
                    onApproveRequest = { viewModel.updateProcurementStatus(it, "approved") },
                    onRejectRequest = { id, reason -> viewModel.updateProcurementStatus(id, "rejected", reason) },
                    onUpdateStatus = { id, newStatus -> viewModel.updateProcurementStatus(id, newStatus) }
                )

                AppTab.AUDIT -> AuditAndReportsTab(
                    auditLogs = auditLogs,
                    currentUser = currentUser,
                    kpis = dashboardKpis
                )
            }

            // Landscape Floating Glass Bubbles Overlay
            if (isLandscape) {
                LandscapeFloatingGlassControls(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it },
                    roleTabs = roleTabs,
                    currentUser = currentUser,
                    allUsers = allUsers,
                    oversights = oversights,
                    selectedOversightId = selectedOversightId,
                    onSwitchUser = { viewModel.switchUserDirectly(it) },
                    onSelectOversight = { viewModel.selectOversight(it) },
                    onLogout = { viewModel.logout() },
                    onOpenSettings = { showUserSettingsDialog = true }
                )
            }
        }
    }

    // --- Dialogs ---

    // 1. Daily Progress and Hours Modal for Supervisor / Planner
    selectedItemForDailyUpdate?.let { item ->
        val itemPrereqIds = allPrerequisites.filter { it.itemId == item.id }.map { it.prerequisiteItemId }
        val prereqItems = rawItems.filter { itemPrereqIds.contains(it.id) }

        SupervisorDailyUpdateDialog(
            item = item,
            prerequisites = prereqItems,
            currentUser = currentUser,
            onDismiss = { selectedItemForDailyUpdate = null },
            onConfirm = { newProgress, manpower, hours, status, remarks, issues ->
                viewModel.recordDailySupervisorWork(
                    itemId = item.id,
                    newProgress = newProgress,
                    manpowerCount = manpower,
                    hoursSpent = hours,
                    status = status,
                    remarks = remarks,
                    issues = issues
                )
                selectedItemForDailyUpdate = null
            }
        )
    }

    // 2. Add WBS Item Modal
    if (showAddWbsItemDialog) {
        val supervisorsList = allUsers.filter { it.role == "supervisor" }
        AddWbsItemDialog(
            existingItems = rawItems,
            supervisors = supervisorsList,
            onDismiss = { showAddWbsItemDialog = false },
            onConfirm = { title, wbsCode, parentItemId, outlineLevel, executiveUnit, generalArea, executionLocation, equipmentName, duration, plannedStart, plannedEnd, prereqIds, supIds ->
                viewModel.createWbsItem(
                    title = title,
                    wbsCode = wbsCode,
                    parentItemId = parentItemId,
                    outlineLevel = outlineLevel,
                    executiveUnit = executiveUnit,
                    generalArea = generalArea,
                    executionLocation = executionLocation,
                    equipmentName = equipmentName,
                    durationHours = duration,
                    plannedStart = plannedStart,
                    plannedEnd = plannedEnd,
                    prerequisiteIds = prereqIds,
                    supervisorIds = supIds
                )
                showAddWbsItemDialog = false
            }
        )
    }

    // 2.1 Edit WBS Item Modal
    editingWbsItem?.let { itemToEdit ->
        val supervisorsList = allUsers.filter { it.role == "supervisor" }
        EditWbsItemDialog(
            item = itemToEdit,
            existingItems = rawItems.filter { it.id != itemToEdit.id },
            supervisors = supervisorsList,
            allPrerequisites = allPrerequisites,
            allAssignments = allAssignments,
            onDismiss = { editingWbsItem = null },
            onConfirm = { updatedItem, prereqIds, supIds ->
                viewModel.updateWbsItem(updatedItem, prereqIds, supIds)
                editingWbsItem = null
            }
        )
    }

    // 3. Add Planning Session Modal
    if (showAddSessionDialog) {
        AddPlanningSessionDialog(
            onDismiss = { showAddSessionDialog = false },
            onConfirm = { title: String, date: String, location: String, minutes: String ->
                viewModel.createPlanningSession(title, date, location, minutes)
                showAddSessionDialog = false
            }
        )
    }

    // 4. Add Decision Modal
    activeSessionIdForDecision?.let { sessionId ->
        AddDecisionDialog(
            wbsItems = rawItems,
            onDismiss = { activeSessionIdForDecision = null },
            onConfirm = { decisionText: String, assignedUnit: String, itemId: Long? ->
                viewModel.addSessionDecision(sessionId, itemId, decisionText, assignedUnit)
                activeSessionIdForDecision = null
            }
        )
    }

    // 5. Add Note Modal
    activeSessionIdForNote?.let { sessionId ->
        AddDecisionDialog(
            wbsItems = rawItems,
            onDismiss = { activeSessionIdForNote = null },
            onConfirm = { noteText: String, _: String, itemId: Long? ->
                viewModel.addSessionNote(sessionId, itemId, noteText)
                activeSessionIdForNote = null
            }
        )
    }

    // 6. Add Procurement Request Modal
    if (showAddProcurementDialog) {
        AddProcurementRequestDialog(
            wbsItems = rawItems,
            onDismiss = { showAddProcurementDialog = false },
            onConfirm = { title: String, itemType: String, quantity: String, estimatedCost: String, itemId: Long ->
                viewModel.createProcurementRequest(itemId, null, title, itemType, quantity, estimatedCost)
                showAddProcurementDialog = false
            }
        )
    }

    // 7. User Settings & Appearance Modal
    if (showUserSettingsDialog) {
        val currentThemeMode by viewModel.appThemeMode.collectAsStateWithLifecycle()
        val currentFontScale by viewModel.appFontScale.collectAsStateWithLifecycle()
        val currentPersianFont by viewModel.appPersianFont.collectAsStateWithLifecycle()

        UserSettingsDialog(
            user = currentUser,
            currentThemeMode = currentThemeMode,
            currentFontScale = currentFontScale,
            currentPersianFont = currentPersianFont,
            onSetThemeMode = { mode ->
                viewModel.setThemeMode(mode)
            },
            onSetFontScale = { scale ->
                viewModel.setFontScale(scale)
            },
            onSetPersianFont = { font ->
                viewModel.setPersianFont(font)
            },
            onChangePassword = { oldPass, newPass, confirmPass ->
                viewModel.changePassword(oldPass, newPass, confirmPass)
            },
            onViewProjectReport = {
                showUserSettingsDialog = false
                showProjectReportDialog = true
            },
            onDismiss = { showUserSettingsDialog = false }
        )
    }

    // 8. Project Technical Report & Documentation Dialog
    if (showProjectReportDialog) {
        ProjectReportViewerDialog(
            onDismiss = { showProjectReportDialog = false }
        )
    }
}
