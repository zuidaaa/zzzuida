package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.SearchCitation
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiEmerald

@Composable
fun SearchCitationCard(
    citation: SearchCitation,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded }
            .testTag("search_citation_card"),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GeminiCyan.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header Row: Domain Badge + Freshness + Link Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GeminiCyan.copy(alpha = 0.18f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = GeminiCyan,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = citation.sourceDomain,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GeminiCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GeminiEmerald,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = citation.freshness,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GeminiEmerald,
                                fontSize = 9.5.sp
                            )
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Citation link",
                    tint = DarkTextMuted,
                    modifier = Modifier.size(13.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Citation Title
            Text(
                text = citation.title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = DarkTextPrimary
                ),
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Citation Snippet
            Text(
                text = citation.snippet,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = DarkTextMuted,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                ),
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
