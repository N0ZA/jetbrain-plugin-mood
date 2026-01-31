package com.antigravity.codemood.ui

import com.antigravity.codemood.services.MoodAnalysisService
import com.antigravity.codemood.services.MoodState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.util.Consumer
import java.awt.event.MouseEvent
import javax.swing.Timer

class MoodStatusBarWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = "MoodStatusBarWidget"
    override fun getDisplayName(): String = "Mood Monitor"
    override fun isAvailable(project: Project): Boolean = true
    override fun createWidget(project: Project): StatusBarWidget = MoodStatusBarWidget(project)
    override fun disposeWidget(widget: StatusBarWidget) = Disposer.dispose(widget)
    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}

class MoodStatusBarWidget(private val project: Project) : StatusBarWidget, StatusBarWidget.TextPresentation {
    private var statusBar: StatusBar? = null
    
    // Timer to update the widget UI periodically
    private val timer = Timer(1000) { 
        statusBar?.updateWidget(ID())
    }

    init {
        timer.start()
    }

    override fun ID(): String = "MoodStatusBarWidget"
    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this
    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
    }
    override fun dispose() {
        timer.stop()
        statusBar = null
    }

    // TextPresentation methods
    override fun getText(): String {
        val service = project.getService(MoodAnalysisService::class.java) ?: return "Mood: ?"
        val mood = service.currentMood
        return when (mood) {
            MoodState.FLOW -> "Mood: ⚡ Flow"
            MoodState.FRUSTRATED -> "Mood: 😤 Frustrated"
            MoodState.TIRED -> "Mood: 😴 Tired"
            MoodState.NEUTRAL -> "Mood: 😐 Neutral"
        }
    }

    override fun getTooltipText(): String = "Current Mental State based on coding patterns"
    override fun getAlignment(): Float = 0.5f
    override fun getClickConsumer(): Consumer<MouseEvent>? = null
}
