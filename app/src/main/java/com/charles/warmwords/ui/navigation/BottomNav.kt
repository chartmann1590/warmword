package com.charles.warmwords.ui.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import com.charles.warmwords.ui.components.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.charles.warmwords.R
import com.charles.warmwords.ui.components.TranslatedText
import com.charles.warmwords.ui.navigation.Screen

data class BottomNavItem(
    val screen: Screen,
    val iconRes: Int,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Chat, R.drawable.ic_nav_chat, "Chat"),
    BottomNavItem(Screen.Journal, R.drawable.ic_nav_journal, "Journal"),
    BottomNavItem(Screen.Insights, R.drawable.ic_nav_insights, "Insights"),
    BottomNavItem(Screen.FindHelp, R.drawable.ic_nav_findhelp, "Find Help"),
    BottomNavItem(Screen.Settings, R.drawable.ic_nav_settings, "Settings")
)

@Composable
fun WarmWordBottomBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.label,
                        tint = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    TranslatedText(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
                    )
                }
            )
        }
    }
}
