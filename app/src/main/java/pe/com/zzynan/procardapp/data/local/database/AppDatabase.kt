package pe.com.zzynan.procardapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pe.com.zzynan.procardapp.data.local.dao.DailyMetricsDao
import pe.com.zzynan.procardapp.data.local.dao.FoodDao
import pe.com.zzynan.procardapp.data.local.dao.TrainingDao
import pe.com.zzynan.procardapp.data.local.dao.UserProfileDao
import pe.com.zzynan.procardapp.data.local.entity.DailyFoodEntryEntity
import pe.com.zzynan.procardapp.data.local.entity.DailyMetricsEntity
import pe.com.zzynan.procardapp.data.local.entity.ExerciseSetStatsEntity
import pe.com.zzynan.procardapp.data.local.entity.FoodItemEntity
import pe.com.zzynan.procardapp.data.local.entity.RoutineDayEntity
import pe.com.zzynan.procardapp.data.local.entity.RoutineExerciseEntity
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
        ExerciseSetStatsEntity::class
    ],
    version = 4,
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
         * Se utiliza fallbackToDestructiveMigration como estrategia inicial para iteración rápida.
         */
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "daily_metrics_db"
            )
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .build()
        }
    }
}
