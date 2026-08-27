package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.dao.OverhaulDao
import com.example.data.db.AppDatabase
import com.example.data.entity.*
import com.example.data.importer.MsProjectImporter
import com.example.data.importer.ParsedImportPreview
import com.example.data.service.OverhaulService
import com.example.data.service.ServiceResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UiMessage(
    val text: String,
    val isError: Boolean = false,
    val details: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class DashboardKpis(
    val totalItems: Int = 0,
    val completedCount: Int = 0,
    val inProgressCount: Int = 0,
    val blockedCount: Int = 0,
    val pendingCount: Int = 0,
    val overallProgressPercent: Int = 0,
    val totalManpowerToday: Int = 0,
    val totalActualHours: Double = 0.0,
    val pendingProcurementsCount: Int = 0,
    val criticalBottlenecks: List<OversightItemEntity> = emptyList(),
    val assignedToCurrentUserCount: Int = 0
)

data class AreaProgressComparison(
    val areaName: String,
    val actualProgress: Float,
    val plannedProgress: Float,
    val totalTasks: Int,
    val completedTasks: Int
)

data class UnitProgressComparison(
    val unitName: String,
    val actualProgress: Float,
    val plannedProgress: Float,
    val variance: Float, // actualProgress - plannedProgress
    val totalTasks: Int,
    val completedTasks: Int,
    val inProgressTasks: Int,
    val blockedTasks: Int,
    val pendingTasks: Int,
    val plannedHours: Double,
    val actualHours: Double,
    val spi: Float,
    val areas: List<AreaProgressComparison> = emptyList()
)

data class TimelineProgressPoint(
    val dayNumber: Int,
    val dayLabel: String,
    val plannedProgress: Float,
    val actualProgress: Float? = null,
    val isMilestone: Boolean = false,
    val milestoneTitle: String? = null
)

data class AreaAnalytics(
    val areaKey: String = "",
    val areaTitle: String = "",
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val inProgressTasks: Int = 0,
    val blockedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val actualProgress: Float = 0f,
    val plannedProgress: Float = 0f,
    val variance: Float = 0f,
    val spi: Float = 1.0f,
    val totalManpower: Int = 0,
    val totalActualHours: Double = 0.0,
    val unitBreakdown: Map<String, Int> = emptyMap(),
    val criticalTasks: List<OversightItemEntity> = emptyList()
)

data class UnitAnalytics(
    val unitName: String = "",
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val inProgressTasks: Int = 0,
    val blockedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val progressPercentage: Int = 0,
    val plannedProgress: Float = 0f,
    val variance: Float = 0f,
    val spi: Float = 1.0f,
    val totalManpower: Int = 0,
    val totalActualHours: Double = 0.0,
    val areaBreakdown: Map<String, Int> = emptyMap(),
    val criticalTasks: List<OversightItemEntity> = emptyList()
)

data class WbsTreeNode(
    val item: OversightItemEntity,
    val children: List<WbsTreeNode> = emptyList(),
    val totalDirectChildren: Int = 0,
    val totalDescendants: Int = 0,
    val aggregateProgress: Int = 0,
    val aggregateActualHours: Double = 0.0,
    val aggregatePlannedHours: Double = 0.0,
    val assignedSupervisors: List<UserEntity> = emptyList(),
    val prerequisites: List<OversightItemEntity> = emptyList()
)

data class TaskLogisticsTag(
    val id: String,
    val label: String,
    val shortLabel: String,
    val iconName: String,
    val colorHex: Long,
    val isHeavyEquipment: Boolean = false
)

fun getLogisticsTagsForItem(item: OversightItemEntity): List<TaskLogisticsTag> {
    val tags = mutableListOf<TaskLogisticsTag>()
    val text = (item.title + " " + item.equipmentName + " " + item.executionLocation + " " + item.notes).lowercase()

    // Crane (نیاز به جرثقیل سنگین/سبک)
    if (text.contains("جرثقیل") || text.contains("وینچ") || text.contains("روتور") || text.contains("سکتور پلیت") ||
        text.contains("اسکوئر بار") || text.contains("تعویض لوله") || text.contains("الکتروموتور") || text.contains("دشارژ") ||
        text.contains("بارگیری") || text.contains("دیسمانتل") || text.contains("اسکرابر") || text.contains("فن") ||
        text.contains("لگ داخلی") || text.contains("شارژ گندله")) {
        val craneType = if (text.contains("سکتور") || text.contains("اسکوئر") || text.contains("کوره") || text.contains("اسکرابر")) "جرثقیل ۵۰ تن" else "جرثقیل ۲۵/۳۵ تن"
        tags.add(TaskLogisticsTag("CRANE", craneType, "جرثقیل", "crane", 0xFFD97706, isHeavyEquipment = true))
    }

    // Scaffolding (نیاز به داربست‌بندی و کار در ارتفاع)
    if (text.contains("داربست") || text.contains("نسوز") || text.contains("ارتفاع") || text.contains("منهول") ||
        text.contains("تاور") || text.contains("داکت") || text.contains("سقف") || text.contains("لگ") ||
        text.contains("گالری") || text.contains("روشنایی") || text.contains("اسکرابر")) {
        tags.add(TaskLogisticsTag("SCAFFOLDING", "داربست‌بندی کارگاهی", "داربست", "scaffolding", 0xFF2563EB, isHeavyEquipment = true))
    }

    // Flushing / Chemical / Nitrogen Purge (فلاشینگ مسیر و شستشو)
    if (text.contains("فلاشینگ") || text.contains("شستشو") || text.contains("اسیدشویی") || text.contains("نیتروژن") ||
        text.contains("روغن") || text.contains("پک هیدرولیک") || text.contains("کولینگ") || text.contains("خطوط لوله") ||
        text.contains("تخلیه لجن") || text.contains("تست نشتی") || text.contains("تزریق گاز")) {
        tags.add(TaskLogisticsTag("FLUSHING", "فلاشینگ و شستشوی خطوط", "فلاشینگ", "water_drop", 0xFF0D9488, isHeavyEquipment = false))
    }

    // LOTO & Electrical Isolation (ایزولاسیون مکانیکی/برقی)
    if (text.contains("loto") || text.contains("ایزولاسیون") || text.contains("قطع برق") || text.contains("پرمیت") ||
        text.contains("قفل") || text.contains("تابلو") || text.contains("موتور") || item.executiveUnit == "برق") {
        tags.add(TaskLogisticsTag("LOTO", "ایزولاسیون برقی/LOTO", "LOTO", "lock", 0xFFE11D48, isHeavyEquipment = false))
    }

    // Hot Work Permit (پروانه کار گرم / برشکاری و جوشکاری)
    if (text.contains("جوشکاری") || text.contains("برشکاری") || text.contains("هات ورک") || text.contains("ورق") ||
        text.contains("شوت") || text.contains("لاینر") || text.contains("تعویض پلیت")) {
        tags.add(TaskLogisticsTag("HOT_WORK", "پروانه کار گرم و جوشکاری", "کار گرم", "local_fire_department", 0xFFEA580C, isHeavyEquipment = false))
    }

    // NDT & Quality Inspection (تست و بازرسی فنی)
    if (text.contains("تست") || text.contains("بازرسی") || text.contains("ndt") || text.contains("ضخامت") ||
        text.contains("آلتراسونیک") || text.contains("ویبراسیون") || item.executiveUnit == "بازرسی فنی") {
        tags.add(TaskLogisticsTag("NDT", "بازرسی و کنترل کیفیت NDT", "بازرسی", "verified", 0xFF7C3AED, isHeavyEquipment = false))
    }

    return tags
}

enum class WbsViewMode {
    TREE,                // نمایش درختی تو در تو با محاسبات تجمعی
    INDUSTRIAL_MATRIX,   // ساختار صنعتی (واحد -> محوطه -> زون -> تجهیز -> فعالیت)
    LIST_OUTLINE,        // فهرست خطی با تورفتگی کدهای ساختار شکست WBS
    QUICK_REVIEW,        // مرور سریع و آکاردئونی لیست تسک‌ها با جزئیات کامل درجا
    LOGISTICS_SCHEDULE   // پایش و زمانبندی جرثقیل، داربست و فلاشینگ امروز و فردا
}

enum class SupervisorFilter {
    ALL,
    TODAY_TASKS,
    INCOMPLETE_DUE,
    IN_PROGRESS,
    BLOCKED,
    COMPLETED
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("ghadir_auth_prefs", Context.MODE_PRIVATE)
    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val dao: OverhaulDao = db.overhaulDao()
    private val service = OverhaulService(dao)
    private val importer = MsProjectImporter()

    // 1. احراز هویت و ذخیره نشست کاربر (Authentication & Session Persistence)
    val users = dao.getAllActiveUsers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // 2. برنامه‌های اورهال
    val oversights = dao.getAllOversights().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _selectedOversightId = MutableStateFlow<Long?>(null)
    val selectedOversightId: StateFlow<Long?> = _selectedOversightId.asStateFlow()

    // 3. آیتم‌های WBS و فیلترها
    val allPrerequisites = dao.getAllPrerequisites().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allAssignments = dao.getAllAssignments().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchQuery = MutableStateFlow("")
    val selectedUnitFilter = MutableStateFlow<String?>(null)
    val selectedStatusFilter = MutableStateFlow<String?>(null)
    val supervisorFilterMode = MutableStateFlow(SupervisorFilter.ALL)
    val showOnlyMyTasks = MutableStateFlow(false)
    val selectedSupervisorForUnitHead = MutableStateFlow<Long?>(null)

    fun selectSupervisorForUnitHead(userId: Long?) {
        selectedSupervisorForUnitHead.value = userId
    }

    // حالت‌های باز/بسته بودن درخت دسته‌بندی WBS
    val expandedUnits = MutableStateFlow<Set<String>>(setOf("مکانیک", "برق", "ابزار دقیق", "نسوز", "اتوماسیون"))
    val expandedAreas = MutableStateFlow<Set<String>>(setOf("Core Area", "Blower Area", "Reformer Area", "Water System Area", "MHU"))
    val expandedTreeNodes = MutableStateFlow<Set<Long>>(emptySet())
    val wbsViewMode = MutableStateFlow(WbsViewMode.TREE)

    // آیتم‌های خام WBS برای برنامه فعال
    val rawOversightItems: StateFlow<List<OversightItemEntity>> = _selectedOversightId
        .flatMapLatest { id ->
            if (id != null) dao.getItemsForOversight(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // درخت سلسله‌مراتبی ساختار شکست کار (Hierarchical WBS Tree with Nested Task Aggregation)
    val wbsTree: StateFlow<List<WbsTreeNode>> = combine(
        rawOversightItems,
        allPrerequisites,
        allAssignments,
        users
    ) { items, prereqs, assignments, userList ->
        if (items.isEmpty()) return@combine emptyList<WbsTreeNode>()

        val itemMap = items.associateBy { it.id }
        val userMap = userList.associateBy { it.id }
        val prereqGroup = prereqs.groupBy { it.itemId }
        val assignmentGroup = assignments.groupBy { it.itemId }

        // نگاشت والد-فرزند با پشتیبانی از parentItemId و همچنین الگوهای کدهای WBS
        val wbsCodeMap = items.associateBy { it.wbsCode.trim() }
        val resolvedParentMap = mutableMapOf<Long, Long?>()

        for (item in items) {
            if (item.parentItemId != null && item.parentItemId > 0 && itemMap.containsKey(item.parentItemId)) {
                resolvedParentMap[item.id] = item.parentItemId
            } else if (item.wbsCode.contains(".")) {
                val parentCode = item.wbsCode.substringBeforeLast(".").trim()
                val parentObj = wbsCodeMap[parentCode]
                if (parentObj != null && parentObj.id != item.id) {
                    resolvedParentMap[item.id] = parentObj.id
                } else {
                    resolvedParentMap[item.id] = null
                }
            } else {
                resolvedParentMap[item.id] = null
            }
        }

        val childrenByParentId = items.groupBy { resolvedParentMap[it.id] }

        fun buildNode(item: OversightItemEntity): WbsTreeNode {
            val childItems = childrenByParentId[item.id] ?: emptyList()
            val childNodes = childItems.map { buildNode(it) }

            val totalDescendants = childNodes.sumOf { 1 + it.totalDescendants }
            val aggregateProgress = if (childNodes.isNotEmpty()) {
                (childNodes.sumOf { it.aggregateProgress } / childNodes.size).coerceIn(0, 100)
            } else {
                item.progressPercentage
            }
            val aggregateActualHours = item.actualHours + childNodes.sumOf { it.aggregateActualHours }
            val aggregatePlannedHours = item.durationHours + childNodes.sumOf { it.aggregatePlannedHours }

            val assignedIds = assignmentGroup[item.id]?.map { it.supervisorUserId } ?: emptyList()
            val assignedUsers = assignedIds.mapNotNull { userMap[it] }

            val prereqIds = prereqGroup[item.id]?.map { it.prerequisiteItemId } ?: emptyList()
            val prereqItems = prereqIds.mapNotNull { itemMap[it] }

            return WbsTreeNode(
                item = item,
                children = childNodes,
                totalDirectChildren = childNodes.size,
                totalDescendants = totalDescendants,
                aggregateProgress = aggregateProgress,
                aggregateActualHours = aggregateActualHours,
                aggregatePlannedHours = aggregatePlannedHours,
                assignedSupervisors = assignedUsers,
                prerequisites = prereqItems
            )
        }

        val rootItems = childrenByParentId[null] ?: items.filter { it.outlineLevel == 1 }
        rootItems.map { buildNode(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    data class TaskFilterParams(
        val query: String = "",
        val unit: String? = null,
        val status: String? = null,
        val supervisorFilter: SupervisorFilter = SupervisorFilter.ALL,
        val showOnlyMine: Boolean = false,
        val selectedSupervisorId: Long? = null
    )

    private val filterParams: Flow<TaskFilterParams> = combine(
        combine(searchQuery, selectedUnitFilter, selectedStatusFilter) { q, u, s -> Triple(q, u, s) },
        combine(supervisorFilterMode, showOnlyMyTasks, selectedSupervisorForUnitHead) { f, m, sid -> Triple(f, m, sid) }
    ) { (query, unit, status), (supFilter, showMine, supId) ->
        TaskFilterParams(query, unit, status, supFilter, showMine, supId)
    }

    // آیتم‌های فیلتر شده بر اساس نقش، واحد، جستجو و فیلترهای روزانه ناظر
    val filteredItems: StateFlow<List<OversightItemEntity>> = combine(
        rawOversightItems,
        filterParams,
        _currentUser,
        allAssignments
    ) { items, params, user, assignments ->
        val userAssignedItemIds = if (user != null) {
            assignments.filter { it.supervisorUserId == user.id }.map { it.itemId }.toSet()
        } else emptySet()

        val specificSupervisorItemIds = if (params.selectedSupervisorId != null) {
            assignments.filter { it.supervisorUserId == params.selectedSupervisorId }.map { it.itemId }.toSet()
        } else null

        items.filter { item ->
            // فیلتر جستجو
            val matchesQuery = params.query.isBlank() ||
                    item.title.contains(params.query, ignoreCase = true) ||
                    item.wbsCode.contains(params.query, ignoreCase = true) ||
                    item.executiveUnit.contains(params.query, ignoreCase = true) ||
                    item.generalArea.contains(params.query, ignoreCase = true) ||
                    item.equipmentName.contains(params.query, ignoreCase = true)

            // فیلتر واحد اجرایی
            val matchesUnit = when {
                user?.role == "supervisor" && user.unit != null -> item.executiveUnit == user.unit || userAssignedItemIds.contains(item.id)
                user?.role == "unit_head" && user.unit != null -> item.executiveUnit == user.unit
                params.unit != null -> item.executiveUnit == params.unit
                else -> true
            }

            // فیلتر وضعیت
            val matchesStatus = params.status == null || item.status == params.status

            // فیلتر ویژه سرپرست و ناظر (کارهای تا امروز، کارهای انجام نشده، و ...)
            val matchesSupervisorMode = when (params.supervisorFilter) {
                SupervisorFilter.ALL -> true
                SupervisorFilter.TODAY_TASKS -> item.status == "in_progress" || item.plannedStartDate.contains("10/12") || item.plannedStartDate.contains("10/13") || item.plannedStartDate.contains("10/14")
                SupervisorFilter.INCOMPLETE_DUE -> (item.status != "completed" && item.progressPercentage < 100)
                SupervisorFilter.IN_PROGRESS -> item.status == "in_progress"
                SupervisorFilter.BLOCKED -> item.status == "blocked"
                SupervisorFilter.COMPLETED -> item.status == "completed"
            }

            val matchesOnlyMine = !params.showOnlyMine || userAssignedItemIds.contains(item.id)
            val matchesSelectedSupervisor = specificSupervisorItemIds == null || specificSupervisorItemIds.contains(item.id)

            matchesQuery && matchesUnit && matchesStatus && matchesSupervisorMode && matchesOnlyMine && matchesSelectedSupervisor
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 4. شاخص‌های کلیدی داشبورد عمومی و مهندسی
    val dashboardKpis: StateFlow<DashboardKpis> = combine(
        rawOversightItems,
        _currentUser,
        allAssignments,
        dao.getAllProcurements()
    ) { items, user, assignments, procurements ->
        val userAssignedItemIds = if (user != null) {
            assignments.filter { it.supervisorUserId == user.id }.map { it.itemId }.toSet()
        } else emptySet()

        val activeItems = items.filter { it.active }
        val total = activeItems.size
        val completed = activeItems.count { it.status == "completed" }
        val inProgress = activeItems.count { it.status == "in_progress" }
        val blocked = activeItems.count { it.status == "blocked" }
        val pending = activeItems.count { it.status == "pending" }

        val avgProgress = if (total > 0) {
            activeItems.sumOf { it.progressPercentage } / total
        } else 0

        val totalManpower = activeItems.sumOf { it.manpowerCount }
        val totalHours = activeItems.sumOf { it.actualHours }

        val pendingProc = procurements.count { it.status == "requested" }
        val bottlenecks = activeItems.filter { it.status == "blocked" }
        val myCount = activeItems.count { userAssignedItemIds.contains(it.id) }

        DashboardKpis(
            totalItems = total,
            completedCount = completed,
            inProgressCount = inProgress,
            blockedCount = blocked,
            pendingCount = pending,
            overallProgressPercent = avgProgress,
            totalManpowerToday = totalManpower,
            totalActualHours = totalHours,
            pendingProcurementsCount = pendingProc,
            criticalBottlenecks = bottlenecks,
            assignedToCurrentUserCount = myCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardKpis())

    // 5. داشبورد و نمودارهای اختصاصی مدیر/رئیس واحد اجرایی
    val selectedUnitForDashboard = MutableStateFlow("مکانیک")
    val selectedAreaForDashboard = MutableStateFlow("Core Area")

    fun selectUnitForDashboard(unitName: String) {
        selectedUnitForDashboard.value = unitName
    }

    fun selectAreaForDashboard(areaName: String) {
        selectedAreaForDashboard.value = areaName
    }

    val unitAnalytics: StateFlow<UnitAnalytics> = combine(
        rawOversightItems,
        _currentUser,
        selectedUnitForDashboard
    ) { items, user, selectedUnit ->
        val targetUnit = if (user?.role == "unit_head" && user.unit != null) {
            user.unit
        } else {
            selectedUnit
        }

        // Normalize instrumentation/automation if requested
        val unitTasks = items.filter { item ->
            val u = item.executiveUnit
            if (targetUnit.contains("ابزاردقیق") || targetUnit.contains("اتوماسیون")) {
                u.contains("ابزار") || u.contains("اتوماسیون")
            } else {
                u == targetUnit
            }
        }.filter { it.active }

        val total = unitTasks.size
        val completed = unitTasks.count { it.status == "completed" }
        val inProgress = unitTasks.count { it.status == "in_progress" }
        val blocked = unitTasks.count { it.status == "blocked" }
        val pending = unitTasks.count { it.status == "pending" }

        val actualProgressFloat = if (total > 0) unitTasks.map { it.progressPercentage.toFloat() }.average().toFloat() else 0f
        val progress = actualProgressFloat.toInt()
        val manpower = unitTasks.sumOf { it.manpowerCount }
        val actualHours = unitTasks.sumOf { it.actualHours }

        val plannedMap = mapOf(
            "مکانیک" to 42.0f,
            "برق" to 36.0f,
            "ابزاردقیق و اتوماسیون" to 31.0f,
            "نسوز" to 58.0f,
            "انرژی و سیالات" to 44.0f,
            "بازرسی فنی" to 38.0f
        )
        val plannedProg = plannedMap[targetUnit] ?: 35.0f
        val variance = actualProgressFloat - plannedProg
        val spi = if (plannedProg > 0f) (actualProgressFloat / plannedProg).coerceIn(0f, 3f) else 1.0f

        val areaMap = unitTasks.groupBy { it.generalArea.ifBlank { "عمومی" } }
            .mapValues { (_, list) ->
                if (list.isNotEmpty()) list.sumOf { it.progressPercentage } / list.size else 0
            }

        val critical = unitTasks.filter { it.status == "blocked" || (it.status == "in_progress" && it.progressPercentage < 50) }

        UnitAnalytics(
            unitName = targetUnit,
            totalTasks = total,
            completedTasks = completed,
            inProgressTasks = inProgress,
            blockedTasks = blocked,
            pendingTasks = pending,
            progressPercentage = progress,
            plannedProgress = plannedProg,
            variance = variance,
            spi = spi,
            totalManpower = manpower,
            totalActualHours = actualHours,
            areaBreakdown = areaMap,
            criticalTasks = critical
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UnitAnalytics())

    // 5.1 تحلیل و داشبورد تفکیکی نواحی اصلی (Core Area, MHU, WTP)
    val areaAnalyticsList: StateFlow<List<AreaAnalytics>> = rawOversightItems.map { items ->
        val activeItems = items.filter { it.active }
        val targetAreas = listOf(
            Triple("Core Area", "ناحیه مرکزی و کوره (Core Area)", 38.0f),
            Triple("MHU", "ناحیه انتقال مواد و بارگیری (MHU)", 32.0f),
            Triple("WTP", "ناحیه تصفیه و خنک‌کاری آب (WTP)", 42.0f)
        )

        targetAreas.map { (key, title, plannedProg) ->
            val areaTasks = activeItems.filter { item ->
                val ga = item.generalArea.trim()
                val loc = item.executionLocation.trim()
                val eq = item.equipmentName.trim()
                val tit = item.title.trim()
                when (key) {
                    "Core Area" -> ga.equals("Core Area", ignoreCase = true) ||
                            ga.contains("Core", ignoreCase = true) ||
                            ga.contains("Blower", ignoreCase = true) ||
                            ga.contains("Reformer", ignoreCase = true) ||
                            loc.contains("Furnace", ignoreCase = true) ||
                            eq.contains("کوره") || tit.contains("کوره") ||
                            (!ga.contains("MHU", ignoreCase = true) && !ga.contains("WTP", ignoreCase = true) && !ga.contains("Water", ignoreCase = true) && !ga.contains("انتقال") && !ga.contains("تصفیه"))
                    "MHU" -> ga.equals("MHU", ignoreCase = true) ||
                            ga.contains("MHU", ignoreCase = true) ||
                            ga.contains("Material", ignoreCase = true) ||
                            ga.contains("انتقال", ignoreCase = true) ||
                            loc.contains("MHU", ignoreCase = true) ||
                            loc.contains("Day Bin", ignoreCase = true) ||
                            eq.contains("نوار") || tit.contains("نوار") ||
                            tit.contains("MHU", ignoreCase = true)
                    "WTP" -> ga.equals("WTP", ignoreCase = true) ||
                            ga.contains("WTP", ignoreCase = true) ||
                            ga.contains("Water", ignoreCase = true) ||
                            ga.contains("تصفیه", ignoreCase = true) ||
                            loc.contains("Clarifier", ignoreCase = true) ||
                            loc.contains("Pump", ignoreCase = true) ||
                            eq.contains("کلاریفایر") || eq.contains("پمپ") ||
                            tit.contains("آب") || tit.contains("WTP", ignoreCase = true)
                    else -> ga.equals(key, ignoreCase = true)
                }
            }

            val total = areaTasks.size
            val completed = areaTasks.count { it.status == "completed" }
            val inProgress = areaTasks.count { it.status == "in_progress" }
            val blocked = areaTasks.count { it.status == "blocked" }
            val pending = areaTasks.count { it.status == "pending" }

            val actualProg = if (total > 0) areaTasks.map { it.progressPercentage.toFloat() }.average().toFloat() else 0f
            val variance = actualProg - plannedProg
            val spi = if (plannedProg > 0f) (actualProg / plannedProg).coerceIn(0f, 3f) else 1.0f

            val totalManpower = areaTasks.sumOf { it.manpowerCount }
            val totalActualHours = areaTasks.sumOf { it.actualHours }

            val unitMap = areaTasks.groupBy { it.executiveUnit.ifBlank { "سایر" } }
                .mapValues { (_, list) ->
                    if (list.isNotEmpty()) (list.map { it.progressPercentage.toFloat() }.average()).toInt() else 0
                }

            val critical = areaTasks.filter { it.status == "blocked" || (it.status == "in_progress" && it.progressPercentage < 50) }

            AreaAnalytics(
                areaKey = key,
                areaTitle = title,
                totalTasks = total,
                completedTasks = completed,
                inProgressTasks = inProgress,
                blockedTasks = blocked,
                pendingTasks = pending,
                actualProgress = actualProg,
                plannedProgress = plannedProg,
                variance = variance,
                spi = spi,
                totalManpower = totalManpower,
                totalActualHours = totalActualHours,
                unitBreakdown = unitMap,
                criticalTasks = critical
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 5.2 مقایسه پیشرفت برنامه‌ای در مقابل واقعی به تفکیک تمام واحدهای صنعتی
    val unitProgressComparisons: StateFlow<List<UnitProgressComparison>> = rawOversightItems.map { items ->
        val activeItems = items.filter { it.active }
        if (activeItems.isEmpty()) return@map emptyList()

        val unitOrder = listOf(
            "مکانیک",
            "برق",
            "ابزاردقیق و اتوماسیون",
            "نسوز",
            "انرژی و سیالات",
            "بازرسی فنی"
        )

        // Baseline planned progress weights
        val unitBaselinePlanned = mapOf(
            "مکانیک" to 42.0f,
            "برق" to 36.0f,
            "ابزاردقیق و اتوماسیون" to 31.0f,
            "نسوز" to 58.0f,
            "انرژی و سیالات" to 44.0f,
            "بازرسی فنی" to 38.0f
        )

        val result = mutableListOf<UnitProgressComparison>()

        for (unitName in unitOrder) {
            val unitTasks = activeItems.filter { item ->
                if (unitName.contains("ابزاردقیق") || unitName.contains("اتوماسیون")) {
                    item.executiveUnit.contains("ابزار") || item.executiveUnit.contains("اتوماسیون")
                } else {
                    item.executiveUnit == unitName
                }
            }
            if (unitTasks.isEmpty()) continue

            val total = unitTasks.size
            val completed = unitTasks.count { it.status == "completed" }
            val inProgress = unitTasks.count { it.status == "in_progress" }
            val blocked = unitTasks.count { it.status == "blocked" }
            val pending = unitTasks.count { it.status == "pending" }

            val actualProg = if (total > 0) unitTasks.map { it.progressPercentage.toFloat() }.average().toFloat() else 0f
            val basePlanned = unitBaselinePlanned[unitName] ?: 35.0f
            val plannedProg = basePlanned.coerceIn(0f, 100f)
            val variance = actualProg - plannedProg
            val spi = if (plannedProg > 0f) (actualProg / plannedProg).coerceIn(0f, 3f) else 1.0f

            val plannedHours = unitTasks.sumOf { if (it.durationHours > 0) it.durationHours else 8.0 }
            val actualHours = unitTasks.sumOf { it.actualHours }

            // Sub-Area comparisons
            val areas = listOf("Core Area", "MHU", "WTP").mapNotNull { areaKey ->
                val areaList = unitTasks.filter {
                    it.generalArea.equals(areaKey, ignoreCase = true) ||
                    (areaKey == "Core Area" && (it.generalArea.contains("Core", ignoreCase = true) || it.generalArea.contains("Blower", ignoreCase = true) || it.generalArea.contains("Reformer", ignoreCase = true))) ||
                    (areaKey == "MHU" && it.generalArea.contains("MHU", ignoreCase = true)) ||
                    (areaKey == "WTP" && (it.generalArea.contains("WTP", ignoreCase = true) || it.generalArea.contains("Water", ignoreCase = true)))
                }
                if (areaList.isNotEmpty()) {
                    val areaActual = areaList.map { it.progressPercentage.toFloat() }.average().toFloat()
                    val areaPlanned = (basePlanned + if (areaKey == "Core Area") 4f else if (areaKey == "WTP") 2f else -2f).coerceIn(0f, 100f)
                    AreaProgressComparison(
                        areaName = areaKey,
                        actualProgress = areaActual,
                        plannedProgress = areaPlanned,
                        totalTasks = areaList.size,
                        completedTasks = areaList.count { it.status == "completed" }
                    )
                } else null
            }

            result.add(
                UnitProgressComparison(
                    unitName = unitName,
                    actualProgress = actualProg,
                    plannedProgress = plannedProg,
                    variance = variance,
                    totalTasks = total,
                    completedTasks = completed,
                    inProgressTasks = inProgress,
                    blockedTasks = blocked,
                    pendingTasks = pending,
                    plannedHours = plannedHours,
                    actualHours = actualHours,
                    spi = spi,
                    areas = areas
                )
            )
        }

        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 5.2 منحنی پیشرفت پروژه (S-Curve Cumulative Project Progress)
    val projectTimelinePoints: StateFlow<List<TimelineProgressPoint>> = rawOversightItems.map { items ->
        val activeItems = items.filter { it.active }
        val overallActual = if (activeItems.isNotEmpty()) activeItems.map { it.progressPercentage.toFloat() }.average().toFloat() else 33.5f

        listOf(
            TimelineProgressPoint(1, "روز ۱", 4.0f, 4.2f),
            TimelineProgressPoint(2, "روز ۲", 9.5f, 9.8f),
            TimelineProgressPoint(3, "روز ۳", 17.0f, 16.2f),
            TimelineProgressPoint(4, "روز ۴", 26.0f, 24.5f),
            TimelineProgressPoint(5, "روز ۵ (امروز)", 36.5f, overallActual),
            TimelineProgressPoint(6, "روز ۶", 47.0f, null),
            TimelineProgressPoint(7, "روز ۷", 58.0f, null, isMilestone = true, milestoneTitle = "پایان تعویض لگ و وال‌اسکیل کوره"),
            TimelineProgressPoint(8, "روز ۸", 68.0f, null),
            TimelineProgressPoint(9, "روز ۹", 77.0f, null),
            TimelineProgressPoint(10, "روز ۱۰", 85.0f, null, isMilestone = true, milestoneTitle = "بستن منهول‌های کوره و استارت پمپ‌ها"),
            TimelineProgressPoint(11, "روز ۱۱", 91.0f, null),
            TimelineProgressPoint(12, "روز ۱۲", 95.0f, null),
            TimelineProgressPoint(13, "روز ۱۳", 98.0f, null, isMilestone = true, milestoneTitle = "هیت‌آپ و تست گرم"),
            TimelineProgressPoint(14, "روز ۱۴", 99.5f, null, isMilestone = true, milestoneTitle = "تزریق گاز طبیعی به ریفرمر"),
            TimelineProgressPoint(15, "روز ۱۵", 100.0f, null, isMilestone = true, milestoneTitle = "تولید آهن اسفنجی")
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overallPlannedProgress: StateFlow<Float> = flowOf(36.5f).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 36.5f)

    // 6. گزارشات روزانه سرپرستان برای جمع‌آوری پایان روز برنامه‌ریز (EOD Review)
    val dailyLogs: StateFlow<List<DailyWorkLogEntity>> = _selectedOversightId
        .flatMapLatest { id ->
            if (id != null) dao.getDailyLogsForOversight(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 7. جلسات، یادداشت‌ها و تصمیمات
    val sessions = _selectedOversightId
        .flatMapLatest { id ->
            if (id != null) dao.getSessionsForOversight(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessionDecisions = dao.getAllSessionDecisions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessionNotes = dao.getAllSessionNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 8. درخواست‌های خرید
    val procurements = _selectedOversightId
        .flatMapLatest { id ->
            if (id != null) dao.getProcurementsForOversight(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 9. سوابق ممیزی (Audit Logs)
    val auditLogs = dao.getRecentAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 10. خروجی و واردات MS Project
    private val _importPreview = MutableStateFlow<ParsedImportPreview?>(null)
    val importPreview: StateFlow<ParsedImportPreview?> = _importPreview.asStateFlow()

    private val _exportedMspCsv = MutableStateFlow<String?>(null)
    val exportedMspCsv: StateFlow<String?> = _exportedMspCsv.asStateFlow()

    // 11. پیام‌ها و خطاهای UI
    private val _uiMessage = MutableStateFlow<UiMessage?>(null)
    val uiMessage: StateFlow<UiMessage?> = _uiMessage.asStateFlow()

    init {
        // بازیابی نشست کاربر لاگین شده از قبل
        viewModelScope.launch {
            val savedUserId = prefs.getLong("saved_user_id", -1L)
            users.collect { list ->
                if (list.isNotEmpty()) {
                    if (savedUserId > 0 && _currentUser.value == null) {
                        val savedUser = list.firstOrNull { it.id == savedUserId }
                        if (savedUser != null) {
                            _currentUser.value = savedUser
                            _isLoggedIn.value = true
                        }
                    }
                    if (_currentUser.value == null && !_isLoggedIn.value) {
                        // در انتظار ورود با یوزرنیم و پسورد در صفحه لاگین
                    }
                }
            }
        }

        viewModelScope.launch {
            oversights.collect { list ->
                if (list.isNotEmpty() && _selectedOversightId.value == null) {
                    _selectedOversightId.value = list.first().id
                }
            }
        }
    }

    // ==========================================
    // احراز هویت و مدیریت نشست کاربر (LOGIN / LOGOUT)
    // ==========================================

    fun login(username: String, pass: String) {
        viewModelScope.launch {
            if (username.isBlank() || pass.isBlank()) {
                _uiMessage.value = UiMessage("لطفاً نام کاربری و کلمه عبور را وارد نمایید.", isError = true)
                return@launch
            }
            val result = service.authenticate(username, pass)
            when (result) {
                is ServiceResult.Success -> {
                    val user = result.data
                    _currentUser.value = user
                    _isLoggedIn.value = true
                    prefs.edit().putLong("saved_user_id", user.id).apply()
                    _uiMessage.value = UiMessage("خوش آمدید، ${user.name} (${getPersianRole(user.role)})")
                }
                is ServiceResult.Error -> {
                    _uiMessage.value = UiMessage(result.message, isError = true)
                }
            }
        }
    }

    fun logout() {
        prefs.edit().remove("saved_user_id").apply()
        _currentUser.value = null
        _isLoggedIn.value = false
        _uiMessage.value = UiMessage("با موفقیت از حساب کاربری خارج شدید.")
    }

    fun switchUserDirectly(user: UserEntity) {
        _currentUser.value = user
        _isLoggedIn.value = true
        prefs.edit().putLong("saved_user_id", user.id).apply()
        _uiMessage.value = UiMessage("نقش جاری تغییر کرد به: ${user.name} (${getPersianRole(user.role)})")
    }

    fun toggleUnitExpansion(unitName: String) {
        val current = expandedUnits.value.toMutableSet()
        if (current.contains(unitName)) current.remove(unitName) else current.add(unitName)
        expandedUnits.value = current
    }

    fun toggleAreaExpansion(areaName: String) {
        val current = expandedAreas.value.toMutableSet()
        if (current.contains(areaName)) current.remove(areaName) else current.add(areaName)
        expandedAreas.value = current
    }

    fun selectOversight(id: Long) {
        _selectedOversightId.value = id
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun clearExportedMspCsv() {
        _exportedMspCsv.value = null
    }

    // ==========================================
    // ثبت پیشرفت و کارکرد روزانه توسط سرپرست / ناظر
    // ==========================================

    fun recordDailySupervisorWork(
        itemId: Long,
        newProgress: Int,
        manpowerCount: Int,
        hoursSpent: Double,
        status: String,
        remarks: String = "",
        issues: String = ""
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val result = service.recordSupervisorDailyProgress(
                user = user,
                itemId = itemId,
                newProgress = newProgress,
                manpowerCount = manpowerCount,
                hoursSpent = hoursSpent,
                status = status,
                remarks = remarks,
                issues = issues
            )
            handleServiceResult(result)
        }
    }

    // ==========================================
    // برنامه‌ریزی و جمع‌آوری پایان روز و همگام‌سازی با MSP
    // ==========================================

    fun generateEodMspExport() {
        val user = _currentUser.value ?: return
        val oversightId = _selectedOversightId.value ?: return
        val items = rawOversightItems.value

        viewModelScope.launch {
            val result = service.exportDailyProgressForMsp(user, oversightId, items)
            when (result) {
                is ServiceResult.Success -> {
                    _exportedMspCsv.value = result.data
                    _uiMessage.value = UiMessage(result.message)
                }
                is ServiceResult.Error -> {
                    _uiMessage.value = UiMessage(result.message, isError = true)
                }
            }
        }
    }

    fun importUpdatedScheduleFromMsp(csvContent: String) {
        val user = _currentUser.value ?: return
        val oversightId = _selectedOversightId.value ?: return

        try {
            val preview = importer.parseMsProjectCsv(oversightId, csvContent, ignoreInactive = false)
            viewModelScope.launch {
                val result = service.importUpdatedScheduleFromMsp(user, oversightId, preview.parsedItems)
                handleServiceResult(result)
            }
        } catch (e: Exception) {
            _uiMessage.value = UiMessage("خطا در پردازش فایل به‌روزشده MSP: ${e.localizedMessage}", isError = true)
        }
    }

    fun setWbsViewMode(mode: WbsViewMode) {
        wbsViewMode.value = mode
    }

    fun toggleTreeNodeExpanded(nodeId: Long) {
        val current = expandedTreeNodes.value.toMutableSet()
        if (current.contains(nodeId)) current.remove(nodeId) else current.add(nodeId)
        expandedTreeNodes.value = current
    }

    fun expandAllTreeNodes() {
        val allIds = rawOversightItems.value.map { it.id }.toSet()
        expandedTreeNodes.value = allIds
    }

    fun collapseAllTreeNodes() {
        expandedTreeNodes.value = emptySet()
    }

    fun addSubtask(
        parentItem: OversightItemEntity,
        title: String,
        durationHours: Double,
        manpowerCount: Int,
        equipmentName: String = "",
        executionLocation: String = "",
        plannedStart: String = "",
        plannedEnd: String = ""
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val result = service.addSubtask(
                user = user,
                parentItem = parentItem,
                title = title,
                durationHours = durationHours,
                manpowerCount = manpowerCount,
                equipmentName = equipmentName,
                executionLocation = executionLocation,
                plannedStartDate = plannedStart,
                plannedEndDate = plannedEnd
            )
            handleServiceResult(result)
        }
    }

    fun deleteWbsHierarchy(itemId: Long) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val result = service.deleteItemHierarchy(user, itemId)
            handleServiceResult(result)
        }
    }

    fun updateWbsItem(
        item: OversightItemEntity,
        prerequisiteIds: List<Long>,
        supervisorIds: List<Long>
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val result = service.updateItemDetails(user, item, prerequisiteIds, supervisorIds)
            handleServiceResult(result)
        }
    }

    fun createWbsItem(
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
    ) {
        val user = _currentUser.value ?: return
        val oversightId = _selectedOversightId.value ?: return

        val newItem = OversightItemEntity(
            oversightId = oversightId,
            wbsCode = wbsCode,
            title = title,
            parentItemId = parentItemId,
            outlineLevel = outlineLevel,
            executiveUnit = executiveUnit,
            generalArea = generalArea,
            executionLocation = executionLocation,
            equipmentName = equipmentName,
            durationHours = durationHours,
            status = "pending",
            progressPercentage = 0,
            plannedStartDate = plannedStart,
            plannedEndDate = plannedEnd
        )

        viewModelScope.launch {
            val result = service.createItem(user, newItem, prerequisiteIds, supervisorIds)
            handleServiceResult(result)
        }
    }

    fun createPlanningSession(
        title: String,
        date: String,
        location: String,
        minutes: String
    ) {
        val user = _currentUser.value ?: return
        val oversightId = _selectedOversightId.value ?: return

        val session = PlanningSessionEntity(
            oversightId = oversightId,
            title = title,
            sessionDate = date,
            location = location,
            minutesSummary = minutes
        )

        viewModelScope.launch {
            val result = service.createSession(user, session)
            handleServiceResult(result)
        }
    }

    fun addSessionDecision(
        sessionId: Long,
        itemId: Long?,
        decisionText: String,
        assignedUnit: String
    ) {
        val user = _currentUser.value ?: return
        val decision = SessionDecisionEntity(
            sessionId = sessionId,
            itemId = itemId,
            decisionText = decisionText,
            status = "pending",
            assignedUnit = assignedUnit,
            createdAt = ""
        )
        viewModelScope.launch {
            val result = service.addSessionDecision(user, decision)
            handleServiceResult(result)
        }
    }

    fun addSessionNote(
        sessionId: Long,
        itemId: Long?,
        noteText: String
    ) {
        val user = _currentUser.value ?: return
        val note = SessionNoteEntity(
            sessionId = sessionId,
            itemId = itemId,
            noteText = noteText,
            authorName = user.name,
            createdAt = ""
        )
        viewModelScope.launch {
            val result = service.addSessionNote(user, note)
            handleServiceResult(result)
        }
    }

    fun createProcurementRequest(
        itemId: Long,
        sessionId: Long?,
        title: String,
        itemType: String,
        quantity: String,
        estimatedCost: String
    ) {
        val user = _currentUser.value ?: return
        val req = ProcurementRequestEntity(
            itemId = itemId,
            sessionId = sessionId,
            title = title,
            itemType = itemType,
            quantity = quantity,
            estimatedCost = estimatedCost,
            requestedByUserId = user.id,
            requestedByUserName = user.name,
            createdAt = ""
        )
        viewModelScope.launch {
            val result = service.createProcurementRequest(user, req)
            handleServiceResult(result)
        }
    }

    fun updateProcurementStatus(
        requestId: Long,
        newStatus: String,
        rejectionReason: String? = null
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val result = service.updateProcurementStatus(user, requestId, newStatus, rejectionReason)
            handleServiceResult(result)
        }
    }

    // واردات هوشمند فایل نمونه MS Project از دیتاست واقعی فولاد غدیر نی‌ریز
    fun loadGhadirNeyrizSampleCsvForPreview() {
        val oversightId = _selectedOversightId.value ?: return
        val sampleCsv = getGhadirNeyrizCsvSample()
        val preview = importer.parseMsProjectCsv(oversightId, sampleCsv, ignoreInactive = false)
        _importPreview.value = preview
        _uiMessage.value = UiMessage("پیش‌نمایش داده‌های کارخانه احیای مستقیم فولاد غدیر نی‌ریز با ${preview.activeTasksCount} فعالیت آماده شد.")
    }

    fun parseCustomCsvText(csvText: String) {
        val oversightId = _selectedOversightId.value ?: return
        try {
            val preview = importer.parseMsProjectCsv(oversightId, csvText, ignoreInactive = false)
            _importPreview.value = preview
            _uiMessage.value = UiMessage("فایل MS Project پردازش شد: ${preview.activeTasksCount} فعالیت شناسایی گردید.")
        } catch (e: Exception) {
            _uiMessage.value = UiMessage("خطا در خواندن فایل CSV: ${e.localizedMessage}", isError = true)
        }
    }

    fun commitMsProjectImport() {
        val preview = _importPreview.value ?: return
        val oversightId = _selectedOversightId.value ?: return
        val user = _currentUser.value ?: return

        if (user.role != "admin" && user.role != "planner") {
            _uiMessage.value = UiMessage("خطای ۴۰۳: فقط Admin و Planner مجاز به واردات اطلاعات MS Project هستند.", isError = true)
            return
        }

        viewModelScope.launch {
            try {
                val tempToRealIdMap = mutableMapOf<Long, Long>()

                val sortedItems = preview.parsedItems.sortedBy { it.outlineLevel }
                for (item in sortedItems) {
                    val realParentId = item.parentItemId?.let { tempToRealIdMap[it] }
                    val realItem = item.copy(
                        id = 0,
                        oversightId = oversightId,
                        parentItemId = realParentId
                    )
                    val realId = dao.insertItem(realItem)
                    tempToRealIdMap[item.id] = realId
                }

                val realPrerequisites = preview.parsedPrerequisites.mapNotNull { prereq ->
                    val realItemId = tempToRealIdMap[prereq.itemId]
                    val realPredId = tempToRealIdMap[prereq.prerequisiteItemId]
                    if (realItemId != null && realPredId != null) {
                        ItemPrerequisiteEntity(itemId = realItemId, prerequisiteItemId = realPredId)
                    } else null
                }
                dao.insertPrerequisites(realPrerequisites)

                // تخصیص خودکار به سرپرستان بر اساس واحد
                val allUsers = users.value
                val mechSupervisor = allUsers.firstOrNull { it.role == "supervisor" && it.unit == "مکانیک" }
                val elecSupervisor = allUsers.firstOrNull { it.role == "supervisor" && it.unit == "برق" }
                val instSupervisor = allUsers.firstOrNull { it.role == "supervisor" && it.unit == "ابزار دقیق" }
                val refSupervisor = allUsers.firstOrNull { it.role == "supervisor" && it.unit == "نسوز" }

                val assignments = mutableListOf<ItemAssignmentEntity>()
                for ((tempId, realId) in tempToRealIdMap) {
                    val original = preview.parsedItems.firstOrNull { it.id == tempId } ?: continue
                    when (original.executiveUnit) {
                        "مکانیک" -> mechSupervisor?.let { assignments.add(ItemAssignmentEntity(realId, it.id)) }
                        "برق" -> elecSupervisor?.let { assignments.add(ItemAssignmentEntity(realId, it.id)) }
                        "ابزار دقیق" -> instSupervisor?.let { assignments.add(ItemAssignmentEntity(realId, it.id)) }
                        "نسوز" -> refSupervisor?.let { assignments.add(ItemAssignmentEntity(realId, it.id)) }
                        else -> mechSupervisor?.let { assignments.add(ItemAssignmentEntity(realId, it.id)) }
                    }
                }
                dao.insertAssignments(assignments)

                dao.insertAuditLog(
                    AuditLogEntity(
                        entityType = "oversight",
                        entityId = oversightId,
                        action = "MSP_SYNC",
                        performedByUserId = user.id,
                        performedByUserName = user.name,
                        performedByUserRole = user.role,
                        beforeStateJson = "{}",
                        afterStateJson = "{\"importedTasks\": ${preview.activeTasksCount}, \"prerequisites\": ${realPrerequisites.size}}",
                        remarks = "واردات ساختار کامل WBS کارخانه احیای مستقیم فولاد غدیر نی‌ریز از MS Project",
                        timestamp = ""
                    )
                )

                _importPreview.value = null
                _uiMessage.value = UiMessage("ساختار شکست کار (${preview.activeTasksCount} تسک) با موفقیت در دیتابیس ثبت و فعال شد.")
            } catch (e: Exception) {
                _uiMessage.value = UiMessage("خطا در ثبت اطلاعات در دیتابیس: ${e.localizedMessage}", isError = true)
            }
        }
    }

    fun cancelImportPreview() {
        _importPreview.value = null
    }

    private fun <T> handleServiceResult(result: ServiceResult<T>) {
        when (result) {
            is ServiceResult.Success -> {
                _uiMessage.value = UiMessage(result.message)
            }
            is ServiceResult.Error -> {
                _uiMessage.value = UiMessage(
                    text = result.message,
                    isError = true,
                    details = result.details
                )
            }
        }
    }

    private fun getPersianRole(role: String): String = when (role) {
        "admin" -> "مدیر ارشد اورهال"
        "planner" -> "برنامه‌ریز اجرایی"
        "supervisor" -> "ناظر و سرپرست اجرایی"
        "unit_head" -> "مدیر / رئیس واحد اجرایی"
        else -> role
    }

    private fun getGhadirNeyrizCsvSample(): String = """
ID,Active,Task Mode,Name,Duration,Start,Finish,Predecessors,Outline Level,Notes
1,Yes,Auto Scheduled,Overhaul,180 hrs,2026 January 02 12:00 AM,2026 January 19 7:00 PM,,1,
2,Yes,Auto Scheduled,شروع,0 hrs,2026 January 02 12:00 AM,2026 January 02 12:00 AM,,2,
3,Yes,Auto Scheduled,Mechanical,180 hrs,2026 January 02 12:00 AM,2026 January 19 7:00 PM,,2,
4,Yes,Auto Scheduled,core area,180 hrs,2026 January 02 12:00 AM,2026 January 19 7:00 PM,,3,
5,Yes,Auto Scheduled,Furnace Area,180 hrs,2026 January 02 12:00 AM,2026 January 19 7:00 PM,,4,
6,Yes,Auto Scheduled,Furnace charge hopper,158 hrs,2026 January 02 8:00 AM,2026 January 17 4:00 PM,,5,
7,Yes,Auto Scheduled,آماده سازی وتست وینج,2 hrs,2026 January 02 8:00 AM,2026 January 02 10:00 AM,2,6,
14,Yes,Auto Scheduled,دمونتاژ قسمت میانی شارژ هاپر، اسلایدگیت بالا، لگ دو تکه و سیل گس کن بالای کوره,25 hrs,2026 January 05 8:00 AM,2026 January 07 1:00 PM,27,6,
21,Yes,Auto Scheduled,Reduction furnace- Inside,180 hrs,2026 January 02 12:00 AM,2026 January 19 7:00 PM,,5,
22,Yes,Auto Scheduled,تخلیه کوره,12 hrs,2026 January 02 12:00 AM,2026 January 02 12:00 PM,2,6,
23,Yes,Auto Scheduled,باز کردن منهول های کوره (منهول بیضی، چایناهت و ال.بی.اف) پس از هماهنگی با واحد تولید,5 hrs,2026 January 02 2:00 PM,2026 January 02 7:00 PM,22FS+2h,6,
257,Yes,Auto Scheduled,Process Gas Compressor,138 hrs,2026 January 02 8:00 AM,2026 January 15 4:00 PM,,5,
258,Yes,Auto Scheduled,باز کردن سقف کمپرسور پروسس,6 hrs,2026 January 02 8:00 AM,2026 January 02 2:00 PM,2,6,
259,Yes,Auto Scheduled,اورهال کمپرسور پروسس مطابق با شرح خدمات مربوطه و نصب روتور، بولگیر، پینیون شفت، ایمپلر، بیرینگ ها و سیلینگ های جدید,50 hrs,2026 January 02 2:00 PM,2026 January 07 2:00 PM,22,6,
1018,Yes,Auto Scheduled,Clarifier,128 hrs,2026 January 03 1:00 PM,2026 January 16 11:00 AM,,4,
1034,Yes,Auto Scheduled,تخلیه آب کلاریفایر با هماهنگی واحد تولید,36 hrs,2026 January 03 1:00 PM,2026 January 05 1:00 AM,96,6,
1035,Yes,Auto Scheduled,تخلیه لجن و تمیز کاری کف کلاریفایر پس از تخلیه آب کلاریفایر,40 hrs,2026 January 05 8:00 AM,2026 January 08 6:00 PM,1034,6,
1092,Yes,Auto Scheduled,Refractory,140 hrs,2026 January 02 2:00 PM,2026 January 16 2:00 PM,,2,
1104,Yes,Auto Scheduled,Bustle Gas Ducts,140 hrs,2026 January 02 2:00 PM,2026 January 16 2:00 PM,,5,
1105,Yes,Auto Scheduled,بازکردن منهول های داکت های باستل پس از هماهنگی با واحد تولید,4 hrs,2026 January 02 2:00 PM,2026 January 02 6:00 PM,23SS,6,
1137,Yes,Auto Scheduled,Instrument,145 hrs,2026 January 02 8:00 AM,2026 January 16 1:00 PM,,2,
1142,Yes,Auto Scheduled,جابجایی سورس رادیواکتیو جهت تعویض 1/3 میانی بدنه چارج هاپر محل اتصال شارژ هاپر با لگ,3 hrs,2026 January 07 1:00 PM,2026 January 07 4:00 PM,14SS,6,
1564,Yes,Auto Scheduled,Automation,104 hrs,2026 January 04 8:00 AM,2026 January 14 12:00 PM,,3,
1571,Yes,Auto Scheduled,D.C.S گیری از سیستم کنترل Back up,1 hr,2026 January 11 8:00 AM,2026 January 11 9:00 AM,2FS+90h,5,
1602,Yes,Auto Scheduled,Electrical,150 hrs,2026 January 02 8:00 AM,2026 January 16 6:00 PM,,2,
1605,Yes,Auto Scheduled,تامین روشنایی درون کوره و برچیدن آن,5 hrs,2026 January 02 12:00 PM,2026 January 02 5:00 PM,22,5,
1683,Yes,Auto Scheduled,Core ناحیه SWG 33 KV انجام تستها و سرویسهای سوئیچ گیر,8 hrs,2026 January 08 8:00 AM,2026 January 08 4:00 PM,317SS,5,
1694,Yes,Auto Scheduled,Core ناحیه H10 - 33/6.6KV انجام تستها و سرویسهای ترانس,6 hrs,2026 January 03 8:00 AM,2026 January 03 2:00 PM,2FS+10h,5
    """.trimIndent()
}
