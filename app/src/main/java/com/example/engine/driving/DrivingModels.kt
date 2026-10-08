package com.example.engine.driving

data class RoutePoiItem(
    val id: String,
    val name: String,
    val category: String, // "Café & Bakery", "Restaurant", "Supercharger", "Fuel"
    val rating: Float,
    val reviewCount: Int,
    val detourMinutes: Int,
    val distanceKm: Float,
    val address: String,
    val openingHoursText: String,
    val isOpenNow: Boolean = true,
    val phone: String = "+49 (0) 89 555-0123",
    val isAddedToRoute: Boolean = false,
    val highlightReason: String = "Direkt an Ausfahrt 42 gelegen, keine Verzögerung"
)

data class TicketHoldAlert(
    val id: String,
    val eventTitle: String,
    val venue: String,
    val priceText: String,
    val quantity: Int = 2,
    val holdTimeRemainingSeconds: Int = 295, // ~5 min cart reservation
    val sourceSite: String = "Fansale / Eventim Resale",
    val isHolding: Boolean = true,
    val isConfirmed: Boolean = false
)

data class DrivingTelemetry(
    val currentRoad: String = "A8 Richtung Stuttgart (Ausfahrt 42)",
    val speedKmh: Int = 96,
    val batteryPct: Int = 82,
    val remainingDistanceKm: Int = 148,
    val etaTimeText: String = "18:42 Uhr",
    val isGpsActive: Boolean = true
)
