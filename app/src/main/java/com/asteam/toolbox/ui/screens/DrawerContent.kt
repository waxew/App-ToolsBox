package com.asteam.toolbox.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.asteam.toolbox.data.UserPreferences

enum class DrawerDestination { HOME, FAVORITES, SETTINGS, ABOUT, CONTACT }

/** Shared right-side drawer with local profile and app navigation. */
@Composable
fun DrawerContent(
    preferences: UserPreferences,
    active: DrawerDestination,
    onDestination: (DrawerDestination) -> Unit,
    onProfileChanged: () -> Unit,
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(preferences.userName) }
    var profileUri by remember { mutableStateOf(preferences.profileImage) }
    var showNameDialog by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            preferences.persistProfileImageUri(context, it)
            profileUri = it.toString()
            onProfileChanged()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.86f)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 24.dp),
    ) {
        // A colored profile header gives the drawer a clear visual identity without using external assets.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer,
                        ),
                    ),
                )
                .padding(horizontal = 16.dp, vertical = 18.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(94.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.75f), CircleShape)
                        .clickable { imagePicker.launch(arrayOf("image/*")) },
                    contentAlignment = Alignment.Center,
                ) {
                    val bitmap = remember(profileUri) {
                        profileUri?.let { value ->
                            runCatching {
                                context.contentResolver.openInputStream(Uri.parse(value))?.use { input ->
                                    BitmapFactory.decodeStream(input)
                                }
                            }.getOrNull()
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "تصویر پروفایل",
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "انتخاب تصویر پروفایل",
                            modifier = Modifier.size(46.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Text(
                    "برای تغییر تصویر، لمس کنید",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        name,
                        modifier = Modifier.padding(horizontal = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = { showNameDialog = true }, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "ویرایش نام", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        DrawerItem("خانه", { Icon(Icons.Default.Home, contentDescription = null) }, active == DrawerDestination.HOME) { onDestination(DrawerDestination.HOME) }
        DrawerItem("علاقه‌مندی‌ها", { Icon(Icons.Default.Favorite, contentDescription = null) }, active == DrawerDestination.FAVORITES) { onDestination(DrawerDestination.FAVORITES) }
        DrawerItem("تنظیمات", { Icon(Icons.Default.Settings, contentDescription = null) }, active == DrawerDestination.SETTINGS) { onDestination(DrawerDestination.SETTINGS) }
        Spacer(Modifier.height(8.dp)); HorizontalDivider(); Spacer(Modifier.height(8.dp))
        DrawerItem("درباره نرم‌افزار", { Icon(Icons.Default.Info, contentDescription = null) }, active == DrawerDestination.ABOUT) { onDestination(DrawerDestination.ABOUT) }
        DrawerItem("ارتباط با ما", { Icon(Icons.Default.Mail, contentDescription = null) }, active == DrawerDestination.CONTACT) { onDestination(DrawerDestination.CONTACT) }

        Spacer(Modifier.weight(1f)); HorizontalDivider(); Spacer(Modifier.height(12.dp))
        Text(
            "گروه توسعه فناوری و نرم افزاری as Team",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            "AS.Support.info@gmail.com",
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(20.dp))
    }

    if (showNameDialog) {
        var edited by remember(name) { mutableStateOf(name) }
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("نام کاربر") },
            text = { OutlinedTextField(edited, { edited = it }, singleLine = true, label = { Text("نام نمایشی") }) },
            confirmButton = {
                TextButton(onClick = {
                    if (edited.isNotBlank()) {
                        preferences.userName = edited
                        name = edited.trim()
                        onProfileChanged()
                    }
                    showNameDialog = false
                }) { Text("ذخیره") }
            },
            dismissButton = { TextButton(onClick = { showNameDialog = false }) { Text("انصراف") } },
        )
    }
}

@Composable
private fun DrawerItem(label: String, icon: @Composable () -> Unit, selected: Boolean, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
        selected = selected,
        onClick = onClick,
        icon = icon,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.padding(vertical = 2.dp),
    )
}
