package com.azcode.busmanagmentsystem.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.azcode.busmanagmentsystem.ui.theme.PrimaryBlue


@Composable
fun LoadingDialog(showDialog: Boolean, onDismiss: () -> Unit) {
    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            )
        ) {
            Box(
                modifier = Modifier.wrapContentSize(align = Alignment.Center)
            ) {
                CircularProgressIndicator(
                    color = PrimaryBlue,
                    strokeWidth = 2.dp,
                    trackColor = Color.Transparent,

                )
            }
        }
    }
}