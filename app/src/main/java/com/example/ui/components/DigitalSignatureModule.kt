package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.DigitalSignatureEntity
import com.example.data.entity.UserEntity
import com.example.data.service.DigitalSignatureService
import com.example.data.service.DynamicTokenInfo
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * ماژول تایید چندمرحله‌ای (سلسله مراتب امضاء) دیجیتال با رمز پویا (OTP)
 * ویژه اسناد اورهال مجتمع فولاد غدیر نی‌ریز
 */
@Composable
fun DigitalSignatureHierarchyCard(
    documentType: String,
    documentId: Long,
    documentTitle: String,
    signatures: List<DigitalSignatureEntity>,
    currentUser: UserEntity?,
    onOpenSignDialog: (stepOrder: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = remember {
        listOf(
            Triple(1, "ثبت اولیه ناظر اجرایی", "supervisor"),
            Triple(2, "تایید رئیس واحد متقاضی", "unit_head"),
            Triple(3, "صدور ایمنی و بهداشت HSE", "hse"),
            Triple(4, "تایید نهایی مدیر اورهال", "admin")
        )
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "سلسله مراتب امضای دیجیتال با رمز پویا",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                val signedCount = signatures.count { it.signatureStatus == "signed" }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (signedCount == 4) Color(0xFF10B981).copy(alpha = 0.12f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "$signedCount از ۴ مرحله امضا شده",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (signedCount == 4) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Steps Chain
            steps.forEachIndexed { index, (stepOrder, title, role) ->
                val sig = signatures.firstOrNull { it.stepOrder == stepOrder && it.signatureStatus == "signed" }
                val isPrevSigned = stepOrder == 1 || signatures.any { it.stepOrder == stepOrder - 1 && it.signatureStatus == "signed" }
                val isCurrentUserAllowed = when (role) {
                    "supervisor" -> currentUser?.role in listOf("supervisor", "planner", "admin")
                    "unit_head" -> currentUser?.role in listOf("unit_head", "admin")
                    "hse" -> currentUser?.role in listOf("hse", "admin") || currentUser?.unit?.contains("ایمنی") == true
                    "admin" -> currentUser?.role == "admin"
                    else -> false
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Timeline indicator column
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(28.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = when {
                                sig != null -> Color(0xFF10B981)
                                isPrevSigned -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outlineVariant
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (sig != null) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Text(
                                        text = "$stepOrder",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (index < steps.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(34.dp)
                                    .background(
                                        if (sig != null) Color(0xFF10B981).copy(alpha = 0.6f)
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Step Info Box
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = if (index < steps.size - 1) 8.dp else 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "گام $stepOrder: $title",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sig != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (sig != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("رمز پویا تایید شد", fontSize = 9.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (sig != null) {
                            Text(
                                text = "امضاکننده: ${sig.signerName} (${sig.signerUnit.ifBlank { sig.signerRole }}) • ${sig.signedAt}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (sig.comments.isNotBlank()) {
                                Text(
                                    text = "ملاحظات: ${sig.comments}",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else if (isPrevSigned) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isCurrentUserAllowed) "در انتظار امضای شما با رمز پویا" else "در انتظار امضای مسوول مربوطه",
                                    fontSize = 10.sp,
                                    color = if (isCurrentUserAllowed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (isCurrentUserAllowed) {
                                    Button(
                                        onClick = { onOpenSignDialog(stepOrder) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("امضا با رمز پویا", fontSize = 9.sp)
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "قفل شده • منوط به تکمیل مراحل قبل",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * دیالوگ امنیتی صدور و اعتبارسنجی رمز پویا (OTP) و ثبت امضای دیجیتال
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicTokenSignatureDialog(
    documentType: String,
    documentId: Long,
    documentTitle: String,
    stepOrder: Int,
    currentUser: UserEntity?,
    service: DigitalSignatureService,
    onDismiss: () -> Unit,
    onConfirmSign: (stepOrder: Int, enteredToken: String, generatedToken: String, expiry: Long, remarks: String) -> Unit
) {
    val stepDef = remember(stepOrder) {
        service.hierarchySteps.firstOrNull { it.stepOrder == stepOrder }
    }

    var tokenInfo by remember {
        mutableStateOf(service.generateDynamicToken(currentUser?.id ?: 0L, documentId))
    }
    var enteredToken by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var remainingSeconds by remember { mutableIntStateOf(120) }

    // Live countdown timer for OTP expiration
    LaunchedEffect(tokenInfo) {
        remainingSeconds = 120
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
    }

    val isExpired = remainingSeconds <= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "امضای دیجیتال با رمز پویا (OTP)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "گام $stepOrder: ${stepDef?.title ?: ""}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Document & User Summary Box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "سند: $documentTitle",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "امضاکننده: ${currentUser?.name ?: ""} | سمت: ${stepDef?.targetRoleLabel ?: ""}",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Dynamic Token Generator Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isExpired) Color(0xFFEF4444).copy(alpha = 0.08f) else Color(0xFF0F766E).copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, if (isExpired) Color(0xFFEF4444).copy(alpha = 0.4f) else Color(0xFF0F766E).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "رمز پویای احراز هویت الکترونیکی (یکبار مصرف)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Monospace OTP Display
                        Text(
                            text = if (isExpired) "منقضی شد" else tokenInfo.token.chunked(3).joinToString(" - "),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isExpired) Color(0xFFEF4444) else Color(0xFF0F766E),
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Countdown
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (isExpired) Color(0xFFEF4444) else Color(0xFFD97706),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val minutes = remainingSeconds / 60
                            val seconds = remainingSeconds % 60
                            Text(
                                text = if (isExpired) "رمز منقضی شده، مجدداً دریافت نمایید" else String.format(Locale.US, "زمان باقیمانده: %02d:%02d", minutes, seconds),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isExpired) Color(0xFFEF4444) else Color(0xFFD97706)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    tokenInfo = service.generateDynamicToken(currentUser?.id ?: 0L, documentId)
                                    enteredToken = tokenInfo.token
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("تولید رمز جدید", fontSize = 9.sp)
                            }

                            if (!isExpired) {
                                Button(
                                    onClick = { enteredToken = tokenInfo.token },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("درج خودکار رمز", fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }

                // OTP Input Field
                OutlinedTextField(
                    value = enteredToken,
                    onValueChange = { if (it.length <= 6) enteredToken = it },
                    label = { Text("ورود رمز پویا (۶ رقمی)", fontSize = 11.sp) },
                    placeholder = { Text("مثال: ${tokenInfo.token}", fontSize = 10.sp) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Remarks Field
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("توضیحات و ملاحظات فنی امضا (اختیاری)", fontSize = 11.sp) },
                    placeholder = { Text("دستورالعمل‌ها یا موارد بررسی شده...", fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                // Legal Statement
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "این امضا با استانداردهای حقوقی اورهال مجتمع فولاد غدیر نی‌ریز ثبت و دارای بار مسئولیت اجرایی و ایمنی است.",
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmSign(
                        stepOrder,
                        enteredToken,
                        tokenInfo.token,
                        tokenInfo.expiresAtTimestamp,
                        remarks
                    )
                },
                enabled = enteredToken.isNotBlank() && !isExpired,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تایید و ثبت امضا", fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", fontSize = 11.sp)
            }
        }
    )
}
