package com.azcode.busmanagmentsystem.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.azcode.busmanagmentsystem.R

val LatoFont = FontFamily(
    Font(R.font.lato_regular, FontWeight.Normal),
    Font(R.font.lato_bold, FontWeight.Bold),
    Font(R.font.lato_black, FontWeight.Medium)
)
// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = TextStyle( // Used for large headings
        fontFamily = LatoFont,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    headlineLarge = TextStyle( // Used for section titles
        fontFamily = LatoFont,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle( // Used for app bars, dialogs
        fontFamily = LatoFont,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle( // Used for tabs
        fontFamily = LatoFont,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    ),
    bodyLarge = TextStyle( // Used for regular text
        fontFamily = LatoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle( // Used for buttons
        fontFamily = LatoFont,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle( // Used for smaller labels
        fontFamily = LatoFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    ),
    labelSmall = TextStyle( // Used for captions
        fontFamily = LatoFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp
    )
)



