package com.example.footballtracker.data.remote

import com.example.footballtracker.BuildConfig
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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

@Serializable
data class EventPlayerDto(
    val name: String,
    val team: String? = null
)

@Serializable
data class EventDto(
    val eventId: Int? = null,
    val date: String? = null,
    val players: List<EventPlayerDto> = emptyList()
)

interface MatchApi {
    @POST("match-result")
    suspend fun uploadMatch(@Body match: MatchUploadDto): Response<Unit>

    @GET("event-players")
    suspend fun getEvent(@Query("date") date: String): Response<EventDto>

    companion object {
        val BASE_URL get() = BuildConfig.BASE_URL
    }
}


