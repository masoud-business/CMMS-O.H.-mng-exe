package com.example.util

import com.example.data.entity.AuditLogEntity
import com.example.data.entity.DailyWorkLogEntity
import com.example.data.entity.OversightItemEntity
import com.example.data.entity.SafetyPermitEntity

/**
 * مدیریت اکسپورت و ایمپورت استاندارد CSV سازگار با Microsoft Project، Power BI و Excel
 * با پشتیبانی کامل از UTF-8 BOM جهت نمایش صحیح متون فارسی
 */
object CsvExportImportHelper {

    // افزودن UTF-8 Byte Order Mark برای باز شدن مستقیم در اکسل فارسی بدون بهم ریختگی
    private const val UTF8_BOM = "\uFEFF"

    /**
     * تولید خروجی کامل ساختار شکست کار (WBS) به صورت CSV
     */
    fun exportWbsToCsv(items: List<OversightItemEntity>): String {
        val sb = StringBuilder()
        sb.append(UTF8_BOM)
        sb.append("ID,WBS_Code,Outline_Level,Title,Executive_Unit,General_Area,Location,Equipment,Planned_Duration_Hours,Actual_Hours,Manpower,%_Progress,Status,Planned_Start,Planned_End,Actual_Start,Actual_End,Notes,Issues\n")

        for (item in items) {
            val title = escapeCsv(item.title)
            val unit = escapeCsv(item.executiveUnit)
            val area = escapeCsv(item.generalArea)
            val loc = escapeCsv(item.executionLocation)
            val equip = escapeCsv(item.equipmentName)
            val notes = escapeCsv(item.notes)
            val issues = escapeCsv(item.issues)

            sb.append("${item.id},")
            sb.append("${item.wbsCode},")
            sb.append("${item.outlineLevel},")
            sb.append("\"$title\",")
            sb.append("\"$unit\",")
            sb.append("\"$area\",")
            sb.append("\"$loc\",")
            sb.append("\"$equip\",")
            sb.append("${item.durationHours},")
            sb.append("${item.actualHours},")
            sb.append("${item.manpowerCount},")
            sb.append("${item.progressPercentage}%,")
            sb.append("${item.status},")
            sb.append("${item.plannedStartDate},")
            sb.append("${item.plannedEndDate},")
            sb.append("${item.actualStartDate ?: ""},")
            sb.append("${item.actualEndDate ?: ""},")
            sb.append("\"$notes\",")
            sb.append("\"$issues\"\n")
        }
        return sb.toString()
    }

    /**
     * تولید خروجی ممیزی و تاریخچه تغییرات دقیق (Audit Logs) به صورت CSV
     */
    fun exportAuditLogsToCsv(logs: List<AuditLogEntity>): String {
        val sb = StringBuilder()
        sb.append(UTF8_BOM)
        sb.append("Log_ID,Timestamp,Action,Entity_Type,Entity_ID,User_Name,User_Role,Remarks,Before_State,After_State\n")

        for (log in logs) {
            val user = escapeCsv(log.performedByUserName)
            val role = escapeCsv(log.performedByUserRole)
            val remarks = escapeCsv(log.remarks)
            val before = escapeCsv(log.beforeStateJson)
            val after = escapeCsv(log.afterStateJson)

            sb.append("${log.id},")
            sb.append("\"${log.timestamp}\",")
            sb.append("${log.action},")
            sb.append("${log.entityType},")
            sb.append("${log.entityId},")
            sb.append("\"$user\",")
            sb.append("\"$role\",")
            sb.append("\"$remarks\",")
            sb.append("\"$before\",")
            sb.append("\"$after\"\n")
        }
        return sb.toString()
    }

    /**
     * تولید خروجی لاگ‌های روزانه سرپرستان کارگاه به صورت CSV
     */
    fun exportDailyWorkLogsToCsv(logs: List<DailyWorkLogEntity>): String {
        val sb = StringBuilder()
        sb.append(UTF8_BOM)
        sb.append("Log_ID,Item_ID,Date,Unit,Recorded_By,Progress_%,Manpower,Hours_Spent,Remarks,Issues,Synced_MSP\n")

        for (log in logs) {
            val unit = escapeCsv(log.unitName)
            val user = escapeCsv(log.recordedByUserName)
            val remarks = escapeCsv(log.remarks)
            val issues = escapeCsv(log.issues)

            sb.append("${log.id},")
            sb.append("${log.itemId},")
            sb.append("${log.date},")
            sb.append("\"$unit\",")
            sb.append("\"$user\",")
            sb.append("${log.progressPercentage}%,")
            sb.append("${log.manpowerCount},")
            sb.append("${log.hoursSpent},")
            sb.append("\"$remarks\",")
            sb.append("\"$issues\",")
            sb.append("${log.syncedToMsp}\n")
        }
        return sb.toString()
    }

