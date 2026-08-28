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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
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
    layoutMode: String = "grid",
    onOpenTool: (ToolItem) -> Unit,
    onToggleFavorite: (ToolItem) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ToolCategory?>(null) }

    val visibleTools = tools.filter { tool ->
        val categoryMatches = selectedCategory == null || tool.category == selectedCategory
        val queryMatches = query.isBlank() || tool.title.contains(query, true) || tool.subtitle.contains(query, true)
        categoryMatches && queryMatches
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
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("همه") },
                )
            }
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
            columns = if (layoutMode == "list") GridCells.Fixed(1) else GridCells.Adaptive(minSize = 156.dp),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(visibleTools, key = { it.id }) { tool ->
                ToolCard(
                    tool = tool,
                    favorite = tool.id in favorites,
                    onClick = { onOpenTool(tool) },
                    onToggleFavorite = { onToggleFavorite(tool) },
                )
            }
        }
    }
}

@Composable
private fun ToolCard(tool: ToolItem, favorite: Boolean, onClick: () -> Unit, onToggleFavorite: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tool.symbol,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (favorite) "حذف از علاقه‌مندی" else "افزودن به علاقه‌مندی",
                    )
                }
            }
            Text(tool.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(tool.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
