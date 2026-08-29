package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

enum class PersianFontFamilyOption(val displayName: String, val description: String, val fontFamily: FontFamily) {
    VAZIRMATN("وزیرمتن (Vazirmatn)", "فونت پیش‌فرض استاندارد مهندسی و اداری", FontFamily.SansSerif),
    IRANYEKAN("ایران‌یکان (IRANYekan)", "فونت رسمی گزارشات و داشبوردهای صنعتی", FontFamily.Default),
    SHABNAM("شبنم (Shabnam)", "فونت خوانا و مدرن با وضوح بالا", FontFamily.SansSerif),
    YEKAN_BAKH("یکان بخ (Yekan Bakh)", "فونت هندسی و مدرن مانیتورینگ کارگاهی", FontFamily.Default),
    SAHEL("ساهل (Sahel)", "فونت کلاسیک و چشم‌نواز اسناد فنی", FontFamily.Serif),
    SYSTEM_DEFAULT("پیش‌فرض سیستم (System)", "فونت استاندارد سیستم‌عامل دستگاه", FontFamily.Default)
}

fun getAppTypography(fontOption: PersianFontFamilyOption = PersianFontFamilyOption.VAZIRMATN): Typography {
    val family = fontOption.fontFamily
    return Typography(
        displayLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 36.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 28.sp
        ),
        titleLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 24.sp
        ),
        titleMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = 22.sp
        ),
        titleSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 18.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.2.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 12.5.sp,
            lineHeight = 18.sp
        ),
        bodySmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            lineHeight = 16.sp
        ),
        labelLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            lineHeight = 14.sp
        ),
        labelSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 9.sp,
            lineHeight = 12.sp
        )
    )
}

// Set of Material typography styles to start with
val Typography = getAppTypography(PersianFontFamilyOption.VAZIRMATN)

