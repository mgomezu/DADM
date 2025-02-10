package co.edu.unal.reto11

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey
    )

    fun fetchFootballStatsAndSendPrompt(bitmap: Bitmap, userPrompt: String) {
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 🔹 Llamada a la API con datos de la liga 135 (Serie A), temporada 2023 y equipo 489 (AC Milan)
                val response = RetrofitInstance.api.getTeamStatistics(
                    league = 135,
                    season = 2023,
                    team = 489
                )

                val stats = response.response

                // 🔹 Extraer datos clave
                val teamName = stats.team.name
                val leagueName = stats.league.name
                val gamesPlayed = stats.fixtures.played.total
                val wins = stats.fixtures.wins.total
                val draws = stats.fixtures.draws.total
                val losses = stats.fixtures.loses.total
                val goalsScored = stats.goals.`for`.total.total

                // 🔹 Crear el prompt con datos del equipo
                val combinedPrompt = """
                    El usuario pregunta: $userPrompt
                    Aquí tienes información sobre el equipo $teamName en la liga $leagueName:
                    - Partidos jugados: $gamesPlayed
                    - Victorias: $wins
                    - Empates: $draws
                    - Derrotas: $losses
                    - Goles anotados: $goalsScored
                """.trimIndent()

                // 🔹 Enviar todo a Gemini
                val geminiResponse = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(combinedPrompt)
                    }
                )

                geminiResponse.text?.let { outputContent ->
                    _uiState.value = UiState.Success(outputContent)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "Error")
            }
        }
    }
}
