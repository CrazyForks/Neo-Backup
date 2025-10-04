package com.machiav3lli.backup.ui.pages

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.R
import com.machiav3lli.backup.ui.activities.NeoActivity
import com.machiav3lli.backup.ui.compose.component.ActionButton
import com.machiav3lli.backup.ui.compose.component.DevTools
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArrowsClockwise
import com.machiav3lli.backup.ui.compose.icons.phosphor.GearSix
import com.machiav3lli.backup.ui.compose.icons.phosphor.LockOpen
import com.machiav3lli.backup.ui.compose.icons.phosphor.Warning
import com.machiav3lli.backup.ui.dialogs.BaseDialog
import com.machiav3lli.backup.utils.SystemUtils
import com.machiav3lli.backup.utils.SystemUtils.applicationIssuer
import com.machiav3lli.backup.utils.restartApp
import kotlin.system.exitProcess

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SplashPage() {
    val context = LocalContext.current
    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(2f))
            Image(
                modifier = Modifier
                    .fillMaxSize(0.7f),
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(id = R.string.app_name)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = listOf(
                    SystemUtils.packageName,
                    SystemUtils.versionName,
                    context.applicationIssuer?.let { "signed by $it" } ?: "",
                ).joinToString("\n"),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RootMissing(activity: Activity? = null) {
    val context = LocalContext.current

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        val showDevTools = remember { mutableStateOf(false) }

        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(50.dp)
        ) {
            Text(
                text = stringResource(R.string.root_missing),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.root_is_mandatory),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.see_faq),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.weight(1f))
            ActionButton(
                text = stringResource(id = R.string.dialogOK),
                icon = Phosphor.Warning,
                fullWidth = true,
                modifier = Modifier
            ) {
                activity?.finishAffinity()
                exitProcess(0)
            }
            Spacer(modifier = Modifier.weight(1f))
            ActionButton(
                text = stringResource(id = R.string.prefs_title),
                icon = Phosphor.GearSix,
                fullWidth = true,
                modifier = Modifier
            ) {
                showDevTools.value = true
            }
            Spacer(modifier = Modifier.weight(1f))
            ActionButton(
                text = "Retry",
                icon = Phosphor.ArrowsClockwise,
                fullWidth = true,
                modifier = Modifier
            ) {
                context.restartApp()
            }
            if (showDevTools.value) {
                BaseDialog(onDismiss = { showDevTools.value = false }) {
                    DevTools(
                        expanded = showDevTools,
                        goto = "devsett",
                        search = "suCommand"
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LockPage() {
    val main = LocalActivity.current as NeoActivity

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        bottomBar = {
            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.Center
            ) {
                ActionButton(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    text = stringResource(id = R.string.dialog_unlock),
                    icon = Phosphor.LockOpen,
                ) {
                    main.resumeMain()
                }
            }
        }
    ) {
        BackHandler {
            main.finishAffinity()
        }
        Box(modifier = Modifier.fillMaxSize()) {}
    }
}

@Preview
@Composable
private fun SplashPreview() {
    SplashPage()
}

@Preview
@Composable
private fun NoRootPreview() {
    RootMissing()
}
