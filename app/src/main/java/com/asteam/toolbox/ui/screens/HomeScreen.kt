package com.asteam.toolbox.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.asteam.toolbox.data.ToolCategory
import com.asteam.toolbox.data.ToolItem

@Composable
fun HomeScreen(
    tools: List<ToolItem>,
    favorites: Set<String>,
    customCollection: Set<String> = emptySet(),
    recentTools: List<String> = emptyList(),
    layoutMode: String = "grid",
    sortMode: String = "catalog",
    cardSize: String = "normal",
    onOpenTool: (ToolItem) -> Unit,
    onToggleFavorite: (ToolItem) -> Unit,
    onToggleCustom: (ToolItem) -> Unit = {},
    onHideTool: (ToolItem) -> Unit = {},
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ToolCategory?>(null) }

    val filtered = tools.filter { tool ->
        val categoryMatches = selectedCategory == null || tool.category == selectedCategory
        val queryMatches = query.isBlank() || tool.title.contains(query, true) || tool.subtitle.contains(query, true)
        categoryMatches && queryMatches
    }
    val recentIndex = remember(recentTools) { recentTools.withIndex().associate { it.value to it.index } }
    val visibleTools = when (sortMode) {
        "title" -> filtered.sortedBy { it.title }
        "recent" -> filtered.sortedWith(compareBy<ToolItem> { recentIndex[it.id] ?: Int.MAX_VALUE }.thenBy { it.title })
        else -> filtered
    }
    val minCardWidth = when (cardSize) {
        "compact" -> 132.dp
        "large" -> 190.dp
        else -> 156.dp
    }
    val cardPadding = when (cardSize) {
        "compact" -> 10.dp
        "large" -> 18.dp
        else -> 14.dp
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("جستجوی ابزار") },
            placeholder = { Text("مثلاً درصد، قطب‌نما، QR...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = selectedCategory == null, onClick = { selectedCategory = null }, label = { Text("همه") }) }
            items(ToolCategory.entries.size) { index ->
                val category = ToolCategory.entries[index]
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category.title) },
                )
            }
        }

        Text(
            text = "${visibleTools.size} ابزار",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LazyVerticalGrid(
            columns = if (layoutMode == "list") GridCells.Fixed(1) else GridCells.Adaptive(minSize = minCardWidth),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(visibleTools, key = { it.id }) { tool ->
                ToolCard(
                    tool = tool,
                    favorite = tool.id in favorites,
                    inCustomCollection = tool.id in customCollection,
                    padding = cardPadding,
                    onClick = { onOpenTool(tool) },
                    onToggleFavorite = { onToggleFavorite(tool) },
                    onToggleCustom = { onToggleCustom(tool) },
                    onHide = { onHideTool(tool) },
                )
            }
        }
    }
}

@Composable
private fun ToolCard(
    tool: ToolItem,
    favorite: Boolean,
    inCustomCollection: Boolean,
    padding: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleCustom: () -> Unit,
    onHide: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tool.symbol,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onToggleCustom) {
                    Icon(
                        imageVector = if (inCustomCollection) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (inCustomCollection) "حذف از مجموعه من" else "افزودن به مجموعه من",
                    )
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (favorite) "حذف از علاقه‌مندی" else "افزودن به علاقه‌مندی",
                    )
                }
                IconButton(onClick = onHide) {
                    Icon(Icons.Outlined.VisibilityOff, contentDescription = "مخفی کردن ابزار")
                }
            }
            Text(tool.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(tool.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
