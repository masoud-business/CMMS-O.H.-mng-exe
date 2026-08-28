package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object برای تمام عملیات‌های سامانه هماهنگی اورهال فولاد غدیر نی‌ریز
 */
@Dao
interface OverhaulDao {

    // --- Users & Authentication ---
    @Query("SELECT * FROM users WHERE isActive = 1 ORDER BY id ASC")
    fun getAllActiveUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(TRIM(username)) = LOWER(TRIM(:username)) LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(TRIM(username)) = LOWER(TRIM(:username)) AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): UserEntity?

    @Query("UPDATE users SET password = :newPassword WHERE id = :userId")
    suspend fun updatePassword(userId: Long, newPassword: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    // --- Oversights (برنامه‌های اورهال) ---
    @Query("SELECT * FROM oversights ORDER BY year DESC, id DESC")
    fun getAllOversights(): Flow<List<OversightEntity>>

    @Query("SELECT * FROM oversights WHERE id = :id LIMIT 1")
    suspend fun getOversightById(id: Long): OversightEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOversight(oversight: OversightEntity): Long

    @Update
    suspend fun updateOversight(oversight: OversightEntity)

    @Query("DELETE FROM oversights WHERE id = :id")
    suspend fun deleteOversightById(id: Long)

    // --- Oversight Items (WBS) ---
    @Query("SELECT * FROM oversight_items WHERE oversightId = :oversightId ORDER BY wbsCode ASC, id ASC")
    fun getItemsForOversight(oversightId: Long): Flow<List<OversightItemEntity>>

    @Query("SELECT * FROM oversight_items WHERE oversightId = :oversightId ORDER BY wbsCode ASC, id ASC")
    suspend fun getItemsForOversightDirect(oversightId: Long): List<OversightItemEntity>

    @Query("SELECT * FROM oversight_items WHERE oversightId = :oversightId AND executiveUnit = :unit ORDER BY wbsCode ASC, id ASC")
    fun getItemsByUnit(oversightId: Long, unit: String): Flow<List<OversightItemEntity>>

    @Query("SELECT * FROM oversight_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): OversightItemEntity?

    @Query("SELECT * FROM oversight_items WHERE id IN (:ids)")
    suspend fun getItemsByIds(ids: List<Long>): List<OversightItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: OversightItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<OversightItemEntity>): List<Long>

    @Update
    suspend fun updateItem(item: OversightItemEntity)

    @Query("SELECT * FROM oversight_items WHERE parentItemId = :parentId ORDER BY wbsCode ASC, id ASC")
    fun getDirectChildren(parentId: Long): Flow<List<OversightItemEntity>>

    @Query("SELECT * FROM oversight_items WHERE parentItemId = :parentId ORDER BY wbsCode ASC, id ASC")
    suspend fun getDirectChildrenList(parentId: Long): List<OversightItemEntity>

    @Query("SELECT * FROM oversight_items WHERE oversightId = :oversightId AND (parentItemId IS NULL OR parentItemId = 0) ORDER BY wbsCode ASC, id ASC")
    fun getRootItemsForOversight(oversightId: Long): Flow<List<OversightItemEntity>>

    @Query("UPDATE oversight_items SET progressPercentage = :progress, status = :status, actualHours = :actualHours, lastUpdatedDate = :lastUpdated WHERE id = :id")
    suspend fun updateItemProgress(id: Long, progress: Int, status: String, actualHours: Double, lastUpdated: String)

    @Query("UPDATE oversight_items SET parentItemId = :parentItemId, outlineLevel = :outlineLevel, wbsCode = :wbsCode WHERE id = :id")
    suspend fun updateItemHierarchy(id: Long, parentItemId: Long?, outlineLevel: Int, wbsCode: String)

    @Query("DELETE FROM oversight_items WHERE id = :id OR parentItemId = :id")
    suspend fun deleteItemAndDirectSubtasks(id: Long)

    @Query("DELETE FROM oversight_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("DELETE FROM oversight_items WHERE oversightId = :oversightId")
    suspend fun deleteItemsForOversight(oversightId: Long)

    // --- Prerequisites (پیش‌نیازها) ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPrerequisite(prerequisite: ItemPrerequisiteEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPrerequisites(prerequisites: List<ItemPrerequisiteEntity>)

    @Query("DELETE FROM item_prerequisites WHERE itemId = :itemId")
    suspend fun deletePrerequisitesForItem(itemId: Long)

    @Query("SELECT prerequisiteItemId FROM item_prerequisites WHERE itemId = :itemId")
    fun getPrerequisiteIdsForItem(itemId: Long): Flow<List<Long>>

    @Query("SELECT * FROM item_prerequisites WHERE itemId = :itemId")
    suspend fun getPrerequisitesListForItem(itemId: Long): List<ItemPrerequisiteEntity>

    @Query("""
        SELECT item.* FROM oversight_items item
        INNER JOIN item_prerequisites p ON item.id = p.prerequisiteItemId
        WHERE p.itemId = :itemId
    """)
    fun getPrerequisiteItems(itemId: Long): Flow<List<OversightItemEntity>>

    @Query("""
        SELECT item.* FROM oversight_items item
        INNER JOIN item_prerequisites p ON item.id = p.prerequisiteItemId
        WHERE p.itemId = :itemId
    """)
    suspend fun getPrerequisiteItemsDirect(itemId: Long): List<OversightItemEntity>

    @Query("SELECT * FROM item_prerequisites")
    fun getAllPrerequisites(): Flow<List<ItemPrerequisiteEntity>>

    // --- Item Assignments (تخصیص سرپرست) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: ItemAssignmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<ItemAssignmentEntity>)

    @Query("DELETE FROM item_assignments WHERE itemId = :itemId")
    suspend fun deleteAssignmentsForItem(itemId: Long)

    @Query("SELECT supervisorUserId FROM item_assignments WHERE itemId = :itemId")
    fun getAssignedUserIdsForItem(itemId: Long): Flow<List<Long>>

    @Query("SELECT itemId FROM item_assignments WHERE supervisorUserId = :userId")
    fun getItemIdsAssignedToUser(userId: Long): Flow<List<Long>>

    @Query("SELECT itemId FROM item_assignments WHERE supervisorUserId = :userId")
    suspend fun getItemIdsAssignedToUserDirect(userId: Long): List<Long>

    @Query("SELECT * FROM item_assignments")
    fun getAllAssignments(): Flow<List<ItemAssignmentEntity>>

    @Query("""
        SELECT i.* FROM oversight_items i
        INNER JOIN item_assignments a ON i.id = a.itemId
        WHERE a.supervisorUserId = :supervisorId AND i.oversightId = :oversightId
        ORDER BY i.wbsCode ASC
    """)
    fun getAssignedItemsForSupervisor(supervisorId: Long, oversightId: Long): Flow<List<OversightItemEntity>>

    // --- Daily Work Logs (گزارشات روزانه سرپرستان) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyWorkLog(log: DailyWorkLogEntity): Long

    @Query("SELECT * FROM daily_work_logs WHERE oversightId = :oversightId ORDER BY id DESC")
    fun getDailyLogsForOversight(oversightId: Long): Flow<List<DailyWorkLogEntity>>

    @Query("SELECT * FROM daily_work_logs WHERE date = :date ORDER BY id DESC")
    fun getDailyLogsForDate(date: String): Flow<List<DailyWorkLogEntity>>

    @Query("SELECT * FROM daily_work_logs WHERE itemId = :itemId ORDER BY id DESC")
    fun getDailyLogsForItem(itemId: Long): Flow<List<DailyWorkLogEntity>>

    @Query("UPDATE daily_work_logs SET syncedToMsp = 1 WHERE id IN (:logIds)")
    suspend fun markLogsAsSyncedToMsp(logIds: List<Long>)

    // --- Planning Sessions (جلسات) ---
    @Query("SELECT * FROM planning_sessions WHERE oversightId = :oversightId ORDER BY sessionDate DESC, id DESC")
    fun getSessionsForOversight(oversightId: Long): Flow<List<PlanningSessionEntity>>

    @Query("SELECT * FROM planning_sessions ORDER BY sessionDate DESC, id DESC")
    fun getAllSessions(): Flow<List<PlanningSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PlanningSessionEntity): Long

    // --- Session Notes & Decisions ---
    @Query("SELECT * FROM session_notes WHERE sessionId = :sessionId ORDER BY id ASC")
    fun getNotesForSession(sessionId: Long): Flow<List<SessionNoteEntity>>

    @Query("SELECT * FROM session_notes ORDER BY id DESC")
    fun getAllSessionNotes(): Flow<List<SessionNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionNote(note: SessionNoteEntity): Long

    @Query("SELECT * FROM session_decisions WHERE sessionId = :sessionId ORDER BY id ASC")
    fun getDecisionsForSession(sessionId: Long): Flow<List<SessionDecisionEntity>>

    @Query("SELECT * FROM session_decisions ORDER BY id DESC")
    fun getAllSessionDecisions(): Flow<List<SessionDecisionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionDecision(decision: SessionDecisionEntity): Long

    @Update
    suspend fun updateSessionDecision(decision: SessionDecisionEntity)

    // --- Procurement Requests (درخواست‌های خرید) ---
    @Query("""
        SELECT p.* FROM procurement_requests p
        INNER JOIN oversight_items i ON p.itemId = i.id
        WHERE i.oversightId = :oversightId
        ORDER BY p.id DESC
    """)
    fun getProcurementsForOversight(oversightId: Long): Flow<List<ProcurementRequestEntity>>

    @Query("SELECT * FROM procurement_requests ORDER BY id DESC")
    fun getAllProcurements(): Flow<List<ProcurementRequestEntity>>

    @Query("SELECT * FROM procurement_requests WHERE id = :id LIMIT 1")
    suspend fun getProcurementById(id: Long): ProcurementRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProcurement(request: ProcurementRequestEntity): Long

    @Update
    suspend fun updateProcurement(request: ProcurementRequestEntity)

    // --- Audit Logs (سوابق ممیزی) ---
    @Query("SELECT * FROM audit_logs ORDER BY id DESC LIMIT 200")
    fun getRecentAuditLogs(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE entityType = :type AND entityId = :entityId ORDER BY id DESC")
    fun getAuditLogsForEntity(type: String, entityId: Long): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity): Long

    // --- Notifications ---
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY id DESC")
    fun getNotificationsForUser(userId: Long): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: Long)

    // --- Safety & HSE Work Permits (پرمیت‌های ایمنی و LOTO) ---
    @Query("SELECT * FROM safety_permits ORDER BY id DESC")
    fun getAllSafetyPermits(): Flow<List<SafetyPermitEntity>>

    @Query("SELECT * FROM safety_permits WHERE oversightId = :oversightId ORDER BY id DESC")
    fun getSafetyPermitsForOversight(oversightId: Long): Flow<List<SafetyPermitEntity>>

    @Query("SELECT * FROM safety_permits WHERE itemId = :itemId LIMIT 1")
    fun getSafetyPermitForItem(itemId: Long): Flow<SafetyPermitEntity?>

    @Query("SELECT * FROM safety_permits WHERE itemId = :itemId LIMIT 1")
    suspend fun getSafetyPermitForItemDirect(itemId: Long): SafetyPermitEntity?

    @Query("SELECT * FROM safety_permits WHERE id = :id LIMIT 1")
    suspend fun getSafetyPermitById(id: Long): SafetyPermitEntity?

    @Query("SELECT * FROM safety_permits WHERE requiresElectricalLoto = 1 ORDER BY id DESC")
    fun getElectricalLotoPermits(): Flow<List<SafetyPermitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSafetyPermit(permit: SafetyPermitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSafetyPermits(permits: List<SafetyPermitEntity>): List<Long>

    @Update
    suspend fun updateSafetyPermit(permit: SafetyPermitEntity)

    @Query("UPDATE safety_permits SET status = :status WHERE id = :id")
    suspend fun updateSafetyPermitStatus(id: Long, status: String)

    @Query("UPDATE safety_permits SET electricalLotoStatus = :status, electricalTaggedBy = :taggedBy WHERE id = :id")
    suspend fun updateElectricalLotoStatus(id: Long, status: String, taggedBy: String)

    @Query("DELETE FROM safety_permits WHERE id = :id")
    suspend fun deleteSafetyPermitById(id: Long)
}
