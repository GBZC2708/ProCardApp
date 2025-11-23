package pe.com.zzynan.procardapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import pe.com.zzynan.procardapp.data.local.dao.DailyMetricsDao
import pe.com.zzynan.procardapp.data.local.dao.DailySupplementDao
import pe.com.zzynan.procardapp.data.local.dao.FoodDao
import pe.com.zzynan.procardapp.data.local.dao.SupplementDao
import pe.com.zzynan.procardapp.data.local.dao.TrainingDao
import pe.com.zzynan.procardapp.data.local.dao.UserProfileDao
import pe.com.zzynan.procardapp.data.local.entity.DailyFoodEntryEntity
import pe.com.zzynan.procardapp.data.local.entity.DailyMetricsEntity
import pe.com.zzynan.procardapp.data.local.entity.DailySupplementEntryEntity
import pe.com.zzynan.procardapp.data.local.entity.ExerciseSetStatsEntity
import pe.com.zzynan.procardapp.data.local.entity.FoodItemEntity
import pe.com.zzynan.procardapp.data.local.entity.RoutineDayEntity
import pe.com.zzynan.procardapp.data.local.entity.RoutineExerciseEntity
import pe.com.zzynan.procardapp.data.local.entity.SupplementItemEntity
import pe.com.zzynan.procardapp.data.local.entity.UserProfileEntity
import pe.com.zzynan.procardapp.data.local.entity.WorkoutExerciseEntity
import pe.com.zzynan.procardapp.data.local.entity.WorkoutSessionEntity
import pe.com.zzynan.procardapp.data.local.entity.WorkoutSetEntryEntity

/**
 * Base de datos de Room configurada como singleton para evitar múltiples instancias en memoria.
 * Se habilita WAL y se mantienen solo las entidades necesarias para reducir IO innecesario.
 */
@Database(
    entities = [
        DailyMetricsEntity::class,
        UserProfileEntity::class,
        FoodItemEntity::class,
        DailyFoodEntryEntity::class,
        WorkoutExerciseEntity::class,
        RoutineDayEntity::class,
        RoutineExerciseEntity::class,
        WorkoutSessionEntity::class,
        WorkoutSetEntryEntity::class,
        ExerciseSetStatsEntity::class,
        SupplementItemEntity::class,
        DailySupplementEntryEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(LocalDateConverters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Expone el DAO de métricas diarias hacia la capa de datos.
     */
    abstract fun dailyMetricsDao(): DailyMetricsDao

    /**
     * Expone el DAO del perfil de usuario minimizando accesos redundantes.
     */
    abstract fun userProfileDao(): UserProfileDao

    /**
     * Expone el DAO de alimentos y planificación diaria.
     */
    abstract fun foodDao(): FoodDao

    /**
     * Expone el DAO del módulo de entrenamiento.
     */
    abstract fun trainingDao(): TrainingDao

    /**
     * Expone el DAO del catálogo de suplementos.
     */
    abstract fun supplementDao(): SupplementDao

    /**
     * Expone el DAO del plan diario de suplementos.
     */
    abstract fun dailySupplementDao(): DailySupplementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Obtiene la instancia única de la base de datos usando doble verificación sincronizada.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        /**
         * Construye la base de datos con políticas de migración seguras y WAL habilitado.
         */
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "daily_metrics_db"
            )
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .addMigrations(
                    MIGRATION_4_5,
                    MIGRATION_5_6
                )
                .build()
        }

        /**
         * Migración 4 -> 5: agrega orderIndex a routine_exercises.
         */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1) Agregar la columna orderIndex con default 0 (no rompe nada existente)
                database.execSQL(
                    "ALTER TABLE routine_exercises ADD COLUMN orderIndex INTEGER NOT NULL DEFAULT 0"
                )

                // 2) Inicializar orderIndex usando el id actual para mantener el orden que ya ves hoy
                database.execSQL(
                    "UPDATE routine_exercises SET orderIndex = id"
                )
            }
        }

        /**
         * Migración 5 -> 6: crea las tablas de suplementación sin tocar datos existentes.
         */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Tabla de catálogo de suplementos
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `supplement_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userName` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `baseAmount` REAL,
                        `baseUnit` TEXT,
                        `isActive` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                // Índice para userName en catálogo
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_supplement_items_userName` ON `supplement_items` (`userName`)"
                )

                // Tabla de plan diario de suplementos
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `daily_supplement_entries` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userName` TEXT NOT NULL,
                        `dateEpochDay` INTEGER NOT NULL,
                        `supplementId` INTEGER NOT NULL,
                        `timeSlot` TEXT NOT NULL,
                        `amount` REAL,
                        `unit` TEXT,
                        `orderInSlot` INTEGER NOT NULL,
                        FOREIGN KEY(`supplementId`) REFERENCES `supplement_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                // Índices para plan diario
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_daily_supplement_entries_userName` ON `daily_supplement_entries` (`userName`)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_daily_supplement_entries_dateEpochDay` ON `daily_supplement_entries` (`dateEpochDay`)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_daily_supplement_entries_supplementId` ON `daily_supplement_entries` (`supplementId`)"
                )
            }
        }
    }
}
