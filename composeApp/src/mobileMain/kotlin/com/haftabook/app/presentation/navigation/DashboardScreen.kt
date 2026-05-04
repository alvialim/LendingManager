package com.haftabook.app.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.haftabook.app.data.remote.PinType
import com.haftabook.app.presentation.components.ResponsiveCentered

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOpenDrawer: () -> Unit,
    isShowMonthlyEnabled: Boolean,
    onSelectMode: (PinType) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Dashboard") },
            navigationIcon = {
                IconButton(onClick = onOpenDrawer) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
            }
        )
        ResponsiveCentered(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { inner ->
            Column(
                modifier = inner
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isShowMonthlyEnabled) {
                        DashboardModeCard(
                            title = "Monthly",
                            icon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF2563EB)) },
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectMode(PinType.MONTHLY) }
                        )
                    }
                    DashboardModeCard(
                        title = "Daily",
                        icon = { Icon(Icons.Default.Today, contentDescription = null, tint = Color(0xFF16A34A)) },
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectMode(PinType.DAILY) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardModeCard(
    title: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            Spacer(Modifier.height(10.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium)
        }
    }
}
