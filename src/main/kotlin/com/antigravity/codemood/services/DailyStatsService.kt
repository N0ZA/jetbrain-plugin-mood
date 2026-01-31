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
        var moodCounts: MutableMap<String, Int> = mutableMapOf() // Mood Name -> Minutes
    ) {
        val averageWpm: Double
            get() = if (wpmSampleCount > 0) totalWpmSum / wpmSampleCount else 0.0
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
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        
        val daily = myState.stats.getOrPut(today) { DailyStats(date = today) }
        
        daily.totalMinutes++
        daily.totalWpmSum += wpm
        daily.wpmSampleCount++
        
        val moodName = mood.name
        daily.moodCounts[moodName] = (daily.moodCounts[moodName] ?: 0) + 1
    }
    
    fun getTodayStats(): DailyStats? {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        return myState.stats[today]
    }
}
