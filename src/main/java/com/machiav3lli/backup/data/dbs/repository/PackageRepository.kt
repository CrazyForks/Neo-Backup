package com.machiav3lli.backup.data.dbs.repository

import android.app.Application
import com.machiav3lli.backup.R
import com.machiav3lli.backup.data.dbs.DB
import com.machiav3lli.backup.data.dbs.entity.AppInfo
import com.machiav3lli.backup.data.dbs.entity.Backup
import com.machiav3lli.backup.data.entity.Package
import com.machiav3lli.backup.data.entity.Package.Companion.invalidateCacheForPackage
import com.machiav3lli.backup.manager.handler.LogsHandler
import com.machiav3lli.backup.manager.handler.ShellCommands
import com.machiav3lli.backup.manager.handler.toPackageList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

class PackageRepository(
    private val db: DB,
    private val appContext: Application,
) {
    private val cc = Dispatchers.IO
    private val jcc = Dispatchers.IO + SupervisorJob()
    val theBackupsMap = ConcurrentHashMap<String, List<Backup>>()
    val mutex = Mutex()

    fun getBackupsFlow(): Flow<Map<String, List<Backup>>> =
        flow {
            emit(theBackupsMap)
            delay(1000)
        }.distinctUntilChanged()
            .flowOn(cc)

    fun getPackagesFlow(): Flow<List<Package>> = combine(
        db.getAppInfoDao().getAllFlow(),
        getBackupsFlow().map { it.values.flatten() },
    ) { appInfos, bkps -> appInfos.toPackageList(appContext) }
        .flowOn(cc)

    fun getBackupsFlow(packageName: String): Flow<List<Backup>> =
        flow {
            emit(theBackupsMap[packageName] ?: emptyList())
            delay(1000)
        }.distinctUntilChanged()
            .flowOn(cc)

    fun getBackups(packageName: String): List<Backup> = theBackupsMap[packageName] ?: emptyList()

    fun getBackups(): List<Backup> = theBackupsMap.values.flatten()

    suspend fun updatePackage(packageName: String) = withContext(jcc) {
        invalidateCacheForPackage(packageName)
        val new = Package(appContext, packageName)
        if (!new.isSpecial) {
            new.refreshFromPackageManager(appContext)
            db.getAppInfoDao().update(new.packageInfo as AppInfo)
        }
    }

    suspend fun upsertAppInfo(vararg appInfos: AppInfo) = withContext(jcc) {
        db.getAppInfoDao().upsert(*appInfos)
    }

    suspend fun replaceAppInfos(vararg appInfos: AppInfo) = withContext(jcc) {
        db.getAppInfoDao().updateList(*appInfos)
    }

    fun updateBackups(packageName: String, backups: List<Backup>) = runBlocking(jcc) {
        mutex.withLock {
            theBackupsMap.put(packageName, backups)
        }
    }

    fun replaceBackups(vararg backups: Backup) = runBlocking(jcc) {
        mutex.withLock {
            theBackupsMap.clear()
            theBackupsMap.putAll(backups.groupBy { it.packageName })
        }
    }

    suspend fun rewriteBackup(pkg: Package?, backup: Backup, changedBackup: Backup) {
        withContext(jcc) {
            pkg?.rewriteBackup(backup, changedBackup)
        }
    }

    fun emptyBackupsTable() = runBlocking(jcc) {
        mutex.withLock {
            theBackupsMap.clear()
        }
    }

    fun deleteAppInfoOf(packageName: String) = db.getAppInfoDao().deleteAllOf(packageName)

    fun deleteBackupsOf(packageName: String) = runBlocking(jcc) {
        mutex.withLock {
            theBackupsMap.remove(packageName)
        }
    }

    fun deleteBackupsNotIn(packageNames: List<String>) = runBlocking(jcc) {
        mutex.withLock {
            theBackupsMap.keys.removeAll { it !in packageNames }
        }
    }


    suspend fun deleteBackup(pkg: Package?, backup: Backup, onDismiss: () -> Unit) =
        withContext(jcc) {
            pkg?.let { pkg ->
                pkg.deleteBackup(backup)
                if (!pkg.isInstalled && pkg.backupList.isEmpty() && backup.packageLabel != "? INVALID") {
                    db.getAppInfoDao().deleteAllOf(pkg.packageName)
                    onDismiss()
                }
            }
        }

    suspend fun deleteAllBackups(pkg: Package?, onDismiss: () -> Unit) {
        withContext(jcc) {
            pkg?.let { pkg ->
                pkg.deleteAllBackups()
                if (!pkg.isInstalled && pkg.backupList.isEmpty()) {
                    db.getAppInfoDao().deleteAllOf(pkg.packageName)
                    onDismiss()
                }
            }
        }
    }

    @Throws(ShellCommands.ShellActionFailedException::class)
    suspend fun enableDisable(packageName: String, users: List<String>, enable: Boolean) {
        withContext(jcc) {
            ShellCommands.enableDisable(users, packageName, enable)
        }
    }

    suspend fun uninstall(
        mPackage: Package?,
        users: List<String>,
        onDismiss: () -> Unit,
        showNotification: (String) -> Unit,
    ) {
        withContext(jcc) {
            mPackage?.let { mPackage ->
                Timber.i("uninstalling: ${mPackage.packageLabel}")
                try {
                    ShellCommands.uninstall(
                        users,
                        mPackage.packageName, mPackage.apkPath,
                        mPackage.dataPath, mPackage.isSystem
                    )
                    if (mPackage.backupList.isEmpty()) {
                        db.getAppInfoDao().deleteAllOf(mPackage.packageName)
                        onDismiss()
                    }
                    showNotification(appContext.getString(R.string.uninstallSuccess))
                } catch (e: ShellCommands.ShellActionFailedException) {
                    showNotification(appContext.getString(R.string.uninstallFailure))
                    e.message?.let { message -> LogsHandler.logErrors(message) }
                }
            }
        }
    }
}