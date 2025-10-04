package com.machiav3lli.backup.ui.compose.component

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.data.entity.ChipItem
import com.machiav3lli.backup.data.entity.InfoChipItem
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.CheckCircle
import com.machiav3lli.backup.ui.compose.icons.phosphor.Circle
import com.machiav3lli.backup.ui.compose.ifThen
import com.machiav3lli.backup.utils.launchView

private enum class SelectionState { Unselected, Selected }

class SelectableChipTransition constructor(
    cornerRadius: State<Dp>,
) {
    val cornerRadius by cornerRadius
}

@Composable
fun selectableChipTransition(selected: Boolean): SelectableChipTransition {
    val transition = updateTransition(
        targetState = if (selected) SelectionState.Selected else SelectionState.Unselected,
        label = "chip_transition"
    )
    val corerRadius = transition.animateDp(label = "chip_corner") { state ->
        when (state) {
            SelectionState.Unselected -> 8.dp
            SelectionState.Selected   -> 16.dp
        }
    }
    return remember(transition) {
        SelectableChipTransition(corerRadius)
    }
}

@Composable
fun SelectionChip(
    item: ChipItem,
    isSelected: Boolean,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        labelColor = MaterialTheme.colorScheme.onSurface,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        iconColor = MaterialTheme.colorScheme.onSurface,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ),
    onClick: () -> Unit,
) {
    val selectionCornerRadius by animateDpAsState(
        when {
            isSelected -> 4.dp
            else       -> 16.dp
        }
    )

    FilterChip(
        colors = colors,
        shape = RoundedCornerShape(selectionCornerRadius),
        border = null,
        selected = isSelected,
        leadingIcon = {
            ButtonIcon(item.icon, item.textId)
        },
        onClick = onClick,
        label = {
            Text(text = stringResource(id = item.textId))
        }
    )
}

@Composable
fun SelectionChip(
    label: String,
    isSelected: Boolean,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        labelColor = MaterialTheme.colorScheme.onSurface,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        iconColor = MaterialTheme.colorScheme.onSurface,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ),
    onClick: () -> Unit,
) {
    val selectionCornerRadius by animateDpAsState(
        when {
            isSelected -> 4.dp
            else       -> 16.dp
        }
    )

    FilterChip(
        colors = colors,
        shape = RoundedCornerShape(selectionCornerRadius),
        border = null,
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(text = label)
        }
    )
}

@Composable
fun InfoChip(
    item: InfoChipItem,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        labelColor = MaterialTheme.colorScheme.onSurface,
        iconColor = MaterialTheme.colorScheme.onSurface,
        selectedContainerColor = item.color ?: MaterialTheme.colorScheme.onPrimaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        selectedLeadingIconColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ),
) {
    FilterChip(
        leadingIcon = {
            if (item.icon != null) Icon(
                imageVector = item.icon,
                contentDescription = item.text,
            )
        },
        border = null,
        selected = item.color != null,
        label = {
            Text(text = item.text)
        },
        colors = colors,
        onClick = {}
    )
}

@Composable
fun StateChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    color: Color,
    index: Int,
    count: Int,
    checked: Boolean,
    onClick: () -> Unit,
) {
    val openPopup = remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(
        topStart = if (index == 0) MaterialTheme.shapes.extraLarge.topStart
        else MaterialTheme.shapes.extraSmall.topStart,
        bottomStart = if (index == 0) MaterialTheme.shapes.extraLarge.topStart
        else MaterialTheme.shapes.extraSmall.topStart,
        topEnd = if (index == count - 1) MaterialTheme.shapes.extraLarge.topStart
        else MaterialTheme.shapes.extraSmall.topStart,
        bottomEnd = if (index == count - 1) MaterialTheme.shapes.extraLarge.topStart
        else MaterialTheme.shapes.extraSmall.topStart,
    )

    Surface(
        modifier = modifier
            .defaultMinSize(minWidth = 56.dp, minHeight = 1.dp)
            .clip(shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { openPopup.value = true }
            ),
        contentColor = if (checked) MaterialTheme.colorScheme.surfaceContainerLowest else color,
        color = if (checked) color else Color.Transparent,
        shape = shape,
        border = BorderStroke(1.dp, color),
    ) {
        Icon(
            modifier = Modifier.padding(8.dp),
            imageVector = icon,
            contentDescription = text,
        )

        if (openPopup.value) {
            Tooltip(text, openPopup)
        }
    }
}

