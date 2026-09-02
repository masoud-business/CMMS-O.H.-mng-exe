package com.example.data.service

import com.example.data.dao.OverhaulDao
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.DigitalSignatureEntity
import com.example.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class DynamicTokenInfo(
    val token: String,
    val generatedAtTimestamp: Long,
    val expiresAtTimestamp: Long,
    val remainingSeconds: Int
)

data class SignatureStepDef(
    val stepOrder: Int,
    val title: String,
    val targetRole: String,
    val targetRoleLabel: String,
    val description: String
)

class DigitalSignatureService(private val dao: OverhaulDao) {

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    val hierarchySteps = listOf(
        SignatureStepDef(
            stepOrder = 1,
            title = "ثبت و استعلام اولیه",
            targetRole = "supervisor",
            targetRoleLabel = "ناظر / سرپرست اجرایی کارگاه",
            description = "تایید اولیه شرح کار، الزامات محیطی و منابع اجرایی"
        ),
        SignatureStepDef(
            stepOrder = 2,
            title = "تایید رئیس واحد متقاضی",
            targetRole = "unit_head",
            targetRoleLabel = "رئیس واحد تخصصی (مکانیک / برق / نسوز / سیالات)",
            description = "بررسی فنی عملیات و تایید مجوز شروع اورهال تجهیز"
        ),
        SignatureStepDef(
            stepOrder = 3,
            title = "صدور و تایید ایمنی و بهداشت HSE",
            targetRole = "hse",
            targetRoleLabel = "سرپرست ارشد HSE (مهندس محب ایران)",
            description = "ممیزی چک‌لیست ایمنی، تست گاز محیطی و ایزولاسیون LOTO"
        ),
        SignatureStepDef(
            stepOrder = 4,
            title = "تایید نهایی مدیر ارشد اورهال",
            targetRole = "admin",
            targetRoleLabel = "مدیر کارخانه و مدیر ارشد پروژه اورهال",
            description = "تایید نهایی تطابق با برنامه زمان‌بندی کلان مجتمع"
        )
    )

    fun getSignaturesFlow(documentType: String, documentId: Long): Flow<List<DigitalSignatureEntity>> {
        return dao.getSignaturesForDocument(documentType, documentId)
    }

    /**
     * تولید رمز پویای امنیتی ۶ رقمی با اعتبار زمانی ۲ دقیقه‌ای (۱۲۰ ثانیه)
     */
    fun generateDynamicToken(userId: Long, documentId: Long): DynamicTokenInfo {
        val now = System.currentTimeMillis()
        val randomPin = Random.nextInt(100000, 999999).toString()
        val validDuration = 120_000L // 120 seconds
        return DynamicTokenInfo(
            token = randomPin,
            generatedAtTimestamp = now,
            expiresAtTimestamp = now + validDuration,
            remainingSeconds = 120
        )
    }

