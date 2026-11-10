package com.example.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.example.R

/**
 * Standard Google Font Provider configured with Google Play Services (GMS) authority
 * and certificate hashes defined in res/values/font_certs.xml
 */
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val CairoFont = GoogleFont("Cairo")
val AmiriFont = GoogleFont("Amiri")

// Fallback font families ensuring the app never crashes if font download fails
val CairoFallback: FontFamily = FontFamily.SansSerif
val AmiriFallback: FontFamily = FontFamily.Serif

/**
 * Cairo Font Family with weights: Normal (400), Medium (500), SemiBold (600), Bold (700).
 * Features fallback to FontFamily.SansSerif if downloading fails.
 */
val CairoFamily: FontFamily = try {
    FontFamily(
        Font(googleFont = CairoFont, fontProvider = provider, weight = FontWeight.Normal),
        Font(googleFont = CairoFont, fontProvider = provider, weight = FontWeight.Medium),
        Font(googleFont = CairoFont, fontProvider = provider, weight = FontWeight.SemiBold),
        Font(googleFont = CairoFont, fontProvider = provider, weight = FontWeight.Bold)
    )
} catch (e: Throwable) {
    FontFamily.SansSerif
}

/**
 * Amiri Font Family with weights: Normal (400), Bold (700).
 * Features fallback to FontFamily.Serif if downloading fails.
 */
val AmiriFamily: FontFamily = try {
    FontFamily(
        Font(googleFont = AmiriFont, fontProvider = provider, weight = FontWeight.Normal),
        Font(googleFont = AmiriFont, fontProvider = provider, weight = FontWeight.Bold)
    )
} catch (e: Throwable) {
    FontFamily.Serif
}
