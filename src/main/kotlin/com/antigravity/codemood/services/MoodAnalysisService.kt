package com.antigravity.codemood.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.util.LinkedList

enum class MoodState {
    NEUTRAL,
    FLOW,
    FRUSTRATED,
    TIRED,
    CONFUSED,
    STUCK,
    DESPERATE,
    COPYING
}

@Service(Service.Level.PROJECT)
class MoodAnalysisService(private val project: Project) {

    private val keystrokeTimestamps = LinkedList<Long>()
    private val deletionTimestamps = LinkedList<Long>()
    private val tabSwitchTimestamps = LinkedList<Long>()
    private val executionTimestamps = LinkedList<Long>()
    private val pasteTimestamps = LinkedList<Long>()
    
    private var isStuckInRedZone = false
    
    private val windowSizeMs = 60 * 1000L // 1 minute window
    
    // Cooldown
    private var lastInterventionTime = 0L
    private val interventionCooldownMs = 5 * 60 * 1000L 

    // Public Stats
    var currentWpm: Double = 0.0
        private set
    var currentDeletions: Int = 0
        private set
    var averageWpm: Double = 0.0
        private set

    // Session Stats
    private var totalWpmSamples = 0
    private var cumulativeWpm = 0.0
    
    // Timer for logging
    private var lastLogTime = System.currentTimeMillis()

    var currentMood: MoodState = MoodState.NEUTRAL
        private set

    // --- Inputs ---

    fun recordKeystroke() {
        cleanOldData()
        keystrokeTimestamps.add(System.currentTimeMillis())
        analyzeMood()
    }
    
    fun recordDeletion() {
        cleanOldData()
        deletionTimestamps.add(System.currentTimeMillis())
        analyzeMood()
    }
    
    fun recordTabSwitch() {
        cleanOldData()
        tabSwitchTimestamps.add(System.currentTimeMillis())
        analyzeMood()
    }
    
    fun recordExecution() {
        cleanOldData()
        executionTimestamps.add(System.currentTimeMillis())
        analyzeMood()
    }
    
    fun recordPaste() {
        cleanOldData()
        pasteTimestamps.add(System.currentTimeMillis())
        analyzeMood()
    }
    
    fun recordStuckState(isStuck: Boolean) {
        if (isStuck != isStuckInRedZone) {
            isStuckInRedZone = isStuck
            analyzeMood()
        }
    }
    
    // Called by UI timer to ensure graph decays when idle
    fun refreshState() {
        cleanOldData()
        analyzeMood()
    }

    private fun cleanOldData() {
        val now = System.currentTimeMillis()
        val cutoff = now - windowSizeMs
        removeOld(keystrokeTimestamps, cutoff)
        removeOld(deletionTimestamps, cutoff)
        removeOld(tabSwitchTimestamps, cutoff)
        removeOld(executionTimestamps, cutoff)
        removeOld(pasteTimestamps, cutoff)
    }
    
    private fun removeOld(list: LinkedList<Long>, cutoff: Long) {
        while (list.isNotEmpty() && list.first < cutoff) {
            list.removeFirst()
        }
    }

    private fun analyzeMood() {
        val now = System.currentTimeMillis()
        
        // 1. Calculate Instant WPM (Last 5 seconds) - For UI Graph/Label
        // "Speedometer" feel. Drops to 0 quickly.
        val instantWindow = 5000L
        val instantKeystrokes = keystrokeTimestamps.count { it > now - instantWindow }
        val instantWpmValue = (instantKeystrokes / 5.0) * (60000.0 / instantWindow)
        
        // 2. Calculate Sustained WPM (Last 60 seconds) - For "Mood" Context
        // "Climate" feel. Stable.
        val sustainedKeystrokes = keystrokeTimestamps.size // Since we cleanOldData(60s) beforehand
        val sustainedWpm = (sustainedKeystrokes / 5.0) 
        
        val deletionRate = deletionTimestamps.size
        val tabRate = tabSwitchTimestamps.size
        val execRate = executionTimestamps.size
        val pasteRate = pasteTimestamps.size
        
        // Update public stats (UI sees the Instant one now!)
        currentWpm = instantWpmValue
        currentDeletions = deletionRate
        
        // Update Historical Average (Use sustained to filter noise)
        if (sustainedWpm > 0) {
            totalWpmSamples++
            cumulativeWpm += sustainedWpm
            averageWpm = cumulativeWpm / totalWpmSamples
        }
        
        // Zero Activity Check
        if (keystrokeTimestamps.isEmpty() && tabRate == 0 && execRate == 0) {
            if (isStuckInRedZone) { 
                currentMood = MoodState.STUCK // Silent suffering
            } else {
                currentMood = MoodState.NEUTRAL
            }
            checkAndLogHistory()
            return
        }

        // --- THE "BRAIN" LOGIC (HEURISTICS) ---
        // We use SUSTAINED WPM for mood to avoid flickering "Flow" every time you pause for 2s.
        
        val flowThreshold = if (averageWpm > 10) averageWpm * 1.2 else 40.0
        
        val newMood = when {
            execRate > 5 -> MoodState.DESPERATE // "The Debug Loop"
            isStuckInRedZone -> MoodState.STUCK // "The Red Zone"
            deletionRate > 20 -> MoodState.FRUSTRATED // "Rage Typing"
            tabRate > 15 -> MoodState.CONFUSED // "The Scroll of Confusion"
            pasteRate > 3 -> MoodState.COPYING // "The Copy-Paste Ratio"
            sustainedWpm > flowThreshold && deletionRate < 5 -> MoodState.FLOW 
            else -> MoodState.NEUTRAL
        }
        
        if (newMood != currentMood) {
            currentMood = newMood
            
            // Trigger specific interventions
            if (currentMood == MoodState.FRUSTRATED || currentMood == MoodState.DESPERATE) {
                tryTriggerIntervention()
            }
        }
        
        checkAndLogHistory()
        // Debug: Print both WPMs
        // println("Mood: $currentMood (InstantWPM: ${instantWpmValue.toInt()}, Sustained: ${sustainedWpm.toInt()})")
    }

    private fun checkAndLogHistory() {
        val now = System.currentTimeMillis()
        if (now - lastLogTime > 60 * 1000) { 
            lastLogTime = now
            val statsService = project.getService(DailyStatsService::class.java)
            statsService.logMinute(currentWpm, currentMood)
        }
    }

    private fun tryTriggerIntervention() {
        val now = System.currentTimeMillis()
        if (now - lastInterventionTime > interventionCooldownMs) {
            lastInterventionTime = now
            com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                com.antigravity.codemood.ui.BreathingExerciseDialog(project).show()
            }
        }
    }
}
