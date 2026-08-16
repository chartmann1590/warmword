package com.charles.warmwords.app.ui.screens.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Horizontal
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.charles.warmwords.app.R
import com.charles.warmwords.app.data.model.JournalEntryModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JournalScreen(
    viewModel: JournalViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }

    val filteredEntries = if (query.isBlank()) {
        uiState.entries
    } else {
        uiState.entries.filter { entry ->
            entry.content.contains(query, ignoreCase = true) ||
                entry.tags.any { it.contains(query, ignoreCase = true) }
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        JournalAppBar(
            showSearch = showSearch,
            query = query,
            onQueryChange = { query = it },
            onToggleSearch = {
                showSearch = !showSearch
                if (!showSearch) query = ""
            }
        )

        com.charles.warmwords.app.ui.components.AdBanner(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        if (uiState.entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "No journal entries yet.\nTap + to write your first one.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (filteredEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No entries match \"$query\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredEntries, key = { it.id }) { entry ->
                    JournalEntryItem(
                        entry = entry,
                        onDelete = { viewModel.deleteEntry(entry) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.End)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Add entry",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    if (showAddDialog) {
        AddJournalDialog(
            onDismiss = { showAddDialog = false },
            onSave = { moodScore, content ->
                viewModel.addEntry(moodScore, content)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun JournalAppBar(
    showSearch: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (showSearch) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search entries...") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onToggleSearch) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Text(
                text = "Journal",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onToggleSearch) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun JournalEntryItem(
    entry: JournalEntryModel,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val date = Date(entry.timestamp)
    val formatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                MoodSelector(score = entry.moodScore, readOnly = true)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = formatter.format(date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(0.7f)
                    )
                }
            }
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (entry.tags.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = entry.tags.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MoodSelector(
    score: Int,
    readOnly: Boolean = false,
    onScoreChange: (Int) -> Unit = {}
) {
    val moods = listOf(
        MoodOption(1, "\uD83D\uDE2D", "Awful"),
        MoodOption(2, "\uD83D\uDE44", "Sad"),
        MoodOption(3, "\uD83D\uDE10", "Okay"),
        MoodOption(4, "\uD83D\uDE42", "Good"),
        MoodOption(5, "\uD83D\uDE0D", "Great")
    )

    Row(
        horizontalArrangement = if (readOnly) Arrangement.Start else Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        moods.forEach { mood ->
            val selected = mood.score == score
            val tint = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f)

            if (readOnly) {
                if (selected) {
                    Text(
                        text = mood.emoji,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clickable { onScoreChange(mood.score) }
                        .size(if (selected) 32.dp else 28.dp)
                ) {
                    Text(
                        text = mood.emoji,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            if (!readOnly && mood.score < 5) {
                Spacer(Modifier.width(4.dp))
            }
        }
    }
}

private data class MoodOption(val score: Int, val emoji: String, val label: String)

@Composable
fun AddJournalDialog(
    onDismiss: () -> Unit,
    onSave: (moodScore: Int, content: String) -> Unit
) {
    var moodScore by rememberSaveable { mutableStateOf(3) }
    var content by rememberSaveable { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text("New Journal Entry") },
        text = {
            Column {
                Text("How are you feeling?", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                MoodSelector(
                    score = moodScore,
                    readOnly = false,
                    onScoreChange = { moodScore = it }
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Write your thoughts...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (content.isNotBlank()) {
                        onSave(moodScore, content)
                    }
                },
                enabled = content.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