    /**
     * ثبت رسمی امضای دیجیتال با رمز پویا و اعمال سلسله مراتب تاییدات
     */
    suspend fun signDocumentWithDynamicToken(
        user: UserEntity,
        documentType: String,
        documentId: Long,
        stepOrder: Int,
        enteredToken: String,
        generatedToken: String,
        tokenExpiryTimestamp: Long,
        remarks: String = ""
    ): ServiceResult<DigitalSignatureEntity> {
        // ۱. اعتبارسنجی انقضا و صحت رمز پویا
        val now = System.currentTimeMillis()
        if (now > tokenExpiryTimestamp) {
            return ServiceResult.Error("رمز پویای وارد شده منقضی گردیده است. لطفاً رمز جدید دریافت نمایید.")
        }
        if (enteredToken.trim() != generatedToken.trim()) {
            return ServiceResult.Error("رمز پویای وارد شده نامعتبر است.")
        }

        // ۲. اعتبارسنجی سلسله مراتب (مرحله قبل باید حتماً امضا شده باشد)
        if (stepOrder > 1) {
            val previousStep = dao.getSignatureForStep(documentType, documentId, stepOrder - 1)
            if (previousStep == null || previousStep.signatureStatus != "signed") {
                val prevDef = hierarchySteps.firstOrNull { it.stepOrder == stepOrder - 1 }
                return ServiceResult.Error("تایید مرحله $stepOrder امکان‌پذیر نیست؛ ابتدا باید مرحله قبلی (${prevDef?.title ?: "مرحله ${stepOrder - 1}"}) امضا گردد.")
            }
        }

        // ۳. بررسی دسترسی نقش کاربر
        val stepDef = hierarchySteps.firstOrNull { it.stepOrder == stepOrder }
            ?: return ServiceResult.Error("مرحله امضای نامعتبر است.")

        val hasAccess = when (stepDef.targetRole) {
            "supervisor" -> user.role in listOf("supervisor", "planner", "admin")
            "unit_head" -> user.role in listOf("unit_head", "admin")
            "hse" -> user.role in listOf("hse", "admin") || (user.unit?.contains("ایمنی") == true)
            "admin" -> user.role == "admin"
            else -> false
        }
        if (!hasAccess) {
            return ServiceResult.Error("شما دسترسی لازم برای امضای مرحله '${stepDef.title}' را ندارید. (نیازمند نقش: ${stepDef.targetRoleLabel})")
        }

        // ۴. بررسی عدم ثبت تکراری امضا برای این مرحله
        val existing = dao.getSignatureForStep(documentType, documentId, stepOrder)
        if (existing != null && existing.signatureStatus == "signed") {
            return ServiceResult.Error("این مرحله قبلاً توسط ${existing.signerName} امضا شده است.")
        }

        val signedAt = getCurrentTimestamp()
        val signature = DigitalSignatureEntity(
            documentType = documentType,
            documentId = documentId,
            stepOrder = stepOrder,
            stepTitle = stepDef.title,
            signerUserId = user.id,
            signerName = user.name,
            signerRole = user.role,
            signerUnit = user.unit ?: "",
            signatureStatus = "signed",
            dynamicToken = enteredToken,
            tokenGeneratedAt = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(now)),
            signedAt = signedAt,
            deviceFingerprint = "تبلت امنیتی اورهال نی‌ریز • IP 10.14.0.${Random.nextInt(10, 99)}",
            comments = remarks
        )

        dao.insertDigitalSignature(signature)

        // ۵. در صورتی که سند مربوط به پرمیت ایمنی باشد، وضعیت پرمیت ارتقا می‌یابد
        if (documentType == "safety_permit") {
            val permit = dao.getSafetyPermitById(documentId)
            if (permit != null) {
                when (stepOrder) {
                    2 -> {
                        // تایید رئیس واحد
                        val updated = permit.copy(
                            status = "unit_approved",
                            unitHeadApproved = true,
                            unitHeadApprovedBy = "${user.name} (${user.unit ?: "واحد"})"
                        )
                        dao.updateSafetyPermit(updated)
                    }
                    3 -> {
                        // صدور HSE
                        val updated = permit.copy(
                            status = "issued",
                            issueDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date()),
                            expiryTimestamp = if (permit.expiryTimestamp > 0) permit.expiryTimestamp else System.currentTimeMillis() + (permit.validHours * 3600 * 1000L),
                            issuedByUserId = user.id,
                            issuedByUserName = "${user.name} (HSE)"
                        )
                        dao.updateSafetyPermit(updated)
                    }
                    4 -> {
                        // تایید نهایی مدیر ارشد
                        if (permit.status == "unit_approved") {
                            val updated = permit.copy(status = "issued")
                            dao.updateSafetyPermit(updated)
                        }
                    }
                }
            }
        }

        // ۶. ثبت لاگ حسابرسی (Audit Trail)
        dao.insertAuditLog(
            AuditLogEntity(
                entityType = "digital_signature",
                entityId = documentId,
                action = "DIGITAL_SIGNATURE_STEP_$stepOrder",
                performedByUserId = user.id,
                performedByUserName = user.name,
                performedByUserRole = user.role,
                beforeStateJson = "{}",
                afterStateJson = "{\"step\": $stepOrder, \"token\": \"$enteredToken\", \"signer\": \"${user.name}\"}",
                remarks = "امضای الکترونیکی مرحله $stepOrder (${stepDef.title}) با تایید رمز پویای امنیتی توسط ${user.name}",
                timestamp = signedAt
            )
        )

        return ServiceResult.Success(signature, "امضای دیجیتال مرحله '${stepDef.title}' با موفقیت در سامانه ثبت گردید.")
    }
}
