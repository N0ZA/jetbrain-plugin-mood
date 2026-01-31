package com.antigravity.codemood.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.util.LinkedList

enum class MoodState {
    NEUTRAL,
    FLOW,
    FRUSTRATED,
    TIRED
}

@Service(Service.Level.PROJECT)
class MoodAnalysisService(private val project: Project) {

    private val keystrokeTimestamps = LinkedList<Long>()
    private val deletionTimestamps = LinkedList<Long>()
    private val windowSizeMs = 60 * 1000L // 1 minute window
    
    // Cooldown to prevent spamming the user with breathing exercises
    private var lastInterventionTime = 0L
    private val interventionCooldownMs = 5 * 60 * 1000L // 5 minutes

    // Public Stats for Dashboard
    var currentWpm: Double = 0.0
        private set
    var currentDeletions: Int = 0
        private set
    var averageWpm: Double = 0.0
        private set

    // Session Stats
    private var totalWpmSamples = 0
    private var cumulativeWpm = 0.0
    
    // Timer for logging history every minute
    private var lastLogTime = System.currentTimeMillis()

    var currentMood: MoodState = MoodState.NEUTRAL
        private set

    fun recordKeystroke() {
        cleanOldData()
        keystrokeTimestamps.add(System.currentTimeMillis())
        analyzeMood()
    }
    
    // Called by DeletionActionListener
    fun recordDeletion() {
        cleanOldData()
        deletionTimestamps.add(System.currentTimeMillis())
        analyzeMood()
    }

    private fun cleanOldData() {
        val now = System.currentTimeMillis()
        val cutoff = now - windowSizeMs
        while (keystrokeTimestamps.isNotEmpty() && keystrokeTimestamps.first < cutoff) {
            keystrokeTimestamps.removeFirst()
        }
        while (deletionTimestamps.isNotEmpty() && deletionTimestamps.first < cutoff) {
            deletionTimestamps.removeFirst()
        }
    }

    private fun analyzeMood() {
        val wpm = (keystrokeTimestamps.size / 5.0) // Approx 5 chars per word
        val deletionRate = deletionTimestamps.size
        
        // Update public stats
        currentWpm = wpm
        currentDeletions = deletionRate
        
        // Update Average WPM (Adaptive Heuristic)
        if (wpm > 0) {
            totalWpmSamples++
            cumulativeWpm += wpm
            averageWpm = cumulativeWpm / totalWpmSamples
        }
        
        // Zero typing = Neutral? Or just waiting.
        if (keystrokeTimestamps.isEmpty()) {
            currentMood = MoodState.NEUTRAL
            return
        }

        // Heuristics: Use Adaptive Average!
        // Flow = 20% faster than your average + low errors
        val flowThreshold = if (averageWpm > 10) averageWpm * 1.2 else 40.0
        
        val newMood = when {
            deletionRate > 20 -> MoodState.FRUSTRATED // High deletions = Rage?
            wpm > flowThreshold && deletionRate < 5 -> MoodState.FLOW // High speed, low errors
            // TODO: fatigue detection needs longer window
            else -> MoodState.NEUTRAL
        }
        
        if (newMood != currentMood) {
            currentMood = newMood
            // Trigger intervention if Frustrated and cooling down
            if (currentMood == MoodState.FRUSTRATED) {
                tryTriggerIntervention()
            }
        }
        
        // Log to History every minute
        checkAndLogHistory()
        
        // Print for debugging (visible in console)
        println("Mood Analysis: WPM=$wpm, Deletions=$deletionRate, State=$currentMood")
    }

    private fun checkAndLogHistory() {
        val now = System.currentTimeMillis()
        if (now - lastLogTime > 60 * 1000) { // 1 minute
            lastLogTime = now
            val statsService = project.getService(DailyStatsService::class.java)
            // We log the current minute's WPM and Mood
            statsService.logMinute(currentWpm, currentMood)
        }
    }

    private fun tryTriggerIntervention() {
        val now = System.currentTimeMillis()
        if (now - lastInterventionTime > interventionCooldownMs) {
            lastInterventionTime = now
            
            // UI must be triggered on EDT
            com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                com.antigravity.codemood.ui.BreathingExerciseDialog(project).show()
            }
        }
    }
}
