package com.haftabook.app.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.haftabook.app.presentation.components.ResponsiveCentered
import com.haftabook.app.utils.CustomerCommunicationText

@Composable
fun ResetPinScreen(
    onSendOtp: () -> Unit,
    onBack: () -> Unit,
    isSending: Boolean,
    modifier: Modifier = Modifier,
) {
    ResponsiveCentered(modifier = modifier) { inner ->
        Column(
            modifier = inner
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Reset PIN",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "We will open SMS with a one-time password to ${CustomerCommunicationText.PIN_RESET_OTP_NUMBER}. Send the message, then verify the OTP on the next screen.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onSendOtp,
                enabled = !isSending,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isSending) "Opening SMS…" else "Send OTP")
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Back")
            }
        }
    }
}
