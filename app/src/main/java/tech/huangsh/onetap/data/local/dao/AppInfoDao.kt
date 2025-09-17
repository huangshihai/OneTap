package tech.huangsh.onetap.data.local.dao

import androidx.room.*
import tech.huangsh.onetap.data.model.AppInfo
import kotlinx.coroutines.flow.Flow

/**
 * 应用信息数据访问对象
 */
@Dao
interface AppInfoDao {

    @Query("SELECT * FROM apps WHERE isEnabled = 1 ORDER BY `order` ASC")
    fun getEnabledApps(): Flow<List<AppInfo>>

    @Query("SELECT * FROM apps ORDER BY `order` ASC")
    fun getAllApps(): Flow<List<AppInfo>>
    
    @Query("SELECT * FROM apps ORDER BY `order` ASC")
    suspend fun getAllAppsList(): List<AppInfo>

    @Query("SELECT * FROM apps WHERE packageName = :packageName")
    suspend fun getAppByPackage(packageName: String): AppInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(appInfo: AppInfo)

    @Update
    suspend fun updateApp(appInfo: AppInfo)

    @Delete
    suspend fun deleteApp(appInfo: AppInfo)

    @Query("UPDATE apps SET isEnabled = :isEnabled WHERE packageName = :packageName")
    suspend fun updateAppEnabledStatus(packageName: String, isEnabled: Boolean)

    @Query("UPDATE apps SET `order` = :newOrder WHERE packageName = :packageName")
    suspend fun updateAppOrder(packageName: String, newOrder: Int)

    @Query("SELECT MAX(`order`) FROM apps")
    suspend fun getMaxOrder(): Int?

    @Query("SELECT COUNT(*) FROM apps WHERE isEnabled = 1")
    fun getEnabledAppCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM apps")
    fun getAppCount(): Flow<Int>

    @Query("DELETE FROM apps")
    suspend fun deleteAllApps()

    @Query("SELECT MAX(`order`) FROM apps WHERE isEnabled = 1")
    suspend fun getMaxEnabledAppOrder(): Int?
}