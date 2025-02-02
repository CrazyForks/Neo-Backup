/*
 * Neo Backup: open-source apps backup and restore app.
 * Copyright (C) 2025  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.manager.tasks

import android.content.Context
import com.machiav3lli.backup.MODE_UNSET
import com.machiav3lli.backup.PACKAGES_LIST_GLOBAL_ID
import com.machiav3lli.backup.data.dbs.DB
import com.machiav3lli.backup.data.dbs.entity.AppExtras
import com.machiav3lli.backup.data.entity.Package
import com.machiav3lli.backup.manager.handler.LogsHandler
import com.machiav3lli.backup.manager.handler.getInstalledPackageList
import com.machiav3lli.backup.utils.FileUtils
import com.machiav3lli.backup.utils.FileUtils.ensureBackups
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException
import com.machiav3lli.backup.utils.filterPackages
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

open class ScheduledActionTask(val context: Context, private val scheduleId: Long) :
    CoroutinesAsyncTask<Unit?, String, Triple<String, List<String>, Int>>(), KoinComponent {
    val database: DB by inject()

    override fun doInBackground(vararg params: Unit?): Triple<String, List<String>, Int>? {

        val scheduleDao = database.getScheduleDao()
        val blacklistDao = database.getBlocklistDao()
        val extrasDao = database.getAppExtrasDao()

        val schedule = scheduleDao.getById(scheduleId)
            ?: return Triple("DbFailed", listOf(), MODE_UNSET)

        val name = schedule.name
        val filter = schedule.filter
        val specialFilter = schedule.specialFilter
        val customList = schedule.customList.toList()
        val customBlocklist = schedule.blockList
        val globalBlocklist = blacklistDao.getBlocklistedPackages(PACKAGES_LIST_GLOBAL_ID)
        val blockList = globalBlocklist.plus(customBlocklist)
        val extrasMap = extrasDao.getAll().associateBy(AppExtras::packageName)
        val allTags = extrasDao.getAll().flatMap { it.customTags }.distinct()
        val tagsList = schedule.tagsList.filter { it in allTags }

        //TODO hg42 the whole filter mechanics should be the same for app and service

        val unfilteredPackages: List<Package> = try {

            // findBackups *is* necessary, because it's *not* done in OABX.onCreate any more
            ensureBackups()

            context.getInstalledPackageList()   // <========================== get the package list

        } catch (e: FileUtils.BackupLocationInAccessibleException) {
            Timber.e("Scheduled backup failed due to ${e.javaClass.simpleName}: $e")
            LogsHandler.logErrors(
                "Scheduled backup failed due to ${e.javaClass.simpleName}: $e"
            )
            return Triple(name, listOf(), MODE_UNSET)
        } catch (e: StorageLocationNotConfiguredException) {
            Timber.e("Scheduled backup failed due to ${e.javaClass.simpleName}: $e")
            LogsHandler.logErrors(
                "Scheduled backup failed due to ${e.javaClass.simpleName}: $e"
            )
            return Triple(name, listOf(), MODE_UNSET)
        }

        val selectedItems =
            filterPackages(
                packages = unfilteredPackages,
                extrasMap = extrasMap,
                filter = filter,
                specialFilter = specialFilter,
                whiteList = customList,
                blackList = blockList,
                tagsList = tagsList,
            ).map(Package::packageName)

        return Triple(
            name,
            selectedItems,
            schedule.mode
        )
    }
}