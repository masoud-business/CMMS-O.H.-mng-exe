package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.AppTab
import com.example.data.entity.OversightEntity
import com.example.data.entity.UserEntity
import com.example.ui.theme.*

/**
 * کنترل‌های دایره‌ای شیشه‌ای شناور در گوشه‌های صفحه برای حالت افقی (Landscape Glass Floating Bubbles)
 * این کامپوننت فضای بالای صفحه و پایین صفحه را کاملاً آزاد می‌کند تا حداکثر فضای عمودی به محتوای اصلی اختصاص یابد.
 */
@Composable
fun LandscapeFloatingGlassControls(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    roleTabs: List<RoleTabConfig>,
    currentUser: UserEntity?,
    allUsers: List<UserEntity>,
    oversights: List<OversightEntity>,
    selectedOversightId: Long?,
    onSwitchUser: (UserEntity) -> Unit,
    onSelectOversight: (Long) -> Unit,
    onLogout: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isUserBubbleExpanded by remember { mutableStateOf(false) }
    var isNavBubbleExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. TOP-START FLOATING GLASS BUBBLE (دایره شیشه‌ای کاربر و تنظیمات در گوشه بالای صفحه)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 10.dp, start = 12.dp)
        ) {
            Surface(
                onClick = { isUserBubbleExpanded = !isUserBubbleExpanded },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                shadowElevation = 8.dp,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val roleColor = when (currentUser?.role) {
                        "admin" -> IndustrialNavy
                        "planner" -> IndustrialPurple
                        "hse" -> Color(0xFF10B981)
                        "unit_head" -> IndustrialEmerald
                        else -> IndustrialAmber
                    }
                    Text(
                        text = currentUser?.name?.take(1) ?: "U",
                        fontWeight = FontWeight.ExtraBold,
                        color = roleColor,
                        fontSize = 17.sp
                    )

                    // Small indicator badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(roleColor)
                            .border(1.dp, Color.White, CircleShape)
                    )
                }
            }

            // User Info & Settings Glass Popup
            if (isUserBubbleExpanded) {
                Popup(
                    alignment = Alignment.TopStart,
                    offset = androidx.compose.ui.unit.IntOffset(0, 52),
                    onDismissRequest = { isUserBubbleExpanded = false },
                    properties = PopupProperties(focusable = true)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shadowElevation = 12.dp,
                        modifier = Modifier
                            .width(320.dp)
                            .padding(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header: User Name & Role
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentUser?.name ?: "کاربر سیستم",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${currentUser?.role} • ${currentUser?.unit ?: "مدیریت اورهال"}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = { isUserBubbleExpanded = false },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "بستن", modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Switch User Dropdown
                            Text("تغییر کاربر و نقش دسترسی:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            var userMenuExpanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(
                                    onClick = { userMenuExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(currentUser?.name ?: "انتخاب کاربر", fontSize = 11.sp)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }
                                DropdownMenu(
                                    expanded = userMenuExpanded,
                                    onDismissRequest = { userMenuExpanded = false }
                                ) {
                                    allUsers.forEach { user ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(user.name, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                    Text("${user.role} - ${user.unit ?: "عمومی"}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            },
                                            onClick = {
                                                onSwitchUser(user)
                                                userMenuExpanded = false
                                                isUserBubbleExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Settings Button
                            Button(
                                onClick = {
                                    isUserBubbleExpanded = false
                                    onOpenSettings()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تنظیمات (قلم، پوسته، رمز عبور)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Logout Button
                            OutlinedButton(
                                onClick = {
                                    isUserBubbleExpanded = false
                                    onLogout()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("خروج از سامانه", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // 2. BOTTOM-END FLOATING GLASS BUBBLE / DOCK (دایره شیشه‌ای ناوبری و تب‌های میز کار در گوشه پایین صفحه)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 10.dp, end = 12.dp)
        ) {
            if (!isNavBubbleExpanded) {
                // Collapsed Floating Glass Bubble
                Surface(
                    onClick = { isNavBubbleExpanded = true },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.90f),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val activeConfig = roleTabs.firstOrNull { it.tab == currentTab }
                        Icon(
                            imageVector = activeConfig?.icon ?: Icons.Default.Dashboard,
                            contentDescription = "میز کار و منو",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else {
                // Expanded Glass Dock Bar
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    shadowElevation = 12.dp,
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        roleTabs.forEach { tabConfig ->
                            val isSelected = currentTab == tabConfig.tab
                            Surface(
                                onClick = {
                                    onTabSelected(tabConfig.tab)
                                    // Keep dock or collapse smoothly
                                },
                                shape = RoundedCornerShape(18.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = tabConfig.icon,
                                        contentDescription = tabConfig.title,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = tabConfig.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        // Close dock button
                        IconButton(
                            onClick = { isNavBubbleExpanded = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "بستن منو",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
