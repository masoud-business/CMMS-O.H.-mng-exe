package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.OverhaulDao
import com.example.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        OversightEntity::class,
        OversightItemEntity::class,
        ItemPrerequisiteEntity::class,
        ItemAssignmentEntity::class,
        DailyWorkLogEntity::class,
        PlanningSessionEntity::class,
        SessionNoteEntity::class,
        SessionDecisionEntity::class,
        ProcurementRequestEntity::class,
        AuditLogEntity::class,
        NotificationEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun overhaulDao(): OverhaulDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ghadir_neyriz_overhaul_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        GhadirNeyrizDataSeeder.seedGhadirNeyrizOverhaul(database.overhaulDao())
                    }
                }
            }
        }
    }
}
