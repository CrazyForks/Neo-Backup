package com.machiav3lli.backup.ui.compose.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.R
import com.machiav3lli.backup.data.dbs.entity.Schedule
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ClockCounterClockwise
import com.machiav3lli.backup.ui.compose.icons.phosphor.TrashSimple
import java.time.LocalTime

@Composable
fun ExportedScheduleItem(
    item: Schedule,
    onRestore: (Schedule) -> Unit = { },
    onDelete: () -> Unit = { },
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
        headlineContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = item.name,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium
                )
                ScheduleFilters(item = item)
            }
        },
        supportingContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    var setTime = LocalTime.of(item.timeHour, item.timeMinute).toString()
                    setTime += " ${
                        LocalContext.current.resources
                            .getQuantityString(
                                R.plurals.sched_interval_formal,
                                item.interval,
                                item.interval
                            )
                    }"
                    Text(
                        text = setTime,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    ScheduleTypes(item = item)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledRoundButton(
                        description = stringResource(id = R.string.delete),
                        icon = Phosphor.TrashSimple,
                        tint = MaterialTheme.colorScheme.tertiaryContainer,
                        onTint = MaterialTheme.colorScheme.onTertiaryContainer,
                        onClick = onDelete
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    ActionButton(
                        text = stringResource(id = R.string.dialog_import),
                        icon = Phosphor.ClockCounterClockwise,
                        positive = true,
                        onClick = { onRestore(item) }
                    )
                }
            }
        },
    )
}

@Preview
@Composable
fun PreviewExportedScheduleItem() {
    ExportedScheduleItem(Schedule())
}