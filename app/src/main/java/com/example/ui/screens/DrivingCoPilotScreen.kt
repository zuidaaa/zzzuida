package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.driving.RoutePoiItem
import com.example.engine.driving.TicketHoldAlert
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrivingCoPilotScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val telemetry by viewModel.drivingTelemetry.collectAsStateWithLifecycle()
    val pois by viewModel.routePois.collectAsStateWithLifecycle()
    val ticketAlert by viewModel.ticketHoldAlert.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceConversationManager.voiceState.collectAsStateWithLifecycle()

    var isCallActive by remember { mutableStateOf(true) }
    var callSeconds by remember { mutableIntStateOf(142) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var voiceInputText by remember { mutableStateOf("Finde ein geöffnetes Café auf meiner Route") }
    var isMuted by remember { mutableStateOf(false) }

    LaunchedEffect(isCallActive) {
        while (isCallActive) {
            delay(1000)
            callSeconds += 1
        }
    }

    val formatCallTime = "${callSeconds / 60}:${String.format("%02d", callSeconds % 60)}"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D14))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP HUD: Automotive Driving Call Status Bar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131924)),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, DeepThinkEmeraldLight.copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(DeepThinkEmeraldLight)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "📞 AGENT CO-PILOT (DRIVE MODE)",
                                fontWeight = FontWeight.ExtraBold,
                                color = DeepThinkEmeraldLight,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = formatCallTime,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Automotive Telemetry Line
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = telemetry.currentRoad,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Zielankunft ${telemetry.etaTimeText} • Noch ${telemetry.remainingDistanceKm} km",
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted)
                            )
                        }

                        Surface(
                            color = Color(0xFF1E2838),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkCyan.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${telemetry.speedKmh}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DeepThinkCyanLight,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("km/h", fontSize = 11.sp, color = DarkTextMuted)
                            }
                        }
                    }
                }
            }
        }

        // TICKET HOLD LIVE ALERT ("It holds them and waits for your yes")
        ticketAlert?.let { alert ->
            if (alert.isHolding) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF261808)),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, DeepThinkAmber)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎟️", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "WATCHER ALERT: TICKETS GEFUNDEN!",
                                            fontWeight = FontWeight.ExtraBold,
                                            color = DeepThinkAmber,
                                            fontSize = 12.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = DeepThinkRose.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "Warenkorb: 04:44 verbleibend",
                                                fontSize = 10.sp,
                                                color = DeepThinkRose,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        "${alert.eventTitle} (${alert.quantity}x ${alert.priceText})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Der Hintergrund-Watcher hat face-value Originalpreis-Tickets auf ${alert.sourceSite} entdeckt. Die Tickets werden im Warenkorb reserviert und warten auf dein 'Ja'.",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.confirmTicketPurchase(alert.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepThinkEmeraldLight),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1.3f).height(48.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("JA, JETZT KAUFEN", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.releaseTicketHold(alert.id) },
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkRose),
                                    modifier = Modifier.weight(0.9f).height(48.dp)
                                ) {
                                    Text("FREIGEBEN", color = DeepThinkRose, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            } else if (alert.isConfirmed) {
                item {
                    Surface(
                        color = Color(0xFF0F2416),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkEmeraldLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("✅", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "2x Konzert-Tickets erfolgreich gekauft & gesichert!",
                                color = DeepThinkEmeraldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // LIVE VOICE INTERACTION PANEL ("Ring it while driving")
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131924)),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, DeepThinkCyan.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🎙️ Sprachdialog & Freisprech-Antwort",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Surface(
                            color = DeepThinkCyan.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "Hands-Free NPU Audio",
                                fontSize = 10.sp,
                                color = DeepThinkCyanLight,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Audio Waveform Visualization during call
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF090D14))
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(8, 16, 24, 12, 28, 20, 30, 18, 26, 10, 22, 28, 14, 20, 12, 18, 24, 16, 10, 14).forEach { barHeight ->
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(barHeight.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(DeepThinkCyanLight)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Spoken question & answer
                    val lastAnswer = if (uiState.lastDrivingCoPilotAnswer.isNotBlank()) {
                        uiState.lastDrivingCoPilotAnswer
                    } else {
                        "Ich habe 'Kaffeewerk Espresso & Bakery' direkt an Ausfahrt 42 gefunden. Jetzt bis 22:00 Uhr geöffnet. Nur 1 Minute Umweg von deiner A8-Route."
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0A0F18))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("Agent Co-Pilot:", fontSize = 10.sp, color = DeepThinkCyanLight, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lastAnswer,
                                fontSize = 13.sp,
                                color = Color.White,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Big Quick Voice Actions (Designed for driving safety)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.processDrivingVoiceQuery("Finde ein geöffnetes Café auf meiner Route") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2838)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("☕ Café Suchen", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.processDrivingVoiceQuery("Finde eine geöffnete Schnellladestation") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2838)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("⚡ Schnelllader", color = DeepThinkCyanLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.processDrivingVoiceQuery("Finde ein Restaurant mit warmer Küche") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2838)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("🍽️ Essen", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // SECTION: PLACES OPEN ON YOUR ROUTE ("Find somewhere open on your route")
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Geöffnete Orte entlang deiner Route (A8)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
                Text("Echtzeit-Öffnungszeiten", fontSize = 11.sp, color = DeepThinkEmeraldLight)
            }
        }

        items(pois) { poi ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131924)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (poi.isAddedToRoute) DeepThinkEmeraldLight else DarkOutlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = poi.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = DeepThinkEmerald.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "JETZT GEÖFFNET",
                                        fontSize = 9.sp,
                                        color = DeepThinkEmeraldLight,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "★ ${poi.rating} (${poi.reviewCount} Rezensionen) • ${poi.category}",
                                fontSize = 11.sp,
                                color = DeepThinkAmber
                            )
                        }

                        Surface(
                            color = Color(0xFF1E2838),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "+${poi.detourMinutes} min Umweg",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepThinkCyanLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = poi.openingHoursText,
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = "📍 ${poi.address} (${poi.highlightReason})",
                        fontSize = 10.sp,
                        color = DarkTextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.addPoiToDrivingRoute(poi.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (poi.isAddedToRoute) DeepThinkEmeraldLight else DeepThinkCyan
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            if (poi.isAddedToRoute) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text("🧭", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (poi.isAddedToRoute) "Zwischenstopp Hinzugefügt" else "Zwischenstopp Hinzufügen",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // BOTTOM DRIVING CONTROL DOCK
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1520)),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A364A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isMuted) DeepThinkRose.copy(alpha = 0.3f) else Color(0xFF1E2838))
                    ) {
                        Text(if (isMuted) "🔇" else "🎙️", fontSize = 22.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.processDrivingVoiceQuery("Finde eine geöffnete Raststätte mit Kaffee und Ladesäule")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepThinkCyan),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(52.dp).weight(1f).padding(horizontal = 12.dp)
                    ) {
                        Text("🗣️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SPRECHEN", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }

                    IconButton(
                        onClick = {
                            isCallActive = false
                            onClose()
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(DeepThinkRose)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "End Call", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}
