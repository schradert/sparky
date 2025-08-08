package com.sparkysballoons.invx.view

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import sparkysballoonsinventory.composeapp.generated.resources.Res
import sparkysballoonsinventory.composeapp.generated.resources.no_data_available

@OptIn(ExperimentalResourceApi::class)
@Composable
fun EmptyContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(stringResource(Res.string.no_data_available))
    }
}