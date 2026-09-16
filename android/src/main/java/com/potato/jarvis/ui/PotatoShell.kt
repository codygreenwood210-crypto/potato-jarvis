package com.potato.jarvis.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.potato.jarvis.Screen

private data class PrimaryDestination(
    val screen: Screen?,
    val symbol: String,
    val label: String,
)

private val primaryDestinations = listOf(
    PrimaryDestination(Screen.CHAT, "●", "Chat"),
    PrimaryDestination(Screen.MEMORY, "◉", "Memory"),
    PrimaryDestination(Screen.TASKS, "✓", "Tasks"),
    PrimaryDestination(Screen.NOTIFICATIONS, "◇", "Alerts"),
    PrimaryDestination(null, "•••", "More"),
)

private val moreDestinations = listOf(
    Screen.PROACTIVE to "Proactive",
    Screen.DEVICES to "Device",
    Screen.SMART_HOME to "Smart Home",
    Screen.TOOLS to "Tools & Web",
    Screen.SECURITY to "Security",
    Screen.SETTINGS to "Settings",
)

@Composable
fun PotatoAppShell(
    screen: Screen,
    online: Boolean,
    onScreenSelected: (Screen) -> Unit,
    content: @Composable () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val expanded = configuration.screenWidthDp >= 720

    PotatoBackdrop(Modifier.fillMaxSize()) {
        if (expanded) {
            Row(Modifier.fillMaxSize()) {
                PotatoNavigationRail(
                    screen = screen,
                    onScreenSelected = onScreenSelected,
                    modifier = Modifier.width(132.dp).fillMaxHeight(),
                )
                Column(Modifier.fillMaxSize()) {
                    PotatoTopBar(online = online, compact = false, onScreenSelected = onScreenSelected)
                    Box(Modifier.fillMaxSize()) { content() }
                }
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                PotatoTopBar(online = online, compact = true, onScreenSelected = onScreenSelected)
                Box(Modifier.weight(1f).fillMaxWidth()) { content() }
                PotatoBottomBar(screen = screen, onScreenSelected = onScreenSelected)
            }
        }
    }
}

@Composable
private fun PotatoTopBar(online: Boolean, compact: Boolean, onScreenSelected: (Screen) -> Unit) {
    Surface(color = PotatoVisuals.Black.copy(alpha = 0.72f), tonalElevation = 0.dp) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = if (compact) 14.dp else 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PotatoBrand(online = online, compact = compact)
            Spacer(Modifier.weight(1f))
            if (!compact) {
                Text(
                    "Good ideas happen here  ·  small steps, bigger things",
                    color = PotatoVisuals.TextSecondary,
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    "⌕",
                    color = PotatoVisuals.TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .clip(RoundedCornerShape(13.dp))
                        .background(PotatoVisuals.SurfaceRaised)
                        .clickable { onScreenSelected(Screen.TOOLS) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    "⚙",
                    color = PotatoVisuals.TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .clip(RoundedCornerShape(13.dp))
                        .background(PotatoVisuals.SurfaceRaised)
                        .clickable { onScreenSelected(Screen.SETTINGS) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun PotatoNavigationRail(
    screen: Screen,
    onScreenSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    var moreOpen by remember { mutableStateOf(false) }
    NavigationRail(
        modifier = modifier.background(PotatoVisuals.Black.copy(alpha = 0.92f)).padding(vertical = 18.dp),
        containerColor = PotatoVisuals.Black.copy(alpha = 0.92f),
        header = {
            Spacer(Modifier.height(8.dp))
        },
    ) {
        primaryDestinations.forEach { destination ->
            val selected = destination.screen == screen || (destination.screen == null && moreDestinations.any { it.first == screen })
            Box {
                NavigationRailItem(
                    selected = selected,
                    onClick = {
                        if (destination.screen != null) onScreenSelected(destination.screen)
                        else moreOpen = true
                    },
                    icon = {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) PotatoVisuals.Brand.copy(alpha = 0.17f) else androidx.compose.ui.graphics.Color.Transparent)
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Text(
                                destination.symbol,
                                color = if (selected) PotatoVisuals.BrandBright else PotatoVisuals.TextSecondary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    },
                    label = {
                        Text(
                            destination.label,
                            color = if (selected) PotatoVisuals.BrandBright else PotatoVisuals.TextSecondary,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                )
                if (destination.screen == null) {
                    DropdownMenu(expanded = moreOpen, onDismissRequest = { moreOpen = false }) {
                        moreDestinations.forEach { (target, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    moreOpen = false
                                    onScreenSelected(target)
                                },
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Text("🌱", color = PotatoVisuals.BrandBright, style = MaterialTheme.typography.titleMedium)
        Text("Small", color = PotatoVisuals.TextSecondary, style = MaterialTheme.typography.labelMedium)
        Text("Steps", color = PotatoVisuals.TextSecondary, style = MaterialTheme.typography.labelMedium)
        Text("Bigger", color = PotatoVisuals.TextSecondary, style = MaterialTheme.typography.labelMedium)
        Text("Things", color = PotatoVisuals.TextSecondary, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(14.dp))
        Text("POTATO", color = PotatoVisuals.BrandBright, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text("V5.8 UI", color = PotatoVisuals.TextSecondary, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun PotatoBottomBar(screen: Screen, onScreenSelected: (Screen) -> Unit) {
    var moreOpen by remember { mutableStateOf(false) }
    Surface(color = PotatoVisuals.Black.copy(alpha = 0.96f)) {
        Box {
            NavigationBar(containerColor = PotatoVisuals.Black.copy(alpha = 0.96f)) {
                primaryDestinations.forEach { destination ->
                    val selected = destination.screen == screen || (destination.screen == null && moreDestinations.any { it.first == screen })
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (destination.screen != null) onScreenSelected(destination.screen)
                            else moreOpen = true
                        },
                        icon = { Text(destination.symbol, fontWeight = FontWeight.Bold) },
                        label = { Text(destination.label) },
                    )
                }
            }
            DropdownMenu(
                expanded = moreOpen,
                onDismissRequest = { moreOpen = false },
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                moreDestinations.forEach { (target, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            moreOpen = false
                            onScreenSelected(target)
                        },
                    )
                }
            }
        }
    }
}
