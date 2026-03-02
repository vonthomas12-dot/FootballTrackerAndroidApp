package com.example.footballtracker.data.remote

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class PlayerUploadDto(
    val name: String,
    val goals: Int,
    val assists: Int,
    val team: String
)

@Serializable
data class MatchUploadDto(
    val teamAName: String,
    val teamBName: String,
    val teamAScore: Int,
    val teamBScore: Int,
    val timestamp: Long,
    val players: List<PlayerUploadDto>
)

interface MatchApi {
    @POST("matches/upload") // Update with your actual endpoint
    suspend fun uploadMatch(@Body match: MatchUploadDto): Response<Unit>

    companion object {
        const val BASE_URL = "https://micoapi/api/" // Update with your actual base URL
    }
}
