package pe.com.zzynan.procardapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pe.com.zzynan.procardapp.data.local.dao.DailyMetricsDao
import pe.com.zzynan.procardapp.data.local.dao.UserProfileDao
import pe.com.zzynan.procardapp.data.local.entity.DailyMetricsEntity
import pe.com.zzynan.procardapp.data.local.entity.UserProfileEntity

/**
 * Base de datos de Room configurada como singleton para evitar múltiples instancias en memoria.
 * Se habilita WAL y se mantienen solo las entidades necesarias para reducir IO innecesario.
 */
@Database(
    entities = [DailyMetricsEntity::class, UserProfileEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Expone el DAO de métricas diarias hacia la capa de datos.
     */
    abstract fun dailyMetricsDao(): DailyMetricsDao

    /**
     * Expone el DAO del perfil de usuario minimizando accesos redundantes.
     */
    abstract fun userProfileDao(): UserProfileDao

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
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
