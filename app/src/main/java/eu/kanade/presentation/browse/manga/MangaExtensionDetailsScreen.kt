package eu.kanade.presentation.browse.manga

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.DisplayMetrics
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.kanade.domain.extension.manga.interactor.MangaExtensionSourceItem
import eu.kanade.presentation.browse.manga.components.MangaExtensionIcon
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.components.WarningBanner
import eu.kanade.presentation.more.settings.widget.TextPreferenceWidget
import eu.kanade.presentation.more.settings.widget.TrailingWidgetBuffer
import eu.kanade.tachiyomi.extension.manga.model.MangaExtension
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.ui.browse.manga.extension.details.MangaExtensionDetailsScreenModel
import eu.kanade.tachiyomi.util.system.LocaleHelper
import kotlinx.collections.immutable.persistentListOf
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen

@Composable
fun ExtensionDetailsScreen(
    navigateUp: () -> Unit,
    state: MangaExtensionDetailsScreenModel.State,
    onClickSourcePreferences: (sourceId: Long) -> Unit,
    onClickWhatsNew: () -> Unit,
    onClickReadme: () -> Unit,
    onClickEnableAll: () -> Unit,
    onClickDisableAll: () -> Unit,
    onClickClearCookies: () -> Unit,
    onClickUninstall: () -> Unit,
    onClickSource: (sourceId: Long) -> Unit,
) {
    Scaffold(
        topBar = { scrollBehavior ->
            AppBar(
                title = stringResource(MR.strings.label_extension_info),
                navigateUp = navigateUp,
                actions = {
                    AppBarActions(
                        actions = persistentListOf<AppBar.AppBarAction>().builder()
                            .apply {
                                if (state.extension?.isUnofficial == false) {
                                    add(
                                        AppBar.Action(
                                            title = stringResource(MR.strings.whats_new),
                                            icon = Icons.Outlined.History,
                                            onClick = onClickWhatsNew,
                                        ),
                                    )
                                    add(
                                        AppBar.Action(
                                            title = stringResource(MR.strings.action_faq_and_guides),
                                            icon = Icons.AutoMirrored.Outlined.HelpOutline,
                                            onClick = onClickReadme,
                                        ),
                                    )
                                }
                                addAll(
                                    listOf(
                                        AppBar.OverflowAction(
                                            title = stringResource(MR.strings.action_enable_all),
                                            onClick = onClickEnableAll,
                                        ),
                                        AppBar.OverflowAction(
                                            title = stringResource(MR.strings.action_disable_all),
                                            onClick = onClickDisableAll,
                                        ),
                                        AppBar.OverflowAction(
                                            title = stringResource(MR.strings.pref_clear_cookies),
                                            onClick = onClickClearCookies,
                                        ),
                                    ),
                                )
                            }
                            .build(),
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        if (state.extension == null) {
            EmptyScreen(
                stringRes = MR.strings.empty_screen,
                modifier = Modifier.padding(paddingValues),
            )
            return@Scaffold
        }

        ExtensionDetails(
            contentPadding = paddingValues,
            extension = state.extension,
            sources = state.sources,
            onClickSourcePreferences = onClickSourcePreferences,
            onClickUninstall = onClickUninstall,
            onClickSource = onClickSource,
        )
    }
}

@Composable
private fun ExtensionDetails(
    contentPadding: PaddingValues,
    extension: MangaExtension.Installed,
    sources: List<MangaExtensionSourceItem>,
    onClickSourcePreferences: (sourceId: Long) -> Unit,
    onClickUninstall: () -> Unit,
    onClickSource: (sourceId: Long) -> Unit,
) {
    val context = LocalContext.current
    var showNsfwWarning by remember { mutableStateOf(false) }

    ScrollbarLazyColumn(
        contentPadding = contentPadding,
    ) {
        when {
            extension.isUnofficial ->
                item {
                    WarningBanner(MR.strings.unofficial_extension_message)
                }
            extension.isObsolete ->
                item {
                    WarningBanner(MR.strings.obsolete_extension_message)
                }
        }

        item {
            DetailsHeader(
                extension = extension,
                onClickUninstall = onClickUninstall,
                onClickAppInfo = {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", extension.pkgName, null)
                        context.startActivity(this)
                    }
                    Unit
                }.takeIf { extension.isShared },
                onClickAgeRating = {
                    showNsfwWarning = true
                },
            )
        }

        items(
            items = sources,
            key = { it.source.id },
        ) { source ->
            SourceSwitchPreference(
                modifier = Modifier.animateItemPlacement(),
                source = source,
                onClickSourcePreferences = onClickSourcePreferences,
                onClickSource = onClickSource,
            )
        }
    }
    if (showNsfwWarning) {
        NsfwWarningDialog(
            onClickConfirm = {
                showNsfwWarning = false
            },
        )
    }
}

@Composable
private fun DetailsHeader(
    extension: MangaExtension,
    onClickAgeRating: () -> Unit,
    onClickUninstall: () -> Unit,
    onClickAppInfo: (() -> Unit)?,
) {
    val context = LocalContext.current

    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MaterialTheme.padding.medium,
                    end = MaterialTheme.padding.medium,
                    top = MaterialTheme.padding.medium,
                    bottom = MaterialTheme.padding.small,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MangaExtensionIcon(
                modifier = Modifier
                    .size(112.dp),
                extension = extension,
                density = DisplayMetrics.DENSITY_XXXHIGH,
            )

            Text(
                text = extension.name,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )

            val strippedPkgName = extension.pkgName.substringAfter("eu.kanade.tachiyomi.extension.")

            Text(
                text = strippedPkgName,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.padding.extraLarge,
                    vertical = MaterialTheme.padding.small,
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InfoText(
                modifier = Modifier.weight(1f),
                primaryText = extension.versionName,
                secondaryText = stringResource(MR.strings.ext_info_version),
            )

            InfoDivider()

            InfoText(
                modifier = Modifier.weight(if (extension.isNsfw) 1.5f else 1f),
                primaryText = LocaleHelper.getSourceDisplayName(extension.lang, context),
                secondaryText = stringResource(MR.strings.ext_info_language),
            )

            if (extension.isNsfw) {
                InfoDivider()

                InfoText(
                    modifier = Modifier.weight(1f),
                    primaryText = stringResource(MR.strings.ext_nsfw_short),
                    primaryTextStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                    ),
                    secondaryText = stringResource(MR.strings.ext_info_age_rating),
                    onClick = onClickAgeRating,
                )
            }
        }

        Row(
            modifier = Modifier.padding(
                start = MaterialTheme.padding.medium,
                end = MaterialTheme.padding.medium,
                top = MaterialTheme.padding.small,
                bottom = MaterialTheme.padding.medium,
            ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onClickUninstall,
            ) {
                Text(stringResource(MR.strings.ext_uninstall))
            }

            if (onClickAppInfo != null) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onClickAppInfo,
                ) {
                    Text(
                        text = stringResource(MR.strings.ext_app_info),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }

        HorizontalDivider()
    }
}

@Composable
private fun InfoText(
    primaryText: String,
    secondaryText: String,
    modifier: Modifier = Modifier,
    primaryTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    onClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(interactionSource, indication = null) { onClick() }
    } else {
        Modifier
    }

    Column(
        modifier = modifier.then(clickableModifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = primaryText,
            textAlign = TextAlign.Center,
            style = primaryTextStyle,
        )

        Text(
            text = secondaryText + if (onClick != null) " ⓘ" else "",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun InfoDivider() {
    VerticalDivider(
        modifier = Modifier.height(20.dp),
    )
}

@Composable
private fun SourceSwitchPreference(
    source: MangaExtensionSourceItem,
    onClickSourcePreferences: (sourceId: Long) -> Unit,
    onClickSource: (sourceId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    TextPreferenceWidget(
        modifier = modifier,
        title = if (source.labelAsName) {
            source.source.toString()
        } else {
            LocaleHelper.getSourceDisplayName(source.source.lang, context)
        },
        widget = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (source.source is ConfigurableSource) {
                    IconButton(onClick = { onClickSourcePreferences(source.source.id) }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(MR.strings.label_settings),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                Switch(
                    checked = source.enabled,
                    onCheckedChange = null,
                    modifier = Modifier.padding(start = TrailingWidgetBuffer),
                )
            }
        },
        onPreferenceClick = { onClickSource(source.source.id) },
    )
}

@Composable
fun NsfwWarningDialog(
    onClickConfirm: () -> Unit,
) {
    AlertDialog(
        text = {
            Text(text = stringResource(MR.strings.ext_nsfw_warning))
        },
        confirmButton = {
            TextButton(onClick = onClickConfirm) {
                Text(text = stringResource(MR.strings.action_ok))
            }
        },
        onDismissRequest = onClickConfirm,
    )
}
