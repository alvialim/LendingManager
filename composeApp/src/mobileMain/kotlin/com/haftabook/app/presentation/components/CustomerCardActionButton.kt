package com.haftabook.app.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/** Square touch target aligned with loan-count chip on the home customer card. */
val CustomerCardActionHeight = 36.dp

private val CustomerCardActionIconSize = 20.dp

/** No border; light fill + dark icon; Material3 press ripple (Share / SMS / Delete style). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerCardActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    backgroundColor: Color,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(CustomerCardActionHeight),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        contentColor = Color.Black,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(CustomerCardActionIconSize),
                tint = Color.Black
            )
        }
    }
}
