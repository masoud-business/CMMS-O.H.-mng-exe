package com.example.data.importer

import com.example.data.entity.ItemPrerequisiteEntity
import com.example.data.entity.OversightItemEntity

data class RawMsProjectTask(
    val msId: Int, // شناسه در MS Project
    val outlineLevel: Int, // 1, 2, 3, 4, 5, 6
    val wbs: String = "",
    val taskName: String,
    val durationText: String = "1 hr",
    val durationHours: Double = 1.0,
    val plannedStart: String = "1404/10/12",
    val plannedFinish: String = "1404/10/14",
    val predecessorsRaw: String = "",
    val active: Boolean = true,
    val resourceNames: String = "",
    val notes: String = ""
)

data class ParsedImportPreview(
    val totalTasksFound: Int,
    val activeTasksCount: Int,
    val rootTasksCount: Int,
    val maxOutlineDepth: Int,
    val executiveUnitsFound: List<String>,
    val parsedItems: List<OversightItemEntity>,
    val parsedPrerequisites: List<ItemPrerequisiteEntity>,
    val msIdToTempIdMap: Map<Int, Long>,
    val parsingNotes: List<String>
)

class MsProjectImporter {

    /**
     * الگوریتم پشته‌ای پیشرفته برای بازسازی ساختار شکست کار (Stack-Based WBS Hierarchy Reconstruction)
     * استخراج خودکار:
     * - واحد اجرایی (مکانیک، نسوز، ابزار دقیق، اتوماسیون، برق)
     * - محل کلی (Core Area, Blower Area, Reformer, Water System, MHU, Substation, ...)
     * - محل و زون اجرا (Furnace Area, Scrubber, Pump House, Clarifier, Day Bin, ...)
     * - نام تجهیز (Charge Hopper, Compressors, Recuperator, Screen, ...)
     */
    fun parseMsProjectCsv(
        oversightId: Long,
        csvContent: String,
        ignoreInactive: Boolean = false
    ): ParsedImportPreview {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        val rawTasks = mutableListOf<RawMsProjectTask>()

        // تشخیص هدر یا خط اول
        val startIndex = if (lines.firstOrNull()?.contains("Name", ignoreCase = true) == true ||
            lines.firstOrNull()?.contains("ID", ignoreCase = true) == true
        ) 1 else 0

        for (i in startIndex until lines.size) {
            val line = lines[i].trim()
            if (line.isBlank()) continue

            val tokens = parseCsvLine(line)
            if (tokens.size >= 4) {
                val msId = tokens.getOrNull(0)?.toIntOrNull() ?: (i + 1)
                val active = tokens.getOrNull(1)?.equals("No", ignoreCase = true)?.not() ?: true
                val name = tokens.getOrNull(3)?.trim() ?: tokens.getOrNull(2)?.trim() ?: "فعالیت $msId"
                val duration = tokens.getOrNull(4)?.trim() ?: "1 hr"
                val start = tokens.getOrNull(5)?.trim() ?: "1404/10/12"
                val finish = tokens.getOrNull(6)?.trim() ?: "1404/10/15"
                val predecessors = tokens.getOrNull(7)?.trim() ?: ""
                val outlineLevel = tokens.getOrNull(8)?.toIntOrNull() ?: 2
                val notes = tokens.getOrNull(9)?.trim() ?: ""

                val durationHours = parseDurationToHours(duration)

                rawTasks.add(
                    RawMsProjectTask(
                        msId = msId,
                        outlineLevel = outlineLevel,
                        wbs = msId.toString(),
                        taskName = name,
                        durationText = duration,
                        durationHours = durationHours,
                        plannedStart = start,
                        plannedFinish = finish,
                        predecessorsRaw = predecessors,
                        active = active,
                        notes = notes
                    )
                )
            }
        }

        return buildHierarchyFromRawTasks(oversightId, rawTasks, ignoreInactive)
    }

