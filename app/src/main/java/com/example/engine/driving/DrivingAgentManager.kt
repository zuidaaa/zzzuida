package com.example.engine.driving

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class DrivingAgentManager(private val context: Context) {

    private val _telemetry = MutableStateFlow(DrivingTelemetry())
    val telemetry: StateFlow<DrivingTelemetry> = _telemetry.asStateFlow()

    private val _routePois = MutableStateFlow<List<RoutePoiItem>>(
        listOf(
            RoutePoiItem(
                id = "poi-1",
                name = "Kaffeewerk Espresso & Artisan Bakery",
                category = "Café & Bakery",
                rating = 4.9f,
                reviewCount = 842,
                detourMinutes = 1,
                distanceKm = 0.8f,
                address = "Autohof Ausfahrt 42, Westallee 4",
                openingHoursText = "Jetzt geöffnet bis 22:00 Uhr (Frische Bohnen & Sauerteig)",
                isOpenNow = true,
                phone = "+49 89 555-0144",
                highlightReason = "Liegt direkt an der Abfahrt ohne Ampelkreuzung"
            ),
            RoutePoiItem(
                id = "poi-2",
                name = "Tesla 300kW Supercharger & Lounge",
                category = "Supercharger",
                rating = 4.8f,
                reviewCount = 1250,
                detourMinutes = 2,
                distanceKm = 1.4f,
                address = "Ladepark A8 Süd",
                openingHoursText = "24/7 geöffnet (16 Ladesäulen frei)",
                isOpenNow = true,
                phone = "+49 89 555-0199",
                highlightReason = "16 von 20 Hypercharger-Plätzen verfügbar"
            ),
            RoutePoiItem(
                id = "poi-3",
                name = "Trattoria & Pizzeria Al Lago",
                category = "Restaurant",
                rating = 4.7f,
                reviewCount = 620,
                detourMinutes = 4,
                distanceKm = 2.8f,
                address = "Seestraße 12, am Baggersee",
                openingHoursText = "Warme Küche geöffnet bis 23:00 Uhr",
                isOpenNow = true,
                phone = "+49 89 555-0288",
                highlightReason = "Schnelle Pasta & Pizza zum Mitnehmen oder vor Ort"
            ),
            RoutePoiItem(
                id = "poi-4",
                name = "Aral Pulse 350kW & REWE To Go",
                category = "Fuel",
                rating = 4.5f,
                reviewCount = 490,
                detourMinutes = 1,
                distanceKm = 0.5f,
                address = "Raststätte A8 West",
                openingHoursText = "24/7 durchgehend geöffnet",
                isOpenNow = true,
                phone = "+49 89 555-0311",
                highlightReason = "Direkt auf der Rastanlage ohne Verlassen der Autobahn"
            )
        )
    )
    val routePois: StateFlow<List<RoutePoiItem>> = _routePois.asStateFlow()

    private val _ticketHoldAlert = MutableStateFlow<TicketHoldAlert?>(
        TicketHoldAlert(
            id = "ticket-taylor-swift",
            eventTitle = "Taylor Swift | The Eras Tour",
            venue = "Olympiastadion München - Innenraum Stehplatz",
            priceText = "120,00 € (Original-Nennwert / Face-Value)",
            quantity = 2,
            holdTimeRemainingSeconds = 284,
            sourceSite = "Fansale / Eventim Resale Queue",
            isHolding = true,
            isConfirmed = false
        )
    )
    val ticketHoldAlert: StateFlow<TicketHoldAlert?> = _ticketHoldAlert.asStateFlow()

    suspend fun searchOpenPlacesOnRoute(query: String, category: String = "All"): List<RoutePoiItem> = withContext(Dispatchers.IO) {
        val q = query.lowercase().trim()
        val all = _routePois.value
        if (q.isEmpty() && (category == "All" || category.isEmpty())) {
            all
        } else {
            all.filter { poi ->
                val matchesCat = category == "All" || category.isEmpty() || poi.category.contains(category, ignoreCase = true)
                val matchesQuery = q.isEmpty() || poi.name.lowercase().contains(q) || poi.category.lowercase().contains(q) || poi.address.lowercase().contains(q)
                matchesCat && matchesQuery
            }
        }
    }

    suspend fun addPoiToActiveRoute(poiId: String) = withContext(Dispatchers.IO) {
        _routePois.update { list ->
            list.map {
                if (it.id == poiId) it.copy(isAddedToRoute = true) else it
            }
        }
    }

    suspend fun confirmTicketPurchase(ticketId: String): String = withContext(Dispatchers.IO) {
        _ticketHoldAlert.update { alert ->
            if (alert?.id == ticketId) alert.copy(isHolding = false, isConfirmed = true) else alert
        }
        "Erfolg: 2x Originalpreis-Tickets verbindlich gebucht & in deinem Account hinterlegt!"
    }

    suspend fun releaseTicketHold(ticketId: String) = withContext(Dispatchers.IO) {
        _ticketHoldAlert.update { null }
    }

    suspend fun generateDrivingSpokenResponse(userQuery: String): String = withContext(Dispatchers.IO) {
        val q = userQuery.lowercase()
        when {
            q.contains("kaffee") || q.contains("cafe") || q.contains("café") || q.contains("bäcker") -> {
                "Ich habe 'Kaffeewerk Espresso & Bakery' direkt an Ausfahrt 42 gefunden. Hat jetzt bis 22:00 Uhr geöffnet. Nur 1 Minute Umweg. Soll ich den Zwischenstopp hinzufügen?"
            }
            q.contains("essen") || q.contains("restaurant") || q.contains("pizza") || q.contains("hunger") -> {
                "'Trattoria Al Lago' hat warme Küche bis 23:00 Uhr geöffnet. 4 Minuten Umweg von der A8. Soll ich einen Tisch vormerken oder die Route anpassen?"
            }
            q.contains("tanken") || q.contains("laden") || q.contains("supercharger") || q.contains("akku") -> {
                "In 1,4 km kommt der Tesla Ladepark mit 16 freien Supercharger-Säulen. Liegt direkt am Autohof."
            }
            q.contains("ticket") || q.contains("konzert") || q.contains("karten") -> {
                "Dein Resale-Watcher hält aktuell 2 Tickets für Taylor Swift im Warenkorb zum Originalpreis von je 120 Euro. Noch 4 Minuten Reservierungszeit verbleibend. Sag einfach 'Ja', um den Kauf abzuschließen."
            }
            else -> {
                "Ich habe drei geöffnete Orte entlang deiner Fahrtroute auf der A8 gefunden: Ein Café an Ausfahrt 42, einen Supercharger und eine Pizzeria mit warmer Küche. Welches Ziel bevorzugst du?"
            }
        }
    }
}
