/*
 * Neo Backup: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
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
package com.machiav3lli.backup.ui.pages

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ALT_MODE_APK
import com.machiav3lli.backup.ALT_MODE_BOTH
import com.machiav3lli.backup.ALT_MODE_DATA
import com.machiav3lli.backup.ALT_MODE_UNSET
import com.machiav3lli.backup.R
import com.machiav3lli.backup.data.entity.Package
import com.machiav3lli.backup.data.preferences.traceCompose
import com.machiav3lli.backup.ui.activities.NeoActivity
import com.machiav3lli.backup.ui.compose.component.ActionButton
import com.machiav3lli.backup.ui.compose.component.ActionChip
import com.machiav3lli.backup.ui.compose.component.ExpandingFadingVisibility
import com.machiav3lli.backup.ui.compose.component.FilledRoundButton
import com.machiav3lli.backup.ui.compose.component.HomePackageRecycler
import com.machiav3lli.backup.ui.compose.component.MainPackageContextMenu
import com.machiav3lli.backup.ui.compose.component.UpdatedPackageRecycler
import com.machiav3lli.backup.ui.compose.component.cachedAsyncImagePainter
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArchiveTray
import com.machiav3lli.backup.ui.compose.icons.phosphor.CaretDown
import com.machiav3lli.backup.ui.compose.icons.phosphor.CircleWavyWarning
import com.machiav3lli.backup.ui.compose.icons.phosphor.FunnelSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.List
import com.machiav3lli.backup.ui.compose.icons.phosphor.Prohibit
import com.machiav3lli.backup.ui.dialogs.BaseDialog
import com.machiav3lli.backup.ui.dialogs.BatchActionDialogUI
import com.machiav3lli.backup.ui.dialogs.GlobalBlockListDialogUI
import com.machiav3lli.backup.ui.navigation.NavItem
import com.machiav3lli.backup.ui.sheets.SortFilterSheet
import com.machiav3lli.backup.utils.altModeToMode
import com.machiav3lli.backup.utils.extensions.IconCache
import com.machiav3lli.backup.utils.extensions.koinNeoViewModel
import com.machiav3lli.backup.viewmodels.MainVM
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") // TODO remove Scaffold
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun HomePage(
    viewModel: MainVM = koinNeoViewModel(),
) {
    // TODO include tags in search
    val mActivity = LocalActivity.current as NeoActivity
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val paneNavigator = rememberListDetailPaneScaffoldNavigator<Any>()

    val mainState by viewModel.homeState.collectAsState()
    val updaterVisible = mainState.updatedPackages.isNotEmpty()
    var updaterExpanded by remember { mutableStateOf(false) }
    var menuPackage by remember { mutableStateOf<Package?>(null) }
    val menuExpanded = rememberSaveable { mutableStateOf(false) }
    val menuButtonAlwaysVisible = pref_menuButtonAlwaysVisible.value

    val openBlocklist = rememberSaveable { mutableStateOf(false) }
    val openBatchDialog = remember { mutableStateOf(false) }
    val appSheetPN: MutableState<String?> = remember { mutableStateOf(null) }

    traceCompose {
        "HomePage filtered=${
            mainState.filteredPackages.size
        } updated=${
            mainState.updatedPackages.size
        }->${
            if (updaterVisible) "visible" else "hidden"
        } menu=${
            menuExpanded.value
        } always=${menuButtonAlwaysVisible} language=${pref_languages.value}"
    }

    val onDismiss: () -> Unit = {
        scope.launch {
            paneNavigator.navigateBack()
        }
    }

    // prefetch icons
    if (mainState.filteredPackages.size > IconCache.size) {    // includes empty cache and empty filteredList
        //beginNanoTimer("prefetchIcons")
        mainState.filteredPackages.forEach { pkg ->
            cachedAsyncImagePainter(model = pkg.iconData)
        }
        //endNanoTimer("prefetchIcons")
    }

    BackHandler(scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
        scope.launch { scaffoldState.bottomSheetState.partialExpand() }
    }

    NavigableListDetailPaneScaffold(
        navigator = paneNavigator,
        listPane = {
            AnimatedPane {
                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = 0.dp,
                    sheetDragHandle = null,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    sheetShape = MaterialTheme.shapes.extraSmall,
                    sheetContent = {
                        // bottom sheets even recomposit when hidden
                        // which is bad when they contain live content
                        if (scaffoldState.bottomSheetState.currentValue != SheetValue.Hidden) {
                            SortFilterSheet(
                                sourcePage = NavItem.Home,
                                onDismiss = {
                                    scope.launch {
                                        scaffoldState.bottomSheetState.partialExpand()
                                    }
                                },
                            )
                        } else {
                            // inexpensive and small placeholder while hidden
                            // necessary because empty sheets are kept hidden
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    },
                    topBar = {
                        Column {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ActionChip(
                                    modifier = Modifier.weight(1f),
                                    icon = Phosphor.Prohibit,
                                    text = stringResource(id = R.string.sched_blocklist),
                                    positive = false,
                                    fullWidth = true,
                                ) {
                                    openBlocklist.value = true
                                }
                                ActionChip(
                                    modifier = Modifier.weight(1f),
                                    icon = Phosphor.FunnelSimple,
                                    text = stringResource(id = R.string.sort_filter),
                                    positive = true,
                                    fullWidth = true,
                                ) {
                                    scope.launch {
                                        scaffoldState.bottomSheetState.expand()
                                    }
                                }
                            }
                            HorizontalDivider(
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 8.dp),
                            )
                        }
                    }
                ) {
                    Scaffold(
                        containerColor = Color.Transparent,
                        floatingActionButton = {
                            if (mainState.selection.isNotEmpty() || updaterVisible || menuButtonAlwaysVisible) {
                                Row(
                                    modifier = Modifier.padding(start = 28.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (updaterVisible) {
                                        ExpandingFadingVisibility(
                                            expanded = updaterExpanded,
                                            expandedView = {
                                                Column {
                                                    Row(
                                                        modifier = Modifier.padding(4.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            8.dp
                                                        )
                                                    ) {
                                                        ActionButton(
                                                            text = stringResource(id = R.string.backup_all_updated),
                                                            icon = Phosphor.ArchiveTray,
                                                            modifier = Modifier.weight(1f),
                                                            fullWidth = true,
                                                            positive = true,
                                                        ) {
                                                            openBatchDialog.value = true
                                                        }
                                                        FilledRoundButton(
                                                            description = stringResource(id = R.string.dialogCancel),
                                                            icon = Phosphor.CaretDown,
                                                        ) {
                                                            updaterExpanded = !updaterExpanded
                                                        }
                                                    }
                                                    UpdatedPackageRecycler(
                                                        productsList = mainState.updatedPackages,
                                                        onClick = { item ->
                                                            scope.launch {
                                                                paneNavigator.navigateTo(
                                                                    ListDetailPaneScaffoldRole.Detail,
                                                                    item.packageName
                                                                )
                                                            }
                                                        }
                                                    )
                                                }
                                            },
                                            collapsedView = {
                                                val text = pluralStringResource(
                                                    id = R.plurals.updated_apps,
                                                    count = mainState.updatedPackages.size,
                                                    mainState.updatedPackages.size
                                                )
                                                ExtendedFloatingActionButton(
                                                    text = { Text(text = text) },
                                                    icon = {
                                                        Icon(
                                                            imageVector = if (updaterExpanded) Phosphor.CaretDown else Phosphor.CircleWavyWarning,
                                                            contentDescription = text
                                                        )
                                                    },
                                                    elevation = FloatingActionButtonDefaults.elevation(
                                                        0.dp
                                                    ),
                                                    onClick = { updaterExpanded = !updaterExpanded }
                                                )
                                            }
                                        )
                                    }
                                    if (!(updaterVisible && updaterExpanded) &&
                                        (mainState.selection.isNotEmpty() || menuButtonAlwaysVisible)
                                    ) {
                                        ExtendedFloatingActionButton(
                                            text = { Text(text = mainState.selection.size.toString()) },
                                            icon = {
                                                Icon(
                                                    imageVector = Phosphor.List,
                                                    contentDescription = stringResource(id = R.string.context_menu)
                                                )
                                            },
                                            onClick = {
                                                menuExpanded.value = true
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    ) {
                        HomePackageRecycler(
                            modifier = Modifier.fillMaxSize(),
                            productsList = mainState.filteredPackages,
                            selection = mainState.selection,
                            onLongClick = { item ->
                                if (mainState.selection.contains(item.packageName)) {
                                    menuPackage = item
                                    menuExpanded.value = true
                                } else {
                                    viewModel.toggleSelection(item.packageName)
                                }
                            },
                            onClick = { item ->
                                if (mainState.filteredPackages.none {
                                        mainState.selection.contains(
                                            it.packageName
                                        )
                                    }) {
                                    scope.launch {
                                        paneNavigator.navigateTo(
                                            ListDetailPaneScaffoldRole.Detail,
                                            item.packageName
                                        )
                                    }
                                } else {
                                    viewModel.toggleSelection(item.packageName)
                                }
                            },
                        )

                        if (menuExpanded.value) {
                            Box(
                                modifier = Modifier     // necessary to move the menu on the whole screen
                                    .fillMaxSize()
                                    .wrapContentSize(Alignment.TopStart)
                            ) {
                                MainPackageContextMenu(
                                    expanded = menuExpanded,
                                    packageItem = menuPackage,
                                    productsList = mainState.filteredPackages,
                                    selection = mainState.selection,
                                    toggleSelection = viewModel::toggleSelection,
                                    openSheet = { item ->
                                        scope.launch {
                                            paneNavigator.navigateTo(
                                                ListDetailPaneScaffoldRole.Detail,
                                                item.packageName
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        detailPane = {
            appSheetPN.value = paneNavigator.currentDestination
                ?.takeIf { it.pane == this.paneRole }?.contentKey?.toString()

            appSheetPN.value?.let { packageName ->
                AnimatedPane {
                    AppPage(
                        packageName = packageName,
                        onDismiss = onDismiss,
                    )
                }
            }
        }
    )

    if (openBlocklist.value) BaseDialog(onDismiss = { openBlocklist.value = false }) {
        GlobalBlockListDialogUI(
            currentBlocklist = mainState.blocklist,
            openDialogCustom = openBlocklist,
        ) { newSet ->
            viewModel.updateBlocklist(newSet)
        }
    }
    if (openBatchDialog.value) BaseDialog(onDismiss = { openBatchDialog.value = false }) {
        val selectedList = mainState.updatedPackages
            .map { it.packageInfo }
            .toCollection(ArrayList())
        val selectedApk = mutableMapOf<String, Int>()
        val selectedData = mutableMapOf<String, Int>()
        val selectedListModes = mainState.updatedPackages
            .map {
                altModeToMode(
                    it.latestBackup?.let { bp ->
                        when {
                            bp.hasApk && bp.hasAppData -> {
                                selectedApk[bp.packageName] = 1
                                selectedData[bp.packageName] = 1
                                ALT_MODE_BOTH
                            }

                            bp.hasApk                  -> {
                                selectedApk[bp.packageName] = 1
                                ALT_MODE_APK
                            }

                            bp.hasAppData              -> {
                                selectedData[bp.packageName] = 1
                                ALT_MODE_DATA
                            }

                            else                       -> ALT_MODE_UNSET
                        }
                    } ?: ALT_MODE_BOTH  // no backup -> try all
                    , true
                )
            }
            .toCollection(ArrayList())

        BatchActionDialogUI(
            backupBoolean = true,
            selectedPackageInfos = selectedList,
            selectedApk = selectedApk,
            selectedData = selectedData,
            onDismiss = { openBatchDialog.value = false },
        ) {
            mActivity.startBatchAction(
                true,
                selectedPackageNames = selectedList.map { it.packageName },
                selectedModes = selectedListModes,
            )
        }
    }
}
