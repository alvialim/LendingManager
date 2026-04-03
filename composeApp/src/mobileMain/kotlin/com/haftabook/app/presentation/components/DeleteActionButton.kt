package com.haftabook.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DeleteActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Delete",
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    shape: RoundedCornerShape = RoundedCornerShape(10.dp),
    /** When true, matches Material chip row height (e.g. next to loan count chip). */
    matchChipRowSize: Boolean = false,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.then(
            if (matchChipRowSize) Modifier.heightIn(min = 32.dp) else Modifier
        ),
        shape = shape,
        border = BorderStroke(1.dp, Color(0xFFDC2626)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = Color(0xFFDC2626),
        ),
        contentPadding = contentPadding,
    ) {
        Text(text)
    }
}

