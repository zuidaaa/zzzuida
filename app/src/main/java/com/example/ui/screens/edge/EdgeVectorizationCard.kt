package com.example.ui.screens.edge

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.edge.WorkspaceSemanticSearchResult
import com.example.ui.theme.*

/**
 * UI Fragment: Workspace Vectorization & Semantic Context Retrieval Card (Design Aspect 2).
 * Enables Zero-Fullcode Transfer by retrieving compact 128-D vector embeddings.
 */
@Composable
fun EdgeVectorizationCard(
    isComputingEmbeddings: Boolean,
    onIndexEmbeddings: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onExecuteSearch: () -> Unit,
    searchResults: List<WorkspaceSemanticSearchResult>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkBackground),
        border = BorderStroke(1.dp, VibrantTeal.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = "Vectorization Hub",
                        tint = VibrantTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Workspace Vectorization (Zero-Fullcode Transfer)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = VibrantTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
                Button(
                    onClick = onIndexEmbeddings,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantTeal),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isComputingEmbeddings) "Computing..." else "Index Embeddings (128-D)",
                        fontSize = 10.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Captures file semantics via compact local embedding models to feed precise context into the prompt stream instead of transmitting entire bulky source trees.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = DarkTextMuted,
                    fontSize = 10.5.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Query semantics (e.g. 'fibonacci', 'unit test', 'dashboard')",
                            color = DarkTextMuted,
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VibrantTeal,
                        unfocusedBorderColor = DarkOutlineVariant,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onExecuteSearch,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(VibrantTeal.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Semantic Search",
                        tint = VibrantTeal,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (searchResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Top Semantic Matches (Cosine Similarity):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = GeminiBlueLight,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                searchResults.forEach { res ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .background(DarkSurfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = res.filePath,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                )
                            )
                            Text(
                                text = res.matchingSnippet,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DarkTextMuted,
                                    fontSize = 9.5.sp
                                ),
                                maxLines = 1
                            )
                        }
                        Text(
                            text = "${(res.similarityScore * 100f).coerceIn(0f, 100f).toInt()}% match",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GeminiEmerald,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
