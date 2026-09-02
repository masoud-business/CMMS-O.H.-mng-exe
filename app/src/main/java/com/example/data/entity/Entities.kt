package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 1. کاربران سیستم (Users)
 * نقش‌ها:
 * - admin: مدیر ارشد اورهال
 * - planner: برنامه‌ریز اجرایی
 * - supervisor: ناظر و سرپرست اجرایی
 * - unit_head: مدیر و رئیس واحد اجرایی
 * - hse: سرپرست و کارشناس ایمنی و بهداشت (مهندس محب ایران)
 * - commercial: نماینده واحد بازرگانی و خرید کالا (مهندس بازرگان)
 * - qc: واحد بازرسی فنی و کنترل کیفیت
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String = "",
    val password: String = "1234",
    val name: String,
    val email: String,
    val role: String, // "admin", "planner", "supervisor", "unit_head", "hse", "commercial", "qc"
    val unit: String? = null, // "مکانیک", "برق", "ابزاردقیق و اتوماسیون", "نسوز", "انرژی و سیالات", "بازرسی فنی", "HSE و ایمنی", "بازرگانی و تامین کالا"
    val siteId: String = "GHADIR_NEYRIZ",
    val isActive: Boolean = true
)

/**
 * 2. برنامه‌های اورهال سالیانه (Oversights)
 */
@Entity(tableName = "oversights")
data class OversightEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val year: Int,
    val equipmentType: String, // کارخانه احیای مستقیم فولاد غدیر نی‌ریز
    val plannedStartDate: String,
    val plannedEndDate: String,
    val actualStartDate: String? = null,
    val actualEndDate: String? = null,
    val status: String = "active", // "draft", "active", "completed", "cancelled"
    val siteId: String = "GHADIR_NEYRIZ"
)

/**
 * 3. آیتم‌های اجرایی ساختار شکست کار (WBS Items Entity in Room)
 * ساختار سلسله‌مراتبی شکست کار شامل:
 * - نام فعالیت: title (String)
 * - شناسه والد: parentItemId (Long? با کلید خارجی به همین جدول)
 * - درصد پیشرفت: progressPercentage (Int: 0 تا 100)
 * - بازه زمانی: plannedStartDate, plannedEndDate, durationHours, actualStartDate, actualEndDate, actualHours
 * 
 * همچنین تفکیک دقیق بر اساس:
 * - نواحی اصلی: Core Area, MHU, WTP (سیستم آب و پساب)
 * - واحدهای اصلی تعمیراتی: مکانیک، برق، ابزاردقیق و اتوماسیون (یکپارچه)، نسوز، انرژی و سیالات، بازرسی فنی
 */
