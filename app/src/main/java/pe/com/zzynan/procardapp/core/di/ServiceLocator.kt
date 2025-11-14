package pe.com.zzynan.procardapp.core.di

import android.content.Context
import pe.com.zzynan.procardapp.data.local.database.AppDatabase
import pe.com.zzynan.procardapp.data.repository.DailyMetricsRepository
import pe.com.zzynan.procardapp.data.repository.UserProfileRepository

/**
 * Service locator sencillo para esta iteración. Evita recrear DAOs y garantiza singletons livianos.
 */
object ServiceLocator {

    fun provideDatabase(context: Context): AppDatabase = AppDatabase.getInstance(context)

    fun provideDailyMetricsRepository(context: Context): DailyMetricsRepository {
        val database = provideDatabase(context)
        return DailyMetricsRepository(database.dailyMetricsDao())
    }

    fun provideUserProfileRepository(context: Context): UserProfileRepository {
        val database = provideDatabase(context)
        return UserProfileRepository(database.userProfileDao())
    }
}
