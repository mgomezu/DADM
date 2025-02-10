package co.edu.unal.reto11

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

// --- INTERFAZ PARA LA API ---
interface FootballApiService {
    @Headers(
        "x-apisports-key: 83d63fc24a996f5dc516b4ea8968c569"
    )
    @GET("teams/statistics")
    suspend fun getTeamStatistics(
        @Query("league") league: Int,
        @Query("season") season: Int,
        @Query("team") team: Int
    ): TeamStatisticsResponse
}

// --- MODELO DE DATOS ---
data class TeamStatisticsResponse(
    val response: TeamStatistics
)

data class ApiResponse(
    val get: String,
    val parameters: Parameters,
    val errors: List<String>,
    val results: Int,
    val paging: Paging,
    val response: TeamStatistics
)

data class Parameters(
    val league: String,
    val season: String,
    val team: String
)

data class Paging(
    val current: Int,
    val total: Int
)

data class TeamStatistics(
    val league: LeagueInfo,
    val team: TeamInfo,
    val form: String,
    val fixtures: FixturesStats,
    val goals: GoalStats,
    val biggest: BiggestStats,
    val penalty: PenaltyStats,
    val lineups: List<Lineup>,
    val cards: CardsStats
)

data class LeagueInfo(
    val id: Int,
    val name: String,
    val country: String,
    val logo: String,
    val flag: String,
    val season: Int
)

data class TeamInfo(
    val id: Int,
    val name: String,
    val logo: String
)

data class FixturesStats(
    val played: HomeAwayTotal,
    val wins: HomeAwayTotal,
    val draws: HomeAwayTotal,
    val loses: HomeAwayTotal
)

data class HomeAwayTotal(
    val home: Double,
    val away: Double,
    val total: Double
)

data class GoalStats(
    val `for`: GoalDetails,
    val against: GoalDetails
)

data class GoalDetails(
    val total: HomeAwayTotal,
    val average: HomeAwayTotal,
    val minute: Map<String, GoalMinute?>,
    val under_over: Map<String, OverUnder>
)

data class GoalMinute(
    val total: Double?,
    val percentage: String?
)

data class OverUnder(
    val over: Double,
    val under: Double
)

data class BiggestStats(
    val streak: StreakStats,
    val wins: HomeAwayString,
    val loses: HomeAwayString,
    val goals: HomeAwayTotal
)

data class StreakStats(
    val wins: Int,
    val draws: Int,
    val loses: Int
)

data class HomeAwayString(
    val home: String,
    val away: String
)

data class PenaltyStats(
    val scored: PenaltyDetail,
    val missed: PenaltyDetail,
    val total: Int
)

data class PenaltyDetail(
    val total: Int,
    val percentage: String
)

data class Lineup(
    val formation: String,
    val played: Int
)

data class CardsStats(
    val yellow: Map<String, CardMinute?>,
    val red: Map<String, CardMinute?>
)

data class CardMinute(
    val total: Int?,
    val percentage: String?
)


// --- CLIENTE RETROFIT ---
object RetrofitInstance {
    private const val BASE_URL = "https://v3.football.api-sports.io/"

    val api: FootballApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FootballApiService::class.java)
    }
}
