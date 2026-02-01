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

    // Custom UI Components
    private val wpmCard = InfoCard("WPM", "0")
    private val deletionCard = InfoCard("Del/Min", "0")
    private val moodCard = InfoCard("Mood", "Neutral", highlight = true)
    
    private val graphPanel = WpmGraphPanel()
    
    // Pomodoro
    private var isPomodoroRunning = false
    private var pomodoroTimeLeft = 25 * 60
    private val timerLabel = JLabel("25:00")
    private val timerProgressBar = JProgressBar(0, 25 * 60)
    private val timerButton = JButton("▶ Start Focus")
    
    private val tipLabel = JTextArea("Tip: Get into the flow...")
    private val motivationButton = JButton("✨ AI Wisdom")

    init {
        background = Color(30, 30, 35) // Dark theme background
        border = JBUI.Borders.empty(15)
        
        // --- 1. Header Stats (Cards) ---
        val statsPanel = JPanel(BorderLayout())
        statsPanel.isOpaque = false
        
        val cardsPanel = JPanel(GridLayout(1, 4, 10, 0)) // 4 columns now
        cardsPanel.isOpaque = false
        cardsPanel.add(wpmCard)
        cardsPanel.add(deletionCard)
        cardsPanel.add(moodCard)
        
        // Info Icon
        val infoLabel = JLabel(com.intellij.icons.AllIcons.General.ContextHelp)
        infoLabel.toolTipText = """
            <html>
            <b>Mood Analysis Guide:</b><br>
            ⚡ <b>Flow:</b> High WPM (>1.2x avg) + Low errors.<br>
            😤 <b>Frustrated:</b> Rapid backspacing (Rage Deleting).<br>
            😵 <b>Confused:</b> Rapidly switching tabs (>15/min).<br>
            🚧 <b>Stuck:</b> Syntax errors for >2 minutes.<br>
            🆘 <b>Desperate:</b> Hitting Run/Debug >5 times/min.<br>
            📋 <b>Copying:</b> High paste rate (StackOverflow mode).
            </html>
        """.trimIndent()
        infoLabel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        
        // History Button
        val historyButton = JButton(com.intellij.icons.AllIcons.Vcs.History)
        historyButton.toolTipText = "View Daily Rhythm (0:00 - 24:00)"
        historyButton.border = JBUI.Borders.empty(2)
        historyButton.isContentAreaFilled = false
        historyButton.addActionListener { showHistoryDialog() }

        // Add icons container
        val iconPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 5, 0))
        iconPanel.isOpaque = false
        iconPanel.add(historyButton)
        iconPanel.add(infoLabel)
        
        val infoContainer = JPanel(GridBagLayout())
        infoContainer.isOpaque = false
        infoContainer.add(iconPanel)
        
        cardsPanel.add(infoContainer)

        statsPanel.add(cardsPanel, BorderLayout.CENTER)
        statsPanel.border = JBUI.Borders.emptyBottom(15)
        
        // --- 2. Center Graph ---
        graphPanel.preferredSize = Dimension(0, 180)
        graphPanel.border = BorderFactory.createLineBorder(Color(60, 63, 65))
        
        // --- 3. Bottom Controls ---
        val controlsPanel = Box.createVerticalBox()
        controlsPanel.border = JBUI.Borders.emptyTop(15)
        
        // Pomodoro Section
        val pomoPanel = JPanel(BorderLayout())
        pomoPanel.isOpaque = false
        
        timerLabel.font = Font("Monospaced", Font.BOLD, 28)
        timerLabel.foreground = Color(200, 200, 200)
        timerLabel.horizontalAlignment = SwingConstants.CENTER
        
        timerProgressBar.value = 25 * 60
        timerProgressBar.foreground = Color(0, 255, 255)
        timerProgressBar.preferredSize = Dimension(0, 4)
        
        val timerTop = JPanel(BorderLayout())
        timerTop.isOpaque = false
        timerTop.add(timerLabel, BorderLayout.CENTER)
        timerTop.add(timerButton, BorderLayout.EAST)
        
        pomoPanel.add(timerTop, BorderLayout.NORTH)
        pomoPanel.add(timerProgressBar, BorderLayout.SOUTH)
        
        // AI Section
        val aiPanel = JPanel(BorderLayout())
        aiPanel.isOpaque = false
        aiPanel.border = JBUI.Borders.emptyTop(15)
        
        tipLabel.isOpaque = false
        tipLabel.lineWrap = true
        tipLabel.wrapStyleWord = true
        tipLabel.font = Font("SansSerif", Font.ITALIC, 16) // Increased from 13 to 16
        tipLabel.foreground = Color(180, 180, 190) // Slightly brighter
        tipLabel.border = JBUI.Borders.empty(10) // More padding
        
        aiPanel.add(motivationButton, BorderLayout.WEST)
        aiPanel.add(tipLabel, BorderLayout.CENTER)

        controlsPanel.add(pomoPanel)
        controlsPanel.add(aiPanel)
        
        // --- Assemble ---
        add(statsPanel, BorderLayout.NORTH)
        add(graphPanel, BorderLayout.CENTER)
        add(controlsPanel, BorderLayout.SOUTH)
        
        // Actions
        timerButton.addActionListener { toggleTimer() }
        motivationButton.addActionListener { fetchMotivation() }
        
        // Loop
        Timer(1000) { refreshUI() }.start()
    }
    
    private fun toggleTimer() {
        isPomodoroRunning = !isPomodoroRunning
        if (isPomodoroRunning) {
            timerButton.text = "⏸ Pause"
            timerButton.foreground = Color(255, 100, 100)
        } else {
            timerButton.text = "▶ Start Focus"
            timerButton.foreground = null
        }
    }
    
    private fun refreshUI() {
        val service = project.getService(MoodAnalysisService::class.java) ?: return
        
        // Force service to recalculate decay
        service.refreshState()
        
        // Update Cards
        wpmCard.setValue("${service.currentWpm.toInt()}")
        deletionCard.setValue("${service.currentDeletions}")
        
        // Mood Color Logic
        val mood = service.currentMood
        val moodColor = when(mood.name) {
            "FLOW" -> Color(0, 255, 100)
            "FRUSTRATED", "DESPERATE" -> Color(255, 50, 50)
            "CONFUSED", "STUCK" -> Color(255, 165, 0)
            else -> Color(200, 200, 200)
        }
        moodCard.setValue(mood.name, moodColor)

        // Graph
        graphPanel.addValue(service.currentWpm.toInt())
        
        // Timer
        if (isPomodoroRunning && pomodoroTimeLeft > 0) {
            pomodoroTimeLeft--
            val min = pomodoroTimeLeft / 60
            val sec = pomodoroTimeLeft % 60
            timerLabel.text = String.format("%02d:%02d", min, sec)
            timerProgressBar.value = pomodoroTimeLeft
        }
    }
    
    private fun fetchMotivation() {
        val aiService = project.getService(com.antigravity.codemood.services.OpenAIService::class.java)
        val moodService = project.getService(MoodAnalysisService::class.java)
        
        motivationButton.isEnabled = false
        motivationButton.text = "Thinking..."
        
        aiService.getMotivation(moodService.currentMood.name) { tip ->
            SwingUtilities.invokeLater {
                tipLabel.text = "\"$tip\""
                motivationButton.isEnabled = true
                motivationButton.text = "✨ AI Wisdom"
            }
        }
    }
    
    // Helper UI Component: InfoCard
    private class InfoCard(val title: String, startVal: String, val highlight: Boolean = false) : JPanel() {
        private val valLabel = JLabel(startVal)
        
        init {
            layout = BorderLayout()
            background = Color(40, 42, 45)
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color(60, 60, 65)),
                JBUI.Borders.empty(8)
            )
            
            val titleLabel = JLabel(title)
            titleLabel.font = Font("SansSerif", Font.BOLD, 10)
            titleLabel.foreground = Color(120, 120, 130)
            
            valLabel.font = Font("SansSerif", Font.BOLD, 16)
            valLabel.foreground = Color(220, 220, 220)
            
            add(titleLabel, BorderLayout.NORTH)
            add(valLabel, BorderLayout.CENTER)
        }
        
        fun setValue(txt: String, color: Color? = null) {
            valLabel.text = txt
            if (color != null) valLabel.foreground = color
        }
    }
    
    private fun showHistoryDialog() {
        val statsService = project.getService(com.antigravity.codemood.services.DailyStatsService::class.java)
        val todayStats = statsService.getTodayStats()
        
        val dialog = JDialog(SwingUtilities.getWindowAncestor(this) as? Frame, "Daily Circadian Rhythm", true)
        val content = JPanel(BorderLayout())
        content.background = Color(20, 20, 30)
        
        val chart = CircadianGraphPanel(todayStats)
        chart.preferredSize = Dimension(500, 300)
        
        content.add(chart, BorderLayout.CENTER)
        dialog.contentPane.add(content)
        dialog.pack()
        dialog.setLocationRelativeTo(this)
        dialog.isVisible = true
    }
}
