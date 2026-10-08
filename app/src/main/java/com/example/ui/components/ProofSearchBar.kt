package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@Composable
fun ProofSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        placeholder = { 
            Text(
                "Search reasoning cache by keyword...", 
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            ) 
        },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LuminousBlue) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LuminousBlue,
            unfocusedBorderColor = BorderSubtle,
            focusedContainerColor = AbsoluteBlack,
            unfocusedContainerColor = AbsoluteBlack,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