    /**
     * تولید خروجی پرمیت‌های ایمنی HSE به صورت CSV
     */
    fun exportSafetyPermitsToCsv(permits: List<SafetyPermitEntity>): String {
        val sb = StringBuilder()
        sb.append(UTF8_BOM)
        sb.append("Permit_Number,Permit_Type,Status,Executive_Unit,Location,Equipment,Issue_Date,Valid_Hours,Issued_By,LOTO_Required,LOTO_Status,Gas_Test,Precautions\n")

        for (p in permits) {
            val num = escapeCsv(p.permitNumber)
            val type = escapeCsv(p.permitType)
            val unit = escapeCsv(p.executiveUnit)
            val loc = escapeCsv(p.location)
            val equip = escapeCsv(p.equipmentName)
            val issuer = escapeCsv(p.issuedByUserName)
            val lotoStatus = escapeCsv(p.electricalLotoStatus)
            val gas = escapeCsv(p.gasTestResult)
            val precautions = escapeCsv(p.safetyPrecautions)

            sb.append("\"$num\",")
            sb.append("\"$type\",")
            sb.append("${p.status},")
            sb.append("\"$unit\",")
            sb.append("\"$loc\",")
            sb.append("\"$equip\",")
            sb.append("${p.issueDate},")
            sb.append("${p.validHours},")
            sb.append("\"$issuer\",")
            sb.append("${p.requiresElectricalLoto},")
            sb.append("\"$lotoStatus\",")
            sb.append("\"$gas\",")
            sb.append("\"$precautions\"\n")
        }
        return sb.toString()
    }

    /**
     * پارس کردن خطوط CSV ورودی از MSP یا اکسل جهت به‌روزرسانی پیشرفت آیتم‌ها
     */
    data class ParsedProgressRow(
        val wbsCode: String,
        val progressPercentage: Int,
        val actualHours: Double? = null,
        val status: String? = null,
        val notes: String? = null
    )

    fun parseProgressCsv(csvText: String): List<ParsedProgressRow> {
        val rows = mutableListOf<ParsedProgressRow>()
        val lines = csvText.lines()
        if (lines.size <= 1) return rows

        val header = lines.first().lowercase()
        val hasWbsIndex = 1 // default column 1 or by name
        val hasProgressIndex = 11 // default

        for (i in 1 until lines.size) {
            val line = lines[i].trim()
            if (line.isBlank()) continue

            try {
                // تجزیه ساده با در نظر گرفتن کاما
                val parts = parseCsvLine(line)
                if (parts.size >= 3) {
                    // یافتن ستون کد WBS (معمولاً ۱ یا ستون با فرمت 1.x)
                    val wbsCode = parts.find { it.matches(Regex("""^\d+(\.\d+)*$""")) } ?: parts.getOrNull(1) ?: ""
                    // یافتن ستون درصد پیشرفت
                    val progressStr = parts.find { it.endsWith("%") }?.removeSuffix("%") 
                        ?: parts.getOrNull(11)?.removeSuffix("%") 
                        ?: parts.getOrNull(2)?.removeSuffix("%") ?: "0"
                    val progress = progressStr.trim().toIntOrNull() ?: 0

                    if (wbsCode.isNotBlank()) {
                        rows.add(ParsedProgressRow(wbsCode = wbsCode, progressPercentage = progress.coerceIn(0, 100)))
                    }
                }
            } catch (_: Exception) {}
        }
        return rows
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var curVal = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            if (ch == '\"') {
                inQuotes = !inQuotes
            } else if (ch == ',' && !inQuotes) {
                result.add(curVal.toString().trim().removeSurrounding("\""))
                curVal = StringBuilder()
            } else {
                curVal.append(ch)
            }
        }
        result.add(curVal.toString().trim().removeSurrounding("\""))
        return result
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"").replace("\n", " ")
    }
}
