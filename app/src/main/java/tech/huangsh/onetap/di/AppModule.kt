package tech.huangsh.onetap.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import tech.huangsh.onetap.data.local.AppDatabase
import tech.huangsh.onetap.data.local.dao.AppInfoDao
import tech.huangsh.onetap.data.local.dao.ContactDao
import tech.huangsh.onetap.data.remote.WeatherService
import tech.huangsh.onetap.data.repository.AppRepository
import tech.huangsh.onetap.data.repository.ContactRepository
import tech.huangsh.onetap.data.repository.SettingsRepository
import javax.inject.Singleton

/**
 * 应用模块 - 提供应用级别的依赖
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "one_tap_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideContactDao(database: AppDatabase): ContactDao = database.contactDao()

    @Provides
    fun provideAppInfoDao(database: AppDatabase): AppInfoDao = database.appInfoDao()

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    fun provideContactRepository(
        contactDao: ContactDao,
        @ApplicationContext context: Context
    ): ContactRepository {
        return ContactRepository(contactDao, context)
    }

    @Provides
    @Singleton
    fun provideAppRepository(
        appInfoDao: AppInfoDao,
        @ApplicationContext context: Context
    ): AppRepository {
        return AppRepository(appInfoDao, context)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository {
        return SettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideWeatherService(
        @ApplicationContext context: Context
    ): WeatherService {
        return WeatherService(context)
    }
}