    fun buildHierarchyFromRawTasks(
        oversightId: Long,
        tasks: List<RawMsProjectTask>,
        ignoreInactive: Boolean = false
    ): ParsedImportPreview {
        val notes = mutableListOf<String>()
        val filteredTasks = if (ignoreInactive) {
            val count = tasks.count { !it.active }
            if (count > 0) notes.add("$count فعالیت غیرفعال نادیده گرفته شدند.")
            tasks.filter { it.active }
        } else tasks

        val parsedItems = mutableListOf<OversightItemEntity>()
        val msIdToTempIdMap = mutableMapOf<Int, Long>()
        var tempIdCounter = 1000L

        // پشته برای سلسله‌مراتب: Pair(outlineLevel, Pair(tempItemId, taskName))
        val stack = ArrayDeque<Pair<Int, Pair<Long, String>>>()
        var maxDepth = 1
        var rootCount = 0

        var currentExecutiveUnit = "مکانیک"
        var currentGeneralArea = "سایت احیا"
        var currentExecutionLocation = "محوطه عمومی"
        var currentEquipment = "تجهیزات فرآیندی"

        for (raw in filteredTasks) {
            val level = raw.outlineLevel
            if (level > maxDepth) maxDepth = level

            while (stack.isNotEmpty() && stack.last().first >= level) {
                stack.removeLast()
            }

            val parentItemId = if (stack.isNotEmpty()) {
                stack.last().second.first
            } else {
                rootCount++
                null
            }

            // استخراج هوشمند سطح و دسته‌بندی
            val taskName = raw.taskName.trim().removePrefix("\u202B").trim()
            if (level == 2) {
                when {
                    taskName.contains("Mechanical", ignoreCase = true) || taskName.contains("مکانیک") -> currentExecutiveUnit = "مکانیک"
                    taskName.contains("Refractory", ignoreCase = true) || taskName.contains("نسوز") -> currentExecutiveUnit = "نسوز"
                    taskName.contains("Instrument", ignoreCase = true) || taskName.contains("ابزار") -> currentExecutiveUnit = "ابزار دقیق"
                    taskName.contains("Automation", ignoreCase = true) || taskName.contains("اتوماسیون") -> currentExecutiveUnit = "اتوماسیون"
                    taskName.contains("Electrical", ignoreCase = true) || taskName.contains("برق") -> currentExecutiveUnit = "برق"
                }
            } else if (level == 3) {
                currentGeneralArea = taskName
            } else if (level == 4) {
                currentExecutionLocation = taskName
            } else if (level == 5) {
                currentEquipment = taskName
            }

            val currentTempId = tempIdCounter++
            msIdToTempIdMap[raw.msId] = currentTempId

            val item = OversightItemEntity(
                id = currentTempId,
                oversightId = oversightId,
                wbsCode = raw.msId.toString(),
                title = taskName,
                parentItemId = parentItemId,
                outlineLevel = level,
                executiveUnit = currentExecutiveUnit,
                generalArea = currentGeneralArea,
                executionLocation = currentExecutionLocation,
                equipmentName = currentEquipment,
                durationHours = raw.durationHours,
                actualHours = 0.0,
                manpowerCount = 0,
                status = "pending",
                progressPercentage = 0,
                plannedStartDate = raw.plannedStart,
                plannedEndDate = raw.plannedFinish,
                notes = raw.notes,
                active = raw.active
            )

            parsedItems.add(item)
            stack.addLast(Pair(level, Pair(currentTempId, taskName)))
        }

        // پردازش پیش‌نیازها
        val prerequisites = mutableListOf<ItemPrerequisiteEntity>()
        for (raw in filteredTasks) {
            val currentTempId = msIdToTempIdMap[raw.msId] ?: continue
            if (raw.predecessorsRaw.isBlank()) continue

            val tokens = raw.predecessorsRaw.split(",", ";").map { it.trim() }.filter { it.isNotEmpty() }
            for (token in tokens) {
                val match = Regex("""^(\d+)""").find(token)
                if (match != null) {
                    val predMsId = match.groupValues[1].toIntOrNull()
                    if (predMsId != null && msIdToTempIdMap.containsKey(predMsId)) {
                        val predTempId = msIdToTempIdMap[predMsId]!!
                        prerequisites.add(
                            ItemPrerequisiteEntity(
                                itemId = currentTempId,
                                prerequisiteItemId = predTempId
                            )
                        )
                    }
                }
            }
        }

        val distinctUnits = parsedItems.map { it.executiveUnit }.distinct()

        return ParsedImportPreview(
            totalTasksFound = tasks.size,
            activeTasksCount = parsedItems.size,
            rootTasksCount = rootCount,
            maxOutlineDepth = maxDepth,
            executiveUnitsFound = distinctUnits,
            parsedItems = parsedItems,
            parsedPrerequisites = prerequisites,
            msIdToTempIdMap = msIdToTempIdMap,
            parsingNotes = notes
        )
    }

    private fun parseDurationToHours(raw: String): Double {
        val clean = raw.lowercase().trim()
        val num = Regex("""([0-9.]+)""").find(clean)?.groupValues?.get(1)?.toDoubleOrNull() ?: 1.0
        return when {
            clean.contains("hr") || clean.contains("ساعت") -> num
            clean.contains("d") || clean.contains("روز") -> num * 8.0
            clean.contains("w") || clean.contains("هفته") -> num * 40.0
            else -> num
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var cur = StringBuilder()
        var insideQuotes = false

        for (ch in line) {
            when (ch) {
                '"' -> insideQuotes = !insideQuotes
                ',' -> {
                    if (insideQuotes) {
                        cur.append(ch)
                    } else {
                        result.add(cur.toString().trim())
                        cur = StringBuilder()
                    }
                }
                else -> cur.append(ch)
            }
        }
        result.add(cur.toString().trim())
        return result
    }
}