@Composable
fun CheckChip(
    modifier: Modifier = Modifier,
    checked: Boolean,
    textId: Int,
    checkedTextId: Int,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(
        labelColor = MaterialTheme.colorScheme.onBackground,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        iconColor = MaterialTheme.colorScheme.onBackground,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
        containerColor = Color.Transparent,
        selectedContainerColor = MaterialTheme.colorScheme.primary
    ),
    onCheckedChange: (Boolean) -> Unit,
) {
    val (checked, check) = remember(checked) { mutableStateOf(checked) }   //TODO hg42 should probably be removed like for MultiChips
    val selectionCornerRadius by animateDpAsState(
        when {
            checked -> 4.dp
            else    -> 28.dp
        }
    )

    FilterChip(
        modifier = modifier,
        selected = checked,
        colors = colors,
        shape = RoundedCornerShape(selectionCornerRadius),
        leadingIcon = {
            Icon(
                imageVector = when {
                    checked -> Phosphor.CheckCircle
                    else    -> Phosphor.Circle
                },
                contentDescription = null,
            )
        },
        onClick = {
            onCheckedChange(!checked)
            check(!checked)
        },
        label = {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = stringResource(id = if (checked) checkedTextId else textId),
            )
        }
    )
}

@Composable
fun ActionChip(
    modifier: Modifier = Modifier,
    text: String = "",
    icon: ImageVector? = null,
    positive: Boolean,
    fullWidth: Boolean = false,
    onClick: () -> Unit = {},
) {
    AssistChip(
        modifier = modifier,
        label = {
            Text(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .ifThen(fullWidth) {
                        fillMaxWidth()
                    },
                text = text,
                textAlign = TextAlign.Center,
            )
        },
        leadingIcon = {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = text
                )
            }
        },
        shape = MaterialTheme.shapes.large,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (positive) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.tertiaryContainer,
            labelColor = if (positive) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onTertiaryContainer,
            leadingIconContentColor = if (positive) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onTertiaryContainer,
        ),
        border = null,
        onClick = onClick
    )
}

@Composable
fun LinkChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    url: String,
) {
    val context = LocalContext.current

    AssistChip(
        modifier = modifier,
        border = null,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        onClick = {
            context.launchView(url)
        }
    )
}

@Composable
fun SwitchChip(
    firstTextId: Int,
    firstIcon: ImageVector,
    secondTextId: Int,
    secondIcon: ImageVector,
    firstSelected: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = SegmentedButtonDefaults.colors(
        inactiveContainerColor = Color.Transparent,
        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
        inactiveContentColor = MaterialTheme.colorScheme.onSurface,
        activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        activeBorderColor = Color.Transparent,
        inactiveBorderColor = Color.Transparent,
    )
    var firstSelected by remember { mutableStateOf(firstSelected) }

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.medium)
            .padding(horizontal = 6.dp)
            .fillMaxWidth(),
    ) {
        SegmentedButton(
            modifier = Modifier.weight(1f),
            selected = firstSelected,
            shape = MaterialTheme.shapes.small,
            colors = colors,
            icon = {
                ButtonIcon(firstIcon, firstTextId)
            },
            onClick = {
                onCheckedChange(true)
                firstSelected = true
            }
        ) {
            Text(text = stringResource(id = firstTextId))
        }
        SegmentedButton(
            modifier = Modifier.weight(1f),
            selected = !firstSelected,
            shape = MaterialTheme.shapes.small,
            colors = colors,
            icon = {
                ButtonIcon(secondIcon, secondTextId)
            },
            onClick = {
                onCheckedChange(false)
                firstSelected = false
            }
        ) {
            Text(text = stringResource(id = secondTextId))
        }
    }
}