package com.antigravity.codemood.listeners

import com.antigravity.codemood.services.MoodAnalysisService
import com.intellij.execution.ExecutionListener
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.AnActionResult
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFile

// 1. "The Scroll of Confusion" - Detecting Rapid Tab Switching
class NavigationListener : FileEditorManagerListener {
    override fun selectionChanged(event: FileEditorManagerEvent) {
        val project = event.manager.project
        val service = project.getService(MoodAnalysisService::class.java)
        service?.recordTabSwitch()
    }
}

// 2. "The Debug Loop" - Detecting Execution Frenzy
class ExecutionRunListener : ExecutionListener {
    override fun processStarted(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler) {
        val project = env.project
        val service = project.getService(MoodAnalysisService::class.java)
        service?.recordExecution()
    }
}

// 3. "The Copy-Paste Ratio" - Detecting Pastes
class CopyPasteActionListener : AnActionListener {
    override fun afterActionPerformed(action: AnAction, event: AnActionEvent, result: AnActionResult) {
        val actionId = com.intellij.openapi.actionSystem.ActionManager.getInstance().getId(action)
        
        if (IdeActions.ACTION_PASTE == actionId) {
            val project = event.project ?: ProjectManager.getInstance().openProjects.firstOrNull()
            project?.let {
                val service = it.getService(MoodAnalysisService::class.java)
                service?.recordPaste()
            }
        }
    }
}
