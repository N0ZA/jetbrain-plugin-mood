package com.antigravity.codemood.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service(Service.Level.PROJECT)
@State(
    name = "com.antigravity.codemood.services.DailyStatsService",
    storages = [Storage("CodeMoodDailyStats.xml")]
)
class DailyStatsService : PersistentStateComponent<DailyStatsService.State> {

    data class DailyStats(
        var date: String = "",
        var totalMinutes: Int = 0,
        var totalWpmSum: Double = 0.0,
        var wpmSampleCount: Int = 0,
        var moodCounts: MutableMap<String, Int> = mutableMapOf(),
        // New: Hourly breakdown (Hour 0-23 -> Sum)
        var hourlyWpmSums: MutableMap<Int, Double> = mutableMapOf(),
        var hourlySampleCounts: MutableMap<Int, Int> = mutableMapOf()
    ) {
        val averageWpm: Double
            get() = if (wpmSampleCount > 0) totalWpmSum / wpmSampleCount else 0.0
            
        fun getHourlyAvg(hour: Int): Double {
            val sum = hourlyWpmSums[hour] ?: 0.0
            val count = hourlySampleCounts[hour] ?: 0
            return if (count > 0) sum / count else 0.0
        }
    }

    data class State(
        var stats: MutableMap<String, DailyStats> = mutableMapOf()
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    fun logMinute(wpm: Double, mood: MoodState) {
        val now = java.time.LocalDateTime.now()
        val today = now.format(DateTimeFormatter.ISO_DATE)
        val hour = now.hour
        
        val daily = myState.stats.getOrPut(today) { DailyStats(date = today) }
        
        // Daily Aggregates
        daily.totalMinutes++
        daily.totalWpmSum += wpm
        daily.wpmSampleCount++
        
        val moodName = mood.name
        daily.moodCounts[moodName] = (daily.moodCounts[moodName] ?: 0) + 1
        
        // Hourly Aggregates
        daily.hourlyWpmSums[hour] = (daily.hourlyWpmSums[hour] ?: 0.0) + wpm
        daily.hourlySampleCounts[hour] = (daily.hourlySampleCounts[hour] ?: 0) + 1
    }
    
    fun getTodayStats(): DailyStats? {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        return myState.stats[today]
    }
}
