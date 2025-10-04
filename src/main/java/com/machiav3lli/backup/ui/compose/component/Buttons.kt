package com.machiav3lli.backup.ui.compose.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ICON_SIZE_SMALL
import com.machiav3lli.backup.NeoApp
import com.machiav3lli.backup.R
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArrowsClockwise
import com.machiav3lli.backup.ui.pages.pref_busyIconScale
import com.machiav3lli.backup.ui.pages.pref_busyIconTurnTime
import kotlin.math.max

@Composable
fun ActionButton(
    text: String,
    modifier: Modifier = Modifier,
    positive: Boolean = true,
    iconOnSide: Boolean = false,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    TextButton(
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (positive) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.tertiary
        ),
        onClick = onClick
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 4.dp),
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall
        )
        if (icon != null) {
            if (iconOnSide) Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = icon,
                contentDescription = text
            )
        }
    }
}

@Composable
fun ElevatedActionButton(
    text: String,
    icon: ImageVector?,
    modifier: Modifier = Modifier,
    positive: Boolean = true,
    fullWidth: Boolean = false,
    enabled: Boolean = true,
    colored: Boolean = true,
    withText: Boolean = text.isNotEmpty(),
    onClick: () -> Unit,
) {
    ElevatedButton(
        modifier = modifier,
        colors = ButtonDefaults.elevatedButtonColors(
            contentColor = when {
                !colored -> MaterialTheme.colorScheme.onSurface
                positive -> MaterialTheme.colorScheme.onPrimary
                else     -> MaterialTheme.colorScheme.onTertiary
            },
            containerColor = when {
                !colored -> MaterialTheme.colorScheme.surfaceContainerHighest
                positive -> MaterialTheme.colorScheme.primary
                else     -> MaterialTheme.colorScheme.tertiary
            }
        ),
        enabled = enabled,
        onClick = onClick
    ) {
        icon?.let {
            Icon(
                imageVector = icon,
                contentDescription = text
            )
        }
        if (withText)
            Text(
                modifier = when {
                    fullWidth -> Modifier.weight(1f)
                    else      -> Modifier.padding(start = 8.dp)
                },
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall
            )
    }
}

@Composable
fun CardButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    description: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val showTooltip = remember { mutableStateOf(false) }

    ListItem(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showTooltip.value = true },
                enabled = enabled,
            ),
        colors = ListItemDefaults.colors(
            leadingIconColor = contentColor,
            headlineColor = contentColor,
            containerColor = containerColor,
        ),
        leadingContent = {
            Icon(imageVector = icon, contentDescription = description)
        },
        headlineContent = {
            Text(
                text = description,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.titleSmall
            )

            if (showTooltip.value) {
                Tooltip(description, showTooltip)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconTextButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    containerColor: Color = MaterialTheme.colorScheme.onSurface,
    description: String,
    enabled: Boolean = true,
    aspectRatio: Float = 2f,
    onClick: () -> Unit,
) {
    TooltipBox(
        positionProvider =
            TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(description) } },
        state = rememberTooltipState(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconButton(
                modifier = modifier
                    .aspectRatio(aspectRatio),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                ),
                shape = MaterialTheme.shapes.extraLarge,
                enabled = enabled,
                onClick = onClick
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = description
                )
            }
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = description,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@Composable
fun RoundButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    description: String = "",
    tint: Color = MaterialTheme.colorScheme.onSurface,
    filled: Boolean = false,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier,
        colors = if (filled) IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) else IconButtonDefaults.iconButtonColors(),
        shape = MaterialTheme.shapes.medium,
        onClick = onClick
    ) {
        Icon(
            imageVector = icon,
            tint = if (filled) LocalContentColor.current
            else tint,
            contentDescription = description
        )
    }
}

@Composable
fun FilledRoundButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = ICON_SIZE_SMALL,
    tint: Color = MaterialTheme.colorScheme.primary,
    onTint: Color = MaterialTheme.colorScheme.onPrimary,
    description: String = "",
    onClick: () -> Unit,
) {
    FilledTonalIconButton(
        modifier = modifier,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = tint,
            contentColor = onTint,
        ),
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier.size(size),
            imageVector = icon,
            contentDescription = description
        )
    }
}

@Composable
fun RefreshButton(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    hideIfNotBusy: Boolean = false,
    onClick: () -> Unit = {},
) {
    val isBusy by remember { NeoApp.busy }

    if (hideIfNotBusy && isBusy.not())
        return

    val scale by animateFloatAsState(
        if (isBusy) 0.01f * pref_busyIconScale.value
        else 1f, label = "iconScale"
    )
    val angle by animateFloatAsState(
        if (isBusy) {
            val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")

            val animationProgress by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = pref_busyIconTurnTime.value,
                        easing = LinearEasing
                    )
                ), label = "animationProgress"
            )
            val angle = 360f * animationProgress
            angle
        } else 0f, label = "iconAngle"
    )

    RoundButton(
        description = stringResource(id = R.string.refresh),
        icon = Phosphor.ArrowsClockwise,
        tint = if (isBusy) Color.Red else tint,
        modifier = modifier
            .scale(scale)
            .rotate(angle),
        onClick = onClick
    )
}


@Preview
@Composable
fun RefreshButtonPreview() {
    val level by remember { NeoApp.busyLevel }
    val factor = 1.0 / max(1, level)

    Column {
        Text("factor: $factor")
        Text("level: $level")
        Text("time: ${(pref_busyIconTurnTime.value * factor).toInt()}")
        Row {
            RefreshButton()
            ActionButton(text = "hit") {
                NeoApp.hitBusy()
            }
            ActionButton(text = "begin") {
                NeoApp.beginBusy()
            }
            ActionButton(text = "end") {
                NeoApp.endBusy()
            }
        }
    }
}