package pe.com.zzynan.procardapp.core.di

import android.content.Context
import pe.com.zzynan.procardapp.data.local.database.AppDatabase
import pe.com.zzynan.procardapp.data.repository.DailyMetricsRepository
import pe.com.zzynan.procardapp.data.repository.FoodRepository
import pe.com.zzynan.procardapp.data.repository.FoodRepositoryImpl
import pe.com.zzynan.procardapp.data.repository.SupplementRepository
import pe.com.zzynan.procardapp.data.repository.TrainingRepository
import pe.com.zzynan.procardapp.data.repository.TrainingRepositoryImpl
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

    fun provideFoodRepository(context: Context): FoodRepository {
        val database = provideDatabase(context)
        return FoodRepositoryImpl(database.foodDao())
    }

    fun provideTrainingRepository(context: Context): TrainingRepository {
        val database = provideDatabase(context)
        return TrainingRepositoryImpl(database.trainingDao())
    }

    fun provideSupplementRepository(context: Context): SupplementRepository {
        val database = provideDatabase(context)
        return SupplementRepository(
            supplementDao = database.supplementDao(),
            dailySupplementDao = database.dailySupplementDao()
        )
    }
}
