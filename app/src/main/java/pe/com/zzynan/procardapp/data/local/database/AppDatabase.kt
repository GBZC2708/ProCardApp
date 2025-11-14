package pe.com.zzynan.procardapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pe.com.zzynan.procardapp.data.local.dao.DailyMetricsDao
import pe.com.zzynan.procardapp.data.local.entity.DailyMetricsEntity

/**
 * Base de datos de Room configurada como singleton para evitar múltiples instancias en memoria.
 * Incluye configuración WAL para mejorar rendimiento de escritura/lectura concurrente.
 */
@Database(
    entities = [DailyMetricsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Expone el DAO de métricas diarias hacia la capa de datos.
     */
    abstract fun dailyMetricsDao(): DailyMetricsDao

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
