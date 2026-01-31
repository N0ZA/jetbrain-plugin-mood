package com.antigravity.codemood.ui

import com.antigravity.codemood.services.MoodAnalysisService
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.*

class CodeMoodToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val content = ContentFactory.getInstance().createContent(CodeMoodDashboard(project), "", false)
        toolWindow.contentManager.addContent(content)
    }
}

class CodeMoodDashboard(private val project: Project) : JPanel(BorderLayout()) {

    private val wpmLabel = JLabel("WPM: 0")
    private val deletionLabel = JLabel("Deletions: 0")
    private val avgWpmLabel = JLabel("Avg WPM: 0")
    private val moodLabel = JLabel("Mood: Neutral")
    private val tipArea = JTextArea("Tip: Keep going! Flow state is near.")
    
    // Graph Panel
    private val graphPanel = WpmGraphPanel()
    
    // Pomodoro
    private var isPomodoroRunning = false
    private var pomodoroTimeLeft = 25 * 60
    private val timerLabel = JLabel("25:00", SwingConstants.CENTER)
    private val timerButton = JButton("Start Focus")
    private val motivationButton = JButton("Get AI Motivation 🤖")

    init {
        border = JBUI.Borders.empty(15)
        
        // 1. Top Panel: Stats
        val statsPanel = JPanel(GridLayout(2, 2, 10, 10))
        statsPanel.add(wpmLabel)
        statsPanel.add(deletionLabel)
        statsPanel.add(avgWpmLabel)
        statsPanel.add(moodLabel)
        
        // 2. Center: Graph
        graphPanel.preferredSize = Dimension(300, 150)
        graphPanel.border = BorderFactory.createTitledBorder("Focus Graph (WPM)")
        
        // 3. Bottom: Pomodoro & Tips
        val bottomPanel = JPanel(BorderLayout())
        
        val timerPanel = JPanel(BorderLayout())
        timerLabel.font = timerLabel.font.deriveFont(24f) // Big font
        timerButton.addActionListener { toggleTimer() }
        
        timerPanel.add(timerLabel, BorderLayout.CENTER)
        timerPanel.add(timerButton, BorderLayout.SOUTH)
        timerPanel.border = JBUI.Borders.empty(10)
        
        val tipsPanel = JPanel(BorderLayout())
        tipsPanel.add(JScrollPane(tipArea), BorderLayout.CENTER)
        tipsPanel.add(motivationButton, BorderLayout.SOUTH)
        
        tipArea.isEditable = false
        tipArea.lineWrap = true
        tipArea.wrapStyleWord = true
        tipArea.background = this.background
        tipArea.font = tipArea.font.deriveFont(Font.ITALIC)
        tipArea.border = BorderFactory.createTitledBorder("AI Motivation")
        
        motivationButton.addActionListener { fetchMotivation() }
        
        bottomPanel.add(timerPanel, BorderLayout.WEST)
        bottomPanel.add(tipsPanel, BorderLayout.CENTER)

        add(statsPanel, BorderLayout.NORTH)
        add(graphPanel, BorderLayout.CENTER)
        add(bottomPanel, BorderLayout.SOUTH)
        
        // Update Loop (1s)
        Timer(1000) { refreshUI() }.start()
    }
    
    private fun toggleTimer() {
        isPomodoroRunning = !isPomodoroRunning
        timerButton.text = if (isPomodoroRunning) "Pause" else "Start Focus"
    }
    
    private fun refreshUI() {
        // 1. Fetch Data
        val service = project.getService(MoodAnalysisService::class.java) ?: return
        
        // 2. Update Stats
        wpmLabel.text = "WPM: ${service.currentWpm.toInt()}"
        deletionLabel.text = "Del/min: ${service.currentDeletions}"
        avgWpmLabel.text = "Avg WPM: ${service.averageWpm.toInt()}"
        moodLabel.text = "Mood: ${service.currentMood}"
        
        // 3. Update Graph
        graphPanel.addValue(service.currentWpm.toInt())
        
        // 4. Update Pomodoro
        if (isPomodoroRunning && pomodoroTimeLeft > 0) {
            pomodoroTimeLeft--
            val min = pomodoroTimeLeft / 60
            val sec = pomodoroTimeLeft % 60
            timerLabel.text = String.format("%02d:%02d", min, sec)
        }
    }
    
    private fun fetchMotivation() {
        val aiService = project.getService(com.antigravity.codemood.services.OpenAIService::class.java)
        val moodService = project.getService(MoodAnalysisService::class.java)
        
        motivationButton.isEnabled = false
        motivationButton.text = "Thinking..."
        
        aiService.getMotivation(moodService.currentMood.name) { tip ->
            // Swing UI update must happen on EDT
            SwingUtilities.invokeLater {
                tipArea.text = tip
                motivationButton.isEnabled = true
                motivationButton.text = "Get AI Motivation 🤖"
            }
        }
    }
}
