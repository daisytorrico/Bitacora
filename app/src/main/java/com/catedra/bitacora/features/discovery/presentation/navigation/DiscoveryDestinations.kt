package com.catedra.bitacora.features.discovery.presentation.navigation

object DiscoveryDestinations {
    const val EXPLORER = "explorer"
    const val ALL_PUBLIC_TRAVELS = "all_public_travels"
    const val PUBLIC_PROFILE = "public_profile/{userId}"
    const val PUBLIC_TRAVEL_DETAIL = "public_travel_detail/{travelId}"
    const val PUBLIC_POINT_DETAIL = "public_point_detail/{travelId}/{pointId}/{ownerId}"
    const val DISCOVERY_GRAPH = "discovery_graph"

    fun publicProfile(userId: String) = "public_profile/$userId"
    fun publicTravelDetail(travelId: String) = "public_travel_detail/$travelId"
    fun publicPointDetail(travelId: String, pointId: String, ownerId: String) = 
        "public_point_detail/$travelId/$pointId/$ownerId"
}