@Entity(
    tableName = "oversight_items",
    foreignKeys = [
        ForeignKey(
            entity = OversightEntity::class,
            parentColumns = ["id"],
            childColumns = ["oversightId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = OversightItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("oversightId"),
        Index("parentItemId"),
        Index("executiveUnit"),
        Index("generalArea")
    ]
)
data class OversightItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val oversightId: Long = 1,
    val wbsCode: String, // شناسه یا کد سلسله‌مراتبی WBS (مانند 1.2.3)
    val title: String, // نام فعالیت اجرایی
    val parentItemId: Long? = null, // شناسه فعالیت والد جهت ساختار درختی
    val outlineLevel: Int = 1, // سطح سلسله‌مراتب (1 تا 6)
    val executiveUnit: String, // واحد تعمیراتی: «مکانیک»، «برق»، «ابزاردقیق و اتوماسیون»، «نسوز»، «انرژی و سیالات»، «بازرسی فنی»
    val generalArea: String = "", // ناحیه اصلی: «Core Area»، «MHU»، «WTP»
    val executionLocation: String = "", // محل دقیق اجرا: «Furnace Area»، «Pump House»، «Clarifier»، «Day Bin»
    val equipmentName: String = "", // نام تجهیز صنعتی: «Furnace charge hopper»، «Compressor»، «Clarifier»
    val durationHours: Double = 0.0, // مدت زمان برآوردی (ساعت) - بخشی از بازه زمانی
    val actualHours: Double = 0.0, // ساعت کارکرد واقعی صرف‌شده
    val manpowerCount: Int = 0, // تعداد نفرات اجرایی
    val status: String = "pending", // وضعیت: "pending", "in_progress", "completed", "blocked"
    val progressPercentage: Int = 0, // درصد پیشرفت فیزیکی (0 تا 100)
    val plannedStartDate: String, // تاریخ شروع برنامه‌ای - بازه زمانی
    val plannedEndDate: String, // تاریخ پایان برنامه‌ای - بازه زمانی
    val actualStartDate: String? = null, // تاریخ شروع واقعی
    val actualEndDate: String? = null, // تاریخ پایان واقعی
    val lastUpdatedDate: String = "",
    val notes: String = "",
    val issues: String = "",
    val requiresTechnicalInspection: Boolean = false, // نیاز به تایید بازرسی فنی (حداکثر تا ۹۵٪ بدون تاییدیه QC)
    val qcApproved: Boolean = false, // تایید نهایی بازرسی فنی
    val qcApprovedBy: String = "", // نام تایید کننده بازرسی فنی (مهندس خاکی)
    val qcApprovalDate: String = "", // تاریخ تایید بازرسی فنی
    val active: Boolean = true,
    val siteId: String = "GHADIR_NEYRIZ"
)

/**
 * 4. پیش‌نیازهای آیتم‌ها (Item Prerequisites - Many to Many)
 */
@Entity(
    tableName = "item_prerequisites",
    primaryKeys = ["itemId", "prerequisiteItemId"],
    foreignKeys = [
        ForeignKey(
            entity = OversightItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = OversightItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["prerequisiteItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("itemId"),
        Index("prerequisiteItemId")
    ]
)
data class ItemPrerequisiteEntity(
    val itemId: Long,
    val prerequisiteItemId: Long
)

/**
 * 5. تخصیص آیتم‌ها به سرپرستان (Item Assignments)
 */
@Entity(
    tableName = "item_assignments",
    primaryKeys = ["itemId", "supervisorUserId"],
    foreignKeys = [
        ForeignKey(
            entity = OversightItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["supervisorUserId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("itemId"),
        Index("supervisorUserId")
    ]
)
data class ItemAssignmentEntity(
    val itemId: Long,
    val supervisorUserId: Long
)

/**
 * 6. گزارشات روزانه سرپرستان و ثبت کارکرد (Daily Work Logs)
 * برای ثبت درصد پیشرفت روزانه، تعداد نفر، ساعت کارکرد و همگام‌سازی پایان روز با MSP
 */
@Entity(
    tableName = "daily_work_logs",
    foreignKeys = [
        ForeignKey(
            entity = OversightItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("itemId"), Index("oversightId"), Index("date")]
)
data class DailyWorkLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: Long,
    val oversightId: Long,
    val date: String, // تاریخ گزارش e.g. 1404/10/14
    val progressPercentage: Int, // درصد پیشرفت در این روز (0 تا 100)
    val manpowerCount: Int = 1, // تعداد نفرات شیفت
    val hoursSpent: Double = 0.0, // ساعات کارکرد در این روز
    val recordedByUserId: Long,
    val recordedByUserName: String,
    val unitName: String,
    val remarks: String = "",
    val issues: String = "",
    val syncedToMsp: Boolean = false
)

/**
 * 7. جلسات هماهنگی (Planning Sessions)
 */
@Entity(
    tableName = "planning_sessions",
    foreignKeys = [
        ForeignKey(
            entity = OversightEntity::class,
            parentColumns = ["id"],
            childColumns = ["oversightId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("oversightId")]
)
data class PlanningSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val oversightId: Long,
    val title: String,
    val sessionDate: String,
    val location: String = "اتاق کنترل مرکزی / دفتر اورهال فولاد غدیر نی‌ریز",
    val minutesSummary: String = "",
    val siteId: String = "GHADIR_NEYRIZ"
)

/**
 * یادداشت‌های جلسه (Session Notes)
 */
@Entity(
    tableName = "session_notes",
    foreignKeys = [
        ForeignKey(
            entity = PlanningSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = OversightItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("sessionId"), Index("itemId")]
)
data class SessionNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val itemId: Long? = null,
    val noteText: String,
    val authorName: String = "",
    val createdAt: String
)

/**
 * تصمیمات جلسه (Session Decisions)
 */
@Entity(
    tableName = "session_decisions",
    foreignKeys = [
        ForeignKey(
            entity = PlanningSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = OversightItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("sessionId"), Index("itemId")]
)
data class SessionDecisionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val itemId: Long? = null,
    val decisionText: String,
    val status: String = "pending", // "pending", "implemented", "cancelled"
    val assignedUnit: String = "",
    val createdAt: String
)

/**
 * 8. درخواست‌های خرید، قطعات یدکی و متریال (Procurement Requests - فرآیند بازرگانی با حضور مهندس بازرگان)
 * چرخه تایید:
 * ثبت درخواست توسط سرپرست با کد کالا -> تایید رئیس واحد اجرایی -> تایید مدیر پروژه -> خرید توسط مهندس بازرگان -> اعلام موجودی در انبار همراه با آلارم
 */
@Entity(
    tableName = "procurement_requests",
    foreignKeys = [
        ForeignKey(
            entity = OversightItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("itemId")]
)
data class ProcurementRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: Long? = null, // شناسه فعالیت WBS (اختیاری برای خریدهای عمومی)
    val sessionId: Long? = null,
    val materialCode: String = "", // کد متریال و قطعه (الزامی - مثال: MAT-HYD-5021)
    val title: String, // نام قطعه، تجهیز یا متریال درخواستی
    val requestingUnit: String = "مکانیک", // واحد متقاضی: مکانیک، برق، ابزاردقیق، نسوز و...
    val itemType: String = "goods", // "goods", "spare_part", "consumable", "contractor_service"
    val quantity: String = "1 عدد", // مقدار / تعداد بدون نیاز به برآورد قیمت توسط مجری
    val estimatedCost: String = "", // در صورت نیاز توسط بازرگانی تکمیل می‌شود
    val status: String = "requested", // "requested", "unit_head_approved", "pm_approved", "in_procurement", "supplied_available", "rejected"
    val requestedByUserId: Long,
    val requestedByUserName: String,
    val unitHeadApproved: Boolean = false,
    val unitHeadApprovedBy: String? = null,
    val projectManagerApproved: Boolean = false,
    val projectManagerApprovedBy: String? = null,
    val commercialRepName: String = "مهندس بازرگان (واحد بازرگانی)", // نماینده بازرگانی
    val warehouseLocation: String = "", // محل تحویل یا قفسه انبار پس از تامین
    val rejectionReason: String? = null,
    val createdAt: String,
    val supplyDate: String? = null,
    val siteId: String = "GHADIR_NEYRIZ"
)

/**
 * 9. لاگ‌های ممیزی و حسابرسی تغییرات (Audit Logs)
 */
@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entityType: String,
    val entityId: Long,
    val action: String, // "STATUS_CHANGE", "PROGRESS_UPDATE", "CREATE", "UPDATE", "APPROVED", "REJECTED", "MSP_SYNC", "QC_APPROVE", "PERMIT_SUSPEND"
    val performedByUserId: Long,
    val performedByUserName: String,
    val performedByUserRole: String,
    val beforeStateJson: String,
    val afterStateJson: String,
    val remarks: String = "",
    val timestamp: String
)

/**
 * 10. اعلانات درون‌برنامه‌ای (Notifications)
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val message: String,
    val isRead: Boolean = false,
    val linkEntityType: String? = null,
    val linkEntityId: Long? = null,
    val createdAt: String
)

/**
 * 11. پرمیت‌های ایمنی کار در کارگاه (Safety & HSE Work Permits)
 * با مدیریت مهندس محب ایران (واحد HSE)
 * شامل پرمیت‌های کار گرم، کار در ارتفاع، فضای بسته، ایزولاسیون LOTO، کارت قرمز برق، حفاری و متفرقه
 */
@Entity(
    tableName = "safety_permits",
    foreignKeys = [
        ForeignKey(
            entity = OversightItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("itemId"), Index("oversightId"), Index("permitNumber")]
)
data class SafetyPermitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: Long? = null,
    val customTaskTitle: String = "",
    val oversightId: Long = 1,
    val permitNumber: String,
    val permitType: String, // "Hot Work", "Confined Space", "Height Work", "High Voltage"
    val status: String = "requested", // requested, unit_approved, issued, suspended, extended, closed
    val executiveUnit: String,
    val location: String = "",
    val equipmentName: String = "",
    val requestDate: String = "1404/10/14",
    val issueDate: String? = null,
    val validHours: Int = 8,
    val expiryTimestamp: Long = 0L, // زمان دقیق انقضا برای محاسبه تایمر معکوس

    // فیلدهای امضا و تایید سلسله مراتبی
    val requestedByUserId: Long = 0L,
    val requestedByUserName: String = "",
    val unitHeadApproved: Boolean = false,
    val unitHeadApprovedBy: String = "",
    val issuedByUserId: Long = 0L,
    val issuedByUserName: String = "",

    // بخش ایزولاسیون و LOTO حرفه‌ای
    val requiresElectricalLoto: Boolean = false,
    val electricalLotoStatus: String = "not_required", // not_required, pending, isolated, energized
    val electricalTaggedBy: String = "",
    val lotoLockNumbersJson: String = "", // ذخیره شماره قفل‌ها به صورت آرایه جیسون

    // بخش سنجش گاز محیطی (Gas Test)
    val requiresGasTest: Boolean = false,
    val gasTestResultO2: String = "",
    val gasTestResultCO: String = "",
    val gasTestResultLEL: String = "",
    val gasTesterName: String = "",
    val lastGasTestTimestamp: String = "",

    // مستندات و چک‌لیست پویای بازرسی میدانی
    val requiresScaffoldingTag: Boolean = false,
    val fireWatchRequired: Boolean = false,
    val safetyPrecautions: String = "",
    val ppeRequirements: String = "",
    val photoAttachmentsJson: String = "", // پیوست تصاویر خطرات کارگاه

    // مدیریت تمدید و تعلیق پرمیت
    val extensionCount: Int = 0,
    val extendedByUserName: String = "",
    val stopReason: String = "",
    val stopDetails: String = "",
    val checklistResultsJson: String = "",
    val createdAt: String = "",
    val siteId: String = "GHADIR_NEYRIZ"
) {
    @get:Ignore
    val gasTestResult: String
        get() = if (gasTestResultO2.isNotBlank() || gasTestResultCO.isNotBlank() || gasTestResultLEL.isNotBlank()) {
            listOfNotNull(
                if (gasTestResultO2.isNotBlank()) "O2: $gasTestResultO2" else null,
                if (gasTestResultCO.isNotBlank()) "CO: $gasTestResultCO" else null,
                if (gasTestResultLEL.isNotBlank()) "LEL: $gasTestResultLEL" else null
            ).joinToString(" | ")
        } else ""
}

/**
 * 12. امضاهای دیجیتال چندمرحله‌ای با رمز پویا (Multi-Step Digital Signatures with Dynamic Token)
 * شامل سلسله مراتب:
 * مرحله ۱: ثبت درخواست توسط ناظر اجرایی (Supervisor)
 * مرحله ۲: تایید رئیس واحد متقاضی (Unit Head)
 * مرحله ۳: صدور پرمیت توسط سرپرست HSE (مهندس محب ایران)
 * مرحله ۴: تایید نظارتی مدیر ارشد اورهال (Plant / Overhaul Director)
 */
@Entity(
    tableName = "digital_signatures",
    indices = [Index("documentType"), Index("documentId"), Index("signerUserId")]
)
data class DigitalSignatureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val documentType: String, // "safety_permit", "wbs_task", "qa_qc"
    val documentId: Long,
    val stepOrder: Int, // 1: Supervisor, 2: Unit Head, 3: HSE, 4: Overhaul Director
    val stepTitle: String,
    val signerUserId: Long,
    val signerName: String,
    val signerRole: String,
    val signerUnit: String = "",
    val signatureStatus: String = "signed", // "signed", "rejected", "pending"
    val dynamicToken: String, // رمز پویا (OTP) ۶ رقمی
    val tokenGeneratedAt: String = "",
    val signedAt: String,
    val deviceFingerprint: String = "سامانه اورهال نایریز - تبلت صنعتی",
    val comments: String = "",
    val siteId: String = "GHADIR_NEYRIZ"
)
